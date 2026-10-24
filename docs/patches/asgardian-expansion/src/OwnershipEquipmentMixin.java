package com.emandev.bifrost.mixin;
import com.emandev.bifrost.WeaponOwnership;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(LivingEntity.class)
public abstract class OwnershipEquipmentMixin {
 @Inject(method="setItemSlot(Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/item/ItemStack;Z)V",at=@At("HEAD"),cancellable=true)
 private void equip(EquipmentSlot slot,ItemStack stack,boolean silent,CallbackInfo ci){
  LivingEntity e=(LivingEntity)(Object)this;if(e.level().isClientSide()||!WeaponOwnership.boundType(stack))return;
  if(!(e instanceof Player p)||!WeaponOwnership.insertionAllowed(p,stack))ci.cancel();
 }
}
