package local.armor;
import java.nio.file.*;
import java.util.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.*;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.*;
import net.minecraft.world.phys.Vec3;
@Mod("armor_probe") public class ArmorProbe {
 int checks;
 final List<String> hidden=List.of("protection_grace_enchantment:protection_grace","nova_structures:antidote","nova_structures:traveler","enderzoology:soulbound","air_jump_enchantment:air_jump");
 void check(boolean ok,String label){if(!ok)throw new AssertionError(label);checks++;System.out.println("ARMOR_PASS "+label);}
 public ArmorProbe(){NeoForge.EVENT_BUS.addListener(this::start);}
 void start(ServerStartedEvent e){var server=e.getServer();server.execute(()->{try{
  var level=server.overworld();var registry=level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
  var disabled=TagKey.create(Registries.ENCHANTMENT,Identifier.parse("penchant:disabled"));
  var viewer=TagKey.create(Registries.ENCHANTMENT,Identifier.parse("c:hidden_from_recipe_viewers"));
  var player=FakePlayerFactory.getMinecraft(level);player.setPos(0,80,0);player.giveExperienceLevels(1000);
  for(String id:hidden){var h=registry.get(Identifier.parse(id)).orElseThrow();check(h.is(disabled),"disabled tag "+id);check(h.is(viewer),"recipe viewer tag "+id);
   var stack=new ItemStack(id.contains("air_jump")||id.contains("traveler")?Items.DIAMOND_BOOTS:Items.DIAMOND_CHESTPLATE);stack.enchant(h,1);check(EnchantmentHelper.getItemEnchantmentLevel(h,stack)==0,"normal application rejected "+id);
   var loot=server.reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE,Identifier.parse("armor_probe:hidden/"+id.replace(':','/'))));
   var params=new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN,Vec3.ZERO).create(LootContextParamSets.CHEST);
   for(int n=0;n<20;n++)for(var item:loot.getRandomItems(params,n))check(!item.getOrDefault(DataComponents.STORED_ENCHANTMENTS,ItemEnchantments.EMPTY).keySet().contains(h),"explicit random loot rejects "+id+" sample "+n);
  }
  CreativeModeTabs.tryRebuildTabContents(level.enabledFeatures(),true,level.registryAccess());
  for(String id:hidden){var h=registry.get(Identifier.parse(id)).orElseThrow();boolean found=false;for(var tab:CreativeModeTabs.allTabs())for(var item:tab.getSearchTabDisplayItems())if(item.getOrDefault(DataComponents.STORED_ENCHANTMENTS,ItemEnchantments.EMPTY).keySet().contains(h))found=true;check(!found,"creative book absent "+id);}
  String[][] blocked={{"minecraft:thorns","minecraft:fire_protection"},{"minecraft:thorns","minecraft:blast_protection"},{"minecraft:thorns","fangs_enchantment:fangs"},{"minecraft:frost_walker","minecraft:soul_speed"}};
  for(var pair:blocked)for(int order=0;order<2;order++){
   var a=registry.get(Identifier.parse(pair[order])).orElseThrow();var b=registry.get(Identifier.parse(pair[1-order])).orElseThrow();
   check(!Enchantment.areCompatible(a,b),"native incompatibility "+a.getRegisteredName()+" / "+b.getRegisteredName());
   var armor=new ItemStack(pair[0].contains("frost")?Items.DIAMOND_BOOTS:pair[1].contains("fangs")?Items.DIAMOND_LEGGINGS:Items.DIAMOND_CHESTPLATE);armor.enchant(a,1);
   var book=new ItemStack(Items.ENCHANTED_BOOK);var ench=new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);ench.set(b,1);book.set(DataComponents.STORED_ENCHANTMENTS,ench.toImmutable());
   var menu=new AnvilMenu(0,player.getInventory(),ContainerLevelAccess.create(level,BlockPos.ZERO));menu.getSlot(0).set(armor);menu.getSlot(1).set(book);menu.createResult();
   check(menu.getSlot(2).getItem().isEmpty(),"actual anvil rejects both orders "+a.getRegisteredName()+" / "+b.getRegisteredName());
  }
  for(var pair:List.of(new String[]{"minecraft:thorns","minecraft:protection"},new String[]{"minecraft:thorns","minecraft:unbreaking"},new String[]{"minecraft:frost_walker","minecraft:thorns"},new String[]{"minecraft:soul_speed","minecraft:thorns"},new String[]{"minecraft:fire_protection","fangs_enchantment:fangs"}))check(Enchantment.areCompatible(registry.get(Identifier.parse(pair[0])).orElseThrow(),registry.get(Identifier.parse(pair[1])).orElseThrow()),"unrelated compatibility retained "+Arrays.toString(pair));
  var allowed=new ItemStack(Items.DIAMOND_CHESTPLATE);allowed.enchant(registry.get(Identifier.parse("minecraft:protection")).orElseThrow(),1);var allowedBook=new ItemStack(Items.ENCHANTED_BOOK);var ae=new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);ae.set(registry.get(Identifier.parse("minecraft:thorns")).orElseThrow(),1);allowedBook.set(DataComponents.STORED_ENCHANTMENTS,ae.toImmutable());var goodMenu=new AnvilMenu(1,player.getInventory(),ContainerLevelAccess.create(level,BlockPos.ZERO));goodMenu.getSlot(0).set(allowed);goodMenu.getSlot(1).set(allowedBook);goodMenu.createResult();check(!goodMenu.getSlot(2).getItem().isEmpty(),"control: actual anvil still combines Protection with Thorns");
  Files.writeString(Path.of("result.txt"),"PASS "+checks);System.out.println("ARMOR_COMPLETE "+checks);server.halt(false);
 }catch(Throwable t){t.printStackTrace();try{Files.writeString(Path.of("result.txt"),"FAIL "+t);}catch(Exception ignored){}server.halt(false);}});}
}
