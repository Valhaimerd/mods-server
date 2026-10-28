package local.armorclient;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import com.google.gson.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.puffish.skillsmod.api.*;
@Mod(value="class_armor_client_probe",dist=Dist.CLIENT)
public class ArmorClientProbe {
 final List<String> results=Collections.synchronizedList(new ArrayList<>());final AtomicReference<String> pending=new AtomicReference<>();
 List<Map.Entry<String,JsonElement>> sets;int ticks,phase,at,index;boolean hidden,done;
 String[] parts={"head","chest","legs","feet"};List<EquipmentSlot> slots=List.of(EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET);
 void check(boolean v,String text){results.add((v?"PASS ":"FAIL ")+text);}
 void near(double a,double b,String text){check(Math.abs(a-b)<.00001,text+" actual="+a+" expected="+b);}
 Item item(String id){return BuiltInRegistries.ITEM.getValue(Identifier.parse(id));}
 interface Work{void run(ServerPlayer p)throws Exception;}
 void server(Work work){pending.set("waiting");var mc=Minecraft.getInstance();mc.getSingleplayerServer().execute(()->{try{work.run(mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID()));pending.set("ready");}catch(Throwable e){e.printStackTrace();check(false,"server exception "+e);pending.set("failed");}});}
 void next(){phase++;at=ticks;}
 public ArmorClientProbe(){NeoForge.EVENT_BUS.addListener(this::tick);}
 void tick(ClientTickEvent.Post event){var mc=Minecraft.getInstance();if(!hidden){org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());mc.options.pauseOnLostFocus=false;hidden=true;}if(done||mc.player==null||mc.level==null)return;mc.mouseHandler.releaseMouse();ticks++;
 try{
  if(ticks>4500)throw new AssertionError("client timeout");
  if(phase==0){if(ticks<30)return;sets=new ArrayList<>(JsonParser.parseString(Files.readString(mc.gameDirectory.toPath().resolve("policy.json"))).getAsJsonObject().entrySet());server(p->{var c=SkillsAPI.getCategory(Identifier.parse("skill_tree_rpgs:class_skills")).orElseThrow();c.unlock(p);c.resetSkills(p);p.getInventory().clearContent();for(var slot:slots)p.setItemSlot(slot,ItemStack.EMPTY);p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);});next();return;}
  if("waiting".equals(pending.get())||ticks-at<15)return;if("failed".equals(pending.get())){finish();return;}
  switch(phase){
   case 1->{mc.setScreenAndShow(null);String id=sets.get(index).getKey();server(p->{for(int i=0;i<4;i++)p.setItemSlot(slots.get(i),new ItemStack(item(id+"_"+parts[i])));p.inventoryMenu.broadcastChanges();});next();}
   case 2->{var row=sets.get(index);String id=row.getKey();var cfg=row.getValue().getAsJsonObject();int total=0;double tough=0;String[] vanilla={"helmet","chestplate","leggings","boots"};
    for(int i=0;i<4;i++){var stack=mc.player.getItemBySlot(slots.get(i));check(stack.is(item(id+"_"+parts[i])),"synchronized equipped item "+id+"_"+parts[i]);int armor=cfg.getAsJsonArray("armor").get(i).getAsInt();total+=armor;tough+=cfg.get("toughness_per_piece").getAsInt();int max=new ItemStack(item("minecraft:"+cfg.get("durability").getAsString()+"_"+vanilla[i])).getMaxDamage();check(stack.getMaxDamage()==max,"client native durability "+id+"_"+parts[i]);
     var lines=stack.getTooltipLines(Item.TooltipContext.of(mc.level),mc.player,TooltipFlag.ADVANCED).stream().map(c->c.getString()).toList();check(lines.stream().anyMatch(s->s.contains("+"+armor+" Armor")),"native armor tooltip "+id+"_"+parts[i]+" "+lines);
    }
    near(mc.player.getAttributeValue(Attributes.ARMOR),total,"client equipped armor "+id);near(mc.player.getAttributeValue(Attributes.ARMOR_TOUGHNESS),tough,"client equipped toughness "+id);
    if(Set.of("wizards:netherite_arcane_robe","paladins:netherite_prior_robe","armory_rpgs:astral_robe","armory_rpgs:deathmantle_armor","armory_rpgs:onslaught_armor").contains(id)){
     mc.setScreenAndShow(new InventoryScreen(mc.player));for(String f:List.of("xpos","ypos")){var field=mc.mouseHandler.getClass().getDeclaredField(f);field.setAccessible(true);field.setDouble(mc.mouseHandler,f.equals("xpos")?424:288);}next();
    }else advance();
   }
   case 3->{Screenshot.grab(mc,false);results.add("SCREENSHOT "+sets.get(index).getKey());advance();}
  }
 }catch(Throwable e){e.printStackTrace();check(false,"client exception "+e);finish();}}
 void advance(){if(++index>=sets.size())finish();else{phase=1;at=ticks;}}
 void finish(){done=true;var mc=Minecraft.getInstance();try{Files.write(mc.gameDirectory.toPath().resolve("client-checks.txt"),results);Files.writeString(mc.gameDirectory.toPath().resolve("client-result.txt"),(results.stream().anyMatch(s->s.startsWith("FAIL"))?"FAIL":"PASS")+" "+results.stream().filter(s->s.startsWith("PASS")).count()+" checks\n");}catch(Exception e){throw new RuntimeException(e);}mc.stop();}
}
