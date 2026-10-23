"""Repack the existing Penchant build with armor policy tags; never installs automatically."""
from pathlib import Path
from zipfile import ZipFile,ZipInfo,ZIP_DEFLATED
import argparse,json,hashlib
HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[2]
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
p=argparse.ArgumentParser();p.add_argument('--input',type=Path);p.add_argument('--output',type=Path);a=p.parse_args()
m=json.loads((HERE/'upstream.json').read_text());src=a.input or ROOT/m['input'];dst=a.output or ROOT/'tmp/armor-enchantments'/m['filename']
assert src.resolve()!=dst.resolve();assert sha(src)==m['input_sha256'],'Unexpected input: preserve prior pack patches'
overrides={p.relative_to(HERE/'overrides').as_posix():p.read_bytes() for p in (HERE/'overrides').rglob('*.json')};dst.parent.mkdir(parents=True,exist_ok=True)
with ZipFile(src) as z,ZipFile(dst,'w') as out:
 for info in z.infolist():out.writestr(info,overrides.get(info.filename,z.read(info)))
 for name,data in overrides.items():
  if name not in z.namelist():
   info=ZipInfo(name,(1980,1,1,0,0,0));info.compress_type=ZIP_DEFLATED;out.writestr(info,data)
with ZipFile(src) as old,ZipFile(dst) as new:
 assert not new.testzip();assert len(new.namelist())==len(set(new.namelist()))
 assert all(new.read(n)==old.read(n) for n in old.namelist() if n not in overrides)
 assert all(new.read(n)==data for n,data in overrides.items())
report={**m,'output_sha256':sha(dst),'changed_entries':sorted(overrides)}
(HERE/'artifacts.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report,indent=2))
