package net.spell_engine.entity;

import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.spell_engine.internals.container.SpellContainerSource;
import net.spell_engine.internals.PartyRules;
import net.spell_engine.rpg_series.config.ConfigFile;
import net.spell_engine.rpg_series.config.EffectConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.bus.api.EventPriority;

/** Small overlay over the installed Warrior rules; no saved IDs change. */
public final class WarriorBalance {
 private static boolean listening;
 public static void configure(ConfigFile.Effects config) {
  config.effects.put("skill_tree_rpgs:killing_spree",new EffectConfig(List.of(
   WarriorTierTwo.attr("minecraft:attack_damage",.02f,Operation.ADD_MULTIPLIED_BASE))));
  config.effects.put("skill_tree_rpgs:enrage",new EffectConfig(List.of(
   WarriorTierTwo.attr("minecraft:attack_damage",.03f,Operation.ADD_MULTIPLIED_BASE),
   WarriorTierTwo.attr("minecraft:scale",.06f,Operation.ADD_MULTIPLIED_BASE))));
  if(listening)return;listening=true;
  NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,WarriorBalance::killed);
 }
 public static void killed(LivingDeathEvent event) {
  if(event.isCanceled()||event.getEntity().level().isClientSide())return;
  if(!(event.getSource().getEntity() instanceof Player player)||!player.isAlive()
    ||!PartyRules.harmfulAllowed(player,event.getEntity()))return;
  if(SpellContainerSource.passiveSpellsOf(player).stream().anyMatch(h->WarriorRules.id(h,"skill_tree_rpgs:warrior_tier_1_passive_1")))
   player.heal(player.getMaxHealth()*.04f);
 }
 public static int freedomTicks(LivingEntity caster) {
  var buff=caster.getEffect(WarriorRules.effect("rogues:charge"));
  return buff==null?40:buff.getDuration();
 }
 public static int speedLevel(LivingEntity caster) {
  var buff=caster.getEffect(WarriorRules.effect(WarriorUltimates.BERSERKER));
  return buff==null?0:Math.min(4,buff.getAmplifier());
 }
 public static boolean interrupting(net.minecraft.world.effect.MobEffectInstance instance) {
  if(instance.getEffect().is(net.minecraft.resources.Identifier.parse("wizards:frozen"))
    ||instance.getEffect().is(net.minecraft.resources.Identifier.parse("spell_engine:stun")))return true;
  var actions=((net.spell_engine.api.effect.ActionImpairing)instance.getEffect().value()).actionsAllowed();
  return actions!=null&&!actions.players().canCastSpell();
 }
 public static void lethalityHeal(Player player,float amount) {
  var spell=WarriorUltimates.spell(player,"skill_tree_rpgs:warrior_tier_4_spell_1_modifier_2");
  var cooldown=((net.spell_engine.internals.casting.SpellCaster.Player)player).getCooldownManager();
  if(cooldown.isCoolingDown(spell))return;
  cooldown.set(spell,Math.max(1,Math.round(net.spell_engine.internals.SpellParameters.getCooldownDuration(player,spell)*20)));
  player.heal(amount);
 }
}
