package net.spell_engine.entity;

import com.google.gson.Gson;
import java.util.*;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.internals.*;
import net.spell_engine.rpg_series.config.*;

/** Warrior-specific overrides layered over the existing party/Rogue rules. */
public final class WarriorRules {
 private static final Gson JSON=new Gson();
 private static boolean listening;
 private record Drag(Vec3 destination,int expires){}
 private static final Map<LivingEntity,Drag> DRAGS=new WeakHashMap<>();
 private static final ThreadLocal<Boolean> DRAGGING=ThreadLocal.withInitial(()->false);
 public static boolean id(Holder<Spell> h,String id){return ArcherProjectileRules.is(h,id);}
 public static boolean node(LivingEntity owner,String suffix){return ArcherProjectileRules.node(owner,"warrior_tier_2_"+suffix);}
 public static net.spell_power.api.SpellSchool hasteSchool(net.spell_power.api.SpellSchool original,LivingEntity caster,Spell spell){
  if(spell.group!=null&&spell.group.startsWith("warrior_"))return net.spell_engine.api.spell.ExternalSpellSchools.PHYSICAL_MELEE;
  var secondWind=net.spell_engine.api.spell.registry.SpellRegistry.from(caster.level()).get(Identifier.parse("skill_tree_rpgs:warrior_tier_2_passive_2"));
  return secondWind.isPresent()&&secondWind.get().value()==spell?net.spell_engine.api.spell.ExternalSpellSchools.PHYSICAL_MELEE:original;
 }
 public static Holder<MobEffect> effect(String id){return BuiltInRegistries.MOB_EFFECT.get(Identifier.parse(id)).orElseThrow();}
 public static void skill(ConfigFile.Effects config){
  WarriorTierTwo.skill(config);
  config.effects.put("skill_tree_rpgs:killing_spree",new EffectConfig(List.of(
   new net.spell_engine.rpg_series.config.AttributeModifier("minecraft:attack_damage",.02f,Operation.ADD_MULTIPLIED_BASE),
   new net.spell_engine.rpg_series.config.AttributeModifier("spell_engine:damage_taken",-.02f,Operation.ADD_MULTIPLIED_BASE))));
  config.effects.put("skill_tree_rpgs:vitality",new EffectConfig(List.of(
   new net.spell_engine.rpg_series.config.AttributeModifier("minecraft:armor",1,Operation.ADD_VALUE),
   new net.spell_engine.rpg_series.config.AttributeModifier("minecraft:max_health",2,Operation.ADD_VALUE))));
  if(listening)return;listening=true;
  // This event occurs after armor, Resistance and enchantment reduction, before absorption.
  net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Pre e)->shield(e));
  net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.tick.EntityTickEvent.Post e)->{
   if(e.getEntity() instanceof LivingEntity living&&!living.level().isClientSide())dragTick(living);
  });
 }
 public static boolean frontal(LivingEntity entity,DamageSource source){
  if(entity.getItemBlockingWith()==null)return false;
  Vec3 point=source.getSourcePosition();
  // Source-less environmental damage has no front; it is not a shield hit.
  if(point==null)return false;
  Vec3 towards=point.subtract(entity.position()).multiply(1,0,1).normalize();
  Vec3 facing=Vec3.directionFromRotation(0,entity.getYHeadRot());
  return towards.lengthSqr()>0&&towards.dot(facing)>0;
 }
 public static void shield(net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Pre event){
  LivingEntity defender=event.getEntity();
  if(event.getNewDamage()<=0||!frontal(defender,event.getSource()))return;
  float prevented=event.getNewDamage()*.8f;
  var stack=defender.getItemBlockingWith();var component=stack.get(DataComponents.BLOCKS_ATTACKS);
  // Preserve NeoForge shield callbacks, including the existing Spell Engine bridge.
  var block=new net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent(defender,event.getContainer(),prevented,true);
  net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(block);
  block.setBlocked(true);block.setBlockedDamage(prevented);
  event.getContainer().setBlockedDamage(block);
  if(defender.level() instanceof ServerLevel level){
   component.onBlocked(level,defender);
   component.hurtBlockingItem(level,stack,defender,defender.getUsedItemHand(),event.getOriginalDamage()*.8f,block.shieldDamage());
  }
 }
 public static boolean direct(DamageSource source){
  if(source==null||source.getEntity()==null||source.getDirectEntity() instanceof SpellCloud)return false;
  String key=source.typeHolder().unwrapKey().orElseThrow().identifier().toString();
  return !Set.of("minecraft:in_fire","minecraft:on_fire","minecraft:lava","minecraft:hot_floor","minecraft:freeze","minecraft:wither","minecraft:magic","minecraft:indirect_magic","minecraft:starve","minecraft:drown","minecraft:fall","minecraft:cramming","minecraft:in_wall","minecraft:fly_into_wall","minecraft:outside_border").contains(key)
   &&!key.contains("poison")&&!key.contains("bleed")&&!key.contains("burn")&&!key.contains("wither");
 }
 public static boolean evaluate(Holder<Spell> h,Spell.Trigger trigger,SpellTriggers.Event event){
  if(id(h,"skill_tree_rpgs:warrior_tier_1_passive_2")&&(!direct(event.damageSource)||event.damageAmount<=0))return false;
  if(id(h,"skill_tree_rpgs:warrior_tier_1_passive_1")){
   var target=event.target(trigger);if(!(target instanceof LivingEntity)||!PartyRules.harmfulAllowed(event.player,target))return false;
  }
  return RogueRules.evaluate(h,trigger,event);
 }
 public static boolean addEffect(LivingEntity target,MobEffectInstance instance,Entity source){
  if(WarriorTierTwo.deny(target,instance.getEffect()))return false;
  float before=target.getMaxHealth();boolean applied=RogueRules.addEffect(target,instance,source);
  applied=WarriorTierTwo.forceDemoralize(target,instance,source,applied);
  WarriorTierTwo.onEffect(target,instance,source,applied);
  if(applied&&instance.getEffect().is(Identifier.parse("skill_tree_rpgs:vitality"))){
   float added=Math.max(0,target.getMaxHealth()-before);if(added>0)target.heal(added);
  }
  if(!applied&&instance.getEffect().is(Identifier.parse("rogues:net_trap"))){
   // The spell's 100+2M HP gate has already been checked. Only type immunity is bypassed.
   try{
    var existing=target.getEffect(instance.getEffect());
    if(existing==null){target.getActiveEffectsMap().put(instance.getEffect(),instance);var m=LivingEntity.class.getDeclaredMethod("onEffectAdded",MobEffectInstance.class,Entity.class);m.setAccessible(true);m.invoke(target,instance,source);return true;}
    if(existing.update(instance)){var m=LivingEntity.class.getDeclaredMethod("onEffectUpdated",MobEffectInstance.class,boolean.class,Entity.class);m.setAccessible(true);m.invoke(target,existing,true,source);return true;}
   }catch(ReflectiveOperationException error){throw new IllegalStateException("Net effect callback",error);}
  }
  return applied;
 }
 public static boolean bossGate(Entity target,net.minecraft.tags.TagKey<EntityType<?>> tag,Spell.Impact impact){
  if(impact.action.type==Spell.Impact.Action.Type.STATUS_EFFECT&&"rogues:net_trap".equals(impact.action.status_effect.effect_id))return false;
  if(impact.action.type==Spell.Impact.Action.Type.STATUS_EFFECT&&java.util.Set.of("rogues:demoralize","minecraft:glowing").contains(impact.action.status_effect.effect_id))return false;
  return PartyRules.bossGate(target,tag,impact);
 }
 public static Spell.Impact impact(LivingEntity caster,Holder<Spell> spell,Spell.Impact original,SpellExecution.ImpactContext context,Entity target){
  var result=RogueRules.impact(caster,spell,original,context,target);
  if(id(spell,"rogues:throw")&&result.action.type==Spell.Impact.Action.Type.DAMAGE){
   result=JSON.fromJson(JSON.toJson(result),Spell.Impact.class);
   result.action.damage.spell_power_coefficient=node(caster,"spell_1_modifier_2")?1:node(caster,"spell_1_root")?.9f:.8f;
  }
  return result;
 }
 public static boolean after(boolean success,LivingEntity caster,Entity target,Holder<Spell> spell,Spell.Impact impact){
  success=RogueRules.after(success,caster,target,spell,impact);
  WarriorTierTwo.after(success,caster,target,spell,impact);
  if(!success||!(target instanceof LivingEntity living)||!PartyRules.harmfulAllowed(caster,target))return success;
  if(id(spell,"rogues:throw")&&impact.action.type==Spell.Impact.Action.Type.DAMAGE&&node(caster,"spell_1_modifier_2"))
   PartyRules.addEffect(living,new MobEffectInstance(effect("spell_engine:stun"),5),caster);
  if(id(spell,"rogues:throw_net")&&impact.action.type==Spell.Impact.Action.Type.STATUS_EFFECT&&"rogues:net_trap".equals(impact.action.status_effect.effect_id)){
   Vec3 forward=caster.getViewVector(1).multiply(1,0,1).normalize();
   DRAGS.put(living,new Drag(caster.position().add(forward.scale(2)),living.tickCount+20));
  }
  return success;
 }
 public static Vec3 movement(Entity entity,Vec3 movement){
  return entity instanceof LivingEntity living&&living.hasEffect(effect("rogues:net_trap"))&&!DRAGGING.get()?Vec3.ZERO:movement;
 }
 public static void dragTick(LivingEntity target){
  var drag=DRAGS.get(target);if(drag==null)return;
  if(!target.isAlive()||target.tickCount>=drag.expires||!target.hasEffect(effect("rogues:net_trap"))){DRAGS.remove(target);return;}
  Vec3 delta=drag.destination.subtract(target.position());
  if(delta.length()<.1){DRAGS.remove(target);return;}
  Vec3 start=target.position(),step=delta.scale(Math.min(1.5,delta.length())/delta.length());
  DRAGGING.set(true);try{target.move(MoverType.SELF,step);}finally{DRAGGING.set(false);}
  if(target instanceof ServerPlayer player)player.teleportTo(target.getX(),target.getY(),target.getZ());
  target.setDeltaMovement(Vec3.ZERO);target.resetFallDistance();target.needsSync=true;
  if(target.position().distanceToSqr(start)<.001)DRAGS.remove(target);
 }
 public static Entity launch(Entity entity,LivingEntity caster,Holder<Spell> spell,SpellExecution.ImpactContext context){
  if(entity instanceof SpellProjectile p&&(id(spell,"rogues:throw")||id(spell,"rogues:throw_net"))){
   p.range=SpellParameters.getRange(caster,spell,context.charge());
   // Native ImpactContext already contains the pre-launch power snapshot, retained in the projectile.
   if(id(spell,"rogues:throw")&&!caster.level().isClientSide())caster.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,40,3),caster);
  }
  return entity;
 }
 public static void beforeTick(SpellProjectile p){
  if(p.level().isClientSide()||!id(p.getSpellEntry(),"rogues:throw")||p.impactHistory.isEmpty()||!(p.getOwner() instanceof LivingEntity caster)||!node(caster,"spell_1_modifier_1"))return;
  var nearest=p.level().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(24),e->e.isAlive()&&e.isAttackable()&&e.distanceToSqr(p)<=576&&!p.impactHistory.contains(e.getId())&&PartyRules.harmfulAllowed(caster,e))
   .stream().min(Comparator.comparingDouble(e->e.distanceToSqr(p))).orElse(null);
  p.setFollowedTarget(nearest);
 }
}
