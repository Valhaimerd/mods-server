package com.emandev.bifrost.mixin;
import com.emandev.bifrost.WeaponOwnership;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
/** Optional integration: Better Revive creates a grave on FIRST downing, not final death. */
@Pseudo
@Mixin(targets="de.mrstilu2k6.better_revive.util.KeepGear",remap=false)
public abstract class OwnershipKeepGearMixin {
 @Inject(method="setGraveSlot",at=@At("HEAD"),cancellable=true,require=0)
 private static void exclude(NonNullList<ItemStack> contents,int slot,ItemStack stack,ServerPlayer player,CallbackInfoReturnable<ItemStack> cir){
  if(WeaponOwnership.boundType(stack)&&!WeaponOwnership.id(stack).isEmpty())cir.setReturnValue(contents.set(slot,ItemStack.EMPTY));
 }
}
