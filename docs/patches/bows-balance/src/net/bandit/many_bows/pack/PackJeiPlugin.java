package net.bandit.many_bows.pack;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

@JeiPlugin
public final class PackJeiPlugin implements IModPlugin {
    public Identifier getPluginUid(){return Identifier.parse("too_many_bows:pack_removed_accessories");}
    public void onRuntimeAvailable(IJeiRuntime runtime){
        var stacks=PackPolicy.REMOVED.stream().map(Identifier::parse).map(id->new ItemStack(BuiltInRegistries.ITEM.getValue(id))).toList();
        runtime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK,stacks);
    }
}
