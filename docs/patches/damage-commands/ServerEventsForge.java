package io.redspace.pvp_flagging.events;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public final class ServerEventsForge {
    private ServerEventsForge() {}
    public static final Identifier BALANCE = Identifier.parse("pvp_flagging:player_power_balance");
    public static void clearPowerPenalty(ServerPlayer player) {
        for (String key : new String[]{"minecraft:attack_damage", "ranged_weapon:damage",
                "spell_power:arcane", "spell_power:fire", "spell_power:frost",
                "spell_power:lightning", "spell_power:soul"}) {
            var holder = BuiltInRegistries.ATTRIBUTE.get(Identifier.parse(key));
            if (holder.isEmpty()) continue;
            var attribute = player.getAttribute(holder.get());
            if (attribute != null) attribute.removeModifier(BALANCE);
        }
    }
    public static ServerPlayer playerSource(net.minecraft.world.entity.Entity entity) {
        for (int depth = 0; entity != null && depth < 4; depth++) {
            if (entity instanceof ServerPlayer player) return player;
            if (entity instanceof net.minecraft.world.entity.projectile.Projectile projectile) {
                entity = projectile.getOwner();
                continue;
            }
            String type = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
            if (!java.util.Set.of("archers:spirit_wolf", "wizards:arcane_emitter",
                    "wizards:fire_hydra", "wizards:frost_elemental").contains(type)) return null;
            if (!(entity instanceof net.minecraft.world.entity.OwnableEntity ownable)) return null;
            entity = ownable.getOwner();
        }
        return null;
    }

    public static void retainPvpDamage(LivingDamageEvent.Pre event) {
        var attacker = event.getSource().getEntity();
        if (attacker == null) attacker = event.getSource().getDirectEntity();
        var owner = playerSource(attacker);
        if (owner != null) {
            ServerPlayer protectedPlayer = event.getEntity() instanceof ServerPlayer player ? player
                    : event.getEntity() instanceof net.minecraft.world.entity.OwnableEntity ownable
                            && ownable.getRootOwner() instanceof ServerPlayer player ? player : null;
            if (protectedPlayer != null && (owner == protectedPlayer || !owner.canHarmPlayer(protectedPlayer)
                    || net.neoforged.fml.ModList.get().isLoaded("better_party")
                    && dev.betterparty.party.PartyManager.forServer(owner.level().getServer())
                            .blocksFriendlyFire(owner, protectedPlayer))) {
                event.setNewDamage(0);
                return;
            }
            event.setNewDamage(event.getNewDamage()
                    * (float)(io.redspace.pvp_flagging.DamageCommands.get(owner.level().getServer()).percent(event.getEntity() instanceof ServerPlayer)/100.0));
        }
    }
}
