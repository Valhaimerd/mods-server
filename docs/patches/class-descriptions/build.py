"""Final English description overlay; gameplay resources and bytecode are unchanged."""
from pathlib import Path
from zipfile import ZipFile
import hashlib,json,shutil
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent
WORK=ROOT/'tmp/class-descriptions';OUT=WORK/'built';OUT.mkdir(parents=True,exist_ok=True)
edits=json.loads((HERE/'descriptions.json').read_text());records=[]
for namespace,changes in edits.items():
 prefix={'archers':'archers-','wizards':'wizards-','skill_tree_rpgs':'skill_tree-'}[namespace]
 source=next((ROOT/'mods').glob(prefix+'*.jar'));before=WORK/'before'/source.name;before.parent.mkdir(exist_ok=True)
 if not before.exists():shutil.copy2(source,before)
 path=f'assets/{namespace}/lang/en_us.json'
 with ZipFile(before) as src,ZipFile(OUT/source.name,'w') as dst:
  original=json.loads(src.read(path));lang=dict(original)
  assert changes.keys()<=lang.keys()
  lang.update(changes)
  for entry in src.infolist():
   data=(json.dumps(lang,indent=2,ensure_ascii=False)+'\n').encode() if entry.filename==path else src.read(entry)
   dst.writestr(entry,data)
 records.append({'jar':source.name,'before_sha256':hashlib.sha256(before.read_bytes()).hexdigest(),'sha256':hashlib.sha256((OUT/source.name).read_bytes()).hexdigest(),'changed':[path],'description_keys':sorted(k for k in changes if original[k]!=lang[k])})
(HERE/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n')
print('Built',sum(len(r['description_keys']) for r in records),'description changes; active pack unchanged')
