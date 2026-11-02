from pathlib import Path
from zipfile import ZipFile
import subprocess,json
R=Path(__file__).resolve().parents[3];W=R/'tmp/combat-followup-phase-one';J=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
cp=';'.join(str(p) for p in (R/'mods').glob('*.jar'))+';'+(R/'tmp/purgatory-continuous-pull/classpath.txt').read_text();(W/'classpath.txt').write_text(cp)
for name in ['net.spell_engine.internals.delivery.melee.Melee','net.spell_engine.internals.SpellExecution','net.spell_engine.api.spell.event.SpellHandlers','net.spell_engine.api.spell.Spell$Target$Aim','net.combat_roll.internals.RollManager','net.combat_roll.network.ServerNetwork','net.spell_engine.api.spell.Spell$Modifier','net.spell_engine.internals.SpellTriggers$Event','net.spell_engine.entity.DamageSourceExtension']:
 q=subprocess.run([str(J/'javap.exe'),'-p','-classpath',';'.join(str(p) for p in (W/'before').glob('*.jar')),name],capture_output=True,text=True);(W/(name.split('.')[-1].replace('$','-')+'.txt')).write_text(q.stdout+q.stderr)
for prefix in ['skill_tree-','rogues-']:
 with ZipFile(next((W/'before').glob(prefix+'*'))) as z:
  matches={n:json.loads(z.read(n)) for n in z.namelist() if n.endswith('definitions.json') and ('categories/' in n)}
  (W/(prefix+'definitions.json')).write_text(json.dumps(matches,indent=2))
