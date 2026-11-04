package net.spell_engine.mixin.batch;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(targets="net.minecraft.world.effect.RegenerationMobEffect")
public class RegenerationMixin {
 @Inject(method="applyEffectTick",at=@At("HEAD"),cancellable=true)
 private void blocked(ServerLevel world,LivingEntity target,int amplifier,CallbackInfoReturnable<Boolean> result){if(net.spell_engine.internals.BatchRules.regenerationBlocked(target))result.setReturnValue(true);}
}
