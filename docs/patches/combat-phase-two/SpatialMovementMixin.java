package net.spell_engine.mixin.entity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.spell_engine.internals.PurgatoryBoundary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(Entity.class)
public class SpatialMovementMixin {
 @ModifyVariable(method="move",at=@At("HEAD"),argsOnly=true)
 private Vec3 pack$purgatoryBoundary(Vec3 movement){return PurgatoryBoundary.clip((Entity)(Object)this,movement);}
}
