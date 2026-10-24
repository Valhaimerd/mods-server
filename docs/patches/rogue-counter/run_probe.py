from pathlib import Path
import shutil,sys
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;work=ROOT/'tmp/rogue-counter';work.mkdir(exist_ok=True)
# Reuse the current broad fixture; its native Rogue tests receive updated expectations.
source=(ROOT/'docs/patches/tenacity-followup/run_probe.py').read_text().replace('tmp/tenacity-followup','tmp/rogue-counter')
runner=(ROOT/'docs/patches/warrior-tier-three/run_probe.py').read_text()
runner=runner.replace("ROOT/'docs/patches/rogue-tier-two/RogueTierTwoProbe.java'","ROOT/'docs/patches/rogue-rebalance/RogueTierTwoProbe.java',ROOT/'docs/patches/rogue-rebalance/RogueProbe.java',ROOT/'docs/patches/rogue-rebalance/ShadowResetProbe.java',ROOT/'docs/patches/rogue-counter/RogueTierThreeProbe.java',ROOT/'docs/patches/rogue-counter/CounterProbe.java'")
runner=runner.replace(".write_text(probe)",".write_text(probe.replace('RogueTierTwoProbe.run(world,player,results::add);','RogueProbe.run(world,player,results::add);RogueTierTwoProbe.run(world,player,results::add);ShadowResetProbe.run(world,player,results::add);RogueTierThreeProbe.run(world,player,results::add);'))")
if '--baseline' in sys.argv:runner=runner.replace("for p in (WORK/'built').glob('*.jar'):shutil.copy2(p,mods/p.name)","pass # baseline uses current active archives")
# Existing followup performs only two runner substitutions; preserve those then execute.
source=source.replace("runner=(ROOT/'docs/patches/warrior-tier-three/run_probe.py').read_text()",'runner='+repr(runner))
exec(compile(source,str(__file__),'exec'))
