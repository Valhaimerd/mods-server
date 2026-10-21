package local.audit;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.*;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.internals.*;
import net.spell_engine.internals.casting.*;
import net.spell_engine.internals.container.SpellContainerSource;
import net.spell_engine.internals.impact.SpellImpacts;
import net.spell_engine.internals.target.SpellTarget;
import net.spell_power.api.*;
public final class PassiveProbe {
 static ServerLevel level;static ServerPlayer player;static Consumer<String> out;
 static Holder<Spell> spell(String id){return SpellRegistry.from(level).get(Identifier.parse(id.contains(":")?id:"skill_tree_rpgs:"+id)).orElseThrow();}
 static void eq(String name,double a,double e){if(Math.abs(a-e)>.006)throw new AssertionError(name+": "+a+" expected "+e);out.accept("Passives: "+name+": "+a);}
 static void equip(List<String> passives,List<String> actives){
  var owner=(SpellContainerSource.Owner)player;var old=owner.getSpellContainers();var ids=new ArrayList<String>();
  for(var id:passives)ids.add("skill_tree_rpgs:"+id);ids.addAll(actives);
  var container=new net.spell_engine.api.spell.container.SpellContainer(net.spell_engine.api.spell.container.SpellContainer.ContentType.NONE,"","",0,ids);
  owner.setSpellContainers(new SpellContainerSource.Result(old.activeContainer(),actives.stream().map(PassiveProbe::spell).toList(),passives.stream().map(PassiveProbe::spell).toList(),List.of(),List.of(new SpellContainerSource.SourcedContainer("passive_probe",ItemStack.EMPTY,container))));
  owner.spellModifierCache().clear();
 }
 static void clear(String id){((SpellCaster.Player)player).getCooldownManager().remove(Identifier.parse(id.contains(":")?id:"skill_tree_rpgs:"+id));}
 static void cast(String id,Entity target){var h=spell(id);clear(id);SpellExecution.performSpell(level,player,h,SpellTarget.SearchResult.of(List.of(target)),h.value().type==Spell.Type.PASSIVE?SpellCast.Action.TRIGGER:SpellCast.Action.RELEASE,1);}
 static net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect(String id){return BuiltInRegistries.MOB_EFFECT.get(Identifier.parse(id)).orElseThrow();}
 static void buff(String id){player.addEffect(new MobEffectInstance(effect("skill_tree_rpgs:"+id),100));}
 static LivingEntity victim(){var t=EntityTypes.CREEPER.create(level,EntitySpawnReason.COMMAND);t.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);t.setHealth(1000);t.setNoAi(true);t.setNoGravity(true);t.setPos(0,80,6);level.addFreshEntity(t);return t;}
 static SpellExecution.ImpactContext context(Holder<Spell> h){return new SpellExecution.ImpactContext(1,1,null,new SpellPower.Result(h.value().school,40,0,1.5),SpellTarget.FocusMode.DIRECT,0);}
 public static void run(ServerLevel world,ServerPlayer owner,Consumer<String> measurements)throws Exception {
  level=world;player=owner;out=measurements;
  var containers=((SpellContainerSource.Owner)player).getSpellContainers();boolean creative=player.getAbilities().instabuild;player.getAbilities().instabuild=true;
  player.setPos(0,80,0);player.removeAllEffects();
  var haste=player.getAttribute(net.spell_engine.rpg_series.config.ConfigUtil.attribute("spell_power:haste").orElseThrow());double oldHaste=haste.getBaseValue();
  try {
   equip(List.of(),List.of());
   for(var school:List.of("arcane","fire","frost"))for(int tier=1;tier<=3;tier++)for(int n=1;n<=2;n++){
    var h=spell(school+"_tier_"+tier+"_passive_"+n);haste.setBaseValue(100);float normal=SpellParameters.getCooldownDuration(player,h);haste.setBaseValue(200);
    eq(h.unwrapKey().orElseThrow().identifier()+" haste cooldown",SpellParameters.getCooldownDuration(player,h),normal/2);
   }
   haste.setBaseValue(100);
   wards();nextSpells();coldSnap();trapsAndRoll();radiance();
  }finally{haste.setBaseValue(oldHaste);player.removeAllEffects();((SpellCaster.Player)player).getInteractor().setProcess(null);((SpellContainerSource.Owner)player).setSpellContainers(containers);((SpellContainerSource.Owner)player).spellModifierCache().clear();player.getAbilities().instabuild=creative;}
 }
 static void wards(){
  for(var school:List.of("arcane","fire","frost")){
   player.removeAllEffects();player.setAbsorptionAmount(0);String id=school+"_tier_3_passive_2";equip(List.of(id),List.of());cast(id,player);
   eq(school+" actual ward absorption at40 power",player.getAbsorptionAmount(),school.equals("arcane")?18:10);
   var ward=effect("skill_tree_rpgs:"+(school.equals("fire")?"fire":school)+"_ward");eq(school+" actual ward duration",player.getEffect(ward).getDuration(),160);
  }
  player.removeAllEffects();player.setAbsorptionAmount(0);
 }
 static void nextSpells(){
  equip(List.of("arcane_tier_3_passive_1"),List.of("wizards:arcane_explosion"));
  clear("arcane_tier_3_passive_1");SpellTriggers.onSpellCast(player,spell("wizards:arcane_blink"),List.of());
  eq("Presence guaranteed native Blink trigger",player.hasEffect(effect("skill_tree_rpgs:presence_of_mind"))?1:0,1);
  eq("Presence no longer instantifies",SpellParameters.isInstantCast(spell("wizards:arcane_explosion"),player)?1:0,0);
  var a=victim();try{
   cast("wizards:arcane_explosion",a);
   eq("Presence consumed on next actual spell",player.hasEffect(effect("skill_tree_rpgs:presence_of_mind"))?1:0,0);
   eq("Presence first hit base damage",1000-a.getHealth(),104);
   var debuff=effect("skill_tree_rpgs:arcane_vulnerability");eq("Presence applies six-second vulnerability",a.hasEffect(debuff)?a.getEffect(debuff).getDuration():0,120);
   a.invulnerableTime=0;float before=a.getHealth();
   var h=spell("wizards:arcane_bolt");SpellImpacts.performImpacts(level,player,a,a,h,h.value().impacts,context(h));
   eq("Arcane vulnerability actual20percent damage increase",before-a.getHealth(),h.value().impacts.getFirst().action.damage.spell_power_coefficient*40*1.2);
  }finally{a.discard();}
  // Native spell delivery snapshots Reflex before a projectile lands.
  equip(List.of("frost_tier_2_passive_2"),List.of("wizards:frost_lance","wizards:frost_nova","wizards:fire_slash"));
  clear("frost_tier_2_passive_2");SpellTriggers.onRoll(player);
  eq("Reflex guaranteed native roll trigger",player.hasEffect(effect("skill_tree_rpgs:arctic_reflex"))?1:0,1);
  eq("Reflex no longer instantifies",SpellParameters.isInstantCast(spell("wizards:frost_lance"),player)?1:0,0);
  var b=victim();try{
   cast("wizards:frost_nova",b);eq("Reflex first actual spell damage1.2x",1000-b.getHealth(),86.4);
   eq("Reflex consumed",player.hasEffect(effect("skill_tree_rpgs:arctic_reflex"))?1:0,0);
  }finally{b.discard();}
  var c=victim();try{cast("wizards:frost_nova",c);eq("Following Frost spell unboosted",1000-c.getHealth(),72);}finally{c.discard();}
  buff("arctic_reflex");var fire=spell("wizards:fire_slash");PassiveRules.snapshot(player,fire,context(fire));
  eq("Wrong school does not consume Reflex",player.hasEffect(effect("skill_tree_rpgs:arctic_reflex"))?1:0,1);
  var nova=spell("wizards:frost_nova");var captured=PassiveRules.snapshot(player,nova,context(nova));
  var delayed=victim();try{SpellImpacts.performImpacts(level,player,delayed,delayed,nova,nova.value().impacts,captured);eq("Delayed context retains consumed Reflex",1000-delayed.getHealth(),86.4);}finally{delayed.discard();}
  buff("arctic_reflex");var lance=spell("wizards:frost_lance");
  var beforeProjectiles=new HashSet<java.util.UUID>();for(var e:level.getAllEntities())if(e instanceof net.spell_engine.entity.SpellProjectile)beforeProjectiles.add(e.getUUID());
  var lanceVictim=victim();try {
   cast("wizards:frost_lance",lanceVictim);
   net.spell_engine.entity.SpellProjectile launched=null;
   for(var e:level.getAllEntities())if(e instanceof net.spell_engine.entity.SpellProjectile projectile && !beforeProjectiles.contains(e.getUUID()) && projectile.getSpellEntry().is(Identifier.parse("wizards:frost_lance")))launched=projectile;
   if(launched==null)throw new AssertionError("Native Reflex Lance did not launch");
   eq("Native projectile consumed Reflex before impact",player.hasEffect(effect("skill_tree_rpgs:arctic_reflex"))?1:0,0);
   SpellImpacts.projectileImpact(player,launched,lanceVictim,lance,launched.getImpactContext());
   eq("Native delayed Lance retains20percent boost",1000-lanceVictim.getHealth(),96);launched.discard();
  }finally{lanceVictim.discard();}
  var blizzard=spell("wizards:frost_blizzard");var interactor=((SpellCaster.Player)player).getInteractor();
  interactor.setProcess(new SpellCast.Process(player,blizzard,null,1,160,level.getGameTime()));buff("arctic_reflex");
  var first=PassiveRules.snapshot(player,blizzard,context(blizzard));var later=PassiveRules.snapshot(player,blizzard,context(blizzard));
  eq("Whole channel retains Reflex snapshot",Objects.equals(first.chargeModifier().spell_pattern,later.chargeModifier().spell_pattern)?1:0,1);
  interactor.setProcess(null);
  // A later roll must not empower a channel which was already started without a buff.
  interactor.setProcess(new SpellCast.Process(player,blizzard,null,1,160,level.getGameTime()+1));
  PassiveRules.snapshot(player,blizzard,context(blizzard));buff("arctic_reflex");var unbuffed=PassiveRules.snapshot(player,blizzard,context(blizzard));
  eq("Mid-channel roll cannot empower existing channel",unbuffed.chargeModifier()==null?1:0,1);
  eq("Mid-channel roll retains buff for next spell",player.hasEffect(effect("skill_tree_rpgs:arctic_reflex"))?1:0,1);
  interactor.setProcess(null);player.removeAllEffects();
 }
 static void coldSnap(){
  var ids=List.of("wizards:frost_nova","wizards:frost_spikes","wizards:frost_shield","wizards:frost_lance","wizards:frost_blizzard","wizards:frost_elemental","wizards:arcane_explosion");
  equip(List.of("frost_tier_3_passive_1"),ids);var manager=((SpellCaster.Player)player).getCooldownManager();
  for(var id:ids)manager.set(spell(id),120);
  cast("frost_tier_3_passive_1",player);
  for(int i=0;i<ids.size();i++)eq("Cold Snap actual cooldown "+ids.get(i),manager.getCooldownDuration(spell(ids.get(i))),i<4?0:120);
 }
 static void radiance(){
  equip(List.of("arcane_tier_1_passive_2"),List.of());clear("arcane_tier_1_passive_2");player.setHealth(10);
  var enemy=victim();try{
   var h=spell("wizards:arcane_explosion");
   SpellTriggers.onSpellImpactSpecific(player,enemy,h,h.value().impacts.getFirst(),false,Spell.Trigger.Stage.POST);
   eq("Radiance native trigger guarantees4HP heal at40 power",player.getHealth(),14);
   eq("Radiance actual three-second cooldown",((SpellCaster.Player)player).getCooldownManager().getCooldownDuration(spell("arcane_tier_1_passive_2")),60);
   SpellTriggers.onSpellImpactSpecific(player,enemy,h,h.value().impacts.getFirst(),false,Spell.Trigger.Stage.POST);
   eq("Radiance cooldown blocks repeated healing",player.getHealth(),14);
  }finally{enemy.discard();player.setHealth(player.getMaxHealth());}
 }
 static void trapsAndRoll(){
  equip(List.of("arcane_tier_2_passive_2"),List.of());cast("arcane_tier_2_passive_2",player);
  var immunities=((net.spell_engine.api.entity.LivingEntityImmunity.Owner)player).getImmunities();
  eq("Phase Shift exactly six immunity ticks",immunities.getLast().validUntil()-player.tickCount,6);
  eq("Phase Shift no additional cooldown",((SpellCaster.Player)player).getCooldownManager().getCooldownDuration(spell("arcane_tier_2_passive_2")),0);
  var a=victim();try{
   var trap=spell("arcane_tier_2_passive_1");SpellImpacts.performImpacts(level,player,a,a,trap,trap.value().impacts,context(trap),false,null);
   eq("Arcane trap actual40percent damage",1000-a.getHealth(),16);
   eq("Arcane trap Weakness II",a.getEffect(effect("minecraft:weakness")).getAmplifier(),1);
   eq("Arcane trap weakness six seconds",a.getEffect(effect("minecraft:weakness")).getDuration(),120);
  }finally{a.discard();}
  var b=victim();try{
   var trap=spell("fire_tier_2_passive_1");SpellImpacts.performImpacts(level,player,b,b,trap,trap.value().impacts,context(trap),false,null);
   eq("Fire trap actual30percent damage",1000-b.getHealth(),12);eq("Fire trap four-second burn",b.getRemainingFireTicks(),80);
   eq("Fire trap no vulnerability",b.hasEffect(effect("skill_tree_rpgs:fire_vulnerability"))?1:0,0);
   b.addEffect(new MobEffectInstance(effect("skill_tree_rpgs:fire_vulnerability"),160,0));
   b.invulnerableTime=0;float before=b.getHealth();var ball=spell("wizards:fireball");SpellImpacts.performImpacts(level,player,b,b,ball,ball.value().impacts,context(ball));
   eq("Fire vulnerability actual4percent per level",before-b.getHealth(),33.28);
  }finally{b.discard();}
 }
}
