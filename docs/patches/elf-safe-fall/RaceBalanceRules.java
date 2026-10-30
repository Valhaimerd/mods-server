/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.Identifier
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$Clone
 *  net.neoforged.neoforge.event.tick.PlayerTickEvent$Post
 */
package com.eiden.iourusraces.race;

import com.eiden.iourusraces.race.PlayerRaceData;
import com.eiden.iourusraces.race.Race;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid="iourusraces")
public final class RaceBalanceRules {
    static final Identifier SPEED = Identifier.parse((String)"iourusraces:orc_attack_speed");
    static final Identifier MELEE = Identifier.parse((String)"iourusraces:orc_melee");
    static final Identifier RANGED = Identifier.parse((String)"iourusraces:orc_ranged");
    static final Identifier DRAW = Identifier.parse((String)"iourusraces:orc_draw_speed");
    static final Identifier ARMOR = Identifier.parse((String)"iourusraces:dwarf_armor");
    static final Identifier SAFE_FALL = Identifier.parse("iourusraces:elf_safe_fall");
    static final String MIGRATION = "iourusraces:orc_attributes_v2";
    static final String SAVED_BASE = "iourusraces:legacy_orc_attack_base";

    static AttributeInstance ranged(Player player) {
        return BuiltInRegistries.ATTRIBUTE.get(Identifier.parse((String)"ranged_weapon:damage")).map(arg_0 -> ((Player)player).getAttribute(arg_0)).orElse(null);
    }

    static AttributeInstance draw(Player player) {
        return BuiltInRegistries.ATTRIBUTE.get(Identifier.parse((String)"ranged_weapon:haste")).map(arg_0 -> ((Player)player).getAttribute(arg_0)).orElse(null);
    }

    static void set(AttributeInstance attributeInstance, Identifier identifier, double d, AttributeModifier.Operation operation) {
        if (attributeInstance == null) {
            return;
        }
        AttributeModifier attributeModifier = attributeInstance.getModifier(identifier);
        if (attributeModifier != null && attributeModifier.amount() == d && attributeModifier.operation() == operation) {
            return;
        }
        if (attributeModifier != null) {
            attributeInstance.removeModifier(identifier);
        }
        if (d != 0.0) {
            attributeInstance.addTransientModifier(new AttributeModifier(identifier, d, operation));
        }
    }

    public static int band(double d) {
        if (d <= 0.200001) {
            return 4;
        }
        if (d <= 0.30000099999999996) {
            return 3;
        }
        if (d <= 0.400001) {
            return 2;
        }
        return 1;
    }

    public static void migrate(Player player, Race race) {
        double d;
        if (!(player instanceof ServerPlayer) || player.getPersistentData().getBooleanOr(MIGRATION, false)) {
            return;
        }
        AttributeInstance attributeInstance = player.getAttribute(Attributes.ATTACK_DAMAGE);
        double d2 = d = attributeInstance == null ? 1.0 : attributeInstance.getBaseValue();
        if (race == Race.ORC && d >= 4.0 && d <= 10.0) {
            player.getPersistentData().putDouble(SAVED_BASE, d);
            attributeInstance.setBaseValue(1.0);
        }
        player.getPersistentData().putBoolean(MIGRATION, true);
    }

    public static void apply(Player player, Race race) {
        RaceBalanceRules.migrate(player, race);
        set(player.getAttribute(Attributes.SAFE_FALL_DISTANCE), SAFE_FALL, race == Race.ELF ? 5 : 0, AttributeModifier.Operation.ADD_VALUE);
        RaceBalanceRules.set(player.getAttribute(Attributes.ATTACK_SPEED), SPEED, race == Race.ORC ? -0.1 : 0.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        RaceBalanceRules.set(player.getAttribute(Attributes.ARMOR), ARMOR, race == Race.DWARF ? 2.0 : 0.0, AttributeModifier.Operation.ADD_VALUE);
        RaceBalanceRules.set(RaceBalanceRules.draw(player), DRAW, race == Race.ORC ? -0.1 : 0.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        RaceBalanceRules.update(player, race);
    }

    public static void update(Player player, Race race) {
        int n = race == Race.ORC ? RaceBalanceRules.band((double)player.getHealth() / (double)player.getMaxHealth()) : 0;
        RaceBalanceRules.set(player.getAttribute(Attributes.ATTACK_DAMAGE), MELEE, n == 0 ? 0.0 : (double)(n + 1), AttributeModifier.Operation.ADD_VALUE);
        RaceBalanceRules.set(RaceBalanceRules.ranged(player), RANGED, n == 0 ? 0.0 : (double)(n + 1) * 0.5, AttributeModifier.Operation.ADD_VALUE);
    }

    public static void remove(Player player) {
        set(player.getAttribute(Attributes.SAFE_FALL_DISTANCE), SAFE_FALL, 0, AttributeModifier.Operation.ADD_VALUE);
        RaceBalanceRules.set(RaceBalanceRules.draw(player), DRAW, 0.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        RaceBalanceRules.set(player.getAttribute(Attributes.ATTACK_SPEED), SPEED, 0.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        RaceBalanceRules.set(player.getAttribute(Attributes.ARMOR), ARMOR, 0.0, AttributeModifier.Operation.ADD_VALUE);
        RaceBalanceRules.set(player.getAttribute(Attributes.ATTACK_DAMAGE), MELEE, 0.0, AttributeModifier.Operation.ADD_VALUE);
        RaceBalanceRules.set(RaceBalanceRules.ranged(player), RANGED, 0.0, AttributeModifier.Operation.ADD_VALUE);
    }


    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post post) {
        Object object = post.getEntity();
        if (object instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)object;
            object = PlayerRaceData.getRace((Player)serverPlayer);
            RaceBalanceRules.apply((Player)serverPlayer, (Race)((Object)object));
        }
    }

    @SubscribeEvent
    public static void clone(PlayerEvent.Clone clone) {
        CompoundTag compoundTag = clone.getOriginal().getPersistentData();
        CompoundTag compoundTag2 = clone.getEntity().getPersistentData();
        if (compoundTag.getBooleanOr(MIGRATION, false)) {
            compoundTag2.putBoolean(MIGRATION, true);
        }
        if (compoundTag.contains(SAVED_BASE)) {
            compoundTag2.putDouble(SAVED_BASE, compoundTag.getDoubleOr(SAVED_BASE, 1.0));
        }
    }
}
