package com.eiden.iourusraces.mixin;
import com.eiden.iourusraces.race.RaceVisualRules;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(LivingEntity.class)
public abstract class LivingEntityClimbingMixin {
 @Inject(method="onClimbable",at=@At("RETURN"),cancellable=true)
 private void raceLeafClimbing(CallbackInfoReturnable<Boolean> cir){
  if(!cir.getReturnValue()&&(Object)this instanceof Player player&&RaceVisualRules.leafClimbing(player))cir.setReturnValue(true);
 }
}
