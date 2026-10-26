package net.spell_engine.internals;

import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.internals.container.SpellContainerSource;
import net.spell_engine.internals.impact.SpellImpacts;
import net.spell_engine.internals.target.SpellTarget;
import net.spell_engine.rpg_series.config.*;
import net.spell_power.api.SpellPower;

/** Health-based protection and nearby-ally Celestial retaliation. */
public final class PriestDefenses {
 public static final String PAIN="skill_tree_rpgs:pain_suppression", ORBS="skill_tree_rpgs:celestial_orb";
 public static final String PAIN_SKILL="skill_tree_rpgs:priest_tier_3_passive_1", ORB_SKILL="skill_tree_rpgs:priest_tier_3_passive_2";
 private static boolean installed,retaliating;
 static boolean equipped(Player player,String id){return SpellContainerSource.passiveSpellsOf(player).stream().anyMatch(h->h.is(Identifier.parse(id)));}
 public static int levels(float health,float maximum){
  if(health<=0||maximum<=0)return 0;
  float ratio=health/maximum;
  return ratio<.20f?4:ratio<.30f?3:ratio<.40f?2:ratio<.50f?1:0;
 }
 public static void configure(ConfigFile.Effects config){
  config.effects.put(PAIN,new EffectConfig(List.of(PriestRules.attr("spell_engine:damage_taken",-.06f,Operation.ADD_MULTIPLIED_BASE))));
  if(installed)return;installed=true;
  NeoForge.EVENT_BUS.addListener((EntityTickEvent.Post event)->{
   if(event.getEntity() instanceof ServerPlayer player&&player.tickCount%5==0)update(player);
  });
  NeoForge.EVENT_BUS.addListener((LivingDeathEvent event)->{
   if(!event.getEntity().level().isClientSide()){event.getEntity().removeEffect(PriestRules.effect(PAIN));event.getEntity().removeEffect(PriestRules.effect(ORBS));}
  });
  NeoForge.EVENT_BUS.addListener((LivingDamageEvent.Post event)->{
   float amount=event.getInflictedDamage()+event.getReduction(net.neoforged.neoforge.common.damagesource.DamageContainer.Reduction.ABSORPTION);
   if(amount>0&&event.getSource().getEntity() instanceof LivingEntity attacker)protectAlly(event.getEntity(),attacker);
  });
 }
 public static void update(ServerPlayer player){
  var effect=PriestRules.effect(PAIN);var previous=player.getEffect(effect);
  int level=player.isAlive()&&equipped(player,PAIN_SKILL)?levels(player.getHealth(),player.getMaxHealth()):0;
  if(level==0){player.removeEffect(effect);return;}
  int before=previous==null?0:previous.getAmplifier()+1;
  // Remove before downgrading so vanilla's hidden stronger effect cannot return later.
  if(before!=level)player.removeEffect(effect);
  player.addEffect(new MobEffectInstance(effect,10,level-1,false,true,true),player);
  if(level>before){
   var spell=SpellRegistry.from(player.level()).get(Identifier.parse(PAIN_SKILL)).orElseThrow();
   var impact=spell.value().impacts.getFirst();
   net.spell_engine.utils.SoundHelper.playSound(player.level(),player,impact.sound);
   if(impact.visuals!=null&&impact.visuals.particles!=null)for(var particles:impact.visuals.particles)
    net.spell_engine.fx.ParticleHelper.play(player.level(),player,player.getYRot(),player.getXRot(),particles);
  }
 }
 public static void protectAlly(LivingEntity victim,LivingEntity attacker){
  if(victim.level().isClientSide()||retaliating||!attacker.isAlive())return;
  retaliating=true;
  try{
   for(var player:victim.level().getEntitiesOfClass(ServerPlayer.class,victim.getBoundingBox().inflate(8))){
    if(player==victim||!player.isAlive()||player.distanceToSqr(victim)>64||!equipped(player,ORB_SKILL)
      ||!PartyRules.allies(player,victim)||!PartyRules.harmfulAllowed(player,attacker))continue;
    var effect=PriestRules.effect(ORBS);var ready=player.getEffect(effect);if(ready==null)continue;
    var spell=SpellRegistry.from(player.level()).get(Identifier.parse(ORB_SKILL)).orElseThrow();
    int remaining=ready.getAmplifier();int duration=ready.getDuration();
    // Reserve the charge before dealing damage: retaliation cannot recursively spend it twice.
    player.removeEffect(effect);
    if(remaining>0)player.addEffect(new MobEffectInstance(effect,duration,remaining-1,ready.isAmbient(),ready.isVisible(),ready.showIcon()),player);
    var context=new SpellExecution.ImpactContext(1,1,attacker.position(),SpellPower.getSpellPower(spell.value().school,player),SpellTarget.FocusMode.DIRECT,0);
    SpellImpacts.performImpacts(player.level(),player,attacker,attacker,spell,spell.value().impacts,context);
   }
  }finally{retaliating=false;}
 }
}
