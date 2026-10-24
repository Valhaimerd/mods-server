package com.emandev.bifrost.mixin;
import com.emandev.bifrost.WeaponOwnership;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(CraftingMenu.class)
public abstract class OwnershipCraftMixin {
 @Inject(method="slotChangedCraftingGrid",at=@At("TAIL"))
 private static void bind(AbstractContainerMenu menu,ServerLevel level,Player p,CraftingContainer grid,ResultContainer result,RecipeHolder<CraftingRecipe> recipe,CallbackInfo ci){
  if(p instanceof ServerPlayer sp&&!WeaponOwnership.prepareCraft(sp,result.getItem(0)))result.setItem(0,ItemStack.EMPTY);
 }
}
