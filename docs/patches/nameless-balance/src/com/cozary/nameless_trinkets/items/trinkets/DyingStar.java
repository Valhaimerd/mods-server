package com.cozary.nameless_trinkets.items.trinkets;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
public class DyingStar extends DyingStarBase implements ICurioItem {
 @Override public boolean canEquip(SlotContext context,ItemStack stack) { return false; }
 @Override public boolean canEquipFromUse(SlotContext context,ItemStack stack) { return false; }
 @Override public boolean canUnequip(SlotContext context,ItemStack stack) { return true; }
 @Override public void onUnequip(SlotContext context,ItemStack previous,ItemStack stack) { cleanup(context); }
 @Override public void curioTick(SlotContext context,ItemStack stack) { cleanup(context); }
 private void cleanup(SlotContext context) {
  if(context.entity().level().isClientSide()) return;

  for(var entry: net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.entrySet()) {
   var attribute=context.entity().getAttribute(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.wrapAsHolder(entry.getValue()));
   if(attribute==null) continue;
   for(var modifier:java.util.List.copyOf(attribute.getModifiers()))
    if(modifier.id().getNamespace().equals("nameless_trinkets") && modifier.id().getPath().startsWith("dying_star_")) attribute.removeModifier(modifier.id());
  }
 }
}
