package com.cozary.nameless_trinkets.items.trinkets;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
public class EtherealWings extends EtherealWingsBase implements ICurioItem {
 @Override public boolean canEquip(SlotContext context,ItemStack stack) { return false; }
 @Override public boolean canEquipFromUse(SlotContext context,ItemStack stack) { return false; }
 @Override public boolean canUnequip(SlotContext context,ItemStack stack) { return true; }
 @Override public void onUnequip(SlotContext context,ItemStack previous,ItemStack stack) { cleanup(context); }
 @Override public void curioTick(SlotContext context,ItemStack stack) { cleanup(context); }
 private void cleanup(SlotContext context) {
  if(context.entity().level().isClientSide()) return;

  if(context.entity() instanceof net.minecraft.world.entity.player.Player player && !player.isCreative() && !player.isSpectator()) {
   player.getAbilities().flying=false;player.getAbilities().mayfly=false;
   player.getAbilities().setFlyingSpeed(0.05F);player.onUpdateAbilities();
  }
 }
}
