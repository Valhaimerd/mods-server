package local.spawnaudit;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.resources.Identifier;
import net.minecraft.nbt.TagParser;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import com.mojang.authlib.GameProfile;
import xyz.verarr.spreadspawnpoints.spawnpoints.*;
import xyz.verarr.spreadspawnpoints.ServerPlayerExtension;
@Mod("spawn_probe")
public class SpawnProbe {
 List<String> checks=new ArrayList<>();
 void check(boolean ok,String text){if(!ok)throw new AssertionError(text);checks.add("PASS "+text);}
 public SpawnProbe(){NeoForge.EVENT_BUS.addListener(this::start);}
 void start(ServerStartedEvent event){event.getServer().execute(()->{
  try{
   var server=event.getServer();var world=server.overworld();var manager=SpawnPointManager.getInstance(world);var id=UUID.fromString("c6a97f67-f493-4bba-aedc-62067f1f067b");
   Path save=Path.of("persisted-spawn.txt");
   if(Files.exists(save)){
    check(manager.getSpawnPointGenerator().identifier().toString().equals("spreadspawnpoints:random"),"Random generator persists across restart");
    check(manager.getSpawnPoint(id).toString().equals(Files.readString(save)),"Player assignment persists across restart");
   }else{
    check(manager.getSpawnPointGenerator().identifier().toString().equals("spreadspawnpoints:vanilla"),"Installed default is vanilla");
    int n=server.getCommands().getDispatcher().execute("spawnpoints generator set spreadspawnpoints:random false",server.createCommandSourceStack());check(n==1,"Native command selects Random without resetting players");
    manager.modifyGenerator(TagParser.parseCompoundFully("{lowerX:-64,lowerZ:-64,upperX:64,upperZ:64,seed:12345L}"));
   }
   Set<String> positions=new HashSet<>();
   for(int i=0;i<8;i++){
    UUID user=i==0?id:UUID.nameUUIDFromBytes(("SpawnTest"+i).getBytes());var point=manager.getSpawnPoint(user);positions.add(point.toString());
    check(point.x>=-64&&point.x<=64&&point.y>=-64&&point.y<=64,"Random assignment stays in configured area #"+i);
    check(point.equals(manager.getSpawnPoint(user)),"Same player keeps assignment #"+i);
    BlockPos actual=ServerPlayerExtension.getSpecificSpawnPos(user,world);check(actual.getX()==point.x&&actual.getZ()==point.y,"Native spawn helper uses assignment #"+i);
   }
   check(positions.size()>1,"Different players receive different random coordinates");
   var player=FakePlayerFactory.get(world,new GameProfile(id,"SpawnProbe"));var transition=TeleportTransition.createDefault(player,TeleportTransition.DO_NOTHING);var point=manager.getSpawnPoint(id);
   check(Math.abs(transition.position().x-point.x)<17&&Math.abs(transition.position().z-point.y)<17,"Default respawn Mixin routes to assigned location");
   check(transition.newLevel()==world,"Default respawn uses expected dimension");
   Files.writeString(save,point.toString());manager.setDirty();Files.write(Path.of("spawn-checks.txt"),checks);Files.writeString(Path.of("spawn-result.txt"),"PASS "+checks.size()+" isolated Spread Spawnpoints runtime checks\n");
  }catch(Throwable t){t.printStackTrace();try{Files.writeString(Path.of("spawn-result.txt"),"FAIL "+t);Files.write(Path.of("spawn-checks.txt"),checks);}catch(Exception ignored){}}
  event.getServer().halt(false);
 });}
}
