package net.valhaimerd.orboblivioncompat.mixin;
import net.valhaimerd.orboblivioncompat.Utilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import net.minecraft.world.entity.player.Player;
@Mixin(Player.class)
public class EnchantMixin {
 @Redirect(method="onEnchantmentPerformed",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/player/Player;giveExperienceLevels(I)V"))
 private void pay(Player p,int levels){Utilities.payEnchant(p,levels);}
}
