package io.redspace.pvp_flagging.events;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public final class ServerEventsForge {
    private ServerEventsForge() {}

    public static void retainPvpDamage(LivingDamageEvent.Pre event) {
        if (event.getEntity() instanceof ServerPlayer
                && event.getSource().getEntity() instanceof ServerPlayer) {
            event.setNewDamage(event.getNewDamage() * 0.5F);
        }
    }
}
