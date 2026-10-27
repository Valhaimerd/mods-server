package net.skill_tree_rpgs.mixin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.skill_tree_rpgs.compat.PaladinDoctrine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PaladinPlayerMixin {
    @Inject(method="tick",at=@At("HEAD"))
    private void paladin$tick(CallbackInfo ci) {
        if((Object)this instanceof ServerPlayer p)PaladinDoctrine.enforce(p);
    }
}
