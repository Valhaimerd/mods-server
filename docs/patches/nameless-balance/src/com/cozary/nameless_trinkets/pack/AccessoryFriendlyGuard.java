package com.cozary.nameless_trinkets.pack;
@net.neoforged.fml.common.EventBusSubscriber(modid="nameless_trinkets")
public final class AccessoryFriendlyGuard {
 @net.neoforged.bus.api.SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.HIGHEST)
 public static void incoming(net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent event){
  if(AccessoryCombat.proc(event.getSource()) && event.getSource().getEntity() instanceof net.minecraft.world.entity.LivingEntity wearer
   && wearer!=event.getEntity() && AccessoryCombat.friendly(wearer,event.getEntity()))event.setCanceled(true);
 }
}
