from pathlib import Path
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent
s=(R/'docs/patches/ward-hit-retirement/run_probe.py').read_text().replace("W=R/'tmp/ward-hit-retirement'","W=R/'tmp/combat-followup-phase-two'").replace("str(H/'WardRetirementProbe.java')","str(H/'LifecycleProbe.java')").replace("(W/'classpath.txt').read_text()","(W/'build-classpath.txt').read_text()")
s=s.replace("shutil.copy2(result,H/(label+'-result.txt'));shutil.copy2(S/'phase-two-checks.txt',H/(label+'-checks.txt'))","shutil.copy2(result,H/'lifecycle-result.txt');shutil.copy2(S/'phase-two-checks.txt',H/'lifecycle-checks.txt')")
s=s.replace("(H/(label+'-tested.json'))","(H/'lifecycle-tested.json')")
exec(compile(s,str(__file__),'exec'))
