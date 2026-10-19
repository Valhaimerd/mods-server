package local.worldbosses.mixin;

import local.worldbosses.RaidRuntime;
import local.worldbosses.GearRetirement;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class BossEffectMixin {
    @ModifyVariable(method="addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",at=@At("HEAD"),argsOnly=true,ordinal=0)
    private MobEffectInstance raidEffect(MobEffectInstance value,MobEffectInstance original,Entity source) {
        return RaidRuntime.effect(value,source,(LivingEntity)(Object)this);
    }
}
