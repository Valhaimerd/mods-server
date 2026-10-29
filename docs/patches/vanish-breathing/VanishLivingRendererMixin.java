package net.rogues.mixin.client;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.rogues.client.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(LivingEntityRenderer.class)
public class VanishLivingRendererMixin {
    @Inject(method="extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",at=@At("TAIL"))
    private void vanish$extract(LivingEntity entity,LivingEntityRenderState state,float partialTick,CallbackInfo ci){
        float alpha=state.appearsGlowing()?-1:VanishVisuals.alpha(entity,partialTick);
        ((VanishState)state).vanish$alpha(alpha);
        if(alpha>=0){state.isInvisible=false;state.isInvisibleToPlayer=false;}
    }
    @ModifyVariable(method="submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",at=@At("HEAD"),argsOnly=true)
    private SubmitNodeCollector vanish$collector(SubmitNodeCollector collector,LivingEntityRenderState state){
        return VanishVisuals.wrap(collector,((VanishState)state).vanish$alpha());
    }
}
