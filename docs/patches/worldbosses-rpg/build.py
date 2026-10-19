"""Build the same-filename World Bosses candidate, leaving the installed JAR untouched.

python docs/patches/worldbosses-rpg/build.py --classpath-file tmp/worldbosses-rpg/classpath.txt
Input: tmp/worldbosses-rpg/upstream.jar (hash pinned). Java 25+ and ASM 9.10.1.
"""
from pathlib import Path
from zipfile import ZipFile, ZipInfo, ZIP_DEFLATED
import argparse, hashlib, json, subprocess

HERE=Path(__file__).resolve().parent
ROOT=HERE.parents[2]
WORK=ROOT/'tmp/worldbosses-rpg'
NAME='world-bosses-neoforge-26.2-2.0.0.jar'
SHA='20b63910decdb07caabf90dc0c0620aa6fdd93982b1a44ec84f140a39b481dc7'
p=argparse.ArgumentParser()
p.add_argument('--classpath-file',required=True)
p.add_argument('--java-bin',default='C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
args=p.parse_args();java=Path(args.java_bin)
assert hashlib.sha256((WORK/'upstream.jar').read_bytes()).hexdigest()==SHA
cp=Path(args.classpath_file).read_text(encoding='utf-8').strip()
for stale in (WORK/'classes').rglob('*.class'):
    assert stale.resolve().is_relative_to((WORK/'classes').resolve())
    stale.unlink()
sources=sorted((HERE/'src').rglob('*.java'))+[HERE/'tools/PatchWorldBosses.java']
compile_args=['--release','25','-proc:none','-encoding','UTF-8','-cp',cp,'-d',str(WORK/'classes'),*[str(f) for f in sources]]
(WORK/'compile.args').write_text('\n'.join('"'+x.replace('\\','/')+'"' for x in compile_args),encoding='utf-8')
subprocess.run([str(java/'javac.exe'),'@'+str(WORK/'compile.args')],check=True)
patchcp=str(WORK/'classes')+';'+';'.join(p for p in cp.split(';') if '/asm/' in p.replace('\\','/') or '/asm-tree/' in p.replace('\\','/'))
subprocess.run([str(java/'java.exe'),'-cp',patchcp,'PatchWorldBosses',str(WORK/'upstream.jar'),str(WORK/'patched')],check=True)
retired={f'worldbosses:{family}_{suffix}' for family in ['worldbreaker','shadowbreaker'] for suffix in ['sword','axe','pickaxe','shovel','hoe','helmet','chestplate','leggings','boots','shield']}
hidden=retired|{'worldbosses:'+x for x in ['worldbreaker_alloy','shadowbreaker_alloy','worldbreaker_upgrade_template','shadowbreaker_upgrade_template','worldbreaker_fragment','shadow_fragment']}
removed=[];changed=[];removed_advancements=[]
mixins={'required':True,'minVersion':'0.8','package':'local.worldbosses.mixin','compatibilityLevel':'JAVA_25','mixins':['HealthRangeMixin','RetiredStackMixin','BossEffectMixin','BossMotionMixin','BossDotMixin','HealingCreditMixin'],'injectors':{'defaultRequire':1}}
with ZipFile(WORK/'upstream.jar') as src,ZipFile(WORK/NAME,'w',ZIP_DEFLATED) as dst:
    for entry in src.infolist():
        n=entry.filename;old=src.read(n);new=old
        if n=='data/worldbosses/advancement/shadowbreaker_armor.json':
            removed_advancements.append(n);continue
        if n.startswith('data/worldbosses/recipe/') and n.endswith('.json'):
            obj=json.loads(old)
            result=obj.get('result',{})
            output=result.get('id',result.get('item','')) if isinstance(result,dict) else result
            if output in hidden or n.endswith('/worldbreaker_gear_upgrade.json'):
                removed.append(n);continue
        if n.endswith('.class') and (WORK/'patched'/n).is_file():new=(WORK/'patched'/n).read_bytes()
        if n=='META-INF/neoforge.mods.toml':new+=b'\n[[mixins]]\nconfig="worldbosses-rpg.mixins.json"\n'
        if new!=old:changed.append(n)
        dst.writestr(entry,new)
    def addition(name,data):
        info=ZipInfo(name,(1980,1,1,0,0,0));info.compress_type=ZIP_DEFLATED;dst.writestr(info,data)
    for f in sorted((WORK/'classes/local/worldbosses').rglob('*.class')):
        addition(f.relative_to(WORK/'classes').as_posix(),f.read_bytes())
    addition('worldbosses-rpg.mixins.json',json.dumps(mixins,indent=2)+'\n')
    addition('worldbosses-rpg-build.json',json.dumps({'upstream_sha256':SHA,'profile_version':1,'retired_items':sorted(retired),'armor_ceiling':30,'health_ceiling':1000000},indent=2)+'\n')
report={'upstream_sha256':SHA,'output_sha256':hashlib.sha256((WORK/NAME).read_bytes()).hexdigest(),'removed_recipes':removed,'removed_advancements':removed_advancements,'changed_classes':len([n for n in changed if n.endswith('.class')])}
(WORK/'build-result.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8')
print(json.dumps(report,indent=2))
