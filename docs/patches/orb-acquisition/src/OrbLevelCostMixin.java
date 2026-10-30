package net.skill_tree_rpgs.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.skill_tree_rpgs.items.RespecItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Keep the native use and Iourus skill/race redirect; charge only successful server uses. */
@Mixin(value=RespecItem.class,remap=false)
public class OrbLevelCostMixin {
 @Inject(method="use",at=@At("HEAD"),cancellable=true,require=1)
 private void orb$requireLevels(Level level,Player player,InteractionHand hand,CallbackInfoReturnable<InteractionResult> cir){
  if(player instanceof ServerPlayer&&!player.getAbilities().instabuild&&player.experienceLevel<30)cir.setReturnValue(InteractionResult.FAIL);
 }
 @Inject(method="use",at=@At("RETURN"),require=1)
 private void orb$chargeSuccessfulReset(Level level,Player player,InteractionHand hand,CallbackInfoReturnable<InteractionResult> cir){
  if(player instanceof ServerPlayer&&!player.getAbilities().instabuild&&cir.getReturnValue().consumesAction())player.giveExperienceLevels(-30);
 }
}
