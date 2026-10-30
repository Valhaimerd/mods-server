package net.valhaimerd.orboblivioncompat.mixin;
import net.valhaimerd.orboblivioncompat.Utilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
@Mixin(PotionContents.class)
public class DrinkEffectMixin {
 @Redirect(method="lambda$applyToLivingEntity$0",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z"))
 private static boolean payload(LivingEntity entity,MobEffectInstance effect){return entity.addEffect(Utilities.extend(entity,effect));}
}
