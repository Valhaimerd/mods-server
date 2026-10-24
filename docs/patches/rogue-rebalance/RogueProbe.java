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
import net.spell_engine.internals.delivery.*;
import net.spell_engine.internals.impact.SpellImpacts;
import net.spell_engine.internals.target.SpellTarget;
import net.spell_power.api.SpellPower;
public final class RogueProbe {
 static ServerLevel world;static ServerPlayer player;static Consumer<String> out;
 static Holder<Spell> spell(String id){return SpellRegistry.from(world).get(Identifier.parse(id)).orElseThrow();}
 static Holder<MobEffect> effect(String id){return BuiltInRegistries.MOB_EFFECT.get(Identifier.parse(id)).orElseThrow();}
 static void eq(String key,double got,double want){if(Math.abs(got-want)>.01)throw new AssertionError(key+": "+got+" != "+want);out.accept("Rogue PASS "+key+" actual="+got+" expected="+want);}
 static void equip(List<String> passives,List<String> mods){
  var source=(SpellContainerSource.Owner)player;var old=source.getSpellContainers();var ids=new ArrayList<>(passives);ids.addAll(mods);
  var container=new net.spell_engine.api.spell.container.SpellContainer(net.spell_engine.api.spell.container.SpellContainer.ContentType.NONE,"","",0,ids);
  source.setSpellContainers(new SpellContainerSource.Result(old.activeContainer(),List.of(),passives.stream().map(RogueProbe::spell).toList(),mods.stream().map(RogueProbe::spell).toList(),List.of(new SpellContainerSource.SourcedContainer("rogue_probe",ItemStack.EMPTY,container))));source.spellModifierCache().clear();
 }
 static SpellExecution.ImpactContext context(Holder<Spell> h){return new SpellExecution.ImpactContext(1,1,null,new SpellPower.Result(h.value().school,40,0,1.5),SpellTarget.FocusMode.DIRECT,0);}
 static LivingEntity mob(String type,double hp){var mob=(LivingEntity)BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(type)).create(world,EntitySpawnReason.COMMAND);mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(hp);mob.setHealth((float)hp);mob.setPos(3,80,0);if(mob instanceof Mob m)m.setNoAi(true);return mob;}
 static void impact(LivingEntity target,Holder<Spell> h){target.invulnerableTime=0;SpellImpacts.performImpacts(world,player,target,target,h,h.value().impacts,context(h),false,null);}
 public static void run(ServerLevel w,ServerPlayer p,Consumer<String> sink)throws Exception{
  world=w;player=p;out=sink;var source=(SpellContainerSource.Owner)player;var old=source.getSpellContainers();
  var damage=player.getAttribute(Attributes.ATTACK_DAMAGE);double before=damage.getBaseValue();damage.setBaseValue(40);var crit=player.getAttribute(BuiltInRegistries.ATTRIBUTE.get(Identifier.parse("critical_strike:chance")).orElseThrow());double oldCrit=crit.getBaseValue();crit.setBaseValue(0);var spellCrit=player.getAttribute(BuiltInRegistries.ATTRIBUTE.get(Identifier.parse("spell_power:critical_chance")).orElseThrow());double oldSpellCrit=spellCrit.getBaseValue();spellCrit.setBaseValue(0);
  try{
   var shock=spell("rogues:shock_powder");eq("Shock cooldown",shock.value().cost.cooldown.duration,14);eq("Shock radius",shock.value().range,4);eq("Explosive radius",spell("skill_tree_rpgs:rogue_tier_2_spell_1_modifier_1").value().area_impact.radius,4);
   for(boolean improved:new boolean[]{false,true}){
    equip(List.of(),improved?List.of("skill_tree_rpgs:rogue_tier_2_spell_1_root"):List.of());
    for(double hp:new double[]{100,130,131,1000}){
     var target=mob("minecraft:villager",hp);impact(target,shock);var stun=target.getEffect(effect("rogues:shock"));eq("Mob stun HP="+hp+" improved="+improved,stun==null?0:stun.getDuration(),20*((hp<=130?3:1)+(improved?1:0)));target.discard();
    }
    var dragon=mob("minecraft:ender_dragon",1000);impact(dragon,shock);eq("Dragon bypass improved="+improved,dragon.hasEffect(effect("rogues:shock"))?dragon.getEffect(effect("rogues:shock")).getDuration():0,20*(improved?2:1));var start=dragon.position();dragon.move(MoverType.SELF,new net.minecraft.world.phys.Vec3(2,0,0));eq("Shocked boss movement held",dragon.position().distanceToSqr(start),0);dragon.discard();
    var enemy=net.neoforged.neoforge.common.util.FakePlayerFactory.get(world,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"ShockEnemy"));enemy.getAttribute(Attributes.MAX_HEALTH).setBaseValue(40);enemy.setPos(3,80,0);
    for(float hp:new float[]{20,23.9f,24,30}){enemy.removeAllEffects();enemy.setHealth(hp);impact(enemy,shock);eq("Player current HP="+hp+" improved="+improved,enemy.getEffect(effect("rogues:shock")).getDuration(),20*((hp<24?3:1)+(improved?1:0)));}enemy.removeAllEffects();
   }
   equip(List.of("skill_tree_rpgs:rogue_tier_2_spell_1_modifier_1"),List.of("skill_tree_rpgs:rogue_tier_2_spell_1_root","skill_tree_rpgs:rogue_tier_2_spell_1_modifier_1_bonus"));
   var managerField=ServerLevel.class.getDeclaredField("entityManager");managerField.setAccessible(true);var manager=(net.minecraft.world.level.entity.PersistentEntitySectionManager<?>)managerField.get(world);manager.updateChunkStatus(new net.minecraft.world.level.ChunkPos(0,0),FullChunkStatus.ENTITY_TICKING);
   var center=mob("minecraft:villager",1000);var edge=mob("minecraft:villager",1000);center.setPos(3,80,0);edge.setPos(5,80,0);world.addFreshEntity(center);world.addFreshEntity(edge);out.accept("Explosive fixture raw nearby="+world.getEntities(center,center.getBoundingBox().inflate(3)).stream().map(e->e.getType()+"@"+e.position()).toList()+" native lookup="+net.spell_engine.utils.TargetHelper.targetsFromArea(world,center,center.position().add(0,center.getBbHeight()/2,0),center.getLookAngle(),2.5f,spell("skill_tree_rpgs:rogue_tier_2_spell_1_modifier_1").value().area_impact.area,null).size()+" block="+world.getBlockState(center.blockPosition()));impact(center,shock);SpellTriggers.onSpellImpactSpecific(player,center,shock,shock.value().impacts.getFirst(),false,Spell.Trigger.Stage.POST);
   eq("Explosive Powder direct damage",1000-center.getHealth(),20);eq("Explosive Powder no distance falloff",1000-edge.getHealth(),20);eq("Explosive has no extra stun second",center.getEffect(effect("rogues:shock")).getDuration(),40);center.discard();edge.discard();
   var slice=spell("rogues:slice_and_dice");eq("Slice cooldown",slice.value().cost.cooldown.duration,16);
   for(int branch=0;branch<3;branch++){
    var mods=new ArrayList<String>();if(branch>0)mods.add("skill_tree_rpgs:rogue_tier_2_spell_2_root");if(branch==2)mods.add("skill_tree_rpgs:rogue_tier_2_spell_2_modifier_1");mods.add("skill_tree_rpgs:rogue_tier_2_spell_2_modifier_2");equip(List.of(),mods);player.removeAllEffects();
    eq("Slice effective cooldown branch="+branch,net.spell_engine.internals.SpellParameters.getCooldownDuration(player,slice),branch==0?16:14);SpellDelivery.deliver(world,slice,player,List.of(new SpellExecution.DeliveryTarget(player,context(slice))),context(slice),null,null);
    int count=branch==2?14:10;eq("Slice native charges branch="+branch,player.getEffect(effect("rogues:slice_and_dice")).getAmplifier()+1,count);eq("Slice no attack damage stacking",player.getAttributeValue(Attributes.ATTACK_DAMAGE),40);
    SpellTriggers.onMeleeImpact(player,player);eq("Self hits preserve charges",player.getEffect(effect("rogues:slice_and_dice")).getAmplifier()+1,count);
    var pet=(net.minecraft.world.entity.animal.wolf.Wolf)mob("minecraft:wolf",40);pet.setOwnerReference(EntityReference.of(player.getUUID()));SpellTriggers.onMeleeImpact(player,pet);eq("Owned pet hits preserve charges",player.getEffect(effect("rogues:slice_and_dice")).getAmplifier()+1,count);pet.discard();
    var target=mob("minecraft:villager",1000);
    for(int hit=1;hit<=count;hit++){
     float hp=target.getHealth();target.invulnerableTime=0;target.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);SpellTriggers.onMeleeImpact(player,target);
     eq("Slice actual bonus branch="+branch+" hit="+hit,hp-target.getHealth(),6);
     var ev=player.getEffect(effect("skill_tree_rpgs:slice_evasion"));eq("Evasion stacks "+hit,ev.getAmplifier()+1,Math.min(hit,10));eq("Evasion four-second refresh",ev.getDuration(),80);
     eq("Native evasion chance",net.spell_engine.api.entity.SpellEngineAttributes.EVASION_CHANCE.asChance(player.getAttributeValue(net.spell_engine.api.entity.SpellEngineAttributes.EVASION_CHANCE.entry)),.05*Math.min(hit,10));
     var speed=player.getEffect(effect("skill_tree_rpgs:fleet_footed"));eq("Fleet stacks "+hit,speed.getAmplifier()+1,Math.min(hit,12));eq("Fleet refresh",speed.getDuration(),80);
     eq("Pull toward caster",target.getDeltaMovement().x,-.2);
    }
    eq("Slice exhausted",player.hasEffect(effect("rogues:slice_and_dice"))?1:0,0);float hp=target.getHealth();SpellTriggers.onMeleeImpact(player,target);eq("No bonus beyond charge cap",hp-target.getHealth(),0);target.discard();
   }
   player.removeAllEffects();equip(List.of(),List.of());var fracture=spell("skill_tree_rpgs:rogue_tier_1_passive_2");eq("Fracture cooldown",fracture.value().cost.cooldown.duration,1);
   var target=mob("minecraft:villager",1000);target.getAttribute(Attributes.ARMOR).setBaseValue(30);
   for(int hit=1;hit<=4;hit++){
    impact(target,fracture);eq("Flat armor stack "+hit,target.getAttributeValue(Attributes.ARMOR),30-6*Math.min(hit,3));eq("Fracture has no damage",target.getHealth(),1000);eq("Fracture refresh",target.getEffect(effect("skill_tree_rpgs:fracture")).getDuration(),120);
   }
   target.removeEffect(effect("skill_tree_rpgs:fracture"));eq("Dispel restores scaled armor",target.getAttributeValue(Attributes.ARMOR),30);
   impact(target,fracture);for(int tick=0;tick<121;tick++)target.tick();eq("Expiry restores scaled armor",target.getAttributeValue(Attributes.ARMOR),30);target.discard();
   var coated=spell("skill_tree_rpgs:rogue_tier_1_passive_1");eq("Coated cooldown",coated.value().cost.cooldown.duration,1);eq("Coated chance",coated.value().passive.triggers.getFirst().chance,.25);
  }finally{player.removeAllEffects();damage.setBaseValue(before);crit.setBaseValue(oldCrit);spellCrit.setBaseValue(oldSpellCrit);source.setSpellContainers(old);source.spellModifierCache().clear();}
 }
}
