package com.emandev.bifrost;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;

public final class AsgardWeapons {
    private AsgardWeapons(){}
    public static String kind(ItemStack stack) {
        if(stack.isEmpty())return "";
        return switch(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()){
            case "bifrost:heimdalls_sword" -> "hofund";
            case "bifrost:thors_hammer" -> "mjolnir";
            case "bifrost:stormbreaker" -> "stormbreaker";
            default -> "";
        };
    }
    public static boolean ready(ServerPlayer player,ItemStack stack) {
        return !kind(stack).isEmpty() && AsgardianAccess.canUse(player) && WeaponOwnership.canUse(player,stack) && !WeaponOwnership.isBusy(player,stack);
    }
}
