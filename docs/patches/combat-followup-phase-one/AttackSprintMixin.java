package net.spell_engine.mixin.warrior;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.spell_engine.entity.ClassRework;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

/** Limit sprint-cancellation suppression to the dynamic scope of a melee attack. */
@Mixin(LivingEntity.class)
public class AttackSprintMixin {
 @Inject(method="setSprinting",at=@At("HEAD"),cancellable=true)
 private void classPack$sprint(boolean sprint,CallbackInfo ci){if(!sprint&&ClassRework.suppressSprint((LivingEntity)(Object)this))ci.cancel();}
}
