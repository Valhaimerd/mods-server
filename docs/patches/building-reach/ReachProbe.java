package local.buildingaxis;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.*;
import sophisticated.building.CommonConfig;
import sophisticated.building.attachment.*;
import sophisticated.building.buildmode.*;
import sophisticated.building.buildmode.buildmodes.Line;
import sophisticated.building.systems.*;
import sophisticated.building.utilities.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
@Mod("building_axis_probe")
public class ReachProbe {
 int ticks;boolean hidden,done;List<String> checks=Collections.synchronizedList(new ArrayList<>());AtomicReference<String> status=new AtomicReference<>();
 public ReachProbe(){NeoForge.EVENT_BUS.addListener(this::tick);}
 void check(boolean ok,String s){if(!ok)throw new AssertionError(s);checks.add("PASS "+s);}
 static class FixedLine extends TwoClicksBuildMode {
  final BlockPos end;
  FixedLine(BlockPos first,BlockPos end){clicks=1;firstBlockEntry=new BlockEntry(first);this.end=end;}
  protected BlockPos findSecondPos(Player p,BlockPos first,boolean skip){return end;}
  protected List<BlockPos> getAllBlocks(Player p,int x1,int y1,int z1,int x2,int y2,int z2){return Line.getLineBlocks(x1,y1,z1,x2,y2,z2,1);}
 }
 BlockSet request(int length){var b=new BlockSet();b.firstPos=new BlockPos(0,81,0);b.lastPos=new BlockPos(length-1,81,0);for(int x=0;x<length;x++){var entry=new BlockEntry(new BlockPos(x,81,0),Blocks.STONE.defaultBlockState(),Items.STONE);entry.existingBlockState=Blocks.AIR.defaultBlockState();b.put(entry.blockPos,entry);}return b;}
 void tick(ClientTickEvent.Post e){var mc=Minecraft.getInstance();if(!hidden){org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());hidden=true;}if(done||mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;ticks++;
 try{
  if(status.get()!=null&&status.get().startsWith("FAIL"))throw new AssertionError(status.get());
  if(ticks==20){mc.setScreenAndShow(null);mc.getSingleplayerServer().execute(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());p.setGameMode(GameType.SURVIVAL);AttachmentHandler.getOrCreatePowerLevel(p).setPowerLevel(0);AttachmentHandler.syncToClient(p);});}
  if(ticks==70){
   check(AttachmentHandler.getPlacementReach(mc.player,false)==1000,"client reach ignores old power-tier config");
   mc.player.setPos(.5,81,.5);mc.player.setYRot(-90);mc.player.setXRot(0);
   for(int x=0;x<64;x++)mc.level.setBlock(new BlockPos(x,82,0),Blocks.AIR.defaultBlockState(),3);
   mc.level.setBlock(new BlockPos(64,82,0),Blocks.STONE.defaultBlockState(),3);
   var hit=ClientBlockUtilities.getLookingAtFar(mc.player);
   check(hit.getType()==net.minecraft.world.phys.HitResult.Type.BLOCK&&hit.getBlockPos().equals(new BlockPos(64,82,0)),"native targeting ray reaches a block 64 blocks away");
   BlockSet.ClientSide.invalidateCache();
   for(int axis=0;axis<3;axis++)for(int sign:new int[]{-1,1}){
    var first=new BlockPos(0,81,0);var end=first.offset(axis==0?sign*31:0,axis==1?sign*31:0,axis==2?sign*31:0);var blocks=new BlockSet();new FixedLine(first,end).findCoordinates(blocks);
    check(blocks.size()==32,"native client preview 32 blocks axis="+axis+" sign="+sign+" actual="+blocks.size());
   }
   var blocks=new BlockSet();new FixedLine(new BlockPos(0,81,0),new BlockPos(199,81,0)).findCoordinates(blocks);check(blocks.size()==128,"200-block preview retains 128-block batch cap");
   check(AttachmentHandler.getMaxBlocksPerAxis(mc.player,false)==Integer.MAX_VALUE,"client axis restriction removed");
   check(BuildLimits.extentWithinAxisLimit(60000000,60000000,60000000,Integer.MAX_VALUE),"full world coordinate span accepted without an axis cap");
   check(BuildLimits.maxBlockDistance(200,6,Integer.MAX_VALUE,200)==Integer.MAX_VALUE,"server distance arithmetic does not overflow");
   check(BuildLimits.startWithinReach(10000,AttachmentHandler.getPlacementReach(mc.player,false)),"100-block starting reach accepted");check(BuildLimits.arrayCount(10,64,Integer.MAX_VALUE)==10,"sparse array offset not limited by old axis cap");
   Class.forName("sophisticated.building.gui.buildmode.RadialMenu");
   var policy=Class.forName("sophisticated.building.pack.AxisPolicy");check("Unlimited".equals(policy.getMethod("label",Player.class,boolean.class).invoke(null,mc.player,false)),"radial axis label is Unlimited");
   var arrayEntry=new sophisticated.building.gui.buildmodifier.ArrayEntry(new sophisticated.building.gui.buildmodifier.ModifiersScreen(),new sophisticated.building.buildmodifier.Array());
   var labelField=sophisticated.building.gui.buildmodifier.BaseModifierEntry.class.getDeclaredField("reachLabel");labelField.setAccessible(true);var label=labelField.get(arrayEntry);var textField=label.getClass().getField("text");check(((net.minecraft.network.chat.Component)textField.get(label)).getString().contains("Unlimited"),"native array reach label renders Unlimited");
   mc.getSingleplayerServer().execute(()->{try{
    var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());var power=AttachmentHandler.getOrCreatePowerLevel(p);
    CommonConfig.reach.level0.set(0);CommonConfig.reach.level1.set(1);CommonConfig.reach.level2.set(8);CommonConfig.reach.level3.set(16);
    int[] batches={128,256,512,2048};for(int n=0;n<4;n++){power.setPowerLevel(n);check(AttachmentHandler.getMaxBlocksPerAxis(p,false)==Integer.MAX_VALUE,"server unlimited axis tier "+n);check(AttachmentHandler.getPlacementReach(p,false)==1000&&power.getPlacementReach(p,true)==1000,"1000-block reach for current/next tier "+n);check(AttachmentHandler.getBuildModeReach(p)==1006,"selection reach avoids integer-square overflow tier "+n);check(AttachmentHandler.getMaxBlocksPlacedAtOnce(p,false)==batches[n],"server original batch tier "+n);}
    power.setPowerLevel(0);p.setGameMode(GameType.CREATIVE);check(AttachmentHandler.getMaxBlocksPerAxis(p,false)==Integer.MAX_VALUE,"creative unlimited axis");check(AttachmentHandler.getMaxBlocksPlacedAtOnce(p,false)==10000,"creative batch unchanged");p.setGameMode(GameType.SURVIVAL);
    p.teleportTo(-64.5,81,3.5);p.getInventory().clearContent();p.getInventory().setItem(0,new ItemStack(Items.STONE,64));p.getInventory().setItem(1,new ItemStack(Items.STONE,64));p.getInventory().setItem(2,new ItemStack(Items.STONE,64));p.getInventory().setItem(3,new ItemStack(Items.STONE,64));
    for(int x=0;x<200;x++){p.level().setBlock(new BlockPos(x,80,0),Blocks.DIRT.defaultBlockState(),3);p.level().setBlock(new BlockPos(x,81,0),Blocks.AIR.defaultBlockState(),3);}
    var placer=new ServerBlockPlacer();var validate=ServerBlockPlacer.class.getDeclaredMethod("validateRequest",Player.class,BlockSet.class);validate.setAccessible(true);
    var longRequest=request(200);check((boolean)validate.invoke(placer,p,longRequest),"server accepts start 64 blocks away and selection beyond 8 blocks");check(longRequest.size()==128,"server truncates oversized request to existing 128-block batch");
    var far=request(32);far.firstPos=new BlockPos(1000,81,1000);check(!(boolean)validate.invoke(placer,p,far),"server retains 1000-block targeting bound");
    var actual=request(32);placer.placeBlocksDelayed(p,actual,0);int queued=placer.getDelayedEntries().size();placer.tick();int placed=0;for(int x=0;x<32;x++)if(p.level().getBlockState(new BlockPos(x,81,0)).is(Blocks.STONE))placed++;
    check(placed==32,"actual Survival placement starting 64 blocks away across 32-block axis actual="+placed+" queued="+queued+" loaded="+p.level().isLoaded(actual.firstPos)+" interact="+p.level().mayInteract(p,actual.firstPos)+" queueAfter="+placer.getDelayedEntries().size());int remaining=0;for(int slot=0;slot<36;slot++)if(p.getInventory().getItem(slot).is(Items.STONE))remaining+=p.getInventory().getItem(slot).getCount();check(remaining==224,"actual 32-block placement consumes 32 materials actual remaining="+remaining);
    check(placer.getDelayedEntries().isEmpty(),"placement queue cleaned after build");status.set("ready");
   }catch(Throwable t){status.set("FAIL "+t);}});
  }
  if(ticks==150){check("ready".equals(status.get()),"server fixture completed");finish("PASS "+checks.size()+" axis/preview/server/batch/material checks");}
 }catch(Throwable t){finish("FAIL "+t);}}
 void finish(String result){done=true;var mc=Minecraft.getInstance();try{Files.writeString(mc.gameDirectory.toPath().resolve("axis-result.txt"),result);Files.write(mc.gameDirectory.toPath().resolve("axis-checks.txt"),checks);}catch(Exception e){throw new RuntimeException(e);}mc.stop();}
}
