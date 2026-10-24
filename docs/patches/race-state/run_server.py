from pathlib import Path
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/race-state';WORK.mkdir(exist_ok=True)
s=(ROOT/'docs/patches/race-balance/RaceBalanceProbe.java').read_text().replace('melee={0,0,2,3,4,5,5,5},range={0,0,1,1.5,2,2.5,2.5,2.5}','melee={2,2,2,3,4,5,5,5},range={1,1,1,1.5,2,2.5,2.5,2.5}').replace('"Healing removes melee bonus",attack.getValue(),1','"Healing restores starting melee bonus",attack.getValue(),3').replace('"Healing removes ranged bonus",ranged.getValue(),rbase','"Healing restores starting ranged bonus",ranged.getValue(),rbase+1')
s=s.replace('race(p,Race.HUMAN);arrow.discard();target.discard();','race(p,Race.HUMAN);arrow.discard();target.discard();DrawProbe.run(p,sink);LifecycleProbe.run(world,p,sink);').replace('public static void run(ServerLevel world,ServerPlayer p,Consumer<String> sink){','public static void run(ServerLevel world,ServerPlayer p,Consumer<String> sink)throws Exception{')
(WORK/'RaceBalanceProbe.java').write_text(s)
runner=(ROOT/'docs/patches/race-balance/run_server.py').read_text().replace("WORK=ROOT/'tmp/race-balance'","WORK=ROOT/'tmp/race-state'")
runner=runner.replace("str(HERE/'RaceBalanceProbe.java')","str(WORK/'RaceBalanceProbe.java'),str(ROOT/'docs/patches/orc-followup/DrawProbe.java'),str(HERE/'LifecycleProbe.java')")
runner=runner.replace('displayName="Race balance isolated probe"\\n', 'displayName="Race balance isolated probe"\\n[[mods]]\\nmodId="race_state_probe"\\nversion="1"\\n')
exec(compile(runner,str(__file__),'exec'))
