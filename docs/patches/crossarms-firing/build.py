"""Rebuild a candidate over the pack's existing CrossArms integration."""
from pathlib import Path
from zipfile import ZipFile,ZipInfo,ZIP_DEFLATED
import argparse,json,hashlib
HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[2]
def overrides():
 return {p.relative_to(HERE/'overrides').as_posix():p.read_bytes() for p in (HERE/'overrides').rglob('*') if p.is_file()}
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--input',type=Path,default=ROOT/'mods/crossarms-1.4.jar');p.add_argument('--output',type=Path,default=ROOT/'tmp/crossarms-firing/crossarms-1.4.jar');a=p.parse_args();new=overrides()
 with ZipFile(a.input) as src,ZipFile(a.output,'w') as dst:
  for e in src.infolist():dst.writestr(e,new.pop(e.filename,src.read(e.filename)))
  for n,data in sorted(new.items()):
   entry=ZipInfo(n,(1980,1,1,0,0,0));entry.compress_type=ZIP_DEFLATED;dst.writestr(entry,data)
 print(hashlib.sha256(a.output.read_bytes()).hexdigest())
