package net.spell_engine.mixin.priest;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.spell_engine.internals.PriestPassives;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(targets="net.minecraft.world.effect.AbsorptionMobEffect")
public abstract class ReceivedAbsorptionMixin {
 @Unique private float priest$before;
 @Inject(method="onEffectStarted",at=@At("HEAD"))
 private void before(LivingEntity entity,int amplifier,CallbackInfo ci){if(!entity.level().isClientSide())priest$before=entity.getAbsorptionAmount();}
 @Inject(method="onEffectStarted",at=@At("TAIL"))
 private void after(LivingEntity entity,int amplifier,CallbackInfo ci){if(!entity.level().isClientSide())PriestPassives.absorptionGrant(entity,(MobEffect)(Object)this,Math.max(0,entity.getAbsorptionAmount()-priest$before),priest$before);}
}
