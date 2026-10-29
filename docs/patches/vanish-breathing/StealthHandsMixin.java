package net.rogues.mixin.client;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.*;
import net.minecraft.client.renderer.*;
import net.rogues.client.VanishVisuals;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(value=ItemInHandRenderer.class,priority=2000)
public class StealthHandsMixin {
    // The collector is replaced before Punchy's HEAD callback, so its own hands also fade.
    @ModifyVariable(method="submitHandsWithItems",at=@At("HEAD"),argsOnly=true,order=400)
    private SubmitNodeCollector vanish$hands(SubmitNodeCollector collector,float tick,PoseStack pose){
        var player=Minecraft.getInstance().player;
        return player==null?collector:VanishVisuals.wrap(collector,VanishVisuals.alpha(player,tick));
    }
    @Redirect(method="submitArmWithItem",at=@At(value="INVOKE",target="Lnet/minecraft/client/player/AbstractClientPlayer;isInvisible()Z"))
    private boolean vanish$armVisible(AbstractClientPlayer player){
        return VanishVisuals.alpha(player,0)<0 && player.isInvisible();
    }
}
