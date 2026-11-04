package net.spell_engine.internals;
import java.util.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.core.*;import net.minecraft.core.registries.*;import net.minecraft.resources.Identifier;import net.minecraft.server.level.ServerPlayer;import net.minecraft.world.entity.*;import net.minecraft.world.entity.ai.attributes.*;import net.minecraft.world.effect.*;import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;import net.neoforged.neoforge.event.tick.EntityTickEvent;import net.neoforged.neoforge.event.entity.living.*;
import net.spell_engine.api.effect.*;import net.spell_engine.api.spell.Spell;import net.spell_engine.rpg_series.config.*;
public final class FinalRules {
 static boolean installed;
 public static String full(String s){return s.contains(":")?s:"skill_tree_rpgs:"+s;}
 public static Holder<MobEffect> effect(String s){return PriestRules.effect(full(s));}
 public static boolean owns(LivingEntity e,String s){return PaladinRevamp.passive(e,full(s));}
 public static void status(LivingEntity e,String id,int ticks,int amplifier,LivingEntity caster){e.addEffect(new MobEffectInstance(effect(id),ticks,amplifier),caster);}
 static net.spell_engine.rpg_series.config.AttributeModifier attr(String s,float v,AttributeModifier.Operation op){return PriestRules.attr(s,v,op);}
 public static void configure(ConfigFile.Effects c){
  for(String name:List.of("ascension_guard","vanguard_guard","break_knockback","purgatory_opening","purelight_cost","onslaught_ready","onslaught_power","deflection_speed")){
   var list=net.skill_tree_rpgs.effect.SkillEffects.entries;var id=Identifier.parse(full(name));if(list.stream().noneMatch(x->x.id.equals(id))){var fx=new CustomStatusEffect(MobEffectCategory.BENEFICIAL,0xF8DC88);Synchronized.configure(fx,true);list.add(new Effects.Entry(id,name.replace('_',' '),"",fx,new EffectConfig(List.of())));}c.effects.put(full(name),new EffectConfig(List.of()));
  }
  var base=AttributeModifier.Operation.ADD_MULTIPLIED_BASE;var value=AttributeModifier.Operation.ADD_VALUE;
  c.effects.put(PriestRules.AURA,new EffectConfig(List.of("minecraft:attack_damage","ranged_weapon:damage","spell_power:arcane","spell_power:fire","spell_power:frost","spell_power:lightning","spell_power:soul").stream().filter(s->BuiltInRegistries.ATTRIBUTE.get(Identifier.parse(s)).isPresent()).map(s->attr(s,.06f,base)).toList()));
  c.effects.put(full("rhythm"),new EffectConfig(List.of(attr("ranged_weapon:haste",.04f,base))));
  c.effects.put(full("break_knockback"),new EffectConfig(List.of(attr("minecraft:knockback_resistance",1,value))));
  c.effects.put(full("purgatory_opening"),new EffectConfig(List.of(attr("minecraft:armor",.3f,base))));
  c.effects.put(full("deflection_speed"),new EffectConfig(List.of(attr("minecraft:movement_speed",.3f,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL))));
  c.effects.put(full("onslaught_power"),new EffectConfig(List.of(attr("ranged_weapon:haste",.3f,base),attr("ranged_weapon:damage",.15f,base))));
  if(installed)return;installed=true;FinalDome.init();FinalPurelight.init();FinalArcher.init();FinalDefenses.init();
  NeoForge.EVENT_BUS.addListener(FinalRules::tick);
  NeoForge.EVENT_BUS.addListener((LivingDeathEvent e)->{for(String id:List.of("ascension_guard","vanguard_guard","break_knockback","purgatory_opening","onslaught_ready","onslaught_power","deflection_speed"))e.getEntity().removeEffect(effect(id));});NeoForge.EVENT_BUS.addListener(FinalRules::defense);NeoForge.EVENT_BUS.addListener(FinalRules::damage);
 }
 public static void registered(){Protection.PROTECTIONS.remove(effect("deflection").unwrapKey().orElseThrow());}
 public static Spell.Impact impact(Spell.Impact input,LivingEntity caster,Holder<Spell> h){
  if(h.is(Identifier.parse("paladins:circle_of_healing"))&&input.action.type==Spell.Impact.Action.Type.HEAL){var x=MageFollowup.copy(input,Spell.Impact.class);x.action.heal.spell_power_coefficient=PriestRules.node(caster,"tier_3_spell_1_root")?.75f:.6f;return x;}return input;
 }
 public static boolean vanguardControl(LivingEntity target,MobEffectInstance fx,LivingEntity caster){boolean applied=PaladinRevamp.addControl(target,fx,caster);if(applied&&caster instanceof ServerPlayer player)status(caster,"vanguard_guard",40,net.skill_tree_rpgs.compat.PaladinPassives.shieldEquipped(player)?1:0,caster);return applied;}
 public static void opening(LivingEntity caster){if(!caster.level().isClientSide())status(caster,"purgatory_opening",60,0,caster);}
 public static void endOpening(LivingEntity caster){caster.removeEffect(effect("purgatory_opening"));}
 public static boolean breakRestriction(net.minecraft.world.level.Level world,Holder<Spell> h,LivingEntity caster,List<SpellExecution.DeliveryTarget> targets,SpellExecution.ImpactContext context,net.minecraft.world.phys.Vec3 at){if(world.isClientSide())return false;PaladinRevamp.cleanse(caster);status(caster,"break_knockback",60,0,caster);return true;}
 static void tick(EntityTickEvent.Post event){if(!(event.getEntity() instanceof LivingEntity e)||e.level().isClientSide())return;
  if(e.hasEffect(effect("ascension_guard"))&&(!e.isAlive()||!e.hasEffect(effect("paladins:levitate"))||!MageFollowup.active(e)))e.removeEffect(effect("ascension_guard"));
  if(e.hasEffect(effect("purgatory_opening"))&&PaladinUltimates.channelOf(e)==null)e.removeEffect(effect("purgatory_opening"));
  if(e.hasEffect(effect("vanguard_guard"))&&(!e.isAlive()||!owns(e,"paladin_tier_1_passive_1")))e.removeEffect(effect("vanguard_guard"));
  if(!owns(e,"archer_tier_2_passive_2")){e.removeEffect(effect("onslaught_ready"));e.removeEffect(effect("onslaught_power"));}
  if(e.hasEffect(effect("deflection"))&&!owns(e,"archer_tier_3_passive_2"))e.removeEffect(effect("deflection"));
 }
 static void defense(LivingDamageEvent.Pre event){var e=event.getEntity();if(!PaladinUltimates.ordinary(event.getSource())||event.getNewDamage()<=0)return;
  double guard=e.hasEffect(effect("ascension_guard"))?.5:0;var v=e.getEffect(effect("vanguard_guard"));if(v!=null)guard=Math.max(guard,v.getAmplifier()>0?.25:.15);if(guard==0)return;
  double old=Math.max(SupportScaling.activeReduction(e),PaladinUltimates.n(PaladinUltimates.bannerValues(e),"reduction"));event.setNewDamage((float)(event.getNewDamage()*(1-Math.max(old,guard))/Math.max(.001,1-old)));
 }
 static void damage(LivingDamageEvent.Post event){if(FinalDefenses.PASSIVE.get()||event.getInflictedDamage()+event.getReduction(net.neoforged.neoforge.common.damagesource.DamageContainer.Reduction.ABSORPTION)<=0||!(event.getSource().getEntity() instanceof ServerPlayer caster)||!caster.hasEffect(effect("onslaught_ready"))||!owns(caster,"archer_tier_2_passive_2")||!FinalDefenses.qualifying(event.getEntity(),event.getSource())||!PartyRules.harmfulAllowed(caster,event.getEntity()))return;caster.removeEffect(effect("onslaught_ready"));status(caster,"onslaught_power",80,0,caster);}
 public static List<String> details(List<String> old,LivingEntity e,String id){var out=new ArrayList<>(old);switch(id){
  case "skill_tree_rpgs:ascension_guard"->out.add("General damage reduction: 50%");
  case "skill_tree_rpgs:vanguard_guard"->out.add("General damage reduction: "+(e.getEffect(effect("vanguard_guard"))!=null&&e.getEffect(effect("vanguard_guard")).getAmplifier()>0?25:15)+"%");
  case "skill_tree_rpgs:onslaught_ready"->out.add("Next direct player hit activates Onslaught");
  case "skill_tree_rpgs:onslaught_power"->{out.add("Draw speed: +30%");out.add("Ranged damage: +15%");}
  case "skill_tree_rpgs:break_knockback"->out.add("Knockback resistance: 100%");
  case "skill_tree_rpgs:purgatory_opening"->out.add("Armor: +30%");
  case "skill_tree_rpgs:deflection_speed"->out.add("Movement speed multiplier: 1.30x");
  case "skill_tree_rpgs:purelight_cost"->out.add("Lose 1 HP every 0.5s; bypasses absorption until downed or dead");
 }return out;}
}
