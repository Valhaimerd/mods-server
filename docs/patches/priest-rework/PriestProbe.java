package local.audit;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.entity.SpellProjectile;
import net.spell_engine.internals.*;
import net.spell_engine.internals.casting.*;
import net.spell_engine.internals.container.SpellContainerSource;
import net.spell_engine.internals.impact.SpellImpacts;
import net.spell_engine.internals.target.SpellTarget;
import net.spell_power.api.SpellPower;
@Mod("priest_probe")
public class PriestProbe {
 List<String> out=new ArrayList<>();ServerLevel world;ServerPlayer p;
 void eq(String k,double a,double b){if(Math.abs(a-b)>.025)throw new AssertionError(k+" actual="+a+" expected="+b);out.add("PASS "+k+" "+a);}
 Holder<Spell> spell(String key){return SpellRegistry.from(world).get(Identifier.parse(key)).orElseThrow();}
 void equip(String...ids){var owner=(SpellContainerSource.Owner)p;var old=owner.getSpellContainers();var list=List.of(ids);var container=new net.spell_engine.api.spell.container.SpellContainer(net.spell_engine.api.spell.container.SpellContainer.ContentType.NONE,"","",0,list);owner.setSpellContainers(new SpellContainerSource.Result(old.activeContainer(),List.of(),List.of(),list.stream().map(this::spell).toList(),List.of(new SpellContainerSource.SourcedContainer("priest",ItemStack.EMPTY,container))));owner.spellModifierCache().clear();}
 SpellExecution.ImpactContext context(Holder<Spell> h,int pulse){return new SpellExecution.ImpactContext(1,1,p.position(),new SpellPower.Result(h.value().school,40,0,1.5),SpellTarget.FocusMode.AREA,pulse);}
 LivingEntity mob(double x,double z){var v=EntityTypes.VILLAGER.create(world,EntitySpawnReason.COMMAND);v.setPos(x,80,z);v.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);v.setHealth(1000);v.setNoAi(true);v.setNoGravity(true);world.addFreshEntity(v);return v;}
 void timing(){
  equip();
  for(String key:List.of("paladins:holy_beam","paladins:penance")){
   var h=spell(key);float full=key.endsWith("penance")?14:24,min=key.endsWith("penance")?8:12;
   eq(key+" per-delivery normalization",PriestRules.normalize(h,context(h,0).channeled(.2f)).channel(),1);
   for(int n=0;n<=20;n++)eq(key+" smooth cooldown "+n,PriestRules.cooldown(p,h,n/20f),min+(full-min)*n/20f);
  }
  eq("Other channels preserve native scaling",PriestRules.normalize(spell("paladins:levitate"),context(spell("paladins:levitate"),0).channeled(.5f)).channel(),.5);
  for(String key:List.of("paladins:levitate","paladins:penance")){
   var h=spell(key);int length=key.endsWith("penance")?50:40;var process=new SpellCast.Process(p,h,Items.STICK,1,length,world.getGameTime());
   for(int i=0;i<=length;i++)if(process.isDue(world.getGameTime()+i)){eq(key+" first/end interval "+i,i%(length/(key.endsWith("penance")?10:4)),0);process.markDue();}
  }
 }
 void holy(){
  var h=spell("paladins:holy_beam");
  for(int branch=0;branch<4;branch++){
   equip(branch==0?new String[]{}:branch==1?new String[]{"skill_tree_rpgs:priest_tier_2_spell_1_root"}:new String[]{"skill_tree_rpgs:priest_tier_2_spell_1_root","skill_tree_rpgs:priest_tier_2_spell_1_modifier_"+(branch-1)});
   var enemy=mob(0,3);p.setHealth(1);p.removeAllEffects();
   SpellImpacts.performImpacts(world,p,p,p,h,h.value().impacts,context(h,0),false,null);eq("Holy healing branch "+branch,p.getHealth()-1,40*(branch==0?.15:branch==2?.30:.20));
   var hit=PriestRules.impact(p,h,h.value().impacts.get(1),context(h,0),enemy);eq("Holy damage coefficient "+branch,hit.action.damage.spell_power_coefficient,branch==0?.30:branch==3?.50:.35);eq("Holy knockback "+branch,hit.action.damage.knockback,branch==2?2:1);
   SpellImpacts.performImpacts(world,p,enemy,p,h,h.value().impacts,context(h,0),false,null);eq("Holy damage native "+branch,1000-enemy.getHealth(),40*hit.action.damage.spell_power_coefficient);enemy.discard();
  }
 }
 void penance()throws Exception{
  var h=spell("paladins:penance");equip();p.removeAllEffects();p.setPos(0,80,0);p.setAbsorptionAmount(0);
  var enemy=mob(0,3);var far=mob(0,9);var pet=EntityTypes.WOLF.create(world,EntitySpawnReason.COMMAND);pet.setOwnerReference(EntityReference.of(p.getUUID()));pet.setPos(1,80,3);world.addFreshEntity(pet);float petHp=pet.getHealth();
  var projectile=new SpellProjectile(world,p,0,81,3,SpellProjectile.Behaviour.FLY,h,context(h,0),new Spell.ProjectileData.Perks());
  var onHit=SpellProjectile.class.getDeclaredMethod("onHitEntity",EntityHitResult.class);onHit.setAccessible(true);onHit.invoke(projectile,new EntityHitResult(enemy,enemy.getBoundingBox().getCenter()));
  eq("Penance single explosion damage",1000-enemy.getHealth(),10);eq("Penance outer radius",far.getHealth(),1000);eq("Penance protects own pet",pet.getHealth(),petHp);eq("Penance buffs nearby owner",p.getAbsorptionAmount(),1);eq("Penance buffs pet",pet.getAbsorptionAmount(),1);eq("Penance projectile discarded",projectile.isRemoved()?1:0,1);
  for(int i=0;i<5;i++)PriestRules.projectile(p,p,p,h,context(h,0).position(p.position()));eq("Six bolt shield",p.getAbsorptionAmount(),6);
  p.setAbsorptionAmount(2);for(int i=0;i<2;i++)PriestRules.projectile(p,p,p,h,context(h,0).position(p.position()));eq("Spent shield remains spent: 6-4+2",p.getAbsorptionAmount(),4);
  for(int i=0;i<2;i++)PriestRules.projectile(p,p,p,h,context(h,0).position(p.position()));eq("Ten stack shield retains spent HP",p.getAbsorptionAmount(),6);
  PriestRules.projectile(p,p,p,h,context(h,0).position(p.position()));eq("Cap refresh cannot restore shield",p.getAbsorptionAmount(),6);
  equip("skill_tree_rpgs:priest_tier_3_spell_2_root","skill_tree_rpgs:priest_tier_3_spell_2_modifier_1","skill_tree_rpgs:priest_tier_3_spell_2_modifier_2");
  eq("Improved node selected",PriestRules.node(p,"tier_3_spell_2_root")?1:0,1);enemy.invulnerableTime=0;enemy.setHealth(1000);PriestRules.projectile(p,p,enemy,h,context(h,0).position(enemy.position()));eq("Improved Penance damage",1000-enemy.getHealth(),12);eq("Improved shield duration",p.getEffect(PriestRules.effect(PriestRules.SHIELD)).getDuration(),80);
  for(int i=0;i<9;i++)PriestRules.projectile(p,p,enemy,h,context(h,0).position(enemy.position()));eq("Hysteria ten stacks",p.getEffect(PriestRules.effect("skill_tree_rpgs:hysteria")).getAmplifier()+1,10);eq("Chastise ten stacks",enemy.getEffect(PriestRules.effect("skill_tree_rpgs:chastise")).getAmplifier()+1,10);eq("Hysteria refreshed 4s",p.getEffect(PriestRules.effect("skill_tree_rpgs:hysteria")).getDuration(),80);
  enemy.discard();far.discard();pet.discard();p.removeAllEffects();
 }
 void ascension(){
  var h=spell("paladins:levitate");equip("skill_tree_rpgs:priest_tier_2_spell_2_root","skill_tree_rpgs:priest_tier_2_spell_2_modifier_1","skill_tree_rpgs:priest_tier_2_spell_2_modifier_2");p.removeAllEffects();p.setAbsorptionAmount(0);
  var enemy=mob(0,3);double first=0;
  for(int i=0;i<4;i++){enemy.setDeltaMovement(Vec3.ZERO);PriestRules.ascension(world,h,p,List.of(),context(h,i),null);if(i==0)first=enemy.getDeltaMovement().horizontalDistance();if(i==3)eq("Final pulse stronger push",enemy.getDeltaMovement().horizontalDistance()>first?1:0,1);eq("Serenity adds only per pulse "+i,p.getAbsorptionAmount(),5*(i+1));}
  eq("Ascension four damage stacks",p.getEffect(PriestRules.effect(PriestRules.AURA)).getAmplifier()+1,4);eq("Ascension improved duration",p.getEffect(PriestRules.effect("paladins:levitate")).getDuration(),160);eq("Serenity improved duration",p.getEffect(PriestRules.effect(PriestRules.SERENITY)).getDuration(),160);
  p.setAbsorptionAmount(2);PriestRules.ascension(world,h,p,List.of(),context(h,3),null);eq("Serenity cap refresh no refill",p.getAbsorptionAmount(),2);enemy.discard();p.removeAllEffects();
 }
 void absorption(){
  equip();
  for(String key:List.of("paladins:priest_absorption","skill_tree_rpgs:arcane_ward","skill_tree_rpgs:fire_ward","skill_tree_rpgs:frost_ward","arsenal:absorption","skill_tree_rpgs:tenacity_ward")){
   var e=PriestRules.effect(key);var target=mob(0,3);int unit=key.endsWith("tenacity_ward")?1:2;
   target.addEffect(new MobEffectInstance(e,100,2));eq(key+" first shield",target.getAbsorptionAmount(),unit*3);
   target.setAbsorptionAmount(unit);target.addEffect(new MobEffectInstance(e,120,3));eq(key+" stack only adds new HP",target.getAbsorptionAmount(),unit*2);
   target.addEffect(new MobEffectInstance(e,140,3));eq(key+" refresh no refill",target.getAbsorptionAmount(),unit*2);
   target.removeEffect(e);target.setAbsorptionAmount(0);target.addEffect(new MobEffectInstance(e,100,2));eq(key+" fresh after removal",target.getAbsorptionAmount(),unit*3);
   target.discard();
  }
 }
 void circle()throws Exception{
  var h=spell("paladins:circle_of_healing");p.removeAllEffects();p.setHealth(1);equip();SpellImpacts.performImpacts(world,p,p,p,h,h.value().impacts,context(h,0),false,null);eq("Circle base healing",p.getHealth()-1,20);
  equip("skill_tree_rpgs:priest_tier_3_spell_1_root","skill_tree_rpgs:priest_tier_3_spell_1_modifier_2");p.setHealth(1);SpellImpacts.performImpacts(world,p,p,p,h,h.value().impacts,context(h,0),false,null);eq("Circle improved healing",p.getHealth()-1,24);eq("Sanctuary P40 regeneration III",p.getEffect(MobEffects.REGENERATION).getAmplifier(),2);eq("Sanctuary duration",p.getEffect(MobEffects.REGENERATION).getDuration(),100);
  var custom=net.spell_engine.api.spell.event.SpellHandlers.customImpact.get("priest_pack:sanctuary");p.removeEffect(MobEffects.REGENERATION);custom.onSpellImpact(h,new SpellPower.Result(h.value().school,27,0,1.5),p,p,context(h,0));eq("Sanctuary P27 rounds to regeneration III",p.getEffect(MobEffects.REGENERATION).getAmplifier(),2);
  p.removeAllEffects();equip("skill_tree_rpgs:priest_tier_3_spell_1_modifier_1");p.addEffect(new MobEffectInstance(MobEffects.POISON,200));p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,200));p.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,200));SpellImpacts.performImpacts(world,p,p,p,h,h.value().impacts,context(h,0),false,null);eq("Mass Dispel removes exactly two",p.getActiveEffects().size(),1);p.removeAllEffects();
 }
 void nativeDelivery()throws Exception{
  equip();p.removeAllEffects();p.setPos(8,80,8);var enemy=mob(8,11);var holy=spell("paladins:holy_beam");
  var targets=new SpellTarget.SearchResult(List.of(enemy),enemy.position());
  PriestRules.deliver(world,p,holy,targets,context(holy,0).channeled(.2f),null);eq("Native Holy delivery applies 30 percent per hit",1000-enemy.getHealth(),12);
  enemy.setHealth(1000);enemy.invulnerableTime=0;var penance=spell("paladins:penance");
  PriestRules.deliver(world,p,penance,targets,context(penance,0).channeled(.25f),null);
  var scheduler=(net.spell_engine.utils.WorldScheduler)world;scheduler.updateScheduledTasks();for(var task:Map.copyOf(scheduler.getScheduledTasks()).entrySet())if(task.getKey()<=world.getGameTime()+1){scheduler.getScheduledTasks().remove(task.getKey());List.copyOf(task.getValue()).forEach(Runnable::run);}
  var arrows=world.getEntitiesOfClass(SpellProjectile.class,p.getBoundingBox().inflate(30),e->e.getSpellEntry().is(Identifier.parse("paladins:penance")));
  eq("Native Penance delivery spawns one bolt",arrows.size(),1);
  var hit=SpellProjectile.class.getDeclaredMethod("onHitEntity",EntityHitResult.class);hit.setAccessible(true);hit.invoke(arrows.getFirst(),new EntityHitResult(enemy,enemy.getBoundingBox().getCenter()));eq("Native Penance bolt applies 25 percent per blast",1000-enemy.getHealth(),10);enemy.discard();
 }
 public PriestProbe(){NeoForge.EVENT_BUS.addListener((ServerStartedEvent event)->{var server=event.getServer();try{
  world=server.overworld();p=FakePlayerFactory.getMinecraft(world);p.setPos(0,80,0);p.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);p.setHealth(100);world.getChunk(0,0);var manager=ServerLevel.class.getDeclaredField("entityManager");manager.setAccessible(true);((net.minecraft.world.level.entity.PersistentEntitySectionManager<?>)manager.get(world)).updateChunkStatus(new net.minecraft.world.level.ChunkPos(0,0),net.minecraft.server.level.FullChunkStatus.ENTITY_TICKING);timing();holy();penance();ascension();absorption();circle();nativeDelivery();PartyProbe.run(server,out);
  Files.write(Path.of("priest-results.txt"),out);Files.writeString(Path.of("priest-result.txt"),"PASS "+out.size()+" Priest/party checks\n");
 }catch(Throwable error){error.printStackTrace();try{Files.write(Path.of("priest-results.txt"),out);Files.writeString(Path.of("priest-result.txt"),"FAIL "+error);}catch(Exception ignored){}}server.halt(false);});}
}

