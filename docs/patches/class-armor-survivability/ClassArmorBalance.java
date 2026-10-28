package MOD_PACKAGE;
import java.util.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.equipment.*;
import net.spell_engine.rpg_series.item.*;
import net.spell_engine.rpg_series.config.*;
/** Pack defense policy, evaluated during native item creation and config registration. */
public final class ClassArmorBalance {
 private static int[] target(String name){return switch(name){
   case "wizard_robe" -> new int[]{2,4,3,2,0,0};
   case "arcane_robe" -> new int[]{2,6,5,2,0,1};
   case "fire_robe" -> new int[]{2,6,5,2,0,1};
   case "frost_robe" -> new int[]{2,6,5,2,0,1};
   case "netherite_arcane_robe" -> new int[]{3,6,5,2,1,2};
   case "netherite_fire_robe" -> new int[]{3,6,5,2,1,2};
   case "netherite_frost_robe" -> new int[]{3,6,5,2,1,2};
   case "priest_robe" -> new int[]{2,4,3,2,0,0};
   case "prior_robe" -> new int[]{2,6,5,2,0,1};
   case "netherite_prior_robe" -> new int[]{3,6,5,2,1,2};
   case "rogue_armor" -> new int[]{2,5,3,2,0,0};
   case "assassin_armor" -> new int[]{3,6,5,2,0,1};
   case "netherite_assassin_armor" -> new int[]{3,6,5,3,1,2};
   case "warrior_armor" -> new int[]{2,5,4,2,0,0};
   case "berserker_armor" -> new int[]{3,6,5,3,0,1};
   case "netherite_berserker_armor" -> new int[]{3,7,5,3,1,2};
   case "archer_armor" -> new int[]{2,5,3,2,0,0};
   case "ranger_armor" -> new int[]{3,6,5,2,0,1};
   case "netherite_ranger_armor" -> new int[]{3,6,5,3,1,2};
   case "astral_robe" -> new int[]{3,6,5,2,1,2};
   case "scarlet_robe" -> new int[]{3,6,5,2,1,2};
   case "glacier_robe" -> new int[]{3,6,5,2,1,2};
   case "tempest_robe" -> new int[]{3,6,5,2,1,2};
   case "smouldering_robe" -> new int[]{3,6,5,2,1,2};
   case "rimeweave_robe" -> new int[]{3,6,5,2,1,2};
   case "avatar_robe" -> new int[]{3,6,5,2,1,2};
   case "absolution_robe" -> new int[]{3,6,5,2,1,2};
   case "deathmantle_armor" -> new int[]{3,6,5,3,1,2};
   case "slayer_armor" -> new int[]{3,6,5,3,1,2};
   case "strider_armor" -> new int[]{3,6,5,3,1,2};
   case "riftstalker_armor" -> new int[]{3,6,5,3,1,2};
   case "destroyer_armor" -> new int[]{3,7,5,3,1,2};
   case "onslaught_armor" -> new int[]{3,7,5,3,1,2};
   default -> null;
 };}
 public static void apply(Map<String,ArmorSetConfig> configs,List<Armor.Entry> entries){
  for(var entry:entries){var target=target(entry.name());if(target==null)continue;
   adjust(entry.defaults(),target);var loaded=configs.get(entry.name());if(loaded!=null)adjust(loaded,target);
  }
 }
 private static boolean defense(AttributeModifier value){return value.attribute.equals("minecraft:armor")||value.attribute.equals("minecraft:armor_toughness");}
 private static void adjust(ArmorSetConfig config,int[] target){
  config.armor_toughness=target[4];var pieces=new ArmorSetConfig.Piece[]{config.head,config.chest,config.legs,config.feet};
  for(int i=0;i<4;i++){
   var piece=pieces[i];if(piece==null)throw new IllegalStateException("Missing class armor piece "+i);
   piece.armor=target[i];if(piece.attributes!=null)piece.attributes.removeIf(ClassArmorBalance::defense);
   if(piece.conditional_attributes!=null){var c=piece.conditional_attributes;piece.conditional_attributes=new ConditionalAttributes(c.required_mod(),c.attributes().stream().filter(a->!defense(a)).toList());}
  }
 }
 private static int durability(Identifier id,int original){var target=target(id.getPath());if(target==null)return original;return switch(target[5]){case 0->ArmorMaterials.IRON.durability();case 1->ArmorMaterials.DIAMOND.durability();default->ArmorMaterials.NETHERITE.durability();};}
 private static ArmorMaterial material(Identifier id,ArmorMaterial source){
  if(target(id.getPath())==null)return source;
  return new ArmorMaterial(durability(id,source.durability()),source.defense(),source.enchantmentValue(),source.equipSound(),source.toughness(),source.knockbackResistance(),source.repairIngredient(),source.assetId());
 }
 public static Armor.Entry create(ArmorMaterial material,Identifier id,int durability,Armor.Set.ItemFactory factory,ArmorSetConfig config){return Armor.Entry.create(material(id,material),id,durability(id,durability),factory,config);}
 public static Armor.Entry create(ArmorMaterial material,Identifier id,int durability,Armor.Set.ItemFactory factory,ArmorSetConfig config,Equipment.LootProperties loot){return Armor.Entry.create(material(id,material),id,durability(id,durability),factory,config,loot);}
 public static Armor.Entry create(ArmorMaterial material,Identifier id,int durability,Armor.Set.ItemFactory factory,ArmorSetConfig config,Equipment.LootProperties loot,Armor.ItemSettingsTweaker tweak){return Armor.Entry.create(material(id,material),id,durability(id,durability),factory,config,loot,tweak);}
}
