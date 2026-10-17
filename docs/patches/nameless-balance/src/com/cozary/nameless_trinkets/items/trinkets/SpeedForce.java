package com.cozary.nameless_trinkets.items.trinkets;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
public class SpeedForce extends SpeedForceBase implements ICurioItem {
 @Override public boolean canEquip(SlotContext context,ItemStack stack) { return false; }
 @Override public boolean canEquipFromUse(SlotContext context,ItemStack stack) { return false; }
 @Override public boolean canUnequip(SlotContext context,ItemStack stack) { return true; }
 @Override public void onUnequip(SlotContext context,ItemStack previous,ItemStack stack) { cleanup(context); }
 @Override public void curioTick(SlotContext context,ItemStack stack) { cleanup(context); }
 private void cleanup(SlotContext context) {
  if(context.entity().level().isClientSide()) return;

  context.entity().getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED).removeModifier(net.minecraft.resources.Identifier.parse("nameless_trinkets:speed_force_movement_speed"));
 }
}
