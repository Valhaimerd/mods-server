package com.cozary.nameless_trinkets.items.trinkets;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
public class BrokenMagnet extends BrokenMagnetBase implements ICurioItem {
 @Override public boolean canEquip(SlotContext context,ItemStack stack) { return false; }
 @Override public boolean canEquipFromUse(SlotContext context,ItemStack stack) { return false; }
 @Override public boolean canUnequip(SlotContext context,ItemStack stack) { return true; }
}
