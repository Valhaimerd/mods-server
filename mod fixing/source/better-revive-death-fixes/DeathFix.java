package de.mrstilu2k6.better_revive.util;

import de.mrstilu2k6.better_revive.config.ReviveConfig;
import de.mrstilu2k6.better_revive.event.ModEvents;
import de.mrstilu2k6.better_revive.block.GraveBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import team.creative.playerrevive.server.PlayerReviveServer;
import java.util.List;
import java.util.UUID;

/** Keeps final death, grave placement, and compass state consistent. */
public final class DeathFix {
    private DeathFix() {}

    public static boolean isFinalDamageType(List<String> configured, String damageType) {
        return !"minecraft:lava".equals(damageType) && configured.contains(damageType);
    }

    public static void afterEnterDowned(ServerPlayer player) {
        if (PlayerReviveServer.isBleeding(player)) {
            DownedPlayerManager.finalizeDeath(player);
        }
    }

    public static void onRespawn(PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        DownedPlayerManager.clearAfterRealDeath(player);
        if (PlayerReviveServer.isBleeding(player)) {
            PlayerReviveServer.getBleeding(player).revive(player);
        }
    }

    public static boolean isCompassValid(UUID id) {
        if (id == null) return false;
        GraveRegistry.GraveLocation location = GraveRegistry.getGraveLocation(id);
        if (location == null) return false;
        return ReviveConfig.collectionDurationSeconds > 0 || location.expiresAtMs() <= 0 || location.expiresAtMs() > System.currentTimeMillis();
    }

    public static void onServerTick(MinecraftServer server) {
        if (server.getTickCount() % 20 != 0) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            CompassHelper.removeStaleCompasses(player);
        }
    }

    public static BlockPos findGravePosition(Level world, BlockPos center) {
        int minY = world.getMinY();
        int maxY = world.getMaxY() - 2;
        int startY = Math.max(minY + 1, Math.min(maxY, center.getY() + 2));
        for (int radius = 0; radius <= 16; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (radius != 0 && Math.max(Math.abs(dx), Math.abs(dz)) != radius) continue;
                    int x = center.getX() + dx;
                    int z = center.getZ() + dz;
                    for (int y = startY; y >= Math.max(minY, startY - 32); y--) {
                        BlockPos supportPos = new BlockPos(x, y, z);
                        if (world.getFluidState(supportPos).is(FluidTags.LAVA)) break;
                        BlockState support = world.getBlockState(supportPos);
                        if (!support.getCollisionShape(world, supportPos).isEmpty()) {
                            BlockPos candidate = supportPos.above();
                            if (canPlace(world, candidate) && world.getBlockState(candidate.above()).getCollisionShape(world, candidate.above()).isEmpty()) return candidate;
                            break;
                        }
                    }
                    int surfaceY = Math.max(minY + 1, Math.min(maxY, world.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, x, z)));
                    BlockPos surface = new BlockPos(x, surfaceY, z);
                    if (canPlace(world, surface) && world.getBlockState(surface.above()).getCollisionShape(world, surface.above()).isEmpty()) return surface;
                }
            }
        }
        // A lava sea has no shore nearby. Place the unbreakable grave just above its surface.
        for (int y = Math.max(minY + 1, startY); y <= Math.min(maxY, startY + 32); y++) {
            BlockPos candidate = new BlockPos(center.getX(), y, center.getZ());
            if (canPlace(world, candidate) && world.getFluidState(candidate.below()).is(FluidTags.LAVA)) return candidate;
        }
        return null;
    }

    private static boolean canPlace(Level world, BlockPos pos) {
        if (pos.getY() < world.getMinY() || pos.getY() >= world.getMaxY()) return false;
        if (world.getFluidState(pos).is(FluidTags.LAVA)) return false;
        BlockState state = world.getBlockState(pos);
        return (state.isAir() || world.getFluidState(pos).is(FluidTags.WATER)) && world.getBlockEntity(pos) == null;
    }
}
