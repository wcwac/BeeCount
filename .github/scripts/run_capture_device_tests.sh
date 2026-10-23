#!/usr/bin/env bash
# Cloud-only, explicit opt-in workflow helper. Do not run locally.
set -euo pipefail

: "${GITHUB_ACTIONS:?This helper is only prepared for GitHub Actions}"
[[ "$GITHUB_ACTIONS" == true ]] || exit 1
[[ "${GITHUB_EVENT_NAME:-}" == workflow_dispatch ]] || {
  echo "Only an explicitly authorized manual workflow may run device tests"
  exit 1
}
[[ "${CAPTURE_DEVICE_TESTS_OPT_IN:-false}" == true ]] || {
  echo "Explicit device-test opt-in is required"
  exit 1
}
: "${GITHUB_WORKSPACE:?}"
: "${RUNNER_TEMP:?}"
: "${ANDROID_HOME:?}"
: "${CAPTURE_SYSTEM_IMAGE:?}"
: "${CAPTURE_IMAGE_REVISION:?}"
: "${CAPTURE_EMULATOR_REVISION:?}"
: "${CAPTURE_PLATFORM_TOOLS_REVISION:?}"

capture_logs="$RUNNER_TEMP/capture-device"
capture_avd="beecount_capture_api36"
capture_serial="emulator-5554"
capture_adb="$ANDROID_HOME/platform-tools/adb"
capture_emulator="$ANDROID_HOME/emulator/emulator"
capture_avdmanager="$ANDROID_HOME/cmdline-tools/latest/bin/avdmanager"
capture_pid=""
mkdir -p "$capture_logs"
export ANDROID_AVD_HOME="$RUNNER_TEMP/capture-avd"
export ANDROID_SERIAL="$capture_serial"
mkdir -p "$ANDROID_AVD_HOME"

check_revision() {
  local capture_properties="$1" capture_expected="$2" capture_actual
  capture_actual="$(sed -n 's/^Pkg.Revision[[:space:]]*=[[:space:]]*//p' "$capture_properties" | tr -d '\r')"
  [[ "$capture_actual" == "$capture_expected" ]] || {
    echo "::error::Unreviewed SDK revision at $capture_properties: $capture_actual (expected $capture_expected)"
    exit 1
  }
}
check_revision "$ANDROID_HOME/emulator/source.properties" "$CAPTURE_EMULATOR_REVISION"
check_revision "$ANDROID_HOME/platform-tools/source.properties" "$CAPTURE_PLATFORM_TOOLS_REVISION"
check_revision "$ANDROID_HOME/system-images/android-36/default/x86_64/source.properties" "$CAPTURE_IMAGE_REVISION"
[[ "$CAPTURE_SYSTEM_IMAGE" == 'system-images;android-36;default;x86_64' ]]

cleanup() {
  local capture_status=$?
  trap - EXIT INT TERM
  if [[ -n "$capture_pid" ]]; then
    timeout 10s "$capture_adb" -s "$capture_serial" logcat -d -v threadtime -t 2000 > "$capture_logs/logcat.txt" 2>&1 || true
    timeout 10s "$capture_adb" -s "$capture_serial" emu kill > "$capture_logs/emulator-stop.txt" 2>&1 || true
    kill "$capture_pid" 2>/dev/null || true
    for capture_attempt in {1..10}; do
      kill -0 "$capture_pid" 2>/dev/null || break
      sleep 1
    done
    kill -KILL "$capture_pid" 2>/dev/null || true
    wait "$capture_pid" 2>/dev/null || true
  fi
  timeout 20s "$capture_avdmanager" delete avd --name "$capture_avd" > "$capture_logs/avd-delete.txt" 2>&1 || true
  exit "$capture_status"
}
trap cleanup EXIT
trap 'exit 130' INT
trap 'exit 143' TERM

timeout 15s "$capture_emulator" -accel-check > "$capture_logs/kvm.txt" 2>&1
timeout 60s "$capture_avdmanager" create avd --name "$capture_avd" \
  --package "$CAPTURE_SYSTEM_IMAGE" --device pixel_6 --force \
  > "$capture_logs/avd-create.txt" 2>&1 <<<'no'

"$capture_emulator" -avd "$capture_avd" -port 5554 -no-window -noaudio \
  -no-boot-anim -no-snapshot -wipe-data -accel on -gpu swiftshader \
  -cores 2 -memory 2048 > "$capture_logs/emulator.txt" 2>&1 &
capture_pid=$!
capture_deadline=$((SECONDS + 180))
capture_booted=false
while (( SECONDS < capture_deadline )); do
  kill -0 "$capture_pid" 2>/dev/null || { echo "::error::Emulator exited before boot"; exit 1; }
  if [[ "$(timeout 5s "$capture_adb" -s "$capture_serial" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r' || true)" == 1 ]]; then
    capture_booted=true
    break
  fi
  sleep 2
done
[[ "$capture_booted" == true ]] || { echo "::error::Emulator did not boot within 180 seconds"; exit 1; }
timeout 10s "$capture_adb" devices -l > "$capture_logs/devices.txt"
mapfile -t capture_devices < <(awk 'NR > 1 && NF { print $1 " " $2 }' "$capture_logs/devices.txt")
[[ "${#capture_devices[@]}" == 1 && "${capture_devices[0]}" == "$capture_serial device" ]] || {
  echo "::error::Expected only the disposable emulator; refusing to run on other devices"
  exit 1
}
timeout 10s "$capture_adb" -s "$capture_serial" shell getprop > "$capture_logs/device-properties.txt"
[[ "$(timeout 10s "$capture_adb" -s "$capture_serial" shell getprop ro.build.version.sdk | tr -d '\r')" == 36 ]]
[[ "$(timeout 10s "$capture_adb" -s "$capture_serial" shell getprop ro.product.cpu.abi | tr -d '\r')" == x86_64 ]]

# Reuse verified artifacts; compilation is intentionally confined to the source run.
python3 - <<'PY_APKS'
from pathlib import Path
import os,subprocess
root=Path(os.environ['GITHUB_WORKSPACE'])/'cloud-apks'
analyzer=Path(os.environ['ANDROID_HOME'])/'cmdline-tools/latest/bin/apkanalyzer'
adb=Path(os.environ['ANDROID_HOME'])/'platform-tools/adb'
expected={'io.github.wcwac.yimu.reference.debug','io.github.wcwac.yimu.reference.debug.test','com.tencent.mm','com.taobao.taobao','io.github.wcwac.paymentfixture.ignored'}
found={}
for apk in root.rglob('*.apk'):
    package=subprocess.check_output([str(analyzer),'manifest','application-id',str(apk)],text=True).strip()
    assert package in expected and package not in found, 'Unexpected/duplicate project test APK: '+package
    found[package]=apk
assert set(found)==expected, 'Missing project test APKs'
for package,apk in found.items():
    subprocess.run([str(adb),'-s','emulator-5554','install','-r','-t',str(apk)],check=True,timeout=90)
PY_APKS

timeout --signal=TERM --kill-after=20s 15m \
  "$capture_adb" -s "$capture_serial" shell am instrument -w -r \
  -e reference_cloud_only true -e timeout_msec 180000 \
  io.github.wcwac.yimu.reference.debug.test/androidx.test.runner.AndroidJUnitRunner \
  | tee "$capture_logs/instrumentation.txt"
python3 - <<'PY_RESULTS'
from pathlib import Path
import os,re
text=(Path(os.environ['RUNNER_TEMP'])/'capture-device/instrumentation.txt').read_text()
assert not any(s in text for s in ['FAILURES!!!','INSTRUMENTATION_FAILED','Process crashed','INSTRUMENTATION_RESULT: shortMsg']), 'Device tests failed or crashed'
match=re.search(r'OK \(([0-9]+) tests?\)',text)
assert match and int(match[1])>=18, 'Required device tests were not discovered or did not all pass'
print('Device tests verified:',match[1])
PY_RESULTS
