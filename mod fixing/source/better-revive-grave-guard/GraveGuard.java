package de.mrstilu2k6.better_revive.util;

import de.mrstilu2k6.better_revive.block.GraveBlockEntity;
import de.mrstilu2k6.better_revive.block.ModBlocks;
import de.mrstilu2k6.better_revive.event.ModEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import java.util.UUID;

/** Restores an active grave if a direct world edit removes its block. */
public final class GraveGuard {
    private GraveGuard() {}

    public static void onRemoved(GraveBlockEntity grave) {
        Level level = grave.getLevel();
        UUID id = grave.getGraveId();
        if (!(level instanceof ServerLevel serverLevel) || id == null || !GraveRegistry.isRegistered(id)) return;
        BlockState state = grave.getBlockState();
        ModEvents.queueDelayedTask(1, () -> {
            if (!GraveRegistry.isRegistered(id) || System.currentTimeMillis() >= grave.getEndTime()) return;
            if (serverLevel.getBlockState(grave.getBlockPos()).is(ModBlocks.GRAVE_BLOCK.get())) return;
            if (serverLevel.setBlock(grave.getBlockPos(), state, 3)) {
                grave.clearRemoved();
                serverLevel.setBlockEntity(grave);
            }
        });
    }
}
