package de.mrstilu2k6.better_revive.util;

import de.mrstilu2k6.better_revive.config.ReviveConfig;
import de.mrstilu2k6.better_revive.event.ModEvents;
import de.mrstilu2k6.better_revive.block.GraveBlockEntity;
import de.mrstilu2k6.better_revive.item.GraveCompassItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import team.creative.playerrevive.server.PlayerReviveServer;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Keeps final death, grave placement, and compass state consistent. */
public final class DeathFix {
    private static final Set<UUID> RESTORE_ON_NEXT_TICK = new HashSet<>();
    private DeathFix() {}

    public static boolean isFinalDamageType(List<String> configured, String damageType) {
        return !"minecraft:lava".equals(damageType) && configured.contains(damageType);
    }

    public static boolean shouldCompletePlayerReviveDeath(ServerPlayer player) {
        return PlayerReviveServer.isBleeding(player);
    }

    public static void onRespawn(PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        RESTORE_ON_NEXT_TICK.add(player.getUUID());
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

    public static int compassSlot(Inventory inventory) {
        return 8;
    }

    public static boolean placeDisplacedHotbarItem(Inventory inventory, ItemStack stack) {
        for (int slot = 9; slot < 36; slot++) {
            if (inventory.getItem(slot).isEmpty()) {
                inventory.setItem(slot, stack.copy());
                return true;
            }
        }
        return inventory.add(stack);
    }

    public static void enforceCompassSlots(ServerPlayer player) {
        Inventory inventory = player.getInventory();
        Set<UUID> seen = new HashSet<>();
        int firstCompassSlot = -1;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!GraveCompassItem.isGraveCompass(stack)) continue;
            ItemStack migrated = GraveCompassItem.migrateLegacyCompass(stack);
            if (!seen.add(CompassHelper.readMarkerId(migrated))) {
                inventory.setItem(slot, ItemStack.EMPTY);
            } else {
                inventory.setItem(slot, migrated);
                if (firstCompassSlot < 0) firstCompassSlot = slot;
            }
        }
        if (firstCompassSlot < 0 || firstCompassSlot == 8) return;
        ItemStack compass = inventory.getItem(firstCompassSlot).copy();
        ItemStack displaced = inventory.getItem(8).copy();
        inventory.setItem(firstCompassSlot, ItemStack.EMPTY);
        inventory.setItem(8, compass);
        if (!displaced.isEmpty() && !placeDisplacedHotbarItem(inventory, displaced)) {
            player.drop(displaced, false);
        }
    }

    /** Keep the one hotbar compass aimed at the newest unexpired grave. */
    public static void syncOwnerCompass(ServerPlayer player) {
        UUID newestId = findNewestGraveId(player.getUUID());
        if (newestId == null) return;
        GraveRegistry.GraveLocation newest = GraveRegistry.getGraveLocation(newestId);
        if (newest == null) return;

        Inventory inventory = player.getInventory();
        boolean hasTarget = false;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!GraveCompassItem.isGraveCompass(stack)) continue;
            UUID id = CompassHelper.readMarkerId(stack);
            if (newestId.equals(id)) {
                hasTarget = true;
            } else {
                GraveRegistry.GraveLocation location = GraveRegistry.getGraveLocation(id);
                if (location != null && player.getUUID().equals(location.ownerUuid())) {
                    inventory.setItem(slot, ItemStack.EMPTY);
                }
            }
        }
        if (!hasTarget) {
            BlockPos pos = newest.pos();
            CompassHelper.giveOrDropCompass(player, GraveCompassItem.createCompass(
                pos.getX(), pos.getY(), pos.getZ(),
                newest.dimension().identifier().toString(), newestId));
        }
        enforceCompassSlots(player);
    }

    public static UUID findNewestGraveId(UUID owner) {
        GraveRegistry.GraveEntry newest = null;
        for (GraveRegistry.GraveEntry entry : GraveRegistry.entriesByOwner(owner)) {
            if (isCompassValid(entry.graveId()) &&
                (newest == null || entry.location().sequence() > newest.location().sequence())) {
                newest = entry;
            }
        }
        return newest == null ? null : newest.graveId();
    }

    public static void onServerTick(MinecraftServer server) {
        if (!RESTORE_ON_NEXT_TICK.isEmpty()) {
            for (UUID id : List.copyOf(RESTORE_ON_NEXT_TICK)) {
                ServerPlayer player = server.getPlayerList().getPlayer(id);
                if (player != null) KeepGear.restore(player);
                RESTORE_ON_NEXT_TICK.remove(id);
            }
        }
        if (server.getTickCount() % 20 != 0) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            CompassHelper.removeStaleCompasses(player);
            syncOwnerCompass(player);
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
