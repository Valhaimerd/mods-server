package com.cozary.nameless_trinkets.pack;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.LivingEntity;
import java.util.Map;
import java.util.HashMap;
import java.util.WeakHashMap;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.event.CurioChangeEvent;
import top.theillusivec4.curios.api.event.SlotModifiersUpdatedEvent;

/** One active equipped copy per item type; never moves or deletes existing stacks. */
@EventBusSubscriber(modid="nameless_trinkets")
public final class PackUniqueness {
    private PackUniqueness() {}
    // Values contain no entity/handler references: weak keys must remain collectible.
    private static final Map<LivingEntity,Snapshot> TICK_SNAPSHOTS=new WeakHashMap<>();
    private record Slot(String identifier,int index) {
        boolean matches(SlotContext context) { return identifier.equals(context.identifier()) && index==context.index() && !context.cosmetic(); }
    }
    private record Snapshot(long gameTime,int entityTick,Map<Item,Slot> first) {}
    private static void invalidate(LivingEntity entity) {
        synchronized(TICK_SNAPSHOTS){TICK_SNAPSHOTS.remove(entity);}
    }
    @SubscribeEvent
    public static void changed(CurioChangeEvent.Item event) {
        // Component/timer changes cannot change which slot owns an item type.
        if(event.getFrom().isEmpty()!=event.getTo().isEmpty() || event.getFrom().getItem()!=event.getTo().getItem())invalidate(event.getEntity());
    }
    @SubscribeEvent
    public static void resized(SlotModifiersUpdatedEvent event) { invalidate(event.getEntity()); }
    /** Share one live slot index across worn callbacks, with mutation invalidation. */
    public static boolean activeTick(SlotContext context,ItemStack stack) {
        if(stack.isEmpty())return false;
        if(context.entity().level().isClientSide())return active(context,stack);
        var inventory=CuriosApi.getCuriosInventory(context.entity()).orElse(null);if(inventory==null)return true;
        synchronized(TICK_SNAPSHOTS) {
            long now=context.entity().level().getGameTime();
            var snapshot=TICK_SNAPSHOTS.get(context.entity());
            if(snapshot!=null && snapshot.gameTime()==now && snapshot.entityTick()==context.entity().tickCount) {
                var first=snapshot.first().get(stack.getItem());
                // Also detect primary removal during a callback before Curios posts its event.
                var handler=first==null?null:inventory.getCurios().get(first.identifier());
                if(first!=null && handler!=null && first.index()<handler.getStacks().getSlots()
                    && handler.getStacks().getStackInSlot(first.index()).is(stack.getItem()))return first.matches(context);
                if(first==null)return true;
            }
            var first=new HashMap<Item,Slot>();
            for(var entry:inventory.getCurios().entrySet()) {
                var slots=entry.getValue().getStacks();
                for(int i=0;i<slots.getSlots();i++) {
                    var equipped=slots.getStackInSlot(i);
                    if(!equipped.isEmpty())first.putIfAbsent(equipped.getItem(),new Slot(entry.getKey(),i));
                }
            }
            TICK_SNAPSHOTS.put(context.entity(),new Snapshot(now,context.entity().tickCount,first));
            var owner=first.get(stack.getItem());return owner==null || owner.matches(context);
        }
    }
    // Read live stacks: Curios findCurios can cache a previous result within the tick.
    public static boolean canEquip(SlotContext context,ItemStack stack) {
        invalidate(context.entity());
        if(stack.isEmpty())return false;
        var inventory=CuriosApi.getCuriosInventory(context.entity()).orElse(null);if(inventory==null)return true;
        for(var entry:inventory.getCurios().entrySet()) {
            var slots=entry.getValue().getStacks();
            for(int i=0;i<slots.getSlots();i++)
                if(slots.getStackInSlot(i).is(stack.getItem()) && !(entry.getKey().equals(context.identifier()) && i==context.index() && !context.cosmetic()))return false;
        }
        return true;
    }
    public static boolean active(SlotContext context,ItemStack stack) {
        invalidate(context.entity());
        if(stack.isEmpty())return false;
        var inventory=CuriosApi.getCuriosInventory(context.entity()).orElse(null);if(inventory==null)return true;
        for(var entry:inventory.getCurios().entrySet()) {
            var slots=entry.getValue().getStacks();
            for(int i=0;i<slots.getSlots();i++)
                if(slots.getStackInSlot(i).is(stack.getItem()))return entry.getKey().equals(context.identifier()) && i==context.index() && !context.cosmetic();
        }
        return true;
    }
    public static boolean inactive(SlotContext context,ItemStack stack) { return !active(context,stack); }
    public static boolean unequip(SlotContext context,ItemStack stack) { return canEquip(context,stack); }
}
