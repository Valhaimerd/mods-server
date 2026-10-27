package MOD_PACKAGE;
import java.util.*;
import net.spell_engine.rpg_series.config.ArmorSetConfig;
import net.spell_engine.rpg_series.item.Armor;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
/** Apply the pack's flat-healing balance to defaults and existing loaded equipment settings. */
public final class HealingArmorBalance {
 public static void apply(Map<String,ArmorSetConfig> configs,List<Armor.Entry> entries){
  for(var entry:entries){adjust(entry.defaults());var loaded=configs.get(entry.name());if(loaded!=null)adjust(loaded);}
 }
 private static void adjust(ArmorSetConfig config){
  for(var piece:new ArmorSetConfig.Piece[]{config.head,config.chest,config.legs,config.feet}){
   if(piece==null||piece.attributes==null)continue;
   for(var modifier:piece.attributes)if("spell_power:healing".equals(modifier.attribute)&&modifier.operation==Operation.ADD_VALUE)modifier.value=.25f;
  }
 }
}
