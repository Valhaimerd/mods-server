package local.crossarms.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Critical Strike rolls independently of the vanilla arrow critical flag. */
@Pseudo
@Mixin(targets = "net.critical_strike.internal.CritLogic", remap = false)
public class ZeroDropCriticalMixin {
    @Inject(method = "modifyDamage", at = @At("HEAD"), cancellable = true, remap = false)
    private static void crossarms$noZeroDropCritical(@Coerce Object striker, DamageSource source,
            float amount, CallbackInfoReturnable<Object> result) {
        if (source.getDirectEntity() instanceof AbstractArrow arrow
                && (arrow.entityTags().contains("CROSSARMS_NO_CRITICAL")
                    || arrow.entityTags().contains("ZERO_DROP"))) {
            result.setReturnValue(null);
        }
    }
}
