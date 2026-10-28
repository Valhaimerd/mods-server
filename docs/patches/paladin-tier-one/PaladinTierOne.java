package net.spell_engine.internals;

import java.util.*;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.event.SpellHandlers;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.internals.delivery.ProjectileLauncher;
import net.spell_engine.internals.impact.SpellImpacts;
import net.spell_engine.internals.container.SpellContainerSource;
import net.spell_power.api.*;
import net.bettercombat.api.AttackHand;
import net.bettercombat.logic.PlayerAttackHelper;

/** Tier-I uses native damage/healing and projectiles; only conditional delivery is custom. */
public final class PaladinTierOne {
 public static final String CHARGES="paladins:blessed_strikes";
 private static final Map<ServerPlayer,Long> NEXT_SWING=new WeakHashMap<>();
 public static boolean node(LivingEntity caster,String tail){return caster instanceof SpellContainerSource.Owner owner&&owner.getSpellContainers().modifiers().stream().anyMatch(h->h.is(Identifier.parse("skill_tree_rpgs:paladin_tier_2_"+tail)));}
 public static Holder<Spell> spell(LivingEntity caster,String path){return SpellRegistry.from(caster.level()).get(Identifier.parse("paladins:"+path)).orElseThrow();}
 public static Holder<net.minecraft.world.effect.MobEffect> chargeEffect(){return BuiltInRegistries.MOB_EFFECT.get(Identifier.parse(CHARGES)).orElseThrow();}
 public static int cloudInterval(int interval,net.spell_engine.entity.SpellCloud cloud){
  var spell=cloud.getSpellEntry();
  if(interval>=10000||spell==null||!(cloud.getOwner() instanceof LivingEntity owner))return interval;
  if(!spell.is(Identifier.parse("paladins:battle_banner"))&&!spell.is(Identifier.parse("skill_tree_rpgs:paladin_tier_4_spell_2_modifier_2")))return interval;
  return Math.max(1,(int)Math.ceil(SpellParameters.hasteAffectedValue(owner,spell.value().school,interval)));
 }
 public static int charges(LivingEntity caster){var e=caster.getEffect(chargeEffect());return e==null?0:Math.min(5,e.getAmplifier()+1);}
 public static void init(){
  SpellHandlers.registerCustomDelivery(Identifier.parse("paladin_pack:purge"),PaladinTierOne::purge);
  SpellHandlers.registerCustomDelivery(Identifier.parse("paladin_pack:prepare"),PaladinTierOne::prepare);
  net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.entity.living.LivingDeathEvent event)->{event.getEntity().removeEffect(chargeEffect());if(event.getEntity() instanceof ServerPlayer p)NEXT_SWING.remove(p);});
  net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event)->{
   if(event.getEntity() instanceof ServerPlayer p&&p.hasEffect(chargeEffect())){
    var old=p.getEffect(chargeEffect());
    if(!net.skill_tree_rpgs.compat.PaladinDoctrine.isPaladin(p)){p.removeEffect(chargeEffect());NEXT_SWING.remove(p);}
    else if(old.getAmplifier()>4){int remaining=old.getDuration();p.removeEffect(chargeEffect());p.addEffect(new MobEffectInstance(chargeEffect(),remaining,4),p);}
   }
  });
 }
 public static SpellExecution.ImpactContext context(LivingEntity caster,SpellSchool school){return new SpellExecution.ImpactContext().channeled(1).power(SpellPower.getSpellPower(school,caster));}
 public static SpellPower.Result scaled(SpellPower.Result power,double multiplier){return new SpellPower.Result(power.school(),power.baseValue()*multiplier,power.criticalChance(),power.criticalDamage());}
 public static void heal(LivingEntity caster,LivingEntity target,double coefficient){
  var healing=spell(caster,"paladin_restore");
  SpellImpacts.performImpacts(caster.level(),caster,target,caster,healing,healing.value().impacts,context(caster,SpellSchools.HEALING).power(scaled(SpellPower.getSpellPower(SpellSchools.HEALING,caster),coefficient)),false,null);
 }
 public static boolean purge(Level world,Holder<Spell> spell,LivingEntity caster,List<SpellExecution.DeliveryTarget> targets,SpellExecution.ImpactContext context,Vec3 location){
  if(world.isClientSide())return false;
  LivingEntity primary=targets.stream().map(SpellExecution.DeliveryTarget::entity).filter(e->e instanceof LivingEntity&&e.isAlive()&&!PartyRules.allies(caster,e)).map(e->(LivingEntity)e).findFirst().orElse(null);
  if(primary==null||caster.distanceToSqr(primary)>24*24||!caster.hasLineOfSight(primary))return false;
  boolean improved=node(caster,"spell_1_root"),strength=node(caster,"spell_1_modifier_1"),absolution=node(caster,"spell_1_modifier_2");
  double damageMultiplier=(absolution?.8:improved?.6:.5)/.5;
  List<LivingEntity> victims=absolution?PriestRules.nearby(caster,primary.position(),3):List.of(primary);boolean hit=false;
  var damageContext=context.channeled(1).power(scaled(SpellPower.getSpellPower(net.spell_engine.api.spell.ExternalSpellSchools.PHYSICAL_MELEE,caster),damageMultiplier));
  for(var enemy:victims)if(enemy.isAlive()&&!PartyRules.allies(caster,enemy)){
   float before=enemy.getHealth()+enemy.getAbsorptionAmount();
   SpellImpacts.performImpacts(world,caster,enemy,primary,spell,spell.value().impacts,damageContext,false,Spell.Impact.Action.Type.DAMAGE);
   hit|=enemy.getHealth()+enemy.getAbsorptionAmount()<before;
  }
  if(hit){double coefficient=strength?.4:improved?.3:.25;heal(caster,caster,coefficient);
   if(strength)for(var ally:PriestRules.nearby(caster,caster.position(),4))if(ally!=caster&&PartyRules.allies(caster,ally))heal(caster,ally,coefficient);
  }
  return true;
 }
 public static boolean prepare(Level world,Holder<Spell> spell,LivingEntity caster,List<SpellExecution.DeliveryTarget> targets,SpellExecution.ImpactContext context,Vec3 location){
  if(world.isClientSide())return false;
  int amount=Math.min(5,charges(caster)+1);caster.addEffect(new MobEffectInstance(chargeEffect(),300,amount-1),caster);
  boolean light=node(caster,"spell_2_modifier_2");heal(caster,caster,light?.1:.05);
  if(light&&context.channelTickIndex()==4)for(var ally:PriestRules.nearby(caster,caster.position(),4))if(ally!=caster&&PartyRules.allies(caster,ally))heal(caster,ally,.1);
  return true;
 }
 /** Invoked once at Better Combat's server swing execution, outside its temporary combo multipliers. */
 public static boolean swing(ServerPlayer player,AttackHand hand){
  if(!player.isAlive()||player.isSpectator()||player.isUsingItem()||hand==null||hand.attack()==null||hand.itemStack().isEmpty()||charges(player)==0||!net.skill_tree_rpgs.compat.PaladinDoctrine.isPaladin(player))return false;
  if(net.spell_engine.api.effect.EntityActionsAllowed.isImpaired(player,net.spell_engine.api.effect.EntityActionsAllowed.Player.ATTACK))return false;
  long now=player.level().getGameTime();if(now<NEXT_SWING.getOrDefault(player,Long.MIN_VALUE))return false;
  // One tick tolerance handles packet/tick quantization, not unrestricted tap spam.
  int interval=Math.max(1,(int)Math.ceil(PlayerAttackHelper.getAttackCooldownTicksCapped(player))-1);
  NEXT_SWING.put(player,now+interval);
  var old=player.getEffect(chargeEffect());int count=charges(player);int duration=old.getDuration();player.removeEffect(chargeEffect());
  if(count>1)player.addEffect(new MobEffectInstance(chargeEffect(),duration,count-2),player);
  var wave=spell(player,node(player,"spell_2_modifier_1")?"blessed_wave_wrath":"blessed_wave");
  ProjectileLauncher.shootProjectile(player.level(),player,null,wave,context(player,net.spell_engine.api.spell.ExternalSpellSchools.PHYSICAL_MELEE));return true;
 }
}
