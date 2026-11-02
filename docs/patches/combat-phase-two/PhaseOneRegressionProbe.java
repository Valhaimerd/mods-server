package local.audit;
import java.util.*;
import java.nio.file.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.*;
import net.spell_engine.api.spell.*;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.internals.*;
import net.spell_engine.internals.casting.*;
import net.spell_engine.internals.container.SpellContainerSource;
import net.spell_engine.internals.cost.*;
import net.spell_engine.internals.impact.SpellImpacts;
import net.spell_engine.internals.target.SpellTarget;
import net.spell_power.api.*;

@Mod("phase_one_probe")
public class PhaseOneRegressionProbe {
 ServerLevel world;ServerPlayer p;int checks;List<String> lines=new ArrayList<>();
 public PhaseOneRegressionProbe(){NeoForge.EVENT_BUS.addListener(this::start);}
 void eq(String label,double a,double b){if(!Double.isFinite(a)||!Double.isFinite(b)||Math.abs(a-b)>.008)throw new AssertionError(label+": "+a+" expected "+b);checks++;lines.add(label+": "+a);}
 Holder<Spell> h(String id){return SpellRegistry.from(world).get(Identifier.parse(id)).orElseThrow();}
 void equip(String...ids){
  var owner=(SpellContainerSource.Owner)p;var old=owner.getSpellContainers();var passive=new ArrayList<Holder<Spell>>();var mods=new ArrayList<Holder<Spell>>();
  for(var id:ids){var s=h(id);if(s.value().type==Spell.Type.PASSIVE)passive.add(s);else mods.add(s);}
  var all=new ArrayList<String>(List.of(ids));all.add("wizards:arcane_explosion");var container=new net.spell_engine.api.spell.container.SpellContainer(net.spell_engine.api.spell.container.SpellContainer.ContentType.NONE,"","",0,all);owner.setSpellContainers(new SpellContainerSource.Result(old.activeContainer(),List.of(h("wizards:arcane_explosion")),passive,mods,List.of(new SpellContainerSource.SourcedContainer("phase_one",ItemStack.EMPTY,container))));owner.spellModifierCache().clear();
 }
 LivingEntity mob(){var t=EntityTypes.CREEPER.create(world,EntitySpawnReason.COMMAND);t.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);t.setHealth(1000);t.setNoAi(true);t.setNoGravity(true);t.setPos(4,80,0);world.addFreshEntity(t);return t;}
 SpellExecution.ImpactContext context(Holder<Spell> s){float c=SpellParameters.channelValueMultiplier(s.value());if(c==0)c=1;return new SpellExecution.ImpactContext(c,1,null,new SpellPower.Result(s.value().school,40,0,1),SpellTarget.FocusMode.DIRECT,0);}
 void hit(String id,double percent){var s=h("wizards:"+id);var t=mob();SpellImpacts.performImpacts(world,p,t,p,s,s.value().impacts,context(s),false,Spell.Impact.Action.Type.DAMAGE);eq(id+" actual damage at40 power",1000-t.getHealth(),40*percent/100*.8);t.discard();}
 void start(ServerStartedEvent e){world=e.getServer().overworld();try{
  p=FakePlayerFactory.get(world,new com.mojang.authlib.GameProfile(UUID.fromString("581c5ce1-dd80-439e-92af-e152ff9fe301"),"PhaseOne"));p.setPos(0,80,0);p.getAbilities().instabuild=true;
  for(String id:List.of("arcane","fire","frost"))p.getAttribute(BuiltInRegistries.ATTRIBUTE.get(Identifier.parse("spell_power:"+id)).orElseThrow()).setBaseValue(40);
  p.getAttribute(BuiltInRegistries.ATTRIBUTE.get(Identifier.parse("spell_power:critical_chance")).orElseThrow()).setBaseValue(0);
  p.getAttribute(net.critical_strike.api.CriticalStrikeAttributes.CHANCE.attributeEntry).setBaseValue(0);
  equip();
  hit("arcane_missile",21);hit("arcane_explosion",210);hit("arcane_beam",16);hit("fire_breath",10);hit("fire_meteor",120);hit("fire_storm",32);hit("frost_blizzard",21);hit("arcane_barrage_bolt",32);hit("hydra_fireball",44);hit("elemental_frost_nova",145);
  for(String[] row:new String[][]{{"arcane_bolt","56"},{"arcane_blast","64"},{"burst_blink_explosion","40"},{"fire_slash","80"},{"fire_scorch","48"},{"fireball","64"},{"fire_blast","80"},{"fire_wall","16"},{"ancient_hydra_breath","19"},{"frost_nova","145"},{"frost_spikes","64"},{"frost_lance","160"},{"frostbolt","64"},{"frost_shard","48"},{"elemental_frost_shard","80"},{"legion_frost_nova","145"}})hit(row[0],Double.parseDouble(row[1]));
  for(float progress:new float[]{0,.2f,.5f,.9f,1})eq("Missiles non-proportional "+progress,PriestTuning.cooldown(p,h("wizards:arcane_missile"),progress),12);
  for(int i:new int[]{0,1,20,49,100})eq("Charged coefficient "+i,RevisionRules.chargedCoefficient(i)*.2,Math.min(.08+.0012*i,.12));
  charges();wards();frenzy();sinful();preparation();nodes();enchantmentComparison();
  timed();
 }catch(Throwable ex){finish(ex);}}
 void finish(Throwable ex){try{Files.writeString(Path.of("phase-one-result.txt"),ex==null?"PASS "+checks+" checks\n":"FAIL "+ex+"\n");Files.write(Path.of("phase-one-checks.txt"),lines);}catch(Exception ignored){}if(ex!=null)ex.printStackTrace();world.getServer().halt(false);}
 void timed(){((net.spell_engine.utils.WorldScheduler)world).schedule(100,this::timedReady);}
 void timedReady(){
  world.setChunkForced(1,0,true);world.setChunkForced(2,0,true);world.getChunk(1,0);world.getChunk(2,0);
  var caster=mob();caster.setPos(30,80,0);var victim=mob();victim.setPos(35,80,0);world.setChunkForced(2,0,true);SinfulDamage.start(victim,4,caster);
  var scheduler=(net.spell_engine.utils.WorldScheduler)world;
  scheduler.schedule(9,()->{try{eq("Timed retaliation waits first0.5s",1000-victim.getHealth(),0);}catch(Throwable ex){finish(ex);}});
  scheduler.schedule(11,()->{try{eq("Timed retaliation first pulse",1000-victim.getHealth(),1);}catch(Throwable ex){finish(ex);}});
  scheduler.schedule(81,()->{try{eq("Timed retaliation eight finite pulses",1000-victim.getHealth(),8);eq("Timed retaliation state consumed",EffectDetails.data(victim).has("sinful_damage")?1:0,0);caster.discard();victim.discard();finish(null);}catch(Throwable ex){finish(ex);}});
 }
 void charges(){
  equip("skill_tree_rpgs:arcane_tier_2_spell_2_modifier_1");var m=((SpellCaster.Player)p).getCooldownManager();m.reset(null);var s=h("wizards:arcane_explosion");
  eq("Echoing initial charges",EchoCharges.available(m),2);var nativeTarget=mob();SpellExecution.performSpell(world,p,s,SpellTarget.SearchResult.of(List.of(nativeTarget)),SpellCast.Action.RELEASE,1);eq("Actual native release spends charge",EchoCharges.available(m),1);nativeTarget.discard();m.reset(null);
  SpellCooldowns.imposeCooldown(p,null,s,1);eq("First native spend",EchoCharges.available(m),1);eq("One charge remains usable",m.isCoolingDown(s)?1:0,0);
  hit("arcane_explosion",130);eq("Hit does not refund",EchoCharges.available(m),1);
  for(int i=0;i<100;i++)m.tickUpdate();SpellCooldowns.imposeCooldown(p,null,s,1);eq("Second native spend",EchoCharges.available(m),0);eq("Empty blocks casting",m.isCoolingDown(s)?1:0,1);
  for(int i=0;i<139;i++)m.tickUpdate();eq("No early recharge",EchoCharges.available(m),0);m.tickUpdate();eq("First exact240 tick charge",EchoCharges.available(m),1);
  for(int i=0;i<239;i++)m.tickUpdate();eq("Second still recovering",EchoCharges.available(m),1);m.tickUpdate();eq("Second exact serial recharge",EchoCharges.available(m),2);
  SpellCooldowns.imposeCooldown(p,null,s,1);equip();eq("Respec preserves recharge gate",m.isCoolingDown(s)?1:0,1);equip("skill_tree_rpgs:arcane_tier_2_spell_2_modifier_1");eq("Reselect does not mint a charge",EchoCharges.available(m),1);
  m.reset(null);m.set(s,80);eq("Old saved ordinary cooldown respected",m.isCoolingDown(s)?1:0,1);SpellCooldowns.imposeCooldown(p,null,s,1);eq("Blocked cast does not spend",EchoCharges.available(m),2);
  m.reset(null);SpellCooldowns.imposeCooldown(p,null,s,1);SpellCooldowns.imposeCooldown(p,null,s,1);for(int i=0;i<37;i++)m.tickUpdate();
  var saved=net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING,world.registryAccess());m.writeCustomData(saved);var reloaded=new SpellCooldownManager(p);reloaded.readCustomData(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING,world.registryAccess(),saved.buildResult()));eq("Reload preserves empty bank",EchoCharges.available(reloaded),0);for(int i=0;i<202;i++)reloaded.tickUpdate();eq("Reload does not shortcut timer",EchoCharges.available(reloaded),0);reloaded.tickUpdate();eq("Reload serial first charge",EchoCharges.available(reloaded),1);for(int i=0;i<240;i++)reloaded.tickUpdate();eq("Reload serial second charge",EchoCharges.available(reloaded),2);
  var fresh=FakePlayerFactory.get(world,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"RespawnOne"));NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone(fresh,p,true));var cloned=((SpellCaster.Player)fresh).getCooldownManager();eq("Native clone event preserves empty bank",EchoCharges.available(cloned),0);for(int i=0;i<203;i++)cloned.tickUpdate();eq("Clone preserves remaining recharge",EchoCharges.available(cloned),1);
  m.reset(null);var haste=p.getAttribute(BuiltInRegistries.ATTRIBUTE.get(Identifier.parse("spell_power:haste")).orElseThrow());haste.setBaseValue(200);SpellCooldowns.imposeCooldown(p,null,s,1);SpellCooldowns.imposeCooldown(p,null,s,1);for(int i=0;i<119;i++)m.tickUpdate();eq("Haste no early charge",EchoCharges.available(m),0);m.tickUpdate();eq("Haste restores at120",EchoCharges.available(m),1);haste.setBaseValue(100);for(int i=0;i<119;i++)m.tickUpdate();eq("Queued interval sampled when begun",EchoCharges.available(m),1);m.tickUpdate();eq("Second queued original haste interval",EchoCharges.available(m),2);m.reset(null);equip();
 }
 void wards(){
  for(String school:List.of("arcane","fire","frost"))for(int power:new int[]{0,7,8,15,16,40,80}){
   p.removeAllEffects();p.setAbsorptionAmount(0);p.getAttribute(BuiltInRegistries.ATTRIBUTE.get(Identifier.parse("spell_power:"+school)).orElseThrow()).setBaseValue(power);
   var s=h("skill_tree_rpgs:"+school+"_tier_3_passive_2");equip("skill_tree_rpgs:"+school+"_tier_3_passive_2");((SpellCaster.Player)p).getCooldownManager().reset(null);
   SpellExecution.performSpell(world,p,s,SpellTarget.SearchResult.of(List.of(p)),SpellCast.Action.TRIGGER,1);
   lines.add("WARD "+school+" P="+power+" effects="+p.getActiveEffects()+" maxAbs="+p.getAttributeValue(Attributes.MAX_ABSORPTION));eq(school+" Ward power "+power,p.getAbsorptionAmount(),2*(power/8));
   if(power>=8){float before=p.getAbsorptionAmount();p.setAbsorptionAmount(before-1);var fx=BuiltInRegistries.MOB_EFFECT.get(Identifier.parse("skill_tree_rpgs:"+school+"_ward")).orElseThrow();var old=p.getEffect(fx);p.addEffect(new MobEffectInstance(fx,200,old.getAmplifier()));eq(school+" refresh does not refill",p.getAbsorptionAmount(),before-1);}
  }
  p.removeAllEffects();p.setAbsorptionAmount(0);equip();
  var arc=BuiltInRegistries.MOB_EFFECT.get(Identifier.parse("skill_tree_rpgs:arcane_ward")).orElseThrow();var fire=BuiltInRegistries.MOB_EFFECT.get(Identifier.parse("skill_tree_rpgs:fire_ward")).orElseThrow();
  p.addEffect(new MobEffectInstance(arc,160,5));eq("First Ward grants10",p.getAbsorptionAmount(),10);p.setAbsorptionAmount(6);p.addEffect(new MobEffectInstance(fire,160,5));eq("Another school cannot restore spent absorption",p.getAbsorptionAmount(),6);p.addEffect(new MobEffectInstance(fire,200,6));eq("Stronger Ward adds only new capacity",p.getAbsorptionAmount(),8);p.removeAllEffects();p.setAbsorptionAmount(0);p.addEffect(new MobEffectInstance(arc,160,5));eq("New Ward after actual expiry grants again",p.getAbsorptionAmount(),10);p.removeAllEffects();p.setAbsorptionAmount(0);
 }
 void frenzy()throws Exception{
  var method=net.minecraft.world.entity.projectile.arrow.AbstractArrow.class.getDeclaredMethod("onHitEntity",EntityHitResult.class);method.setAccessible(true);
  for(boolean frenzy:new boolean[]{false,true}){
   var t=mob();t.invulnerableTime=20;var a=EntityTypes.ARROW.create(world,EntitySpawnReason.COMMAND);a.setOwner(p);a.setBaseDamage(5);a.setDeltaMovement(1,0,0);if(frenzy)a.addTag("FRENZY");world.addFreshEntity(a);method.invoke(a,new EntityHitResult(t));
   eq("Arrow iframe damage frenzy="+frenzy,1000-t.getHealth(),frenzy?4:4); // Native differing-hit rule is separately checked below.
   t.discard();a.discard();
  }
  var t=mob();var a=EntityTypes.ARROW.create(world,EntitySpawnReason.COMMAND);a.setOwner(p);a.setBaseDamage(5);a.setDeltaMovement(1,0,0);a.addTag("FRENZY");world.addFreshEntity(a);
  t.hurtServer(world,p.damageSources().playerAttack(p),20);float before=t.getHealth();int iframe=t.invulnerableTime;method.invoke(a,new EntityHitResult(t));eq("Frenzy repeated weaker hit registers",before-t.getHealth(),4);eq("Frenzy restores prior iframe",t.invulnerableTime,iframe);t.discard();a.discard();
  for(boolean tagged:new boolean[]{false,true}){t=mob();t.hurtServer(world,p.damageSources().playerAttack(p),20);before=t.getHealth();a=EntityTypes.ARROW.create(world,EntitySpawnReason.COMMAND);a.setOwner(p);a.setBaseDamage(5);a.setDeltaMovement(1,0,0);if(tagged)a.addTag("FRENZY");world.addFreshEntity(a);if(tagged){t.setInvulnerable(true);p.getAbilities().instabuild=false;}method.invoke(a,new EntityHitResult(t));p.getAbilities().instabuild=true;eq(tagged?"Explicit invulnerability retained":"Ordinary repeated arrow still respects iframes",before-t.getHealth(),0);t.discard();a.discard();}
 }
 void sinful(){
  var t=mob();t.getAttribute(Attributes.ARMOR).setBaseValue(100);t.getAttribute(Attributes.MAX_ABSORPTION).setBaseValue(2);t.setAbsorptionAmount(2);SinfulDamage.start(t,4,p);eq("Sinful no time-zero damage",t.getHealth(),1000);
  for(int i=0;i<3;i++)SinfulDamage.pulse(p,t);eq("True drain consumes absorption",t.getAbsorptionAmount(),0);eq("Only remainder reaches health",1000-t.getHealth(),.4);
  t.setHealth(1000);for(int i=0;i<8;i++)SinfulDamage.pulse(p,t);eq("Eight raw1 pulses retain80pct",1000-t.getHealth(),6.4);t.discard();
  t=mob();t.setInvulnerable(true);SinfulDamage.pulse(p,t);eq("True damage respects explicit invulnerability",t.getHealth(),1000);t.setInvulnerable(false);t.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(net.minecraft.world.item.Items.TOTEM_OF_UNDYING));t.setHealth(.5f);SinfulDamage.pulse(p,t);eq("True damage honors native totem",t.isAlive()?1:0,1);eq("Native rescue consumes totem",t.getOffhandItem().isEmpty()?1:0,1);t.discard();
 }
 void preparation(){
  equip();for(String[] row:new String[][]{{"paladins:blessed_strikes","1.5"},{"paladins:judgement","0.25"},{"rogues:throw","0.3"},{"rogues:throw_net","0.3"}})eq("Native cast time "+row[0],SpellParameters.getCastDuration(p,h(row[0]).value()),Double.parseDouble(row[1]));
  eq("Blessed five pulses retained",SpellParameters.channelTicks(p,h("paladins:blessed_strikes")),5);eq("Purge target range",SpellParameters.getRange(p,h("paladins:flash_heal")),30);
  for(String id:List.of("rogues:throw","rogues:throw_net"))for(float q:new float[]{0,.5f,1}){var s=h(id);var t=mob();var c=context(s).channeled(1).charge(q);SpellImpacts.performImpacts(world,p,t,p,s,List.of(s.value().impacts.getFirst()),c,false,null);float earliest=s.value().active.cast.charge.min_release_ratio;double ratio=Math.clamp((q-earliest)/(1-earliest),0,1),coefficient=id.equals("rogues:throw")?.4+.4*ratio:.075+.075*ratio;eq("Throw charge curve "+id+" "+q,1000-t.getHealth(),40*coefficient*.8);t.discard();}
 }
 void nodes(){
  var c=net.puffish.skillsmod.api.SkillsAPI.getCategory(Identifier.parse("skill_tree_rpgs:class_skills")).orElseThrow();c.unlock(p);c.resetSkills(p);c.setExtraPoints(p,1000);
  var a=p.getAttribute(Attributes.ATTACK_DAMAGE);a.setBaseValue(10);c.getSkill("e5ljj7jx6pmdjatf").orElseThrow().unlock(p);eq("Existing Warrior root reward4pct",a.getValue(),10.4);c.getSkill("jo7zae6quimtvale").orElseThrow().unlock(p);eq("Warrior ordinary reward adds4pct",a.getValue(),10.8);c.resetSkills(p);
  var speed=p.getAttribute(Attributes.ATTACK_SPEED);speed.setBaseValue(4);c.setExtraPoints(p,1000);c.getSkill("63gwys64onqg00ry").orElseThrow().unlock(p);eq("Rogue root reward2pct",speed.getValue(),4.08);c.getSkill("iaztym6bad9vpl6j").orElseThrow().unlock(p);eq("Rogue ordinary reward adds2pct",speed.getValue(),4.16);c.resetSkills(p);
  var adv=net.puffish.skillsmod.api.SkillsAPI.getCategory(Identifier.parse("adventurer_skills:adventurer_skills")).orElseThrow();adv.unlock(p);adv.resetSkills(p);adv.setExtraPoints(p,1000);var melee=p.getAttribute(BuiltInRegistries.ATTRIBUTE.get(Identifier.parse("puffish_attributes:melee_damage")).orElseThrow());for(var mod:List.copyOf(melee.getModifiers()))melee.removeModifier(mod.id());melee.setBaseValue(1);adv.getSkill("510lurvlze473o6j").orElseThrow().unlock(p);eq("Adventurer ordinary melee3pct modifier",melee.getModifiers().stream().filter(m->m.operation()==net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL).mapToDouble(m->m.amount()).sum(),.03);adv.resetSkills(p);
 }
 void enchantmentComparison(){
  var fixture=FakePlayerFactory.get(world,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"EnchantMath"));
  var generic=fixture.getAttribute(BuiltInRegistries.ATTRIBUTE.get(Identifier.parse("spell_power:generic")).orElseThrow());
  Identifier flat=Identifier.parse("phase_test:flat"),other=Identifier.parse("phase_test:other"),weapon=Identifier.parse("phase_test:weapon");
  for(var school:List.of(SpellSchools.ARCANE,SpellSchools.FIRE,SpellSchools.FROST)){
   var a=fixture.getAttribute(school.attributeEntry);a.setBaseValue(1);a.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(flat,19,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));
   for(double bonus:new double[]{0,.5,1,1.5,2.35}){
    if(bonus>0)a.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(other,bonus,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    eq(school.id+" unenchanted other="+bonus,SpellPower.getSpellPower(school,fixture).nonCriticalValue(),20*(1+bonus));
    // These are the exact native operations/amounts serialized by four matching V armor enchants and Spell Power V.
    generic.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(weapon,.25,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    eq(school.id+" weaponV only other="+bonus,SpellPower.getSpellPower(school,fixture).nonCriticalValue(),20*(1+bonus)+4.75);
    for(int i=0;i<4;i++)a.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(Identifier.parse("phase_test:armor"+i),.15,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    eq(school.id+" armorVx4 plus weaponV other="+bonus,SpellPower.getSpellPower(school,fixture).nonCriticalValue(),20*(1.6+bonus)+4.75);
    generic.removeModifier(weapon);eq(school.id+" armorVx4 only other="+bonus,SpellPower.getSpellPower(school,fixture).nonCriticalValue(),20*(1.6+bonus));
    for(int i=0;i<4;i++)a.removeModifier(Identifier.parse("phase_test:armor"+i));a.removeModifier(other);
   }
   a.removeModifier(flat);
  }
 }
}

