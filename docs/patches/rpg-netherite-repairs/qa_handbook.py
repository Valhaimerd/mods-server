from pathlib import Path
import sys,json,hashlib,re,math
sys.path.insert(0,str(Path('tmp/paladin-revamp/python').resolve()))
import pymupdf as fitz
from PIL import Image,ImageOps,ImageDraw
from html.parser import HTMLParser
from urllib.parse import unquote
out=Path('tmp/rpg-netherite-repairs/pdf-review');out.mkdir(exist_ok=True)
pdf=Path('modpack-player-handbook.pdf');doc=fitz.open(pdf);toc=doc[1];links=toc.get_links();checks=[]
words=toc.get_text('words')
for l in links:
 r=l['from'];numeric=[w for w in words if w[0]>500 and w[4].isdigit()];nearest=min(numeric,key=lambda w:abs((w[1]+w[3])/2-(r.y0+r.y1)/2));assert abs((nearest[1]+nearest[3])/2-(r.y0+r.y1)/2)<3;(actual,printed)=(l['page']+1,int(nearest[4]));assert actual==printed,(l.get('nameddest'),actual,printed);checks.append({'anchor':l.get('nameddest'),'page':actual})
assert len(checks)==51
class Links(HTMLParser):
 def __init__(self):super().__init__();self.ids=set();self.refs=[]
 def handle_starttag(self,tag,attrs):
  d=dict(attrs)
  if 'id' in d:self.ids.add(d['id'])
  if d.get('href','').startswith('#'):self.refs.append(unquote(d['href'][1:]))
p=Links();p.feed(Path('tmp/rpg-netherite-repairs/handbook/handbook.html').read_text(encoding='utf-8'));missing=set(p.refs)-p.ids;assert not missing,missing
mins=[]
for i,page in enumerate(doc):
 text=page.get_text();assert len(text.strip())>100,(i+1,'blank');mins.append(len(text));pix=page.get_pixmap(matrix=fitz.Matrix(1.2,1.2),alpha=False);pix.save(str(out/f'page-{i+1:02}.png'))
for start in range(0,len(doc),16):
 sheet=Image.new('RGB',(1280,1880),'#d0d0d0');draw=ImageDraw.Draw(sheet)
 for j in range(16):
  i=start+j
  if i>=len(doc):break
  img=Image.open(out/f'page-{i+1:02}.png');img.thumbnail((310,430));x=(j%4)*320+(320-img.width)//2;y=(j//4)*470+24;sheet.paste(img,(x,y));draw.text(((j%4)*320+12,(j//4)*470+6),str(i+1),fill='black')
 sheet.save(out/f'contact-{start//16+1}.png')
assert pdf.read_bytes()==Path('output/pdf/modpack-player-handbook.pdf').read_bytes()
md=Path('docs/MODLIST.md').read_text(encoding='utf-8');alpha=md.split('### Alphabetical mod index')[1].split('### Gameplay-tag index')[0];names=re.findall(r'^- \[([^]]+)\]',alpha,re.M);assert len(names)==185;assert len(names)==len(set(names));assert names==sorted(names,key=str.casefold)
result={'pages':len(doc),'toc_links':len(checks),'toc_numbers_match':True,'zero_blank_pages':True,'zero_missing_html_targets':True,'alphabetical_entries':len(names),'main_jars':len(list(Path('mods').glob('*.jar'))),'client_jars':len(list(Path('mod store').glob('*.jar'))),'pdf_sha256':hashlib.sha256(pdf.read_bytes()).hexdigest(),'all_pages_rendered':True,'visual_review_pending':True,'changed_pages':[17]}
Path('docs/patches/rpg-netherite-repairs/handbook-verification.json').write_text(json.dumps(result,indent=2)+'\n');print(json.dumps(result,indent=2))
