package local.crossarms.mixin;
import local.crossarms.runtime.*;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(CrossbowItem.class)
public class FrenzyUseMixin {
 @Inject(method="use",at=@At("HEAD"),cancellable=true)
 private void crossarms$predictBurst(Level level,Player player,InteractionHand hand,CallbackInfoReturnable<InteractionResult> cir){
  var stack=player.getItemInHand(hand);if(!FrenzyState.applies(player,stack))return;
  if(FrenzyReliability.awaitingAutoFeed(player,stack)){if(player.isUsingItem())player.stopUsingItem();FrenzyReliability.sync(player);cir.setReturnValue(InteractionResult.CONSUME);return;}
  if(FrenzyReliability.state(player,stack)==2&&(CrossbowItem.isCharged(stack)||FrenzyReliability.saved(stack))){if(!FrenzyReliability.normalLoaded(stack)){cir.setReturnValue(InteractionResult.FAIL);return;}FrenzyReliability.parkLoaded(player,stack);player.startUsingItem(hand);FrenzyReliability.sync(player);cir.setReturnValue(InteractionResult.CONSUME);}
 }
}
