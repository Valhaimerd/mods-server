package net.bettercombat.logic;
import net.minecraft.world.entity.player.Player;
import net.bettercombat.BetterCombatMod;
/** Match the already-registered client targeting extension, without another attribute modifier. */
public final class ScaleReach {
 public static double apply(double weaponRange,Player player){return weaponRange*BetterCombatMod.config.getAttackRangeMultiplierForScale(player.getScale());}
}
