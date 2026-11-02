from pathlib import Path
from zipfile import ZipFile
import subprocess
R=Path(__file__).resolve().parents[3];W=R/'tmp/combat-followup-phase-two';J='C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin/javap.exe'
cp=';'.join(str(p) for p in (R/'mods').glob('*.jar') if any(x in p.name for x in ['party','bettercombat','Trinkets','curios']))
for name in ['dev.betterparty.party.PartyManager','dev.betterparty.party.VanillaTeamBridge','dev.betterparty.party.Party','com.cozary.nameless_trinkets.items.trinkets.TitansMarkBase']:
 q=subprocess.run([J,'-p','-classpath',cp,name],capture_output=True,text=True);(W/(name.split('.')[-1]+'.txt')).write_text(q.stdout+q.stderr)
