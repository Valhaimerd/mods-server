package net.valhaimerd.orboblivioncompat.mixin;
import net.valhaimerd.orboblivioncompat.Utilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
@Mixin(ExperienceOrb.class)
public class OrbXpMixin {
 @Redirect(method="playerTouch",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/player/Player;giveExperiencePoints(I)V"))
 private void award(Player p,int amount){p.giveExperiencePoints(p instanceof ServerPlayer sp?Utilities.xp(sp,amount):amount);}
}
