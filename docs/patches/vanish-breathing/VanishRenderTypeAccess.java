package net.rogues.mixin.client;
import net.minecraft.client.renderer.rendertype.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(RenderType.class)
public interface VanishRenderTypeAccess {
    @Accessor("state") RenderSetup vanish$setup();
}
