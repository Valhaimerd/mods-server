package local.raceclient;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import com.eiden.iourusraces.race.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
@Mod("race_client_probe")
public class ClientLifecycleProbe {
 int tick;boolean hidden,done;List<String> checks=new ArrayList<>();AtomicReference<String> failure=new AtomicReference<>();
 public ClientLifecycleProbe(){NeoForge.EVENT_BUS.addListener(this::tick);}
 void check(boolean ok,String label){if(!ok)throw new AssertionError(label);checks.add("PASS "+label);}
 void server(java.util.function.Consumer<net.minecraft.server.level.ServerPlayer> f){var mc=Minecraft.getInstance();mc.getSingleplayerServer().execute(()->{try{f.accept(mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID()));}catch(Throwable t){failure.set(t.toString());}});}
 void tick(ClientTickEvent.Post e){
  var mc=Minecraft.getInstance();if(!hidden){org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());hidden=true;}if(done||mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;tick++;
  try{
   if(failure.get()!=null)throw new AssertionError(failure.get());
   if(tick==20){mc.setScreenAndShow(null);server(p->{p.removeAllEffects();p.teleportTo(.5,81,.5);PlayerRaceData.setRace(p,Race.ELF);});}
   if(tick==40){check(ClientRaceData.getRace()==Race.ELF,"Elf identity synced");check(!RaceVisualRules.nightVision(ClientRaceData.getRace(),false),"Elf night vision absent");check(RaceVisualRules.solidLeaves(ClientRaceData.getRace()),"Elf solid leaf footing");check(Math.abs(mc.player.getAttributeValue(Attributes.MOVEMENT_SPEED)-.108)<.001,"Elf movement synced");server(p->PlayerRaceData.setRace(p,Race.GOBLIN));}
   if(tick==60){check(ClientRaceData.getRace()==Race.GOBLIN,"Goblin identity synced");check(RaceVisualRules.nightVision(ClientRaceData.getRace(),false),"Goblin night vision active");check(Math.abs(mc.player.getAttributeValue(Attributes.SCALE)-.75)<.001,"Goblin scale synced");server(p->PlayerRaceData.setRace(p,Race.HUMAN));}
   if(tick==80){check(ClientRaceData.getRace()==Race.HUMAN,"Human identity synced after Goblin");check(!RaceVisualRules.nightVision(ClientRaceData.getRace(),false),"Goblin vision clears on Human");check(Math.abs(mc.player.getAttributeValue(Attributes.SCALE)-1)<.001,"Goblin scale clears on Human");server(p->{
    for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++){p.level().setBlock(new BlockPos(x,80,z),Blocks.STONE.defaultBlockState(),3);for(int y=81;y<=84;y++)p.level().setBlock(new BlockPos(x,y,z),Blocks.WATER.defaultBlockState(),3);}
    p.teleportTo(.5,81,.5);p.setAirSupply(5);PlayerRaceData.setRace(p,Race.MELFORK);
   });}
   if(tick==120){check(ClientRaceData.getRace()==Race.MELFORK,"Melfork identity synced");check(mc.player.isInWater(),"Connected player immersed in water");check(RaceVisualRules.nightVision(ClientRaceData.getRace(),mc.player.isInWater()),"Melfork water vision active");server(p->{check(p.isInWater(),"Server sees real water immersion");check(p.getAirSupply()==p.getMaxAirSupply(),"Melfork underwater air refills");check(p.getAttributeValue(Attributes.ATTACK_DAMAGE)>=4,"Melfork water strength present");PlayerRaceData.setRace(p,Race.HUMAN);});}
   if(tick==155){check(ClientRaceData.getRace()==Race.HUMAN,"Human identity synced underwater");check(!RaceVisualRules.nightVision(ClientRaceData.getRace(),true),"Melfork water vision clears on Human");server(p->{check(p.getAttribute(Attributes.ATTACK_DAMAGE).getModifier(net.minecraft.resources.Identifier.parse("iourusraces:melfork_water_strength"))==null,"Melfork water strength cleared");check(p.getAttributeValue(Attributes.WATER_MOVEMENT_EFFICIENCY)==0,"Melfork swim modifier cleared");});}
   if(tick==180){
    var m=Arrays.stream(net.minecraft.client.renderer.Lightmap.class.getDeclaredMethods()).filter(x->x.getName().contains("raceVision")).findFirst().orElseThrow();m.setAccessible(true);var field=Arrays.stream(mc.gameRenderer.getClass().getDeclaredFields()).filter(f->f.getType()==net.minecraft.client.renderer.Lightmap.class).findFirst().orElseThrow();field.setAccessible(true);check(Math.abs((float)m.invoke(field.get(mc.gameRenderer),.25f)-.25)<.001,"Native lightmap hook preserves Human lighting");
    ClientRaceData.setRace(Race.GOBLIN);NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut(null,mc.player,null));check(ClientRaceData.getRace()==Race.NONE,"Client identity resets on logout event");result("PASS "+checks.size()+" connected race lifecycle checks");
   }
  }catch(Throwable t){t.printStackTrace();result("FAIL "+t);}
 }
 void result(String message){done=true;var mc=Minecraft.getInstance();try{Files.writeString(mc.gameDirectory.toPath().resolve("race-client-result.txt"),message);Files.write(mc.gameDirectory.toPath().resolve("race-client-checks.txt"),checks);}catch(Exception e){throw new RuntimeException(e);}mc.stop();}
}
