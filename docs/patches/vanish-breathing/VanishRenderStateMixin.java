package net.rogues.mixin.client;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.rogues.client.VanishState;
import org.spongepowered.asm.mixin.*;
@Mixin(LivingEntityRenderState.class)
public class VanishRenderStateMixin implements VanishState {
    @Unique private float vanish$alpha = -1;
    public float vanish$alpha(){return vanish$alpha;}
    public void vanish$alpha(float value){vanish$alpha=value;}
}
