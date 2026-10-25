package local.damagecommands;
import java.nio.file.*;
import java.util.*;
import io.redspace.pvp_flagging.DamageCommands;
import io.redspace.pvp_flagging.CombatRules;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
@Mod("damage_commands_probe")
public class DamageCommandsProbe {
 int checks;List<String> results=new ArrayList<>();
 public DamageCommandsProbe(){NeoForge.EVENT_BUS.addListener(this::run);}
 void check(boolean b,String text){if(!b)throw new AssertionError(text);checks++;results.add("PASS "+text);}
 void near(double got,double want,String text){check(Math.abs(got-want)<.0001,text+" actual="+got);}
 ServerPlayer player(MinecraftServer server,ServerLevel l,String name){
  var profile=new GameProfile(UUID.nameUUIDFromBytes(name.getBytes()),name);var p=new ServerPlayer(server,l,profile,ClientInformation.createDefault());
  p.connection=new net.minecraft.server.network.ServerGamePacketListenerImpl(server,new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND),p,net.minecraft.server.network.CommonListenerCookie.createInitial(profile,false));p.connection.markClientLoaded();p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.getAbilities().invulnerable=false;return p;
 }
 float hit(ServerLevel l,LivingEntity target,net.minecraft.world.damagesource.DamageSource source){target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);target.setHealth(1000);target.invulnerableTime=0;target.hurtServer(l,source,20);return 1000-target.getHealth();}
 void command(MinecraftServer s,String text)throws Exception{s.getCommands().getDispatcher().execute(text,s.createCommandSourceStack());}
 void run(ServerStartedEvent e){var server=e.getServer();try{
  server.setDifficulty(net.minecraft.world.Difficulty.NORMAL,true);
  var l=server.overworld();var a=player(server,l,"DamageOwner");var b=player(server,l,"DamageTarget");l.players().add(a);
  var dispatcher=server.getCommands().getDispatcher();var source=server.createCommandSourceStack();
  if(Files.exists(Path.of("verify-restart"))){near(DamageCommands.get(server).percent(true),67.5,"PvP value survives server restart");near(DamageCommands.get(server).percent(false),92.5,"PvE value survives server restart");}
  else{near(DamageCommands.get(server).percent(true),35,"default PvP unchanged");near(DamageCommands.get(server).percent(false),80,"default PvE unchanged");}
  check(dispatcher.execute("pvpdamage",source)==1,"query PvP without value");check(dispatcher.execute("pvedamage",source)==1,"query PvE without value");
  for(String bad:List.of("pvpdamage -1","pvpdamage 101","pvedamage -0.1","pvedamage 100.1","pvpdamage NaN","pvedamage Infinity")){
   boolean rejected=false;try{dispatcher.execute(bad,source);}catch(CommandSyntaxException ex){rejected=true;}check(rejected,"invalid percentage rejected "+bad);
  }
  check(!dispatcher.getRoot().getChild("pvpdamage").canUse(a.createCommandSourceStack()),"non-operator cannot configure PvP");check(!dispatcher.getRoot().getChild("pvedamage").canUse(a.createCommandSourceStack()),"non-operator cannot configure PvE");
  for(double percent:new double[]{0,35,67.5,100}){
   command(server,"pvpdamage "+percent);command(server,"pvedamage 80");near(hit(l,b,l.damageSources().playerAttack(a)),20*percent/100,"native PvP command damage "+percent);near(hit(l,EntityTypes.PIG.create(l,EntitySpawnReason.COMMAND),l.damageSources().playerAttack(a)),16,"PvP command leaves PvE unchanged");
   command(server,"pvedamage "+percent);near(hit(l,EntityTypes.PIG.create(l,EntitySpawnReason.COMMAND),l.damageSources().playerAttack(a)),20*percent/100,"native PvE command damage "+percent);
   for(String type:List.of("archers:spirit_wolf","wizards:arcane_emitter","wizards:fire_hydra","wizards:frost_elemental")){
    var summon=(net.spell_engine.entity.SummonedEntity)net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getValue(net.minecraft.resources.Identifier.parse(type)).create(l,EntitySpawnReason.MOB_SUMMONED);summon.setOwnerUuid(a.getUUID());
    var arrow=EntityTypes.ARROW.create(l,EntitySpawnReason.COMMAND);arrow.setOwner(summon);
    for(var damage:List.of(l.damageSources().mobAttack(summon),l.damageSources().arrow(arrow,summon))){near(hit(l,b,damage),20*percent/100,"summon PvP follows command "+type+" "+percent);near(hit(l,EntityTypes.PIG.create(l,EntitySpawnReason.COMMAND),damage),20*percent/100,"summon PvE follows command "+type+" "+percent);}
   }
  }
  command(server,"pvpdamage 0");CombatRules.sync(a);a.getPersistentData().remove(CombatRules.KEY);b.getPersistentData().remove(CombatRules.KEY);near(hit(l,b,l.damageSources().playerAttack(a)),0,"zero PvP blocks damage");check(!CombatRules.flagged(a)&&!CombatRules.flagged(b),"zero-damage command does not combat flag");
  command(server,"pvpdamage 100");command(server,"pvedamage 100");var team=server.getScoreboard().addPlayerTeam("command-team");server.getScoreboard().addPlayerToTeam(a.getScoreboardName(),team);server.getScoreboard().addPlayerToTeam(b.getScoreboardName(),team);team.setAllowFriendlyFire(false);near(hit(l,b,l.damageSources().playerAttack(a)),0,"100 percent still respects friendly protection");server.getScoreboard().removePlayerTeam(team);
  var mob=EntityTypes.ZOMBIE.create(l,EntitySpawnReason.COMMAND);near(hit(l,b,l.damageSources().mobAttack(mob)),20,"ordinary mob damage unchanged");
  command(server,"pvpdamage 67.5");command(server,"pvedamage 92.5");l.players().remove(a);
  Files.writeString(Path.of("damage-command-result.txt"),"PASS "+checks+" damage command checks");Files.write(Path.of("damage-command-checks.txt"),results);
 }catch(Throwable t){t.printStackTrace();try{Files.writeString(Path.of("damage-command-result.txt"),"FAIL "+t);}catch(Exception ignored){}}finally{server.halt(false);}}
}
