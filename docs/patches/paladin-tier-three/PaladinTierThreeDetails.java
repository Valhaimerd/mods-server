package net.spell_engine.internals;
import java.util.*;import net.minecraft.world.entity.LivingEntity;import net.minecraft.world.effect.MobEffectInstance;import net.minecraft.core.registries.BuiltInRegistries;
public final class PaladinTierThreeDetails {public static List<String> append(List<String> original,LivingEntity e,MobEffectInstance effect){var result=new ArrayList<String>(original);result.addAll(PaladinUltimates.details(e,BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value()).toString()));return result;}}
