package local.worldbosses.mixin;

import local.worldbosses.RaidRuntime;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class BossMotionMixin {
    @Inject(method="knockback(DDDLnet/minecraft/world/damagesource/DamageSource;FZ)V",at=@At("HEAD"),cancellable=true)
    private void safeKnockback(double strength,double x,double z,DamageSource source,float amount,boolean force,CallbackInfo ci){if(!RaidRuntime.knockback((LivingEntity)(Object)this,strength,x,z,source))ci.cancel();}
}
