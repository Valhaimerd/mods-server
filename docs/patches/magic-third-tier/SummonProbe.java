package local.audit;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.api.spell.summon.*;
import net.spell_engine.entity.SummonedEntity;
import net.spell_engine.internals.*;
public final class SummonProbe {
 static void eq(String name,double actual,double expected,Consumer<String> out){if(Math.abs(actual-expected)>.005)throw new AssertionError(name+": "+actual+" expected "+expected);out.accept(name+": "+actual);}
 static Holder<Spell> spell(ServerLevel l,String id){return SpellRegistry.from(l).get(Identifier.parse(id)).orElseThrow();}
 public static void run(ServerLevel level,ServerPlayer owner,Consumer<String> out) throws Exception {
  var frost=spell(level,"wizards:frost_elemental");
  var def=frost.value().impacts.getFirst().action.summon;
  double power=net.spell_power.api.SpellPower.getSpellPower(net.spell_power.api.SpellSchools.FROST,owner).nonCriticalValue();
  eq("elemental cooldown",frost.value().cost.cooldown.duration,30,out);
  eq("elemental haste",frost.value().cost.cooldown.haste_affected?1:0,1,out);
  for(String mode:List.of("base","frost_legion","frost_colossus")) {
   AttributeScaling scaling=new AttributeScaling();
   if(!mode.equals("base")){var en=new AttributeScaling.Entry();en.attribute_id="magic_pack:"+mode;scaling.entries.add(en);}
   var e=(SummonedEntity)net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse("wizards:frost_elemental")).create(level,EntitySpawnReason.MOB_SUMMONED);
   e.onSummonedBySpell(new SpellSummoned.Args(owner,frost,def.behaviour,scaling,new SpellExecution.ImpactContext()));
   double f=mode.equals("base")?1:mode.equals("frost_legion")?.8:2;
   eq(mode+" health",e.getMaxHealth(),(30+.5*power)*f,out);
   eq(mode+" armor",e.getAttributeValue(Attributes.ARMOR),(5+.08*power)*f,out);
   eq(mode+" melee",e.getAttributeValue(Attributes.ATTACK_DAMAGE),.5*power*f,out);
   eq(mode+" scale",e.getScale(),mode.equals("frost_colossus")?1.7:1,out);
   eq(mode+" frost",e.getAttributeValue(net.spell_engine.rpg_series.config.ConfigUtil.attribute("spell_power:frost").orElseThrow()),.2*power*f,out);
   eq(mode+" pet owner",e.getOwnerReference().getUUID().equals(owner.getUUID())?1:0,1,out);
   if(f==2)eq("colossus owner knockback resistance",e.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE),owner.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE),out);
   nova(level,e,mode,.2*power*f,out);
   if(mode.equals("base")) partyDiscovery(level,owner,e,out);
   // Exercise caps beyond vanilla armor30/health1024 through actual entity accessors.
   e.getAttribute(Attributes.ARMOR).setBaseValue(150);eq(mode+" armor cap bypass",e.getAttributeValue(Attributes.ARMOR),150,out);
   e.getAttribute(Attributes.MAX_HEALTH).setBaseValue(3000);e.setHealth(3000);eq(mode+" health cap bypass",e.getHealth(),3000,out);
  }
  var ancient=spell(level,"skill_tree_rpgs:fire_tier_4_spell_2_modifier_2").value().modifiers;
  var hydra=spell(level,"wizards:fire_hydra");
  var hd=hydra.value().impacts.getFirst().action.summon;
  eq("Ancient exact count",SummonRules.count(4,hd,ancient,false),1,out);
  var scale=ancient.getFirst().summon_attribute_scaling;
  var h=(SummonedEntity)net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse("wizards:fire_hydra")).create(level,EntitySpawnReason.MOB_SUMMONED);
  h.onSummonedBySpell(new SpellSummoned.Args(owner,hydra,hd.behaviour,scale,new SpellExecution.ImpactContext()));
  eq("Ancient scale",h.getScale(),3,out);
  eq("Ancient target range",h.detectionRange(),12,out);
  eq("Ancient breath action",h.behaviour.actions.getFirst().spell_cast.spell_id.equals("wizards:ancient_hydra_breath")?1:0,1,out);
  h.setPos(0,80,0);
  var close=net.minecraft.world.entity.EntityTypes.CREEPER.create(level,EntitySpawnReason.COMMAND);
  close.setPos(0,80,6);
  h.lockRotationTo(close);
  var delta=close.getBoundingBox().getCenter().subtract(SummonRules.mouth(h)).normalize();
  eq("Ancient mouth aim at close ground target",delta.dot(h.getLookAngle()),1,out);
  var breath=spell(level,"wizards:ancient_hydra_breath");
  eq("Ancient damage per delivery",breath.value().impacts.getFirst().action.damage.spell_power_coefficient*SpellParameters.channelValueMultiplier(breath.value()),.12,out);
  eq("Ancient channel deliveries",breath.value().active.cast.channelTicks(),25,out);
  eq("Ancient cone",breath.value().target.area.angle_degrees,40,out);
  eq("Ancient cooldown",breath.value().cost.cooldown.duration,0,out);
  continuousBreath(level,owner,h,breath,out);
  hydraTargeting(level,h,breath,out);
  ordinaryHydra(level,owner,hydra,hd,out);
  eq("Elemental shard own power coefficient",spell(level,"wizards:elemental_frost_shard").value().impacts.getFirst().action.damage.spell_power_coefficient,1,out);
  eq("Player Nova range remains",spell(level,"wizards:frost_nova").value().range,10,out);
  eq("Player Nova freeze remains",spell(level,"wizards:frost_nova").value().impacts.get(1).action.status_effect.duration,2,out);
  eq("Shared summon Nova action unchanged",def.behaviour.actions.get(1).spell_cast.spell_id.equals("wizards:frost_nova")?1:0,1,out);
 }
 static void nova(ServerLevel level,SummonedEntity elemental,String mode,double power,Consumer<String> out)throws Exception {
  boolean legion=mode.equals("frost_legion");
  var action=elemental.behaviour.actions.get(1).spell_cast;
  eq(mode+" Nova action",action.spell_id.equals(legion?"wizards:legion_frost_nova":"wizards:elemental_frost_nova")?1:0,1,out);
  var nova=spell(level,action.spell_id);
  elemental.setPos(0,80,0);
  elemental.getAttribute(net.spell_engine.rpg_series.config.ConfigUtil.attribute("spell_power:critical_chance").orElseThrow()).setBaseValue(0);
  eq(mode+" Nova radius",SpellParameters.getRange(elemental,nova),legion?6:10,out);
  var inside=net.minecraft.world.entity.EntityTypes.CREEPER.create(level,EntitySpawnReason.COMMAND);
  var outside=net.minecraft.world.entity.EntityTypes.CREEPER.create(level,EntitySpawnReason.COMMAND);
  for(var t:List.of(inside,outside)) {t.setNoAi(true);t.setNoGravity(true);t.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);t.setHealth(1000);}
  inside.setPos(0,80,5);outside.setPos(0,80,7);
  for(var t:List.of(inside,outside)){level.getChunkAt(t.blockPosition());if(!level.addFreshEntity(t))throw new AssertionError("Nova target spawn failed");}
  try {
   var controller=new net.spell_engine.internals.casting.MobCastController(elemental,action);
   var fire=controller.getClass().getDeclaredMethod("performSpell",Holder.class);fire.setAccessible(true);
   fire.invoke(controller,nova);
   eq(mode+" actual Nova damage from own Frost power",1000-inside.getHealth(),1.8*power,out);
   var frozen=net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.get(Identifier.parse("wizards:frozen")).orElseThrow();
   eq(mode+" actual Nova Frozen ticks",inside.hasEffect(frozen)?inside.getEffect(frozen).getDuration():0,legion?10:40,out);
   eq(mode+" Nova seven-block target damage",1000-outside.getHealth(),legion?0:1.8*power,out);
   eq(mode+" Nova seven-block target freeze",outside.hasEffect(frozen)?1:0,legion?0:1,out);
  }finally{inside.discard();outside.discard();}
 }
 static void hydraTargeting(ServerLevel level,SummonedEntity hydra,Holder<Spell> breath,Consumer<String> out)throws Exception {
  hydra.setPos(100,80,0);hydra.setNoAi(true);hydra.setNoGravity(true);
  var near=net.minecraft.world.entity.EntityTypes.CREEPER.create(level,EntitySpawnReason.COMMAND);
  var far=net.minecraft.world.entity.EntityTypes.CREEPER.create(level,EntitySpawnReason.COMMAND);
  near.setPos(100,80,7);far.setPos(100,80,9);
  for(var t:List.of(near,far)){t.setNoAi(true);t.setNoGravity(true);level.getChunkAt(t.blockPosition());if(!level.addFreshEntity(t))throw new AssertionError("Hydra targeting fixture failed");}
  try {
   hydra.tickCount=25;hydra.setTarget(far);hydra.tick();eq("Hydra actual entity tick chooses nearest",hydra.getTarget()==near?1:0,1,out);
   var controller=new net.spell_engine.internals.casting.MobCastController(hydra,hydra.behaviour.actions.getFirst().spell_cast);
   eq("Hydra native controller begins on local target",controller.canBegin()?1:0,1,out);controller.begin();
   eq("Hydra native channel continues on local target",controller.shouldContinue()?1:0,1,out);
   near.setPos(100,80,14);hydra.tick();
   eq("Hydra native channel stops tracking escaped target",controller.shouldContinue()?1:0,0,out);controller.end();
   eq("Hydra native controller can restart on replacement",controller.canBegin()?1:0,1,out);
   eq("Hydra escaped target replaced with local enemy",hydra.getTarget()==far?1:0,1,out);
   far.setPos(100,80,12);hydra.tick();eq("Hydra includes twelve-block boundary",hydra.getTarget()==far?1:0,1,out);
   far.setPos(100,80,12.01);hydra.tick();eq("Hydra drops targets beyond twelve blocks",hydra.getTarget()==null?1:0,1,out);
   near.setPos(100,80,7);hydra.tick();eq("Hydra reacquires returning enemy",hydra.getTarget()==near?1:0,1,out);
   hydra.lockRotationTo(near);
   var packet=SummonRules.breathParticles(hydra,breath);
   eq("Hydra breath packet coordinate source",packet.sourceType()==net.spell_engine.network.Packets.ParticleEffects.SourceType.COORDINATE?1:0,1,out);
   eq("Hydra breath particle batches",packet.spawns().size(),3,out);
   for(var spawn:packet.spawns()) {
    eq("Hydra particle exact mouth origin",spawn.sourceLocation().distanceTo(SummonRules.mouth(hydra)),0,out);
    eq("Hydra particle yaw",spawn.yaw(),hydra.getYRot(),out);eq("Hydra particle pitch",spawn.pitch(),hydra.getXRot(),out);
    eq("Hydra visual cone",spawn.effect().batch.angle,40,out);
   }
   eq("Hydra packet produces flame instructions",net.spell_engine.fx.ParticleHelper.convertToInstructions(level,packet).isEmpty()?0:1,1,out);
   // Put two targets at 15 and 25 degrees around a level mouth aim: full cone is 40 degrees.
   hydra.setYRot(0);hydra.setYBodyRot(0);hydra.setXRot(0);
   var mouth=SummonRules.mouth(hydra);
   for(int i=0;i<2;i++) {var t=i==0?near:far;double a=Math.toRadians(i==0?15:25);t.setPos(mouth.x+Math.sin(a)*4,mouth.y-t.getBbHeight()/2,mouth.z+Math.cos(a)*4);}
   eq("Hydra 40-degree cone includes fifteen degrees",SummonRules.targets(hydra).entities().contains(near)?1:0,1,out);
   eq("Hydra 40-degree cone excludes twenty-five degrees",SummonRules.targets(hydra).entities().contains(far)?1:0,0,out);
  }finally{near.discard();far.discard();}
 }
 static void ordinaryHydra(ServerLevel level,ServerPlayer owner,Holder<Spell> summon,Spell.Impact.Action.Summon def,Consumer<String> out)throws Exception {
  var hydra=(SummonedEntity)net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse("wizards:fire_hydra")).create(level,EntitySpawnReason.MOB_SUMMONED);
  hydra.onSummonedBySpell(new SpellSummoned.Args(owner,summon,def.behaviour,def.attribute_scaling,new SpellExecution.ImpactContext()));
  eq("Ordinary Hydra summon-only fireball",hydra.behaviour.actions.getFirst().spell_cast.spell_id.equals("wizards:hydra_fireball")?1:0,1,out);
  eq("Ordinary Hydra target radius",hydra.detectionRange(),12,out);
  var ball=spell(level,"wizards:hydra_fireball");
  eq("Ordinary Hydra fireball own power coefficient",ball.value().impacts.getFirst().action.damage.spell_power_coefficient,.55,out);
  eq("Player Fireball unchanged",spell(level,"wizards:fireball").value().impacts.getFirst().action.damage.spell_power_coefficient,.8,out);
  var victim=net.minecraft.world.entity.EntityTypes.CREEPER.create(level,EntitySpawnReason.COMMAND);victim.setPos(0,80,6);victim.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);victim.setHealth(1000);
  var power=net.spell_power.api.SpellPower.getSpellPower(net.spell_power.api.SpellSchools.FIRE,hydra);
  var context=new SpellExecution.ImpactContext().power(new net.spell_power.api.SpellPower.Result(net.spell_power.api.SpellSchools.FIRE,power.nonCriticalValue(),0,1.5));
  net.spell_engine.internals.impact.SpellImpacts.performImpacts(level,hydra,victim,victim,ball,ball.value().impacts,context);
  eq("Ordinary Hydra actual raw hit",1000-victim.getHealth(),.55*power.nonCriticalValue(),out);
  eq("Observed old 9.1 hit scales to new damage",9.1*.55/.8,6.25625,out);
 }
 @SuppressWarnings({"unchecked","rawtypes"})
 static void partyDiscovery(ServerLevel level,ServerPlayer owner,SummonedEntity pet,Consumer<String> out)throws Exception {
  pet.setPos(0,80,0);pet.setNoAi(true);pet.setNoGravity(true);
  level.getChunkAt(pet.blockPosition());
  if(!level.addFreshEntity(pet))throw new AssertionError("Could not spawn elemental for addon discovery");
  try {
   Class<?> network=Class.forName("dev.betterparty.tamedmobs.TamedMobNetwork");
   var collect=network.getDeclaredMethod("collectLoadedPets",net.minecraft.server.MinecraftServer.class,Set.class);collect.setAccessible(true);
   var all=(Map<UUID,List<?>>)collect.invoke(null,level.getServer(),Set.of(owner.getUUID()));
   var snapshot=all.getOrDefault(owner.getUUID(),List.of()).stream().filter(row->{try{return row.getClass().getMethod("petId").invoke(row).equals(pet.getUUID());}catch(Exception ex){throw new RuntimeException(ex);}}).findFirst().orElseThrow(()->new AssertionError("Actual addon collector omitted Frost Elemental"));
   eq("Better Party collector elemental owner",snapshot.getClass().getMethod("ownerId").invoke(snapshot).equals(owner.getUUID())?1:0,1,out);
   eq("Better Party collector elemental health",((Number)snapshot.getClass().getMethod("health").invoke(snapshot)).doubleValue(),pet.getHealth(),out);
   Class<?> payloadClass=Class.forName("dev.betterparty.tamedmobs.PetSnapshotPayload");
   Object payload=payloadClass.getConstructor(List.class).newInstance(List.of(snapshot));
   var codec=(net.minecraft.network.codec.StreamCodec)payloadClass.getField("STREAM_CODEC").get(null);
   var buffer=new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),level.registryAccess());
   try {
    codec.encode(buffer,payload);Object decoded=codec.decode(buffer);
    var pets=(List<?>)payloadClass.getMethod("pets").invoke(decoded);
    eq("Better Party payload count",pets.size(),1,out);
    eq("Better Party payload preserves elemental",pets.getFirst().equals(snapshot)?1:0,1,out);
    eq("Better Party payload readable bytes",buffer.readableBytes(),0,out);
   } finally {buffer.release();}
  } finally {pet.discard();}
 }
 static void continuousBreath(ServerLevel level,ServerPlayer owner,SummonedEntity hydra,Holder<Spell> breath,Consumer<String> out)throws Exception {
  var power=net.spell_engine.rpg_series.config.ConfigUtil.attribute("spell_power:fire").orElseThrow();
  hydra.getAttribute(power).setBaseValue(40);
  hydra.getAttribute(net.spell_engine.rpg_series.config.ConfigUtil.attribute("spell_power:critical_chance").orElseThrow()).setBaseValue(0);
  var target=net.minecraft.world.entity.EntityTypes.CREEPER.create(level,EntitySpawnReason.COMMAND);
  target.setPos(0,80,6);target.setNoAi(true);target.setNoGravity(true);
  target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);target.setHealth(1000);
  level.getChunkAt(target.blockPosition());
  if(!level.addFreshEntity(target))throw new AssertionError("Could not spawn Ancient breath target");
  hydra.lockRotationTo(target);
  eq("Ancient mouth cone selects actual ground enemy",SummonRules.targets(hydra).entities().contains(target)?1:0,1,out);
  long start=level.getGameTime();
  var data=(net.minecraft.world.level.storage.ServerLevelData)level.getLevelData();
  hydra.declareCastProcess(new net.spell_engine.internals.casting.SpellCast.Process(hydra,breath,null,1,100,start));
  var controller=new net.spell_engine.internals.casting.MobCastController(hydra,hydra.behaviour.actions.getFirst().spell_cast);
  var tick=controller.getClass().getDeclaredMethod("tickChannel",Holder.class,Spell.class);tick.setAccessible(true);
  int hits=0,firstWindow=0,secondWindow=0;
  try {
   for(int t=0;t<200;t++) {
    data.setGameTime(start+t);target.invulnerableTime=0;
    float before=target.getHealth();tick.invoke(controller,breath,breath.value());
    double damage=before-target.getHealth();
    if(damage>0) {
     eq("Ancient actual breath hit tick "+t,damage,4.8,out);
     eq("Ancient delivery cadence "+hits,t,2+4*hits,out);
     hits++;if(t<100)firstWindow++;else secondWindow++;
    }
   }
   eq("Ancient first five seconds actual deliveries",firstWindow,25,out);
   eq("Ancient next five seconds actual deliveries",secondWindow,25,out);
   eq("Ancient continuous ten seconds total damage",1000-target.getHealth(),240,out);
   eq("Ancient channel rollover start",hydra.getSpellCastProcess().startedAt(),start+100,out);
   eq("Ancient zero native cooldown",hydra.cooldownManager.isCoolingDown(breath)?1:0,0,out);
  } finally {data.setGameTime(start);hydra.declareCastProcess(null);target.discard();}
 }

}
