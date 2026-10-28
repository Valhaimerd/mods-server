package MOD_PACKAGE;
import java.util.*;
import net.spell_engine.rpg_series.config.ArmorSetConfig;
import net.spell_engine.rpg_series.item.Armor;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
/** Apply the pack's flat-healing balance to defaults and existing loaded equipment settings. */
public final class HealingArmorBalance {
 public static void apply(Map<String,ArmorSetConfig> configs,List<Armor.Entry> entries){
  for(var entry:entries){float amount=switch(entry.name()){case "paladin_armor"->1f;case "crusader_armor"->1.25f;case "netherite_crusader_armor"->1.5f;case "justicar_armor","lightbringer_armor"->1.75f;default->-1f;};
   if(amount<0)continue;adjust(entry.defaults(),amount);var loaded=configs.get(entry.name());if(loaded!=null)adjust(loaded,amount);}
 }
 private static void adjust(ArmorSetConfig config,float amount){
  for(var piece:new ArmorSetConfig.Piece[]{config.head,config.chest,config.legs,config.feet}){
   if(piece==null||piece.attributes==null)continue;
   for(var modifier:piece.attributes)if("spell_power:healing".equals(modifier.attribute)&&modifier.operation==Operation.ADD_VALUE)modifier.value=amount;
  }
 }
}
