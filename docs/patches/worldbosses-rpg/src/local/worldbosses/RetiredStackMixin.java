package local.worldbosses.mixin;

import local.worldbosses.RaidRuntime;
import local.worldbosses.GearRetirement;

import net.minecraft.core.Holder;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class RetiredStackMixin {
    @Shadow @Final @Mutable private Holder<Item> item;
    @Shadow @Final @Mutable private PatchedDataComponentMap components;
    @Inject(method="<init>(Lnet/minecraft/core/Holder;ILnet/minecraft/core/component/PatchedDataComponentMap;)V",at=@At("RETURN"))
    private void retireEquipment(CallbackInfo ci) {
        ItemStack original=(ItemStack)(Object)this;
        ItemStack replacement=GearRetirement.convert(original);
        if(replacement!=original) {
            item=replacement.typeHolder();
            components=((PatchedDataComponentMap)replacement.getComponents()).copy();
        }
    }
}
