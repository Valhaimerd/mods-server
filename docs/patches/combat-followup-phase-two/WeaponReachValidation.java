package net.bettercombat.logic;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.*;
import net.bettercombat.logic.WeaponRegistry;

/** Native attack validation must use Better Combat weapon reach and its scaled shoulder origin. */
public final class WeaponReachValidation {
 public static boolean handles(ItemStack weapon){return WeaponRegistry.getAttributes(weapon)!=null&&!weapon.has(net.minecraft.core.component.DataComponents.PIERCING_WEAPON);}
 public static double range(Player player,ItemStack weapon){return ScaleReach.apply(PlayerAttackHelper.getRangeForItem(player,weapon),player);}
 public static Vec3 origin(Player player){return player.getEyePosition().subtract(0,player.getBbHeight()*.15*player.getAgeScale(),0);}
 public static boolean within(Player player,ItemStack weapon,AABB target,double buffer){double limit=range(player,weapon)+buffer;return limit>=0&&target.distanceToSqr(origin(player))<=limit*limit;}
}
