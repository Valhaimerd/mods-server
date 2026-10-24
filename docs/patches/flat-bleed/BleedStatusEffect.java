package net.spell_engine.api.effect;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
/** One fixed debuff; existing cadence and damage source are preserved. */
public class BleedStatusEffect extends MobEffect {
 public BleedStatusEffect(MobEffectCategory category,int color){super(category,color);}
 @Override public boolean shouldApplyEffectTickThisTick(int duration,int amplifier){return duration%25==0;}
 @Override public boolean applyEffectTick(ServerLevel world,LivingEntity entity,int amplifier){
  // Ignore microscopic floating-point drift; vertical movement alone is stationary.
  float damage=entity.getDeltaMovement().horizontalDistanceSqr()>1e-6?6f:2f;
  entity.hurtServer(world,entity.damageSources().magic(),damage);return true;
 }
}
