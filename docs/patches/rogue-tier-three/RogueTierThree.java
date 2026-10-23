package net.spell_engine.internals;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.spell_engine.api.effect.*;
import net.spell_engine.internals.container.SpellContainerSource;
import net.spell_engine.rpg_series.config.*;
/** Changes only Rogue tier III; ordinary effect expiry and explicit dispels remain intact. */
public final class RogueTierThree {
 public static void rogue(ConfigFile.Effects config){
  var id=Identifier.parse("rogues:improved_stealth");
  if(net.rogues.effect.RogueEffects.entries.stream().noneMatch(e->e.id.equals(id)))
   net.rogues.effect.RogueEffects.entries.add(new Effects.Entry(id,"Improved Stealth","Reduced stealth movement penalty.",new CustomStatusEffect(MobEffectCategory.BENEFICIAL,0xAAAAAA)));
  config.effects.put(id.toString(),new EffectConfig(List.of(new AttributeModifier("minecraft:movement_speed",.1f,Operation.ADD_MULTIPLIED_BASE))));
  config.effects.put("rogues:stealth_speed",new EffectConfig(List.of(new AttributeModifier("minecraft:movement_speed",.2f,Operation.ADD_MULTIPLIED_BASE))));
 }
 public static void skill(ConfigFile.Effects config){
  var id=Identifier.parse("skill_tree_rpgs:cheat_death_evasion");
  if(net.skill_tree_rpgs.effect.SkillEffects.entries.stream().noneMatch(e->e.id.equals(id)))
   net.skill_tree_rpgs.effect.SkillEffects.entries.add(new Effects.Entry(id,"Cheat Death Evasion","Increased evasion after Cheat Death.",new CustomStatusEffect(MobEffectCategory.BENEFICIAL,0xAAAAAA),new EffectConfig(List.of(new AttributeModifier("spell_engine:evasion_chance",.3f,Operation.ADD_MULTIPLIED_BASE)))));
 }
 public static boolean shadow(LivingEntity entity){
  return entity instanceof SpellContainerSource.Owner owner&&owner.getSpellContainers().modifiers().stream().anyMatch(h->h.is(Identifier.parse("skill_tree_rpgs:rogue_tier_4_spell_1_modifier_2")));
 }
 public static boolean removeForAction(LivingEntity entity,Holder<MobEffect> effect){
  if(effect.is(Identifier.parse("rogues:stealth"))&&shadow(entity))return false;
  return entity.removeEffect(effect);
 }
 public static int removeOnHit(Level level,MobEffect effect,DamageSource source,LivingEntity entity){
  if(effect==net.rogues.effect.RogueEffects.STEALTH.effect&&shadow(entity))return 0;
  return RemoveOnHit.removeCount(level,effect,source);
 }
 public static void attack(net.minecraft.world.entity.player.Player player,net.minecraft.world.entity.Entity target,net.spell_engine.internals.delivery.melee.Melee.AttackContext context){
  if(!context.spellId().equals(Identifier.parse("rogues:mutilate"))){player.attack(target);return;}
  // Native melee delivery fills lastHurtMobTimestamp, not the actual attack charge fields.
  var access=(net.spell_engine.mixin.thirdtier.RogueAttackAccess)player;
  int strength=access.rogueAttackStrength(),swap=access.rogueSwapStrength();
  try{access.rogueAttackStrength(100);access.rogueSwapStrength(100);player.attack(target);}
  finally{access.rogueAttackStrength(strength);access.rogueSwapStrength(swap);}
 }
 public static boolean meleeImpacts(LivingEntity caster,java.util.List<net.minecraft.world.entity.Entity> targets,Holder<net.spell_engine.api.spell.Spell> spell,SpellExecution.ImpactContext context){
  if(spell.is(Identifier.parse("rogues:mutilate"))){
   var damage=caster.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
   var removed=new java.util.ArrayList<net.minecraft.world.entity.ai.attributes.AttributeModifier>();
   try{
    for(String key:List.of("melee_attack","melee_attack_dual_wield","melee_attack_charge")){
     var modifier=damage.getModifier(Identifier.parse("spell_engine:"+key));
     if(modifier!=null){removed.add(modifier);damage.removeModifier(modifier);}
    }
    context=context.power(net.spell_power.api.SpellPower.getSpellPower(spell.value().school,caster));
   }finally{for(var modifier:removed)damage.addTransientModifier(modifier);}
  }
  return net.spell_engine.internals.impact.SpellImpacts.meleeImpact(caster,targets,spell,context);
 }
 public static void cleanup(LivingEntity entity){
  var effect=net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.get(Identifier.parse("rogues:improved_stealth"));
  effect.ifPresent(entity::removeEffect);
 }
}
