package local.asgardprobe;
import java.nio.file.*;
import java.util.*;
import com.google.gson.*;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.JsonOps;
import com.eiden.iourusraces.race.*;
import com.eiden.iourusraces.data.PlayerRaceState;
import com.eiden.iourusraces.network.RaceSyncPayload;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
@Mod("asgard_probe") public final class AsgardProbe {
 final List<String> checks=new ArrayList<>();ServerLevel world;ServerPlayer attacker,victim;LivingEntity mob;
 void near(String key,double got,double expected){if(Math.abs(got-expected)>.005)throw new AssertionError(key+": "+got+" != "+expected);checks.add("PASS "+key+" actual="+got+" expected="+expected);}
 void check(String key,boolean pass){near(key,pass?1:0,1);}
 void race(ServerPlayer player,Race race){PlayerRaceState.get(world.getServer()).setRace(player.getUUID(),race);RaceAttributes.applyRaceAttributes(player,race);}
 Holder<DamageType> type(String id){return world.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).get(Identifier.parse(id)).orElseThrow();}
 double hit(LivingEntity target,DamageSource source){target.setHealth(target.getMaxHealth());target.invulnerableTime=0;float hp=target.getHealth();target.hurtServer(world,source,10);return hp-target.getHealth();}
 public AsgardProbe(){NeoForge.EVENT_BUS.addListener(this::start);}
 void start(ServerStartedEvent event){var server=event.getServer();server.execute(()->{
  String result;
  try{world=server.overworld();run();Files.writeString(Path.of("asgard-measurements.txt"),String.join("\n",checks));result="PASS "+checks.size()+" Asgardian native server measurements";}
  catch(Throwable t){result="FAIL "+t;t.printStackTrace();}
  try{Files.writeString(Path.of("asgard-result.txt"),result);}catch(Exception e){throw new RuntimeException(e);}server.halt(false);
 });}
 double incoming(ServerPlayer target,DamageSource source){
  var event=new net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent(target,new net.neoforged.neoforge.common.damagesource.DamageContainer(source,10));NeoForge.EVENT_BUS.post(event);return event.isCanceled()?0:event.getAmount();
 }
 void run()throws Exception{
  Race asgard=Race.valueOf("ASGARDIAN");var restartId=UUID.fromString("2c0e09bc-2091-4c44-ace1-795d2582d4ad");
  if(Files.exists(Path.of("asgard-restart-marker")))check("Actual server restart kept saved race",PlayerRaceState.get(world.getServer()).getRace(restartId)==asgard);
  PlayerRaceState.get(world.getServer()).setRace(restartId,asgard);Files.writeString(Path.of("asgard-restart-marker"),"pending restart");String[] old={"NONE","HUMAN","ORC","GOBLIN","ELF","DWARF","MELFORK","DRACONIC"};near("Nine enum values incl NONE",Race.values().length,9);
  for(int i=0;i<old.length;i++)check("Original ordinal "+old[i],Race.values()[i].name().equals(old[i]));near("Asgard appended",asgard.ordinal(),8);
  attacker=FakePlayerFactory.get(world,new GameProfile(UUID.randomUUID(),"AsgardProbe"));victim=FakePlayerFactory.get(world,new GameProfile(UUID.randomUUID(),"MagicVictim"));
  attacker.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);victim.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);attacker.removeAllEffects();victim.removeAllEffects();attacker.setPos(0,80,0);victim.setPos(3,80,0);attacker.getAbilities().invulnerable=false;victim.getAbilities().invulnerable=false;
  race(attacker,Race.HUMAN);double movement=attacker.getAttributeValue(Attributes.MOVEMENT_SPEED),scale=attacker.getAttributeValue(Attributes.SCALE),mining=attacker.getAttributeValue(Attributes.BLOCK_BREAK_SPEED);
  for(int repeat=0;repeat<4;repeat++){race(attacker,asgard);near("22 HP repeat="+repeat,attacker.getMaxHealth(),22);near("0.95 speed repeat="+repeat,attacker.getAttributeValue(Attributes.MOVEMENT_SPEED),movement*.95);near("1.05 scale repeat="+repeat,attacker.getAttributeValue(Attributes.SCALE),scale*1.05);near("2% mining repeat="+repeat,attacker.getAttributeValue(Attributes.BLOCK_BREAK_SPEED),mining*1.02);}
  var gear=new AttributeModifier(Identifier.parse("probe:gear_health"),10,AttributeModifier.Operation.ADD_VALUE);attacker.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(gear);race(attacker,asgard);near("Gear max HP retained",attacker.getMaxHealth(),32);attacker.getAttribute(Attributes.MAX_HEALTH).removeModifier(gear);
  for(Race r:Race.values()){race(attacker,r);near("Health native race="+r,attacker.getMaxHealth(),r.getMaxHealth());near("Speed native race="+r,attacker.getAttributeValue(Attributes.MOVEMENT_SPEED),movement*r.getSpeedMultiplier());near("Scale native race="+r,attacker.getAttributeValue(Attributes.SCALE),scale*r.getScale());near("Mining cleanup race="+r,attacker.getAttributeValue(Attributes.BLOCK_BREAK_SPEED),mining*(r==asgard?1.02:1));}
  var saved=new PlayerRaceState();var ids=new HashMap<Race,UUID>();for(Race r:Race.values()){UUID id=UUID.randomUUID();ids.put(r,id);saved.setRace(id,r);}
  var json=PlayerRaceState.CODEC.encodeStart(JsonOps.INSTANCE,saved).getOrThrow();check("Save uses stable name",json.toString().contains("ASGARDIAN"));var loaded=PlayerRaceState.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow();for(Race r:Race.values())check("Codec save/load "+r,loaded.getRace(ids.get(r))==r);
  var buffer=io.netty.buffer.Unpooled.buffer();try{RaceSyncPayload.STREAM_CODEC.encode(buffer,new RaceSyncPayload("ASGARDIAN"));check("Network codec name",RaceSyncPayload.STREAM_CODEC.decode(buffer).raceName().equals("ASGARDIAN"));}finally{buffer.release();}
  var state=PlayerRaceState.get(world.getServer());state.setRace(attacker.getUUID(),Race.NONE);world.getServer().getCommands().getDispatcher().execute("race asgardian",attacker.createCommandSourceStack().withSuppressedOutput());check("Server command selects Asgardian",PlayerRaceData.getRace(attacker)==asgard);
  world.getServer().getCommands().getDispatcher().execute("race human",attacker.createCommandSourceStack().withSuppressedOutput());check("Ordinary reselection denied",PlayerRaceData.getRace(attacker)==asgard);
  com.eiden.iourusraces.race.PlayerRaceData.copyRace(attacker,victim);check("Clone copies race",PlayerRaceData.getRace(victim)==asgard);com.eiden.iourusraces.event.PlayerJoinHandler.onPlayerRespawn(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent(victim,false));near("Respawn reapplies HP",victim.getMaxHealth(),22);
  PlayerRaceData.resetRace(attacker);RaceAttributes.applyRaceAttributes(attacker,Race.NONE);check("Reset clears saved race",PlayerRaceData.getRace(attacker)==Race.NONE);near("Reset removes mining",attacker.getAttributeValue(Attributes.BLOCK_BREAK_SPEED),mining);
  mob=(LivingEntity)net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse("minecraft:villager")).create(world,EntitySpawnReason.COMMAND);mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);mob.setHealth(1000);mob.setPos(3,80,3);world.addFreshEntity(mob);
  var arrow=(Arrow)net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse("minecraft:arrow")).create(world,EntitySpawnReason.COMMAND);arrow.setOwner(attacker);
  var melee=world.damageSources().playerAttack(attacker);var ranged=world.damageSources().arrow(arrow,attacker);var arcane=new DamageSource(type("spell_power:arcane"),attacker);var hybrid=new DamageSource(type("spell_power:arcane"),arrow,attacker);
  for(var source:List.of(melee,ranged,arcane,hybrid)){race(attacker,Race.HUMAN);double base=hit(mob,source);check("Baseline NPC damage positive "+source.typeHolder().unwrapKey(),base>0);race(attacker,asgard);near("Outgoing 2% once "+source.typeHolder().unwrapKey(),hit(mob,source),base*1.02);}
  race(victim,Race.HUMAN);double normalMagic=incoming(victim,world.damageSources().magic());race(victim,asgard);near("Magic weakness 10%",incoming(victim,world.damageSources().magic()),normalMagic*1.1);
  near("Lightning direct immunity",incoming(victim,world.damageSources().lightningBolt()),0);near("No general fire immunity",incoming(victim,world.damageSources().inFire()),10);near("No physical weakness",incoming(victim,world.damageSources().generic()),10);
  var lightning=(LightningBolt)net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse("minecraft:lightning_bolt")).create(world,EntitySpawnReason.COMMAND);var strike=new net.neoforged.neoforge.event.entity.EntityStruckByLightningEvent(victim,lightning);NeoForge.EVENT_BUS.post(strike);check("Strike ignition prevented",strike.isCanceled());
  race(victim,Race.HUMAN);var ordinaryStrike=new net.neoforged.neoforge.event.entity.EntityStruckByLightningEvent(victim,lightning);NeoForge.EVENT_BUS.post(ordinaryStrike);check("Other races can be struck",!ordinaryStrike.isCanceled());
  race(attacker,asgard);attacker.setHealth(10);attacker.heal(4);near("Healing unchanged",attacker.getHealth(),14);race(attacker,Race.HUMAN);race(victim,Race.HUMAN);mob.discard();arrow.discard();lightning.discard();
 }
}
