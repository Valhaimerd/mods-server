package net.spell_engine.mixin.warrior;
import net.minecraft.world.entity.player.Player;
import net.spell_engine.entity.WarriorUltimates;
import net.spell_engine.internals.casting.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(SpellCastInteractor.class)
public abstract class WarriorChannelMixin {
 @Shadow @Final private Player player;
 @Shadow private SpellCast.Process process;
 @Inject(method="setProcess",at=@At("HEAD"))
 private void warriorChangeProcess(SpellCast.Process next,CallbackInfo ci){WarriorUltimates.changeProcess(player,process,next);}
 @Inject(method="tick",at=@At("HEAD"),cancellable=true)
 private void warriorInterrupt(CallbackInfo ci){if(WarriorUltimates.interrupt(player,process))ci.cancel();}
}
