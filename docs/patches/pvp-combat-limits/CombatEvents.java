package io.redspace.pvp_flagging;
import io.redspace.pvp_flagging.events.ServerEventsForge;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
@EventBusSubscriber(modid="pvp_flagging")
public final class CombatEvents {
 @SubscribeEvent(priority=EventPriority.LOWEST)
 public static void hit(LivingDamageEvent.Post e){
  if(!(e.getEntity() instanceof ServerPlayer victim)||!(e.getInflictedDamage()>0))return;
  var entity=e.getSource().getEntity();if(entity==null)entity=e.getSource().getDirectEntity();
  var owner=ServerEventsForge.playerSource(entity);if(owner==null||owner==victim)return;
  CombatRules.flag(owner);CombatRules.flag(victim);
 }
 @SubscribeEvent(priority=EventPriority.LOWEST)
 public static void breaking(BreakBlockEvent e){if(e.getPlayer() instanceof ServerPlayer p&&!CombatRules.attempt(p,false,1))e.setCanceled(true);}
 @SubscribeEvent(priority=EventPriority.LOWEST)
 public static void clicking(PlayerInteractEvent.LeftClickBlock e){
  if(e.getEntity() instanceof ServerPlayer p&&e.getAction()==PlayerInteractEvent.LeftClickBlock.Action.START&&!CombatRules.attempt(p,false,1))e.setCanceled(true);
 }
 @SubscribeEvent(priority=EventPriority.LOWEST)
 public static void placing(BlockEvent.EntityPlaceEvent e){
  if(e.getEntity() instanceof ServerPlayer p&&!CombatRules.attempt(p,true,e instanceof BlockEvent.EntityMultiPlaceEvent multi?multi.getReplacedBlockSnapshots().size():1))e.setCanceled(true);
 }
 @SubscribeEvent public static void tick(PlayerTickEvent.Post e){if(e.getEntity() instanceof ServerPlayer p)CombatRules.tick(p);}
 @SubscribeEvent public static void clone(PlayerEvent.Clone e){e.getOriginal().getPersistentData().getCompound(CombatRules.KEY).ifPresent(t->e.getEntity().getPersistentData().put(CombatRules.KEY,t.copy()));}
 @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p)CombatRules.sync(p);}
}
