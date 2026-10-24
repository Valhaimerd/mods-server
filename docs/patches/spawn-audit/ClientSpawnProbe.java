package local.spawnclient;
import java.nio.file.*;
import net.minecraft.client.Minecraft;
import net.neoforged.fml.common.Mod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import xyz.verarr.spreadspawnpoints.spawnpoints.SpawnPointManager;
import net.minecraft.nbt.TagParser;
@Mod(value="spawn_client_probe",dist=Dist.CLIENT)
public class ClientSpawnProbe {
 boolean hidden,done;int ticks;
 public ClientSpawnProbe(){NeoForge.EVENT_BUS.addListener(this::started);NeoForge.EVENT_BUS.addListener(this::tick);}
 void started(ServerStartedEvent e){try{
  var server=e.getServer();server.getCommands().getDispatcher().execute("spawnpoints generator set spreadspawnpoints:random false",server.createCommandSourceStack());
  SpawnPointManager.getInstance(server.overworld()).modifyGenerator(TagParser.parseCompoundFully("{lowerX:200,lowerZ:200,upperX:216,upperZ:216,seed:6789L}"));
 }catch(Exception ex){throw new RuntimeException(ex);}}
 void tick(ClientTickEvent.Post event){var mc=Minecraft.getInstance();if(!hidden){org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());hidden=true;}if(done||mc.player==null||mc.level==null||mc.getSingleplayerServer()==null||++ticks<40)return;done=true;
  mc.getSingleplayerServer().execute(()->{try{
   var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());var point=SpawnPointManager.getInstance(p.level()).getSpawnPoint(p.getUUID());
   if(point.x<200||point.x>216||point.y<200||point.y>216)throw new AssertionError("Bounds "+point);
   if(Math.abs(p.getX()-point.x)>17||Math.abs(p.getZ()-point.y)>17)throw new AssertionError("First join ignored assigned point: "+p.position()+" assigned="+point);
   Files.writeString(mc.gameDirectory.toPath().resolve("spawn-client-result.txt"),"PASS actual client first join at "+p.position()+" assigned="+point+"; mod alone with bundled dependency\n");
  }catch(Throwable t){t.printStackTrace();try{Files.writeString(mc.gameDirectory.toPath().resolve("spawn-client-result.txt"),"FAIL "+t);}catch(Exception ignored){}}
  mc.execute(mc::stop);});
 }
}
