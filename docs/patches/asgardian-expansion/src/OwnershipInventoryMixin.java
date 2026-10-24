package com.emandev.bifrost.mixin;
import com.emandev.bifrost.WeaponOwnership;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(Inventory.class)
public abstract class OwnershipInventoryMixin {
 @Shadow @Final public Player player;
 @Inject(method="setItem",at=@At("HEAD"),cancellable=true)
 private void boundSet(int slot,ItemStack stack,CallbackInfo ci){if(!WeaponOwnership.insertionAllowed(player,stack))ci.cancel();}
 @Inject(method="add(ILnet/minecraft/world/item/ItemStack;)Z",at=@At("HEAD"),cancellable=true)
 private void boundAdd(int slot,ItemStack stack,CallbackInfoReturnable<Boolean> cir){if(!WeaponOwnership.insertionAllowed(player,stack))cir.setReturnValue(false);}
}
