package local.netheriterepairs;
import java.nio.file.*;
import java.util.*;
import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.inventory.AnvilMenu;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import archives.tater.penchant.component.EnchantmentProgress;
import archives.tater.penchant.registry.PenchantComponents;
import archives.tater.penchant.PenchantmentDefinition;
import archives.tater.penchant.util.PenchantmentHelper;

@Mod("netherite_repair_probe")
public class Probe {
 private final List<String> checks=new ArrayList<>();
 public Probe(){NeoForge.EVENT_BUS.addListener(this::run);}
 private void check(boolean value,String label){if(!value)throw new AssertionError(label);checks.add("PASS "+label);}
 private boolean target(String id){var parts=id.split(":");return Set.of("wizards","paladins","rogues","archers").contains(parts[0])&&Arrays.asList(parts[1].split("_")).contains("netherite");}
 private String snapshot(ItemStack stack){var repair=stack.get(DataComponents.REPAIRABLE);return repair==null?"none":repair.items().stream().map(h->BuiltInRegistries.ITEM.getKey(h.value()).toString()).sorted().toList().toString();}
 private void run(ServerStartedEvent event){var server=event.getServer();String result;
  try{
   boolean baseline=Files.exists(Path.of("baseline"));
   var ops=server.registryAccess().createSerializationContext(JsonOps.INSTANCE);
   var prior=baseline?new JsonObject():JsonParser.parseString(Files.readString(Path.of("baseline-items.json"))).getAsJsonObject();
   var saved=baseline?new JsonObject():JsonParser.parseString(Files.readString(Path.of("baseline-stacks.json"))).getAsJsonObject();
   var snapshot=new JsonObject();var names=new ArrayList<String>();
   var player=net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(server.overworld());
   var unbreaking=server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.UNBREAKING);
   PenchantmentDefinition.buildCache(server.registryAccess());
   if(Files.exists(Path.of("expected-module.txt"))){boolean expected=Boolean.parseBoolean(Files.readString(Path.of("expected-module.txt")));check(server.getPackRepository().getSelectedIds().stream().anyMatch(s->s.contains("durability_rework"))==expected,"requested Durability Rework module state "+expected);}
   check(PenchantmentHelper.getMaxLevel(unbreaking)==10,"server-authoritative progression maximum is X");
   for(var item:BuiltInRegistries.ITEM){
    var id=BuiltInRegistries.ITEM.getKey(item).toString();
    if(!Set.of("wizards","paladins","rogues","archers","armory_rpgs").contains(id.split(":")[0]))continue;
    var stack=new ItemStack(item);snapshot.addProperty(id,snapshot(stack));
    if(!target(id)){
     if(!baseline)check(prior.get(id).getAsString().equals(snapshot(stack)),"non-netherite/legendary repair unchanged "+id);
     continue;
    }
    names.add(id);
    if(baseline){stack.setDamageValue(100);saved.add(id,ItemStack.CODEC.encodeStart(ops,stack).getOrThrow());continue;}
    var repair=stack.get(DataComponents.REPAIRABLE);
    check(repair!=null&&repair.isValidRepairItem(new ItemStack(Items.NETHERITE_SCRAP))&&!repair.isValidRepairItem(new ItemStack(Items.NETHERITE_INGOT)),"scraps replace ingots "+id);
    stack=ItemStack.CODEC.parse(ops,saved.get(id)).getOrThrow();
    check(stack.getDamageValue()==100&&stack.get(DataComponents.REPAIRABLE).isValidRepairItem(new ItemStack(Items.NETHERITE_SCRAP)),"saved gear retains wear and picks up new default "+id);
    var anvil=new AnvilMenu(1,player.getInventory());anvil.getSlot(0).set(stack.copy());anvil.getSlot(1).set(new ItemStack(Items.NETHERITE_SCRAP));
    check(!anvil.getSlot(2).getItem().isEmpty()&&anvil.getSlot(2).getItem().getDamageValue()<100,"native anvil repairs with scraps "+id);
    anvil.getSlot(1).set(new ItemStack(Items.NETHERITE_INGOT));check(anvil.getSlot(2).getItem().isEmpty(),"native anvil rejects ingots "+id);
   }
   Files.writeString(Path.of("item-snapshot.json"),snapshot.toString());Files.writeString(Path.of("target-items.json"),new Gson().toJson(names));
   if(baseline)Files.writeString(Path.of("saved-stacks.json"),saved.toString());
   // Reproduce the reported complete VIII bar on actual worn RPG gear, not just vanilla tools.
   for(String id:List.of("paladins:netherite_prior_robe_legs","paladins:prior_robe_legs","wizards:netherite_arcane_robe_chest","rogues:netherite_berserker_armor_chest","archers:netherite_ranger_armor_chest")){
    var item=BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.parse(id));var stack=new ItemStack(item);stack.enchant(unbreaking,8);
    var slot=id.endsWith("legs")?EquipmentSlot.LEGS:EquipmentSlot.CHEST;player.setItemSlot(slot,stack);
    for(int current=8;current<10;current++){
     int required=EnchantmentProgress.getMaxProgress(unbreaking,current,stack);var pending=EnchantmentProgress.EMPTY.toMutable();pending.setProgress(unbreaking,required);
     stack.set(PenchantComponents.ENCHANTMENT_PROGRESS,pending.toImmutable());player.tickCount=20;
     player.getInventory().tick();
     var equipment=net.minecraft.world.entity.LivingEntity.class.getDeclaredField("equipment");equipment.setAccessible(true);
     ((net.minecraft.world.entity.EntityEquipment)equipment.get(player)).tick(player);
     check(stack.getEnchantments().getLevel(unbreaking)==current+1,"actual worn full bar "+current+" advances to "+(current+1)+" "+id);
    }
    player.setItemSlot(slot,ItemStack.EMPTY);
   }
   check(names.size()==53,"all 53 netherite RPG items inventoried");
   result="PASS "+checks.size()+" native repair/progression checks";
  }catch(Throwable t){t.printStackTrace();result="FAIL "+t;}
  try{Files.writeString(Path.of("result.txt"),result);Files.write(Path.of("checks.txt"),checks);}catch(Exception t){throw new RuntimeException(t);}server.halt(false);
 }
}
