package net.rogues.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.rogues.effect.RogueEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=ItemInHandRenderer.class, priority=2000)
public abstract class StealthHandsMixin {
    // Run before Punchy's HEAD callback, which can submit its own hand models.
    @Inject(method="submitHandsWithItems", at=@At("HEAD"), cancellable=true, order=500)
    private void rogues$hideStealthHands(float partialTick, PoseStack pose,
            SubmitNodeCollector collector, LocalPlayer player, int light, CallbackInfo ci) {
        if (player.hasEffect(RogueEffects.STEALTH.entry)) ci.cancel();
    }
}
