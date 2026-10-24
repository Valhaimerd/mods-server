package local.audit;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.internals.*;
import net.spell_engine.internals.casting.SpellCaster;
import net.spell_engine.internals.container.SpellContainerSource;
import net.spell_engine.internals.impact.SpellImpacts;
import net.spell_engine.internals.target.SpellTarget;
import net.spell_power.api.SpellPower;
import net.rogues.entity.BearTrapEntity;
public final class RogueTierTwoProbe {
 static ServerLevel world;static ServerPlayer player;static Consumer<String> out;
 static Holder<Spell> spell(String id){return SpellRegistry.from(world).get(Identifier.parse(id)).orElseThrow();}
 static Holder<MobEffect> effect(String id){return BuiltInRegistries.MOB_EFFECT.get(Identifier.parse(id)).orElseThrow();}
 static void eq(String key,double got,double want){if(Math.abs(got-want)>.01)throw new AssertionError(key+": "+got+" != "+want);out.accept("Rogue II PASS "+key+" actual="+got+" expected="+want);}
 static void equip(List<String> passives,List<String> mods){
  var owner=(SpellContainerSource.Owner)player;var old=owner.getSpellContainers();var ids=new ArrayList<>(passives);ids.addAll(mods);
  var container=new net.spell_engine.api.spell.container.SpellContainer(net.spell_engine.api.spell.container.SpellContainer.ContentType.NONE,"","",0,ids);
  owner.setSpellContainers(new SpellContainerSource.Result(old.activeContainer(),List.of(),passives.stream().map(RogueTierTwoProbe::spell).toList(),mods.stream().map(RogueTierTwoProbe::spell).toList(),List.of(new SpellContainerSource.SourcedContainer("rogue_ii",ItemStack.EMPTY,container))));owner.spellModifierCache().clear();
 }
 static SpellExecution.ImpactContext context(Holder<Spell> h){return new SpellExecution.ImpactContext(1,1,null,new SpellPower.Result(h.value().school,40,0,1.5),SpellTarget.FocusMode.DIRECT,0);}
 static LivingEntity mob(String type,double x,double y,double z){var m=(LivingEntity)BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(type)).create(world,EntitySpawnReason.COMMAND);m.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);m.setHealth(1000);m.setPos(x,y,z);if(m instanceof Mob ai)ai.setNoAi(true);world.addFreshEntity(m);return m;}
 static void impact(LivingEntity target,Holder<Spell> h){target.invulnerableTime=0;SpellImpacts.performImpacts(world,player,target,target,h,h.value().impacts,context(h),false,null);}
 public static void run(ServerLevel w,ServerPlayer p,Consumer<String> sink)throws Exception{
  world=w;player=p;out=sink;var owner=(SpellContainerSource.Owner)player;var old=owner.getSpellContainers();var originalPos=player.position();
  var haste=player.getAttribute(BuiltInRegistries.ATTRIBUTE.get(Identifier.parse("spell_power:haste")).orElseThrow());double oldHaste=haste.getBaseValue();
  try{
   var f=ServerLevel.class.getDeclaredField("entityManager");f.setAccessible(true);((net.minecraft.world.level.entity.PersistentEntitySectionManager<?>)f.get(world)).updateChunkStatus(new net.minecraft.world.level.ChunkPos(0,0),FullChunkStatus.ENTITY_TICKING);
   for(int x=4;x<=12;x++)for(int z=4;z<=12;z++)world.setBlockAndUpdate(new net.minecraft.core.BlockPos(x,79,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
   player.setPos(1,80,1);equip(List.of(),List.of());var shadow=spell("rogues:shadow_step");eq("Shadow range",shadow.value().range,14);
   equip(List.of(),List.of("skill_tree_rpgs:rogue_tier_3_spell_1_root"));eq("Improved Shadow range",shadow.value().range+SpellModifiers.of(player,shadow).stream().mapToDouble(m->m.range_add).sum(),18);
   var victim=mob("minecraft:villager",8,80,8);var near=(Mob)mob("minecraft:zombie",10,80,8);var far=(Mob)mob("minecraft:zombie",15,80,8);near.setTarget(victim);far.setTarget(victim);eq("Mob accepts initial target",near.getTarget()==victim?1:0,1);
   equip(List.of(),List.of("skill_tree_rpgs:rogue_tier_3_spell_1_modifier_2"));player.setPos(8,80,8);impact(victim,shadow);
   eq("Ambush immediate damage",1000-victim.getHealth(),20);for(String id:List.of("blindness","weakness","slowness")){eq("Ambush "+id+" duration",victim.getEffect(effect("minecraft:"+id)).getDuration(),40);eq("Ambush "+id+" level I",victim.getEffect(effect("minecraft:"+id)).getAmplifier(),0);}
   out.accept("Shadow actual arrival="+player.position()+" near="+near.position());eq("Nearby mob disrupted",near.hasEffect(effect(TrapRules.DISRUPTION))?near.getEffect(effect(TrapRules.DISRUPTION)).getDuration():0,30);eq("Nearby target cleared",near.getTarget()==null?1:0,1);near.setTarget(victim);eq("Reacquisition blocked",near.getTarget()==null?1:0,1);eq("Distant mob unaffected",far.hasEffect(effect(TrapRules.DISRUPTION))?1:0,0);
   near.removeEffect(effect(TrapRules.DISRUPTION));near.setTarget(victim);eq("Reacquisition resumes",near.getTarget()==victim?1:0,1);victim.discard();near.discard();far.discard();
   var defender=mob("minecraft:villager",8,80,8);defender.hurtServer(world,world.damageSources().generic(),1);eq("Unprotected damage",defender.getHealth(),999);defender.setHealth(1000);defender.invulnerableTime=0;
   var immunity=spell("skill_tree_rpgs:rogue_tier_3_spell_1_modifier_1").value().modifiers.getFirst().impacts.getFirst();var perform=SpellImpacts.class.getDeclaredMethod("performImpact",net.minecraft.world.level.Level.class,LivingEntity.class,Entity.class,Holder.class,Spell.Impact.class,SpellExecution.ImpactContext.class,Collection.class);perform.setAccessible(true);
   perform.invoke(null,world,defender,defender,shadow,immunity,context(shadow),null);
   eq("Cloak immunity ticks",((net.spell_engine.api.entity.LivingEntityImmunity.Owner)defender).getImmunities().getLast().validUntil()-defender.tickCount,20);
   for(var damage:List.of(world.damageSources().generic(),world.damageSources().magic(),world.damageSources().inFire(),world.damageSources().fellOutOfWorld())){defender.invulnerableTime=0;defender.hurtServer(world,damage,10);eq("Cloak repeat blocks "+damage.typeHolder().unwrapKey(),defender.getHealth(),1000);}
   defender.tickCount+=21;defender.tick();defender.invulnerableTime=0;defender.hurtServer(world,world.damageSources().generic(),1);eq("Cloak expires",defender.getHealth(),999);defender.discard();
   equip(List.of(),List.of());var dragon=mob("minecraft:ender_dragon",8,80,8);impact(dragon,spell("rogues:bear_trap"));eq("Bear Trap roots immune boss",dragon.hasEffect(effect("rogues:bear_trap"))?1:0,1);var dragonPos=dragon.position();dragon.move(MoverType.SELF,new net.minecraft.world.phys.Vec3(2,1,0));eq("Bear Trap holds custom boss movement",dragon.position().distanceToSqr(dragonPos),0);dragon.removeEffect(effect("rogues:bear_trap"));dragon.discard();
   traps();serrated();rolls();
   equip(List.of(),List.of());for(String id:List.of("rogues:shock_powder","rogues:slice_and_dice","rogues:shadow_step","rogues:bear_trap","rogues:vanish","rogues:mutilate","skill_tree_rpgs:rogue_tier_1_passive_1","skill_tree_rpgs:rogue_tier_1_passive_2","skill_tree_rpgs:rogue_tier_2_passive_1","skill_tree_rpgs:rogue_tier_2_passive_2","skill_tree_rpgs:rogue_tier_3_passive_1","skill_tree_rpgs:rogue_tier_3_passive_2")){
    var h=spell(id);eq(id+" haste flag",h.value().cost.cooldown.haste_affected?1:0,1);haste.setBaseValue(100);double base=SpellParameters.getCooldownDuration(player,h);haste.setBaseValue(200);eq(id+" double haste cooldown",SpellParameters.getCooldownDuration(player,h),base/2);
    if(h.value().type==Spell.Type.ACTIVE){eq(id+" haste cast flag",h.value().active.cast.haste_affected?1:0,1);haste.setBaseValue(100);double cast=SpellParameters.getCastTimeDetails(player,h.value()).length();haste.setBaseValue(200);eq(id+" double haste cast",SpellParameters.getCastTimeDetails(player,h.value()).length(),cast/2);}

   }
  }finally{player.removeAllEffects();haste.setBaseValue(oldHaste);player.setPos(originalPos);owner.setSpellContainers(old);owner.spellModifierCache().clear();}
 }
 static void traps(){
  var h=spell("rogues:bear_trap");eq("Trap cast",h.value().active.cast.duration,.2);eq("Trap cooldown",h.value().cost.cooldown.duration,4);eq("One placement",h.value().deliver.clouds.getFirst().additional_placements.size(),0);eq("Forward placement",h.value().deliver.clouds.getFirst().placement.location_offset_by_look,3);
  for(boolean extensive:new boolean[]{false,true}){
   equip(List.of(),extensive?List.of("skill_tree_rpgs:rogue_tier_3_spell_2_root","skill_tree_rpgs:rogue_tier_3_spell_2_modifier_2"):List.of());
   var mod=new Spell.Modifier.Cloud();for(var m:SpellModifiers.of(player,h))mod.radius_add+=m.cloud.radius_add;
   var cloud=h.value().deliver.clouds.getFirst();var trap=(BearTrapEntity)BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse("rogues:bear_trap")).create(world,EntitySpawnReason.COMMAND);trap.setOwner(player);trap.onCreatedFromSpell(Identifier.parse("rogues:bear_trap"),cloud,context(h),20,mod);trap.setPos(8,80,8);world.addFreshEntity(trap);
   eq("Trap height",trap.getBbHeight(),2.5);eq("Trap hit radius extensive="+extensive,trap.getBbWidth()/2,extensive?2.25:1.5);eq("Trap cannot be targeted",trap.isPickable()?1:0,0);eq("Trap does not intercept projectiles",trap.canBeHitByProjectile()?1:0,0);
   var band=mob("minecraft:villager",extensive?10.1:9.6,80,8);trap.tickCount=19;trap.tick();eq("Trap arming visible",TrapRules.hidden(trap)?1:0,0);eq("No damage while arming",band.getHealth(),1000);
   var pet=(net.minecraft.world.entity.animal.wolf.Wolf)mob("minecraft:wolf",8.3,80,8);pet.setOwnerReference(EntityReference.of(player.getUUID()));trap.tickCount=20;trap.tick();eq("Outer band and protected pet cannot activate",trap.isSprung()?1:0,0);eq("Armed trap hidden",TrapRules.hidden(trap)?1:0,1);
   var jumping=mob("minecraft:villager",8.7,81.2,8);jumping.setOnGround(false);trap.tickCount=21;trap.tick();eq("Jumping enemy activates",trap.isSprung()?1:0,1);eq("Activated trap visible",TrapRules.hidden(trap)?1:0,0);
   double damage=extensive?30:16;eq("Primary trap damage",1000-jumping.getHealth(),damage);eq("Multiple enemies caught",1000-band.getHealth(),damage);
   for(var m:List.of(band,jumping)){eq("Unrestricted high-health root",m.getEffect(effect("rogues:bear_trap")).getDuration(),20);eq("Trap Slowness I duration",m.getEffect(effect("minecraft:slowness")).getDuration(),60);var pos=m.position();m.move(MoverType.SELF,new net.minecraft.world.phys.Vec3(1,1,0));eq("Root physically holds",m.position().distanceToSqr(pos),0);}
   trap.tickCount=22;trap.tick();eq("Single detonation only",1000-band.getHealth(),damage);eq("Protected pet unharmed by detonation",pet.getHealth(),1000);eq("Protected pet not rooted",pet.hasEffect(effect("rogues:bear_trap"))?1:0,0);trap.discard();band.discard();jumping.discard();pet.discard();
  }
 }
 static void serrated(){
  equip(List.of(),List.of("skill_tree_rpgs:rogue_tier_3_spell_2_root","skill_tree_rpgs:rogue_tier_3_spell_2_modifier_1"));var h=spell("rogues:bear_trap");var target=mob("minecraft:villager",8,80,8);impact(target,h);
  eq("Improved trap damage with Serrated",1000-target.getHealth(),30);eq("Serrated five seconds",target.getEffect(effect("spell_engine:bleed")).getDuration(),100);eq("Improved does not amplify Bleed power",target.getEffect(effect("spell_engine:bleed")).getAmplifier(),11);target.discard();
 }
 static void rolls(){
  player.removeAllEffects();equip(List.of("skill_tree_rpgs:rogue_tier_2_passive_1"),List.of());
  for(int roll=0;roll<3;roll++){SpellTriggers.onRoll(player);var e=player.getEffect(effect("skill_tree_rpgs:side_step"));eq("Sidestep nonstacking",e.getAmplifier(),0);eq("Sidestep duration",e.getDuration(),40);eq("Sidestep evasion",net.spell_engine.api.entity.SpellEngineAttributes.EVASION_CHANCE.asChance(player.getAttributeValue(net.spell_engine.api.entity.SpellEngineAttributes.EVASION_CHANCE.entry)),.2);}
  var enemy=mob("minecraft:zombie",4,80,4);SpellTriggers.onDamageTaken(player,world.damageSources().mobAttack(enemy),1);eq("Sidestep survives damage event",player.hasEffect(effect("skill_tree_rpgs:side_step"))?1:0,1);enemy.discard();eq("Sidestep no cooldown",spell("skill_tree_rpgs:rogue_tier_2_passive_1").value().cost.cooldown.duration,0);
  player.removeAllEffects();equip(List.of("skill_tree_rpgs:rogue_tier_2_passive_2"),List.of());var crit=player.getAttribute(BuiltInRegistries.ATTRIBUTE.get(Identifier.parse("critical_strike:damage")).orElseThrow());double before=crit.getValue();SpellTriggers.onRoll(player);
  eq("Opportunist guaranteed proc",player.hasEffect(effect("skill_tree_rpgs:opportunist"))?1:0,1);eq("Opportunist extra critical damage points",crit.getValue()-before,20);eq("Opportunist cooldown",spell("skill_tree_rpgs:rogue_tier_2_passive_2").value().cost.cooldown.duration,6);
 }
}
