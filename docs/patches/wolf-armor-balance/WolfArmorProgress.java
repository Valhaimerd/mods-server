package archives.tater.penchant.util;
import archives.tater.penchant.component.EnchantmentProgress;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
public final class WolfArmorProgress {
 public static boolean eligible(LivingEntity entity){return entity instanceof ServerPlayer||entity instanceof Wolf wolf&&wolf.getOwner() instanceof ServerPlayer;}
 public static void damage(ItemStack stack,LivingEntity entity){
  if(entity instanceof ServerPlayer){EnchantmentProgress.onDurabilityDamage(stack,entity);return;}
  if(entity instanceof Wolf wolf&&wolf.getOwner() instanceof ServerPlayer owner){
   var id=BuiltInRegistries.ITEM.getKey(stack.getItem());
   if(id.toString().equals("minecraft:wolf_armor")||id.getNamespace().equals("ironwolfarmor")&&id.getPath().endsWith("_wolf_armor"))
    EnchantmentProgress.onDurabilityDamage(stack,owner);
  }
 }
}
