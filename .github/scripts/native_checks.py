"""Native branch boundaries and evidence validation. No project code is imported."""
import argparse
from collections import Counter
import hashlib
import json
import os
from pathlib import Path, PurePosixPath
import re
import shutil
import struct
import tarfile
import tomllib
import xml.etree.ElementTree as ET
import zipfile

ROOT = Path(__file__).resolve().parents[2]
PKG = 'io.github.wcwac.beecount.nativeapp'
ANDROID = '{http://schemas.android.com/apk/res/android}'
INVENTORY = ROOT / '.github/native-cases.json'


def source():
    settings=(ROOT/'android/settings.gradle').read_text()
    assert settings.index('plugins {') < settings.index('dependencyResolutionManagement {'), 'Gradle plugin block must precede dependency resolution statements'
    manifest = ET.parse(ROOT / 'android/app/src/native/AndroidManifest.xml').getroot()
    assert manifest.find('application').get(ANDROID + 'name') == '.NativeApplication'
    for name in ('app/src/native', 'capture/src/main', 'data/src/main', 'domain/src/main', 'ui/src/main'):
        folder = ROOT / 'android' / name
        assert not list(folder.rglob('*.java')), 'Production Kotlin boundary: ' + name
        assert not [p for p in folder.rglob('*') if p.suffix.lower() in {'.so', '.dex', '.apk', '.tflite', '.pt', '.onnx'}]
        for path in folder.rglob('*.kt'):
            text = path.read_text()
            assert not re.search(r'\b(System\.loadLibrary|DexClassLoader|Runtime\.getRuntime\(\)\.exec|ProcessBuilder)\b', text), path
    inventory = json.loads((ROOT / 'android/capture/rule-transcription-inventory.json').read_text())
    assert len(inventory['rules']) == 15
    assert sum(row['methods'] for row in inventory['rules']) == 167
    for file in (ROOT/'android').rglob('*.gradle'):
        if '/build/' in str(file): continue
        assert not re.search(r"['\"][^'\"\n]+:\+['\"]", file.read_text()), 'Dynamic dependency: ' + str(file)
    print('Kotlin, offline payload and fixed dependency source boundaries checked')


def datastore_native_evidence(archive, native):
    # These binaries are part of the pinned AndroidX preference dependency, not
    # recovered application payloads. Verify bytes against the resolved AAR; the
    # build reports that these already-built libraries are packaged without stripping.
    version = tomllib.loads((ROOT/'android/gradle/libs.versions.toml').read_text())['versions']['datastore']
    assert version == '1.1.7', 'Review DataStore native provenance before changing its pinned version'
    approved_aar = 'adca1d7cde73406fcca2a0eeabac63459adc9d1fe201b79ba08711fa2e331984'
    cache = Path(os.environ.get('GRADLE_USER_HOME', str(Path.home()/'.gradle')))/'caches/modules-2/files-2.1/androidx.datastore'
    sources = {}
    for module in ('datastore-core-android',):
        for artifact in sorted((cache/module/version).glob('*/*.aar')):
            artifact_digest = hashlib.sha256(artifact.read_bytes()).hexdigest()
            assert artifact_digest == approved_aar, 'DataStore AAR differs from the reviewed 1.1.7 artifact'
            with zipfile.ZipFile(artifact) as dependency:
                for name in dependency.namelist():
                    if re.fullmatch(r'jni/(arm64-v8a|armeabi-v7a|x86|x86_64)/libdatastore_shared_counter\.so', name):
                        key = name.removeprefix('jni/')
                        digest = hashlib.sha256(dependency.read(name)).hexdigest()
                        sources.setdefault(key, []).append({'coordinate': 'androidx.datastore:'+module+':'+version, 'aarSha256': artifact_digest, 'sha256': digest})
    evidence = []
    for name in native:
        if Path(name).name != 'libdatastore_shared_counter.so': continue
        digest = hashlib.sha256(archive.read(name)).hexdigest()
        matched = [source for source in sources.get(name.removeprefix('lib/'), []) if source['sha256'] == digest]
        assert matched, 'DataStore native library differs from the pinned AndroidX AAR: '+name
        evidence.append({'apkEntry': name, **matched[0]})
    destination = ROOT/'build/native-audit/reports'
    destination.mkdir(parents=True, exist_ok=True)
    (destination/'datastore-native-provenance.json').write_text(json.dumps(evidence, indent=2)+'\n')
    return evidence


def packaged(manifest, apk):
    root = ET.parse(manifest).getroot()
    assert root.get('package') == PKG
    permissions = {node.get(ANDROID+'name') for node in root.findall('uses-permission')}
    allowed = {'android.permission.SYSTEM_ALERT_WINDOW','android.permission.POST_NOTIFICATIONS','android.permission.RECEIVE_BOOT_COMPLETED','android.permission.USE_BIOMETRIC','android.permission.WAKE_LOCK','android.permission.ACCESS_NETWORK_STATE','android.permission.FOREGROUND_SERVICE',PKG+'.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION'}
    # WorkManager's generic merger may contribute network state; app explicitly removes it.
    assert 'android.permission.INTERNET' not in permissions
    assert 'android.permission.ACCESS_NETWORK_STATE' not in permissions
    assert permissions <= allowed, 'Unexpected runtime permission: '+str(permissions-allowed)
    app=root.find('application')
    assert app.get(ANDROID+'debuggable','false') == 'false'
    assert app.get(ANDROID+'allowBackup') == 'false'
    assert any('NativeCaptureService' in node.get(ANDROID+'name','') for node in app.findall('service'))
    with zipfile.ZipFile(apk) as archive:
        names=archive.namelist()
        assert len(names)==len(set(names))
        assert not any(re.search(r'(^|/)(flutter_assets|libflutter\.so|libapp\.so)|\.(tflite|onnx|pt)$', name) for name in names)
        native = [name for name in names if name.startswith('lib/') and name.endswith('.so')]
        assert all(Path(name).name in {'libandroidx.graphics.path.so', 'libdatastore_shared_counter.so'} for name in native), 'Unreviewed native runtime payload: '+str(native)
        print('AndroidX native payload:',native)
        print('DataStore AAR byte matches:', len(datastore_native_evidence(archive, native)))
    print('Release package, permissions, payload and backup boundaries checked:',hashlib.sha256(Path(apk).read_bytes()).hexdigest())


def unit():
    required=set(json.loads(INVENTORY.read_text())['unit'])
    seen=Counter()
    for report in (ROOT/'build').glob('*/test-results/**/TEST-*.xml'):
        for case in ET.parse(report).getroot().iter('testcase'):
            name=case.get('classname')+'.'+case.get('name')
            assert not any(case.find(tag) is not None for tag in ('failure','error','skipped')), 'Failed/skipped '+name
            seen[name]+=1
    assert set(seen)==required and all(n==1 for n in seen.values()), 'Unit inventory mismatch: missing='+str(required-set(seen))+' unexpected='+str(set(seen)-required)
    print('Required unit assertions passed:',len(seen))


def png(path):
    raw=path.read_bytes()
    assert 32<=len(raw)<=16*1024*1024 and raw[:8]==b'\x89PNG\r\n\x1a\n', 'Invalid PNG: '+str(path)
    width,height=struct.unpack('>II',raw[16:24])
    assert 200<=min(width,height)<=max(width,height)<=4096
    return {'file':path.name,'width':width,'height':height,'sha256':hashlib.sha256(raw).hexdigest()}


def extract(archive,destination):
    destination.mkdir(parents=True,exist_ok=True)
    assert not destination.is_symlink()
    total=0; seen=set()
    with tarfile.open(archive,'r:') as stream:
        for entry in stream:
            path=PurePosixPath(entry.name)
            assert not path.is_absolute() and '..' not in path.parts
            if entry.isdir():
                assert path.as_posix()=='.'; continue
            assert entry.isfile() and len(path.parts)==1 and path.suffix=='.png'
            assert path.name not in seen and len(seen)<16; seen.add(path.name)
            assert 0<entry.size<=16*1024*1024
            total+=entry.size; assert total<=128*1024*1024
            target=destination/path.name
            assert not target.exists() and not target.is_symlink()
            with stream.extractfile(entry) as source, target.open('xb') as output: shutil.copyfileobj(source,output)


def instrumentation_group(text, required, count):
    started=Counter(); passed=Counter(); case={}; errors=[]
    if any(word in text for word in ('FAILURES!!!','INSTRUMENTATION_FAILED','INSTRUMENTATION_ABORTED','Process crashed','INSTRUMENTATION_RESULT: shortMsg')):
        errors.append('Runner reported a failure/crash')
    for line in text.splitlines():
        field=re.fullmatch(r'INSTRUMENTATION_STATUS: (class|test)=(.*)',line.strip())
        if field:case[field[1]]=field[2]
        status=re.fullmatch(r'INSTRUMENTATION_STATUS_CODE: (-?\d+)',line.strip())
        if not status:continue
        if 'class' in case and 'test' in case:
            name=case['class']+'.'+case['test']; code=int(status[1])
            if code in (0,1): (started if code==1 else passed)[name]+=1
            else: errors.append(name+' failed/skipped (status '+str(code)+')')
        case={}
    if set(started)!=required or set(passed)!=required or any(n!=1 for n in list(started.values())+list(passed.values())):
        errors.append('Named case inventory mismatch: missing='+str(sorted(required-set(passed)))+' unexpected='+str(sorted((set(started)|set(passed))-required)))
    ok=[int(v) for v in re.findall(r'OK \(([0-9]+) tests?\)',text)]
    if ok!=[count]: errors.append('Expected exactly one OK ('+str(count)+' tests), found '+str(ok))
    return {'started':dict(started),'passed':dict(passed),'errors':errors}


def device():
    folder=Path(os.environ['RUNNER_TEMP'])/'capture-device'
    spec=json.loads(INVENTORY.read_text()); required=set(spec['device'])
    groups=(('financial','NativeFinancialDeviceTest',8),('screen','NativeScreenDeviceTest',2),('capture','NativeCaptureDeviceTest',2))
    assert len(required)==len(spec['device'])==12, 'The approved device inventory must contain exactly 12 distinct cases'
    errors=[]; results=[]; combined=[]; grouped_cases=set()
    for name, class_name, count in groups:
        group_required={case for case in required if case.startswith(PKG+'.'+class_name+'.')}
        assert len(group_required)==count, 'Device class count changed: '+class_name
        grouped_cases.update(group_required)
        path=folder/('instrumentation-'+name+'.txt')
        text=path.read_text() if path.is_file() else ''
        result=instrumentation_group(text,group_required,count)
        results.append({'group':name,'expected':count,**result})
        errors.extend(name+': '+error for error in result['errors'])
        combined.append('# instrumentation group: '+name+'\n'+text+'\n')
    assert grouped_cases==required, 'Device classes do not cover the approved inventory'
    status_path=folder/'instrumentation-groups.tsv'
    statuses=status_path.read_text().splitlines() if status_path.is_file() else []
    if statuses!=[name+'\t'+str(count)+'\t0' for name,_,count in groups]: errors.append('Instrumentation command exits were not all zero: '+str(statuses))
    summary_path=folder/'instrumentation.txt'
    summary=summary_path.read_text() if summary_path.is_file() else ''
    if summary!=''.join(combined): errors.append('Combined instrumentation log differs from the three group logs')
    if [int(v) for v in re.findall(r'OK \(([0-9]+) tests?\)',summary)]!=[8,2,2]: errors.append('Combined instrumentation must report exactly OK (8), OK (2), OK (2)')
    images=sorted((folder/'screen-evidence').rglob('*.png'))
    if {p.name for p in images}!=set(spec['screens']) or len(images)!=len(spec['screens']): errors.append('Missing/duplicate screenshots')
    screenshots=[]
    for path in images:
        try: screenshots.append(png(path))
        except (AssertionError,OSError,struct.error) as error: errors.append(str(error))
    evidence={'cases':len(required),'groups':results,'screens':screenshots,'errors':errors,'scope':'Synthetic project payment pages and selected native screens; not third-party app compatibility or complete UI coverage.'}
    (folder/'device-results.json').write_text(json.dumps(evidence,ensure_ascii=False,indent=2)+'\n')
    assert not errors, '\n'.join(errors)
    (folder/'verified.json').write_text(json.dumps(evidence,ensure_ascii=False,indent=2)+'\n')
    print('Device checks and screenshots verified:',len(required),len(images))


def release():
    folder=Path(os.environ['RUNNER_TEMP'])/'capture-device'
    assert not (folder/'release-blocked.txt').exists(), 'Unverified/unsigned release cannot pass launch gate'
    assert (folder/'release-command-status.txt').read_text().strip()=='0', 'Release execution/evidence collection failed'
    assert (folder/'release-home-ready.txt').read_text().strip()=='true', 'Release home did not finish initialization'
    assert 'Status: ok' in (folder/'release-start.txt').read_text()
    assert re.fullmatch(r'\d+\s*', (folder/'release-pid.txt').read_text()), 'Release process exited'
    activities=(folder/'release-activities.txt').read_text()
    assert re.search(r'(?:mResumedActivity|topResumedActivity).*'+re.escape(PKG)+r'/\.MainActivity', activities), 'Release Activity is not resumed'
    logs=(folder/'release-logcat.txt').read_text()
    assert not re.search(r'FATAL EXCEPTION|ANR in '+re.escape(PKG),logs)
    assert any(node.get('text')=='全部流水' for node in ET.parse(folder/'release-ui.xml').iter('node')), 'Release initialized screen is missing'
    png(folder/'release-home.png')
    assert 'TOTAL' in (folder/'release-memory.txt').read_text()
    print('Exact shrunk release APK launched; runtime screenshot and emulator memory evidence available')


if __name__=='__main__':
    parser=argparse.ArgumentParser()
    parser.add_argument('--manifest');parser.add_argument('--apk');parser.add_argument('--unit',action='store_true');parser.add_argument('--device',action='store_true');parser.add_argument('--release-evidence',action='store_true');parser.add_argument('--extract',type=Path);parser.add_argument('--destination',type=Path)
    args=parser.parse_args()
    if args.extract:extract(args.extract,args.destination)
    elif args.unit:unit()
    elif args.device:device()
    elif args.release_evidence:release()
    elif args.manifest:packaged(args.manifest,args.apk)
    else:source()
