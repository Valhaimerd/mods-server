package local.pvp;
import java.nio.file.*;
import java.util.*;
import com.mojang.authlib.GameProfile;
import io.redspace.pvp_flagging.core.PlayerFlagManager;
import io.redspace.pvp_flagging.config.PvpConfig;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.GameType;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.spell_power.api.*;

@Mod("pvp_probe")
public class PvpProbe {
 int checks;
 void check(boolean b,String s){if(!b)throw new AssertionError(s);checks++;System.out.println("PVP_PASS "+s);}
 void near(double a,double b,String s){check(Math.abs(a-b)<.0001,s+" actual="+a+" expected="+b);}
 public PvpProbe(){NeoForge.EVENT_BUS.addListener(this::run);}
 ServerPlayer player(net.minecraft.server.MinecraftServer server,ServerLevel level,String name,String uuid)throws Exception{
  var profile=new GameProfile(UUID.fromString(uuid),name);
  var p=new ServerPlayer(server,level,profile,ClientInformation.createDefault());
  p.connection=new net.minecraft.server.network.ServerGamePacketListenerImpl(server,new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND),p,net.minecraft.server.network.CommonListenerCookie.createInitial(profile,false));
  p.connection.markClientLoaded();
  p.setInvulnerable(false);return p;
 }
 float hit(ServerLevel l,LivingEntity target,DamageSource source){target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);target.setHealth(100);target.invulnerableTime=0;target.hurtServer(l,source,20);return 100-target.getHealth();}
 @SuppressWarnings("unchecked")
 void run(ServerStartedEvent e){var server=e.getServer();try{
  var l=server.overworld();
  var a=player(server,l,"PvPAttacker","00000000-0000-0000-0000-000000001111");
  var b=player(server,l,"PvPVictim","00000000-0000-0000-0000-000000002222");
  a.setGameMode(GameType.SURVIVAL);b.setGameMode(GameType.SURVIVAL);a.getAbilities().invulnerable=false;b.getAbilities().invulnerable=false;
  var field=PlayerFlagManager.class.getDeclaredField("flaggedPlayers");field.setAccessible(true);
  var flagged=(Set<UUID>)field.get(PlayerFlagManager.INSTANCE);flagged.add(a.getUUID());flagged.add(b.getUUID());
  check(a.canHarmPlayer(b),"both flagged may fight");
  near(PvpConfig.SERVER.PVP_DAMAGE_MULIPLIER.get().doubleValue(),.5,"default multiplier .5");
  near(hit(l,b,l.damageSources().playerAttack(a)),10,"melee PvP halved");
  var arrow=EntityTypes.ARROW.create(l,EntitySpawnReason.COMMAND);
  near(hit(l,b,l.damageSources().arrow(arrow,a)),10,"projectile PvP halved");
  for(var school:List.of(SpellSchools.ARCANE,SpellSchools.FIRE,SpellSchools.FROST)){
   var source=SpellDamageSource.player(school,a);
   check(source.getEntity()==a,"spell caster attribution "+school.id);
   near(hit(l,b,source),10,"spell PvP halved "+school.id);
   var pig=EntityTypes.PIG.create(l,EntitySpawnReason.COMMAND);
   near(hit(l,pig,source),20,"spell PvE unchanged "+school.id);
  }
  PvpConfig.SERVER.PVP_DAMAGE_MULIPLIER.set(1D);
  near(hit(l,b,SpellDamageSource.player(SpellSchools.ARCANE,a)),10,"stale config cannot restore full PvP damage");
  flagged.remove(b.getUUID());check(!a.canHarmPlayer(b),"unflagged target protected");
  near(hit(l,b,l.damageSources().playerAttack(a)),0,"unflagged melee blocked");
  near(hit(l,b,SpellDamageSource.player(SpellSchools.ARCANE,a)),0,"unflagged spell blocked");
  flagged.add(b.getUUID());flagged.remove(a.getUUID());check(!a.canHarmPlayer(b),"unflagged attacker protected");
  near(hit(l,b,l.damageSources().playerAttack(a)),0,"unflagged attacker cannot melee");
  near(hit(l,b,SpellDamageSource.player(SpellSchools.FROST,a)),0,"unflagged attacker cannot cast harmful spell");
  var pig=EntityTypes.PIG.create(l,EntitySpawnReason.COMMAND);
  near(hit(l,pig,l.damageSources().playerAttack(a)),20,"melee PvE unchanged");
  flagged.add(a.getUUID());flagged.remove(b.getUUID());
  var board=server.getScoreboard();var team=board.addPlayerTeam("probe-team");board.addPlayerToTeam(a.getScoreboardName(),team);
  check(!a.canHarmPlayer(b),"team membership cannot bypass unflagged target protection");
  flagged.add(b.getUUID());check(a.canHarmPlayer(b),"flagged opposing teams can fight");
  board.addPlayerToTeam(b.getScoreboardName(),team);team.setAllowFriendlyFire(false);
  check(!a.canHarmPlayer(b),"allied friendly-fire protection");
  team.setAllowFriendlyFire(true);check(a.canHarmPlayer(b),"flagged allied friendly fire allowed");
  flagged.remove(b.getUUID());check(!a.canHarmPlayer(b),"friendly fire does not bypass unflagged protection");
  flagged.add(b.getUUID());
  var saved=PlayerFlagManager.INSTANCE.save();flagged.clear();PlayerFlagManager.INSTANCE.load(saved);
  check(flagged.contains(a.getUUID())&&flagged.contains(b.getUUID()),"flag state NBT roundtrip");
  board.removePlayerTeam(team);
  flagged.clear();
  Files.writeString(Path.of("result.txt"),"PASS "+checks+" checks\n");
 }catch(Throwable t){t.printStackTrace();try{Files.writeString(Path.of("result.txt"),"FAIL "+t);}catch(Exception ignored){}}
 finally{server.halt(false);}}
}
