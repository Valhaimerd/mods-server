package com.emandev.bifrost;
import java.nio.file.*;import java.util.*;
import net.neoforged.fml.common.Mod;import net.neoforged.neoforge.common.NeoForge;import net.neoforged.neoforge.event.server.ServerStartedEvent;
@Mod("asgard_expansion_probe") public final class OwnershipServerProbe {
 public OwnershipServerProbe(){NeoForge.EVENT_BUS.addListener(this::start);}
 void start(ServerStartedEvent event){var server=event.getServer();server.execute(()->{String result;List<String> out=new ArrayList<>();try{OwnershipProbe.run(server,out);result="PASS "+String.join("; ",out);}catch(Throwable e){e.printStackTrace();result="FAIL "+e;}try{Files.writeString(Path.of("expansion-result.txt"),result);}catch(Exception e){throw new RuntimeException(e);}server.halt(false);});}
}
