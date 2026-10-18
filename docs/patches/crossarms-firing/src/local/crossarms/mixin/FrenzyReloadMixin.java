package local.crossarms.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import local.crossarms.runtime.FrenzyState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CrossbowItem.class)
public class FrenzyReloadMixin {
    // Guard before vanilla clears loaded ammunition; a preloaded spare cannot bypass the lock.
    @Inject(method="performShooting",at=@At("HEAD"),cancellable=true)
    private void crossarms$requireCrank(Level level,LivingEntity shooter,InteractionHand hand,ItemStack stack,float power,float spread,LivingEntity target,CallbackInfo ci){
        if(FrenzyState.applies(shooter,stack)&&FrenzyState.empty(shooter)){FrenzyState.notice(shooter);ci.cancel();}
    }
    @WrapOperation(method="onUseTick",at=@At(value="INVOKE",target="Lnet/minecraft/world/item/CrossbowItem;tryLoadProjectiles(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean crossarms$completeCrank(LivingEntity shooter,ItemStack stack,Operation<Boolean> original){
        if(FrenzyState.applies(shooter,stack)&&!shooter.getProjectile(stack).is(Items.ARROW)) return false;
        boolean loaded=original.call(shooter,stack);
        if(loaded&&FrenzyState.applies(shooter,stack)&&FrenzyState.empty(shooter)) FrenzyState.cranked(shooter);
        return loaded;
    }
}
