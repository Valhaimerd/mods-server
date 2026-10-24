package com.emandev.bifrost.mixin;
import com.emandev.bifrost.WeaponOwnership;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.transfer.item.*;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(VanillaContainerWrapper.class)
public abstract class OwnershipTransferMixin {
 @Shadow @Final private Container container;
 @Inject(method="insert(ILnet/neoforged/neoforge/transfer/item/ItemResource;ILnet/neoforged/neoforge/transfer/transaction/TransactionContext;)I",at=@At("HEAD"),cancellable=true)
 private void insert(int slot,ItemResource item,int amount,TransactionContext transaction,CallbackInfoReturnable<Integer> cir){
  var stack=item.toStack();if(WeaponOwnership.boundType(stack)&&(!(container instanceof Inventory i)||!WeaponOwnership.insertionAllowed(i.player,stack)))cir.setReturnValue(0);
 }
}
