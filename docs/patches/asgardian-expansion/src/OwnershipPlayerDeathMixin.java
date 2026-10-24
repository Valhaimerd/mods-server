package com.emandev.bifrost.mixin;
import com.emandev.bifrost.WeaponOwnership;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(ServerPlayer.class)
public abstract class OwnershipPlayerDeathMixin {
 // The first getGameRules is reached only after CommonHooks.onLivingDeath returns false.
 @Inject(method="die",at=@At(value="INVOKE",target="Lnet/minecraft/server/level/ServerLevel;getGameRules()Lnet/minecraft/world/level/gamerules/GameRules;",ordinal=0))
 private void death(DamageSource source,CallbackInfo ci){WeaponOwnership.finalDeath((ServerPlayer)(Object)this);}
}
