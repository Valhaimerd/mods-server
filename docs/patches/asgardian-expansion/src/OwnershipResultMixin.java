package com.emandev.bifrost.mixin;
import com.emandev.bifrost.WeaponOwnership;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(ResultSlot.class)
public abstract class OwnershipResultMixin {
 @Inject(method="onTake",at=@At("HEAD"))
 private void commit(Player player,ItemStack stack,CallbackInfo ci){if(player instanceof ServerPlayer p)WeaponOwnership.finishCraft(p,stack);}
 @Inject(method="safeClone",at=@At("HEAD"),cancellable=true)
 private void clone(Player p,CallbackInfoReturnable<ItemStack> cir){if(WeaponOwnership.boundType(((ResultSlot)(Object)this).getItem()))cir.setReturnValue(ItemStack.EMPTY);}
}
