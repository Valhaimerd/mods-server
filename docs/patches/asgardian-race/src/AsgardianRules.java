package com.eiden.iourusraces.race;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
/** Server-authoritative, once-per-hit racial damage; does not alter healing or class power snapshots. */
@EventBusSubscriber(modid="iourusraces")
public final class AsgardianRules {
 private static final Identifier MINING=Identifier.parse("iourusraces:asgardian_mining");
 private static final TagKey<DamageType> MAGIC=TagKey.create(Registries.DAMAGE_TYPE,Identifier.parse("c:is_magic"));
 public static void applyMining(Player player,Race race){
  if(race==Race.ASGARDIAN){var attribute=player.getAttribute(Attributes.BLOCK_BREAK_SPEED);if(attribute!=null)attribute.addPermanentModifier(new AttributeModifier(MINING,.02,AttributeModifier.Operation.ADD_MULTIPLIED_BASE));}
 }
 public static void removeMining(Player player){var attribute=player.getAttribute(Attributes.BLOCK_BREAK_SPEED);if(attribute!=null)attribute.removeModifier(MINING);}
 public static boolean magic(DamageSource source){
  return source.is(MAGIC)||source.is(DamageTypes.MAGIC)||source.is(DamageTypes.INDIRECT_MAGIC)||source.is(DamageTypes.DRAGON_BREATH)||source.is(DamageTypes.WITHER);
 }
 @SubscribeEvent
 public static void onLightning(net.neoforged.neoforge.event.entity.EntityStruckByLightningEvent event){
  if(event.getEntity() instanceof Player player&&PlayerRaceData.getRace(player)==Race.ASGARDIAN)event.setCanceled(true);
 }
 @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.HIGHEST)
 public static void onDamage(LivingIncomingDamageEvent event){
  var source=event.getSource();
  boolean targetAsgard=event.getEntity() instanceof Player target&&PlayerRaceData.getRace(target)==Race.ASGARDIAN;
  if(targetAsgard&&source.is(DamageTypes.LIGHTNING_BOLT)){event.setCanceled(true);return;}
  boolean magical=magic(source);float amount=event.getAmount();
  if(source.getEntity() instanceof Player attacker&&PlayerRaceData.getRace(attacker)==Race.ASGARDIAN
   &&(magical||source.is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE)||source.is(DamageTypes.PLAYER_ATTACK)))amount*=1.02f;
  if(targetAsgard&&magical)amount*=1.10f;
  if(amount!=event.getAmount())event.setAmount(amount);
 }
}
