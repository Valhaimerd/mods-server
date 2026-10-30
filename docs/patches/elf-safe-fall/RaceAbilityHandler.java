/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.resources.Identifier
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.tags.DamageTypeTags
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent
 *  net.neoforged.neoforge.event.entity.player.AttackEntityEvent
 *  net.neoforged.neoforge.event.tick.PlayerTickEvent$Post
 */
package com.eiden.iourusraces.event;

import com.eiden.iourusraces.race.PlayerRaceData;
import com.eiden.iourusraces.race.Race;
import com.eiden.iourusraces.race.RaceBalanceRules;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class RaceAbilityHandler {
    private static final Identifier MELFORK_WATER_STRENGTH_ID = Identifier.fromNamespaceAndPath((String)"iourusraces", (String)"melfork_water_strength");
    private static final Identifier MELFORK_LAND_WEAKNESS_ID = Identifier.fromNamespaceAndPath((String)"iourusraces", (String)"melfork_land_weakness");

    private RaceAbilityHandler() {
    }

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent livingIncomingDamageEvent) {
        LivingEntity livingEntity = livingIncomingDamageEvent.getEntity();
        if (!(livingEntity instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer serverPlayer = (ServerPlayer)livingEntity;
        Race race = PlayerRaceData.getRace((Player)serverPlayer);
        if (race == Race.DRACONIC && livingIncomingDamageEvent.getSource().is(DamageTypeTags.IS_FIRE)) {
            livingIncomingDamageEvent.setCanceled(true);
            return;
        }
    }

    @SubscribeEvent
    public static void onOrcAttack(AttackEntityEvent attackEntityEvent) {
        Player player = attackEntityEvent.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)player;
            RaceBalanceRules.apply((Player)serverPlayer, PlayerRaceData.getRace((Player)serverPlayer));
        }
    }

    @SubscribeEvent
    public static void onOrcLowHealthParticles(PlayerTickEvent.Post post) {
        Player player = post.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer serverPlayer = (ServerPlayer)player;
        if (PlayerRaceData.getRace((Player)serverPlayer) != Race.ORC) {
            return;
        }
        float f = serverPlayer.getHealth() / serverPlayer.getMaxHealth();
        if (f > 0.5f) {
            return;
        }
        serverPlayer.level().sendParticles((ParticleOptions)ParticleTypes.CRIMSON_SPORE, serverPlayer.getX(), serverPlayer.getY() + 1.0, serverPlayer.getZ(), 6, 0.35, 0.45, 0.35, 0.01);
    }


    @SubscribeEvent
    public static void onElfArrowDamage(LivingIncomingDamageEvent livingIncomingDamageEvent) {
    }

    @SubscribeEvent
    public static void onMelforkAbilities(PlayerTickEvent.Post post) {
        Player player = post.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer serverPlayer = (ServerPlayer)player;
        AttributeInstance attributeInstance = serverPlayer.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attributeInstance == null) {
            return;
        }
        Race race = PlayerRaceData.getRace((Player)serverPlayer);
        if (race != Race.MELFORK) {
            attributeInstance.removeModifier(MELFORK_WATER_STRENGTH_ID);
            attributeInstance.removeModifier(MELFORK_LAND_WEAKNESS_ID);
            return;
        }
        attributeInstance.removeModifier(MELFORK_WATER_STRENGTH_ID);
        attributeInstance.removeModifier(MELFORK_LAND_WEAKNESS_ID);
        if (serverPlayer.isInWater()) {
            serverPlayer.setAirSupply(serverPlayer.getMaxAirSupply());
            attributeInstance.addTransientModifier(new AttributeModifier(MELFORK_WATER_STRENGTH_ID, 3.0, AttributeModifier.Operation.ADD_VALUE));
        } else {
            attributeInstance.addTransientModifier(new AttributeModifier(MELFORK_LAND_WEAKNESS_ID, -2.0, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    @SubscribeEvent
    public static void onDraconicPassiveEffects(PlayerTickEvent.Post post) {
        Player player = post.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer serverPlayer = (ServerPlayer)player;
        if (PlayerRaceData.getRace((Player)serverPlayer) != Race.DRACONIC) {
            return;
        }
        if (serverPlayer.isInLava() && serverPlayer.getDeltaMovement().y < -0.1) {
            serverPlayer.setDeltaMovement(serverPlayer.getDeltaMovement().x, -0.1, serverPlayer.getDeltaMovement().z);
        }
    }
}
