package local.pvp;
import java.nio.file.*;
import java.util.*;
import com.mojang.authlib.GameProfile;
import dev.betterparty.party.*;

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
  check(a.canHarmPlayer(b),"strangers can fight without flags");
  var arrow=EntityTypes.ARROW.create(l,EntitySpawnReason.COMMAND);
  var sources=List.of(l.damageSources().playerAttack(a),l.damageSources().arrow(arrow,a),SpellDamageSource.player(SpellSchools.ARCANE,a),SpellDamageSource.player(SpellSchools.FIRE,a),SpellDamageSource.player(SpellSchools.FROST,a));
  for(var source:sources){
   near(hit(l,b,source),10,"stranger PvP halved "+source.type().msgId());
   var pig=EntityTypes.PIG.create(l,EntitySpawnReason.COMMAND);
   near(hit(l,pig,source),20,"PvE unchanged "+source.type().msgId());
  }
  near(hit(l,b,l.damageSources().generic()),20,"ownerless damage unchanged");
  check(server.getCommands().getDispatcher().getRoot().getChild("pvpFlag")==null,"flag command absent");
  check(server.getCommands().getDispatcher().getRoot().getChild("pvpZone")==null,"zone command absent");
  var manager=PartyManager.forServer(server);var party=new Party(UUID.randomUUID());
  party.addMember(new PartyMember(a.getUUID(),"PvPAttacker",PartyRole.LEADER));
  party.addMember(new PartyMember(b.getUUID(),"PvPVictim",PartyRole.MEMBER));party.setFriendlyFire(false);
  var pf=PartyManager.class.getDeclaredField("parties");pf.setAccessible(true);var parties=(Map<UUID,Party>)pf.get(manager);parties.put(party.id(),party);
  var mf=PartyManager.class.getDeclaredField("playerParties");mf.setAccessible(true);var memberships=(Map<UUID,UUID>)mf.get(manager);memberships.put(a.getUUID(),party.id());memberships.put(b.getUUID(),party.id());
  check(manager.blocksFriendlyFire(a,b),"Better Party blocks friendly fire");
  for(var source:sources)near(hit(l,b,source),0,"protected party damage blocked "+source.type().msgId());
  party.setFriendlyFire(true);check(!manager.blocksFriendlyFire(a,b),"Better Party permits chosen friendly fire");
  for(var source:sources)near(hit(l,b,source),10,"enabled party damage halved "+source.type().msgId());
  party.setFriendlyFire(false);memberships.remove(b.getUUID());party.removeMember(b.getUUID());
  near(hit(l,b,SpellDamageSource.player(SpellSchools.ARCANE,a)),10,"leaving party restores halved PvP");
  memberships.remove(a.getUUID());parties.remove(party.id());
  var board=server.getScoreboard();var team=board.addPlayerTeam("retention-team");board.addPlayerToTeam(a.getScoreboardName(),team);board.addPlayerToTeam(b.getScoreboardName(),team);team.setAllowFriendlyFire(false);
  near(hit(l,b,l.damageSources().playerAttack(a)),0,"vanilla team protection respected");
  team.setAllowFriendlyFire(true);near(hit(l,b,l.damageSources().playerAttack(a)),10,"vanilla team allowed damage halved");board.removePlayerTeam(team);
  Files.writeString(Path.of("result.txt"),"PASS "+checks+" checks\n");
 }catch(Throwable t){t.printStackTrace();try{Files.writeString(Path.of("result.txt"),"FAIL "+t);}catch(Exception ignored){}}
 finally{server.halt(false);}}
}
