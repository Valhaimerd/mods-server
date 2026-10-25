package net.spell_engine.mixin.client.render.priest;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.spell_engine.internals.PriestPassives;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=ItemInHandRenderer.class,priority=2000)
public abstract class FadeHandsMixin {
 @Inject(method="submitHandsWithItems",at=@At("HEAD"),cancellable=true,order=500)
 private void hide(float partial,PoseStack pose,SubmitNodeCollector collector,LocalPlayer player,int light,CallbackInfo ci){if(PriestPassives.faded(player))ci.cancel();}
}
