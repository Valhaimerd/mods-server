package com.emandev.bifrost.mixin;
import com.emandev.bifrost.WeaponOwnership;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(Mob.class)
public abstract class OwnershipMobMixin {
 @Inject(method="pickUpItem",at=@At("HEAD"),cancellable=true)
 private void pickup(ServerLevel level,ItemEntity item,CallbackInfo ci){if(WeaponOwnership.boundType(item.getItem()))ci.cancel();}
 @Inject(method="canHoldItem",at=@At("HEAD"),cancellable=true)
 private void hold(ItemStack stack,CallbackInfoReturnable<Boolean> cir){if(WeaponOwnership.boundType(stack))cir.setReturnValue(false);}
 @Inject(method="wantsToPickUp",at=@At("HEAD"),cancellable=true)
 private void wants(ServerLevel level,ItemStack stack,CallbackInfoReturnable<Boolean> cir){if(WeaponOwnership.boundType(stack))cir.setReturnValue(false);}
}
