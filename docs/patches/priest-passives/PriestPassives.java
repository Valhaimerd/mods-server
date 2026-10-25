package net.spell_engine.internals;
import java.util.*;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.entity.WarriorTierTwo;
import net.spell_engine.rpg_series.config.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.*;
/** Received shielding scales only newly granted HP, never previously spent HP. */
public final class PriestPassives {
 public static final String FADE="skill_tree_rpgs:priest_fade";
 private static boolean installed;
 public static boolean faded(LivingEntity entity){return BuiltInRegistries.MOB_EFFECT.get(Identifier.parse(FADE)).map(entity::hasEffect).orElse(false);}
 public static double absorptionBonus(LivingEntity target){var effect=target.getEffect(PriestRules.effect("skill_tree_rpgs:healing_focus"));return effect==null?0:.04*(effect.getAmplifier()+1);}
 static Identifier capacityId(MobEffect source){return Identifier.fromNamespaceAndPath("priest_pack","received_"+BuiltInRegistries.MOB_EFFECT.getKey(source).toString().replace(':','_'));}
 public static void absorptionGrant(LivingEntity target,MobEffect source,float base,float before){
  if(target.level().isClientSide())return;
  var attribute=target.getAttribute(Attributes.MAX_ABSORPTION);if(attribute==null)return;
  double extra=Math.max(0,base)*absorptionBonus(target);var key=capacityId(source);var old=attribute.getModifier(key);
  if(extra>0){double total=extra+(old==null?0:old.amount());attribute.removeModifier(key);attribute.addTransientModifier(new AttributeModifier(key,total,AttributeModifier.Operation.ADD_VALUE));}
  target.setAbsorptionAmount(Math.min((float)attribute.getValue(),before+Math.max(0,base)+(float)extra));
 }
 public static void cleanup(LivingEntity target,MobEffect source){var attribute=target.getAttribute(Attributes.MAX_ABSORPTION);if(attribute!=null)attribute.removeModifier(capacityId(source));}
 public static void configure(ConfigFile.Effects config){
  config.effects.put("skill_tree_rpgs:healing_focus",new EffectConfig(List.of(PriestRules.attr("spell_engine:healing_taken",.04f,AttributeModifier.Operation.ADD_MULTIPLIED_BASE))));
  config.effects.put("skill_tree_rpgs:incanter_cadence",new EffectConfig(List.of(PriestRules.attr("spell_power:haste",.02f,AttributeModifier.Operation.ADD_MULTIPLIED_BASE))));
  config.effects.put("skill_tree_rpgs:divine_favor",new EffectConfig(List.of()));
  WarriorTierTwo.entry(FADE,"Fade",List.of(),config);
  if(installed)return;installed=true;
  net.spell_engine.api.spell.event.SpellEvents.HEAL.register(args->{
   if(args.amount()>0&&args.caster() instanceof net.minecraft.world.entity.player.Player player){
    var impact=new Spell.Impact();impact.action=new Spell.Impact.Action();impact.action.type=Spell.Impact.Action.Type.HEAL;impact.action.heal=new Spell.Impact.Action.Heal();
    SpellTriggers.onSpellImpactSpecific(player,args.target(),args.spell(),impact,false,Spell.Trigger.Stage.POST);
   }
  });
  NeoForge.EVENT_BUS.addListener((MobEffectEvent.Remove event)->{if(!event.getEntity().level().isClientSide()&&event.getEffectInstance()!=null)cleanup(event.getEntity(),event.getEffectInstance().getEffect().value());});
  NeoForge.EVENT_BUS.addListener((MobEffectEvent.Expired event)->{if(!event.getEntity().level().isClientSide()&&event.getEffectInstance()!=null)cleanup(event.getEntity(),event.getEffectInstance().getEffect().value());});
  NeoForge.EVENT_BUS.addListener((LivingDamageEvent.Post event)->{if((event.getInflictedDamage()>0||event.getReduction(net.neoforged.neoforge.common.damagesource.DamageContainer.Reduction.ABSORPTION)>0)&&event.getSource().getEntity() instanceof LivingEntity attacker)retaliate(event.getEntity(),attacker);});
 }
 public static void retaliate(LivingEntity victim,LivingEntity attacker){
  if(victim.level().isClientSide()||!victim.hasEffect(PriestRules.effect("skill_tree_rpgs:divine_favor"))||!PartyRules.harmfulAllowed(victim,attacker))return;
  victim.removeEffect(PriestRules.effect("skill_tree_rpgs:divine_favor"));
  var targets=new HashSet<>(PriestRules.nearby(victim,victim.position(),2));targets.add(attacker);
  for(var target:targets)if(PartyRules.harmfulAllowed(victim,target)&&target.isAlive())PartyRules.addEffect(target,new MobEffectInstance(PriestRules.effect("spell_engine:stun"),20),victim);
 }
}
