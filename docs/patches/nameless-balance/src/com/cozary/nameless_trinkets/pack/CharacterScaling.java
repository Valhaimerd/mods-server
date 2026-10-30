package com.cozary.nameless_trinkets.pack;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.*;

/** Reconstruct outside attributes without temporary mutation or accessory feedback. */
public final class CharacterScaling {
 private CharacterScaling(){}
 private static final Set<String> EXCLUDED=Set.of(
  "nameless_trinkets:titans_mark_health","nameless_trinkets:ghast_eye_extra_health",
  "nameless_trinkets:woundbearer_attack_damage","artifacts:crystal_heart/max_health","artifacts:power_glove/attack_damage");
 private static boolean excluded(AttributeModifier modifier,Holder<Attribute> attribute){
  return EXCLUDED.contains(modifier.id().toString()) || (attribute.equals(Attributes.MAX_HEALTH) && modifier.id().toString().equals("minecraft:effect.health_boost"));
 }
 public static double factor(LivingEntity wearer,Holder<Attribute> attribute){
  var instance=wearer.getAttribute(attribute);if(instance==null)return 1;
  double baseMultiplier=1,totalMultiplier=1;
  for(var modifier:instance.getModifiers())if(!excluded(modifier,attribute)){
   if(modifier.operation()==AttributeModifier.Operation.ADD_MULTIPLIED_BASE)baseMultiplier+=modifier.amount();
   if(modifier.operation()==AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)totalMultiplier*=1+modifier.amount();
  }
  double factor=baseMultiplier*totalMultiplier;
  return Double.isFinite(factor) && factor>0 ? factor : 0;
 }
 public static double baseline(LivingEntity wearer,Holder<Attribute> attribute){
  var instance=wearer.getAttribute(attribute);if(instance==null)return 0;
  double value=instance.getBaseValue();
  for(var modifier:instance.getModifiers())if(!excluded(modifier,attribute) && modifier.operation()==AttributeModifier.Operation.ADD_VALUE)value+=modifier.amount();
  value*=factor(wearer,attribute);return Double.isFinite(value)?Math.max(0,value):0;
 }
 public static double amount(LivingEntity wearer,Holder<Attribute> attribute,double ratio,double cap){
  double factor=factor(wearer,attribute);return factor>0?Math.min(cap,baseline(wearer,attribute)*ratio)/factor:0;
 }
 public static double woundCap(LivingEntity wearer){return Math.min(4,baseline(wearer,Attributes.ATTACK_DAMAGE)*.1);}
 public static void update(LivingEntity wearer,Holder<Attribute> attribute,String key,double ratio,double cap){
  var instance=wearer.getAttribute(attribute);if(instance==null)return;
  var id=Identifier.parse(key);double value=amount(wearer,attribute,ratio,cap);var previous=instance.getModifier(id);
  if(previous==null || Math.abs(previous.amount()-value)>1e-8 || previous.operation()!=AttributeModifier.Operation.ADD_VALUE){
   instance.removeModifier(id);instance.addTransientModifier(new AttributeModifier(id,value,AttributeModifier.Operation.ADD_VALUE));
  }
  clampHealth(wearer);
 }
 public static void clampHealth(LivingEntity wearer){if(wearer.getHealth()>wearer.getMaxHealth())wearer.setHealth(wearer.getMaxHealth());}
}
