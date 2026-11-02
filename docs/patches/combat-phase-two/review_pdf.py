from pathlib import Path
import fitz,re,json
from PIL import Image,ImageOps,ImageDraw
R=Path(__file__).resolve().parents[3];W=R/'tmp/combat-phase-two';p=R/'docs/MODLIST.md';s=p.read_text(encoding='utf-8');d=fitz.open(R/'modpack-player-handbook.pdf')
links=[l for page in d for l in page.get_links() if l.get('nameddest')]
assert all(0<=l.get('page',-1)<len(d) for l in links)
dest={l['nameddest']:l['page']+1 for l in links};changes=[]
def row(m):
 key=m[2];actual=dest[key]
 if int(m[3])!=actual:changes.append((key,int(m[3]),actual))
 return m[1]+str(actual)+m[4]
pattern=r'(<tr[^>]*><td><a href="#([^\"]+)">.*?</a></td><td>)(\d+)(</td></tr>)'
updated,count=re.subn(pattern,row,s);assert count==51,count
if changes:p.write_text(updated,encoding='utf-8')
before=fitz.open(W/'handbook-before.pdf');changed=[i for i in range(len(d)) if i>=len(before) or d[i].get_text()!=before[i].get_text()]
pages=sorted(set([1]+changed));thumbs=[]
for i in pages:
 pix=d[i].get_pixmap(matrix=fitz.Matrix(1,1));im=Image.frombytes('RGB',[pix.width,pix.height],pix.samples);im.save(W/f'handbook-page-{i+1}.png');im.thumbnail((430,610));tile=Image.new('RGB',(450,640),'#ddd');tile.paste(im,((450-im.width)//2,20));ImageDraw.Draw(tile).text((10,622),str(i+1),fill='black');thumbs.append(tile)
for j in range(0,len(thumbs),6):
 group=thumbs[j:j+6];sheet=Image.new('RGB',(450*min(3,len(group)),640*((len(group)+2)//3)),'white')
 for k,im in enumerate(group):sheet.paste(im,((k%3)*450,(k//3)*640))
 sheet.save(W/f'handbook-review-{j//6+1}.png')
report={'pages':len(d),'internal_links':len(links),'toc_rows':count,'toc_corrections':changes,'changed_pages':[i+1 for i in changed]};(W/'handbook-review.json').write_text(json.dumps(report,indent=2));print(json.dumps(report))
