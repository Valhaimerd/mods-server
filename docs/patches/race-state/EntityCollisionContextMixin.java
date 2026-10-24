package com.eiden.iourusraces.mixin;
import com.eiden.iourusraces.race.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(EntityCollisionContext.class)
public abstract class EntityCollisionContextMixin {
 @Inject(method="getCollisionShape",at=@At("HEAD"),cancellable=true)
 private void raceLeaves(BlockState state,CollisionGetter world,BlockPos pos,CallbackInfoReturnable<VoxelShape> cir){
  if(!RaceVisualRules.leaves(state))return;
  var entity=((EntityCollisionContext)(Object)this).getEntity();
  if(entity instanceof Player p)cir.setReturnValue(RaceVisualRules.solidLeaves(PlayerRaceData.getRace(p))?Shapes.block():Shapes.empty());
 }
}
