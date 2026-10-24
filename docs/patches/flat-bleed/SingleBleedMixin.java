package net.spell_engine.mixin.flatbleed;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.*;
import net.spell_engine.api.effect.BleedStatusEffect;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Includes copied, network-decoded and saved legacy high-level instances. */
@Mixin(MobEffectInstance.class) public abstract class SingleBleedMixin {
 @Shadow @Final private Holder<MobEffect> effect;
 @Shadow private int amplifier;
 @Inject(method={"<init>(Lnet/minecraft/core/Holder;IIZZZLnet/minecraft/world/effect/MobEffectInstance;)V","<init>(Lnet/minecraft/world/effect/MobEffectInstance;)V"},at=@At("TAIL"))
 private void singleLevel(CallbackInfo ci){if(effect!=null&&effect.value() instanceof BleedStatusEffect)amplifier=0;}
}
