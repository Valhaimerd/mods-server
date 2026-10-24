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
  server.setDifficulty(net.minecraft.world.Difficulty.NORMAL,true);
  var a=player(server,l,"PvPAttacker","00000000-0000-0000-0000-000000001111");
  var b=player(server,l,"PvPVictim","00000000-0000-0000-0000-000000002222");
  a.setGameMode(GameType.SURVIVAL);b.setGameMode(GameType.SURVIVAL);a.getAbilities().invulnerable=false;b.getAbilities().invulnerable=false;
  for(String id:List.of("minecraft:attack_damage","ranged_weapon:damage","spell_power:arcane","spell_power:fire","spell_power:frost")){
   var attribute=a.getAttribute(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.get(net.minecraft.resources.Identifier.parse(id)).orElseThrow());attribute.setBaseValue(100);
   attribute.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(io.redspace.pvp_flagging.events.ServerEventsForge.BALANCE,-.2,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
   for(int i=0;i<100;i++)io.redspace.pvp_flagging.events.ServerEventsForge.clearPowerPenalty(a);
   near(attribute.getValue(),100,"legacy attribute penalty removed "+id);
  }
  var clone=player(server,l,"Clone","00000000-0000-0000-0000-000000003333");clone.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(100);
  clone.getAttribute(Attributes.ATTACK_DAMAGE).addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(io.redspace.pvp_flagging.events.ServerEventsForge.BALANCE,-.2,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
  NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone(clone,a,true));near(clone.getAttributeValue(Attributes.ATTACK_DAMAGE),100,"death clone cleans old penalty");
  for(String spellId:List.of("wizards:arcane_missile","wizards:fire_breath","wizards:frost_nova")){
   var spell=net.spell_engine.api.spell.registry.SpellRegistry.from(l).get(net.minecraft.resources.Identifier.parse(spellId)).orElseThrow();
   var impacts=spell.value().impacts.stream().filter(i->i.action.type==net.spell_engine.api.spell.Spell.Impact.Action.Type.DAMAGE).toList();
   var power=SpellPower.getSpellPower(spell.value().school,a);near(power.baseValue(),100,"full power restored "+spellId);
   var context=new net.spell_engine.internals.SpellExecution.ImpactContext(1,1,null,new SpellPower.Result(power.school(),power.baseValue(),0,1.5),net.spell_engine.internals.target.SpellTarget.FocusMode.DIRECT,0);
   var victim=EntityTypes.PIG.create(l,EntitySpawnReason.COMMAND);victim.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);victim.setHealth(1000);
   net.spell_engine.internals.impact.SpellImpacts.performImpacts(l,a,victim,victim,spell,impacts,context,false,null);
   near(1000-victim.getHealth(),80*impacts.getFirst().action.damage.spell_power_coefficient,"native spell reduced once at damage event "+spellId);
  }
  check(a.canHarmPlayer(b),"strangers can fight without flags");
  var arrow=EntityTypes.ARROW.create(l,EntitySpawnReason.COMMAND);
  var sources=new ArrayList<DamageSource>(List.of(l.damageSources().playerAttack(a),l.damageSources().arrow(arrow,a),SpellDamageSource.player(SpellSchools.ARCANE,a),SpellDamageSource.player(SpellSchools.FIRE,a),SpellDamageSource.player(SpellSchools.FROST,a)));
  for(var source:sources){
   near(hit(l,b,source),7,"raw damage PvP retains thirty-five percent "+source.type().msgId());
   var pig=EntityTypes.PIG.create(l,EntitySpawnReason.COMMAND);
   near(hit(l,pig,source),16,"player PvE retains eighty percent "+source.type().msgId());
  }
  l.players().add(a);
  for(String type:List.of("archers:spirit_wolf","wizards:arcane_emitter","wizards:fire_hydra","wizards:frost_elemental")){
   var summon=(net.spell_engine.entity.SummonedEntity)net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getValue(net.minecraft.resources.Identifier.parse(type)).create(l,EntitySpawnReason.MOB_SUMMONED);summon.setOwnerUuid(a.getUUID());
   check(io.redspace.pvp_flagging.events.ServerEventsForge.playerSource(summon)==a,"player summon resolved "+type);
   var projectile=EntityTypes.ARROW.create(l,EntitySpawnReason.COMMAND);projectile.setOwner(summon);
   for(var source:List.of(l.damageSources().mobAttack(summon),SpellDamageSource.create(SpellSchools.ARCANE,summon),l.damageSources().arrow(projectile,summon))){
    near(hit(l,b,source),7,"summon versus player thirty-five percent "+type);
    near(hit(l,EntityTypes.PIG.create(l,EntitySpawnReason.COMMAND),source),16,"summon versus mob eighty percent "+type);sources.add(source);
   }
   summon.setOwnerUuid(null);near(hit(l,b,l.damageSources().mobAttack(summon)),20,"unowned summon remains ordinary entity "+type);summon.setOwnerUuid(a.getUUID());
  }
  near(hit(l,b,l.damageSources().generic()),20,"ownerless damage unchanged");
  var zombie=EntityTypes.ZOMBIE.create(l,EntitySpawnReason.COMMAND);
  near(hit(l,b,l.damageSources().mobAttack(zombie)),20,"mob attacks on players unchanged");
  near(hit(l,EntityTypes.PIG.create(l,EntitySpawnReason.COMMAND),l.damageSources().mobAttack(zombie)),20,"ordinary entity versus entity unchanged");
  var wolf=EntityTypes.WOLF.create(l,EntitySpawnReason.COMMAND);wolf.setOwnerReference(EntityReference.of(a.getUUID()));
  near(hit(l,EntityTypes.PIG.create(l,EntitySpawnReason.COMMAND),l.damageSources().mobAttack(wolf)),20,"pet attacks not double-scaled as player attacks");
  a.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);a.setHealth(20);a.heal(20);near(a.getHealth(),40,"healing unchanged");
  check(server.getCommands().getDispatcher().getRoot().getChild("pvpFlag")==null,"flag command absent");
  check(server.getCommands().getDispatcher().getRoot().getChild("pvpZone")==null,"zone command absent");
  var manager=PartyManager.forServer(server);var party=new Party(UUID.randomUUID());
  party.addMember(new PartyMember(a.getUUID(),"PvPAttacker",PartyRole.LEADER));
  party.addMember(new PartyMember(b.getUUID(),"PvPVictim",PartyRole.MEMBER));party.setFriendlyFire(false);
  var pf=PartyManager.class.getDeclaredField("parties");pf.setAccessible(true);var parties=(Map<UUID,Party>)pf.get(manager);parties.put(party.id(),party);
  var mf=PartyManager.class.getDeclaredField("playerParties");mf.setAccessible(true);var memberships=(Map<UUID,UUID>)mf.get(manager);memberships.put(a.getUUID(),party.id());memberships.put(b.getUUID(),party.id());
  check(manager.blocksFriendlyFire(a,b),"Better Party blocks friendly fire");
  for(var source:sources)near(hit(l,b,source),0,"protected party damage blocked "+source.type().msgId());
  var partyPet=EntityTypes.WOLF.create(l,EntitySpawnReason.COMMAND);partyPet.setOwnerReference(EntityReference.of(b));
  for(var source:sources)near(hit(l,partyPet,source),0,"party pet protected "+source.type().msgId());
  party.setFriendlyFire(true);check(!manager.blocksFriendlyFire(a,b),"Better Party permits chosen friendly fire");
  for(var source:sources)near(hit(l,b,source),7,"enabled party damage uses thirty-five percent "+source.type().msgId());
  party.setFriendlyFire(false);memberships.remove(b.getUUID());party.removeMember(b.getUUID());
  near(hit(l,b,SpellDamageSource.player(SpellSchools.ARCANE,a)),7,"leaving party restores thirty-five percent PvP");
  memberships.remove(a.getUUID());parties.remove(party.id());
  var board=server.getScoreboard();var team=board.addPlayerTeam("retention-team");board.addPlayerToTeam(a.getScoreboardName(),team);board.addPlayerToTeam(b.getScoreboardName(),team);team.setAllowFriendlyFire(false);
  near(hit(l,b,l.damageSources().playerAttack(a)),0,"vanilla team protection respected");
  team.setAllowFriendlyFire(true);near(hit(l,b,l.damageSources().playerAttack(a)),7,"vanilla team allowed damage uses thirty-five percent");board.removePlayerTeam(team);
  l.players().remove(a);
  Files.writeString(Path.of("result.txt"),"PASS "+checks+" checks\n");
 }catch(Throwable t){t.printStackTrace();try{Files.writeString(Path.of("result.txt"),"FAIL "+t);}catch(Exception ignored){}}
 finally{server.halt(false);}}
}
