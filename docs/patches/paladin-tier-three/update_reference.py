from pathlib import Path
from zipfile import ZipFile
import hashlib,json,re
R=Path(__file__).resolve().parents[3];W=R/'tmp/paladin-tier-three';p=R/'mod fixing/Paladin skills - raw attributes and nodes.md';s=p.read_text(encoding="utf-8")
s=s.replace('Inspected **28 October 2026**','Inspected **29 October 2026**').replace('| III | Battle Banner | Immolation |','| III | Battle Banner | Purgatory |')
s=re.sub(r'- Immolation blends.*?\n','- Purgatory uses holy hybrid power for pulses and M for its finishing explosion. Tier-I attacks use M for damage and P for healing. The old Divine Hammer attack is retired.\n',s)
s=s.replace('Banner and Consecration pulse intervals also scale with haste; their durations remain fixed.','Battle Banner and Purgatory have fixed 0.5s pulses; haste changes their cooldowns, never their pulse schedules or Purgatory stages.')
a=s.index('## Tier III -');b=s.index('## Registered status attributes',a);section=(Path(__file__).parent/'reference-tier-three.md').read_text(encoding='utf-8');s=s[:a]+section+'\n'+s[b:]
s=s.replace('| `paladins:battle_banner` | Attack speed, spell haste, ranged haste and knockback resistance each+0.4 ADD_MULTIPLIED_BASE |2s refreshed |','| `paladins:battle_banner` | Independent caster-snapshotted source records; strongest unexpired amount per stat |3s refreshed |')
s=s.replace('| `skill_tree_rpgs:banner_protection` | Incoming-damage attribute-0.3 ADD_MULTIPLIED_BASE |2s refreshed |','| `skill_tree_rpgs:banner_protection` | Legacy effect retained; no native attribute or current application |Not applied |')
s=s.replace('| `skill_tree_rpgs:ardent_defender` | Maximum health+1.0 ADD_MULTIPLIED_BASE |10s default |','| `skill_tree_rpgs:ardent_defender` | +10 armor and +5% general reduction; separate Resistance I |10s |\n| `skill_tree_rpgs:purgatory` | Stationary channel with orange weapon glint |Until channel ends/dispel/cleanup |\n| `skill_tree_rpgs:paladin_sanctity` | Ordinary damage immunity; direct costs/void/admin kills excluded |0.2?0.7s final pulse;2s Banner rescue;3s Final Resolute |\n| `skill_tree_rpgs:purgatory_ward` | Tracked absorption grant, never refills spent shield on refresh |8s |\n| `skill_tree_rpgs:reinforced` | +5% base armor and Healing Power per low-health band |0.25s checks |')
s=s.replace('Later tier coefficients remain unchanged; cloud pulse intervals now scale with haste.','Tier III is rebuilt by the [current implementation and native/client checks](../docs/patches/paladin-tier-three/README.md), including fixed pulse timing, channel endings and anti-refill shields.')
s=s.replace('Protection and Protective Banner use the stronger reduction.','Current Banner general and additional magic reduction are independent layers; the former Protective Banner modifier is retired.')
files={}
for pattern,namespace in [('paladins-*','paladins'),('skill_tree-*','skill_tree_rpgs')]:
 with ZipFile(next((W/'built').glob(pattern))) as z:
  for n in z.namelist():
   if n.startswith(f'data/{namespace}/spell/') and n.endswith('.json') and (namespace=='paladins' or '/paladin_' in n):files[namespace+':'+Path(n).stem]=json.loads(z.read(n))
# Replace existing exact resource blocks; preserve placement/extraction annotations outside code fences.
for name,value in files.items():
 pattern=r'(### `'+re.escape(name)+r'`\n[\s\S]*?```json\n)[\s\S]*?(\n```)'
 s=re.sub(pattern,lambda m:m[1]+json.dumps(value,ensure_ascii=False,indent=2)+m[2],s,count=1)
for name in ['paladins:banner_visual','paladins:purgatory_pulse','paladins:purgatory_burst']:
 if '### `'+name+'`' not in s:s+='\n### `'+name+'`\n\n```json\n'+json.dumps(files[name],ensure_ascii=False,indent=2)+'\n```\n'
for f in (W/'built').glob('*.jar'):
 sha=hashlib.sha256(f.read_bytes()).hexdigest();s=re.sub(r'(`'+re.escape(f.name)+r'`[^\n]*?SHA-256 `)[a-f0-9]{64}',lambda m:m[1]+sha,s)
# Keep exact category definitions aligned with renamed node title keys.
with ZipFile(next((W/'built').glob('skill_tree-*'))) as z:
 defs=json.loads(z.read('data/skill_tree_rpgs/puffish_skills/categories/class_skills/definitions.json'))
 a=s.index('### Definitions');b=s.index('### Class-category placements',a);part=s[a:b];start=part.index('```json\n')+8;end=part.index('\n```',start);old=json.loads(part[start:end]);old={key:defs.get(key,value) for key,value in old.items()};part=part[:start]+json.dumps(old,ensure_ascii=False,indent=2)+part[end:];s=s[:a]+part+s[b:]
p.write_text(s,encoding='utf-8');print('Updated Paladin reference, exact resource appendix and hashes')
