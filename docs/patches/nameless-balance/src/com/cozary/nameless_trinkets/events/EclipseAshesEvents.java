package com.cozary.nameless_trinkets.events;

import com.cozary.nameless_trinkets.NamelessTrinkets;
import com.cozary.nameless_trinkets.init.ModItems;
import com.cozary.nameless_trinkets.util.TrinketUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = NamelessTrinkets.MOD_ID)
public class EclipseAshesEvents {

    @SubscribeEvent
    public static void expireSessions(net.neoforged.neoforge.event.tick.ServerTickEvent.Post event) {
        EclipseAshesHandler.cleanupInactive(event.getServer());
    }

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if(event.getSource().getEntity() instanceof Wolf wolf && EclipseAshesHandler.isPhantomWolf(wolf)) {
            event.setCanceled(true);
            var owner=EclipseAshesHandler.owner(wolf);
            if(owner!=null)com.cozary.nameless_trinkets.pack.AccessoryCombat.damage(owner,event.getEntity(),(float)Math.max(0,Math.min(3,wolf.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE))));
            EclipseAshesHandler.onPhantomWolfAttack(wolf,event.getEntity());
            return;
        }
        if(event.getEntity() instanceof Player player && !com.cozary.nameless_trinkets.pack.AccessoryCombat.proc(event.getSource())) {
            var stack=TrinketUtils.getEquippedTrinket(player,ModItems.ECLIPSE_ASHES.get());
            if(!stack.isEmpty() && event.getSource().getEntity() instanceof LivingEntity attacker)
                EclipseAshesHandler.onPlayerAttacked(player,attacker,stack);
        }
    }
}
