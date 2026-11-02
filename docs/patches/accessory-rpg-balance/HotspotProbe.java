package local.accessoryperf;

import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import top.theillusivec4.curios.api.*;
import top.theillusivec4.curios.api.event.CurioChangeEvent;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.common.inventory.DynamicStackHandler;
import com.cozary.nameless_trinkets.init.*;
import com.cozary.nameless_trinkets.pack.PackUniqueness;
import com.cozary.nameless_trinkets.events.WoundbearerHandler;

/** Test-only operation counters; never ship this mod or instrumented item classes. */
@Mod("accessory_hotspot_probe")
public class HotspotProbe {
    static int scans;
    int ticks;
    final List<String> results = new ArrayList<>();
    public static void countScan() { scans++; }
    public HotspotProbe() { NeoForge.EVENT_BUS.addListener(this::tick); }
    void check(boolean success, String description) { results.add((success ? "PASS " : "FAIL ") + description); }
    static class Slots extends DynamicStackHandler {
        int reads;
        Slots(FakePlayer player) { super(21, i -> new SlotContext("test", player, i, false, true)); }
        @Override public ItemStack getStackInSlot(int index) { reads++; return super.getStackInSlot(index); }
    }
    static boolean active(SlotContext context, ItemStack stack) throws Exception {
        try { return (boolean)PackUniqueness.class.getMethod("activeTick", SlotContext.class, ItemStack.class).invoke(null, context, stack); }
        catch (NoSuchMethodException oldBuild) { return PackUniqueness.active(context, stack); }
    }
    void tick(ServerTickEvent.Post event) {
        if (++ticks != 60) return;
        var server = event.getServer();
        try {
            var level = server.overworld(); level.getChunk(0,0);
            var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "HotspotProbe"));
            player.setPos(0, 65, 0); player.setHealth(20);
            var attack = player.getAttribute(Attributes.ATTACK_DAMAGE); attack.setBaseValue(100);
            var context = new SlotContext("hands", player, 0, false, true);
            var wound = new ItemStack(ModItems.WOUNDBEARER.get());
            wound.set(ModDataComponents.WOUNDBEARER_DAMAGE.get(), 2F);
            WoundbearerHandler.LAST_DAMAGE_TICKS.put(player.getUUID(), player.tickCount);
            var item = (ICurioItem)wound.getItem(); item.curioTick(context, wound);
            int updates = 0;
            for (int i=0; i<200; i++) {
                player.getAttributes().getAttributesToUpdate().clear();
                item.curioTick(context, wound);
                if (player.getAttributes().getAttributesToUpdate().contains(attack)) updates++;
            }
            results.add("METRIC stable_woundbearer_dirty_updates=" + updates + " / 200 calls");
            check(updates == 0, "unchanged Woundbearer does not dirty attack attributes");
            check(Math.abs(player.getAttributeValue(Attributes.ATTACK_DAMAGE)-102)<.001, "Woundbearer amount preserved");
            wound.set(ModDataComponents.WOUNDBEARER_DAMAGE.get(), 1F); item.curioTick(context,wound);
            check(Math.abs(player.getAttributeValue(Attributes.ATTACK_DAMAGE)-101)<.001, "changed Woundbearer updates immediately");
            wound.set(ModDataComponents.WOUNDBEARER_DAMAGE.get(), 0F); item.curioTick(context,wound);
            check(Math.abs(player.getAttributeValue(Attributes.ATTACK_DAMAGE)-100)<.001, "empty Woundbearer removes bonus");

            var tickStack = new ItemStack(ModItems.TICK.get());
            player.getPersistentData().putLong("nameless_trinkets:proc_tick", level.getGameTime()+20);
            scans = 0;
            for(int i=0;i<200;i++) ((ICurioItem)tickStack.getItem()).curioTick(context,tickStack);
            results.add("METRIC cooling_tick_entity_queries=" + scans + " / 200 calls");
            check(scans == 0, "Tick cooldown checked before querying world entities");

            var items = BuiltInRegistries.ITEM.stream().filter(i -> i != Items.AIR).limit(21).toList();
            int totalReads = 0;
            for(int wearer=0;wearer<24;wearer++) {
                var p = new FakePlayer(level,new GameProfile(UUID.randomUUID(),"Loadout"+wearer));
                var slots = new Slots(p);
                for(int i=0;i<21;i++) slots.setStackInSlot(i,new ItemStack(items.get(i)));
                var handler = (ICurioStacksHandler)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{ICurioStacksHandler.class},(proxy,method,args)-> {
                    if(method.getName().equals("getStacks")) return slots;
                    if(method.getName().equals("toString")) return "CountingSlots";
                    throw new UnsupportedOperationException(method.getName());
                });
                CuriosApi.getCuriosInventory(p).orElseThrow().setCurios(new LinkedHashMap<>(Map.of("test",handler)));
                slots.reads=0;
                for(int i=0;i<21;i++) {
                    var ctx=new SlotContext("test",p,i,false,true);
                    check(active(ctx,new ItemStack(items.get(i))),"unique slot active "+wearer+":"+i);
                }
                totalReads+=slots.reads;
                var duplicate = new ItemStack(items.get(0));
                var previous=slots.getStackInSlot(1);
                slots.setStackInSlot(1,duplicate);
                NeoForge.EVENT_BUS.post(new CurioChangeEvent.Item(p,"test",1,previous,duplicate));
                check(!active(new SlotContext("test",p,1,false,true),duplicate),"same-tick insertion excludes duplicate "+wearer);
                previous=slots.getStackInSlot(0); slots.setStackInSlot(0,ItemStack.EMPTY);
                NeoForge.EVENT_BUS.post(new CurioChangeEvent.Item(p,"test",0,previous,ItemStack.EMPTY));
                check(active(new SlotContext("test",p,1,false,true),duplicate),"same-tick primary removal promotes next copy "+wearer);
                check(!active(new SlotContext("test",p,1,true,true),duplicate),"cosmetic duplicate remains inactive "+wearer);
                previous=slots.getStackInSlot(5);
                var inserted=new ItemStack(items.get(10));slots.setStackInSlot(5,inserted);
                NeoForge.EVENT_BUS.post(new CurioChangeEvent.Item(p,new SlotContext("test",p,5,false,true),previous,inserted));
                check(!active(new SlotContext("test",p,10,false,true),new ItemStack(items.get(10))),"item event invalidates an earlier inserted duplicate "+wearer);
                check(active(new SlotContext("test",p,5,false,true),inserted),"earlier inserted duplicate becomes primary "+wearer);
                p.tickCount++;slots.reads=0;
                active(new SlotContext("test",p,5,false,true),inserted);
                check(slots.reads>=21,"next entity tick rebuilds the slot index "+wearer);
            }
            results.add("METRIC unique_loadout_slot_reads="+totalReads+" / 24 wearers x 21 items");
            check(totalReads<=24*42,"duplicate prevention has linear slot reads per wearer per tick");
        } catch(Throwable failure) { results.add("ERROR "+failure); failure.printStackTrace(); }
        finally {
            try { Files.write(Path.of("hotspot-results.txt"),results); }
            catch(Exception e) { throw new RuntimeException(e); }
            results.stream().filter(s->!s.startsWith("PASS unique slot")).forEach(System.out::println);
            server.halt(false);
        }
    }
}
