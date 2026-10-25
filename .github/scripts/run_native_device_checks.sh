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
capture_logcat_pid=""
capture_logcat_remote="/data/local/tmp/beecount-capture-logs"
capture_evidence_attempted=false
capture_evidence_status=1
capture_evidence_remote="/sdcard/Android/data/io.github.wcwac.beecount.nativeapp.debug/files/test-evidence"
capture_evidence_folder="$capture_logs/screen-evidence"
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

collect_screen_evidence() {
  if [[ "$capture_evidence_attempted" == true ]]; then
    return "$capture_evidence_status"
  fi
  capture_evidence_attempted=true
  if timeout 30s "$capture_adb" -s "$capture_serial" pull "$capture_evidence_remote" "$capture_evidence_folder" \
      > "$capture_logs/screen-evidence-pull.txt" 2>&1; then
    capture_evidence_status=0
    return 0
  fi
  # Android can restrict shell access to app-specific external storage. The tested
  # debug app can read its own files; copy only its data, never execute an artifact.
  if timeout 30s "$capture_adb" -s "$capture_serial" exec-out run-as io.github.wcwac.beecount.nativeapp.debug \
      tar -C "$capture_evidence_remote" -cf - . > "$capture_logs/screen-evidence.tar" \
      2> "$capture_logs/screen-evidence-run-as.txt"; then
    if python3 .github/scripts/native_checks.py --extract "$capture_logs/screen-evidence.tar" \
        --destination "$capture_evidence_folder"; then
      capture_evidence_status=0
      return 0
    fi
  fi
  return 1
}

cleanup() {
  local capture_status=$?
  trap - EXIT INT TERM
  if [[ -n "$capture_pid" ]]; then
    collect_screen_evidence || true
    if [[ -n "$capture_logcat_pid" ]]; then
      kill "$capture_logcat_pid" 2>/dev/null || true
      for capture_attempt in {1..10}; do
        kill -0 "$capture_logcat_pid" 2>/dev/null || break
        sleep 0.1
      done
      kill -KILL "$capture_logcat_pid" 2>/dev/null || true
      wait "$capture_logcat_pid" 2>/dev/null || true
      timeout 20s "$capture_adb" -s "$capture_serial" pull "$capture_logcat_remote" "$capture_logs/logcat-rolling" \
        > "$capture_logs/logcat-pull.txt" 2>&1 || true
    fi
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

# Keep the failure-time framework exception even when later cases overwrite the
# emulator's ring buffer. Five 4MiB files cap this at 20MiB on this wiped emulator.
# This starts only after the one-device/API/ABI checks; cleanup stops the stream,
# retrieves data and destroys the same emulator on every exit path.
if timeout 10s "$capture_adb" -s "$capture_serial" shell mkdir -p "$capture_logcat_remote"; then
  "$capture_adb" -s "$capture_serial" shell logcat -b main -b system -b crash -v threadtime \
    -f "$capture_logcat_remote/logcat.txt" -r 4096 -n 4 \
    > "$capture_logs/logcat-stream.txt" 2>&1 &
  capture_logcat_pid=$!
fi

# Use only APKs staged by the build step in this workflow run.
python3 - <<'PY_APKS'
from pathlib import Path
import os,subprocess
root=Path(os.environ['GITHUB_WORKSPACE'])/'cloud-apks'
analyzer=Path(os.environ['ANDROID_HOME'])/'cmdline-tools/latest/bin/apkanalyzer'
adb=Path(os.environ['ANDROID_HOME'])/'platform-tools/adb'
expected={'io.github.wcwac.beecount.nativeapp.debug','io.github.wcwac.beecount.nativeapp.debug.test','com.tencent.mm','com.taobao.taobao','com.eg.android.AlipayGphone','com.sankuai.meituan','io.github.wcwac.paymentfixture.ignored'}
found={}
for apk in root.rglob('*.apk'):
    package=subprocess.check_output([str(analyzer),'manifest','application-id',str(apk)],text=True).strip()
    assert package in expected and package not in found, 'Unexpected/duplicate project test APK: '+package
    found[package]=apk
assert set(found)==expected, 'Missing project test APKs'
for package,apk in found.items():
    subprocess.run([str(adb),'-s','emulator-5554','install','-r','-t',str(apk)],check=True,timeout=90)
PY_APKS

capture_instrument_status=0
: > "$capture_logs/instrumentation.txt"
: > "$capture_logs/instrumentation-groups.tsv"
# Isolate runner crashes by class while keeping the same APKs and emulator.
# No group is retried; all named cases must still pass exactly once.
for capture_group in financial screen capture; do
  case "$capture_group" in
    financial) capture_class=NativeFinancialDeviceTest; capture_case_count=8 ;;
    screen) capture_class=NativeScreenDeviceTest; capture_case_count=2 ;;
    capture) capture_class=NativeCaptureDeviceTest; capture_case_count=2 ;;
  esac
  capture_group_status=0
  timeout 15s "$capture_adb" -s "$capture_serial" shell am force-stop io.github.wcwac.beecount.nativeapp.debug \
    > "$capture_logs/$capture_group-reset.txt" 2>&1 || capture_group_status=$?
  timeout 10s "$capture_adb" -s "$capture_serial" logcat -c \
    > "$capture_logs/$capture_group-logcat-clear.txt" 2>&1 || true
  timeout --signal=TERM --kill-after=20s 10m \
    "$capture_adb" -s "$capture_serial" shell am instrument -w -r \
    -e class "io.github.wcwac.beecount.nativeapp.$capture_class" \
    -e native_cloud_only true -e timeout_msec 180000 \
    io.github.wcwac.beecount.nativeapp.debug.test/androidx.test.runner.AndroidJUnitRunner \
    < /dev/null 2>&1 | tee "$capture_logs/instrumentation-$capture_group.txt" || capture_group_status=$?
  printf '%s\t%s\t%s\n' "$capture_group" "$capture_case_count" "$capture_group_status" >> "$capture_logs/instrumentation-groups.tsv"
  printf '# instrumentation group: %s\n' "$capture_group" >> "$capture_logs/instrumentation.txt"
  cat "$capture_logs/instrumentation-$capture_group.txt" >> "$capture_logs/instrumentation.txt"
  printf '\n' >> "$capture_logs/instrumentation.txt"
  timeout 20s "$capture_adb" -s "$capture_serial" logcat -b main -b system -b crash -d -v threadtime \
    > "$capture_logs/$capture_group-logcat.txt" 2>&1 || true
  if [[ "$capture_group_status" != 0 ]]; then capture_instrument_status=1; fi
done
collect_screen_evidence || capture_evidence_status=$?
capture_device_validation_status=0
python3 .github/scripts/native_checks.py --device || capture_device_validation_status=$?

# Verify the exact shrunk release artifact, without rebuilding or modifying it.
# A failed instrumentation group must not prevent independent release evidence.
# This same temporary emulator remains alive until EXIT cleanup.
capture_release_status=0
if [[ "${SIGN_APK:-false}" == true && "${NATIVE_RELEASE_VERIFIED:-false}" == true \
    && -f "$RUNNER_TEMP/native-release-verified.sha256" ]] \
    && sha256sum --check --status "$RUNNER_TEMP/native-release-verified.sha256"; then
  capture_release_apk="build/app/outputs/apk/release/app-release.apk"
  if timeout 90s "$capture_adb" -s "$capture_serial" install -r "$capture_release_apk" \
      > "$capture_logs/release-install.txt" 2>&1; then
    timeout 15s "$capture_adb" -s "$capture_serial" shell am force-stop io.github.wcwac.beecount.nativeapp.debug || capture_release_status=1
    timeout 15s "$capture_adb" -s "$capture_serial" logcat -c || capture_release_status=1
    timeout 30s "$capture_adb" -s "$capture_serial" shell am start -W -n io.github.wcwac.beecount.nativeapp/.MainActivity \
      > "$capture_logs/release-start.txt" 2>&1 || capture_release_status=1
    capture_home_ready=false
    for capture_attempt in {1..20}; do
      timeout 15s "$capture_adb" -s "$capture_serial" shell uiautomator dump /data/local/tmp/native-release-ui.xml >/dev/null 2>&1 || true
      timeout 10s "$capture_adb" -s "$capture_serial" exec-out cat /data/local/tmp/native-release-ui.xml > "$capture_logs/release-ui.xml" 2> "$capture_logs/release-ui-read.txt" || true
      if python3 -c 'import sys,xml.etree.ElementTree as E; r=E.parse(sys.argv[1]); sys.exit(0 if any(n.get("text")=="全部流水" for n in r.iter("node")) else 1)' "$capture_logs/release-ui.xml" 2>/dev/null; then capture_home_ready=true; break; fi
      sleep 1
    done
    # Collect the crash/initialization failure evidence BEFORE reporting failure.
    timeout 15s "$capture_adb" -s "$capture_serial" logcat -b main -b system -b crash -d -v threadtime > "$capture_logs/release-logcat.txt" 2>&1 || capture_release_status=1
    timeout 10s "$capture_adb" -s "$capture_serial" shell pidof io.github.wcwac.beecount.nativeapp > "$capture_logs/release-pid.txt" 2> "$capture_logs/release-pid-error.txt" || capture_release_status=1
    timeout 10s "$capture_adb" -s "$capture_serial" shell dumpsys activity activities > "$capture_logs/release-activities.txt" 2>&1 || capture_release_status=1
    timeout 10s "$capture_adb" -s "$capture_serial" shell dumpsys meminfo io.github.wcwac.beecount.nativeapp > "$capture_logs/release-memory.txt" 2>&1 || capture_release_status=1
    timeout 10s "$capture_adb" -s "$capture_serial" shell dumpsys gfxinfo io.github.wcwac.beecount.nativeapp framestats > "$capture_logs/release-frames.txt" 2>&1 || capture_release_status=1
    timeout 10s "$capture_adb" -s "$capture_serial" exec-out screencap -p > "$capture_logs/release-home.png" 2> "$capture_logs/release-screen-error.txt" || capture_release_status=1
    printf '%s\n' "$capture_home_ready" > "$capture_logs/release-home-ready.txt"
    if [[ "$capture_home_ready" != true ]]; then echo "::error::Release home did not finish initialization; runtime evidence retained"; capture_release_status=1; fi
  else
    capture_release_status=1
  fi
else
  echo "Release signature/provenance was not verified or the APK changed after verification" > "$capture_logs/release-blocked.txt"
  capture_release_status=1
fi
printf '%s\n' "$capture_release_status" > "$capture_logs/release-command-status.txt"
[[ "$capture_instrument_status" == 0 && "$capture_evidence_status" == 0 \
  && "$capture_device_validation_status" == 0 && "$capture_release_status" == 0 ]]
