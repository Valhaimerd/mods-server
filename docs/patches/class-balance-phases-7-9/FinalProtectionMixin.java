package net.spell_engine.mixin.finale;
import net.minecraft.world.entity.LivingEntity;import net.minecraft.world.damagesource.DamageSource;import net.minecraft.server.level.ServerLevel;import net.spell_engine.api.effect.Protection;import net.spell_engine.internals.FinalDefenses;
import org.spongepowered.asm.mixin.Mixin;import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
@Mixin(value=Protection.class,remap=false) public class FinalProtectionMixin {
 @WrapMethod(method="tryProtect") private static boolean window(LivingEntity target,ServerLevel level,DamageSource source,Operation<Boolean> original){return FinalDefenses.block(target,source)||original.call(target,level,source);}
}
