package net.bandit.many_bows.mixin;
import net.bandit.many_bows.item.ModBowItem;
import net.bandit.many_bows.pack.BowRangedBridge;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.rpg_foundation.ranged_weapon.internal.ArrowExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModBowItem.class)
public class PackRangedDamageMixin {
    @Inject(method="applyBowDamageAttribute",at=@At("TAIL"))
    private void pack$preventDuplicateScaling(AbstractArrow arrow,Player player,CallbackInfo callback){
        String bow=BuiltInRegistries.ITEM.getKey((ModBowItem)(Object)this).getPath();
        if(BowRangedBridge.alreadyScales(bow,BuiltInRegistries.ENTITY_TYPE.getKey(arrow.getType()).getNamespace()))
            ((ArrowExtension)arrow).rwa_markModified(true);
    }
}
