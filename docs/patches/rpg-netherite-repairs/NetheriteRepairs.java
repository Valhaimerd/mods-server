package net.spell_engine.rpg_series.item;
import java.util.Set;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Repairable;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

/** Only explicitly netherite-tier RPG gear; legendary sets retain their materials. */
@EventBusSubscriber(modid="spell_engine")
public final class NetheriteRepairs {
 private static final Set<String> CLASSES=Set.of("wizards","paladins","rogues","archers");
 @SubscribeEvent(priority=EventPriority.LOWEST)
 public static void repairMaterials(ModifyDefaultComponentsEvent event){
  event.modifyMatching((item,components)->{
   var id=BuiltInRegistries.ITEM.getKey(item);
   return CLASSES.contains(id.getNamespace())&&Set.of(id.getPath().split("_")).contains("netherite")&&components.has(DataComponents.MAX_DAMAGE);
  },(builder,registries,item)->builder.set(DataComponents.REPAIRABLE,new Repairable(HolderSet.direct(Items.NETHERITE_SCRAP.builtInRegistryHolder()))));
 }
}
