package local.bowsclient;
import java.nio.file.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.bandit.many_bows.pack.PackPolicy;
import mezz.jei.api.constants.VanillaTypes;

@Mod("bows_client_probe")
public class ClientProbe {
 int ticks;boolean hidden;
 public ClientProbe(){NeoForge.EVENT_BUS.addListener(this::tick);}
 void tick(ClientTickEvent.Post event){
  var mc=Minecraft.getInstance();
  if(!hidden){long window=mc.getWindow().handle();org.lwjgl.glfw.GLFW.glfwSetKeyCallback(window,null);org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback(window,null);org.lwjgl.glfw.GLFW.glfwHideWindow(window);mc.options.pauseOnLostFocus=false;hidden=true;}
  if(mc.player==null||++ticks<80)return;
  var runtime=mezz.jei.common.Internal.getOptionalJeiRuntime();if(runtime.isEmpty()&&ticks<1200)return;
  try{
   var all=runtime.orElseThrow().getIngredientManager().getAllIngredients(VanillaTypes.ITEM_STACK);
   if(all.stream().anyMatch(PackPolicy::removed))throw new AssertionError("Removed accessories in JEI");
   if(all.stream().noneMatch(s->BuiltInRegistries.ITEM.getKey(s.getItem()).toString().equals("too_many_bows:wind_glove")))throw new AssertionError("Unrelated glove missing from JEI");
   for(var id:PackPolicy.REMOVED){
    var stack=new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(id)));
    var text=stack.getTooltipLines(Item.TooltipContext.of(mc.level),mc.player,TooltipFlag.NORMAL).stream().map(c->c.getString()).toList();
    if(text.stream().noneMatch(s->s.contains("legacy item has no effect")))throw new AssertionError("Stale tooltip "+id+text);
    if(text.stream().anyMatch(s->s.contains("Increases bow")))throw new AssertionError("Old bonus advertised "+id);
   }
   Files.writeString(mc.gameDirectory.toPath().resolve("bows-client-result.txt"),"PASS removed accessories absent from JEI; glove retained; all three legacy tooltips inert\n");
  }catch(Throwable t){t.printStackTrace();try{Files.writeString(mc.gameDirectory.toPath().resolve("bows-client-result.txt"),"FAIL "+t);}catch(Exception ignored){}}
  mc.stop();
 }
}
