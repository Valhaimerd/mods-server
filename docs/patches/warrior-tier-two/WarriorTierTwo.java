package net.spell_engine.entity;
import java.util.*;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.spell_engine.api.effect.*;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.internals.*;
import net.spell_engine.rpg_series.config.*;
import net.spell_power.api.*;
/** Explicit Warrior tier-II rules; saved spell and effect IDs remain stable. */
public final class WarriorTierTwo {
 public static final String FREEDOM="skill_tree_rpgs:warrior_freedom",ENDURANCE="skill_tree_rpgs:warrior_endurance",MOMENTUM="skill_tree_rpgs:warrior_momentum";
 private static final Identifier ARMOR=Identifier.parse("warrior_pack:vitality_scaling");
 static net.spell_engine.rpg_series.config.AttributeModifier attr(String id,float amount,Operation op){return new net.spell_engine.rpg_series.config.AttributeModifier(id,amount,op);}
 public static double half(double amount){return Math.floor(Math.max(0,amount)*2+.5)/2;}
 public static double power(LivingEntity caster){return SpellPower.getSpellPower(net.spell_engine.api.spell.ExternalSpellSchools.PHYSICAL_MELEE,caster).nonCriticalValue();}
 public static boolean node(LivingEntity caster,String suffix){return ArcherProjectileRules.node(caster,"warrior_tier_3_"+suffix);}
 public static void entry(String id,String name,List<net.spell_engine.rpg_series.config.AttributeModifier> attrs,ConfigFile.Effects config){
  var key=Identifier.parse(id);var entries=net.skill_tree_rpgs.effect.SkillEffects.entries;
  if(entries.stream().noneMatch(e->e.id.equals(key))){
   var effect=new CustomStatusEffect(MobEffectCategory.BENEFICIAL,0xE7B14C);net.spell_engine.api.effect.Synchronized.configure(effect,true);
   entries.add(new Effects.Entry(key,name,name,effect,new EffectConfig(attrs)));
  }
  config.effects.put(id,new EffectConfig(attrs));
 }
 public static List<net.spell_engine.rpg_series.config.AttributeModifier> offense(float amount){
  var result=new ArrayList<net.spell_engine.rpg_series.config.AttributeModifier>();
  result.add(attr("minecraft:attack_damage",amount,Operation.ADD_MULTIPLIED_BASE));result.add(attr("ranged_weapon:damage",amount,Operation.ADD_MULTIPLIED_BASE));
  for(String school:List.of("arcane","fire","frost","lightning","soul")){
   String id="spell_power:"+school;if(BuiltInRegistries.ATTRIBUTE.get(Identifier.parse(id)).isPresent())result.add(attr(id,amount,Operation.ADD_MULTIPLIED_BASE));
  }
  return result;
 }
 public static void skill(ConfigFile.Effects config){
  entry(FREEDOM,"Unhindered",List.of(),config);
  entry(ENDURANCE,"Endurance",List.of(attr("minecraft:knockback_resistance",1,Operation.ADD_VALUE)),config);
  entry("skill_tree_rpgs:warrior_momentum_ready","Momentum Ready",List.of(),config);
  entry(MOMENTUM,"Momentum",List.of(attr("minecraft:movement_speed",.05f,Operation.ADD_MULTIPLIED_BASE),attr("minecraft:attack_speed",.05f,Operation.ADD_MULTIPLIED_BASE),attr("minecraft:attack_damage",.02f,Operation.ADD_MULTIPLIED_BASE),attr("spell_engine:damage_taken",-.02f,Operation.ADD_MULTIPLIED_BASE)),config);
  config.effects.put("skill_tree_rpgs:battle_shout",new EffectConfig(offense(.08f)));
  OnRemoval.configure(net.skill_tree_rpgs.effect.SkillEffects.VITALITY.effect,c->{var armor=c.entity().getAttribute(Attributes.ARMOR);if(armor!=null)armor.removeModifier(ARMOR);});
 }
 public static void rogue(ConfigFile.Effects config){
  config.effects.put("rogues:charge",new EffectConfig(List.of(attr("minecraft:movement_speed",.5f,Operation.ADD_MULTIPLIED_BASE),attr("minecraft:knockback_resistance",.25f,Operation.ADD_VALUE))));
  config.effects.put("rogues:demoralize",new EffectConfig(offense(-.08f)));
 }
 public static boolean freedom(LivingEntity target){return BuiltInRegistries.MOB_EFFECT.get(Identifier.parse(FREEDOM)).map(target::hasEffect).orElse(false);}
 public static boolean impairing(Holder<MobEffect> effect){
  var allowed=((ActionImpairing)effect.value()).actionsAllowed();
  if(allowed!=null&&(!allowed.canMove()||!allowed.canJump()||!allowed.players().canCastSpell()))return true;
  String id=BuiltInRegistries.MOB_EFFECT.getKey(effect.value()).toString();
  if(Set.of("wizards:frozen","spell_engine:stun","rogues:shock","rogues:bear_trap","rogues:net_trap","skill_tree_rpgs:natures_grasp").contains(id))return true;
  boolean[] negative={false};effect.value().createModifiers(0,(attribute,modifier)->{
   if((attribute.equals(Attributes.MOVEMENT_SPEED)||attribute.equals(Attributes.FLYING_SPEED)||attribute.equals(Attributes.JUMP_STRENGTH))&&modifier.amount()<0)negative[0]=true;
  });return negative[0];
 }
 public static boolean deny(LivingEntity target,Holder<MobEffect> effect){return freedom(target)&&impairing(effect);}
 public static void cleanse(LivingEntity target){
  if(!freedom(target))return;
  for(var effect:List.copyOf(target.getActiveEffects()))if(impairing(effect.getEffect()))target.removeEffect(effect.getEffect());
  ((EntityActionsAllowed.ControlledEntity)target).updateEntityActionsAllowed();
 }
 public static boolean impaired(LivingEntity target,EntityActionsAllowed.Player action,boolean show,Holder<Spell> spell){
  if(action==EntityActionsAllowed.Player.CAST_SPELL&&(WarriorRules.id(spell,"rogues:charge")||WarriorRules.id(spell,"rogues:shout")))return false;
  return EntityActionsAllowed.isImpaired(target,action,show);
 }
 public static void onEffect(LivingEntity target,MobEffectInstance instance,Entity source,boolean applied){
  if(!applied)return;
  if(instance.getEffect().is(Identifier.parse(FREEDOM)))cleanse(target);
  if(instance.getEffect().is(Identifier.parse("skill_tree_rpgs:vitality"))){
   var armor=target.getAttribute(Attributes.ARMOR);var current=target.getEffect(instance.getEffect());
   if(armor!=null&&current!=null){double bonus=half(.06*power(target))*(current.getAmplifier()+1);armor.removeModifier(ARMOR);if(bonus>0)armor.addPermanentModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(ARMOR,bonus,Operation.ADD_VALUE));}
  }
 }
 public static boolean forceDemoralize(LivingEntity target,MobEffectInstance effect,Entity source,boolean applied){
  if(applied||!(effect.getEffect().is(Identifier.parse("rogues:demoralize"))||effect.getEffect().is(Identifier.parse("minecraft:glowing"))))return applied;
  try{var current=target.getEffect(effect.getEffect());
   if(current==null){target.getActiveEffectsMap().put(effect.getEffect(),effect);var m=LivingEntity.class.getDeclaredMethod("onEffectAdded",MobEffectInstance.class,Entity.class);m.setAccessible(true);m.invoke(target,effect,source);return true;}
   if(current.update(effect)){var m=LivingEntity.class.getDeclaredMethod("onEffectUpdated",MobEffectInstance.class,boolean.class,Entity.class);m.setAccessible(true);m.invoke(target,current,true,source);return true;}
  }catch(ReflectiveOperationException error){throw new IllegalStateException("Demoralize callbacks",error);}return false;
 }
 public static void after(boolean success,LivingEntity caster,Entity target,Holder<Spell> spell,Spell.Impact impact){
  if(success&&WarriorRules.id(spell,"rogues:charge")&&impact.action.type==Spell.Impact.Action.Type.STATUS_EFFECT&&"rogues:charge".equals(impact.action.status_effect.effect_id)){
   caster.addEffect(new MobEffectInstance(WarriorRules.effect(FREEDOM),40),caster);cleanse(caster);
   if(node(caster,"spell_1_modifier_1")){var duration=caster.getEffect(WarriorRules.effect("rogues:charge")).getDuration();caster.addEffect(new MobEffectInstance(WarriorRules.effect(ENDURANCE),duration),caster);}
  }
 }
 public static SpellExecution.ImpactContext context(LivingEntity caster,Holder<Spell> spell,SpellExecution.ImpactContext context){
  if(!WarriorRules.id(spell,"skill_tree_rpgs:warrior_tier_2_passive_1"))return context;
  double p=context.power()!=null?context.power().nonCriticalValue():power(caster);
  return context.power(new SpellPower.Result(spell.value().school,1+half(.06*p),0,1));
 }
}
