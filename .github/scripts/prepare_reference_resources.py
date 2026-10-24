"""Select referenced original resources; let resolved AndroidX AARs supply their own resources.
Only reads XML/archive data. Does not load or execute code from an AAR or the original APK.
Invoked on the cloud build runner before AAPT; no additional dependencies are needed.
"""
from pathlib import Path
from collections import defaultdict,deque
import argparse,copy,re,shutil,struct,zipfile,zlib
import xml.etree.ElementTree as ET

A='{http://schemas.android.com/apk/res/android}'
APP='{http://schemas.android.com/apk/res-auto}'
TOOLS='{http://schemas.android.com/tools}'
ET.register_namespace('android',A[1:-1]);ET.register_namespace('app',APP[1:-1]);ET.register_namespace('tools',TOOLS[1:-1])
REFERENCE=re.compile(r'[@?](?:\+)?(?:\*?([\w.]+):)?([\w]+)/([\w.]+)')
# RemoteViews permits framework views only. Keep native ImageView tint attributes
# in these recovered widget layouts instead of introducing AppCompat subclasses.
REMOTE_VIEWS_LAYOUTS={'reference_widget','layout_app_widget_repayment','item_widget_repayment_list','item_widget_no_tip_small'}

def source_nine_patch(data,label):
    """Restore editable borders from an APK PNG without changing its image pixels.

    AAPT removes compiled npTc metadata from an ordinary .png. Renaming a
    compiled .9.png therefore loses both stretch regions and content padding.
    Feed AAPT a real source .9.png instead: its extra one-pixel border is removed
    during compilation and the stretch/padding chunk is generated again.
    """
    def require(condition,message):
        if not condition:raise ValueError(f'{label}: {message}')
    require(data.startswith(b'\x89PNG\r\n\x1a\n') and len(data)<=16*1024*1024,'invalid or oversized PNG')
    chunks={};pos=8;ended=False
    while pos+12<=len(data):
        size=struct.unpack_from('>I',data,pos)[0];kind=data[pos+4:pos+8]
        require(size<=len(data)-pos-12,'truncated PNG chunk')
        value=data[pos+8:pos+8+size];crc=struct.unpack_from('>I',data,pos+8+size)[0]
        require(zlib.crc32(kind+value)&0xffffffff==crc,'PNG chunk CRC mismatch')
        chunks.setdefault(kind,[]).append(value);pos+=size+12
        if kind==b'IEND':
            require(size==0 and pos==len(data),'invalid PNG end');ended=True;break
    require(ended and len(chunks.get(b'IHDR',[]))==1,'missing PNG header/end')
    # Existing editable nine-patches retain their own border and are compiled normally.
    if b'npTc' not in chunks:return data
    require(len(chunks[b'npTc'])==1,'multiple stretch metadata chunks')
    # Current optical-inset resources are provided by the resolved AndroidX AARs
    # and never reach this path. Do not silently discard a future non-SDK inset.
    require(b'npLb' not in chunks,'optical-inset nine-patch needs an editable original')
    header=chunks[b'IHDR'][0];require(len(header)==13,'invalid PNG header')
    width,height,depth,color,compression,filter_method,interlace=struct.unpack('>IIBBBBB',header)
    channels={0:1,2:3,4:2,6:4}.get(color)
    require(0<width<=4096 and 0<height<=4096 and depth==8 and channels is not None and
            compression==0 and filter_method==0 and interlace==0,'unsupported compiled PNG format')
    require(b'tRNS' not in chunks,'unsupported transparent-color PNG')
    allowed={b'IHDR',b'IDAT',b'IEND',b'PLTE'}
    require(all(kind[0]&32 or kind in allowed for kind in chunks),'unsupported critical PNG chunk')
    patch=chunks[b'npTc'][0];require(len(patch)>=32,'truncated stretch metadata')
    nx,ny,ncolors=patch[1:4]
    require(nx>0 and ny>0 and nx%2==0 and ny%2==0 and
            len(patch)==32+4*(nx+ny+ncolors),'invalid stretch metadata size')
    xdiv=struct.unpack_from('>'+str(nx)+'i',patch,32)
    ydiv=struct.unpack_from('>'+str(ny)+'i',patch,32+4*nx)
    left,right,top,bottom=struct.unpack_from('>iiii',patch,12)
    def valid_divs(values,extent):
        return 0<=values[0] and values[-1]<=extent and all(a<b for a,b in zip(values,values[1:]))
    require(valid_divs(xdiv,width) and valid_divs(ydiv,height),'stretch boundaries outside image')
    require(0<=left and 0<=right and left+right<width and
            0<=top and 0<=bottom and top+bottom<height,'padding outside image')
    stride=width*channels;expected=(stride+1)*height
    require(expected<=64*1024*1024 and b'IDAT' in chunks,'invalid image data size')
    decoder=zlib.decompressobj();raw=decoder.decompress(b''.join(chunks[b'IDAT']),expected+1)
    require(len(raw)==expected and decoder.eof and not decoder.unconsumed_tail and
            not decoder.unused_data,'invalid compressed image data')
    rows=[];previous=bytearray(stride)
    for y in range(height):
        offset=y*(stride+1);method=raw[offset];require(method<=4,'unknown PNG filter')
        row=bytearray(raw[offset+1:offset+1+stride])
        for x in range(stride):
            a=row[x-channels] if x>=channels else 0;b=previous[x];c=previous[x-channels] if x>=channels else 0
            if method==1:row[x]=(row[x]+a)&255
            elif method==2:row[x]=(row[x]+b)&255
            elif method==3:row[x]=(row[x]+(a+b)//2)&255
            elif method==4:
                p=a+b-c;pa=abs(p-a);pb=abs(p-b);pc=abs(p-c)
                row[x]=(row[x]+(a if pa<=pb and pa<=pc else b if pb<=pc else c))&255
        rgba=bytearray(width*4)
        for x in range(width):
            start=x*channels
            if color==0:r=g=b=row[start];alpha=255
            elif color==2:r,g,b=row[start:start+3];alpha=255
            elif color==4:r=g=b=row[start];alpha=row[start+1]
            else:r,g,b,alpha=row[start:start+4]
            rgba[x*4:x*4+4]=bytes((r,g,b,alpha))
        rows.append(rgba);previous=row
    output_width=width+2;output=[bytearray(output_width*4) for _ in range(height+2)]
    for y,row in enumerate(rows):output[y+1][4:4+width*4]=row
    black=b'\x00\x00\x00\xff'
    for start,end in zip(xdiv[::2],xdiv[1::2]):
        for x in range(start,end):output[0][(x+1)*4:(x+2)*4]=black
    for start,end in zip(ydiv[::2],ydiv[1::2]):
        for y in range(start,end):output[y+1][:4]=black
    for x in range(left,width-right):output[-1][(x+1)*4:(x+2)*4]=black
    for y in range(top,height-bottom):output[y+1][-4:]=black
    def chunk(kind,value):
        return struct.pack('>I',len(value))+kind+value+struct.pack('>I',zlib.crc32(kind+value)&0xffffffff)
    image=b''.join(b'\x00'+row for row in output)
    return (b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>IIBBBBB',width+2,height+2,8,6,0,0,0))+
            chunk(b'IDAT',zlib.compress(image,9))+chunk(b'IEND',b''))

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
    kept=0;restored_nine_patches=0
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
            target=output/relative;target.parent.mkdir(parents=True,exist_ok=True)
            if path in documents:
                root=copy.deepcopy(documents[path].getroot())
                if path.parent.name.startswith('layout') and path.stem in REMOTE_VIEWS_LAYOUTS:
                    # RemoteViews cannot inflate AppCompat views. Android's native tint
                    # works on the supported API levels; the AppCompat lint replacement
                    # would break the system widget, so exempt only these native nodes.
                    for e in root.iter():
                        if e.tag in {'ImageView','ImageButton','android.widget.ImageView','android.widget.ImageButton'} and A+'tint' in e.attrib:
                            ignored={x.strip() for x in e.get(TOOLS+'ignore','').split(',') if x.strip()}
                            ignored.add('UseAppTint');e.set(TOOLS+'ignore',','.join(sorted(ignored)))
                elif path.parent.name.startswith('layout'):
                    for e in root.iter():
                        if e.tag in {'ImageView','ImageButton','android.widget.ImageView','android.widget.ImageButton','androidx.appcompat.widget.AppCompatImageView','androidx.appcompat.widget.AppCompatImageButton'} and A+'tint' in e.attrib:
                            e.tag='androidx.appcompat.widget.AppCompatImageButton' if e.tag.endswith('ImageButton') else 'androidx.appcompat.widget.AppCompatImageView';e.set(APP+'tint',e.attrib.pop(A+'tint'))
                            if A+'tintMode' in e.attrib:e.set(APP+'tintMode',e.attrib.pop(A+'tintMode'))
                ET.indent(root,space='    ');ET.ElementTree(root).write(target,encoding='utf-8',xml_declaration=True)
            elif path.name.endswith('.9.png'):
                target.write_bytes(source_nine_patch(path.read_bytes(),str(relative)));restored_nine_patches+=1
            else:shutil.copyfile(path,target)
            kept+=1
    print(f'Prepared {kept} original resource files; {restored_nine_patches} source nine-patches; {len(provided)} SDK names supplied by resolved AARs')

if __name__=='__main__':
    parser=argparse.ArgumentParser();parser.add_argument('--source',type=Path,required=True);parser.add_argument('--output',type=Path,required=True);parser.add_argument('--java',type=Path,required=True);parser.add_argument('--manifest',type=Path,required=True);parser.add_argument('--aar',type=Path,action='append',default=[]);args=parser.parse_args()
    prepare(args.source.resolve(),args.output.resolve(),args.java.resolve(),args.manifest.resolve(),args.aar)
