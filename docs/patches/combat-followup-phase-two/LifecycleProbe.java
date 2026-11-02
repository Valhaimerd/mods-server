package local.audit;
import java.util.*;import java.nio.file.*;import com.mojang.authlib.GameProfile;
import net.neoforged.fml.common.Mod;import net.neoforged.neoforge.common.NeoForge;import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.minecraft.world.entity.*;import net.minecraft.world.entity.ai.attributes.Attributes;import net.minecraft.world.level.GameType;import net.minecraft.world.phys.Vec3;
import net.spell_engine.internals.*;
@Mod("phase_two_probe") public class LifecycleProbe {
 List<String> lines=new ArrayList<>();int n;void check(boolean ok,String text){if(!ok)throw new AssertionError(text);lines.add("PASS "+text);n++;}
 public LifecycleProbe(){NeoForge.EVENT_BUS.addListener(this::start);}
 void start(ServerStartedEvent e){var w=e.getServer().overworld();try{
  var caster=net.neoforged.neoforge.common.util.FakePlayerFactory.get(w,new GameProfile(UUID.randomUUID(),"LifecycleCaster"));var p=net.neoforged.neoforge.common.util.FakePlayerFactory.get(w,new GameProfile(UUID.randomUUID(),"LifecycleEnemy"));p.setGameMode(GameType.SURVIVAL);caster.setPos(0,100,0);caster.setHealth(caster.getMaxHealth());p.setHealth(p.getMaxHealth());p.setPos(11.4,100,0);PaladinUltimates.startChannel(caster,w.getGameTime());var wall=PurgatoryBoundary.active(w).getFirst();var cache=wall.getClass().getDeclaredField("cachedOwner");cache.setAccessible(true);cache.set(wall,caster);var delta=new Vec3(3,0,0);var origin=new Vec3(11.4,100,0);
  check(MultiplayerBoundary.clipFrom(p,origin,delta).x<1,"Active hostile wall restricts displacement");
  p.setGameMode(GameType.CREATIVE);check(MultiplayerBoundary.clipFrom(p,origin,delta).x<1,"Creative physical movement still blocked");p.setGameMode(GameType.SPECTATOR);check(MultiplayerBoundary.clipFrom(p,origin,delta).equals(delta),"Spectator policy preserved");p.setGameMode(GameType.SURVIVAL);
  for(double y:new double[]{90,106})check(MultiplayerBoundary.clipFrom(p,new Vec3(11.4,y,0),delta).equals(delta),"No floor/roof at y="+y);
  check(MultiplayerBoundary.clipFrom(p,new Vec3(11.4,94,0),delta).x<1,"Body intersection blocks below origin");
  check(MultiplayerBoundary.clipFrom(p,new Vec3(11.8,100,0),new Vec3(-1,0,0)).x<-.9,"Overlapping body can return inward");
  check(MultiplayerBoundary.clipFrom(caster,new Vec3(11.4,100,0),delta).equals(delta),"Caster freely crosses own wall");
  var wolf=net.minecraft.world.entity.EntityTypes.WOLF.create(w,EntitySpawnReason.COMMAND);wolf.setOwnerReference(EntityReference.of(caster.getUUID()));wolf.setPos(11.4,100,0);check(PurgatoryBoundary.clip(wolf,delta).equals(delta),"Owned pet keeps friendly passage");wolf.discard();
  caster.setHealth(0);check(MultiplayerBoundary.clipFrom(p,origin,delta).equals(delta),"Dead caster immediately stops restriction");caster.setHealth(caster.getMaxHealth());
  wall.discard();check(MultiplayerBoundary.clipFrom(p,origin,delta).equals(delta),"Removed wall immediately stops restriction");PaladinUltimates.end(caster,PaladinUltimates.End.CLEANUP);
  PaladinUltimates.startChannel(caster,w.getGameTime());for(var b:PurgatoryBoundary.active(w))if(!b.isRemoved())cache.set(b,caster);PaladinUltimates.end(caster,PaladinUltimates.End.RELEASE);check(MultiplayerBoundary.clipFrom(p,origin,delta).equals(delta),"Channel release leaves no movement restriction");
  p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_SWORD));p.getAttribute(Attributes.SCALE).setBaseValue(.75);p.setPos(0,100,0);check(!p.isWithinAttackRange(p.getMainHandItem(),new net.minecraft.world.phys.AABB(0,100,10,1,102,11),3),"Distant forged target still fails validation");
  check(!net.bettercombat.logic.WeaponReachValidation.handles(net.minecraft.world.item.ItemStack.EMPTY),"Nonweapon native validation remains");
  Files.writeString(Path.of("phase-two-result.txt"),"PASS "+n+" lifecycle checks\n");
 }catch(Throwable t){t.printStackTrace();try{Files.writeString(Path.of("phase-two-result.txt"),"FAIL "+t);}catch(Exception ignored){}}finally{try{Files.write(Path.of("phase-two-checks.txt"),lines);}catch(Exception ignored){}e.getServer().halt(false);}}
}
