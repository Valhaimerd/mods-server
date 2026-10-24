package com.emandev.bifrost;

import com.eiden.iourusraces.race.PlayerRaceData;
import com.eiden.iourusraces.race.Race;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import team.creative.playerrevive.server.PlayerReviveServer;
import de.mrstilu2k6.better_revive.util.DownedPlayerManager;

/** Stored Iourus identity is authoritative; carrying a focus never grants a race. */
@EventBusSubscriber(modid="bifrost")
public final class AsgardianAccess {
    private static final Race ASGARDIAN = requireRace();
    private static Race requireRace() {
        try { return Race.valueOf("ASGARDIAN"); }
        catch (IllegalArgumentException e) {
            throw new IllegalStateException("Asgardian_Mod requires the pack's Asgardian-enabled Iourus Races JAR on both client and server.",e);
        }
    }
    public static void verifyIntegration() { requireRace(); }
    public static boolean downed(Player player) {
        if (ModList.get().isLoaded("playerrevive") && PlayerReviveServer.isBleeding(player)) return true;
        return ModList.get().isLoaded("better_revive") && DownedPlayerManager.isDowned(player.getUUID());
    }
    public static boolean canUse(Entity entity) {
        return entity instanceof Player player && !player.level().isClientSide()
            && player.isAlive() && !player.isSpectator() && !downed(player)
            && PlayerRaceData.getRace(player)==ASGARDIAN;
    }
    public static boolean allow(Entity entity) {
        if (entity!=null && entity.level().isClientSide()) return true;
        if (canUse(entity)) return true;
        if (entity instanceof Player player) player.sendSystemMessage(Component.literal(
            PlayerRaceData.getRace(player)!=ASGARDIAN ? "Only Asgardians can use Bifrost equipment and markers."
                : "You cannot use the Bifrost while downed, dead or spectating."));
        return false;
    }
    static boolean focus(ItemStack stack) {
        return !AsgardWeapons.kind(stack).isEmpty();
    }
    @SubscribeEvent public static void place(BlockEvent.EntityPlaceEvent event) {
        if (event.getPlacedBlock().is(ModBlocks.BIFROST_MARKER.get()) && !allow(event.getEntity())) event.setCanceled(true);
    }
    @SubscribeEvent public static void attack(AttackEntityEvent event) {
        if (!focus(event.getEntity().getMainHandItem())) return;
        if (!allow(event.getEntity())) {event.setCanceled(true);return;}
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            if (!WeaponOwnership.canUse(player,player.getMainHandItem()) || AsgardAbilities.active(player)) {event.setCanceled(true);return;}
            if (BifrostSkills.customAbilitiesEnabled() && player.isShiftKeyDown() && !AsgardWeapons.kind(player.getMainHandItem()).equals("hofund")) {
                WeaponOwnership.throwWeapon(player,player.getMainHandItem(),false);event.setCanceled(true);
            }
        }
    }
    @SubscribeEvent public static void tooltip(ItemTooltipEvent event) {
        if (focus(event.getItemStack()) || event.getItemStack().is(ModItems.BIFROST_MARKER.get()))
            event.getToolTip().add(Component.literal("Requires Asgardian race").withStyle(ChatFormatting.GOLD));
        String kind=AsgardWeapons.kind(event.getItemStack());
        if(!kind.isEmpty()) {
            event.getToolTip().add(Component.literal("Bifrost: use the last skill slot to open destinations").withStyle(ChatFormatting.GRAY));
        }
    }
}
