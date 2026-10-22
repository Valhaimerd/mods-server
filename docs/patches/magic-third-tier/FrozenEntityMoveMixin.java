package net.spell_engine.mixin.thirdtier;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Frozen holds even entities whose custom AI ignores the movement-speed attribute. */
@Mixin(Entity.class)
public abstract class FrozenEntityMoveMixin {
    @ModifyVariable(method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V",
        at = @At("HEAD"), argsOnly = true)
    private Vec3 thirdTierFrozenMovement(Vec3 movement) {
        if ((Object)this instanceof LivingEntity living) {
            var stun = BuiltInRegistries.MOB_EFFECT.get(Identifier.parse("spell_engine:stun"));
            if (stun.isPresent() && living.hasEffect(stun.get())) return Vec3.ZERO;
            var shock = BuiltInRegistries.MOB_EFFECT.get(Identifier.parse("rogues:shock"));
            if (shock.isPresent() && living.hasEffect(shock.get())) return Vec3.ZERO;
            var trap = BuiltInRegistries.MOB_EFFECT.get(Identifier.parse("rogues:bear_trap"));
            if (trap.isPresent() && living.hasEffect(trap.get())) return Vec3.ZERO;
            var grasp = BuiltInRegistries.MOB_EFFECT.get(Identifier.parse("skill_tree_rpgs:natures_grasp"));
            if (grasp.isPresent() && living.hasEffect(grasp.get())) return Vec3.ZERO;
            var frozen = BuiltInRegistries.MOB_EFFECT.get(Identifier.parse("wizards:frozen"));
            if (frozen.isPresent() && living.hasEffect(frozen.get())) return Vec3.ZERO;
        }
        return movement;
    }
}
