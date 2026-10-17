package net.valhaimerd.orboblivioncompat;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.skill_tree_rpgs.utils.SkillHelper;

@Mod(OrbOblivionPuffishCompat.MOD_ID)
public final class OrbOblivionPuffishCompat {
    public static final String MOD_ID = "orb_of_oblivion_puffish_compat";

    private static final Identifier ADVENTURER_SKILLS =
            Identifier.fromNamespaceAndPath("adventurer_skills", "adventurer_skills");

    public OrbOblivionPuffishCompat(IEventBus modEventBus) {
        SkillOverhaul.init();
        if (!SkillHelper.RESET_CATEGORIES.contains(ADVENTURER_SKILLS)) {
            SkillHelper.RESET_CATEGORIES.add(ADVENTURER_SKILLS);
        }
    }
}
