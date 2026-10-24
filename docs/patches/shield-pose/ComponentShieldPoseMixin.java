package punchy.mixin.client;

import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemInHandRenderer.class)
public abstract class ComponentShieldPoseMixin {
    // The third getItem call is solely the native BLOCK branch's ShieldItem guard.
    // Use its shield transform for component shields; the real rendered stack/model is untouched.
    @Redirect(method="submitArmWithItem",at=@At(value="INVOKE",target="Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;",ordinal=2),require=1)
    private Item punchy$componentShieldPose(ItemStack stack){
        return stack.has(DataComponents.BLOCKS_ATTACKS)?Items.SHIELD:stack.getItem();
    }
}
