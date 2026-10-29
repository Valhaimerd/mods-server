package net.rogues.mixin.client;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(targets="net.minecraft.client.renderer.rendertype.RenderSetup$TextureBinding")
public interface VanishTextureAccess {
    @Accessor("location") Identifier vanish$location();
}
