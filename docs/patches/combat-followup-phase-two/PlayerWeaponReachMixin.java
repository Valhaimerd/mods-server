package net.bettercombat.mixin.player;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.bettercombat.logic.WeaponReachValidation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Player.class)
public class PlayerWeaponReachMixin {
 @Inject(method="isWithinAttackRange",at=@At("HEAD"),cancellable=true)
 private void pack$weaponRange(ItemStack weapon,AABB target,double buffer,CallbackInfoReturnable<Boolean> cir){if(WeaponReachValidation.handles(weapon))cir.setReturnValue(WeaponReachValidation.within((Player)(Object)this,weapon,target,buffer));}
}
