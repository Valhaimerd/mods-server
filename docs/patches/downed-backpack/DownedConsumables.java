package net.p3pp3rf1y.sophisticatedcore.compat;

import de.mrstilu2k6.better_revive.util.DownedPlayerManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;
import team.creative.playerrevive.server.PlayerReviveServer;

/** Optional integration shared by feeding and alchemy, including placed storage. */
public final class DownedConsumables {
    private DownedConsumables() {}

    public static boolean isDowned(Entity entity) {
        if (!(entity instanceof Player player) || player.level().isClientSide()) return false;
        if (ModList.get().isLoaded("playerrevive") && PlayerReviveServer.isBleeding(player)) return true;
        return ModList.get().isLoaded("better_revive") && DownedPlayerManager.isDowned(player.getUUID());
    }
}
