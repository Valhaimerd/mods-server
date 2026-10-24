package net.spell_engine.internals;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.*;
import net.spell_engine.api.effect.*;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.internals.casting.SpellCaster;
import net.spell_engine.rpg_series.config.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.*;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
/** Readiness is a native saved effect: no entity map, scheduler or tick listener. */
@EventBusSubscriber(modid="spell_engine")
public final class PreparedCounter {
 public static final Identifier READY=Identifier.parse("skill_tree_rpgs:prepared_counter_ready");
 public static void configure(ConfigFile.Effects config){
  var entries=net.skill_tree_rpgs.effect.SkillEffects.entries;
  if(entries.stream().noneMatch(e->e.id.equals(READY)))entries.add(new Effects.Entry(READY,"Prepared Counter","Your next basic melee hit adds 25% of melee power.",new CustomStatusEffect(MobEffectCategory.BENEFICIAL,0xCCBB77)));
  config.effects.put(READY.toString(),new EffectConfig(List.of()));
 }
 public static boolean allowed(Holder<Spell> spell,Spell.Trigger trigger,SpellTriggers.Event event){
  if(!spell.is(Identifier.parse("skill_tree_rpgs:rogue_tier_3_passive_2")))return true;
  var enemy=event.target(trigger);return enemy instanceof LivingEntity&&enemy.isAlive()&&PartyRules.harmfulAllowed(event.player,enemy);
 }
 @SubscribeEvent(priority=EventPriority.LOWEST)
 public static void hit(LivingIncomingDamageEvent event){
  if(!(event.getSource().getEntity() instanceof Player p)||p.level().isClientSide()||event.getAmount()<=0||event.getSource().getDirectEntity()!=p
    ||!event.getSource().is(net.minecraft.world.damagesource.DamageTypes.PLAYER_ATTACK)
    ||((net.spell_engine.entity.DamageSourceExtension)event.getSource()).isSpellIndirect()
    ||((SpellCaster.Player)p).getActiveMeleeSkill()!=null||!PartyRules.harmfulAllowed(p,event.getEntity()))return;
  var effect=BuiltInRegistries.MOB_EFFECT.get(READY).orElseThrow();if(!p.hasEffect(effect))return;
  double power=net.spell_power.api.SpellPower.getSpellPower(net.spell_engine.api.spell.ExternalSpellSchools.PHYSICAL_MELEE,p).nonCriticalValue();
  p.removeEffect(effect);event.setAmount(event.getAmount()+(float)(.25*power));
 }
}
