package net.bandit.many_bows.pack;

import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/** Keep registered legacy IDs while removing their acquisition and benefits. */
@EventBusSubscriber(modid="too_many_bows")
public final class PackPolicy {
    public static final Set<String> REMOVED=Set.of("too_many_bows:stormbound_signet", "too_many_bows:sharpshot_ring", "too_many_bows:dead_eyes_pendant");
    public static boolean removed(ItemStack stack) {return REMOVED.contains(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());}
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void creative(BuildCreativeModeTabContentsEvent event) {
        event.removeIf(PackPolicy::removed,CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void tooltip(ItemTooltipEvent event) {
        if(!removed(event.getItemStack()))return;
        var lines=event.getToolTip();
        while(lines.size()>1)lines.removeLast();
        lines.add(Component.literal("Removed in this modpack; legacy item has no effect."));
    }
}
