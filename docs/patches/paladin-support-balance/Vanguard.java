package net.spell_engine.internals;
import net.minecraft.world.entity.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.spell_power.api.*;
import net.spell_engine.api.spell.event.SpellHandlers;
import net.spell_engine.api.spell.ExternalSpellSchools;
import net.spell_engine.internals.impact.SpellImpacts;
/** Native melee-impact trigger/cooldown owns readiness; the bonus cannot recursively proc itself. */
public final class Vanguard {
 public static void init(){SpellHandlers.registerCustomDelivery(net.minecraft.resources.Identifier.parse("paladin_pack:vanguard"),(world,h,caster,targets,context,pos)->{
  if(world.isClientSide()||!(caster instanceof ServerPlayer p))return false;
  var victim=targets.stream().map(SpellExecution.DeliveryTarget::entity).filter(e->e instanceof LivingEntity l&&l.isAlive()&&PartyRules.harmfulAllowed(p,l)).map(e->(LivingEntity)e).findFirst().orElse(null);if(victim==null)return false;
  var bonus=PaladinRevamp.spell(p,"paladins:oath_extra");double amount=.2*SpellPower.getSpellPower(ExternalSpellSchools.PHYSICAL_MELEE,p).nonCriticalValue();
  SpellImpacts.performImpacts(world,p,victim,p,bonus,bonus.value().impacts,new SpellExecution.ImpactContext().channeled(1).power(new SpellPower.Result(ExternalSpellSchools.PHYSICAL_MELEE,amount,0,1)),false,null);
  PaladinRevamp.addControl(victim,new MobEffectInstance(PaladinRevamp.effect("spell_engine:stun"),net.skill_tree_rpgs.compat.PaladinPassives.shieldEquipped(p)?40:20),p);return true;
 });}
}
