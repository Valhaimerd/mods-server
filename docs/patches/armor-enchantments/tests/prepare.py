from pathlib import Path
from zipfile import ZipFile
import subprocess,shutil,json,os,argparse
a=argparse.ArgumentParser();a.add_argument("--candidate",type=Path);args_cli=a.parse_args()
root=Path.cwd();w=root/'tmp/armor-enchantments';s=w/'server'
for pat in ['penchant*','patchouli*','UniversalEnchants*','PuzzlesLib*','EnderZoology*','dungeons-and-taverns*','air-jump*','protection-grace*','ly-fangs*']:
 p=next((root/'mods').glob(pat));shutil.copy2(p,s/'mods'/p.name)
(s/'eula.txt').write_text('eula=true\n');(s/'server.properties').write_text('online-mode=false\nserver-port=25678\nlevel-type=minecraft:flat\ngenerate-structures=false\nspawn-protection=0\nview-distance=2\nsimulation-distance=2\nmax-tick-time=180000\ninitial-disabled-packs=mod/penchant:resourcepacks/no_anvil_books\n')
cp=(root/'tmp/bows-balance/classpath.txt').read_text().strip();cp+=';'+str(next((root/'mods').glob('penchant*')))
args=['--release','25','-encoding','UTF-8','-cp',cp,'-d',str(w/'classes'),str(root/'docs/patches/armor-enchantments/tests/ArmorProbe.java')]
(w/'compile.args').write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args))
subprocess.run(['C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin/javac.exe','@'+str(w/'compile.args')],check=True)
ids=['protection_grace_enchantment:protection_grace','nova_structures:antidote','nova_structures:traveler','enderzoology:soulbound','air_jump_enchantment:air_jump']
with ZipFile(s/'mods/armor-probe.jar','w') as z:
 z.writestr('META-INF/neoforge.mods.toml','modLoader="javafml"\nloaderVersion="[1,)"\nlicense="MIT"\n[[mods]]\nmodId="armor_probe"\nversion="1"\ndisplayName="Armor rule probe"\n')
 for p in (w/'classes').rglob('*.class'):z.write(p,p.relative_to(w/'classes').as_posix())
 for id in ids:
  z.writestr('data/armor_probe/loot_table/hidden/'+id.replace(':','/')+'.json',json.dumps({'type':'minecraft:chest','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'minecraft:book','functions':[{'function':'minecraft:enchant_randomly','options':[id],'only_compatible':False}]}]}]}))
if args_cli.candidate:shutil.copy2(args_cli.candidate,s/'mods'/args_cli.candidate.name)
print('Test fixture ready; launch from tmp/armor-enchantments/server with NeoForge 26.2.0.90 win_args.txt')
