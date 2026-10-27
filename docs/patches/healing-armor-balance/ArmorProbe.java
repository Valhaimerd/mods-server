package local.healingarmor;
import java.nio.file.*;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
@Mod("healing_armor_probe")
public class ArmorProbe {
 static final List<String> SETS=List.of("paladins:paladin_armor","paladins:crusader_armor","paladins:netherite_crusader_armor","armory_rpgs:justicar_armor","armory_rpgs:lightbringer_armor");
 List<String> checks=new ArrayList<>();
 void check(boolean value,String text){if(!value)throw new AssertionError(text);checks.add("PASS "+text);}
 public ArmorProbe(){NeoForge.EVENT_BUS.addListener(this::run);}
 void run(ServerStartedEvent e){try{
  var gson=new Gson();var all=new TreeMap<String,List<String>>();var nonHealing=new TreeMap<String,List<String>>();var amounts=new TreeMap<String,Double>();
  for(var item:BuiltInRegistries.ITEM){
   String id=BuiltInRegistries.ITEM.getKey(item).toString();if(!id.startsWith("paladins:")&&!id.startsWith("armory_rpgs:"))continue;
   boolean targeted=SETS.stream().anyMatch(s->id.startsWith(s+"_"));var attributes=item.getDefaultInstance().get(DataComponents.ATTRIBUTE_MODIFIERS);if(attributes==null)continue;
   var values=new ArrayList<String>();var preserved=new ArrayList<String>();
   for(var entry:attributes.modifiers()){
    var modifier=entry.modifier();String attribute=entry.attribute().unwrapKey().orElseThrow().identifier().toString();
    String value=attribute+"|"+modifier.id()+"|"+modifier.amount()+"|"+modifier.operation()+"|"+entry.slot();values.add(value);
    if(targeted&&attribute.equals("spell_power:healing")&&modifier.operation()==Operation.ADD_VALUE)amounts.merge(id,modifier.amount(),Double::sum);else preserved.add(value);
   }
   Collections.sort(values);Collections.sort(preserved);all.put(id,values);nonHealing.put(id,preserved);
  }
  Files.writeString(Path.of("armor-attributes.json"),gson.toJson(all));Files.writeString(Path.of("preserved-attributes.json"),gson.toJson(nonHealing));
  check(amounts.size()==20,"all twenty plate pieces registered");
  for(var entry:amounts.entrySet())check(Math.abs(entry.getValue()-.25)<.00001,"flat healing is 0.25: "+entry.getKey()+" actual="+entry.getValue());
  for(var set:SETS)check(Math.abs(amounts.entrySet().stream().filter(x->x.getKey().startsWith(set+"_")).mapToDouble(Map.Entry::getValue).sum()-1)<.00001,"full set grants one flat Healing Power: "+set);
  if(Files.exists(Path.of("expected-preserved.json"))){var expected=gson.<Map<String,List<String>>>fromJson(Files.readString(Path.of("expected-preserved.json")),new TypeToken<Map<String,List<String>>>(){}.getType());check(expected.equals(nonHealing),"all other native item attribute modifiers remain identical");}
  check(all.entrySet().stream().anyMatch(x->x.getKey().startsWith("armory_rpgs:avatar_robe_")&&x.getValue().stream().anyMatch(v->v.startsWith("spell_power:healing|")&&v.contains("|0.3499999940395355|ADD_MULTIPLIED_BASE"))),"Avatar percentage Healing Power retained");
  check(all.entrySet().stream().anyMatch(x->x.getKey().startsWith("armory_rpgs:absolution_robe_")&&x.getValue().stream().anyMatch(v->v.startsWith("spell_power:healing|")&&v.contains("|0.3499999940395355|ADD_MULTIPLIED_BASE"))),"Absolution percentage Healing Power retained");
  Files.writeString(Path.of("armor-result.txt"),"PASS "+checks.size()+" native armor checks\n");
 }catch(Throwable error){error.printStackTrace();try{Files.writeString(Path.of("armor-result.txt"),"FAIL "+error);}catch(Exception ignored){}}finally{try{Files.write(Path.of("armor-checks.txt"),checks);}catch(Exception ignored){}e.getServer().halt(false);}}
}
