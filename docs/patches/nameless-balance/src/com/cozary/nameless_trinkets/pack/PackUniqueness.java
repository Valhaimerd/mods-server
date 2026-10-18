package com.cozary.nameless_trinkets.pack;

import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;

/** One active equipped copy per item type; never moves or deletes existing stacks. */
public final class PackUniqueness {
    private PackUniqueness() {}
    // Read live stacks: Curios findCurios can cache a previous result within the tick.
    public static boolean canEquip(SlotContext context,ItemStack stack) {
        if(stack.isEmpty())return false;
        var inventory=CuriosApi.getCuriosInventory(context.entity()).orElse(null);if(inventory==null)return true;
        for(var entry:inventory.getCurios().entrySet()) {
            var slots=entry.getValue().getStacks();
            for(int i=0;i<slots.getSlots();i++)
                if(slots.getStackInSlot(i).is(stack.getItem()) && !(entry.getKey().equals(context.identifier()) && i==context.index() && !context.cosmetic()))return false;
        }
        return true;
    }
    public static boolean active(SlotContext context,ItemStack stack) {
        if(stack.isEmpty())return false;
        var inventory=CuriosApi.getCuriosInventory(context.entity()).orElse(null);if(inventory==null)return true;
        for(var entry:inventory.getCurios().entrySet()) {
            var slots=entry.getValue().getStacks();
            for(int i=0;i<slots.getSlots();i++)
                if(slots.getStackInSlot(i).is(stack.getItem()))return entry.getKey().equals(context.identifier()) && i==context.index() && !context.cosmetic();
        }
        return true;
    }
    public static boolean inactive(SlotContext context,ItemStack stack) { return !active(context,stack); }
    public static boolean unequip(SlotContext context,ItemStack stack) { return canEquip(context,stack); }
}
