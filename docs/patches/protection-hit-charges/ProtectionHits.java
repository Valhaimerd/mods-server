package net.spell_engine.internals;

import com.google.gson.JsonObject;
import java.util.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.tags.DamageTypeTags;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/** Finite attack blocks supplement Divine Protection's persistent percentage reduction. */
@EventBusSubscriber(modid="spell_engine")
public final class ProtectionHits {
 private static JsonObject state(JsonObject root) {
  if(!root.has("support"))return null;
  var support=root.getAsJsonObject("support");
  return support.has("protection")?support.getAsJsonObject("protection"):null;
 }
 private static int remaining(JsonObject r) {
  if(!r.has("blocks")) {
   r.addProperty("blocks",r.has("flat")?Math.max(0,r.get("flat").getAsInt()):0);
   r.remove("flat");
  }
  return Math.max(0,r.get("blocks").getAsInt());
 }
 public static boolean cast(boolean result,LivingEntity caster) {
  if(!result||caster.level().isClientSide())return result;
  for(var ally:PriestRules.nearby(caster,caster.position(),8))if(PartyRules.allies(caster,ally)) {
   var root=EffectDetails.data(ally);var r=state(root);if(r==null)continue;
   // Native delivery snapshots the count into the former flat field on each successful cast.
   if(r.has("flat")){r.addProperty("blocks",Math.max(0,r.get("flat").getAsInt()));r.remove("flat");}
   EffectDetails.save(ally,root);EffectDetails.update(ally);
  }
  return result;
 }
 @SubscribeEvent(priority=EventPriority.LOWEST)
 public static void incoming(LivingIncomingDamageEvent event) {
  var victim=event.getEntity();var root=EffectDetails.data(victim);var r=state(root);
  if(r==null||event.getAmount()<=0||r.get("expires").getAsDouble()<=victim.level().getGameTime()||!victim.hasEffect(PaladinRevamp.effect(PaladinRevamp.DP)))return;
  if(!(event.getSource().getEntity() instanceof LivingEntity attacker)||attacker==victim||!PartyRules.harmfulAllowed(attacker,victim)||event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY))return;
  int count=remaining(r);if(count<=0)return;
  r.addProperty("blocks",count-1);event.setCanceled(true);
  EffectDetails.save(victim,root);EffectDetails.update(victim);
 }
 public static List<String> details(List<String> original,LivingEntity e,String id) {
  if(!id.equals(PaladinRevamp.DP))return original;
  var r=state(EffectDetails.data(e));if(r==null)return original;
  var lines=new ArrayList<String>();
  for(var line:original)if(!line.startsWith("Blocks "))lines.add(line);
  lines.add("Attacks blocked: "+remaining(r)+" remaining");return lines;
 }
}
