package local.elfslowfall;
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
 int ticks,landed=-1;boolean hidden,done;int hops;boolean wasGrounded=true;double maxDown;float initialHp;AtomicReference<String> status=new AtomicReference<>();List<String> trace=new ArrayList<>();
 public ClientFallProbe(){NeoForge.EVENT_BUS.addListener(this::tick);}
 void tick(ClientTickEvent.Post event){
  var mc=Minecraft.getInstance();if(!hidden){org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());hidden=true;}
  if(done||mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;ticks++;
  try{
   if(ticks==20){mc.setScreenAndShow(null);mc.getSingleplayerServer().execute(()->{try{
    var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
    PlayerRaceData.setRace(p,Race.ELF);RaceAttributes.applyRaceAttributes(p,Race.ELF);
    p.setGameMode(GameType.SURVIVAL);p.getAbilities().flying=false;
    p.setOnGround(false);
    for(double distance:new double[]{0,.25,1,1.9,2.49}){
     p.fallDistance=distance;p.setDeltaMovement(.3,-.8,.4);p.hurtMarked=false;
     RaceBalanceRules.slowFall(p);
     if(p.getDeltaMovement().y!=-.8||p.hurtMarked)throw new AssertionError("Short jump altered at "+distance);
    }
    for(double distance:new double[]{2.5,3,20}){
     p.fallDistance=distance;p.setDeltaMovement(.3,-.8,.4);p.hurtMarked=false;RaceBalanceRules.slowFall(p);
     if(p.getDeltaMovement().y!=-.2||p.getDeltaMovement().x!=.3||p.getDeltaMovement().z!=.4||!p.hurtMarked||p.fallDistance>=3)throw new AssertionError("Long fall not safely capped at "+distance);
     RaceBalanceRules.slowFall(p);if(p.getDeltaMovement().y!=-.2)throw new AssertionError("Slow fall oscillated");
    }
    p.setOnGround(true);p.fallDistance=0;p.setDeltaMovement(0,-.8,0);p.hurtMarked=false;RaceBalanceRules.slowFall(p);
    if(p.getDeltaMovement().y!=-.8||p.hurtMarked)throw new AssertionError("Landed player altered");
    p.setOnGround(false);p.fallDistance=1;p.setDeltaMovement(0,-.8,0);RaceBalanceRules.slowFall(p);
    if(p.getDeltaMovement().y!=-.8)throw new AssertionError("Protection leaked into next hop");
    p.fallDistance=20;p.setDeltaMovement(0,.4,0);RaceBalanceRules.slowFall(p);if(p.getDeltaMovement().y!=.4)throw new AssertionError("Upward movement altered");
    p.getAbilities().flying=true;p.setDeltaMovement(0,-.8,0);RaceBalanceRules.slowFall(p);if(p.getDeltaMovement().y!=-.8)throw new AssertionError("Creative flight altered");
    trace.add("PASS short-fall/threshold/continuation/landing/re-hop/upward/flight checks");
    p.getAbilities().flying=false;p.getAbilities().mayfly=false;p.onUpdateAbilities();p.removeAllEffects();
    for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++)p.level().setBlock(new BlockPos(x,80,z),Blocks.STONE.defaultBlockState(),3);
    p.teleportTo(.5,81,.5);p.setDeltaMovement(0,0,0);p.fallDistance=0;

   }catch(Throwable e){status.set("FAIL "+e);}});}
   if(status.get()!=null&&status.get().startsWith("FAIL")){result(status.get());return;}
   if(ticks==40)mc.options.keyJump.setDown(true);
   if(ticks>40&&ticks<95){
    boolean ground=mc.player.onGround();if(wasGrounded&&!ground)hops++;wasGrounded=ground;
    if(!ground&&Math.abs(mc.player.getDeltaMovement().y+.2)<.000001)throw new AssertionError("Bunny hop capped by slow fall");
   }
   if(ticks==95){mc.options.keyJump.setDown(false);if(hops<2)throw new AssertionError("Bunny hop fixture did not jump repeatedly: "+hops);trace.add("PASS "+hops+" actual connected-client bunny hops without slow-fall cap");}
   if(ticks==100){mc.setScreenAndShow(null);mc.getSingleplayerServer().execute(()->{try{
    var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
    PlayerRaceData.setRace(p,Race.ELF);RaceAttributes.applyRaceAttributes(p,Race.ELF);p.setGameMode(GameType.SURVIVAL);p.getAbilities().flying=false;p.getAbilities().mayfly=false;p.onUpdateAbilities();p.removeAllEffects();
    for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++)p.level().setBlock(new BlockPos(x,80,z),Blocks.STONE.defaultBlockState(),3);
    p.teleportTo(.5,101,.5);p.setDeltaMovement(0,0,0);p.setHealth(p.getMaxHealth());initialHp=p.getHealth();status.set("ready");
   }catch(Throwable e){status.set("FAIL "+e);}});}
   if(ticks>105&&"ready".equals(status.get())){
    maxDown=Math.max(maxDown,-mc.player.getDeltaMovement().y);
    trace.add(ticks+" clientY="+mc.player.getY()+" vy="+mc.player.getDeltaMovement().y+" fall="+mc.player.fallDistance+" hp="+mc.player.getHealth());
    if(mc.player.onGround()&&mc.player.getY()<83&&landed<0)landed=ticks;
   }
   if(ticks==290){
    if(!"ready".equals(status.get()))throw new AssertionError(status.get());
    if(landed<0)throw new AssertionError("Never landed");
    if(landed<150)throw new AssertionError("Elf fell too quickly: bunny hops="+hops+" landed tick="+landed+" maxDown="+maxDown);
    if(mc.player.getHealth()<initialHp-.01)throw new AssertionError("Elf took fall damage: "+mc.player.getHealth()+" initial="+initialHp);
    result("PASS connected Elf slow fall; bunny hops="+hops+" landed tick="+landed+" maxDown="+maxDown+" health="+mc.player.getHealth());
   }
  }catch(Throwable t){result("FAIL "+t);}
 }
 void result(String text){done=true;var mc=Minecraft.getInstance();try{Files.writeString(mc.gameDirectory.toPath().resolve("fall-result.txt"),text);Files.write(mc.gameDirectory.toPath().resolve("fall-trace.txt"),trace);}catch(Exception e){throw new RuntimeException(e);}mc.stop();}
}
