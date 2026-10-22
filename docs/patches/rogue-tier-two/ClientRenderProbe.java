package local.rogueclient;
import java.nio.file.*;
import net.minecraft.client.Minecraft;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
@Mod("rogue_render_probe") public class ClientRenderProbe {
 boolean done;
 public ClientRenderProbe(){NeoForge.EVENT_BUS.addListener(this::tick);}
 void tick(ClientTickEvent.Post event){if(done)return;done=true;var mc=Minecraft.getInstance();org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());String result;
  try{
   var unsafeField=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");unsafeField.setAccessible(true);var unsafe=(sun.misc.Unsafe)unsafeField.get(null);
   var rendererClass=Class.forName("net.rogues.client.entity.BearTrapEntityRenderer");var stateClass=Class.forName("net.rogues.client.entity.BearTrapEntityRenderer$State");
   var renderer=unsafe.allocateInstance(rendererClass);var state=stateClass.getConstructor().newInstance();var hidden=stateClass.getField("packHidden");hidden.setBoolean(state,true);
   java.lang.reflect.Method submit=null;for(var m:rendererClass.getDeclaredMethods())if(m.getName().equals("submit")&&m.getParameterTypes()[0]==stateClass)submit=m;
   if(submit==null)throw new AssertionError("Typed submit missing");submit.invoke(renderer,state,null,null,null);
   hidden.setBoolean(state,false);boolean visiblePath=false;try{submit.invoke(renderer,state,null,null,null);}catch(java.lang.reflect.InvocationTargetException e){visiblePath=e.getCause() instanceof NullPointerException;}
   if(!visiblePath)throw new AssertionError("Visible render path not reached");result="PASS actual NeoForge client verifies patched renderer/state; hidden submit skips all render work; visible submit reaches original render path";
  }catch(Throwable error){result="FAIL "+error;error.printStackTrace();}
  try{Files.writeString(mc.gameDirectory.toPath().resolve("rogue-render-result.txt"),result);}catch(Exception error){throw new RuntimeException(error);}mc.stop();
 }
}
