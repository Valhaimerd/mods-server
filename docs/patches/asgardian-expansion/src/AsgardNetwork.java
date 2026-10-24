package com.emandev.bifrost;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** The packet contains intent only; never client damage, owner IDs or held durations. */
@EventBusSubscriber(modid="bifrost")
public final class AsgardNetwork {
    public static final String PROTOCOL = "asgardian-expansion-v1";
    private static final Map<UUID, Long> LAST_START = new HashMap<>();
    private AsgardNetwork() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar(PROTOCOL).playToServer(Intent.TYPE, Intent.CODEC,
            (payload, context) -> context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) handle(player, payload.action(), payload.skill());
            }));
    }

    public static void send(int action, int skill) {
        net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(new Intent(action, skill));
    }

    static boolean handle(ServerPlayer player, int action, int skill) {
        if (action < 0 || action > 3) return false;
        // A release must still cancel a channel after a swap, race loss, or downing.
        if (action == 2) {
            AsgardAbilities.endPrimary(player);
            return true;
        }
        ItemStack held = player.getMainHandItem();
        String kind = AsgardWeapons.kind(held);
        int skills = kind.equals("stormbreaker") ? 3 : kind.isEmpty() ? 0 : 4;
        if (skills == 0 || !AsgardianAccess.allow(player)) return false;
        if (!WeaponOwnership.canUse(player, held) || WeaponOwnership.isBusy(player, held)) {
            player.sendSystemMessage(Component.literal("This weapon is unclaimed, belongs to another owner, or is unavailable."));
            return false;
        }
        if (action == 0 && (skill < 0 || skill >= skills)) return false;
        if (action == 3 && !kind.equals("mjolnir") && !kind.equals("stormbreaker")) return false;
        if (action == 1 && (!player.isUsingItem()
            || player.getUsedItemHand() != net.minecraft.world.InteractionHand.MAIN_HAND
            || player.getUseItem() != held)) return false;
        long now = player.level().getGameTime();
        Long previous = LAST_START.get(player.getUUID());
        if (previous != null && now >= previous && now - previous < 4) return false;
        LAST_START.put(player.getUUID(), now);
        return switch (action) {
            case 0 -> AsgardAbilities.activate(player, skill);
            case 1 -> AsgardAbilities.beginPrimary(player);
            case 3 -> WeaponOwnership.throwWeapon(player, held, false);
            default -> false;
        };
    }

    @SubscribeEvent public static void stopped(ServerStoppedEvent event) { LAST_START.clear(); }

    public record Intent(int action, int skill) implements CustomPacketPayload {
        public static final Type<Intent> TYPE = new Type<>(BifrostMod.id("ability_intent"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Intent> CODEC = StreamCodec.of(
            (buffer, value) -> { buffer.writeByte(value.action); buffer.writeByte(value.skill); },
            buffer -> new Intent(buffer.readByte(), buffer.readByte()));
        @Override public Type<Intent> type() { return TYPE; }
    }
}
