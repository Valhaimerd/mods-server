from pathlib import Path
from zipfile import ZipFile
import subprocess,shutil,json,hashlib
R=Path(__file__).resolve().parents[3];W=R/'tmp/combat-followup-phase-two';J=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
records=[]
for prefix in ['spell_engine-','bettercombat-','paladins-','Nameless Trinkets-']:
 matches=list((R/'mods').glob(prefix+'*.jar'))
 if not matches:continue
 p=matches[0];b=W/'before'/p.name
 if not b.exists():shutil.copy2(p,b)
 records.append({'jar':p.name,'sha256':hashlib.sha256(b.read_bytes()).hexdigest()})
(W/'baseline.json').write_text(json.dumps(records,indent=2))
mc=R/'tmp/better-revive-regression/server/libraries/net/neoforged/minecraft-server-patched/26.2.0.90/minecraft-server-patched-26.2.0.90.jar'
targets={mc:['net/minecraft/server/network/ServerGamePacketListenerImpl.class','net/minecraft/server/level/ServerPlayer.class','net/minecraft/world/entity/player/Player.class','net/minecraft/world/entity/LivingEntity.class','net/minecraft/world/item/component/AttackRange.class'],next((W/'before').glob('spell_engine-*')):['net/spell_engine/internals/PurgatoryBoundary.class','net/spell_engine/internals/PartyRules.class'],next((W/'before').glob('bettercombat-*')):['net/bettercombat/network/ServerNetwork.class','net/bettercombat/mixin/player/PlayerEntityRangeMixin.class','net/bettercombat/network/Packets$C2S_AttackRequest.class'],next((W/'before').glob('paladins-*')):['net/paladins/entity/BarrierEntity.class']}
for jar,entries in targets.items():
 with ZipFile(jar) as z:
  for n in entries:
   p=W/'inspect'/n;p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(z.read(n));subprocess.run([str(J/'java.exe'),'-jar',str(R/'tmp/bows-balance/cfr.jar'),str(p),'--outputdir',str(W/'decompiled'),'--silent','true'],stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL,check=True)
print('Captured current inputs and decompiled movement/attack paths')
