package local.utilitiesfoodclient;
import java.nio.file.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@Mod("utilities_food_client_probe")
public class ClientProbe {
 private int ticks;
 private boolean hidden;
 public ClientProbe(){NeoForge.EVENT_BUS.addListener(this::tick);}
 private void tick(ClientTickEvent.Post event){
  var mc=Minecraft.getInstance();
  if(!hidden){org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());hidden=true;}
  if(mc.gui.screen() instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen){mc.gui.setScreen(new TitleScreen());return;}
  if(!(mc.gui.screen() instanceof TitleScreen)||mc.gui.overlay()!=null)return;
  if(++ticks<20)return;
  try{Files.writeString(mc.gameDirectory.toPath().resolve("food-client-result.txt"),"PASS full distributed client pack reaches title screen with Utilities and Alex's Caves\n");}
  catch(Exception e){throw new RuntimeException(e);}
  mc.stop();
 }
}
