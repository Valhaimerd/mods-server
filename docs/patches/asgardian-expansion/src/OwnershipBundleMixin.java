package com.emandev.bifrost.mixin;
import com.emandev.bifrost.WeaponOwnership;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(BundleContents.Mutable.class)
public abstract class OwnershipBundleMixin {
 @Inject(method="tryInsert",at=@At("HEAD"),cancellable=true)
 private void insert(ItemStack stack,CallbackInfoReturnable<Integer> cir){if(WeaponOwnership.boundType(stack))cir.setReturnValue(0);}
}
