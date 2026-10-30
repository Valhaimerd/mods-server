package net.valhaimerd.orboblivioncompat;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.skill_tree_rpgs.utils.SkillHelper;
@Mod(OrbOblivionPuffishCompat.MOD_ID)
public final class OrbOblivionPuffishCompat {
 public static final String MOD_ID="orb_of_oblivion_puffish_compat";
 public OrbOblivionPuffishCompat(IEventBus bus) {
  SkillOverhaul.init(); Utilities.init(bus);
  if(!SkillHelper.RESET_CATEGORIES.contains(SkillOverhaul.CATEGORY))SkillHelper.RESET_CATEGORIES.add(SkillOverhaul.CATEGORY);
 }
}
