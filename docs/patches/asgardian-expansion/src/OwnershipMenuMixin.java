package com.emandev.bifrost.mixin;
import com.emandev.bifrost.WeaponOwnership;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(AbstractContainerMenu.class)
public abstract class OwnershipMenuMixin {
 @Shadow public abstract ItemStack getCarried();
 @Shadow public abstract Slot getSlot(int slot);
 @Shadow public abstract boolean isValidSlotIndex(int slot);
 @Shadow public abstract void broadcastFullState();
 @Inject(method="clicked",at=@At("HEAD"),cancellable=true)
 private void clicked(int slot,int button,ContainerInput input,Player p,CallbackInfo ci){
  if(!(p instanceof ServerPlayer))return;
  ItemStack target=slot>=0&&isValidSlotIndex(slot)?getSlot(slot).getItem():ItemStack.EMPTY;
  ItemStack cursor=getCarried();
  if(WeaponOwnership.boundType(target)&&input==ContainerInput.QUICK_MOVE&&getSlot(slot).container instanceof Inventory&&!((Object)this instanceof InventoryMenu)){ci.cancel();broadcastFullState();return;}
  if(WeaponOwnership.boundType(target)&&(input==ContainerInput.CLONE||input==ContainerInput.THROW)){ci.cancel();broadcastFullState();return;}
  if(slot>=0&&isValidSlotIndex(slot)&&!(getSlot(slot).container instanceof Inventory)&&input==ContainerInput.SWAP&&button>=0&&button<p.getInventory().getContainerSize()&&WeaponOwnership.boundType(p.getInventory().getItem(button))){ci.cancel();broadcastFullState();return;}
  if(WeaponOwnership.boundType(target)&&cursor.has(net.minecraft.core.component.DataComponents.BUNDLE_CONTENTS)){ci.cancel();broadcastFullState();return;}
  boolean deniedTarget=WeaponOwnership.boundType(target)&&!WeaponOwnership.owns(p,target);
  if(WeaponOwnership.boundType(target)&&!(getSlot(slot).container instanceof ResultContainer)&&p instanceof ServerPlayer sp&&!WeaponOwnership.canUse(sp,target))deniedTarget=true;
  if(deniedTarget||(WeaponOwnership.boundType(cursor)&&!WeaponOwnership.owns(p,cursor))){ci.cancel();broadcastFullState();return;}
  // Keep bound artifacts out of foreign containers, including swap and quick-move transactions.
  if(WeaponOwnership.boundType(cursor)&&slot>=0&&isValidSlotIndex(slot)&&!(getSlot(slot).container instanceof Inventory)){ci.cancel();broadcastFullState();}
 }
}
