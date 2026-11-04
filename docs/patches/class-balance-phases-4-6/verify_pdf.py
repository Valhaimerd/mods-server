"""Check the current generated handbook without historical fixed content counts."""
from pathlib import Path
import json,re,hashlib,html
import pymupdf
from pypdf import PdfReader
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent
source=(R/'tmp/pdfs/handbook.html').read_text(encoding='utf-8')
doc=pymupdf.open(R/'modpack-player-handbook.pdf')
ids=set(re.findall(r'\bid="([^"]+)"',source))
targets={n[1:] for n in re.findall(r'href="(#[^"]+)"',source)}
texts=[page.get_text() for page in doc]
images=len(re.findall(r'<img\b',source));placements=sum(len(page.get_images(full=True)) for page in doc)
links=[link for page in doc for link in page.get_links()]
title=html.unescape(re.search(r'<title>(.*?)</title>',source,re.S).group(1))
assert doc.page_count and all(len(text.strip())>=40 for text in texts)
assert all(abs(page.rect.width-594.96)<.1 and abs(page.rect.height-841.92)<.1 for page in doc)
assert not targets-ids and images>0 and placements>=images
internal=sum(1 for page in PdfReader(R/'modpack-player-handbook.pdf').pages for ref in page.get('/Annots',[]) if ((a:=ref.get_object()).get('/Dest') is not None or a.get('/A',{}).get('/S')=='/GoTo'))
assert internal>0
assert any(link['kind']==pymupdf.LINK_URI for link in links)
assert '\ufffd' not in ''.join(texts)
assert doc.metadata['title']==title
assert hashlib.sha256((R/'modpack-player-handbook.pdf').read_bytes()).digest()==hashlib.sha256((R/'output/pdf/modpack-player-handbook.pdf').read_bytes()).digest()
report={'pages':doc.page_count,'html_images':images,'pdf_image_placements':placements,'links':len(links),'internal_links':internal,'missing_local_targets':sorted(targets-ids),'blank_pages':[],'title':title,'reviewed_render_pages':[17,18]}
(H/'pdf-review.json').write_text(json.dumps(report,indent=2)+'\n')
print('PASS PDF: ',report)
