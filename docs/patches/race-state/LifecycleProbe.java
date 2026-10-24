package local.asgardprobe;
import java.util.*;
import java.util.function.Consumer;
import com.eiden.iourusraces.race.*;
import com.eiden.iourusraces.data.PlayerRaceState;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
@Mod("race_state_probe")
public class LifecycleProbe {
 static final DeferredRegister<Block> BLOCKS=DeferredRegister.create(Registries.BLOCK,"race_state_probe");
 static final java.util.function.Supplier<Block> MOD_LEAVES=BLOCKS.register("untagged_leaves",()->new TintedParticleLeavesBlock(.01f,BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK,Identifier.parse("race_state_probe:untagged_leaves"))).noCollision()));
 public LifecycleProbe(IEventBus bus){BLOCKS.register(bus);}
 static Consumer<String> out;
 static void eq(String k,double a,double b){if(Math.abs(a-b)>.006)throw new AssertionError(k+" "+a+" != "+b);out.accept("PASS "+k+" actual="+a);}
 public static void run(ServerLevel world,ServerPlayer p,Consumer<String> sink)throws Exception{
  out=sink;p.removeAllEffects();p.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(1);PlayerRaceData.setRace(p,Race.HUMAN);p.setHealth(p.getMaxHealth());
  PlayerRaceData.setRace(p,Race.ELF);eq("Race setter immediately applies Elf movement",p.getAttributeValue(Attributes.MOVEMENT_SPEED),.108);
  PlayerRaceData.setRace(p,Race.HUMAN);eq("Race setter immediately clears Elf jump",p.getAttributeValue(Attributes.JUMP_STRENGTH),.42);
  BlockPos pos=new BlockPos(0,80,0);var custom=MOD_LEAVES.get().defaultBlockState();eq("Custom leaf is untagged fixture",custom.is(net.minecraft.tags.BlockTags.LEAVES)?1:0,0);
  for(Race from:Race.values())for(Race to:Race.values()){
   PlayerRaceData.setRace(p,from);p.setHealth(p.getMaxHealth());PlayerRaceData.setRace(p,to);p.setHealth(p.getMaxHealth());RaceBalanceRules.apply(p,to);
   String key=from+"->"+to;
   eq(key+" HP",p.getMaxHealth(),to.getMaxHealth());eq(key+" speed",p.getAttributeValue(Attributes.MOVEMENT_SPEED),.1*to.getSpeedMultiplier());eq(key+" scale",p.getAttributeValue(Attributes.SCALE),to.getScale());
   eq(key+" jump",p.getAttributeValue(Attributes.JUMP_STRENGTH),.42+(to==Race.ELF?.1:to==Race.GOBLIN?.15:0));eq(key+" armor",p.getAttributeValue(Attributes.ARMOR),to==Race.DWARF?2:0);
   eq(key+" melee",p.getAttributeValue(Attributes.ATTACK_DAMAGE),to==Race.ORC?3:to==Race.MELFORK?0:1);
   eq(key+" attack speed",p.getAttributeValue(Attributes.ATTACK_SPEED),to==Race.ORC?3.6:4);
   eq(key+" modded leaf collision",CollisionContext.of(p).getCollisionShape(custom,world,pos).isEmpty()?0:1,to==Race.ELF?1:0);
   eq(key+" vanilla leaf collision",CollisionContext.of(p).getCollisionShape(Blocks.OAK_LEAVES.defaultBlockState(),world,pos).isEmpty()?0:1,to==Race.ELF?1:0);
  }
  PlayerRaceData.setRace(p,Race.MELFORK);eq("Melfork land penalty installed immediately",p.getAttributeValue(Attributes.ATTACK_DAMAGE),0);PlayerRaceData.setRace(p,Race.HUMAN);eq("Melfork penalty clears before next tick",p.getAttributeValue(Attributes.ATTACK_DAMAGE),1);
  PlayerRaceState.get(world.getServer()).setRace(p.getUUID(),Race.GOBLIN);NeoForge.EVENT_BUS.post(new PlayerTickEvent.Pre(p));eq("Direct admin state edit reconciles scale",p.getAttributeValue(Attributes.SCALE),.75);
  PlayerRaceState.get(world.getServer()).setRace(p.getUUID(),Race.HUMAN);NeoForge.EVENT_BUS.post(new PlayerTickEvent.Pre(p));eq("Direct admin state edit clears jump",p.getAttributeValue(Attributes.JUMP_STRENGTH),.42);
  p.setPos(.5,80,.5);world.setBlock(pos,Blocks.WATER.defaultBlockState(),3);world.setBlock(pos.above(),Blocks.WATER.defaultBlockState(),3);
  var wet=net.minecraft.world.entity.Entity.class.getDeclaredField("wasTouchingWater");wet.setAccessible(true);wet.setBoolean(p,true);eq("Water fixture detects immersion",p.isInWater()?1:0,1);
  p.setAirSupply(5);PlayerRaceData.setRace(p,Race.MELFORK);eq("Melfork water strength immediate",p.getAttributeValue(Attributes.ATTACK_DAMAGE),4);eq("Melfork water breathing",p.getAirSupply(),p.getMaxAirSupply());
  PlayerRaceData.setRace(p,Race.HUMAN);eq("Melfork water strength cleared immediately",p.getAttributeValue(Attributes.ATTACK_DAMAGE),1);eq("Melfork swim modifier removed",p.getAttributeValue(Attributes.WATER_MOVEMENT_EFFICIENCY),0);
  world.setBlock(pos,Blocks.AIR.defaultBlockState(),3);world.setBlock(pos.above(),Blocks.AIR.defaultBlockState(),3);wet.setBoolean(p,false);
  for(Race race:Race.values())for(boolean water:new boolean[]{false,true})eq("Vision policy "+race+" wet="+water,RaceVisualRules.nightVision(race,water)?1:0,race==Race.GOBLIN||(race==Race.MELFORK&&water)?1:0);
  var gear=Identifier.parse("probe:unrelated_gear");p.getAttribute(Attributes.ARMOR).addTransientModifier(new AttributeModifier(gear,3,AttributeModifier.Operation.ADD_VALUE));
  PlayerRaceData.setRace(p,Race.DWARF);PlayerRaceData.setRace(p,Race.HUMAN);eq("Race cleanup preserves unrelated gear",p.getAttributeValue(Attributes.ARMOR),3);p.getAttribute(Attributes.ARMOR).removeModifier(gear);
 }
}
