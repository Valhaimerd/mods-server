package com.emandev.bifrost;
import com.google.gson.*;
import com.mojang.serialization.Codec;
import java.util.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.*;

/** Game-tick cooldowns and passive counters survive relog/restart; no wall-clock exploits. */
public final class AsgardState extends SavedData {
    private final Map<String,Long> values=new HashMap<>();
    private static final SavedDataType<AsgardState> TYPE=new SavedDataType<>(BifrostMod.id("combat_state"),AsgardState::new,Codec.STRING.xmap(AsgardState::decode,AsgardState::encode));
    public static AsgardState get(MinecraftServer server){return server.overworld().getDataStorage().computeIfAbsent(TYPE);}
    static AsgardState decode(String text){var state=new AsgardState();JsonParser.parseString(text).getAsJsonObject().entrySet().forEach(e->state.values.put(e.getKey(),e.getValue().getAsLong()));return state;}
    String encode(){var object=new JsonObject();values.forEach(object::addProperty);return object.toString();}
    public long value(UUID owner,String key){return values.getOrDefault(owner+"/"+key,0L);}
    public void value(UUID owner,String key,long value){if(value==0)values.remove(owner+"/"+key);else values.put(owner+"/"+key,value);setDirty();}
    public static long now(ServerPlayer p){return p.level().getServer().overworld().getGameTime();}
    public static long get(ServerPlayer p,String key){return get(p.level().getServer()).value(p.getUUID(),key);}
    public static void put(ServerPlayer p,String key,long value){get(p.level().getServer()).value(p.getUUID(),key,value);}
}
