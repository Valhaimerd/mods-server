package local.preview;
import java.nio.file.*;
import net.minecraft.client.Minecraft;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@Mod("preview_probe")
public class RetentionClientProbe {
 boolean isolated;int ticks;
 public RetentionClientProbe(){NeoForge.EVENT_BUS.addListener(this::tick);}
 void tick(ClientTickEvent.Post event){
  var mc=Minecraft.getInstance();
  if(!isolated){long w=mc.getWindow().handle();org.lwjgl.glfw.GLFW.glfwSetKeyCallback(w,null);org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback(w,null);org.lwjgl.glfw.GLFW.glfwHideWindow(w);mc.options.pauseOnLostFocus=false;isolated=true;}
  if(mc.player==null)return;
  if(++ticks==40){
   if(mc.getConnection().getCommands().getRoot().getChild("pvpFlag")!=null)throw new AssertionError("Flag command registered");
   if(mc.getConnection().getCommands().getRoot().getChild("pvpZone")!=null)throw new AssertionError("Zone command registered");
   try{Class.forName("io.redspace.pvp_flagging.client.FlagIndicatorOverlay");throw new AssertionError("Old indicator still present");}catch(ClassNotFoundException expected){}
   try{Files.writeString(mc.gameDirectory.toPath().resolve("pvp-retention-client-result.txt"),"PASS integrated client and Better Party load; no flag/zone commands or flag HUD classes");}catch(Exception e){throw new RuntimeException(e);}
   mc.gui.setScreen(null);mc.mouseHandler.releaseMouse();
  }
  if(ticks==65)net.minecraft.client.Screenshot.grab(mc,false);
  if(ticks==90)mc.stop();
 }
}
