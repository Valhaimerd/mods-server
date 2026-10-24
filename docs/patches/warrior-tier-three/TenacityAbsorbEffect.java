package net.spell_engine.entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;
/** One HP per level allows exact whole-HP rounding with native absorption attribute expiry. */
public class TenacityAbsorbEffect extends MobEffect {
 public TenacityAbsorbEffect(){super(MobEffectCategory.BENEFICIAL,0xF0C85A);}
 @Override public void onEffectStarted(LivingEntity entity,int amplifier){super.onEffectStarted(entity,amplifier);entity.setAbsorptionAmount(Math.max(entity.getAbsorptionAmount(),amplifier+1));}
 @Override public boolean shouldApplyEffectTickThisTick(int duration,int amplifier){return true;}
 @Override public boolean applyEffectTick(ServerLevel world,LivingEntity entity,int amplifier){return entity.getAbsorptionAmount()>0;}
}
