package com.eiden.iourusraces.race;

import com.eiden.iourusraces.race.Race;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public final class RaceAttributes {
    private static final Identifier HEALTH_ID = Identifier.fromNamespaceAndPath((String)"iourusraces", (String)"race_health");
    private static final Identifier ELF_JUMP_ID = Identifier.fromNamespaceAndPath((String)"iourusraces", (String)"elf_jump");
    private static final Identifier GOBLIN_JUMP_ID = Identifier.fromNamespaceAndPath((String)"iourusraces", (String)"goblin_jump");
    private static final Identifier DWARF_MINING_ID = Identifier.fromNamespaceAndPath((String)"iourusraces", (String)"dwarf_mining_speed");
    private static final Identifier MELFORK_WATER_STRENGTH_ID = Identifier.fromNamespaceAndPath((String)"iourusraces", (String)"melfork_water_strength");
    private static final Identifier MELFORK_LAND_WEAKNESS_ID = Identifier.fromNamespaceAndPath((String)"iourusraces", (String)"melfork_land_weakness");
    private static final Identifier SPEED_ID = Identifier.fromNamespaceAndPath((String)"iourusraces", (String)"race_speed");
    private static final Identifier SCALE_ID = Identifier.fromNamespaceAndPath((String)"iourusraces", (String)"race_scale");
    private static final Identifier WATER_MOVEMENT_ID = Identifier.fromNamespaceAndPath((String)"iourusraces", (String)"race_water_movement");

    private RaceAttributes() {
    }

    public static void applyRaceAttributes(Player player, Race race) {
        RaceAttributes.removeAttributes(player);
        AttributeInstance jumpAttribute = player.getAttribute(Attributes.JUMP_STRENGTH);
        AttributeInstance miningAttribute = player.getAttribute(Attributes.MINING_EFFICIENCY);
        if (jumpAttribute != null && race == Race.ELF) {
            jumpAttribute.addPermanentModifier(new AttributeModifier(ELF_JUMP_ID, 0.1, AttributeModifier.Operation.ADD_VALUE));
        }
        if (jumpAttribute != null && race == Race.GOBLIN) {
            jumpAttribute.addPermanentModifier(new AttributeModifier(GOBLIN_JUMP_ID, 0.15, AttributeModifier.Operation.ADD_VALUE));
        }
        if (miningAttribute != null && race == Race.DWARF) {
            miningAttribute.addPermanentModifier(new AttributeModifier(DWARF_MINING_ID, 2.3, AttributeModifier.Operation.ADD_VALUE));
        }
        AttributeInstance healthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        AttributeInstance scaleAttribute = player.getAttribute(Attributes.SCALE);
        AttributeInstance waterMovementAttribute = player.getAttribute(Attributes.WATER_MOVEMENT_EFFICIENCY);
        if (healthAttribute != null) {
            healthAttribute.addPermanentModifier(new AttributeModifier(HEALTH_ID, race.getMaxHealth() - 20.0, AttributeModifier.Operation.ADD_VALUE));
        }
        if (speedAttribute != null) {
            speedAttribute.addPermanentModifier(new AttributeModifier(SPEED_ID, race.getSpeedMultiplier() - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
        if (scaleAttribute != null) {
            scaleAttribute.addPermanentModifier(new AttributeModifier(SCALE_ID, (double)race.getScale() - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
        AsgardianRules.applyMining(player, race);
        if (waterMovementAttribute != null && race == Race.MELFORK) {
            waterMovementAttribute.addPermanentModifier(new AttributeModifier(WATER_MOVEMENT_ID, 1.0, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    public static void removeAttributes(Player player) {
        AsgardianRules.removeMining(player);
        RaceAttributes.removeModifier(player.getAttribute(Attributes.MAX_HEALTH), HEALTH_ID);
        RaceAttributes.removeModifier(player.getAttribute(Attributes.MOVEMENT_SPEED), SPEED_ID);
        RaceAttributes.removeModifier(player.getAttribute(Attributes.SCALE), SCALE_ID);
        RaceAttributes.removeModifier(player.getAttribute(Attributes.WATER_MOVEMENT_EFFICIENCY), WATER_MOVEMENT_ID);
        RaceAttributes.removeModifier(player.getAttribute(Attributes.JUMP_STRENGTH), ELF_JUMP_ID);
        RaceAttributes.removeModifier(player.getAttribute(Attributes.JUMP_STRENGTH), GOBLIN_JUMP_ID);
        RaceAttributes.removeModifier(player.getAttribute(Attributes.MINING_EFFICIENCY), DWARF_MINING_ID);
    }

    private static void removeModifier(AttributeInstance attribute, Identifier id) {
        if (attribute != null) {
            attribute.removeModifier(id);
        }
    }
}
