package net.skill_tree_rpgs.compat;
import java.util.*;
import net.minecraft.resources.Identifier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.ItemStack;
import net.spell_engine.internals.container.SpellContainerSource;
import net.spell_engine.internals.PriestRules;
import net.spell_engine.rpg_series.config.ConfigFile;
import net.spell_engine.rpg_series.config.EffectConfig;
import net.spell_power.api.SpellSchools;

public final class PaladinPassives {
 public static final Identifier ARMOR=Identifier.parse("paladin_pack:vanguard_equipped"),HEALING=Identifier.parse("paladin_pack:guardian_empty_offhand");
 public static final String VANGUARD="skill_tree_rpgs:paladin_tier_1_passive_1",GUARDIAN="skill_tree_rpgs:paladin_tier_1_passive_2";
 public static boolean owns(ServerPlayer player,String id){return player instanceof SpellContainerSource.Owner owner&&owner.getSpellContainers().passives().stream().anyMatch(h->h.is(Identifier.parse(id)));}
 public static boolean shield(ItemStack item){return !item.isEmpty()&&(item.has(DataComponents.BLOCKS_ATTACKS)||item.canPerformAction(net.neoforged.neoforge.common.ItemAbility.get("shield_block")));}
 public static boolean shieldEquipped(ServerPlayer player){for(var slot:EquipmentSlot.VALUES)if(shield(player.getItemBySlot(slot)))return true;return false;}
 private static void modifier(AttributeInstance attribute,Identifier id,double amount){
  if(attribute==null)return;var old=attribute.getModifier(id);
  if(amount==0){if(old!=null)attribute.removeModifier(id);return;}
  if(old!=null&&Math.abs(old.amount()-amount)>1e-8){attribute.removeModifier(id);old=null;}
  if(old==null)attribute.addTransientModifier(new AttributeModifier(id,amount,AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
 }
 public static double bonus(ServerPlayer player){if(!player.isAlive()||!owns(player,GUARDIAN))return 0;var stack=player.getEffect(PriestRules.effect("skill_tree_rpgs:vengeance"));return .02+(stack==null?0:Math.min(4,stack.getAmplifier()+1)*(shieldEquipped(player)?.03:.02));}
 public static void refresh(ServerPlayer player){
  boolean vanguard=player.isAlive()&&owns(player,VANGUARD),guardian=player.isAlive()&&owns(player,GUARDIAN);
  modifier(player.getAttribute(Attributes.ARMOR),ARMOR,0);modifier(player.getAttribute(SpellSchools.HEALING.attributeEntry),ARMOR,0);
  clean(player,"skill_tree_rpgs:vengeance",guardian);double amount=bonus(player);modifier(player.getAttribute(SpellSchools.HEALING.attributeEntry),HEALING,amount);modifier(player.getAttribute(Attributes.ARMOR),HEALING,amount);
  clean(player,"skill_tree_rpgs:redoubt",false);clean(player,"skill_tree_rpgs:vengeance",guardian);
 }
 private static void clean(ServerPlayer player,String id,boolean owns){
  var effect=PriestRules.effect(id);var old=player.getEffect(effect);if(old==null)return;
  if(!owns){player.removeEffect(effect);return;}
  if(old.getAmplifier()>3||old.getDuration()>100){int amplifier=Math.min(3,old.getAmplifier()),duration=Math.min(100,old.getDuration());player.removeEffect(effect);player.addEffect(new net.minecraft.world.effect.MobEffectInstance(effect,duration,amplifier),player);}
 }
 public static void configure(ConfigFile.Effects config){
 net.spell_engine.internals.PaladinRevamp.configureSkill(config);
  config.effects.put("skill_tree_rpgs:redoubt",new EffectConfig(List.of()));
  config.effects.put("skill_tree_rpgs:vengeance",new EffectConfig(List.of()));
  net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.tick.PlayerTickEvent.Post e)->{if(e.getEntity() instanceof ServerPlayer p)refresh(p);});
  net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.entity.living.LivingDeathEvent e)->{if(e.getEntity() instanceof ServerPlayer p){modifier(p.getAttribute(Attributes.ARMOR),ARMOR,0);modifier(p.getAttribute(SpellSchools.HEALING.attributeEntry),ARMOR,0);modifier(p.getAttribute(SpellSchools.HEALING.attributeEntry),HEALING,0);modifier(p.getAttribute(Attributes.ARMOR),HEALING,0);p.removeEffect(PriestRules.effect("skill_tree_rpgs:redoubt"));p.removeEffect(PriestRules.effect("skill_tree_rpgs:vengeance"));}});
 }
}
