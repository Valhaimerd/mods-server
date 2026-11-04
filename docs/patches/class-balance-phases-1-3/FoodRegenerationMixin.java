package net.spell_engine.mixin.batch;
import net.minecraft.world.food.FoodData;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(FoodData.class)
public class FoodRegenerationMixin {
 @Redirect(method="tick",at=@At(value="INVOKE",target="Lnet/minecraft/server/level/ServerPlayer;heal(F)V"))
 private void blocked(ServerPlayer player,float amount){if(!net.spell_engine.internals.BatchRules.regenerationBlocked(player))player.heal(amount);}
}
