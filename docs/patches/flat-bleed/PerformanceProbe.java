package local.performance;
import java.nio.file.*;import java.util.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.minecraft.core.*;import net.minecraft.resources.Identifier;
import net.minecraft.server.level.*;import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.*;import net.minecraft.world.level.storage.*;
import net.spell_engine.entity.*;import net.spell_engine.api.spell.*;
import net.spell_engine.api.spell.registry.SpellRegistry;
@Mod("flat_bleed_probe") public final class PerformanceProbe {
 final List<String> out=new ArrayList<>();int failures;
 void check(String name,boolean ok){out.add((ok?"PASS ":"FAIL ")+name);if(!ok)failures++;}
 public PerformanceProbe(){NeoForge.EVENT_BUS.addListener((ServerStartedEvent e)->e.getServer().execute(()->run(e.getServer())));}
 void run(net.minecraft.server.MinecraftServer server){try {
  var level=server.overworld();level.getChunk(0,0);
  var player=net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(level);local.audit.BleedProbe.run(level,player,out::add);
 }catch(Throwable t){t.printStackTrace();failures++;out.add("FAIL "+t);}
 try{Files.write(Path.of("bleed-measurements.txt"),out);Files.writeString(Path.of("bleed-result.txt"),(failures==0?"PASS ":"FAIL ")+out.size()+" flat Bleed checks; failures="+failures);}catch(Exception ex){throw new RuntimeException(ex);}server.halt(false);}
}
