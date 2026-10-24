package local.racebalance;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import com.eiden.iourusraces.race.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.GameType;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
@Mod("race_fall_probe")
public class ClientFallProbe {
 int ticks,landed=-1;boolean hidden,done;double maxDown;float initialHp;AtomicReference<String> status=new AtomicReference<>();List<String> trace=new ArrayList<>();
 public ClientFallProbe(){NeoForge.EVENT_BUS.addListener(this::tick);}
 void tick(ClientTickEvent.Post event){
  var mc=Minecraft.getInstance();if(!hidden){org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());hidden=true;}
  if(done||mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;ticks++;
  try{
   if(ticks==20){mc.setScreenAndShow(null);mc.getSingleplayerServer().execute(()->{try{
    var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
    PlayerRaceData.setRace(p,Race.ELF);RaceAttributes.applyRaceAttributes(p,Race.ELF);p.setGameMode(GameType.SURVIVAL);p.getAbilities().flying=false;p.getAbilities().mayfly=false;p.onUpdateAbilities();p.removeAllEffects();
    for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++)p.level().setBlock(new BlockPos(x,80,z),Blocks.STONE.defaultBlockState(),3);
    p.teleportTo(.5,101,.5);p.setDeltaMovement(0,0,0);p.setHealth(p.getMaxHealth());initialHp=p.getHealth();status.set("ready");
   }catch(Throwable e){status.set("FAIL "+e);}});}
   if(ticks>25&&"ready".equals(status.get())){
    maxDown=Math.max(maxDown,-mc.player.getDeltaMovement().y);
    trace.add(ticks+" clientY="+mc.player.getY()+" vy="+mc.player.getDeltaMovement().y+" fall="+mc.player.fallDistance+" hp="+mc.player.getHealth());
    if(mc.player.onGround()&&mc.player.getY()<83&&landed<0)landed=ticks;
   }
   if(ticks==210){
    if(!"ready".equals(status.get()))throw new AssertionError(status.get());
    if(landed<0)throw new AssertionError("Never landed");
    if(landed<90)throw new AssertionError("Elf fell too quickly: landed tick="+landed+" maxDown="+maxDown);
    if(mc.player.getHealth()<initialHp-.01)throw new AssertionError("Elf took fall damage: "+mc.player.getHealth()+" initial="+initialHp);
    result("PASS connected Elf slow fall; landed tick="+landed+" maxDown="+maxDown+" health="+mc.player.getHealth());
   }
  }catch(Throwable t){result("FAIL "+t);}
 }
 void result(String text){done=true;var mc=Minecraft.getInstance();try{Files.writeString(mc.gameDirectory.toPath().resolve("fall-result.txt"),text);Files.write(mc.gameDirectory.toPath().resolve("fall-trace.txt"),trace);}catch(Exception e){throw new RuntimeException(e);}mc.stop();}
}
