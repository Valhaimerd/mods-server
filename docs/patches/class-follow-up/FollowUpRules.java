package net.spell_engine.internals;

import java.util.*;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.*;
import net.minecraft.world.damagesource.*;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.event.SpellHandlers;
import net.spell_engine.internals.casting.SpellCaster;
import net.spell_engine.rpg_series.config.*;
import net.spell_power.api.SpellPower;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.*;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.tick.*;

/** Native effects own saved durations; no retained player references. */
@EventBusSubscriber(modid="spell_engine")
public final class FollowUpRules {
 public static final String OP="skill_tree_rpgs:rogue_tier_2_passive_2",READY="skill_tree_rpgs:opportunist_ready",CRIT="skill_tree_rpgs:opportunist_window";
 private static boolean installed;
 static Identifier id(String s){return Identifier.parse(s);}
 public static boolean owns(LivingEntity e,String s){return PaladinRevamp.passive(e,s);}
 public static boolean basic(Player p,DamageSource s){return s.getEntity()==p&&s.getDirectEntity()==p&&s.is(DamageTypes.PLAYER_ATTACK)&&!((net.spell_engine.entity.DamageSourceExtension)s).isSpellIndirect()&&((SpellCaster.Player)p).getActiveMeleeSkill()==null;}
 public static boolean guaranteed(net.critical_strike.internal.CriticalStriker original,DamageSource source){
  return source.getEntity() instanceof Player p&&basic(p,source)&&p.hasEffect(PaladinRevamp.effect(CRIT))||original.rng_shouldDealCriticalHit();
 }
 public static void configure(ConfigFile.Effects config){
  config.effects.put("skill_tree_rpgs:supercharge",new EffectConfig(List.of()));
  config.effects.put(CRIT,new EffectConfig(List.of()));
  config.effects.put("skill_tree_rpgs:incanter_cadence",new EffectConfig(List.of(new AttributeModifier("spell_power:haste",.03f,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE))));
  config.effects.put("skill_tree_rpgs:warrior_momentum",new EffectConfig(List.of(new AttributeModifier("minecraft:attack_speed",.02f,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE))));
  net.spell_engine.entity.WarriorTierTwo.entry(READY,"Opportunist Ready",List.of(),config);
  net.spell_engine.entity.WarriorTierTwo.entry(CRIT,"Opportunist",List.of(),config);
  if(installed)return;installed=true;
  SpellHandlers.registerCustomDelivery(id("class_pack:opportunist"),(world,h,caster,targets,context,pos)->{
   if(world.isClientSide()||!(caster instanceof ServerPlayer p)||p.hasEffect(PaladinRevamp.effect(READY))||p.hasEffect(PaladinRevamp.effect(CRIT)))return false;
   if(((SpellCaster.Player)p).getCooldownManager().isCoolingDown(h))return false;
   p.addEffect(new MobEffectInstance(PaladinRevamp.effect(READY),100),p);return true;
  });
  SpellHandlers.registerCustomDelivery(id("class_pack:fade"),(world,h,caster,targets,context,pos)->{
   if(!(world instanceof ServerLevel level))return false;
   for(Entity entity:level.getAllEntities())if(entity instanceof Mob mob&&mob.getTarget()==caster){
    mob.setTarget(null);
    for(var memory:List.of(net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET,net.minecraft.world.entity.ai.memory.MemoryModuleType.ANGRY_AT))mob.getBrain().eraseMemory(memory);
    if(mob instanceof NeutralMob neutral)neutral.stopBeingAngry();
    mob.setLastHurtByMob(null);
   }
   caster.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,20),caster);
   caster.addEffect(new MobEffectInstance(PaladinRevamp.effect(PriestPassives.FADE),20),caster);return true;
  });
 }
 public static boolean deferred(Holder<Spell> spell){return spell.is(id(OP));}
 public static void cooldown(LivingEntity p){if(!(p instanceof ServerPlayer player)||!owns(p,OP))return;var h=PaladinRevamp.spell(p,OP);((SpellCaster.Player)player).getCooldownManager().set(h,Math.round(SpellParameters.getCooldownDuration(p,h)*20));}
 @SubscribeEvent(priority=EventPriority.LOWEST) public static void damaged(LivingDamageEvent.Post event){
  if(event.getInflictedDamage()<=0&&event.getReduction(net.neoforged.neoforge.common.damagesource.DamageContainer.Reduction.ABSORPTION)<=0)return;
  if(!(event.getSource().getEntity() instanceof ServerPlayer p)||!PartyRules.harmfulAllowed(p,event.getEntity()))return;
  if(p.hasEffect(PaladinRevamp.effect("rogues:stealth"))&&RogueTierThree.shadow(p))p.removeEffect(PaladinRevamp.effect("rogues:stealth"));
  if(basic(p,event.getSource())&&owns(p,OP)&&p.hasEffect(PaladinRevamp.effect(READY))){
   p.removeEffect(PaladinRevamp.effect(READY));p.addEffect(new MobEffectInstance(PaladinRevamp.effect(CRIT),20),p);
  }
 }
 @SubscribeEvent public static void removed(MobEffectEvent.Remove e){if(e.getEffectInstance()!=null&&e.getEffectInstance().getEffect().is(id(READY)))cooldown(e.getEntity());}
 @SubscribeEvent public static void expired(MobEffectEvent.Expired e){if(e.getEffectInstance()!=null&&e.getEffectInstance().getEffect().is(id(READY)))cooldown(e.getEntity());}
 @SubscribeEvent public static void tick(PlayerTickEvent.Post event){
  var p=event.getEntity();if(p.level().isClientSide()||p.tickCount%5!=0)return;
  // Removed effect templates cannot remove modifiers persisted by older templates.
  for(String[] entry:new String[][]{{"minecraft:movement_speed","skill_tree_rpgs:warrior_momentum"},{"minecraft:attack_damage","skill_tree_rpgs:warrior_momentum"},{"spell_engine:damage_taken","skill_tree_rpgs:warrior_momentum"},{"ranged_weapon:haste","skill_tree_rpgs:supercharge"}}){
   var holder=net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.get(id(entry[0]));
   if(holder.isPresent()){var attribute=p.getAttribute(holder.get());if(attribute!=null)attribute.removeModifier(id(entry[1]));}
  }
  p.removeEffect(PaladinRevamp.effect("skill_tree_rpgs:opportunist"));
  if(!p.isAlive()||!owns(p,OP)){p.removeEffect(PaladinRevamp.effect(READY));p.removeEffect(PaladinRevamp.effect(CRIT));}
 }
 public static SpellExecution.ImpactContext warmup(LivingEntity e,Holder<Spell> spell,SpellExecution.ImpactContext context){
  if(!spell.is(id("skill_tree_rpgs:warrior_tier_2_passive_1")))return context;
  var school=net.spell_engine.api.spell.ExternalSpellSchools.PHYSICAL_MELEE_DUAL;
  double d=SpellPower.getSpellPower(school,e).nonCriticalValue();
  return context.power(new SpellPower.Result(spell.value().school,1+Math.floor(Math.max(0,d)/14),0,1));
 }
}
