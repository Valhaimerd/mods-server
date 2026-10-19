package local.worldbosses.mixin;

import local.worldbosses.RaidRuntime;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.PoisonMobEffect;
import net.minecraft.world.effect.WitherMobEffect;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Source ownership exists only during the recorded boss effect's own damage tick. */
@Mixin({PoisonMobEffect.class,WitherMobEffect.class})
public abstract class BossDotMixin {
    @Inject(method="applyEffectTick",at=@At("HEAD"))
    private void begin(ServerLevel level,LivingEntity target,int amplifier,CallbackInfoReturnable<Boolean> ci){RaidRuntime.beginDot(target,(Object)this instanceof PoisonMobEffect?"poison":"wither");}
    @Inject(method="applyEffectTick",at=@At("RETURN"))
    private void end(CallbackInfoReturnable<Boolean> ci){RaidRuntime.endDot();}
}
