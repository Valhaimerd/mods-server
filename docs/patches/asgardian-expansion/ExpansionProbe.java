package com.emandev.bifrost;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
@Mod("asgard_expansion_probe") public final class ExpansionProbe {
    final List<String> checks=new ArrayList<>();
    void check(String name,boolean ok){if(!ok)throw new AssertionError(name);checks.add("PASS "+name);}
    int warmup=-1;
    public ExpansionProbe(){NeoForge.EVENT_BUS.addListener(this::start);NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.tick.ServerTickEvent.Post e)->{if(warmup>0&&--warmup==0)exercise(e.getServer());});}
    void start(ServerStartedEvent event){var level=event.getServer().overworld();for(int z=-1;z<=2;z++)for(int x=-1;x<=1;x++){level.getChunk(x,z);level.setChunkForced(x,z,true);}warmup=40;}
    void exercise(net.minecraft.server.MinecraftServer server){server.execute(()->{
        String result;try{
            check("Stormbreaker registered",BuiltInRegistries.ITEM.get(Identifier.parse("bifrost:stormbreaker")).isPresent());
            run(server);result="PASS "+checks.size()+" Asgardian expansion checks";
        }catch(Throwable t){t.printStackTrace();result="FAIL "+t;}
        try{Files.write(Path.of("expansion-measurements.txt"),checks);Files.writeString(Path.of("expansion-result.txt"),result);}catch(Exception e){throw new RuntimeException(e);}server.halt(false);
    });}
    void run(net.minecraft.server.MinecraftServer server)throws Exception{
        CombatProbe.run(server,checks);
        OwnershipProbe.run(server,checks);
        TransitProbe.run(server,checks);
    }
}
