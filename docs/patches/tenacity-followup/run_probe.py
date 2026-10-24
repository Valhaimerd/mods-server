"""Reuse the prior ultimate regression fixture with independent new boundary assertions."""
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3];WORK=ROOT/'tmp/tenacity-followup';WORK.mkdir(exist_ok=True)
probe=(ROOT/'docs/patches/warrior-tier-three/WarriorUltimatesProbe.java').read_text()
probe=probe.replace('p.setHealth(p.getMaxHealth()*.4f);WarriorUltimates.tenacity(p);','p.setHealth(p.getMaxHealth()*.4f);WarriorUltimates.tenacity(p);eq("Tenacity inactive at forty percent",p.getAbsorptionAmount(),0);eq("Tenacity no cooldown above threshold",((SpellCaster.Player)p).getCooldownManager().isCoolingDown(spell("skill_tree_rpgs:warrior_tier_3_passive_2"))?1:0,0);p.setHealth(p.getMaxHealth()*.3f);WarriorUltimates.tenacity(p);')
probe=probe.replace('getDuration(),WarriorUltimates.TENACITY_TICKS','getDuration(),240')
(WORK/'WarriorUltimatesProbe.java').write_text(probe)
runner=(ROOT/'docs/patches/warrior-tier-three/run_probe.py').read_text().replace("WORK=ROOT/'tmp/warrior-tier-three'","WORK=ROOT/'tmp/tenacity-followup'").replace("HERE/'WarriorUltimatesProbe.java'","WORK/'WarriorUltimatesProbe.java'")
exec(compile(runner,str(__file__),'exec'))
