from pathlib import Path
from zipfile import ZipFile
import subprocess
R=Path(__file__).resolve().parents[3];W=R/'tmp/combat-followup-phase-three';J='C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin/java.exe'
mc=R/'tmp/better-revive-regression/server/libraries/net/neoforged/minecraft-client-patched/26.2.0.90/minecraft-client-patched-26.2.0.90.jar'
if not mc.exists():mc=Path(__import__('os').environ['APPDATA'])/'.minecraft/libraries/net/neoforged/minecraft-client-patched/26.2.0.90/minecraft-client-patched-26.2.0.90.jar'
with ZipFile(mc) as z:
 for n in ['net/minecraft/client/multiplayer/MultiPlayerGameMode.class','net/minecraft/client/player/LocalPlayer.class']:
  p=W/'inspect'/n;p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(z.read(n));subprocess.run([J,'-jar',str(R/'tmp/bows-balance/cfr.jar'),str(p),'--outputdir',str(W/'decompiled'),'--silent','true'],stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL)
for pattern in ['ranged_weapon_api-*','expanded-crossbow-*','archers-*']:
 with ZipFile(next((R/'mods').glob(pattern))) as z:
  for n in z.namelist():
   if n.endswith('.class') and ('Crossbow' in n or 'ClientPlayer' in n or 'autofire' in n):
    p=W/'inspect'/n;p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(z.read(n));subprocess.run([J,'-jar',str(R/'tmp/bows-balance/cfr.jar'),str(p),'--outputdir',str(W/'decompiled'),'--silent','true'],stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL)
for jar in (W/'candidate/client/mods').glob('*.jar'):
 with ZipFile(jar) as z:
  for n in z.namelist():
   if n.endswith('.class'):
    b=z.read(n)
    if (b'isCharged' in b or b'CHARGED_PROJECTILES' in b or b'CrossbowItem' in b) and (b'stopUsingItem' in b or b'releaseUsingItem' in b or b'setDown' in b):
     print(jar.name,n);p=W/'inspect'/n;p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(b);subprocess.run([J,'-jar',str(R/'tmp/bows-balance/cfr.jar'),str(p),'--outputdir',str(W/'decompiled'),'--silent','true'],stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL)
