package de.mrstilu2k6.gateway_of_doom.entity;

import de.mrstilu2k6.better_revive.util.DownedPlayerManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;
import team.creative.playerrevive.server.PlayerReviveServer;

/** Keeps Gateway's own scoring and combat rules, while excluding players who cannot fight. */
public final class DownedTargeting {
    private DownedTargeting() {}

    public static boolean isActive(LivingEntity entity) {
        if (entity == null || !entity.isAlive()) return false;
        if (!(entity instanceof Player player)) return true;
        if (ModList.get().isLoaded("playerrevive") && PlayerReviveServer.isBleeding(player)) return false;
        return !ModList.get().isLoaded("better_revive") || !DownedPlayerManager.isDowned(player.getUUID());
    }
}
