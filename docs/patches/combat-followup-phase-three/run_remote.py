from pathlib import Path
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent
s=(R/'docs/patches/combat-followup-phase-two/run_remote.py').read_text().replace("W=R/'tmp/combat-followup-phase-two'","W=R/'tmp/combat-followup-phase-three'").replace('RemoteServerProbe','FrenzyServerProbe').replace('RemoteClientProbe','FrenzyClientProbe').replace("['Nameless Trinkets-*','arsenal-*']","['crossarms-*','expanded-crossbow-*','arsenal-*']")
s=s.replace(" if src=='FrenzyServerProbe':"," if False:")
exec(compile(s,str(__file__),'exec'))
