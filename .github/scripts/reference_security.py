"""Inspect source or a built APK; never imports application code or loads its libraries."""
from pathlib import Path
import argparse
import hashlib
import re
import struct
import xml.etree.ElementTree as ET
import zipfile

A = '{http://schemas.android.com/apk/res/android}'
ROOT = Path(__file__).resolve().parents[2]
SOURCE = ROOT / 'android/app/src/yimu'
COMPONENTS = {
    'com.wangc.bill.reference.ui.MainActivity',
    'com.wangc.bill.reference.ui.EditorActivity',
    'com.wangc.bill.reference.ui.ManagerActivity',
    'com.wangc.bill.reference.ui.LocalUnlockActivity',
    'com.wangc.bill.reference.ui.OriginalPermissionRequestActivity',
    'com.wangc.bill.reference.capture.ReferenceCaptureService',
    'com.wangc.bill.reference.capture.PlanReceiver',
    'com.wangc.bill.reference.ReferenceWidget',
    'com.wangc.bill.reference.OriginalRepaymentWidget',
    'com.wangc.bill.reference.OriginalRepaymentWidgetService',
    'com.wangc.bill.reference.OriginalCreditStatementActivity',
    'androidx.core.content.FileProvider',
}
PERMISSIONS = {'android.permission.USE_BIOMETRIC', 'android.permission.POST_NOTIFICATIONS', 'android.permission.RECEIVE_BOOT_COMPLETED', 'android.permission.SYSTEM_ALERT_WINDOW', 'android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS'}

def manifest(path, merged=False):
    root = ET.parse(path).getroot()
    private_permission='io.github.wcwac.yimu.reference.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION'
    if merged:
        assert root.get('package') == 'io.github.wcwac.yimu.reference'
    for p in root.findall('uses-permission'):
        if not merged and p.get('{http://schemas.android.com/tools}node') == 'remove':
            continue
        name=p.get(A+'name')
        if merged and name==private_permission:
            definition=next((v for v in root.findall('permission') if v.get(A+'name')==private_permission),None)
            assert definition is not None and definition.get(A+'protectionLevel') in {'signature','0x2','2'}
        else:
            assert name in PERMISSIONS, 'Unexpected permission: ' + str(p.attrib)
    app = root.find('application')
    assert app is not None and app.get(A+'allowBackup') == 'false'
    assert app.get(A+'name') == 'com.wangc.bill.reference.ReferenceApplication'
    assert app.get(A+'usesCleartextTraffic') == 'false'
    assert app.get(A+'debuggable','false')=='false'
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
        elif name.endswith('OriginalRepaymentWidget'):
            assert c.get(A+'exported') == 'true'
            actions=[a.get(A+'name') for a in c.findall('intent-filter/action')]
            assert actions == ['android.appwidget.action.APPWIDGET_UPDATE']
            metadata=c.find('meta-data')
            assert metadata is not None and metadata.get(A+'name') == 'android.appwidget.provider'
        elif name.endswith('OriginalRepaymentWidgetService'):
            assert c.get(A+'exported') == 'false' and c.get(A+'permission') == 'android.permission.BIND_REMOTEVIEWS'
        else:
            assert c.get(A+'exported') == 'false', 'Component exposure: '+name
    assert found == COMPONENTS, 'Missing or unexpected components'

def source():
    manifest(SOURCE/'AndroidManifest.xml')
    forbidden = re.compile(r'\b(?:DexClassLoader|InMemoryDexClassLoader|PathClassLoader|ProcessBuilder|Runtime\s*\.\s*getRuntime|System\s*\.\s*load(?:Library)?|java\.lang\.reflect|java\.net\.|android\.webkit\.|loadDex|loadClass|Class\s*\.\s*forName)\b')
    count = 0
    for p in (SOURCE/'java').rglob('*.java'):
        text = p.read_text()
        if p.relative_to(SOURCE/'java').as_posix() == 'com/wangc/bill/reference/ui/OriginalOemPermissionGuidance.java':
            # Only this reviewed, immutable block reads the built-in platform's
            # fixed ro.build.display.id property. It cannot select a class, method
            # or key from input. Keep all other reflection/loading APIs forbidden.
            start=text.index('    private static String displayId() {')
            end=text.index('    /** Public no-arg Fragment',start)
            block=text[start:end]
            assert hashlib.sha256(block.encode()).hexdigest() == '48567d53903d5cd06d87855610de2bfc0565407ca2eaa53921f23f52e88f9b85', 'Unreviewed platform-property lookup change'
            assert text.count('import java.lang.reflect.Method;') == 1
            text=(text[:start]+text[end:]).replace('import java.lang.reflect.Method;','')
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
                    if e.tag=='view':assert e.get('class','').startswith(('android.','androidx.','com.wangc.bill.reference.ui.')), 'Unreviewed view class attribute'
    for path in (SOURCE/'res').rglob('reference_accessibility.xml'):
        access=ET.parse(path).getroot()
        assert access.get(A+'canRetrieveWindowContent') == 'true'
        assert access.get(A+'canPerformGestures') != 'true'
        # Original 6.3.0 read profile (75). The deprecated web-read capability is declared
        # by the reference too; app networking, WebView code and executable assets stay forbidden.
        assert access.get(A+'canRequestEnhancedWebAccessibility') == 'true'
        assert set(access.get(A+'accessibilityFlags','').split('|')) == {
            'flagRetrieveInteractiveWindows','flagRequestEnhancedWebAccessibility',
            'flagIncludeNotImportantViews','flagDefault'}
    paths = ET.parse(SOURCE/'res/xml/reference_file_paths.xml').getroot()
    assert len(paths) == 1 and paths[0].tag == 'files-path' and paths[0].get('path') == 'attachments/'
    print(f'Source boundary verified: {count} Java files, read-only accessibility, local attachments, no unreviewed dynamic/native/network APIs')

def apk(path):
    count=0
    unexpected=[]
    with zipfile.ZipFile(path) as archive:
        assert archive.read('META-INF/com.google.android.material_material.version').strip()==b'1.10.0', 'Unexpected Material UI version'
        for name in archive.namelist():
            assert not name.startswith('lib/'), 'Unexpected native library: '+name
            if name.startswith('assets/'):
                profiles={'assets/dexopt/baseline.prof':b'pro\x00','assets/dexopt/baseline.profm':b'prm\x00'}
                assert name in profiles and archive.getinfo(name).file_size<1024*1024, 'Unexpected asset: '+name
                assert archive.read(name).startswith(profiles[name]), 'Invalid profile data'
            assert not name.lower().endswith(('.so','.jar','.apk','.js','.html','.ser')), 'Unexpected bundled executable: '+name
            if re.fullmatch(r'classes(?:[0-9]+)?\.dex',name):
                class_count,unreviewed=inspect_classes(archive.read(name))
                count+=class_count
                unexpected.extend(name+': '+entry for entry in unreviewed)
        assert 'classes.dex' in archive.namelist()
    # Report every unreviewed definition from every DEX in this build. Keep
    # failure strict, without needing another CI run for each subsequent class.
    assert not unexpected, 'Unreviewed APK class definitions ('+str(len(unexpected))+'):\n'+'\n'.join(sorted(unexpected))
    print(f'APK payload verified: {count} class definitions, no unknown SDKs/native libraries/nested APKs/scripts')

def inspect_classes(data):
    assert data[:4]==b'dex\n'
    def pair(offset):return struct.unpack_from('<II',data,offset)
    def uleb(p):
        while data[p]&128:p+=1
        return p+1
    n,off=pair(0x38);strings=[]
    for i in range(n):
        p=uleb(struct.unpack_from('<I',data,off+4*i)[0]);strings.append(data[p:data.index(0,p)].decode('utf-8','replace'))
    n,off=pair(0x40);types=[strings[struct.unpack_from('<I',data,off+4*i)[0]] for i in range(n)]
    n,off=pair(0x60)
    own=re.compile(r'L(?:com/wangc/bill/(?:reference/.*|auto/action/[a-p](?:\$.*)?|auto/x1(?:\$.*)?|http/entity/BillInfo(?:\$.*)?|utils/(?:a2|b2|h2)|database/action/(?:p|s)|(?:R|Manifest|BuildConfig)(?:\$.*)?)|com/blankj/utilcode/util/(?:t0|y)(?:\$.*)?|com/google/android/accessibility/selecttospeak/SelectToSpeakService(?:\$.*)?|s6/b);')
    unexpected=[]
    for i in range(n):
        name=types[struct.unpack_from('<I',data,off+i*32)[0]]
        if name.startswith('Landroidx/test/'):
            unexpected.append('Test code in release APK: '+name)
            continue
        # Material 1.10.0 is the UI library version declared in the reference APK;
        # it supplies the actual BottomSheetDialog used by local permission guides.
        if not (name.startswith(('Landroidx/','Landroid/support/','Lkotlin/','Lkotlinx/','Lorg/jetbrains/annotations/','Lorg/intellij/lang/annotations/','Lorg/jspecify/annotations/','Lcom/android/tools/r8/','Lcom/google/android/material/')) or name=='Lcom/google/common/util/concurrent/ListenableFuture;' or own.fullmatch(name)):
            unexpected.append(name)
    return n,unexpected

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--manifest')
    parser.add_argument('--apk')
    args = parser.parse_args()
    source()
    if args.manifest: manifest(args.manifest, merged=True)
    if args.apk: apk(args.apk)
