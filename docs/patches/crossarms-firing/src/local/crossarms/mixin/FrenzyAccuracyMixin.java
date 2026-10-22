package local.crossarms.mixin;
import java.util.List;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import local.crossarms.runtime.FrenzyState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Spread follows displayed heat; one firing action counts once, not once per arrow. */
@Mixin(ProjectileWeaponItem.class)
public class FrenzyAccuracyMixin {
    @ModifyVariable(method="shoot",at=@At("HEAD"),argsOnly=true,ordinal=1)
    private float crossarms$tightSpread(float inaccuracy,ServerLevel level,LivingEntity shooter,InteractionHand hand,ItemStack stack,List<ItemStack> arrows,float speed,float ignored,boolean critical,LivingEntity target){
        return FrenzyState.applies(shooter,stack)?inaccuracy*FrenzyState.spread(shooter):inaccuracy;
    }
    @Inject(method="shoot",at=@At("HEAD"))
    private void crossarms$rememberShot(ServerLevel level,LivingEntity shooter,InteractionHand hand,ItemStack stack,List<ItemStack> arrows,float speed,float inaccuracy,boolean critical,LivingEntity target,CallbackInfo ci,@Share("crossarms$frenzyShot") LocalBooleanRef frenzy){
        frenzy.set(FrenzyState.applies(shooter,stack)&&arrows.stream().anyMatch(arrow->!arrow.isEmpty()));
    }
    @Inject(method="shoot",at=@At("RETURN"))
    private void crossarms$countShot(ServerLevel level,LivingEntity shooter,InteractionHand hand,ItemStack stack,List<ItemStack> arrows,float speed,float inaccuracy,boolean critical,LivingEntity target,CallbackInfo ci,@Share("crossarms$frenzyShot") LocalBooleanRef frenzy){
        if(frenzy.get()) FrenzyState.fired(shooter,hand);
    }
}
