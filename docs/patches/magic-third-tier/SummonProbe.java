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
   double f=mode.equals("base")?1:mode.equals("frost_legion")?.8:2.5;
   eq(mode+" health",e.getMaxHealth(),(30+.5*power)*f,out);
   eq(mode+" armor",e.getAttributeValue(Attributes.ARMOR),(5+.08*power)*f,out);
   eq(mode+" melee",e.getAttributeValue(Attributes.ATTACK_DAMAGE),.5*power*f,out);
   eq(mode+" scale",e.getScale(),f,out);
   eq(mode+" frost",e.getAttributeValue(net.spell_engine.rpg_series.config.ConfigUtil.attribute("spell_power:frost").orElseThrow()),.5*power*f,out);
   eq(mode+" pet owner",e.getOwnerReference().getUUID().equals(owner.getUUID())?1:0,1,out);
   if(f==2.5)eq("colossus owner knockback resistance",e.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE),owner.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE),out);
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
  eq("Ancient damage per delivery",breath.value().impacts.getFirst().action.damage.spell_power_coefficient*SpellParameters.channelValueMultiplier(breath.value()),.05,out);
  eq("Ancient channel deliveries",breath.value().active.cast.channelTicks(),25,out);
  eq("Ancient cone",breath.value().target.area.angle_degrees,20,out);
  eq("Ancient cooldown",breath.value().cost.cooldown.duration,0,out);
  continuousBreath(level,owner,h,breath,out);
  eq("Elemental shard own power coefficient",spell(level,"wizards:elemental_frost_shard").value().impacts.getFirst().action.damage.spell_power_coefficient,1,out);
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
     eq("Ancient actual breath hit tick "+t,damage,2,out);
     eq("Ancient delivery cadence "+hits,t,2+4*hits,out);
     hits++;if(t<100)firstWindow++;else secondWindow++;
    }
   }
   eq("Ancient first five seconds actual deliveries",firstWindow,25,out);
   eq("Ancient next five seconds actual deliveries",secondWindow,25,out);
   eq("Ancient continuous ten seconds total damage",1000-target.getHealth(),100,out);
   eq("Ancient channel rollover start",hydra.getSpellCastProcess().startedAt(),start+100,out);
   eq("Ancient zero native cooldown",hydra.cooldownManager.isCoolingDown(breath)?1:0,0,out);
  } finally {data.setGameTime(start);hydra.declareCastProcess(null);target.discard();}
 }

}
