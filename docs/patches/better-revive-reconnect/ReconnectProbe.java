package local.reconnect;
import java.nio.file.*;
import java.util.*;
import java.lang.reflect.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.*;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import de.mrstilu2k6.better_revive.util.KeepGear;
@Mod("reconnect_probe")
public class ReconnectProbe {
 private final List<String> results=new ArrayList<>();
 public ReconnectProbe(){NeoForge.EVENT_BUS.addListener(this::run);}
 private void check(boolean yes,String message){results.add((yes?"PASS ":"FAIL ")+message);if(!yes)throw new AssertionError(message);}
 private int count(ServerPlayer p,Item item){int n=0;for(int i=0;i<p.getInventory().getContainerSize();i++){var s=p.getInventory().getItem(i);if(s.is(item))n+=s.getCount();}return n;}
 private void run(ServerStartedEvent event){var server=event.getServer();try{
  var level=server.overworld();var id=UUID.randomUUID();var profile=new GameProfile(id,"ReconnectProbe");
  var old=new FakePlayer(level,profile);old.setHealth(20);
  var grave=NonNullList.withSize(41,ItemStack.EMPTY);
  KeepGear.setGraveSlot(grave,0,new ItemStack(Items.DIAMOND_SWORD),old);
  KeepGear.setGraveSlot(grave,38,new ItemStack(Items.DIAMOND_CHESTPLATE),old);
  KeepGear.setGraveSlot(grave,40,new ItemStack(Items.SHIELD),old);
  KeepGear.setGraveSlot(grave,10,new ItemStack(Items.EMERALD,16),old);
  check(grave.get(0).isEmpty()&&grave.get(38).isEmpty()&&grave.get(40).isEmpty()&&grave.get(10).getCount()==16,"retained gear separate from ordinary grave items");
  // Simulate a server restart: only the persisted player tag survives.
  var field=KeepGear.class.getDeclaredField("PENDING");field.setAccessible(true);((Map<?,?>)field.get(null)).clear();
  old.setHealth(0);check(!old.isAlive(),"reconnecting player is still dead before respawn");
  NeoForge.EVENT_BUS.post(new PlayerEvent.PlayerLoggedInEvent(old));
  check(old.getPersistentData().contains("better_revive_keep_gear"),"dead login preserves retained-gear record for respawn");
  check(count(old,Items.DIAMOND_SWORD)==0,"dead login does not restore gear into discarded inventory");
  var replacement=new FakePlayer(level,profile);replacement.setHealth(20);
  NeoForge.EVENT_BUS.post(new PlayerEvent.Clone(replacement,old,true));
  check(replacement.getPersistentData().contains("better_revive_keep_gear"),"death clone preserves persisted recovery record");
  NeoForge.EVENT_BUS.post(new PlayerEvent.PlayerRespawnEvent(replacement,false));
  // Unlisted fake players do not receive DeathFix's online-player next-tick lookup.
  // Invoke its exact restore operation after the real clone/respawn events.
  KeepGear.restore(replacement);
  check(count(replacement,Items.DIAMOND_SWORD)==1&&count(replacement,Items.DIAMOND_CHESTPLATE)==1&&count(replacement,Items.SHIELD)==1,"death clone and respawn restore every retained slot");
  check(!replacement.getPersistentData().contains("better_revive_keep_gear"),"successful respawn consumes recovery record");
  KeepGear.restore(replacement);check(count(replacement,Items.DIAMOND_SWORD)==1,"repeated restore does not duplicate gear");
  var live=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"LiveRecovery"));live.setHealth(20);
  KeepGear.setGraveSlot(NonNullList.withSize(41,ItemStack.EMPTY),0,new ItemStack(Items.IRON_SWORD),live);
  ((Map<?,?>)field.get(null)).clear();NeoForge.EVENT_BUS.post(new PlayerEvent.PlayerLoggedInEvent(live));
  check(count(live,Items.IRON_SWORD)==1&&!live.getPersistentData().contains("better_revive_keep_gear"),"living login still recovers a stranded saved record");
 }catch(Throwable failure){results.add("ERROR "+failure);failure.printStackTrace();}finally{
  try{Files.write(Path.of("reconnect-results.txt"),results);}catch(Exception e){throw new RuntimeException(e);}
  results.forEach(System.out::println);server.halt(false);
 }}
}
