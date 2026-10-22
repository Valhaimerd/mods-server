package local.audit;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.puffish.skillsmod.api.SkillsAPI;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.api.spell.summon.*;
import net.spell_engine.entity.SummonedEntity;
import net.spell_engine.internals.*;
import net.spell_engine.internals.casting.SpellCaster;
import net.spell_engine.internals.container.SpellContainerSource;
import net.valhaimerd.orboblivioncompat.SkillOverhaul;
/** Native haste, summon-owner and automatic Adventurer pet-stat checks. Never ship. */
public final class TierTwoProbe {
 static ServerLevel level;static ServerPlayer player;static Consumer<String> out;
 static Holder<Spell> spell(String id){return SpellRegistry.from(level).get(Identifier.parse(id)).orElseThrow();}
 static void eq(String label,double actual,double expected){if(Math.abs(actual-expected)>.005)throw new AssertionError(label+" "+actual+" != "+expected);out.accept("PASS "+label+" actual="+actual+" expected="+expected);}
 static void equip(List<String> passives,List<String> mods){
  var owner=(SpellContainerSource.Owner)player;var old=owner.getSpellContainers();var ids=new ArrayList<String>(passives);ids.addAll(mods);
  var container=new net.spell_engine.api.spell.container.SpellContainer(net.spell_engine.api.spell.container.SpellContainer.ContentType.NONE,"","",0,ids);
  owner.setSpellContainers(new SpellContainerSource.Result(old.activeContainer(),List.of(),passives.stream().map(TierTwoProbe::spell).toList(),mods.stream().map(TierTwoProbe::spell).toList(),List.of(new SpellContainerSource.SourcedContainer("tier_two_probe",ItemStack.EMPTY,container))));owner.spellModifierCache().clear();
 }
 public static void run(ServerLevel world,ServerPlayer owner,Consumer<String> sink)throws Exception{
  level=world;player=owner;out=sink;var previous=((SpellContainerSource.Owner)player).getSpellContainers();
  var rangedHaste=player.getAttribute(BuiltInRegistries.ATTRIBUTE.get(Identifier.parse("ranged_weapon:haste")).orElseThrow());
  var magicHaste=player.getAttribute(BuiltInRegistries.ATTRIBUTE.get(Identifier.parse("spell_power:haste")).orElseThrow());
  double oldRanged=rangedHaste.getBaseValue(),oldMagic=magicHaste.getBaseValue();
  try {
   equip(List.of(),List.of());var barrage=spell("archers:barrage");
   eq("Barrage cast",barrage.value().active.cast.duration,.1);eq("Barrage cooldown",barrage.value().cost.cooldown.duration,8);
   eq("Barrage retained damage",barrage.value().arrow_perks.damage_multiplier,.7);eq("Barrage interval three ticks",barrage.value().deliver.shoot_arrow.launch_properties.extra_launch_delay,3);eq("Barrage zero divergence",barrage.value().deliver.shoot_arrow.divergence,0);
   eq("Extensive adds two arrows",spell("skill_tree_rpgs:archer_tier_3_spell_1_modifier_1").value().modifiers.getFirst().projectile_launch.extra_launch_count,2);
   eq("Blood Barrage six percent R",spell("skill_tree_rpgs:archer_tier_3_spell_1_modifier_2").value().modifiers.getFirst().impacts.getFirst().action.heal.spell_power_coefficient,.06);
   var wolf=spell("archers:spirit_wolf");eq("Wolf cooldown",wolf.value().cost.cooldown.duration,35);eq("Wolf range",wolf.value().range,32);eq("Wolf active lifetime",wolf.value().impacts.getFirst().action.summon.behaviour.lifespan.active_seconds,20);
   eq("Enduring adds ten seconds",spell("skill_tree_rpgs:archer_tier_3_spell_2_modifier_2").value().modifiers.getFirst().summon_behaviour.lifespan.active_seconds_add,10);
   for(String id:List.of("archers:power_shot","archers:entangling_roots","archers:barrage","archers:spirit_wolf","archers:rain_of_arrows","archers:magic_arrow","skill_tree_rpgs:archer_tier_1_passive_1","skill_tree_rpgs:archer_tier_1_passive_2","skill_tree_rpgs:archer_tier_2_passive_1","skill_tree_rpgs:archer_tier_2_passive_2","skill_tree_rpgs:archer_tier_3_passive_1","skill_tree_rpgs:archer_tier_3_passive_2","skill_tree_rpgs:weapon_bow_passive_1","skill_tree_rpgs:weapon_bow_passive_2","skill_tree_rpgs:weapon_crossbow_passive_1","skill_tree_rpgs:weapon_crossbow_passive_2")){
    var h=spell(id);eq(id+" haste flag",h.value().cost.cooldown.haste_affected?1:0,1);
    rangedHaste.setBaseValue(100);magicHaste.setBaseValue(100);double base=SpellParameters.getCooldownDuration(player,h);
    rangedHaste.setBaseValue(200);magicHaste.setBaseValue(200);eq(id+" native double haste halves cooldown",SpellParameters.getCooldownDuration(player,h),base/2);
    if(h.value().type==Spell.Type.ACTIVE&&h.value().active.cast!=null){
     rangedHaste.setBaseValue(100);magicHaste.setBaseValue(100);double cast=SpellParameters.getCastTimeDetails(player,h.value()).length();
     rangedHaste.setBaseValue(200);magicHaste.setBaseValue(200);eq(id+" native double haste cast",SpellParameters.getCastTimeDetails(player,h.value()).length(),cast/2);
    }
   }
   rangedHaste.setBaseValue(100);magicHaste.setBaseValue(100);
   equip(List.of(),List.of("skill_tree_rpgs:archer_tier_3_spell_1_root"));eq("Improved Barrage effective cooldown",SpellParameters.getCooldownDuration(player,barrage),7);
   equip(List.of("skill_tree_rpgs:archer_tier_2_passive_1"),List.of());eq("Momentum cooldown",spell("skill_tree_rpgs:archer_tier_2_passive_1").value().cost.cooldown.duration,1);
   player.removeAllEffects();var rhythm=BuiltInRegistries.MOB_EFFECT.get(Identifier.parse("skill_tree_rpgs:rhythm")).orElseThrow();
   for(int i=1;i<=3;i++){
    ((SpellCaster.Player)player).getCooldownManager().remove(Identifier.parse("skill_tree_rpgs:archer_tier_2_passive_1"));SpellTriggers.onRoll(player);
    eq("Momentum two stacks per roll capped five "+i,player.getEffect(rhythm).getAmplifier()+1,Math.min(2*i,5));eq("Momentum retains four-second Rhythm",player.getEffect(rhythm).getDuration(),80);
   }
   player.removeAllEffects();equip(List.of("skill_tree_rpgs:archer_tier_2_passive_2"),List.of());SpellTriggers.onRoll(player);
   var tactical=BuiltInRegistries.MOB_EFFECT.get(Identifier.parse("skill_tree_rpgs:tactical_maneuver")).orElseThrow();eq("Tactical guaranteed roll trigger",player.hasEffect(tactical)?1:0,1);
   eq("Tactical cooldown",spell("skill_tree_rpgs:archer_tier_2_passive_2").value().cost.cooldown.duration,10);
   player.removeAllEffects();equip(List.of(),List.of());tierThree();pets();
  }finally{player.removeAllEffects();rangedHaste.setBaseValue(oldRanged);magicHaste.setBaseValue(oldMagic);((SpellContainerSource.Owner)player).setSpellContainers(previous);((SpellContainerSource.Owner)player).spellModifierCache().clear();}
 }
 static void tierThree()throws Exception{
  var rain=spell("archers:rain_of_arrows");var arrow=spell("archers:magic_arrow");
  eq("Rain cooldown",rain.value().cost.cooldown.duration,20);eq("Rain damage",rain.value().impacts.getFirst().action.damage.spell_power_coefficient,.4);
  eq("Rain splash",rain.value().area_impact.radius,2.5);
  for(int branch=0;branch<3;branch++){
   equip(List.of(),branch==0?List.of():List.of("skill_tree_rpgs:archer_tier_4_spell_1_modifier_"+branch));
   var meteor=rain.value().deliver.meteor;var launch=meteor.launch_properties.copy();float radius=meteor.launch_radius;
   for(var m:SpellModifiers.of(player,rain)){if(m.projectile_launch!=null)launch.mutatingCombine(m.projectile_launch);radius+=m.meteor_launch_radius_add;}
   eq("Rain branch "+branch+" total arrows",1+launch.extra_launch_count,branch==2?40:30);
   eq("Rain branch "+branch+" spacing ticks",launch.extra_launch_delay,branch==1?2:5);
   eq("Rain branch "+branch+" scatter",radius,branch==1?2:1.5);
   eq("Rain branch "+branch+" splash unchanged",rain.value().area_impact.radius,2.5);
  }
  eq("Magic Arrow cooldown",arrow.value().cost.cooldown.duration,14);eq("Magic Arrow base range",arrow.value().range,32);
  eq("Magic Arrow full range",arrow.value().range+arrow.value().active.cast.charge.bonus.range_add,128);
  eq("Magic Arrow coefficient",arrow.value().impacts.getFirst().action.damage.spell_power_coefficient,2.5);
  for(boolean improved:new boolean[]{false,true}){
   equip(List.of(),improved?List.of("skill_tree_rpgs:archer_tier_4_spell_2_root"):List.of());double power=1;
   for(var m:SpellModifiers.of(player,arrow))if(m.power_modifier!=null)power+=m.power_modifier.power_multiplier;
   eq("Magic Arrow full-charge coefficient improved="+improved,2.5*power,improved?2.7:2.5);
   eq("Magic Arrow half charge improved="+improved,2.5*power*SpellParameters.chargeOutputMultiplier(arrow,.5f),improved?1.35:1.25);
  }
  var target=BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse("minecraft:zombie")).create(level,EntitySpawnReason.COMMAND);target.setPos(0,80,10);
  var follow=net.spell_engine.entity.SpellProjectile.class.getDeclaredMethod("followTarget");follow.setAccessible(true);
  for(boolean full:new boolean[]{false,true}){
   equip(List.of(),full?List.of("skill_tree_rpgs:archer_tier_4_spell_2_modifier_1"):List.of());
   var projectile=new net.spell_engine.entity.SpellProjectile(level,player,0,80,0,net.spell_engine.entity.SpellProjectile.Behaviour.FLY,arrow,new SpellExecution.ImpactContext(),arrow.value().deliver.projectile.projectile.perks.copy());
   projectile.setDeltaMovement(new net.minecraft.world.phys.Vec3(1.5,0,0));projectile.setFollowedTarget(target);follow.invoke(projectile);
   double angle=Math.toDegrees(Math.acos(projectile.getDeltaMovement().normalize().x));eq("Native homing turn full="+full,angle,full?90:10);
   eq("Homing preserves velocity",projectile.getDeltaMovement().length(),1.5);projectile.discard();
  }
  eq("Shared arrow homing not mutated",arrow.value().deliver.projectile.projectile.homing_angle,10);target.discard();
  var supercharge=spell("skill_tree_rpgs:archer_tier_3_passive_1").value();eq("Supercharge chance",supercharge.passive.triggers.getFirst().chance,1);eq("Supercharge retained arrow damage",supercharge.arrow_perks.damage_multiplier,1.25);eq("Supercharge cooldown",supercharge.cost.cooldown.duration,8);
  var deflection=spell("skill_tree_rpgs:archer_tier_3_passive_2").value();eq("Deflection cooldown",deflection.cost.cooldown.duration,40);eq("Deflection three charges",deflection.impacts.getFirst().action.status_effect.amplifier+1,3);
  equip(List.of(),List.of());
 }
 static void pets()throws Exception{
  var damage=player.getAttribute(BuiltInRegistries.ATTRIBUTE.get(Identifier.parse("ranged_weapon:damage")).orElseThrow());double before=damage.getBaseValue();
  var tree=SkillsAPI.getCategory(SkillOverhaul.CATEGORY).orElseThrow();tree.unlock(player);tree.resetSkills(player);tree.setExtraPoints(player,1000);
  boolean added=!level.players().contains(player);if(added)level.players().add(player);
  try {
   for(double r:new double[]{0,20,40})for(boolean improved:new boolean[]{false,true}){
    tree.resetSkills(player);tree.setExtraPoints(player,1000);damage.setBaseValue(r);
    var h=spell("archers:spirit_wolf");var def=h.value().impacts.getFirst().action.summon;var scaling=def.attribute_scaling;
    if(improved)scaling=AttributeScaling.merged(scaling,spell("skill_tree_rpgs:archer_tier_3_spell_2_root").value().modifiers.getFirst().summon_attribute_scaling);
    var wolf=(SummonedEntity)BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse("archers:spirit_wolf")).create(level,EntitySpawnReason.MOB_SUMMONED);
    wolf.onSummonedBySpell(new SpellSummoned.Args(player,h,def.behaviour,scaling,new SpellExecution.ImpactContext()));
    eq("Spirit Wolf resolves actual owner",wolf.getOwner()==player?1:0,1);
    double hp=20+(improved?.7:.5)*r,attack=3+.2*r;
    eq("Spirit Wolf native owner-scaled HP R="+r+" improved="+improved,wolf.getMaxHealth(),hp);eq("Wolf armor",wolf.getAttributeValue(Attributes.ARMOR),10+.05*r);eq("Wolf attack",wolf.getAttributeValue(Attributes.ATTACK_DAMAGE),attack);eq("Wolf resistance",wolf.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE),.2);
    for(String id:List.of("15trz7ezr8z7kcp8","qp2x45h0nbr233e1","e7fyzt06vz8mu5iv","5grntvcsxgm61gbq","kbheklivvbq2aaj8","87cn8ldpzmjljp0q","5frbav4zdc1hwquo"))tree.getSkill(id).orElseThrow().unlock(player);
    wolf.tickCount=20;NeoForge.EVENT_BUS.post(new EntityTickEvent.Post(wolf));
    eq("Automatic Adventurer +2 flat pet attack",wolf.getAttributeValue(Attributes.ATTACK_DAMAGE),attack+2);eq("Automatic pet HP",wolf.getMaxHealth(),hp+4);eq("Automatic pet speed",wolf.getAttributeValue(Attributes.MOVEMENT_SPEED),.35*1.03);eq("Automatic pet knockback",wolf.getAttributeValue(Attributes.ATTACK_KNOCKBACK),.1*r+.1);eq("Automatic pet knockback resistance",wolf.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE),.3);eq("Automatic pet damage reduction",SkillOverhaul.reducedDamage(wolf,20),19);
    tree.getSkill("iggdp0a33bljw4kl").orElseThrow().unlock(player);NeoForge.EVENT_BUS.post(new EntityTickEvent.Post(wolf));eq("Pet flat damage then Pack Leader multiplier",wolf.getAttributeValue(Attributes.ATTACK_DAMAGE),(attack+2)*1.2);
    for(int i=0;i<3;i++)NeoForge.EVENT_BUS.post(new EntityTickEvent.Post(wolf));eq("Repeated pet updates do not stack",wolf.getAttributeValue(Attributes.ATTACK_DAMAGE),(attack+2)*1.2);
    wolf.discard();
   }
  }finally{tree.resetSkills(player);damage.setBaseValue(before);if(added)level.players().remove(player);}
 }
}

