package net.darkhax.ironwolfarmor.common;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
public final class WolfArmorBalance {
 public static double reduction(Item item){
  var id=BuiltInRegistries.ITEM.getKey(item);
  if(id.toString().equals("minecraft:wolf_armor"))return .20;
  if(!id.getNamespace().equals("ironwolfarmor"))return 0;
  return switch(id.getPath()){
   case "leather_wolf_armor"->.10;
   case "copper_wolf_armor","gold_wolf_armor"->.15;
   case "chainmail_wolf_armor"->.20;
   case "iron_wolf_armor"->.25;
   case "diamond_wolf_armor"->.35;
   case "netherite_wolf_armor"->.45;
   default->0;
  };
 }
 public static float damage(Wolf wolf,DamageSource source,float amount){
  return source.is(DamageTypeTags.BYPASSES_WOLF_ARMOR)?amount:(float)(amount*(1-reduction(wolf.getBodyArmorItem().getItem())));
 }
 public static void incoming(LivingIncomingDamageEvent event){
  if(event.getEntity() instanceof Wolf wolf)event.setAmount(damage(wolf,event.getSource(),event.getAmount()));
 }
 public static void components(ModifyDefaultComponentsEvent event){
  event.modifyMatching((item,components)->reduction(item)>0,(builder,registry,item)->{
   var attributes=builder.get(DataComponents.ATTRIBUTE_MODIFIERS);
   if(attributes!=null)builder.set(DataComponents.ATTRIBUTE_MODIFIERS,new ItemAttributeModifiers(attributes.modifiers().stream()
     .filter(entry->!entry.attribute().equals(Attributes.ARMOR)&&!entry.attribute().equals(Attributes.ARMOR_TOUGHNESS)).toList()));
   var lore=builder.get(DataComponents.LORE);
   builder.set(DataComponents.LORE,(lore==null?ItemLore.EMPTY:lore).withLineAdded(
     Component.translatable("tooltip.ironwolfarmor.damage_reduction",(int)Math.round(reduction(item)*100)).withStyle(net.minecraft.ChatFormatting.BLUE)));
  });
 }
}
