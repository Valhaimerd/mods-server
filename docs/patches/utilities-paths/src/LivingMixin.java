package net.valhaimerd.orboblivioncompat.mixin;
import net.valhaimerd.orboblivioncompat.Utilities;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.phys.Vec3;
import net.minecraft.tags.BlockTags;
import net.minecraft.server.level.ServerPlayer;
@Mixin(LivingEntity.class)
public class LivingMixin {
 @Unique private double utilities$airFraction;
 @Inject(method="decreaseAirSupply",at=@At("RETURN"),cancellable=true)
 private void air(int old,CallbackInfoReturnable<Integer> cir){
  if((Object)this instanceof ServerPlayer p&&cir.getReturnValue()<old&&Utilities.stat(p,"breath")>0){
   double amount=(old-cir.getReturnValue())/(1+Utilities.stat(p,"breath"));utilities$airFraction+=amount;
   int loss=(int)Math.floor(utilities$airFraction+1e-9);utilities$airFraction-=loss;cir.setReturnValue(old-loss);
  }
 }
 @Inject(method="handleRelativeFrictionAndCalculateMovement",at=@At("RETURN"),cancellable=true)
 private void climb(Vec3 input,float friction,CallbackInfoReturnable<Vec3> cir){
  if((Object)this instanceof Player p&&p.onClimbable()&&p.getInBlockState().is(BlockTags.CLIMBABLE)){
   Vec3 v=cir.getReturnValue();if(v.y>0)cir.setReturnValue(new Vec3(v.x,v.y*(1+Utilities.stat(p,"climb")),v.z));
  }
 }
}
