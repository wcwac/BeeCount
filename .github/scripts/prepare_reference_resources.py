"""Select referenced original resources; let resolved AndroidX AARs supply their own resources.
Only reads XML/archive data. Does not load or execute code from an AAR or the original APK.
Invoked on the cloud build runner before AAPT; no additional dependencies are needed.
"""
from pathlib import Path
from collections import defaultdict,deque
import argparse,copy,re,shutil,zipfile
import xml.etree.ElementTree as ET

A='{http://schemas.android.com/apk/res/android}'
APP='{http://schemas.android.com/apk/res-auto}'
ET.register_namespace('android',A[1:-1]);ET.register_namespace('app',APP[1:-1])
REFERENCE=re.compile(r'[@?](?:\+)?(?:\*?([\w.]+):)?([\w]+)/([\w.]+)')
# RemoteViews permits framework views only. Keep native ImageView tint attributes
# in these recovered widget layouts instead of introducing AppCompat subclasses.
REMOTE_VIEWS_LAYOUTS={'reference_widget','layout_app_widget_repayment','item_widget_repayment_list','item_widget_no_tip_small'}

def key(element):
    kind=element.get('type') if element.tag=='item' else element.tag
    if kind in {'string-array','integer-array'}:kind='array'
    return kind,element.get('name')

def dependencies(element):
    result=set()
    for e in element.iter():
        for value in [*e.attrib.values(),e.text or '']:
            for package,kind,name in REFERENCE.findall(value):
                if package not in {'android'}:result.add((kind,name))
            if re.fullmatch(r'\?[A-Za-z_]\w*',value):result.add(('attr',value[1:]))
        for attr in e.attrib:
            if attr.startswith(APP):result.add(('attr',attr[len(APP):]))
        if e.tag=='style':
            parent=e.get('parent')
            if parent and not parent.startswith('@'):result.add(('style',parent))
            elif parent is None and '.' in e.get('name',''):result.add(('style',e.get('name').rsplit('.',1)[0]))
        if e.tag=='item' and e.get('name') and not e.get('name').startswith('android:'):
            result.add(('attr',e.get('name')))
    return result

def library_resources(paths):
    keys=set()
    for path in paths:
        with zipfile.ZipFile(path) as aar:
            for name in aar.namelist():
                parts=name.split('/')
                if len(parts)!=3 or parts[0]!='res' or name.endswith('/'):continue
                folder,filename=parts[1:];kind=folder.split('-')[0]
                if kind=='values' and name.endswith('.xml'):
                    for e in ET.fromstring(aar.read(name)):
                        if e.get('name'):keys.add(key(e))
                        if e.tag=='declare-styleable':
                            for attr in e:
                                if attr.get('name') and not attr.get('name').startswith('android:'):keys.add(('attr',attr.get('name')))
                else:keys.add((kind,filename.split('.')[0]))
    return keys

def prepare(source,output,java,manifest,aars):
    assert source.is_dir() and source!=output
    provided=library_resources(aars)
    definitions=defaultdict(list);documents={};files={}
    for path in source.rglob('*'):
        if not path.is_file():continue
        kind=path.parent.name.split('-')[0]
        if kind=='values':
            tree=ET.parse(path);documents[path]=tree
            for element in tree.getroot():
                if element.get('name'):definitions[key(element)].append(element)
        else:
            resource=(kind,path.name.split('.')[0]);files.setdefault(resource,[]).append(path)
            if path.suffix=='.xml':documents[path]=ET.parse(path);definitions[resource].append(documents[path].getroot())
            else:definitions.setdefault(resource,[])
    roots=set(dependencies(ET.parse(manifest).getroot()))
    names=defaultdict(set)
    for k in definitions:names[k[1]].add(k)
    for path in java.rglob('*.java'):
        text=path.read_text()
        for literal in re.findall(r'"([A-Za-z_][A-Za-z0-9_.]*)"',text):roots.update(names.get(literal,set()))
        for kind,name in re.findall(r'\bR\.(\w+)\.(\w+)',text):roots.add((kind,name))
    # Category/background names are data in local backups; retain original icon choices.
    roots.update(k for k in files if k[0]=='mipmap')
    roots.update(('drawable','selector_'+name) for name in ['main','calendar','asset','statistics','my'])
    needed=set();pending=deque(roots)
    while pending:
        resource=pending.popleft()
        if resource in needed or resource in provided:continue
        needed.add(resource)
        for element in definitions.get(resource,[]):pending.extend(dependencies(element)-needed)
    if output.exists():shutil.rmtree(output)
    output.mkdir(parents=True)
    kept=0
    for path,tree in documents.items():
        if path.parent.name.split('-')[0]!='values':continue
        root=ET.Element('resources')
        for element in tree.getroot():
            if key(element) in needed and key(element) not in provided:root.append(copy.deepcopy(element))
        if len(root):
            target=output/path.relative_to(source);target.parent.mkdir(parents=True,exist_ok=True);ET.indent(root,space='    ');ET.ElementTree(root).write(target,encoding='utf-8',xml_declaration=True);kept+=1
    for resource,paths in files.items():
        if resource not in needed or resource in provided:continue
        for path in paths:
            relative=path.relative_to(source)
            # APK PNGs already contain compiled stretch metadata, not editable 9-patch borders.
            # Preserve the bytes and resource name while avoiding a second border compilation.
            if path.name.endswith('.9.png'):relative=relative.with_name(path.name.replace('.9.png','.png'))
            target=output/relative;target.parent.mkdir(parents=True,exist_ok=True)
            if path in documents:
                root=copy.deepcopy(documents[path].getroot())
                if path.parent.name.startswith('layout') and path.stem not in REMOTE_VIEWS_LAYOUTS:
                    for e in root.iter():
                        if e.tag in {'ImageView','ImageButton','android.widget.ImageView','android.widget.ImageButton','androidx.appcompat.widget.AppCompatImageView','androidx.appcompat.widget.AppCompatImageButton'} and A+'tint' in e.attrib:
                            e.tag='androidx.appcompat.widget.AppCompatImageButton' if e.tag.endswith('ImageButton') else 'androidx.appcompat.widget.AppCompatImageView';e.set(APP+'tint',e.attrib.pop(A+'tint'))
                            if A+'tintMode' in e.attrib:e.set(APP+'tintMode',e.attrib.pop(A+'tintMode'))
                ET.indent(root,space='    ');ET.ElementTree(root).write(target,encoding='utf-8',xml_declaration=True)
            else:shutil.copyfile(path,target)
            kept+=1
    print(f'Prepared {kept} original resource files; {len(provided)} SDK names supplied by resolved AARs')

if __name__=='__main__':
    parser=argparse.ArgumentParser();parser.add_argument('--source',type=Path,required=True);parser.add_argument('--output',type=Path,required=True);parser.add_argument('--java',type=Path,required=True);parser.add_argument('--manifest',type=Path,required=True);parser.add_argument('--aar',type=Path,action='append',default=[]);args=parser.parse_args()
    prepare(args.source.resolve(),args.output.resolve(),args.java.resolve(),args.manifest.resolve(),args.aar)
