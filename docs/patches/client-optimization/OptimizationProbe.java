package local.optimization;
import java.nio.file.*;import java.util.*;
import net.minecraft.client.Minecraft;import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.api.distmarker.Dist;import net.neoforged.fml.ModList;import net.neoforged.fml.common.Mod;import net.neoforged.neoforge.common.NeoForge;import net.neoforged.neoforge.client.event.RenderFrameEvent;
@Mod(value="spatial_client_probe",dist=Dist.CLIENT) public class OptimizationProbe {
 final List<String> checks=new ArrayList<>();boolean hidden,done,title;int phase;long since;java.util.concurrent.CompletableFuture<?> reload;
 public OptimizationProbe(){NeoForge.EVENT_BUS.addListener(this::tick);NeoForge.EVENT_BUS.addListener(this::opening);}
 void opening(net.neoforged.neoforge.client.event.ScreenEvent.Opening event){title=event.getNewScreen() instanceof TitleScreen;System.out.println("OPTIMIZATION_PROBE screen="+event.getNewScreen());}
 void check(boolean ok,String label){checks.add((ok?"PASS ":"FAIL ")+label);if(!ok)throw new AssertionError(label);}
 void finish(){done=true;var mc=Minecraft.getInstance();try{Files.write(mc.gameDirectory.toPath().resolve("spatial-client-checks.txt"),checks);Files.writeString(mc.gameDirectory.toPath().resolve("spatial-client-result.txt"),checks.stream().anyMatch(s->s.startsWith("FAIL"))?"FAIL":"PASS");}catch(Exception e){throw new RuntimeException(e);}mc.stop();}
 void tick(RenderFrameEvent.Post event){if(done)return;var mc=Minecraft.getInstance();try{
  if(!hidden&&mc.getWindow()!=null){org.lwjgl.glfw.GLFW.glfwSetWindowPos(mc.getWindow().handle(),-30000,-30000);hidden=true;}
  if(phase==0&&title){
   for(var id:List.of("sodium","ferritecore","moreculling","immediatelyfast","entityculling","dynamic_fps","modernfix"))check(ModList.get().isLoaded(id),"Loaded compatible optimizer: "+id);
   check(!ModList.get().isLoaded("vectors"),"Vectors absent from complete client pack");check(ModList.get().getMods().size()>300,"Complete pack, not only an optimizer fixture");
   check(mc.getTextureManager()!=null&&mc.getEntityRenderDispatcher()!=null,"Native texture and entity renderer initialization");
   reload=mc.reloadResourcePacks();phase=1;return;
  }
  if(phase==1&&reload.isDone()){reload.join();check(true,"Complete-pack resource reload succeeds with combined optimizer stack");since=System.currentTimeMillis();phase=2;return;}
  if(phase==2&&System.currentTimeMillis()-since>5000){check(title,"Client remains responsive after reload");finish();}
 }catch(Throwable t){t.printStackTrace();checks.add("FAIL "+t);finish();}}
}
