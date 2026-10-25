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
import xml.etree.ElementTree as ET
import zipfile

ROOT = Path(__file__).resolve().parents[2]
PKG = 'io.github.wcwac.beecount.nativeapp'
ANDROID = '{http://schemas.android.com/apk/res/android}'
INVENTORY = ROOT / '.github/native-cases.json'


def source():
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
        assert all(Path(name).name in {'libandroidx.graphics.path.so'} for name in native), 'Unreviewed native runtime payload: '+str(native)
        print('Official AndroidX native payload:',native)
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


def device():
    folder=Path(os.environ['RUNNER_TEMP'])/'capture-device'
    text=(folder/'instrumentation.txt').read_text()
    assert not any(word in text for word in ('FAILURES!!!','INSTRUMENTATION_FAILED','Process crashed','INSTRUMENTATION_RESULT: shortMsg'))
    spec=json.loads(INVENTORY.read_text()); required=set(spec['device'])
    started=Counter(); passed=Counter(); case={}
    for line in text.splitlines():
        field=re.fullmatch(r'INSTRUMENTATION_STATUS: (class|test)=(.*)',line.strip())
        if field:case[field[1]]=field[2]
        status=re.fullmatch(r'INSTRUMENTATION_STATUS_CODE: (-?\d+)',line.strip())
        if not status:continue
        if 'class' in case and 'test' in case:
            name=case['class']+'.'+case['test']; code=int(status[1]); assert code in (0,1), name+' failed/skipped'
            (started if code==1 else passed)[name]+=1
        case={}
    assert set(started)==set(passed)==required and all(n==1 for n in list(started.values())+list(passed.values())), 'Device case inventory mismatch: '+str(required-set(passed))
    assert [int(v) for v in re.findall(r'OK \(([0-9]+) tests?\)',text)]==[len(required)]
    images=sorted((folder/'screen-evidence').rglob('*.png'))
    assert {p.name for p in images}==set(spec['screens']) and len(images)==len(spec['screens']), 'Missing/duplicate screenshots'
    evidence={'cases':len(required),'screens':[png(p) for p in images],'scope':'Synthetic project payment pages and selected native screens; not third-party app compatibility or complete UI coverage.'}
    (folder/'verified.json').write_text(json.dumps(evidence,ensure_ascii=False,indent=2)+'\n')
    print('Device checks and screenshots verified:',len(required),len(images))


def release():
    folder=Path(os.environ['RUNNER_TEMP'])/'capture-device'
    assert not (folder/'release-blocked.txt').exists(), 'Unsigned release cannot pass launch gate'
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
