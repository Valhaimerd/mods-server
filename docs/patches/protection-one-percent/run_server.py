from pathlib import Path
from zipfile import ZipFile,ZIP_DEFLATED
import subprocess,shutil,uuid,sys,json,hashlib
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/protection-one-percent'
baseline='--baseline' in sys.argv;legacy='--legacy' in sys.argv;label=('baseline' if baseline else 'candidate')+('-legacy' if legacy else '')
server=WORK/(label+'-'+uuid.uuid4().hex[:8]);server.mkdir();mods=server/'mods';mods.mkdir()
for p in (ROOT/'tmp/priest-tier-three/server/mods').glob('*.jar'):
    if 'probe' not in p.name:shutil.copy2(ROOT/'mods'/p.name,mods/p.name)
shutil.copy2(next((ROOT/'mods').glob('bettercombat-*.jar')),mods/next((ROOT/'mods').glob('bettercombat-*.jar')).name)
shutil.copy2(next((ROOT/'mods').glob('pvp_flagging-*.jar')),mods/next((ROOT/'mods').glob('pvp_flagging-*.jar')).name)
for pattern in ['*PuzzlesLib*','penchant-*','Patchouli-*','patchouli-*','UniversalEnchants-*','IronWolfArmor-*','Bookshelf-*','PrickleMC-*']:
 for p in (ROOT/'mods').glob(pattern+'.jar'):shutil.copy2(p,mods/p.name)

for p in (WORK/('before' if baseline else 'built')).glob('*.jar'):shutil.copy2(p,mods/p.name)

if legacy:
    dest=server/'config/skill_tree_rpgs';dest.mkdir(parents=True,exist_ok=True)
    (dest/'effects.json').write_text(json.dumps({'effects':{k:{'attributes':[{'attribute':attribute,'value':value,'operation':'ADD_MULTIPLIED_BASE'}]} for k,attribute,value in [('skill_tree_rpgs:supercharge','ranged_weapon:haste',-.25),('skill_tree_rpgs:incanter_cadence','spell_power:haste',.02),('skill_tree_rpgs:warrior_momentum','minecraft:movement_speed',.02),('skill_tree_rpgs:opportunist_window','critical_strike:chance',1)]}}))
subprocess.run(['cmd','/c','mklink','/J',str(server/'libraries'),str(ROOT/'tmp/pvp-26.2/server/libraries')],check=True,capture_output=True)
(server/'eula.txt').write_text('eula=true\n')
(server/'server.properties').write_text('online-mode=false\nlevel-type=minecraft:flat\nserver-port=0\nview-distance=2\nsimulation-distance=2\nmax-tick-time=-1\n')
lines=(ROOT/'tmp/rogue-tier-two/client/compile.args').read_text().splitlines();cp=lines[lines.index('"-cp"')+1].strip('"')
cp=';'.join(str(p) for p in mods.glob('*.jar'))+';'+cp
classes=WORK/'server-probe-classes';classes.mkdir(exist_ok=True)
java=Path('C:/Users/Valhaimerd/.gradle/jdks/eclipse_adoptium-25-amd64-windows.2/bin')
args=['--release','25','-proc:none','-cp',cp,'-d',str(classes),str(HERE/'ProtectionProbe.java')]
argfile=WORK/'probe.args';argfile.write_text('\n'.join(json.dumps(a.replace('\\','/')) for a in args))
subprocess.run([str(java/'javac.exe'),'@'+str(argfile)],check=True)
with ZipFile(mods/'paladin-doctrine-probe.jar','w',ZIP_DEFLATED) as z:
    z.writestr('META-INF/neoforge.mods.toml','modLoader="javafml"\nloaderVersion="[4,)"\nlicense="MIT"\n[[mods]]\nmodId="paladin_doctrine_probe"\nversion="1"\ndisplayName="Isolated Paladin doctrine regression"\n')
    z.writestr('data/c/tags/damage_type/is_magic.json',json.dumps({'values':['minecraft:falling_block']}))
    for p in classes.rglob('*.class'):z.write(p,p.relative_to(classes).as_posix())
with (WORK/(label+'.log')).open('w',encoding='utf-8') as log:
    r=subprocess.run([str(java/'java.exe'),'-Xmx2G','-Xverify:all','@libraries/net/neoforged/neoforge/26.2.0.90/win_args.txt','nogui'],cwd=server,stdout=log,stderr=subprocess.STDOUT,timeout=180)
result=server/'paladin-result.txt';assert result.exists(),'No result: inspect '+str(WORK/(label+'.log'))
print(result.read_text());

shutil.copy2(result,HERE/(label+'-result.txt'));shutil.copy2(server/'paladin-checks.txt',HERE/(label+'-checks.txt'))
(HERE/(label+'-tested.json')).write_text(json.dumps({p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in mods.glob('*.jar')},indent=2)+'\n')
assert result.read_text().startswith('FAIL' if baseline else 'PASS'),result.read_text()
