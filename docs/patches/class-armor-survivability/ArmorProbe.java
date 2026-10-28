package local.armorprobe;
import java.nio.file.*;
import java.util.*;
import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
@Mod("class_armor_probe")
public class ArmorProbe {
 final List<String> checks=new ArrayList<>();int failures;
 final Gson gson=new GsonBuilder().setPrettyPrinting().create();
 void check(boolean v,String s){checks.add((v?"PASS ":"FAIL ")+s);if(!v)failures++;}
 void near(double a,double b,String s){check(Math.abs(a-b)<.00001,s+" actual="+a+" expected="+b);}
 Item item(String id){return BuiltInRegistries.ITEM.getValue(Identifier.parse(id));}
 double attribute(ItemStack s,String id){double v=0;var attrs=s.get(DataComponents.ATTRIBUTE_MODIFIERS);if(attrs!=null)for(var e:attrs.modifiers())if(e.attribute().is(Identifier.parse(id)))v+=e.modifier().amount();return v;}
 List<String> preserved(ItemStack s,boolean target){var list=new ArrayList<String>();var attrs=s.get(DataComponents.ATTRIBUTE_MODIFIERS);if(attrs!=null)for(var e:attrs.modifiers()){String id=e.attribute().unwrapKey().orElseThrow().identifier().toString();if(target&&(id.equals("minecraft:armor")||id.equals("minecraft:armor_toughness")))continue;list.add(id+"|"+e.modifier().id()+"|"+e.modifier().amount()+"|"+e.modifier().operation()+"|"+e.slot());}Collections.sort(list);return list;}
 void equipment(ServerPlayer p)throws Exception{var m=LivingEntity.class.getDeclaredMethod("detectEquipmentUpdates");m.setAccessible(true);m.invoke(p);}
 public ArmorProbe(){NeoForge.EVENT_BUS.addListener(this::run);}
 void run(ServerStartedEvent event){var server=event.getServer();try{
  var policy=JsonParser.parseString(Files.readString(Path.of("policy.json"))).getAsJsonObject();boolean baseline=!Files.exists(Path.of("expected-items.json"));
  var prior=baseline?new JsonObject():JsonParser.parseString(Files.readString(Path.of("expected-items.json"))).getAsJsonObject();
  var fixtures=baseline?new JsonObject():JsonParser.parseString(Files.readString(Path.of("old-stacks.json"))).getAsJsonObject();var snapshots=new JsonObject();
  var ops=server.registryAccess().createSerializationContext(JsonOps.INSTANCE);
  var p=FakePlayerFactory.getMinecraft(server.overworld());var slots=List.of(EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET);String[] parts={"head","chest","legs","feet"};
  for(var nativeItem:BuiltInRegistries.ITEM){String id=BuiltInRegistries.ITEM.getKey(nativeItem).toString();if(!Set.of("wizards","rogues","archers","paladins","armory_rpgs").contains(Identifier.parse(id).getNamespace()))continue;
   var stack=new ItemStack(nativeItem);String set=id.replaceFirst("_(head|chest|legs|feet)$","");boolean target=policy.has(set)&&!set.equals(id);
   var snap=new JsonObject();snap.add("attributes",gson.toJsonTree(preserved(stack,target)));if(!target)snap.addProperty("durability",stack.getMaxDamage());
   snapshots.add(id,snap);if(!baseline)check(prior.has(id)&&prior.get(id).equals(snap),"preserved other modifiers/item durability "+id);
  }
  Files.writeString(Path.of("item-snapshot.json"),gson.toJson(snapshots));
  for(var row:policy.entrySet()){
   String set=row.getKey();var cfg=row.getValue().getAsJsonObject();var armor=cfg.getAsJsonArray("armor");int total=0;double tough=0;
   for(var slot:slots)p.setItemSlot(slot,ItemStack.EMPTY);equipment(p);
   for(int i=0;i<4;i++){
    String id=set+"_"+parts[i];var stack=new ItemStack(item(id));check(!stack.is(Items.AIR),"real target item "+id);int a=armor.get(i).getAsInt();int t=cfg.get("toughness_per_piece").getAsInt();
    near(attribute(stack,"minecraft:armor"),a,"native armor "+id);near(attribute(stack,"minecraft:armor_toughness"),t,"native toughness "+id);
    String[] vanilla={"helmet","chestplate","leggings","boots"};int max=new ItemStack(item("minecraft:"+cfg.get("durability").getAsString()+"_"+vanilla[i])).getMaxDamage();
    check(stack.getMaxDamage()==max,"native durability "+id+" actual="+stack.getMaxDamage()+" expected="+max);
    int toughEntries=0;for(var e:stack.get(DataComponents.ATTRIBUTE_MODIFIERS).modifiers())if(e.attribute().is(Identifier.parse("minecraft:armor_toughness")))toughEntries++;
    check(toughEntries<2,"no duplicate toughness "+id);
    p.setItemSlot(slots.get(i),stack);total+=a;tough+=t;
    if(baseline){var saved=stack.copy();saved.setDamageValue(50);saved.enchant(server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.UNBREAKING),3);fixtures.add(id,ItemStack.CODEC.encodeStart(ops,saved).getOrThrow());saved.setDamageValue(saved.getMaxDamage()-1);fixtures.add(id+"#nearbroken",ItemStack.CODEC.encodeStart(ops,saved).getOrThrow());}
    else {var loaded=ItemStack.CODEC.parse(ops,fixtures.get(id)).getOrThrow();check(loaded.getDamageValue()==50,"old worn stack keeps damage "+id);check(loaded.getEnchantments().getLevel(server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.UNBREAKING))==3,"old worn stack keeps enchantment "+id);check(loaded.getMaxDamage()==max&&loaded.getDamageValue()<max,"old stack receives valid new durability "+id);var fragileJson=fixtures.get(id+"#nearbroken");var fragile=ItemStack.CODEC.parse(ops,fragileJson).getOrThrow();check(!fragile.isEmpty(),"near-broken old stack survives loading "+id);check(fragile.get(DataComponents.DAMAGE)==fragileJson.getAsJsonObject().getAsJsonObject("components").get("minecraft:damage").getAsInt(),"near-broken old stack retains wear component "+id);}
   }
   equipment(p);near(p.getAttributeValue(Attributes.ARMOR),total,"equipped total armor "+set);near(p.getAttributeValue(Attributes.ARMOR_TOUGHNESS),tough,"equipped total toughness "+set);
  }
  if(baseline)Files.writeString(Path.of("old-stacks.json"),gson.toJson(fixtures));
 }catch(Throwable e){e.printStackTrace();check(false,"exception "+e);}finally{try{Files.write(Path.of("armor-checks.txt"),checks);Files.writeString(Path.of("armor-result.txt"),(failures==0?"PASS ":"FAIL ")+checks.size()+" checks; failures="+failures+"\n");}catch(Exception e){throw new RuntimeException(e);}server.halt(false);}}
}
