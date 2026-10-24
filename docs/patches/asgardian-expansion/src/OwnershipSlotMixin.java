package com.emandev.bifrost.mixin;
import com.emandev.bifrost.WeaponOwnership;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(Slot.class)
public abstract class OwnershipSlotMixin {
 @Shadow @Final public Container container;
 @Shadow public abstract ItemStack getItem();
 @Inject(method="mayPickup",at=@At("HEAD"),cancellable=true)
 private void pickup(Player p,CallbackInfoReturnable<Boolean> cir){if(WeaponOwnership.boundType(getItem())&&(!WeaponOwnership.owns(p,getItem())||(!(container instanceof ResultContainer)&&p instanceof net.minecraft.server.level.ServerPlayer sp&&!WeaponOwnership.canUse(sp,getItem()))))cir.setReturnValue(false);}
 @Inject(method="mayPlace",at=@At("HEAD"),cancellable=true)
 private void place(ItemStack stack,CallbackInfoReturnable<Boolean> cir){if(WeaponOwnership.boundType(stack)&&(!(container instanceof Inventory i)||!WeaponOwnership.insertionAllowed(i.player,stack)))cir.setReturnValue(false);}
 @Inject(method="safeClone",at=@At("HEAD"),cancellable=true)
 private void clone(Player p,CallbackInfoReturnable<ItemStack> cir){if(WeaponOwnership.boundType(getItem()))cir.setReturnValue(ItemStack.EMPTY);}
 @Inject(method="set(Lnet/minecraft/world/item/ItemStack;)V",at=@At("HEAD"),cancellable=true)
 private void set(ItemStack stack,CallbackInfo ci){if(WeaponOwnership.boundType(stack)&&container instanceof Inventory i&&!WeaponOwnership.insertionAllowed(i.player,stack))ci.cancel();}
}
