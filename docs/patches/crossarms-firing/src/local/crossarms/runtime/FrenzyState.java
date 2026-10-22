package local.crossarms.runtime;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/** Player-owned burst state is shared across hands and survives weapon switches. */
public final class FrenzyState {
    private FrenzyState() {}
    public static boolean applies(LivingEntity shooter, ItemStack stack) {
        if (!(stack.getItem() instanceof CrossbowItem)) return false;
        var frenzy=shooter.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(Identifier.parse("crossarms:frenzy"));
        return frenzy.isPresent() && EnchantmentHelper.getItemEnchantmentLevel(frenzy.get(),stack)>0;
    }
    public static int score(LivingEntity entity, String key) {
        var board=entity.level().getScoreboard();var objective=board.getObjective(key);
        if (objective==null) return 0;
        var value=board.getPlayerScoreInfo(entity,objective);
        return value==null?0:value.value();
    }
    private static void set(LivingEntity entity, String key, int value) {
        var board=entity.level().getScoreboard();var objective=board.getObjective(key);
        if (objective!=null) board.getOrCreatePlayerScore(entity,objective).set(value);
    }
    public static boolean empty(LivingEntity entity) {return entity instanceof ServerPlayer && score(entity,"frenzy_empty")==1;}
    public static void notice(LivingEntity entity) {
        if(score(entity,"frenzy_notice")>0&&entity instanceof ServerPlayer player) player.sendOverlayMessage(Component.translatableWithFallback("crossarms.pack.frenzy.empty","Empty - Reload"));
    }
    public static void fired(LivingEntity entity, net.minecraft.world.InteractionHand hand) {
        if (!(entity instanceof ServerPlayer)) return;
        entity.swingTime=-1;entity.swing(hand,true);
        int count=score(entity,"frenzy_shots")+1;set(entity,"frenzy_shots",count);set(entity,"frenzy_idle",0);
        if(count>=26){set(entity,"frenzy_empty",1);set(entity,"frenzy_notice",60);notice(entity);}
    }
    public static void cranked(LivingEntity entity) {
        if(score(entity,"frenzy_notice")>0&&entity instanceof ServerPlayer player) player.sendOverlayMessage(Component.empty());
        set(entity,"frenzy_notice",0);
        set(entity,"frenzy_shots",0);set(entity,"frenzy_idle",0);set(entity,"frenzy_empty",0);
    }
    public static float spread(LivingEntity entity) {
        int heat=score(entity,"heat");
        return heat<=10?1.15f:heat<=150?1.25f:heat<=300?1.40f:heat<=450?1.60f:heat<=610?1.85f:2.20f;
    }
}
