package com.emandev.bifrost.mixin;
import com.emandev.bifrost.WeaponOwnership;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(Inventory.class)
public abstract class OwnershipDropMixin {
 @Shadow public abstract ItemStack getSelectedItem();
 @Inject(method="removeFromSelected",at=@At("HEAD"),cancellable=true)
 private void drop(boolean all,CallbackInfoReturnable<ItemStack> cir){if(WeaponOwnership.boundType(getSelectedItem())&&!WeaponOwnership.id(getSelectedItem()).isEmpty())cir.setReturnValue(ItemStack.EMPTY);}
}
