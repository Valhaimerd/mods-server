package local.audit;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
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
import net.spell_engine.entity.*;
import net.spell_engine.internals.*;
import net.spell_engine.internals.casting.SpellCaster;
import net.spell_engine.internals.container.SpellContainerSource;
import net.spell_engine.internals.impact.SpellImpacts;
import net.spell_engine.internals.target.SpellTarget;
import net.spell_power.api.SpellPower;
@Mod("warrior_probe")
public class WarriorProbe {
 final List<String> results=new ArrayList<>();ServerLevel world;ServerPlayer player;
 void eq(String label,double actual,double expected){if(Math.abs(actual-expected)>.012)throw new AssertionError(label+" actual="+actual+" expected="+expected);results.add("PASS "+label+" "+actual);}
 Holder<Spell> spell(String id){return SpellRegistry.from(world).get(Identifier.parse(id)).orElseThrow();}
 Holder<MobEffect> effect(String id){return WarriorRules.effect(id);}
 void equip(List<String> passives,List<String> modifiers){
  var owner=(SpellContainerSource.Owner)player;var old=owner.getSpellContainers();var ids=new ArrayList<>(passives);ids.addAll(modifiers);
  var container=new net.spell_engine.api.spell.container.SpellContainer(net.spell_engine.api.spell.container.SpellContainer.ContentType.NONE,"","",0,ids);
  owner.setSpellContainers(new SpellContainerSource.Result(old.activeContainer(),List.of(),passives.stream().map(this::spell).toList(),modifiers.stream().map(this::spell).toList(),List.of(new SpellContainerSource.SourcedContainer("warrior",ItemStack.EMPTY,container))));owner.spellModifierCache().clear();
 }
 LivingEntity mob(String type,double x,double y,double z,float hp){var m=(LivingEntity)BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(type)).create(world,EntitySpawnReason.COMMAND);m.setPos(x,y,z);m.getAttribute(Attributes.MAX_HEALTH).setBaseValue(hp);m.setHealth(hp);if(m instanceof Mob ai)ai.setNoAi(true);m.setNoGravity(true);world.addFreshEntity(m);return m;}
 SpellExecution.ImpactContext context(Holder<Spell> h,float q){return new SpellExecution.ImpactContext(1,1,null,new SpellPower.Result(h.value().school,40,0,1.5),SpellTarget.FocusMode.DIRECT,0).charge(q).chargeModifier(SpellModifiers.scaledBy(h.value().active.cast.charge.bonus,q));}
 void hit(SpellProjectile p,LivingEntity target)throws Exception{p.setPos(target.getBoundingBox().getCenter());target.invulnerableTime=0;var method=SpellProjectile.class.getDeclaredMethod("onHitEntity",EntityHitResult.class);method.setAccessible(true);method.invoke(p,new EntityHitResult(target,target.getBoundingBox().getCenter()));}
 Spell.ProjectileData.Perks perks(Holder<Spell> h){var p=h.value().deliver.projectile.projectile.perks.copy();for(var m:SpellModifiers.of(player,h))if(m.projectile_perks!=null)p.mutatingCombine(m.projectile_perks);return p;}
 SpellProjectile projectile(Holder<Spell> h,float q){return new SpellProjectile(world,player,player.getX(),player.getEyeY(),player.getZ(),SpellProjectile.Behaviour.FLY,h,context(h,q),perks(h));}
 void throwsTest()throws Exception{
  player.setPos(0,80,0);var h=spell("rogues:throw");eq("Throw cooldown",h.value().cost.cooldown.duration,5);eq("Throw speed",h.value().deliver.projectile.launch_properties.velocity,1);
  var target=mob("minecraft:villager",0,80,4,1000);
  for(int branch=0;branch<3;branch++)for(float charge:new float[]{0,.05f,.2f,.5f,.6f,1}){
   var mods=new ArrayList<String>();if(branch>0)mods.add("skill_tree_rpgs:warrior_tier_2_spell_1_root");if(branch>1)mods.add("skill_tree_rpgs:warrior_tier_2_spell_1_modifier_2");equip(List.of(),mods);player.removeAllEffects();target.removeAllEffects();target.setHealth(1000);
   var p=projectile(h,charge);WarriorRules.launch(p,player,h,context(h,charge));eq("Throw Weakness IV",player.getEffect(MobEffects.WEAKNESS).getAmplifier(),3);eq("Throw weakness two seconds",player.getEffect(MobEffects.WEAKNESS).getDuration(),40);
   hit(p,target);eq("Throw snapshot damage branch="+branch+" q="+charge,1000-target.getHealth(),40*((branch==0?.4:branch==1?.5:.6)+.4*Math.max(0,Math.min(1,(charge-.2)/.8))));
   eq("Throw slow one second",target.getEffect(MobEffects.SLOWNESS).getDuration(),20);if(branch==2)eq("Head stun high-HP target",target.getEffect(effect("spell_engine:stun")).getDuration(),5);p.discard();
  }
  equip(List.of(),List.of("skill_tree_rpgs:warrior_tier_2_spell_1_root","skill_tree_rpgs:warrior_tier_2_spell_1_modifier_1"));player.removeAllEffects();target.removeAllEffects();var next=mob("minecraft:villager",2,80,5,1000);var third=mob("minecraft:villager",4,80,6,1000);var p=projectile(h,1);p.setDeltaMovement(0,0,1);eq("Strong Throw two pierces",p.mutablePerks().pierce,2);eq("Strong Throw no ricochet",p.mutablePerks().ricochet,0);eq("Strong Throw no bounce",p.mutablePerks().bounce,0);
  hit(p,target);WarriorRules.beforeTick(p);results.add("Strong diagnostic target="+p.getFollowedTarget()+" owner="+p.getOwner()+" next="+next+" pos="+p.position()+" pierce="+p.mutablePerks().pierce);eq("Strong Throw acquires next",p.getFollowedTarget()==next?1:0,1);hit(p,next);WarriorRules.beforeTick(p);eq("Strong Throw acquires third",p.getFollowedTarget()==third?1:0,1);hit(p,third);eq("Strong Throw stops after three",p.isRemoved()?1:0,1);target.discard();next.discard();third.discard();player.removeAllEffects();
 }
 void netsTest()throws Exception{
  equip(List.of(),List.of());var rangedNet=spell("rogues:throw_net");
  for(float q:new float[]{0,.05f,.2f,.5f,.6f,1}){var dummy=mob("minecraft:villager",0,80,10,1000);var arrow=projectile(rangedNet,q);hit(arrow,dummy);eq("Net minimum-to-full damage q="+q,1000-dummy.getHealth(),40*(.075+.075*Math.max(0,Math.min(1,(q-.2)/.8))));dummy.discard();arrow.discard();}
  var h=spell("rogues:throw_net");eq("Net cast half second",h.value().active.cast.duration,.5);eq("Net cooldown five",h.value().cost.cooldown.duration,5);eq("Net full damage coefficient",h.value().impacts.getFirst().action.damage.spell_power_coefficient,.15);
  equip(List.of(),List.of("skill_tree_rpgs:warrior_tier_2_spell_2_root"));var p=projectile(h,1);WarriorRules.launch(p,player,h,context(h,1));eq("Improved net actual flight budget",p.range,30);p.discard();
  equip(List.of(),List.of());var victim=mob("minecraft:villager",0,80,10,100);var high=mob("minecraft:villager",3,80,10,1000);p=projectile(h,1);hit(p,victim);eq("Net full damage",100-victim.getHealth(),6);eq("Net duration",victim.getEffect(effect("rogues:net_trap")).getDuration(),40);
  var start=victim.position();victim.move(MoverType.SELF,new Vec3(1,0,0));eq("Net prevents independent movement",victim.position().distanceToSqr(start),0);WarriorRules.dragTick(victim);eq("Net moves gradually first step",victim.position().distanceTo(start),1.5);for(int i=0;i<7;i++)WarriorRules.dragTick(victim);eq("Net finishes within 0.1 blocks of pull destination",victim.position().distanceTo(player.position().add(player.getViewVector(1).multiply(1,0,1).normalize().scale(2)))<.1?1:0,1);
  p=projectile(h,1);hit(p,high);eq("High HP takes damage",1000-high.getHealth(),6);eq("Net preserves HP gate",high.hasEffect(effect("rogues:net_trap"))?1:0,0);victim.discard();high.discard();
  equip(List.of(),List.of("skill_tree_rpgs:warrior_tier_2_spell_2_modifier_1"));var center=mob("minecraft:villager",0,80,10,100);var near=mob("minecraft:villager",4.5,80,10,100);var far=mob("minecraft:villager",5.7,80,10,100);var pet=(net.minecraft.world.entity.animal.wolf.Wolf)mob("minecraft:wolf",1,80,10,100);pet.setOwnerReference(EntityReference.of(player.getUUID()));p=projectile(h,1);hit(p,center);results.add("Bigger diagnostic area="+new com.google.gson.Gson().toJson(SpellModifiers.extendedImpactsOf(player,h).areaImpact())+" near="+near.position()+" query="+world.getEntitiesOfClass(LivingEntity.class,center.getBoundingBox().inflate(5)).size());eq("Bigger Net nearby damage",100-near.getHealth(),6);eq("Bigger Net nearby rooted",near.hasEffect(effect("rogues:net_trap"))?1:0,1);eq("Bigger Net outside unaffected",far.getHealth(),100);eq("Bigger Net pet protected",pet.getHealth(),100);eq("Bigger Net pet not rooted",pet.hasEffect(effect("rogues:net_trap"))?1:0,0);center.discard();near.discard();far.discard();pet.discard();
  equip(List.of(),List.of("skill_tree_rpgs:warrior_tier_2_spell_2_modifier_2"));eq("Multiple Nets cooldown",SpellParameters.getCooldownDuration(player,h),3);victim=mob("minecraft:villager",0,80,10,100);p=projectile(h,.5f);hit(p,victim);eq("Half net damage",100-victim.getHealth(),4.125);eq("Multiple Nets duration",victim.getEffect(effect("rogues:net_trap")).getDuration(),30);victim.discard();
 }
 void passives(){
  var target=mob("minecraft:villager",0,80,4,1000);player.removeAllEffects();equip(List.of("skill_tree_rpgs:warrior_tier_1_passive_1"),List.of());var cd=((SpellCaster.Player)player).getCooldownManager();double base=player.getAttributeValue(Attributes.ATTACK_DAMAGE);
  for(int i=1;i<=7;i++){cd.remove(Identifier.parse("skill_tree_rpgs:warrior_tier_1_passive_1"));SpellTriggers.onMeleeImpact(player,target);var e=player.getEffect(effect("skill_tree_rpgs:killing_spree"));eq("Killing Spree live-hit stack "+i,e.getAmplifier()+1,Math.min(i,5));eq("Spree duration",e.getDuration(),160);eq("Spree damage modifier",player.getAttributeValue(Attributes.ATTACK_DAMAGE),base*(1+.02*Math.min(i,5)));}
  player.removeAllEffects();equip(List.of("skill_tree_rpgs:warrior_tier_1_passive_2"),List.of());float initial=player.getMaxHealth();double armor=player.getAttributeValue(Attributes.ARMOR);player.setHealth(5);
  for(int i=1;i<=12;i++){
   cd.remove(Identifier.parse("skill_tree_rpgs:warrior_tier_1_passive_2"));float before=player.getHealth();SpellTriggers.onDamageTaken(player,world.damageSources().mobAttack(target),1);
   eq("Vitality maximum HP "+i,player.getMaxHealth(),initial+2*Math.min(i,10));eq("Vitality flat armor "+i,player.getAttributeValue(Attributes.ARMOR),armor+Math.min(i,10));eq("Vitality heals only new hearts "+i,player.getHealth(),before+(i<=10?2:0));eq("Vitality duration",player.getEffect(effect("skill_tree_rpgs:vitality")).getDuration(),160);
  }
  player.removeAllEffects();for(var source:List.of(world.damageSources().onFire(),world.damageSources().wither(),world.damageSources().freeze(),world.damageSources().magic(),world.damageSources().fall())){
   cd.remove(Identifier.parse("skill_tree_rpgs:warrior_tier_1_passive_2"));SpellTriggers.onDamageTaken(player,new net.minecraft.world.damagesource.DamageSource(source.typeHolder(),target),1);eq("Vitality ignores attributed DOT/environment "+source.typeHolder().unwrapKey(),player.hasEffect(effect("skill_tree_rpgs:vitality"))?1:0,0);
  }
  eq("Vitality removes bonus HP",player.getMaxHealth(),initial);eq("Vitality health clamped after removal",player.getHealth()<=player.getMaxHealth()?1:0,1);target.discard();player.removeAllEffects();equip(List.of(),List.of());
 }
 void shields()throws Exception{
  var defender=mob("minecraft:villager",0,80,0,1000);defender.setYRot(0);defender.setYHeadRot(0);defender.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.SHIELD));defender.startUsingItem(InteractionHand.OFF_HAND);var attacker=mob("minecraft:zombie",0,80,4,100);
  defender.hurtServer(world,world.damageSources().mobAttack(attacker),10);eq("Shield activation delay",1000-defender.getHealth(),10);
  for(int i=0;i<10;i++)defender.tick();defender.setHealth(1000);defender.invulnerableTime=0;
  eq("Shield active",defender.getItemBlockingWith()!=null?1:0,1);defender.hurtServer(world,world.damageSources().mobAttack(attacker),10);eq("Shield frontal eighty percent",1000-defender.getHealth(),2);
  defender.addEffect(new MobEffectInstance(MobEffects.RESISTANCE,100,1));defender.setHealth(1000);defender.invulnerableTime=0;defender.hurtServer(world,world.damageSources().mobAttack(attacker),10);eq("Shield multiplies Resistance II",1000-defender.getHealth(),1.2);defender.removeAllEffects();
  defender.setHealth(1000);defender.invulnerableTime=0;defender.hurtServer(world,world.damageSources().sonicBoom(attacker),10);eq("Shield stops sonic bypass",1000-defender.getHealth(),2);
  var arrow=EntityTypes.ARROW.create(world,EntitySpawnReason.COMMAND);arrow.setPos(0,81,2);var pierce=net.minecraft.world.entity.projectile.arrow.AbstractArrow.class.getDeclaredMethod("setPierceLevel",byte.class);pierce.setAccessible(true);pierce.invoke(arrow,(byte)4);defender.setHealth(1000);defender.invulnerableTime=0;defender.hurtServer(world,world.damageSources().arrow(arrow,attacker),10);eq("Shield stops piercing arrow",1000-defender.getHealth(),2);arrow.discard();
  var reduction=defender.getAttribute(BuiltInRegistries.ATTRIBUTE.get(Identifier.parse("spell_engine:damage_taken")).orElseThrow());double originalReduction=reduction.getBaseValue();reduction.setBaseValue(originalReduction*.5);defender.setHealth(1000);defender.invulnerableTime=0;defender.hurtServer(world,world.damageSources().mobAttack(attacker),10);eq("Fifty percent reduction times shield equals one",1000-defender.getHealth(),1);reduction.setBaseValue(originalReduction);
  attacker.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_AXE));defender.setHealth(1000);defender.invulnerableTime=0;defender.hurtServer(world,world.damageSources().mobAttack(attacker),10);eq("Axe does not interrupt block",defender.isUsingItem()?1:0,1);
  defender.getItemBlockingWith().get(net.minecraft.core.component.DataComponents.BLOCKS_ATTACKS).disable(world,defender,5,defender.getItemBlockingWith());eq("Direct disable attempt ignored",defender.isUsingItem()?1:0,1);
  attacker.setPos(0,80,-4);defender.setHealth(1000);defender.invulnerableTime=0;defender.hurtServer(world,world.damageSources().mobAttack(attacker),10);eq("Shield rear full damage",1000-defender.getHealth(),10);defender.discard();attacker.discard();
 }
 void haste(){
  var attr=player.getAttribute(BuiltInRegistries.ATTRIBUTE.get(Identifier.parse("spell_power:haste")).orElseThrow());double old=attr.getBaseValue();
  var ids=new ArrayList<>(List.of("rogues:throw","rogues:throw_net","rogues:charge","rogues:shout","rogues:mortal_strike","rogues:last_stand"));for(int t=1;t<=3;t++)for(int i=1;i<=2;i++)ids.add("skill_tree_rpgs:warrior_tier_"+t+"_passive_"+i);
  try{for(String id:ids){var h=spell(id);eq(id+" haste enabled",h.value().cost.cooldown.haste_affected?1:0,1);attr.setBaseValue(100);double a=SpellParameters.getCooldownDuration(player,h);attr.setBaseValue(200);eq(id+" cooldown halves at double haste",SpellParameters.getCooldownDuration(player,h),a/2);if(h.value().type==Spell.Type.ACTIVE){eq(id+" cast haste",h.value().active.cast.haste_affected?1:0,1);attr.setBaseValue(100);double cast=SpellParameters.getCastTimeDetails(player,h.value()).length();attr.setBaseValue(200);eq(id+" cast halves at double haste",SpellParameters.getCastTimeDetails(player,h.value()).length(),Math.round(cast/2));}}}finally{attr.setBaseValue(old);}
 }
 public WarriorProbe(){NeoForge.EVENT_BUS.addListener(this::start);}
 void start(ServerStartedEvent event){var server=event.getServer();server.execute(()->{try{
  world=server.overworld();player=FakePlayerFactory.getMinecraft(world);player.setPos(0,80,0);world.getChunk(0,0);var manager=ServerLevel.class.getDeclaredField("entityManager");manager.setAccessible(true);((net.minecraft.world.level.entity.PersistentEntitySectionManager<?>)manager.get(world)).updateChunkStatus(new net.minecraft.world.level.ChunkPos(0,0),FullChunkStatus.ENTITY_TICKING);throwsTest();netsTest();passives();shields();haste();
  RogueTierTwoProbe.run(world,player,results::add);PartyProbe.run(server,results);
  Files.write(Path.of("warrior-results.txt"),results);Files.writeString(Path.of("warrior-result.txt"),"PASS "+results.size()+" Warrior/shield/party checks\n");
 }catch(Throwable e){e.printStackTrace();try{Files.write(Path.of("warrior-results.txt"),results);Files.writeString(Path.of("warrior-result.txt"),"FAIL "+e);}catch(Exception ignored){}}server.halt(false);});}
}
