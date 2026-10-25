package local.audit;
import java.util.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.spell_engine.internals.*;
import net.spell_engine.internals.casting.SpellCaster;
import net.spell_engine.internals.container.SpellContainerSource;
import net.spell_engine.internals.impact.SpellImpacts;
import net.spell_engine.api.spell.Spell;
public final class PassiveProbe {
 static void equip(PriestProbe t,String key){var owner=(SpellContainerSource.Owner)t.p;var old=owner.getSpellContainers();var ids=List.of(key);var container=new net.spell_engine.api.spell.container.SpellContainer(net.spell_engine.api.spell.container.SpellContainer.ContentType.NONE,"","",0,ids);owner.setSpellContainers(new SpellContainerSource.Result(old.activeContainer(),List.of(),List.of(t.spell(key)),List.of(),List.of(new SpellContainerSource.SourcedContainer("passive",ItemStack.EMPTY,container))));owner.spellModifierCache().clear();}
 static void waitCooldown(PriestProbe t,String key){var cd=((SpellCaster.Player)t.p).getCooldownManager();for(int n=0;n<100&&cd.isCoolingDown(t.spell(key));n++)cd.tickUpdate();}
 public static void run(PriestProbe t){
  var p=t.p;var world=t.world;var cd=((SpellCaster.Player)p).getCooldownManager();p.removeAllEffects();
  String focus="skill_tree_rpgs:priest_tier_1_passive_1";equip(t,focus);var holy=t.spell("paladins:holy_beam");
  for(int n=1;n<=5;n++){
   p.setHealth(1);SpellImpacts.performImpacts(world,p,p,p,holy,holy.value().impacts,t.context(holy,0),false,null);
   t.eq("Focus stack "+n,p.getEffect(PriestRules.effect("skill_tree_rpgs:healing_focus")).getAmplifier()+1,n);t.eq("Focus duration "+n,p.getEffect(PriestRules.effect("skill_tree_rpgs:healing_focus")).getDuration(),120);
   SpellImpacts.performImpacts(world,p,p,p,holy,holy.value().impacts,t.context(holy,0),false,null);t.eq("Focus prevents same-tick stack "+n,p.getEffect(PriestRules.effect("skill_tree_rpgs:healing_focus")).getAmplifier()+1,n);waitCooldown(t,focus);
  }
  p.setHealth(1);SpellImpacts.performImpacts(world,p,p,p,holy,holy.value().impacts,t.context(holy,0),false,null);t.eq("Focus 20 percent received healing",p.getHealth()-1,7.2);
  p.setAbsorptionAmount(0);PriestRules.stack(p,PriestRules.SHIELD,1,10,60,p);t.eq("Focus boosts newly received shield",p.getAbsorptionAmount(),1.2);
  p.setAbsorptionAmount(.2f);PriestRules.stack(p,PriestRules.SHIELD,1,10,60,p);t.eq("Focus preserves consumed shield",p.getAbsorptionAmount(),1.4);
  for(int i=0;i<8;i++)PriestRules.stack(p,PriestRules.SHIELD,1,10,60,p);t.eq("Focus boosted shield maximum",p.getAbsorptionAmount(),11);
  PriestRules.stack(p,PriestRules.SHIELD,1,10,60,p);t.eq("Focus capped shield no refill",p.getAbsorptionAmount(),11);
  p.removeAllEffects();p.setAbsorptionAmount(0);t.eq("Focus shield extra capacity cleans up",p.getAttributeValue(Attributes.MAX_ABSORPTION),0);
  String cadence="skill_tree_rpgs:priest_tier_1_passive_2";equip(t,cadence);double haste=p.getAttributeValue(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.get(Identifier.parse("spell_power:haste")).orElseThrow());var enemy=t.mob(0,3);
  for(int n=1;n<=5;n++){
   SpellTriggers.onSpellImpactSpecific(p,enemy,holy,holy.value().impacts.get(1),false,Spell.Trigger.Stage.POST);t.eq("Cadence guaranteed stack "+n,p.getEffect(PriestRules.effect("skill_tree_rpgs:incanter_cadence")).getAmplifier()+1,n);
   SpellTriggers.onSpellImpactSpecific(p,enemy,holy,holy.value().impacts.get(1),false,Spell.Trigger.Stage.POST);t.eq("Cadence rate limit "+n,p.getEffect(PriestRules.effect("skill_tree_rpgs:incanter_cadence")).getAmplifier()+1,n);waitCooldown(t,cadence);
  }
  t.eq("Cadence maximum haste",p.getAttributeValue(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.get(Identifier.parse("spell_power:haste")).orElseThrow()),haste*1.1);t.eq("Cadence duration",p.getEffect(PriestRules.effect("skill_tree_rpgs:incanter_cadence")).getDuration(),120);p.removeAllEffects();enemy.discard();
  String fade="skill_tree_rpgs:priest_tier_2_passive_1";equip(t,fade);SpellTriggers.onRoll(p);t.eq("Fade works without nearby enemies",PriestPassives.faded(p)?1:0,1);t.eq("Fade invisibility duration",p.getEffect(MobEffects.INVISIBILITY).getDuration(),40);t.eq("Fade base cooldown",t.spell(fade).value().cost.cooldown.duration,6);p.removeAllEffects();
  String divine="skill_tree_rpgs:priest_tier_2_passive_2";equip(t,divine);double crit=p.getAttributeValue(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.get(Identifier.parse("spell_power:critical_chance")).orElseThrow());SpellTriggers.onRoll(p);t.eq("Divine guaranteed readiness",p.getEffect(PriestRules.effect("skill_tree_rpgs:divine_favor")).getDuration(),300);t.eq("Divine removes old critical bonus",p.getAttributeValue(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.get(Identifier.parse("spell_power:critical_chance")).orElseThrow()),crit);
  var attacker=t.mob(0,9);var near=t.mob(1,1);var far=t.mob(5,5);PriestPassives.retaliate(p,attacker);t.eq("Divine distant attacker stunned",attacker.getEffect(PriestRules.effect("spell_engine:stun")).getDuration(),20);t.eq("Divine nearby enemy stunned",near.getEffect(PriestRules.effect("spell_engine:stun")).getDuration(),20);t.eq("Divine outside radius safe",far.hasEffect(PriestRules.effect("spell_engine:stun"))?1:0,0);t.eq("Divine caster safe",p.hasEffect(PriestRules.effect("spell_engine:stun"))?1:0,0);t.eq("Divine readiness consumed",p.hasEffect(PriestRules.effect("skill_tree_rpgs:divine_favor"))?1:0,0);
  attacker.discard();near.discard();far.discard();p.removeAllEffects();t.equip();
  var victim=t.mob(0,3);attacker=t.mob(0,9);victim.addEffect(new MobEffectInstance(PriestRules.effect("skill_tree_rpgs:divine_favor"),300));victim.hurtServer(world,world.damageSources().mobAttack(attacker),2);t.eq("Native damage event consumes Divine",victim.hasEffect(PriestRules.effect("skill_tree_rpgs:divine_favor"))?1:0,0);t.eq("Native damage event stuns attacker",attacker.getEffect(PriestRules.effect("spell_engine:stun")).getDuration(),20);victim.discard();attacker.discard();
 }
}
