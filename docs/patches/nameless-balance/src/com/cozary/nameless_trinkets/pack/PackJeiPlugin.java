package com.cozary.nameless_trinkets.pack;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/** Optional JEI integration; this class is discovered by JEI only on clients. */
@JeiPlugin
public final class PackJeiPlugin implements IModPlugin {
    @Override public Identifier getPluginUid() {
        return Identifier.parse("nameless_trinkets:pack_removed_items");
    }

    @Override public void onRuntimeAvailable(IJeiRuntime runtime) {
        var removed = BuiltInRegistries.ITEM.entrySet().stream()
            .filter(entry -> entry.getKey().identifier().getNamespace().equals("nameless_trinkets"))
            .filter(entry -> PackBalance.removed(entry.getKey().identifier().getPath()))
            .map(entry -> new ItemStack(entry.getValue())).toList();
        runtime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, removed);
    }
}
