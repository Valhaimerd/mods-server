package net.valhaimerd.orboblivioncompat.mixin;
import net.valhaimerd.orboblivioncompat.Utilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
@Mixin(PowderSnowBlock.class)
public class SnowMixin {
 @Inject(method="canEntityWalkOnPowderSnow",at=@At("HEAD"),cancellable=true)
 private static void walk(Entity e,CallbackInfoReturnable<Boolean> cir){if(e instanceof Player p&&p.isAlive()&&p.hasEffect(Utilities.SNOW_GUARD))cir.setReturnValue(true);}
}
