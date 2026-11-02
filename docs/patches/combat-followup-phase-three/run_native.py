from pathlib import Path
import sys
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent
s=(R/'docs/patches/ward-hit-retirement/run_probe.py').read_text().replace("W=R/'tmp/ward-hit-retirement'","W=R/'tmp/combat-followup-phase-three'").replace("str(H/'WardRetirementProbe.java')","str(H/'FrenzyNativeProbe.java')").replace("(W/'classpath.txt').read_text()","(R/'tmp/combat-followup-phase-one/build-classpath.txt').read_text()")
s=s.replace("for p in (R/'mods').glob('curios-*.jar'):","for p in list((R/'mods').glob('crossarms-*'))+list((R/'mods').glob('expanded-crossbow-*'))+list((R/'mods').glob('curios-*.jar')):")
if '--arrows' in sys.argv:
 s=s.replace("C=W/'spatial-classes'","C=W/'arrow-classes'")
 s=s.replace("str(H/'FrenzyNativeProbe.java')","str(H/'ArrowNearMissProbe.java')")
 s=s.replace("label='baseline' if baseline else 'candidate'","label='arrows-baseline' if baseline else 'arrows-candidate'")
 s=s.replace("if not result.exists() or not result.read_text().startswith('FAIL' if baseline else 'PASS')","if not result.exists() or not result.read_text().startswith('PASS')")
exec(compile(s,str(__file__),'exec'))
