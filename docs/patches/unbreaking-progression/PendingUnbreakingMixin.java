package archives.tater.penchant.mixin.leveling;
import archives.tater.penchant.component.EnchantmentProgress;
import archives.tater.penchant.registry.PenchantComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Reconcile already-earned progress after saved thresholds/caps change; award nothing. */
@Mixin(ItemStack.class)
public class PendingUnbreakingMixin {
 @Inject(method="inventoryTick",at=@At("HEAD"))
 private void penchant$pendingUnbreaking(Level world,Entity entity,EquipmentSlot slot,CallbackInfo ci){
  if(world.isClientSide()||!(entity instanceof ServerPlayer player)||player.tickCount%20!=0)return;
  var stack=(ItemStack)(Object)this;var pending=stack.get(PenchantComponents.ENCHANTMENT_PROGRESS);if(pending==null)return;
  var enchantment=world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.UNBREAKING);
  int level=stack.getEnchantments().getLevel(enchantment);if(level<1||level>=10)return;
  if(pending.getProgress(enchantment)>=EnchantmentProgress.getMaxProgress(enchantment,level,stack))
   EnchantmentProgress.addToProgress(stack,0,player);
 }
}
