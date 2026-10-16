package de.mrstilu2k6.gateway_of_doom.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import team.creative.playerrevive.server.PlayerReviveServer;
import de.mrstilu2k6.better_revive.util.DownedPlayerManager;

/** Shared gateway targeting rules for rescue-aware multiplayer combat. */
public final class GatewayTargeting {
    private static final int RETARGET_INTERVAL_TICKS = 60;
    private GatewayTargeting() {}

    public static boolean isActive(Player player) {
        if (player == null || !player.isAlive() || player.isCreative() || player.isSpectator()) return false;
        if (ModList.get().isLoaded("playerrevive") && PlayerReviveServer.isBleeding(player)) return false;
        return !ModList.get().isLoaded("better_revive") || !DownedPlayerManager.isDowned(player.getUUID());
    }

    public static Player nearestActive(Mob mob, double range) {
        return nearestActive(mob, range, false);
    }

    public static Player nearestActive(Mob mob, double range, boolean daytimeLandOnly) {
        Level level = mob.level();
        Player nearest = null;
        double best = range * range;
        for (Player player : level.players()) {
            if (!isActive(player) || !canAttackInCurrentMode(mob, player, daytimeLandOnly)) continue;
            double distance = mob.distanceToSqr(player);
            if (distance <= best) {
                best = distance;
                nearest = player;
            }
        }
        return nearest;
    }

    private static boolean canAttackInCurrentMode(Mob mob, Player player, boolean daytimeLandOnly) {
        return !daytimeLandOnly || !mob.level().isBrightOutside() || !player.isInWater();
    }

    /** Reconsider the nearest eligible player every three seconds per mob. */
    public static Player refreshTarget(Mob mob, Player current, double range, boolean daytimeLandOnly) {
        if (current != null && mob.tickCount % RETARGET_INTERVAL_TICKS != 0
                && isActive(current) && canAttackInCurrentMode(mob, current, daytimeLandOnly)) {
            return current;
        }
        return nearestActive(mob, range, daytimeLandOnly);
    }

    public static ServerPlayer nearestActive(ServerLevel level, Entity origin) {
        ServerPlayer nearest = null;
        double best = Double.MAX_VALUE;
        for (ServerPlayer player : level.players()) {
            if (!isActive(player)) continue;
            double distance = player.distanceToSqr(origin);
            if (distance < best) {
                best = distance;
                nearest = player;
            }
        }
        return nearest;
    }
}
