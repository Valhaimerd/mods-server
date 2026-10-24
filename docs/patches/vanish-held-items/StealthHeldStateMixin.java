package net.rogues.mixin.client;

import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.rogues.effect.RogueEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmedEntityRenderState.class)
public abstract class StealthHeldStateMixin {
    @Inject(method="extractArmedEntityRenderState", at=@At("TAIL"))
    private static void rogues$hideStealthHeldItems(LivingEntity entity,
            ArmedEntityRenderState state, ItemModelResolver resolver, float partialTick, CallbackInfo ci) {
        if (!entity.hasEffect(RogueEffects.STEALTH.entry)) return;
        // Clear this frame's render snapshot only; equipped inventory stacks stay untouched.
        state.rightHandItemState.clear();
        state.leftHandItemState.clear();
        state.rightHandItemStack = ItemStack.EMPTY;
        state.leftHandItemStack = ItemStack.EMPTY;
    }
}
