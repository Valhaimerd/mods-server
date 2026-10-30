package net.valhaimerd.orboblivioncompat.mixin;
import net.valhaimerd.orboblivioncompat.Utilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
@Mixin(ApplyStatusEffectsConsumeEffect.class)
public class FoodEffectMixin {
 @Redirect(method="apply",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z"))
 private boolean payload(LivingEntity entity,MobEffectInstance effect){return entity.addEffect(Utilities.extend(entity,effect));}
}
