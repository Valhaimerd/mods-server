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
    Files.writeString(mc.gameDirectory.toPath().resolve("title-check.txt"),"PASS 9 blue client title components");
   }catch(Exception ex){throw new RuntimeException(ex);}
  }
  if(ticks==100){Screenshot.grab(mc,false);try{Files.writeString(mc.gameDirectory.toPath().resolve("skill-screen.txt"),"captured Adventurer screen");}catch(Exception e){throw new RuntimeException(e);}}
  if(ticks==130)mc.stop();
 }
}
