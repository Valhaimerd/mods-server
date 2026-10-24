package com.emandev.bifrost.mixin;
import com.emandev.bifrost.WeaponOwnership;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
/** Keep an owner's bound weapon in their inventory while grave capture downs them. */
@Pseudo
@Mixin(targets="de.mrstilu2k6.better_revive.event.ModEvents",remap=false)
public abstract class OwnershipReviveInventoryMixin {
 @Redirect(method={"snapshotPlayerAndCreateGrave","snapshotPlayerAndScheduleExplosionGrave"},
  at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/player/Inventory;setItem(ILnet/minecraft/world/item/ItemStack;)V"),require=0)
 private static void retain(Inventory inventory,int slot,ItemStack replacement){
  ItemStack current=inventory.getItem(slot);
  if(replacement.isEmpty()&&WeaponOwnership.boundType(current)&&!WeaponOwnership.id(current).isEmpty())return;
  inventory.setItem(slot,replacement);
 }
}
