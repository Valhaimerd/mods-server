package local.preview;
import java.nio.file.*;
import net.minecraft.client.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
@Mod("preview_probe")
public class ClientProbe {
 boolean isolated; int ticks; net.puffish.skillsmod.client.gui.SkillsScreen screen;
 public ClientProbe(){NeoForge.EVENT_BUS.addListener(this::tick);NeoForge.EVENT_BUS.addListener(this::opening);}
 void opening(net.neoforged.neoforge.client.event.ScreenEvent.Opening event){if(event.getNewScreen() instanceof net.puffish.skillsmod.client.gui.SkillsScreen s)screen=s;}
 void tick(ClientTickEvent.Post event){
  var mc=Minecraft.getInstance();
  if(!isolated){long w=mc.getWindow().handle();org.lwjgl.glfw.GLFW.glfwSetKeyCallback(w,null);org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback(w,null);org.lwjgl.glfw.GLFW.glfwHideWindow(w);mc.options.pauseOnLostFocus=false;isolated=true;}
  if(mc.player==null)return;mc.mouseHandler.releaseMouse();
  if(++ticks==40){var server=mc.getSingleplayerServer();if(server!=null)server.execute(()->{
   var player=server.getPlayerList().getPlayers().getFirst();
   net.puffish.skillsmod.api.SkillsAPI.getCategory(net.minecraft.resources.Identifier.parse("adventurer_skills:adventurer_skills")).orElseThrow().openScreen(player);
  });}
  if(ticks==60 && screen!=null){
   screen.mouseScrolled(210,150,0,-10);
   try{var f=screen.getClass().getDeclaredField("optActiveCategoryData");f.setAccessible(true);var cat=(net.puffish.skillsmod.client.data.ClientCategoryData)((java.util.Optional<?>)f.get(screen)).orElseThrow();
    for(var key:java.util.List.of("martial_prowess","hunter","magistrate","aegis","colossus","bulwark","packleader","guardian_stead","root_versatile")){
     var title=cat.getConfig().definitions().get(key).title();if(title.getStyle().getColor()==null || title.getStyle().getColor().getValue()!=0x5555ff)throw new AssertionError("Not blue: "+key);
    }
    if(!cat.getConfig().definitions().get("packleader").title().getString().equals("Pack Leader"))throw new AssertionError("Pack Leader title mismatch");
    if(!cat.getConfig().definitions().get("ranged_damage+3%").title().getString().equals("+2% Ranged Damage"))throw new AssertionError("Stale Adventurer ranged title");
    if(!cat.getConfig().definitions().get("hunter").description().getString().startsWith("+6% Ranged Damage"))throw new AssertionError("Stale Hunter description");
    var hunter=cat.getConfig().definitions().get("hunter").description().getString();if(hunter.contains("Luck")||hunter.contains("Jump")||!hunter.contains("+5% Draw Speed"))throw new AssertionError("Stale Hunter bonuses");
    if(!cat.getConfig().definitions().get("draw_speed").title().getString().startsWith("+5%"))throw new AssertionError("Stale draw speed");
    if(!cat.getConfig().definitions().get("caster_power").title().getString().startsWith("+3%"))throw new AssertionError("Stale ordinary spell power");
    var magistrate=cat.getConfig().definitions().get("magistrate").description().getString();if(!magistrate.startsWith("+10%")||!magistrate.contains("+5% Magic Resistance"))throw new AssertionError("Stale Magistrate description");
    var dataField=screen.getClass().getDeclaredField("data");dataField.setAccessible(true);
    var data=(net.puffish.skillsmod.client.data.ClientSkillScreenData)dataField.get(screen);
    for(var categoryName:java.util.List.of("class_skills","weapon_skills")){
     var cfg=data.getCategory(net.minecraft.resources.Identifier.parse("skill_tree_rpgs:"+categoryName)).orElseThrow().getConfig();
     for(var key:java.util.List.of("warrior_root","warrior_boost","archer_root","archer_boost")){
      var text=cfg.definitions().get(key).description().getString();
      if(!text.equals((key.startsWith("archer") ? "+2% Ranged Damage":"+3% Attack Damage")))throw new AssertionError("Stale tooltip "+categoryName+"/"+key+": "+text);
     }
     for(var key:java.util.List.of("rogue_root","rogue_boost"))if(!cfg.definitions().get(key).description().getString().equals("+2% Attack Speed"))throw new AssertionError("Stale assassin description");
     for(var school:java.util.List.of("arcane","fire","frost"))for(var suffix:java.util.List.of("root","boost"))if(!cfg.definitions().get(school+"_"+suffix).description().getString().startsWith("+3%"))throw new AssertionError("Stale school power description");
     if(!cfg.definitions().get("weapon_bow_root").description().getString().startsWith("+2% Ranged Damage"))throw new AssertionError("Stale conditional bow description "+categoryName);
    }
    Files.writeString(mc.gameDirectory.toPath().resolve("rpg-tooltip-result.txt"),"PASS October 20: Hunter6/projectile25/haste5 withoutLuckJump; Magistrate10/resistance5; ordinary ranged2/melee3/projectile25/draw5/power3; RPG ranged2/melee3/assassin2/power3; blue titles");
    Files.writeString(mc.gameDirectory.toPath().resolve("title-check.txt"),"PASS 9 blue client title components");
   }catch(Exception ex){throw new RuntimeException(ex);}
  }
  if(ticks==100){Screenshot.grab(mc,false);try{Files.writeString(mc.gameDirectory.toPath().resolve("skill-screen.txt"),"captured Adventurer screen");}catch(Exception e){throw new RuntimeException(e);}}
  if(ticks==130)mc.stop();
 }
}
