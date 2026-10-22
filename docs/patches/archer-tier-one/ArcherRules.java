package net.spell_engine.internals;
import java.util.List;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.spell_engine.rpg_series.config.AttributeModifier;
import net.spell_engine.rpg_series.config.ConfigFile;
import net.spell_engine.rpg_series.config.EffectConfig;
/** Enforce the pack's changed effects even with pre-existing generated configs. */
public final class ArcherRules {
 private ArcherRules(){}
 public static void archer(ConfigFile.Effects config){
  config.effects.put("archers:hunters_mark",new EffectConfig(List.of(
   new AttributeModifier("spell_engine:damage_taken",.07f,Operation.ADD_MULTIPLIED_BASE))));
  config.effects.put("archers:entangling_roots",new EffectConfig(List.of(
   new AttributeModifier("minecraft:movement_speed",-.6f,Operation.ADD_MULTIPLIED_BASE),
   new AttributeModifier("minecraft:jump_strength",-.6f,Operation.ADD_MULTIPLIED_BASE))));
 }
 public static void skill(ConfigFile.Effects config){
  config.effects.put("skill_tree_rpgs:rhythm",new EffectConfig(List.of(
   new AttributeModifier("ranged_weapon:haste",.03f,Operation.ADD_MULTIPLIED_BASE))));
 }
}
