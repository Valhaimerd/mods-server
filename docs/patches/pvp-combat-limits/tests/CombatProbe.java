package pack.pvp_probe;
import java.util.*;
import io.redspace.pvp_flagging.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.GameType;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import sophisticated.building.systems.ServerBlockPlacer;
import sophisticated.building.utilities.*;
import org.xiyu.onekeyminer.chain.*;
public class CombatProbe {
 static final net.neoforged.neoforge.registries.DeferredRegister<net.minecraft.world.level.block.Block> REG=net.neoforged.neoforge.registries.DeferredRegister.create(net.minecraft.core.registries.Registries.BLOCK,"pvp_probe");
 static final java.util.function.Supplier<net.minecraft.world.level.block.Block> REFUSES=REG.register("refuses", id->new net.minecraft.world.level.block.Block(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK,id))){
  @Override public boolean onDestroyedByPlayer(net.minecraft.world.level.block.state.BlockState state,net.minecraft.world.level.Level level,BlockPos pos,net.minecraft.world.entity.player.Player player,ItemStack tool,boolean harvest,net.minecraft.world.level.material.FluidState fluid){return false;}
 });
 public static void register(net.neoforged.bus.api.IEventBus bus){REG.register(bus);}
 static List<String> results=new ArrayList<>();
 static void check(boolean b,String s){if(!b)throw new AssertionError(s);results.add("PASS "+s);}
 static void clear(ServerPlayer p){p.getPersistentData().remove(CombatRules.KEY);}
 static void expire(ServerPlayer p){var s=CombatRules.state(p);s.until=System.currentTimeMillis()-1;CombatRules.save(p,s);}
 static BlockSet request(BlockPos pos){var b=new BlockSet();b.firstPos=b.lastPos=pos;var e=new BlockEntry(pos,Blocks.STONE.defaultBlockState(),Items.STONE);e.existingBlockState=Blocks.AIR.defaultBlockState();b.put(pos,e);return b;}
 public static void run(MinecraftServer server,ServerPlayer a,ServerPlayer b)throws Exception{
  var l=server.overworld();a.setGameMode(GameType.SURVIVAL);b.setGameMode(GameType.SURVIVAL);clear(a);clear(b);
  b.setHealth(100);b.invulnerableTime=0;b.hurtServer(l,l.damageSources().playerAttack(a),10);
  check(CombatRules.flagged(a)&&CombatRules.flagged(b),"confirmed native player hit flags both");
  long until=CombatRules.state(a).until;check(until-System.currentTimeMillis()>39000,"combat lasts 40 seconds");
  CombatRules.attempt(a,true,1);CombatRules.completed(a,true,19);long placement=CombatRules.state(a).placeUntil;
  CombatRules.flag(a);check(CombatRules.state(a).placed==19&&CombatRules.state(a).placeUntil==placement,"refresh preserves budget/window");
  clear(a);clear(b);b.invulnerableTime=0;b.hurtServer(l,l.damageSources().generic(),2);check(!CombatRules.flagged(b),"environment damage does not flag");
  b.invulnerableTime=0;b.hurtServer(l,l.damageSources().playerAttack(a),0);check(!CombatRules.flagged(a)&&!CombatRules.flagged(b),"zero damage does not flag");
  var team=server.getScoreboard().addPlayerTeam("combat-protected");server.getScoreboard().addPlayerToTeam(a.getScoreboardName(),team);server.getScoreboard().addPlayerToTeam(b.getScoreboardName(),team);team.setAllowFriendlyFire(false);
  b.invulnerableTime=0;b.hurtServer(l,l.damageSources().playerAttack(a),10);check(!CombatRules.flagged(a)&&!CombatRules.flagged(b),"protected damage does not flag");server.getScoreboard().removePlayerTeam(team);
  CombatRules.flag(a);check(CombatRules.state(a).breakUntil==0&&CombatRules.state(a).placeUntil==0,"flag alone starts neither block window");
  var click=new PlayerInteractEvent.LeftClickBlock(a,new BlockPos(0,80,0),Direction.UP,PlayerInteractEvent.LeftClickBlock.Action.START);NeoForge.EVENT_BUS.post(click);
  check(CombatRules.state(a).breakUntil>0&&CombatRules.state(a).placeUntil==0,"first break attempt starts only break window");
  a.getInventory().clearContent();a.getInventory().setSelectedSlot(0);a.getInventory().setItem(0,new ItemStack(Items.DIAMOND_PICKAXE));a.teleportTo(.5,81,3.5);
  var refuses=new BlockPos(-1,80,0);l.setBlock(refuses,REFUSES.get().defaultBlockState(),3);a.gameMode.destroyBlock(refuses);check(l.getBlockState(refuses).is(REFUSES.get()),"modded block refuses destruction");check(CombatRules.state(a).broken==0,"refused native removal consumes no break allowance");
  for(int i=0;i<31;i++){var pos=new BlockPos(i,80,0);l.setBlock(pos,Blocks.STONE.defaultBlockState(),3);boolean success=a.gameMode.destroyBlock(pos);check(success==(i<30),"native break budget boundary "+i);}
  check(CombatRules.state(a).broken==30,"successful breaks charged exactly once");check(!l.getBlockState(new BlockPos(30,80,0)).isAir(),"31st block remains intact");
  long breakEnd=CombatRules.state(a).breakUntil;CombatRules.attempt(a,false,1);check(CombatRules.state(a).breakUntil==breakEnd,"denied attempt does not extend break deadline");
  a.getInventory().setItem(0,new ItemStack(Items.STONE,64));int itemBefore=a.getMainHandItem().getCount();
  for(int i=0;i<21;i++){
   var floor=new BlockPos(i,80,4);l.setBlock(floor,Blocks.DIRT.defaultBlockState(),3);l.setBlock(floor.above(),Blocks.AIR.defaultBlockState(),3);
   var context=new UseOnContext(a,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(floor).add(0,.5,0),Direction.UP,floor,false));
   var result=CommonHooks.onPlaceItemIntoWorld(context);check(result.consumesAction()==(i<20),"native placement boundary "+i);
  }
  check(CombatRules.state(a).placed==20,"ordinary placements charged exactly once");check(a.getMainHandItem().getCount()==itemBefore-20,"denied placement consumes no material");check(l.getBlockState(new BlockPos(20,81,4)).isAir(),"over-budget predicted placement rolled back");
  var expiredWindow=CombatRules.state(a);expiredWindow.placeUntil=System.currentTimeMillis()-1;CombatRules.save(a,expiredWindow);check(CombatRules.attempt(a,true,1),"placement window refreshes independently");check(!CombatRules.attempt(a,false,1),"placement refresh does not refund breaks");
  var root=new BlockPos(0,81,8);l.setBlock(root.below(),Blocks.DIRT.defaultBlockState(),3);l.setBlock(root,Blocks.AIR.defaultBlockState(),3);
  var snapshot=BlockSnapshot.create(l.dimension(),l,root);var s=CombatRules.state(a);s.placed=19;CombatRules.save(a,s);
  check(EventHooks.onMultiBlockPlace(a,List.of(snapshot,snapshot),Direction.UP),"multi-place rejected if whole action exceeds remainder");check(CombatRules.state(a).placed==19,"rejected multi-place consumes no allowance");
  expire(a);CombatRules.sync(a);check(!CombatRules.flagged(a)&&!a.getPersistentData().contains(CombatRules.KEY),"expiry clears both windows and persisted state");check(CombatRules.attempt(a,false,100),"expiry lifts limits even before block window ends");
  var placer=new ServerBlockPlacer();var pos=new BlockPos(0,81,10);l.setBlock(pos,Blocks.AIR.defaultBlockState(),3);l.setBlock(pos.below(),Blocks.DIRT.defaultBlockState(),3);a.teleportTo(.5,81,10.5);
  a.getInventory().setItem(0,new ItemStack(Items.STONE,64));placer.placeBlocksDelayed(a,request(pos),l.getGameTime()+1000);check(placer.getDelayedEntries().size()==1,"advanced request queued before combat");
  CombatRules.flag(a);placer.tick();check(placer.getDelayedEntries().isEmpty()&&l.getBlockState(pos).isAir(),"combat discards pending building immediately on queue tick");check(a.getMainHandItem().getCount()==64,"discarded work consumes no material");
  placer.placeBlocksDelayed(a,request(pos),0);check(placer.getDelayedEntries().isEmpty(),"advanced request refused while flagged");placer.applyBlockSet(a,request(pos));check(l.getBlockState(pos).isAir(),"direct advanced apply blocked");
  placer.undoBlockSet(a,request(pos));check(l.getBlockState(pos).isAir(),"direct undo blocked");check(!sophisticated.building.SophisticatedBuilding.UNDO_REDO.undo(a)&&!sophisticated.building.SophisticatedBuilding.UNDO_REDO.redo(a),"undo and redo entry points blocked");
  var c=ChainActionContext.builder().player(a).level(l).originPos(root).originState(Blocks.STONE.defaultBlockState()).actionType(ChainActionType.MINING).heldItem(new ItemStack(Items.DIAMOND_PICKAXE)).hand(InteractionHand.MAIN_HAND).build();var mined=ChainActionLogic.execute(c);check(mined.stopReason()==ChainActionResult.StopReason.PERMISSION_DENIED&&mined.totalCount()==0,"OneKeyMiner shared chain operation blocked");
  var tag=a.getPersistentData().getCompoundOrEmpty(CombatRules.KEY).copy();var clone=net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(l);clear(clone);NeoForge.EVENT_BUS.post(new PlayerEvent.Clone(clone,a,true));check(clone.getPersistentData().getCompoundOrEmpty(CombatRules.KEY).equals(tag),"respawn clone preserves entire timer/budget state");
  clear(a);a.getPersistentData().put(CombatRules.KEY,tag.copy());check(CombatRules.flagged(a),"saved player data restores combat");
  expire(a);placer.placeBlocksDelayed(a,request(pos),0);placer.tick();check(l.getBlockState(pos).is(Blocks.STONE),"ordinary advanced behavior resumes after combat expiry");
  java.nio.file.Files.write(java.nio.file.Path.of("combat-checks.txt"),results);System.out.println("COMBAT PASS "+results.size());
 }
}
