/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.resources.Identifier
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.tags.DamageTypeTags
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent
 *  net.neoforged.neoforge.event.entity.player.AttackEntityEvent
 *  net.neoforged.neoforge.event.tick.PlayerTickEvent$Post
 */
package com.eiden.iourusraces.event;

import com.eiden.iourusraces.race.PlayerRaceData;
import com.eiden.iourusraces.race.Race;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
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
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player = (ServerPlayer)livingEntity;
        Race race = PlayerRaceData.getRace((Player)player);
        if (race == Race.DRACONIC && event.getSource().is(DamageTypeTags.IS_FIRE)) {
            event.setCanceled(true);
            return;
        }
    }

    @SubscribeEvent
    public static void onOrcAttack(AttackEntityEvent event) {
        if(event.getEntity() instanceof ServerPlayer p)com.eiden.iourusraces.race.RaceBalanceRules.apply(p,PlayerRaceData.getRace(p));
    }

    @SubscribeEvent
    public static void onOrcLowHealthParticles(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        if (PlayerRaceData.getRace((Player)player2) != Race.ORC) {
            return;
        }
        float healthPercent = player2.getHealth() / player2.getMaxHealth();
        if (healthPercent > 0.5f) {
            return;
        }
        player2.level().sendParticles((ParticleOptions)ParticleTypes.CRIMSON_SPORE, player2.getX(), player2.getY() + 1.0, player2.getZ(), 6, 0.35, 0.45, 0.35, 0.01);
    }

    @SubscribeEvent
    public static void onElfSlowFalling(PlayerTickEvent.Post event) {
        if(event.getEntity() instanceof ServerPlayer p && PlayerRaceData.getRace(p)==Race.ELF)com.eiden.iourusraces.race.RaceBalanceRules.slowFall(p);
    }

    @SubscribeEvent
    public static void onElfArrowDamage(LivingIncomingDamageEvent event) {
        // Elf no longer amplifies projectile damage.
    }

    @SubscribeEvent
    public static void onMelforkAbilities(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        AttributeInstance attackAttribute = player2.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttribute == null) {
            return;
        }
        Race race = PlayerRaceData.getRace((Player)player2);
        if (race != Race.MELFORK) {
            attackAttribute.removeModifier(MELFORK_WATER_STRENGTH_ID);
            attackAttribute.removeModifier(MELFORK_LAND_WEAKNESS_ID);
            return;
        }
        attackAttribute.removeModifier(MELFORK_WATER_STRENGTH_ID);
        attackAttribute.removeModifier(MELFORK_LAND_WEAKNESS_ID);
        if (player2.isInWater()) {
            player2.setAirSupply(player2.getMaxAirSupply());
            attackAttribute.addTransientModifier(new AttributeModifier(MELFORK_WATER_STRENGTH_ID, 3.0, AttributeModifier.Operation.ADD_VALUE));
        } else {
            attackAttribute.addTransientModifier(new AttributeModifier(MELFORK_LAND_WEAKNESS_ID, -2.0, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    @SubscribeEvent
    public static void onDraconicPassiveEffects(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        if (PlayerRaceData.getRace((Player)player2) != Race.DRACONIC) {
            return;
        }
        if (player2.isInLava() && player2.getDeltaMovement().y < -0.1) {
            player2.setDeltaMovement(player2.getDeltaMovement().x, -0.1, player2.getDeltaMovement().z);
        }
    }
}
