package net.spell_engine.mixin.warrior;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.spell_engine.entity.WarriorTierTwo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(LivingEntity.class)
public abstract class WarriorFreedomMixin {
 @Inject(method="canBeAffected",at=@At("HEAD"),cancellable=true)
 private void warriorControlImmunity(MobEffectInstance effect,CallbackInfoReturnable<Boolean> cir){if(WarriorTierTwo.deny((LivingEntity)(Object)this,effect.getEffect()))cir.setReturnValue(false);}
 @Inject(method="tick",at=@At("HEAD"))
 private void warriorFreedomTick(CallbackInfo ci){WarriorTierTwo.cleanse((LivingEntity)(Object)this);}
}
