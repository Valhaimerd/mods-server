"""Synchronize current player references; preserve unrelated user edits."""
from pathlib import Path
from zipfile import ZipFile
import json
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/class-balance-phases-1-3'
def transform(path,fn):
 s=path.read_text(encoding='utf-8');path.write_text(fn(s),encoding='utf-8')
def guide(s):
 s=s.replace('Arcane Barrage bolts deal 32%','Arcane Barrage bolts deal 40%').replace('at 19% of its own Fire power','at 24% of its own Fire power').replace('deal 44% of their own power','deal 55% of their own power').replace('Super Nova deals 145%','Super Nova deals 180%')
 s=s.replace('both projectiles travel at speed 1.6','Throw Weapon travels at speed 1.8 and Throw Net at 1.4').replace('Endured Vitality grants two armor','Endured Vitality grants one armor')
 s=s.replace('healing newly added HP and fully healing on pulse five; it retains Regeneration I and knockback immunity','healing newly added HP and another 20% current maximum HP on every pulse; it retains knockback immunity')
 s=s.replace('Magistrate ends with +10% Magic Damage including Healing Power, +5% spell haste and +5% magic resistance','Magistrate ends with +10% Magic Damage including Healing Power, +5% spell haste and +15% magic resistance')
 s=s.replace('Ordinary physical damage nodes grant +3% melee','Ordinary physical damage nodes grant +2% melee')
 old=next((line for line in s.splitlines() if line.startswith('Defensive offers **Aegis**')),None)
 if old:s=s.replace(old,'Defense has three exclusive eight-point paths, each with seven ordinary nodes and one normal-sized specialty. Both final branches must connect to and unlock the specialty. Full Aegis grants +4 HP, +2 armor, 4% general Resistance and 4% healing received; surviving a direct enemy hit grants another 15% damage reduction for 3s, once per 20s. Full Colossus grants +6 HP, +2 armor, 2% general Resistance and 8% healing received; a direct hit below 40% health starts recovery of 10% sampled maximum HP over 5s before healing bonuses, once per 30s. Full Bulwark grants +2 HP, +4 armor, 8% general Resistance and 20 knockback-resistance points. General Resistance covers physical and magic damage. Defense healing rewards do not increase outgoing Healing Power. Existing Defense allocations receive a one-time scoped refund; other choices, points and earned XP remain.')
 s=s.replace('Emergency Rations restores four drumsticks at three or less','Emergency Rations fully restores hunger and saturation at three drumsticks or less')
 s=s.replace('Only Utilities allocations are refunded once for this update; combat/Companion choices and earned XP remain.','Utilities masteries use compact melon, diamond-boots and writable-book icons, with five chain nodes and two required final branches. Existing Utility benefits and cooldowns remain.')
 s=s.replace('retains 42 trinkets and disables 12 relics','retains 41 trinkets and disables 13 relics').replace('Nullstone reduces recognized magic damage by 15%.','Fractured Nullstone is retired; saved copies are inert.')
 if 'Rogue pressure changes:' not in s:
  anchor=next((line for line in s.splitlines() if line.startswith('Archers, Paladins & Priests, Rogues & Warriors')),None)
  if anchor:s=s.replace(anchor,anchor+'\n\nRogue pressure changes: Shock Powder has a 12s base cooldown, Smoke Screen grants 65 evasion points, and Explosive Powder adds Weakness I and Blindness I for 3s. Coated Blades prevents natural regeneration and Regeneration for 8s while leaving direct healing and absorption available. Vital Hit replaces Fracture: each stack removes 1 armor plus 1 per complete 13 Dual Wield power, and each proc deals physical damage equal to 5% target maximum HP before defenses. Shadowstep roots its selected enemy for 1s. Bear Trap lasts 25s; Serrated Bleed lasts 5s, and Extensive Coverage hits within 2 blocks. Bloody Strikes applies Bleed and prevents rolling for 5s. Sprinting and rolling preserve base Vanish, retaining its movement penalty and other break rules.')
 if 'Both Warrior ultimates deliver' not in s:
  anchor=next((line for line in s.splitlines() if line.startswith('Berserker channels five pulses')),None)
  if anchor:s=s.replace(anchor,anchor+' Both Warrior ultimates deliver five pulses every 0.8s over 4s before haste, earning 40/45/50/55/60s base cooldowns. Berserker fatal rescue stuns for 8s. Wild Charge with Endurance grants 8 total armor.')
 if 'Multi Roll grants at most two additional rolls' not in s:
  anchor='#### Combat Roll'
  if anchor in s:s=s.replace(anchor,anchor+'\n\nMulti Roll grants at most two additional rolls: +1 at level I, +2 at II and III. Higher saved levels also stop at +2; other roll sources remain separate.')
 return s
transform(R/'docs/MODLIST.md',guide)
def root(s):
 s=guide(s)
 old=next((line for line in s.splitlines() if line.startswith('The [combat phase-one update]')),None)
 if old:s=s.replace(old,'The [combined class-balance phases 1-3](docs/patches/class-balance-phases-1-3/README.md) restore the original mage coefficients, make ordinary RPG magic respect armor, rebuild general Defense and Utilities progression, and update Rogue/Warrior skills. Magistrate grants 15% magic resistance; Fractured Nullstone is retired. General Resistance, Pain Suppression and frontal shields cover physical and magic damage without duplicate reduction. The earlier spatial, sequential Echoing Blast recharge, ward, melee and targeting fixes remain. Later class phases are still planned.')
 return s
transform(R/'README.md',root)
# Replace only current inventory policy; retain historical verification artifacts.
with ZipFile(next((W/'built').glob('Nameless*'))) as z:(R/'docs/patches/nameless-balance/balance.json').write_bytes(z.read('nameless-pack-balance.json'))
p=R/'docs/patches/accessory-rpg-balance/inventory.json';inventory=json.loads(p.read_text(encoding='utf-8'));p.write_text(json.dumps([row for row in inventory if not(row['mod']=='nameless_trinkets' and row['id']=='fractured_nullstone')],ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
transform(R/'docs/patches/accessory-rpg-balance/inventory.md',lambda s:'\n'.join(line for line in s.replace('Nameless Trinkets - 42 available','Nameless Trinkets - 41 available').split('\n') if not line.startswith('| Fractured Nullstone (')))
for filename in ['Rogue skills - raw attributes and nodes.md','Warrior skills - raw attributes and nodes.md','Magic spells - raw attributes and nodes.md','RPG Skills - description rewrite.md','Nameless Trinkets - effects.md']:
 p=R/'mod fixing'/filename
 if not p.exists():continue
 note='\n> **Current update - 4 November 2026:** [Combined phases 1-3](../docs/patches/class-balance-phases-1-3/README.md) and its [exact resource snapshot](../docs/patches/class-balance-phases-1-3/current-resources.json) supersede the affected values in older exports below. Later class phases remain planned.\n'
 transform(p,lambda s:s[:s.index('\n')+1]+note+s[s.index('\n')+1:] if note not in s else s)
for folder in ['combat-phase-one','combat-followup-phase-one','utilities-paths','nameless-balance']:
 p=R/'docs/patches'/folder/'README.md'
 if p.exists():
  note='\n> Affected current values are superseded by [the combined phases 1-3 update](../class-balance-phases-1-3/README.md). This document retains its historical implementation and test evidence.\n'
  transform(p,lambda s:s[:s.index('\n')+1]+note+s[s.index('\n')+1:] if note not in s else s)
snapshot={}
for p in (W/'built').glob('*.jar'):
 with ZipFile(p) as z:
  rows={n:json.loads(z.read(n)) for n in z.namelist() if n.startswith('data/') and n.endswith('.json') and '/spell/' in n}
  if rows:snapshot[p.name]=rows
(H/'current-resources.json').write_text(json.dumps(snapshot,indent=2)+'\n',encoding='utf-8')
print('Synchronized current references, inventory and exact spell resources.')
