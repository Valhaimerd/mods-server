package net.skill_tree_rpgs.mixin;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface PaladinEquipmentAccess {
    @Invoker("detectEquipmentUpdates") void paladin$detectEquipmentUpdates();
}
