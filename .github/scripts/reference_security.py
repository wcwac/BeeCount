"""Inspect source or a built APK; never imports application code or loads its libraries."""
from pathlib import Path
import argparse
import re
import xml.etree.ElementTree as ET
import zipfile

A = '{http://schemas.android.com/apk/res/android}'
ROOT = Path(__file__).resolve().parents[2]
SOURCE = ROOT / 'android/app/src/yimu'
COMPONENTS = {
    'com.wangc.bill.reference.ui.MainActivity',
    'com.wangc.bill.reference.ui.EditorActivity',
    'com.wangc.bill.reference.ui.ManagerActivity',
    'com.wangc.bill.reference.capture.ReferenceCaptureService',
    'com.wangc.bill.reference.capture.PlanReceiver',
    'com.wangc.bill.reference.ReferenceWidget',
    'androidx.core.content.FileProvider',
}
PERMISSIONS = {'android.permission.POST_NOTIFICATIONS', 'android.permission.RECEIVE_BOOT_COMPLETED'}

def manifest(path, merged=False):
    root = ET.parse(path).getroot()
    if merged:
        assert root.get('package') == 'io.github.wcwac.yimu.reference'
    for p in root.findall('uses-permission'):
        if not merged and p.get('{http://schemas.android.com/tools}node') == 'remove':
            continue
        assert p.get(A+'name') in PERMISSIONS, 'Unexpected permission: ' + str(p.attrib)
    app = root.find('application')
    assert app is not None and app.get(A+'allowBackup') == 'false'
    assert app.get(A+'name') == 'com.wangc.bill.reference.ReferenceApplication'
    assert app.get(A+'usesCleartextTraffic') == 'false'
    found = set()
    for c in app:
        if c.tag not in {'activity','service','receiver','provider','activity-alias'}:
            continue
        if not merged and c.get('{http://schemas.android.com/tools}node') == 'remove':
            continue
        name = c.get(A+'name')
        assert name in COMPONENTS, 'Unexpected component: ' + str(name)
        found.add(name)
        if name.endswith('MainActivity'):
            assert c.get(A+'exported') == 'true'
        elif name.endswith('ReferenceCaptureService'):
            assert c.get(A+'exported') == 'true' and c.get(A+'permission') == 'android.permission.BIND_ACCESSIBILITY_SERVICE'
        else:
            assert c.get(A+'exported') == 'false', 'Component exposure: '+name
    assert found == COMPONENTS, 'Missing or unexpected components'

def source():
    manifest(SOURCE/'AndroidManifest.xml')
    forbidden = re.compile(r'\b(?:DexClassLoader|InMemoryDexClassLoader|PathClassLoader|ProcessBuilder|Runtime\s*\.\s*getRuntime|System\s*\.\s*load(?:Library)?|java\.lang\.reflect|java\.net\.|android\.webkit\.|loadDex|Class\s*\.\s*forName)\b')
    count = 0
    for p in (SOURCE/'java').rglob('*.java'):
        text = p.read_text()
        # Class-name strings such as payment WebView activities are data, not imports/calls.
        code = re.sub(r'/\*.*?\*/|//[^\n]*|"(?:\\.|[^"\\])*"', '', text, flags=re.S)
        assert not forbidden.search(code), 'Forbidden execution/network API in '+str(p)
        assert not re.search(r'\bnative\s+\w', code), 'Native method in '+str(p)
        count += 1
    for p in SOURCE.rglob('*'):
        if not p.is_file():
            continue
        assert p.suffix.lower() not in {'.so','.dex','.jar','.apk','.js','.html','.ser','.bin'}, 'Unexpected executable asset '+str(p)
        if p.suffix == '.xml':
            tree = ET.parse(p).getroot()
            for e in tree.iter():
                assert A+'onClick' not in e.attrib, 'Automatic callback in '+str(p)
                if p.parent.name.startswith('layout'):
                    assert e.tag not in {'fragment','WebView','android.webkit.WebView'}
                    assert '.' not in e.tag or e.tag.startswith(('android.','androidx.','com.wangc.bill.reference.ui.')), 'Unreviewed view class '+e.tag
    access = ET.parse(SOURCE/'res/xml/reference_accessibility.xml').getroot()
    assert access.get(A+'canRetrieveWindowContent') == 'true'
    assert access.get(A+'canPerformGestures') != 'true'
    assert access.get(A+'canRequestEnhancedWebAccessibility') != 'true'
    paths = ET.parse(SOURCE/'res/xml/reference_file_paths.xml').getroot()
    assert len(paths) == 1 and paths[0].tag == 'files-path' and paths[0].get('path') == 'attachments/'
    print(f'Source boundary verified: {count} Java files, read-only accessibility, local attachments, no dynamic/native/network APIs')

def apk(path):
    with zipfile.ZipFile(path) as archive:
        for name in archive.namelist():
            assert not name.startswith(('lib/','assets/')), 'Unexpected native library/asset: '+name
            assert not name.lower().endswith(('.so','.jar','.apk','.js','.html','.ser')), 'Unexpected bundled executable: '+name
        assert 'classes.dex' in archive.namelist()
    print('APK payload verified: no native libraries, SDK assets, nested APKs or scripts')

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--manifest')
    parser.add_argument('--apk')
    args = parser.parse_args()
    source()
    if args.manifest: manifest(args.manifest, merged=True)
    if args.apk: apk(args.apk)
