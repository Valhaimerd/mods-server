package local.goldcapture;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.entity.Entity;
import net.spell_engine.api.spell.fx.ParticleGroup;
import net.spell_engine.fx.ParticleHelper;
@Mixin(ParticleHelper.class)
public class CaptureMixin {
 @Inject(method="sendBatches(Lnet/minecraft/world/entity/Entity;Ljava/util/List;)V",at=@At("HEAD"))
 private static void packets(Entity e,List<ParticleGroup> groups,CallbackInfo ci){local.paladinprobe.ShoutProbe.capture(e,groups);}
}
