from pathlib import Path
import re,json,zipfile
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent
for filename in ['README.md','docs/MODLIST.md']:
 p=R/filename;s=p.read_text(encoding='utf-8')
 replacements={
  'Ancient Hydra continuously breathes fire at 24%':'Ancient Hydra continuously breathes fire at 19%',
  'ordinary Hydra Fireballs deal 55%':'ordinary Hydra Fireballs deal 44%',
  'Super Nova deals 180%':'Super Nova deals 145%',
  'Arcane Barrage bolts deal 40%':'Arcane Barrage bolts deal 32%',
  'Sinful Retaliation readies a mark and burn after rolling':'Sinful Retaliation readies a mark and a four-second true-damage retaliation after rolling',
  'Arcane Ward grants one absorption heart plus one heart per five Arcane power. Flame and Frost Wards grant one heart plus one per ten matching power. All three last eight seconds.':'Arcane, Flame and Frost Wards grant one absorption heart per eight matching school power, last eight seconds and have 15s base cooldowns. Refreshes preserve spent absorption; overlapping Wards do not add their shields.',
  'Warrior and Archer root/empowerment tooltips both show 2%.':'Warrior root/ordinary damage nodes give 4%; Archer damage nodes remain 2%. Rogue root/ordinary attack-speed nodes give 2%.',
 }
 for old,new in replacements.items():s=s.replace(old,new)
 if filename=='README.md':
  paragraph='The [combat phase-one update](docs/patches/combat-phase-one/README.md) reduces mage active/weapon/summon coefficients to rounded 80% values while retaining separate passive damage and healing. Echoing Blast stores two casts with sequential recharge; Missiles and Explosion have 12s base cooldowns and 40-block targeting. It also updates Wards, trap cooldowns, melee/Rogue progression, faster Paladin/Warrior preparation, Sinful Retaliation and Frenzy impact handling. Phase two remains deferred.\n\n'
  marker='The [local magic rework]';i=s.index(marker);s=s[:i]+paragraph+s[i:]
 else:
  marker='Wizard passive cooldowns scale with haste.'
  extra='Mage active, weapon and summon damage now uses rounded 80% coefficients; separate school passives and healing remain. Echoing Blast stores two casts and recovers one at a time per haste-adjusted 12s, with no hit resets. Missiles has a fixed 12s base cooldown; both first-tier Arcane skills target within 40 blocks. Purge Heal reaches 30 blocks, Blessed Strikes channels five pulses in 1.5s, Divine Oath casts in 0.25s and both Warrior throws fully charge in 0.3s. Purgatory has a 12s base cooldown. Frenzy arrows bypass ordinary hit i-frames while retaining defenses. '
  assert marker in s;s=s.replace(marker,extra+marker)
 p.write_text(s,encoding='utf-8')
# The focused current overlay supersedes historical raw exports without presenting old values as current.
for filename in ['Magic spells - raw attributes and nodes.md','Paladin skills - raw attributes and nodes.md','Warrior skills - raw attributes and nodes.md','RPG Skills - description rewrite.md']:
 p=R/'mod fixing'/filename
 if not p.exists():continue
 s=p.read_text(encoding='utf-8');line=s.index('\n')+1
 note='\n> **Current phase-one update — 2 November 2026:** [Combat values and behavior](../docs/patches/combat-phase-one/README.md) and [exact resource snapshot](../docs/patches/combat-phase-one/current-resources.json) supersede the historical coefficients, cooldowns and descriptions below for changed entries. The source export below is retained as historical investigation; it must not be used to restore an older JAR.\n'
 s=s[:line]+note+s[line:];p.write_text(s,encoding='utf-8')
rows={}
for src in (R/'tmp/combat-phase-one/built').glob('*.jar'):
 with zipfile.ZipFile(src) as z:
  selected={}
  for n in z.namelist():
   if '/spell/' in n and n.endswith('.json') and n.startswith('data/'):
    if any(ns in n for ns in ['/wizards/','/paladins/','/rogues/','/skill_tree_rpgs/']):selected[n]=json.loads(z.read(n))
  if selected:rows[src.name]=selected
def mechanical(v):
 if isinstance(v,dict):return {k:mechanical(x) for k,x in v.items() if k not in ['visuals','particles','animation','sound','sounds','start_sound','launch_sound','client_data','travel_particles']}
 if isinstance(v,list):return [mechanical(x) for x in v]
 return v
(H/'current-resources.json').write_text(json.dumps(mechanical(rows),indent=2)+'\n')
