package com.eiden.iourusraces.race;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
public final class RaceVisualRules {
 public static boolean leaves(BlockState state){return state.is(BlockTags.LEAVES)||state.getBlock() instanceof LeavesBlock;}
 public static boolean solidLeaves(Race race){return race==Race.ELF;}
 public static boolean nightVision(Race race,boolean water){return race==Race.GOBLIN||(race==Race.MELFORK&&water);}
}
