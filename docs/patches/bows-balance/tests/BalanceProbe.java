package local.bows;
import java.nio.file.*;
import java.util.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.bandit.many_bows.pack.PackPolicy;
import top.theillusivec4.curios.api.*;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

@Mod("bows_probe")
public class BalanceProbe {
 int checks;
 void check(boolean value,String label){if(!value)throw new AssertionError(label);checks++;System.out.println("BOWS_PASS "+label);}
 public BalanceProbe(){NeoForge.EVENT_BUS.addListener(this::started);}
 void started(ServerStartedEvent event){var server=event.getServer();server.execute(()->{
  try{
   var player=FakePlayerFactory.getMinecraft(server.overworld());
   for(var id:PackPolicy.REMOVED){
    var item=BuiltInRegistries.ITEM.getValue(Identifier.parse(id));check(item!=Items.AIR,"legacy registration "+id);
    String name=id.endsWith("stormbound_signet")?"StormboundSignetCurio":id.endsWith("sharpshot_ring")?"SharpshotRingCurio":"DeadEyesPendantCurio";
    var curio=(ICurioItem)Class.forName("net.bandit.many_bows.neoforge.curio."+name).getConstructor().newInstance();var stack=new ItemStack(item);
    check(curio.getDefaultCurioAttributeModifiers(stack).modifiers().isEmpty(),"no legacy modifiers "+id);
    check(!curio.canEquip(new SlotContext("ring",player,0,false,true),stack),"cannot equip "+id);
    check(!stack.is(net.minecraft.tags.ItemTags.create(Identifier.parse("curios:ring")))&&!stack.is(net.minecraft.tags.ItemTags.create(Identifier.parse("curios:necklace"))),"removed slot tags "+id);
   }
   var loot=Class.forName("net.bandit.many_bows.loot.ModLootModifiers").getDeclaredMethod("createConfigDrivenPool",float.class,List.class);loot.setAccessible(true);
   check(loot.invoke(null,1f,new ArrayList<>(PackPolicy.REMOVED))==null,"old config cannot inject removed loot");
   check(loot.invoke(null,1f,List.of("minecraft:apple"))!=null,"unrelated loot retained");
   var enchantments=server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
   for(var id:List.of("capacity","acceleration","bombard","deflector","frenzy","recycling","ricochet","zero_drop"))check(enchantments.get(Identifier.parse("crossarms:"+id)).isPresent(),"CrossArms enchantment "+id);
   var multi=enchantments.get(Identifier.parse("minecraft:multishot")).orElseThrow();var pierce=enchantments.get(Identifier.parse("minecraft:piercing")).orElseThrow();
   check(multi.value().getMaxLevel()==3,"CrossArms Multishot III preserved");
   check(Enchantment.areCompatible(multi,pierce),"Multishot and Piercing coexist");
   var capacity=enchantments.get(Identifier.parse("crossarms:capacity")).orElseThrow();check(!Enchantment.areCompatible(capacity,multi),"CrossArms Capacity exclusion preserved");
   var looting=enchantments.get(Identifier.parse("minecraft:looting")).orElseThrow();check(looting.value().isSupportedItem(new ItemStack(Items.CROSSBOW)),"Looting supports crossbow");check(looting.value().isSupportedItem(new ItemStack(Items.DIAMOND_SWORD)),"Looting still supports swords");
   CreativeModeTabs.tryRebuildTabContents(server.getWorldData().enabledFeatures(),true,server.registryAccess());
   check(BuiltInRegistries.CREATIVE_MODE_TAB.stream().flatMap(t->t.getDisplayItems().stream()).noneMatch(PackPolicy::removed),"removed from creative tabs");
   Files.writeString(Path.of("result.txt"),"PASS "+checks+" checks\n");
  }catch(Throwable t){t.printStackTrace();try{Files.writeString(Path.of("result.txt"),"FAIL "+t);}catch(Exception ignored){}}
  server.halt(false);
 });}
}
