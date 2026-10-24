package com.emandev.bifrost.mixin;
import com.emandev.bifrost.WeaponOwnership;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Explosion;
@Mixin(ItemEntity.class)
public abstract class OwnershipItemEntityMixin {
 @Inject(method="tick",at=@At("HEAD"),cancellable=true)
 private void tick(CallbackInfo ci){ItemEntity e=(ItemEntity)(Object)this;if(!e.level().isClientSide()&&WeaponOwnership.protectedVisual(e))ci.cancel();}
 @Inject(method="playerTouch",at=@At("HEAD"),cancellable=true)
 private void touch(Player p,CallbackInfo ci){ItemEntity e=(ItemEntity)(Object)this;if(WeaponOwnership.protectedVisual(e)){WeaponOwnership.touch(e,p);ci.cancel();}}
 @Inject(method="hurtServer",at=@At("HEAD"),cancellable=true)
 private void hurt(ServerLevel level,DamageSource source,float damage,CallbackInfoReturnable<Boolean> cir){if(WeaponOwnership.protectedVisual((ItemEntity)(Object)this))cir.setReturnValue(false);}
 @Inject(method="ignoreExplosion",at=@At("HEAD"),cancellable=true)
 private void explosion(Explosion explosion,CallbackInfoReturnable<Boolean> cir){if(WeaponOwnership.protectedVisual((ItemEntity)(Object)this))cir.setReturnValue(true);}
}
