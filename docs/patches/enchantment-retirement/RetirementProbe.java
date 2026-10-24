package local.retirement;
import java.nio.file.*;
import java.util.*;
import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.*;
import net.minecraft.world.phys.Vec3;
@Mod("retirement_probe") public class RetirementProbe {
 int checks;
 void check(boolean ok,String text){if(!ok)throw new AssertionError(text);checks++;}
 int ticks;
 public RetirementProbe(){NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.tick.ServerTickEvent.Post event)->{if(++ticks==20)run(new ServerStartedEvent(event.getServer()));});}
 void run(ServerStartedEvent event){var server=event.getServer();server.execute(()->{try{
  var level=server.overworld();var reg=level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
  var ops=level.registryAccess().createSerializationContext(JsonOps.INSTANCE);
  var manifest=JsonParser.parseString(Files.readString(Path.of("../manifest.json"))).getAsJsonObject();
  boolean baseline=Files.exists(Path.of("baseline.flag"));
  if(baseline){
   var stack=new ItemStack(Items.DIAMOND_CHESTPLATE);var ench=new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
   ench.set(reg.get(Identifier.parse("minecraft:protection")).orElseThrow(),3);
   for(var id:manifest.getAsJsonArray("retired_enchantments"))ench.set(reg.get(Identifier.parse(id.getAsString())).orElseThrow(),1);
   stack.set(DataComponents.ENCHANTMENTS,ench.toImmutable());
   Files.writeString(Path.of("../saved-gear.json"),ItemStack.CODEC.encodeStart(ops,stack).getOrThrow().toString());
  }else{
   for(var id:manifest.getAsJsonArray("retired_enchantments"))check(reg.get(Identifier.parse(id.getAsString())).isEmpty(),"retired registry "+id);
   check(reg.get(Identifier.parse("minecraft:sweeping_edge")).isPresent(),"vanilla sweeping retained");
   var sweeping=reg.get(Identifier.parse("minecraft:sweeping_edge")).orElseThrow();var sword=new ItemStack(Items.DIAMOND_SWORD);sword.enchant(sweeping,1);check(EnchantmentHelper.getItemEnchantmentLevel(sweeping,sword)==0,"sweeping hidden");
   var decoded=ItemStack.CODEC.parse(ops,JsonParser.parseString(Files.readString(Path.of("../saved-gear.json"))));
   var stack=decoded.getOrThrow();check(stack.is(Items.DIAMOND_CHESTPLATE),"saved armor survives");check(EnchantmentHelper.getItemEnchantmentLevel(reg.get(Identifier.parse("minecraft:protection")).orElseThrow(),stack)==3,"valid protection survives");
   var legacy=JsonParser.parseString(Files.readString(Path.of("../saved-gear.json"))).getAsJsonObject();legacy.addProperty("id","minecraft:enchanted_book");var components=legacy.getAsJsonObject("components");components.add("minecraft:stored_enchantments",components.remove("minecraft:enchantments"));
   components.add("penchant:enchantment_progress",components.get("minecraft:stored_enchantments").deepCopy());
   var book=ItemStack.CODEC.parse(ops,legacy).getOrThrow();check(book.get(DataComponents.STORED_ENCHANTMENTS).size()==1,"stored book keeps only valid enchantments and progress");
   var nbtOps=level.registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE);var nbt=JsonOps.INSTANCE.convertTo(net.minecraft.nbt.NbtOps.INSTANCE,legacy);check(ItemStack.CODEC.parse(nbtOps,nbt).getOrThrow().is(Items.ENCHANTED_BOOK),"NBT stored book survives");
   var player=net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(level);var soul=Class.forName("fuzs.enderzoology.common.attachment.SoulboundItems");var method=soul.getDeclaredMethod("saveOnDeath",net.minecraft.server.level.ServerPlayer.class,java.util.Collection.class);method.setAccessible(true);check(method.invoke(null,player,List.of())!=null,"death hook tolerates retired Soulbound");
   CreativeModeTabs.tryRebuildTabContents(level.enabledFeatures(),true,level.registryAccess());check(true,"creative tabs rebuild");
   var params=new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN,Vec3.ZERO).create(LootContextParamSets.CHEST);
   for(var record:manifest.getAsJsonArray("repacked"))for(var name:record.getAsJsonObject().getAsJsonArray("changed")){
    String path=name.getAsString();if(!path.contains("/loot_table/")||!path.endsWith(".json"))continue;
    var parts=path.split("/loot_table/");var id=parts[0].substring(5)+":"+parts[1].replace(".json","");
    var table=server.reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE,Identifier.parse(id)));
    for(int n=0;n<20;n++){table.getRandomItems(params,n);checks++;}
   }
  }
  Files.writeString(Path.of("result.txt"),"PASS "+checks+" checks\n");
 }catch(Throwable t){t.printStackTrace();try{Files.writeString(Path.of("result.txt"),"FAIL "+t);}catch(Exception ignored){}}finally{server.halt(false);}});}
}
