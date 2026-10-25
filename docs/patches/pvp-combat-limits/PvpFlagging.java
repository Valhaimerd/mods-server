package io.redspace.pvp_flagging;

import io.redspace.pvp_flagging.events.ServerEventsForge;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/** Pack damage retention and automatic combat limits. Combat permission belongs to Minecraft and party mods. */
@Mod(PvpFlagging.MODID)
public final class PvpFlagging {
    public static final String MODID = "pvp_flagging";

    public PvpFlagging(net.neoforged.bus.api.IEventBus modBus) {
        modBus.addListener((net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent e) -> e.registrar("1").playToClient(CombatStatus.TYPE, CombatStatus.CODEC));
        NeoForge.EVENT_BUS.addListener(ServerEventsForge::retainPvpDamage);
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.entity.EntityJoinLevelEvent event) -> {
            if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)
                ServerEventsForge.clearPowerPenalty(player);
        });
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone event) -> {
            if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)
                ServerEventsForge.clearPowerPenalty(player);
        });
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.tick.EntityTickEvent.Pre event) -> {
            if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player && player.tickCount % 20 == 0)
                ServerEventsForge.clearPowerPenalty(player);
        });
    }
}
