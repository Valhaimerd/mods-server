package local.balanceclient;
import java.nio.file.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import com.cozary.nameless_trinkets.pack.PackBalance;
import mezz.jei.api.constants.VanillaTypes;
@Mod("balance_client_probe")
public class ClientProbe {
 int ticks; boolean hidden;
 public ClientProbe(){NeoForge.EVENT_BUS.addListener(this::tick);}
 void tick(ClientTickEvent.Post event){
  var mc=Minecraft.getInstance();
  if(!hidden){long w=mc.getWindow().handle();org.lwjgl.glfw.GLFW.glfwSetKeyCallback(w,null);org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback(w,null);org.lwjgl.glfw.GLFW.glfwHideWindow(w);mc.options.pauseOnLostFocus=false;hidden=true;}
  if(mc.player==null)return;
  if(++ticks<80)return;
  var runtime=mezz.jei.common.Internal.getOptionalJeiRuntime();
  if(runtime.isEmpty() && ticks<1200)return;
  try{
   var manager=runtime.orElseThrow().getIngredientManager();
   var all=manager.getAllIngredients(VanillaTypes.ITEM_STACK);
   var invalid=all.stream().filter(s->BuiltInRegistries.ITEM.getKey(s.getItem()).getNamespace().equals("nameless_trinkets")&&PackBalance.removed(BuiltInRegistries.ITEM.getKey(s.getItem()).getPath())).toList();
   if(!invalid.isEmpty())throw new AssertionError("Removed items in JEI: "+invalid);
   var kept=all.stream().filter(s->BuiltInRegistries.ITEM.getKey(s.getItem()).toString().equals("nameless_trinkets:titans_mark")).count();
   if(kept!=1)throw new AssertionError("Titan missing from JEI");
   Files.writeString(mc.gameDirectory.toPath().resolve("client-result.txt"),"PASS: removed relics absent from JEI; Titan retained\n");
  }catch(Throwable ex){ex.printStackTrace();try{Files.writeString(mc.gameDirectory.toPath().resolve("client-result.txt"),"FAIL "+ex);}catch(Exception ignored){}}
  mc.stop();
 }
}
