"""Compile/run the isolated visibility regression against installed-library caches.

Requires the cached NeoForge26.2.0.90 server and the local port checkout; never
writes to the personal game directory. Logs and worlds live below tmp/delight-batch.
"""
from pathlib import Path
import json, os, shlex, shutil, subprocess, sys, zipfile

ROOT=Path(__file__).resolve().parents[3]
TMP=ROOT/'tmp/delight-batch'
JAVA=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
DD=ROOT/'mods/displaydelight-26.2-1.7.0-fix2.jar'
FD=ROOT/'mods/FarmersDelight-26.2-1.3.3-fix5.jar'
EXP=ROOT/'mods/expandeddelight-26.2-0.1.4-fix1.jar'

def compile_probe():
    lines=(ROOT/'tmp/rogue-tier-two/client/compile.args').read_text().splitlines()
    cp=lines[lines.index('"-cp"')+1].strip('"').split(';')
    for p in (Path.home()/'.gradle/caches/modules-2/files-2.1/mezz.jei').rglob('*.jar'):
        if '30.39.0.233' in str(p):cp.append(str(p))
    cp=';'.join(map(str,[DD,FD,*cp]))
    out=TMP/'probe-classes';out.mkdir(parents=True,exist_ok=True)
    args=['--release','25','-cp',cp,'-d',str(out),str(Path(__file__).with_name('VisibilityProbe.java'))]
    argfile=TMP/'probe-compile.args'
    argfile.write_text('\n'.join(json.dumps(a.replace('\\','/')) for a in args))
    subprocess.run([str(JAVA/'javac.exe'),'@'+str(argfile)],check=True,cwd=ROOT)
    jar=TMP/'delight-probe.jar'
    with zipfile.ZipFile(jar,'w',zipfile.ZIP_DEFLATED) as z:
        for p in out.rglob('*.class'):z.write(p,p.relative_to(out).as_posix())
        z.writestr('META-INF/neoforge.mods.toml','''modLoader="javafml"
loaderVersion="[4,)"
license="MIT"
[[mods]]
modId="delight_probe"
version="1"
displayName="Isolated Delight Regression"
''')
    return jar

def launch(label,dd,addons,probe=None):
    game=TMP/label;game.mkdir(parents=True,exist_ok=True)
    mods=game/'mods';mods.mkdir(exist_ok=True)
    assert not list(mods.glob('*.jar')),f'Use a new test label: {game}'
    for p in [FD,EXP,dd,probe or compile_probe(),*addons]:shutil.copy2(p,mods/p.name)
    libraries=ROOT/'tmp/pvp-26.2/server/libraries'
    if not (game/'libraries').exists():
        subprocess.run(['cmd','/c','mklink','/J',str(game/'libraries'),str(libraries)],check=True,capture_output=True)
    (game/'eula.txt').write_text('eula=true\n')
    (game/'server.properties').write_text('online-mode=false\nserver-port=0\nlevel-type=minecraft:flat\nview-distance=2\nsimulation-distance=2\nmax-tick-time=-1\nspawn-protection=0\ngenerator-settings={"layers":[{"height":1,"block":"minecraft:bedrock"},{"height":2,"block":"minecraft:dirt"},{"height":1,"block":"minecraft:grass_block"}],"biome":"minecraft:plains","structures":{}}\n')
    args=shlex.split((libraries/'net/neoforged/neoforge/26.2.0.90/win_args.txt').read_text(),posix=True)
    log=(game/'console.log').open('w',encoding='utf-8')
    process=subprocess.Popen([str(JAVA/'java.exe'),'-Xmx2G',*args,'nogui'],cwd=game,stdout=log,stderr=subprocess.STDOUT)
    (game/'server.pid').write_text(str(process.pid));print(json.dumps({'pid':process.pid,'log':str(game/'console.log')}))

if __name__=='__main__':
    launch(sys.argv[1],Path(sys.argv[2]).resolve(),[Path(a).resolve() for a in sys.argv[3:]])
