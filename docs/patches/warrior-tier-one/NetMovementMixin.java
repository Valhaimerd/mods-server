package net.spell_engine.mixin.warrior;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.spell_engine.entity.WarriorRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(Entity.class)
public abstract class NetMovementMixin {
 @ModifyVariable(method="move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V",at=@At("HEAD"),argsOnly=true)
 private Vec3 warriorNetMovement(Vec3 movement){return WarriorRules.movement((Entity)(Object)this,movement);}
}
