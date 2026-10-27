package net.skill_tree_rpgs.mixin;
import java.util.Map;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.skill_tree_rpgs.compat.PaladinDoctrine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class PaladinEquipmentMixin {
    @Inject(method="collectEquipmentChanges",at=@At("HEAD"))
    private void paladin$scan(Map<EquipmentSlot,ItemStack> previous,CallbackInfoReturnable<Map<EquipmentSlot,ItemStack>> cir) {
        if((Object)this instanceof ServerPlayer p)PaladinDoctrine.beforeEquipmentScan(p);
    }
}
