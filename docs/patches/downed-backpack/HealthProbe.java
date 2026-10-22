package local.downedprobe;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import team.creative.playerrevive.PlayerRevive;
import team.creative.playerrevive.server.PlayerReviveServer;
import team.creative.playerrevive.server.ReviveEventServer;
import de.mrstilu2k6.better_revive.util.DownedPlayerManager;

final class HealthProbe {
    static void run(ServerPlayer player,ConsumablesProbe probe) {
        var health=player.getAttribute(Attributes.MAX_HEALTH);
        int originalReviveHealth=PlayerRevive.CONFIG.revive.healthAfter;
        PlayerRevive.CONFIG.revive.healthAfter=7;
        try {
            for(double maximum:new double[]{20,40,73.5}) {
                health.setBaseValue(maximum);player.setGameMode(GameType.SURVIVAL);
                PlayerReviveServer.getBleeding(player).revive(player);player.setHealth(0);
                var death=new LivingDeathEvent(player,player.damageSources().generic());
                new ReviveEventServer().playerDied(death);
                probe.check(death.isCanceled() && PlayerReviveServer.isBleeding(player),"native knockdown intercepts death at max "+maximum);
                probe.check(player.getHealth()==player.getMaxHealth(),"PlayerRevive death entry uses current max HP "+maximum+", actual="+player.getHealth());
                PlayerReviveServer.revive(player);
                probe.check(player.getHealth()==7,"configured post-rescue health remains 7 at max "+maximum);
                player.setHealth(1);PlayerReviveServer.startBleeding(player,player.damageSources().generic());
                probe.check(player.getHealth()==player.getMaxHealth(),"PlayerRevive API entry uses current max HP "+maximum);
                PlayerReviveServer.revive(player);
                DownedPlayerManager.enterDowned(player);
                probe.check(DownedPlayerManager.isDowned(player.getUUID()),"Better Revive enters native downed state");
                probe.check(player.getHealth()==player.getMaxHealth(),"Better Revive entry uses current max HP "+maximum+", actual="+player.getHealth());
                DownedPlayerManager.clearAll();player.setGameMode(GameType.SURVIVAL);
            }
            health.setBaseValue(20);
            var id=Identifier.parse("downed_probe:gear_health");
            health.addTransientModifier(new AttributeModifier(id,13,AttributeModifier.Operation.ADD_VALUE));
            try {
                player.setHealth(1);PlayerReviveServer.startBleeding(player,player.damageSources().generic());
                probe.check(player.getHealth()==33,"gear-added maximum health is included on knockdown");
                PlayerReviveServer.revive(player);
            } finally {health.removeModifier(id);}
        } finally {
            PlayerRevive.CONFIG.revive.healthAfter=originalReviveHealth;
            PlayerReviveServer.getBleeding(player).revive(player);
            DownedPlayerManager.clearAll();player.setGameMode(GameType.SURVIVAL);
            health.setBaseValue(40);player.setHealth(20);
        }
    }
}
