package sophisticated.building.pack;
import net.minecraft.world.entity.player.Player;
/** Axis selection is unrestricted; native batch, start reach and material checks remain. */
public final class AxisPolicy {
 private AxisPolicy(){}
 public static int maximumDistance(int mode,double start,int axis,int modifier){
  if(axis==Integer.MAX_VALUE)return Integer.MAX_VALUE;
  return (int)Math.min(Integer.MAX_VALUE,Math.ceil(Math.max(mode,start))+(long)axis+modifier+3);
 }
 public static String label(Player player,boolean next){return "Unlimited";}
 public static String label(int limit){return limit==Integer.MAX_VALUE?"Unlimited":Integer.toString(limit);}
}
