package com.eiden.iourusraces.race;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
public final class RaceVisualRules {
 public static boolean leaves(BlockState state){return state.is(BlockTags.LEAVES)||state.getBlock() instanceof LeavesBlock;}
 public static boolean solidLeaves(Race race){return true;}
 public static boolean leafClimbing(net.minecraft.world.entity.player.Player player){
  Race race=PlayerRaceData.getRace(player);
  if((race!=Race.ELF&&race!=Race.GOBLIN)||!player.horizontalCollision||player.isSpectator()||player.isPassenger())return false;
  var b=player.getBoundingBox();double e=.03;
  return leafFace(player,b.minX-e,b.minY+.001,b.minZ+.001,b.minX-.001,b.maxY-.001,b.maxZ-.001)
      ||leafFace(player,b.maxX+.001,b.minY+.001,b.minZ+.001,b.maxX+e,b.maxY-.001,b.maxZ-.001)
      ||leafFace(player,b.minX+.001,b.minY+.001,b.minZ-e,b.maxX-.001,b.maxY-.001,b.minZ-.001)
      ||leafFace(player,b.minX+.001,b.minY+.001,b.maxZ+.001,b.maxX-.001,b.maxY-.001,b.maxZ+e);
 }
 private static boolean leafFace(net.minecraft.world.entity.player.Player player,double x1,double y1,double z1,double x2,double y2,double z2){
  for(var pos:net.minecraft.core.BlockPos.betweenClosed(net.minecraft.util.Mth.floor(x1),net.minecraft.util.Mth.floor(y1),net.minecraft.util.Mth.floor(z1),net.minecraft.util.Mth.floor(x2),net.minecraft.util.Mth.floor(y2),net.minecraft.util.Mth.floor(z2)))
   if(leaves(player.level().getBlockState(pos)))return true;
  return false;
 }
 public static boolean nightVision(Race race,boolean water){return race==Race.GOBLIN||(race==Race.MELFORK&&water);}
}
