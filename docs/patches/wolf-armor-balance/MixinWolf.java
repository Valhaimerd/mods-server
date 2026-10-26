package net.darkhax.ironwolfarmor.common.mixin;
import net.darkhax.ironwolfarmor.common.WolfArmorBalance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.animal.wolf.Wolf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Wolf.class)
public class MixinWolf {
 @Inject(method="canArmorAbsorb",at=@At("HEAD"),cancellable=true)
 private void canArmorAbsorb(DamageSource source,CallbackInfoReturnable<Boolean> callback){
  var wolf=(Wolf)(Object)this;
  if(WolfArmorBalance.reduction(wolf.getBodyArmorItem().getItem())>0)callback.setReturnValue(false);
 }
}
