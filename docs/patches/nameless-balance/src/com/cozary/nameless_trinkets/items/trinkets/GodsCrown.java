package com.cozary.nameless_trinkets.items.trinkets;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
public class GodsCrown extends GodsCrownBase implements ICurioItem {
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
   attribute.removeModifier(net.minecraft.resources.Identifier.parse("nameless_trinkets:gods_crown_armor"));
   attribute.removeModifier(net.minecraft.resources.Identifier.parse("nameless_trinkets:gods_crown_armor_thougness"));
   attribute.removeModifier(net.minecraft.resources.Identifier.parse("nameless_trinkets:gods_crown_attack_damage"));
   attribute.removeModifier(net.minecraft.resources.Identifier.parse("nameless_trinkets:gods_crown_attack_knockback"));
   attribute.removeModifier(net.minecraft.resources.Identifier.parse("nameless_trinkets:gods_crown_attack_speed"));
   attribute.removeModifier(net.minecraft.resources.Identifier.parse("nameless_trinkets:gods_crown_block_reach"));
   attribute.removeModifier(net.minecraft.resources.Identifier.parse("nameless_trinkets:gods_crown_entity_reach"));
   attribute.removeModifier(net.minecraft.resources.Identifier.parse("nameless_trinkets:gods_crown_flying_speed"));
   attribute.removeModifier(net.minecraft.resources.Identifier.parse("nameless_trinkets:gods_crown_knockback_resistance"));
   attribute.removeModifier(net.minecraft.resources.Identifier.parse("nameless_trinkets:gods_crown_luck"));
   attribute.removeModifier(net.minecraft.resources.Identifier.parse("nameless_trinkets:gods_crown_max_health"));
   attribute.removeModifier(net.minecraft.resources.Identifier.parse("nameless_trinkets:gods_crown_movement_speed"));
   attribute.removeModifier(net.minecraft.resources.Identifier.parse("nameless_trinkets:gods_crown_step_height_addition"));
   attribute.removeModifier(net.minecraft.resources.Identifier.parse("nameless_trinkets:gods_crown_swim_speed"));
  }
 }
}
