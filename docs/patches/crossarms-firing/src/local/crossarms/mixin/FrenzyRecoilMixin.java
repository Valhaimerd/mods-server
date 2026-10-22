package local.crossarms.mixin;
import local.crossarms.runtime.FrenzyState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** A rapid shot must restart recoil even in the first half of the preceding swing. */
@Mixin(LivingEntity.class)
public class FrenzyRecoilMixin {
 @Inject(method="swing(Lnet/minecraft/world/InteractionHand;Z)V",at=@At("HEAD"))
 private void crossarms$restartRecoil(InteractionHand hand,boolean includeSelf,CallbackInfo ci){
  LivingEntity entity=(LivingEntity)(Object)this;
  if(entity.level().isClientSide()&&FrenzyState.applies(entity,entity.getItemInHand(hand)))entity.swingTime=-1;
 }
}
