package net.spell_engine.internals;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.internals.casting.SpellCaster;
import net.spell_engine.rpg_series.config.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
/** Short scalar window, no retained entity map and no global tick listener. */
@EventBusSubscriber(modid="spell_engine")
public final class RogueAdjustments {
 private static final String WINDOW="rogue_pack_shadow_reset_until";
 private static final Identifier SHADOW=Identifier.parse("rogues:shadow_step");
 public static void configure(ConfigFile.Effects config){
  config.effects.put("skill_tree_rpgs:opportunist",new EffectConfig(List.of(new AttributeModifier("critical_strike:chance",1f,Operation.ADD_MULTIPLIED_BASE))));
 }
 public static void arrived(boolean success,LivingEntity caster,Holder<Spell> spell,Spell.Impact impact){
  if(success&&caster instanceof ServerPlayer p&&spell.is(SHADOW)&&impact.action.type==Spell.Impact.Action.Type.TELEPORT)
   p.getPersistentData().putLong(WINDOW,p.level().getServer().overworld().getGameTime()+80);
 }
 @SubscribeEvent(priority=EventPriority.LOWEST)
 public static void killed(LivingDeathEvent event){
  if(!(event.getSource().getEntity() instanceof ServerPlayer p)||!p.isAlive()||!PartyRules.harmfulAllowed(p,event.getEntity()))return;
  long token=p.getPersistentData().getLongOr(WINDOW,0),now=p.level().getServer().overworld().getGameTime();
  if(token<=now){p.getPersistentData().remove(WINDOW);return;}
  var victim=event.getEntity();
  // Let the cast impose its cooldown and revive/cancellation handlers finish first.
  ((net.spell_engine.utils.WorldScheduler)p.level()).schedule(1,()->{
   if(p.isRemoved()||!p.isAlive()||victim.isAlive()||p.getPersistentData().getLongOr(WINDOW,0)!=token)return;
   p.getPersistentData().remove(WINDOW);
   ((SpellCaster.Player)p).getCooldownManager().remove(SHADOW);
  });
 }
 @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){e.getEntity().getPersistentData().remove(WINDOW);}
 @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){e.getEntity().getPersistentData().remove(WINDOW);}
}
