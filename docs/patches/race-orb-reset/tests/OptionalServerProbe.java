package local.optionalrace;
import com.eiden.iourusraces.race.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
@Mod("optional_race_probe")
public class OptionalServerProbe {
 public OptionalServerProbe(){NeoForge.EVENT_BUS.addListener(this::start);}
 void start(ServerStartedEvent e){var s=e.getServer();try{
  if(net.neoforged.fml.ModList.get().isLoaded("skill_tree_rpgs"))throw new AssertionError("Fixture must exclude RPG Skill Tree");
  var p=net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(s.overworld());
  com.eiden.iourusraces.data.PlayerRaceState.get(s).setRace(p.getUUID(),Race.ELF);
  if(PlayerRaceData.getRace(p)!=Race.ELF)throw new AssertionError("native server identity");
  java.nio.file.Files.writeString(java.nio.file.Path.of("optional-result.txt"),"PASS dedicated race mod loads without optional RPG classes; server race data works");
 }catch(Throwable t){t.printStackTrace();try{java.nio.file.Files.writeString(java.nio.file.Path.of("optional-result.txt"),"FAIL "+t);}catch(Exception ignored){}}finally{s.halt(false);}}
}
