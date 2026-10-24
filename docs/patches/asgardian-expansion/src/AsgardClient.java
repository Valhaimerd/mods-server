package com.emandev.bifrost;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

/** Loaded only through the distribution-gated client registration branch. */
public final class AsgardClient {
    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(Identifier.fromNamespaceAndPath("bifrost", "abilities"));
    public static final KeyMapping ABILITY = new KeyMapping("key.bifrost.asgardian_ability", InputConstants.Type.KEYSYM, -1, CATEGORY);
    private static boolean wasUsing;
    private static boolean attackWasDown;
    private AsgardClient() {}

    public static void register(IEventBus bus) {
        if(!BifrostSkills.customAbilitiesEnabled())return;
        bus.addListener(AsgardClient::keys);
        NeoForge.EVENT_BUS.addListener(AsgardClient::tick);
        NeoForge.EVENT_BUS.addListener(AsgardClient::interact);
    }
    private static void keys(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        event.register(ABILITY);
    }
    private static void tick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) { wasUsing = false; attackWasDown = false; return; }
        String kind = AsgardWeapons.kind(mc.player.getMainHandItem());
        while (ABILITY.consumeClick()) {
            if (!kind.isEmpty() && mc.gui.screen() == null) {
                AsgardNetwork.send(2, 0);
                mc.gui.setScreen(new AsgardAbilityScreen(kind));
            }
        }
        boolean using = mc.player.isUsingItem() && !AsgardWeapons.kind(mc.player.getUseItem()).isEmpty();
        if (wasUsing && (!using || !mc.options.keyUse.isDown() || mc.gui.screen() != null)) AsgardNetwork.send(2, 0);
        wasUsing = using;
        if (!mc.options.keyAttack.isDown()) attackWasDown = false;
    }
    private static void interact(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isAttack() || event.getHand() != InteractionHand.MAIN_HAND) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gui.screen() != null) return;
        String kind = AsgardWeapons.kind(mc.player.getMainHandItem());
        if (!kind.equals("mjolnir") && !kind.equals("stormbreaker")) return;
        boolean air = mc.hitResult == null || mc.hitResult.getType() == HitResult.Type.MISS;
        boolean entity = mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.ENTITY;
        if (!air && !(entity && mc.player.isShiftKeyDown())) return;
        event.setCanceled(true);
        event.setSwingHand(false);
        // Holding attack cannot repeatedly launch after a return; another click is required.
        if (!attackWasDown) { attackWasDown = true; AsgardNetwork.send(3, 0); }
    }
}
