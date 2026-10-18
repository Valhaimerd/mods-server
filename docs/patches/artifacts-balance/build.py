"""Compile two replacement classes and repack the hash-pinned upstream archive.

python docs/patches/artifacts-balance/build.py --classpath-file <file>
Classpath must contain the NeoForge-patched MC26.2 JAR, Artifacts upstream,
and its development dependencies. Output remains under tmp/ until verified.
"""
import argparse,hashlib,json,subprocess
from pathlib import Path
from zipfile import ZipFile

HERE=Path(__file__).resolve().parent
ROOT=HERE.parents[2]
WORK=ROOT/'tmp/artifacts-balance'
NAME='artifacts-neoforge-15.1.3+26.2.1.jar'
UPSTREAM_SHA='3be92116769cbba850acfc0e03bd0f3688c459e2b17144d97da36679fe35c104'
parser=argparse.ArgumentParser()
parser.add_argument('--classpath-file',required=True)
parser.add_argument('--java-bin',default='C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
args=parser.parse_args()
upstream=WORK/'upstream.jar'
assert hashlib.sha256(upstream.read_bytes()).hexdigest()==UPSTREAM_SHA,'Upstream hash mismatch'
classes=WORK/'classes';classes.mkdir(parents=True,exist_ok=True)
cp=Path(args.classpath_file).read_text().strip()
sources=sorted((HERE/'src').rglob('*.java'))
command=['--release','25','-encoding','UTF-8','-cp',cp,'-d',str(classes),*[str(f) for f in sources]]
argfile=WORK/'compile.args'
argfile.write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in command))
subprocess.run([str(Path(args.java_bin)/'javac.exe'),'@'+str(argfile)],check=True)
replacements={f.relative_to(classes).as_posix():f.read_bytes() for f in classes.rglob('*.class')}
assert set(replacements)=={'artifacts/config/value/ConfigValue.class','artifacts/registry/ModItems.class'},set(replacements)
output=WORK/NAME
changed=[]
with ZipFile(upstream) as src,ZipFile(output,'w') as dst:
 for info in src.infolist():
  old=src.read(info.filename);new=replacements.get(info.filename,old)
  if new!=old:changed.append(info.filename)
  dst.writestr(info,new)
 dst.writestr('artifacts-pack-balance.json',(HERE/'balance.json').read_bytes())
assert len(changed)==2,changed
report={'upstream_sha256':UPSTREAM_SHA,'output_sha256':hashlib.sha256(output.read_bytes()).hexdigest(),'replaced':changed}
(WORK/'build-result.json').write_text(json.dumps(report,indent=2)+'\n')
print(json.dumps(report,indent=2))
