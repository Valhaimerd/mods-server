package com.emandev.bifrost.mixin;
import com.emandev.bifrost.WeaponOwnership;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(HopperBlockEntity.class)
public abstract class OwnershipHopperMixin {
 @Inject(method="addItem(Lnet/minecraft/world/Container;Lnet/minecraft/world/entity/item/ItemEntity;)Z",at=@At("HEAD"),cancellable=true)
 private static void pickup(Container container,ItemEntity item,CallbackInfoReturnable<Boolean> cir){if(WeaponOwnership.boundType(item.getItem()))cir.setReturnValue(false);}
}
