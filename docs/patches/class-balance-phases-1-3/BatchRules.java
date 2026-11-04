package net.spell_engine.internals;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.food.FoodData;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.effect.*;
import net.spell_engine.rpg_series.config.ConfigFile;
import net.spell_engine.rpg_series.config.EffectConfig;
import net.spell_power.api.*;
public final class BatchRules {
 public static final String REGEN_LOCK="skill_tree_rpgs:coated_regeneration_lock";
 public static void skill(ConfigFile.Effects config){
  config.effects.put("skill_tree_rpgs:vitality",new EffectConfig(List.of(new net.spell_engine.rpg_series.config.AttributeModifier("minecraft:armor",1,AttributeModifier.Operation.ADD_VALUE),new net.spell_engine.rpg_series.config.AttributeModifier("minecraft:max_health",2,AttributeModifier.Operation.ADD_VALUE))));
  config.effects.put("skill_tree_rpgs:fracture",new EffectConfig(List.of(new net.spell_engine.rpg_series.config.AttributeModifier("minecraft:armor",-1,AttributeModifier.Operation.ADD_VALUE))));
  config.effects.put("skill_tree_rpgs:smoke_screen",new EffectConfig(List.of(new net.spell_engine.rpg_series.config.AttributeModifier("spell_engine:evasion_chance",.65f,AttributeModifier.Operation.ADD_MULTIPLIED_BASE))));
  config.effects.put(net.spell_engine.entity.WarriorTierTwo.ENDURANCE,new EffectConfig(List.of(new net.spell_engine.rpg_series.config.AttributeModifier("minecraft:knockback_resistance",1,AttributeModifier.Operation.ADD_VALUE),new net.spell_engine.rpg_series.config.AttributeModifier("minecraft:armor",4,AttributeModifier.Operation.ADD_VALUE))));
  var key=Identifier.parse(REGEN_LOCK);var entries=net.skill_tree_rpgs.effect.SkillEffects.entries;
  if(entries.stream().noneMatch(e->e.id.equals(key))){var effect=new CustomStatusEffect(MobEffectCategory.HARMFUL,0x60852C);Synchronized.configure(effect,true);entries.add(new Effects.Entry(key,"Regeneration Blocked","Natural regeneration and Regeneration are disabled.",effect,new EffectConfig(List.of())));}
  config.effects.put(REGEN_LOCK,new EffectConfig(List.of()));
  var aegis=Identifier.parse("skill_tree_rpgs:aegis_guard");
  if(entries.stream().noneMatch(e->e.id.equals(aegis))){var effect=new CustomStatusEffect(MobEffectCategory.BENEFICIAL,0x80C8FF);Synchronized.configure(effect,true);entries.add(new Effects.Entry(aegis,"Aegis Guard","15% physical and magic damage reduction.",effect,new EffectConfig(List.of())));}
  config.effects.put(aegis.toString(),new EffectConfig(List.of(new net.spell_engine.rpg_series.config.AttributeModifier("spell_engine:damage_taken",-.15f,AttributeModifier.Operation.ADD_MULTIPLIED_BASE))));
 }
 public static boolean regenerationBlocked(LivingEntity target){return BuiltInRegistries.MOB_EFFECT.get(Identifier.parse(REGEN_LOCK)).map(target::hasEffect).orElse(false);}
 public static void fullFood(FoodData food,int ignored){food.setFoodLevel(20);food.setSaturation(20);}
 public static boolean retired(boolean old,String id){return old||id.equals("fractured_nullstone")||id.equals("nameless_trinkets:fractured_nullstone");}
 public static void retiredStats(Object stats){
  if(!stats.getClass().getName().contains("FracturedNullstoneBase$Stats"))return;
  try{stats.getClass().getField("isEnable").setBoolean(stats,false);stats.getClass().getField("magicDamageReductionPercentage").setFloat(stats,0);}catch(ReflectiveOperationException ex){throw new IllegalStateException(ex);}
 }
 public static double trapActivation(double radius,double oldRatio){return radius>1.5?1.5:radius*oldRatio;}
 public static float pulseOffset(float old,Holder<Spell> spell){return spell.is(Identifier.parse("rogues:last_stand"))||spell.is(Identifier.parse("rogues:mortal_strike"))?0:old;}
 public static double fractureScaling(double power){return -Math.floor(Math.max(0,power)/13.);}
 public static boolean after(boolean ok,LivingEntity caster,Entity target,Holder<Spell> spell,Spell.Impact impact){
  if(!ok||caster.level().isClientSide()||!(target instanceof LivingEntity victim)||!PartyRules.harmfulAllowed(caster,target))return ok;
  if(spell.is(Identifier.parse("rogues:shadow_step"))&&impact.action.type==Spell.Impact.Action.Type.TELEPORT)
   PartyRules.addEffect(victim,new MobEffectInstance(PaladinRevamp.effect("archers:entangling_roots"),20),caster);
  if(spell.is(Identifier.parse("skill_tree_rpgs:rogue_tier_1_passive_2"))&&impact.action.type==Spell.Impact.Action.Type.STATUS_EFFECT&&"skill_tree_rpgs:fracture".equals(impact.action.status_effect.effect_id))
   victim.hurtServer((ServerLevel)caster.level(),new VitalDamage(caster),victim.getMaxHealth()*.05f);
  return ok;
 }
 private static final class VitalDamage extends net.minecraft.world.damagesource.DamageSource {
  VitalDamage(LivingEntity caster){super(caster.damageSources().mobAttack(caster).typeHolder(),caster);((net.spell_engine.entity.DamageSourceExtension)(Object)this).setSpellIndirect(true);}
  @Override public boolean is(net.minecraft.tags.TagKey<net.minecraft.world.damagesource.DamageType> tag){return tag==net.minecraft.tags.DamageTypeTags.BYPASSES_COOLDOWN||super.is(tag);}
 }
 public static void lastStandPulse(LivingEntity caster,float ignored){caster.heal(caster.getMaxHealth()*.2f);}
}
