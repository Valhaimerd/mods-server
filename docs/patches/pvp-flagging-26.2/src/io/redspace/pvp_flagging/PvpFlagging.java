package io.redspace.pvp_flagging;

import io.redspace.pvp_flagging.events.ServerEventsForge;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/** Pack-only damage retention. Combat permission belongs to Minecraft and party mods. */
@Mod(PvpFlagging.MODID)
public final class PvpFlagging {
    public static final String MODID = "pvp_flagging";

    public PvpFlagging() {
        NeoForge.EVENT_BUS.addListener(ServerEventsForge::retainPvpDamage);
    }
}
