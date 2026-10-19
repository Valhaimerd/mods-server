package local.worldbosses.mixin;

import local.worldbosses.RaidRuntime;
import local.worldbosses.GearRetirement;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RangedAttribute.class)
public abstract class HealthRangeMixin {
    @Inject(method="sanitizeValue",at=@At("HEAD"),cancellable=true)
    private void raidHealth(double value,CallbackInfoReturnable<Double> ci) {
        if((Object)this==Attributes.MAX_HEALTH.value())ci.setReturnValue(Double.isNaN(value)?1:Math.clamp(value,1,1_000_000));
    }
    @Inject(method="getMaxValue",at=@At("HEAD"),cancellable=true)
    private void raidHealthLimit(CallbackInfoReturnable<Double> ci) {
        if((Object)this==Attributes.MAX_HEALTH.value())ci.setReturnValue(1_000_000.0);
    }
}
