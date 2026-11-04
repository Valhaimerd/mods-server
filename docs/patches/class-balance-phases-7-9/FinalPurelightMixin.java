package net.spell_engine.mixin.finale;
import net.minecraft.world.entity.LivingEntity;import net.minecraft.world.damagesource.DamageSource;import net.spell_engine.internals.FinalPurelight;
import org.spongepowered.asm.mixin.Mixin;import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
@Mixin(LivingEntity.class) public abstract class FinalPurelightMixin {
 @WrapMethod(method="checkTotemDeathProtection") private boolean purelight(DamageSource source,Operation<Boolean> original){return FinalPurelight.rescue(original.call(source),(LivingEntity)(Object)this,source);}
}
