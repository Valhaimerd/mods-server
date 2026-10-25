package local.leafclimb;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import com.eiden.iourusraces.race.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.*;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
@Mod("leaf_climb_probe")
public class LeafClimbProbe {
 static final DeferredRegister<Block> BLOCKS=DeferredRegister.create(Registries.BLOCK,"leaf_climb_probe");
 static final java.util.function.Supplier<Block> MOD_LEAF=BLOCKS.register("untagged_leaves",()->new TintedParticleLeavesBlock(.01f,BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK,Identifier.parse("leaf_climb_probe:untagged_leaves"))).noCollision()));
 int ticks,phase=-1;boolean hidden,done;double startY;List<String> checks=Collections.synchronizedList(new ArrayList<>());AtomicReference<String> error=new AtomicReference<>();
 Race[] races={Race.ELF,Race.GOBLIN,Race.ELF,Race.HUMAN,Race.GOBLIN};
 public LeafClimbProbe(IEventBus bus){BLOCKS.register(bus);NeoForge.EVENT_BUS.addListener(this::tick);}
 void check(boolean ok,String label){if(!ok)throw new AssertionError(label);checks.add("PASS "+label);}
 void wall(ServerPlayer p,BlockState state){
  for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++)for(int y=80;y<95;y++)p.level().setBlock(new BlockPos(x,y,z),y==80?Blocks.STONE.defaultBlockState():(x==1?state:Blocks.AIR.defaultBlockState()),3);
  p.setPos(1-p.getBbWidth()/2,81,.5);p.horizontalCollision=true;p.fallDistance=0;
 }
 boolean climb(ServerPlayer p){try{var field=net.minecraft.world.entity.Entity.class.getDeclaredField("inBlockState");field.setAccessible(true);field.set(p,null);return p.onClimbable();}catch(Exception e){throw new RuntimeException(e);}}
 void matrix(ServerPlayer p){
  p.setGameMode(GameType.SURVIVAL);p.getAbilities().flying=false;p.removeAllEffects();
  var vanilla=Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true);var modded=MOD_LEAF.get().defaultBlockState();
  check(!modded.is(net.minecraft.tags.BlockTags.LEAVES),"synthetic mod leaf is untagged");
  for(Race race:Race.values()){
   PlayerRaceData.setRace(p,race);
   for(var state:List.of(vanilla,modded)){
    wall(p,state);check(!CollisionContext.of(p).getCollisionShape(state,p.level(),new BlockPos(1,81,0)).isEmpty(),race+" solid "+state.getBlock());
    check(climb(p)==(race==Race.ELF||race==Race.GOBLIN),race+" leaf-only native climb "+state.getBlock());
   }
   wall(p,Blocks.STONE.defaultBlockState());check(!climb(p),race+" cannot naturally climb stone");
   var inv=CuriosApi.getCuriosInventory(p).orElseThrow();inv.loadDatapacks();var entry=inv.getCurios().entrySet().stream().filter(e->e.getValue().getStacks().getSlots()>0).findFirst().orElseThrow();
   var stack=new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse("nameless_trinkets:spider_legs")));entry.getValue().getStacks().setStackInSlot(0,stack);
   p.setDeltaMovement(0,0,0);((ICurioItem)stack.getItem()).curioTick(new SlotContext(entry.getKey(),p,0,false,true),stack);
   check(p.getDeltaMovement().y>0,race+" equipped Spider Legs still climbs stone");entry.getValue().getStacks().setStackInSlot(0,ItemStack.EMPTY);
  }
  PlayerRaceData.setRace(p,Race.ELF);wall(p,Blocks.STONE.defaultBlockState());p.level().setBlock(new BlockPos(0,80,0),vanilla,3);check(!climb(p),"leaf floor does not enable stone climbing");
  p.level().setBlock(new BlockPos(0,84,0),vanilla,3);check(!climb(p),"leaf ceiling does not enable stone climbing");
  wall(p,vanilla);p.horizontalCollision=false;check(!climb(p),"leaf climbing requires wall contact");
  p.horizontalCollision=true;PlayerRaceData.setRace(p,Race.HUMAN);check(!climb(p),"race switch immediately removes leaf climbing");PlayerRaceData.setRace(p,Race.GOBLIN);check(climb(p),"race switch immediately grants Goblin leaf climbing");
  wall(p,Blocks.STONE.defaultBlockState());p.level().setBlock(new BlockPos(0,81,1),Blocks.STONE.defaultBlockState(),2);p.level().setBlock(new BlockPos(0,81,0),Blocks.LADDER.defaultBlockState(),2);p.setPos(-5,81,.5);p.setPos(.5,81,.5);check(climb(p),"ordinary ladder behavior retained at "+p.blockPosition()+" state="+p.getInBlockState());
 }
 void tick(ClientTickEvent.Post event){
  var mc=Minecraft.getInstance();if(!hidden){org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());hidden=true;}
  if(done||mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;ticks++;
  try{
   if(error.get()!=null)throw new AssertionError(error.get());
   if(ticks==20){mc.setScreenAndShow(null);mc.getSingleplayerServer().execute(()->{try{matrix(mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID()));}catch(Throwable e){error.set(e.toString());}});}
   for(int n=0;n<races.length;n++){
    if(ticks==50+n*60){final int index=n;mc.options.keyUp.setDown(false);mc.getSingleplayerServer().execute(()->{try{
     var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());PlayerRaceData.setRace(p,races[index]);wall(p,index==2?Blocks.STONE.defaultBlockState():index==4?MOD_LEAF.get().defaultBlockState():Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true));
     p.teleportTo(.5,81,.5);p.setDeltaMovement(0,0,0);p.setYRot(-90);p.setXRot(0);
    }catch(Throwable e){error.set(e.toString());}});phase=n;}
    if(ticks==65+n*60){check(ClientRaceData.getRace()==races[n],"client race synchronized phase "+n);mc.player.setYRot(-90);mc.player.setXRot(0);startY=mc.player.getY();mc.options.keyUp.setDown(true);}
    if(ticks==100+n*60){mc.options.keyUp.setDown(false);double rise=mc.player.getY()-startY;check(n<2||n==4?rise>2:rise<.5,"connected movement phase "+n+" "+races[n]+" rise="+rise);check(mc.player.getBoundingBox().maxX<=1.05,"connected wall cannot be bypassed phase "+n);}
   }
   if(ticks==350)finish("PASS "+checks.size()+" leaf collision/climbing/item checks");
  }catch(Throwable e){finish("FAIL "+e);}
 }
 void finish(String result){done=true;var mc=Minecraft.getInstance();mc.options.keyUp.setDown(false);try{Files.writeString(mc.gameDirectory.toPath().resolve("leaf-result.txt"),result);Files.write(mc.gameDirectory.toPath().resolve("leaf-checks.txt"),checks);}catch(Exception e){throw new RuntimeException(e);}mc.stop();}
}
