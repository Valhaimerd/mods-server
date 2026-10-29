package net.spell_engine.internals;

import java.util.*;
import com.google.gson.*;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.*;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.spell_engine.api.effect.*;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.event.SpellHandlers;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.entity.WarriorRules;
import net.spell_engine.entity.WarriorTierTwo;
import net.spell_engine.internals.casting.SpellCaster;
import net.spell_engine.internals.container.SpellContainerSource;
import net.spell_engine.internals.impact.SpellImpacts;
import net.spell_engine.internals.target.SpellTarget;
import net.spell_engine.rpg_series.config.ConfigFile;
import net.spell_power.api.*;

/** Caster snapshots and per-recipient support effects. Ordinary damage/healing remain native impacts. */
public final class PaladinRevamp {
 public static final String DP="paladins:divine_protection",OATH="skill_tree_rpgs:divine_oath",HOT="skill_tree_rpgs:merciful_recovery",KR="skill_tree_rpgs:faith_knockback",POWER="skill_tree_rpgs:protection_power",GUARD="skill_tree_rpgs:paladin_control_guard",READY="skill_tree_rpgs:sinful_retaliation_ready",MARK="skill_tree_rpgs:sinful_mark";
 private static final Identifier DP_DR=key("protection_reduction"),DP_OVERLAP=key("protection_overlap"),DP_POWER=key("protection_power"),DP_KR=key("faith_knockback"),OATH_ARMOR=key("oath_armor"),OATH_ABS=key("oath_absorption");
 private static final Gson JSON=new Gson();private static boolean installed;private static final ThreadLocal<Boolean> EXTRA=ThreadLocal.withInitial(()->false);
 private static final TagKey<EntityType<?>> UNDEAD=TagKey.create(Registries.ENTITY_TYPE,Identifier.parse("minecraft:undead"));
 private static Identifier key(String path){return Identifier.parse("paladin_pack:"+path);}
 public static Holder<Spell> spell(LivingEntity e,String id){return SpellRegistry.from(e.level()).get(Identifier.parse(id)).orElseThrow();}
 public static boolean node(LivingEntity e,String suffix){return e instanceof SpellContainerSource.Owner owner&&owner.getSpellContainers().modifiers().stream().anyMatch(h->h.is(Identifier.parse("skill_tree_rpgs:paladin_"+suffix)));}
 public static boolean passive(LivingEntity e,String id){return e instanceof SpellContainerSource.Owner owner&&owner.getSpellContainers().passives().stream().anyMatch(h->h.is(Identifier.parse(id)));}
 public static Holder<MobEffect> effect(String id){return BuiltInRegistries.MOB_EFFECT.get(Identifier.parse(id)).orElseThrow();}
 private static JsonObject states(JsonObject root){return EffectDetails.object(root,"support");}
 private static JsonObject record(JsonObject root,String key){var states=states(root);return states.has(key)?states.getAsJsonObject(key):null;}
 private static double n(JsonObject o,String key){return o==null?0:EffectDetails.number(o,key);}
 private static boolean flag(JsonObject o,String key){return o!=null&&o.has(key)&&o.get(key).getAsBoolean();}
 private static long now(LivingEntity e){return e.level().getGameTime();}
 private static void status(LivingEntity e,String id,int ticks,LivingEntity source){e.addEffect(new MobEffectInstance(effect(id),Math.max(1,ticks),0),source);}
 private static void attribute(LivingEntity e,Holder<Attribute> attribute,Identifier id,double amount,AttributeModifier.Operation op){
  var value=e.getAttribute(attribute);if(value==null)return;var old=value.getModifier(id);if(old!=null&&Math.abs(old.amount()-amount)<.000001&&old.operation()==op)return;
  if(old!=null)value.removeModifier(id);if(Math.abs(amount)>.000001)value.addTransientModifier(new AttributeModifier(id,amount,op));
 }
 private static void remove(LivingEntity e,Holder<Attribute> attribute,Identifier id){var value=e.getAttribute(attribute);if(value!=null)value.removeModifier(id);}
 private static Holder<Attribute> damageTaken(){return BuiltInRegistries.ATTRIBUTE.get(Identifier.parse("spell_engine:damage_taken")).orElseThrow();}
 static double pSnapshot(LivingEntity e){var a=e.getAttribute(SpellSchools.HEALING.attributeEntry);var old=a==null?null:a.getModifier(DP_POWER);try{if(old!=null)a.removeModifier(DP_POWER);return SpellPower.getSpellPower(SpellSchools.HEALING,e).nonCriticalValue();}finally{if(old!=null)a.addTransientModifier(old);}}
 static double armorSnapshot(LivingEntity e){var a=e.getAttribute(Attributes.ARMOR);var old=a==null?null:a.getModifier(OATH_ARMOR);try{if(old!=null)a.removeModifier(OATH_ARMOR);return e.getAttributeValue(Attributes.ARMOR);}finally{if(old!=null)a.addTransientModifier(old);}}
 private static double melee(LivingEntity e){return SupportScaling.melee(e);}
 private static double banner(LivingEntity e){var old=e.getEffect(effect("skill_tree_rpgs:banner_protection"));if(old==null)return 0;double[] value={0};old.getEffect().value().createModifiers(old.getAmplifier(),(a,m)->{if(a.equals(damageTaken())&&m.operation()==AttributeModifier.Operation.ADD_MULTIPLIED_BASE)value[0]-=m.amount();});return Math.clamp(value[0],0,1);}
 private static boolean hostileHit(LivingEntity target,LivingEntity attacker){return attacker!=target&&PartyRules.harmfulAllowed(attacker,target);}
 public static void configureSkill(ConfigFile.Effects config){
  for(String[] row:new String[][]{{OATH,"Divine Oath"},{HOT,"Merciful Recovery"},{KR,"Steadfast Faith"},{POWER,"Divine Protection"},{GUARD,"Control Protection"},{READY,"Sinful Retaliation"}})WarriorTierTwo.entry(row[0],row[1],List.of(),config);
  WarriorTierTwo.entry(MARK,"Marked for Retribution",List.of(PriestRules.attr("spell_engine:damage_taken",.15f,AttributeModifier.Operation.ADD_MULTIPLIED_BASE)),config);
  ClassStatusVisuals.configure();
 }
 public static void ignoreOldProtection(Holder<MobEffect> effect,Protection.Pop pop){} // Replaces only Divine Protection's former finite-hit registry call.
 public static void init(){if(installed)return;installed=true;
  SpellHandlers.registerCustomDelivery(key("protection"),PaladinRevamp::protect);
  SpellHandlers.registerCustomDelivery(key("oath_launch"),PaladinRevamp::launchOath);
  SpellHandlers.registerCustomDelivery(key("break_restriction"),(world,h,caster,targets,context,pos)->{cleanse(caster);return true;});
  NeoForge.EVENT_BUS.addListener((EntityTickEvent.Pre event)->{if(event.getEntity() instanceof LivingEntity e&&!e.level().isClientSide()&&e.hasData(EffectDetails.DATA))tick(e);});
  NeoForge.EVENT_BUS.addListener((EntityTickEvent.Post event)->{if(event.getEntity() instanceof LivingEntity e&&!e.level().isClientSide()&&e.tickCount%5==0&&(e.hasData(EffectDetails.DATA)||e.getActiveEffects().stream().anyMatch(f->EffectDetails.rpg(BuiltInRegistries.MOB_EFFECT.getKey(f.getEffect().value()).toString()))))EffectDetails.update(e);});
  // Finite blocks are consumed before absorption by ProtectionHits. No flat-HP subtraction.
  NeoForge.EVENT_BUS.addListener((LivingDamageEvent.Post event)->{if(event.getInflictedDamage()<=0&&event.getReduction(net.neoforged.neoforge.common.damagesource.DamageContainer.Reduction.ABSORPTION)<=0)return;
   if(event.getSource().getEntity() instanceof LivingEntity attacker&&hostileHit(event.getEntity(),attacker)){incoming(event.getEntity(),attacker);extra(attacker,event.getEntity());}
  });
  NeoForge.EVENT_BUS.addListener((LivingDeathEvent event)->clear(event.getEntity()));
 }
 public static void cleanse(LivingEntity target){for(var current:List.copyOf(target.getActiveEffects()))if(current.getEffect().value().getCategory()==MobEffectCategory.HARMFUL&&WarriorTierTwo.impairing(current.getEffect()))target.removeEffect(current.getEffect());((EntityActionsAllowed.ControlledEntity)target).updateEntityActionsAllowed();}
 public static boolean protectedControl(boolean old,LivingEntity e){return old||e.hasEffect(effect(GUARD));}
 public static boolean impaired(LivingEntity e,EntityActionsAllowed.Player action,boolean show,Holder<Spell> h){
  if(action==EntityActionsAllowed.Player.CAST_SPELL&&h.is(Identifier.parse(DP))&&node(e,"tier_3_spell_1_root"))return false;
  return WarriorTierTwo.impaired(e,action,show,h);
 }
 public static boolean control(LivingEntity e){return !EntityActionsAllowed.fromEffects(e.getActiveEffects().stream().map(MobEffectInstance::getEffect).toList()).canMove()||(e.getAttributeValue(Attributes.MOVEMENT_SPEED)<=0&&e.getActiveEffects().stream().anyMatch(f->f.getEffect().value().getCategory()==MobEffectCategory.HARMFUL&&WarriorTierTwo.impairing(f.getEffect())));}
 public static boolean addControl(LivingEntity target,MobEffectInstance effect,LivingEntity caster){return !WarriorTierTwo.deny(target,effect.getEffect())&&PartyRules.addEffect(target,effect,caster);}
 public static boolean breakReady(LivingEntity e){return passive(e,"skill_tree_rpgs:paladin_tier_2_passive_2")&&(!(e instanceof SpellCaster.Player caster)||!caster.getCooldownManager().isCoolingDown(spell(e,"skill_tree_rpgs:paladin_tier_2_passive_2")));}
 public static boolean rollAvailable(boolean original,Player p,boolean enabled,boolean rolling,int available){if(!p.isAlive()||p.isSleeping()||p.isSpectator())return false;if(control(p))return enabled&&!rolling&&available>0&&breakReady(p);return original;}
 public static boolean rollAllowed(ServerPlayer p){return p.isAlive()&&(!control(p)||breakReady(p));}
 public static boolean rollGround(boolean original,Player p){return original||(control(p)&&breakReady(p)&&p.level().getBlockCollisions(p,p.getBoundingBox().move(0,-.1,0)).iterator().hasNext());}
 public static boolean evaluate(Holder<Spell> h,Spell.Trigger trigger,SpellTriggers.Event event){
  if(h.is(Identifier.parse("skill_tree_rpgs:paladin_tier_1_passive_2"))&&(event.damageSource==null||!(event.damageSource.getEntity() instanceof LivingEntity attacker)||!hostileHit(event.player,attacker)||event.damageAmount<=0))return false;
  if(h.is(Identifier.parse("skill_tree_rpgs:paladin_tier_1_passive_1"))&&((SpellCaster.Player)event.player).getActiveMeleeSkill()!=null)return false;
  return WarriorRules.evaluate(h,trigger,event);
 }
 public static Spell.Impact impact(LivingEntity caster,Holder<Spell> h,Spell.Impact input,SpellExecution.ImpactContext context,Entity target){
  var out=PriestRules.impact(caster,h,input,context,target);String id=h.unwrapKey().orElseThrow().identifier().toString();
  boolean holy=id.startsWith("paladins:")||id.startsWith("skill_tree_rpgs:paladin_")||id.startsWith("skill_tree_rpgs:priest_");
  if(holy&&out.action.type==Spell.Impact.Action.Type.DAMAGE&&target instanceof LivingEntity mob&&!(mob instanceof Player)&&mob.getType().builtInRegistryHolder().is(UNDEAD)){out=JSON.fromJson(JSON.toJson(out),Spell.Impact.class);out.action.damage.spell_power_coefficient*=1.25f;}
  return out;
 }
 public static boolean protect(Level world,Holder<Spell> h,LivingEntity caster,List<SpellExecution.DeliveryTarget> targets,SpellExecution.ImpactContext context,Vec3 location){
  if(world.isClientSide())return false;double p=pSnapshot(caster),m=melee(caster);boolean improved=node(caster,"tier_3_spell_1_root"),bulwark=node(caster,"tier_3_spell_1_modifier_1"),merciful=node(caster,"tier_3_spell_1_modifier_2");int ticks=improved?200:160;
  double reduction=SupportScaling.reduction(p,bulwark),knockback=bulwark||merciful?SupportScaling.knockback(m):0,blocks=SupportScaling.blocks(m);
  for(var ally:PriestRules.nearby(caster,caster.position(),8))if(PartyRules.allies(caster,ally)){
   var root=EffectDetails.data(ally);var old=record(root,"protection");var r=old==null?new JsonObject():old;
   r.addProperty("expires",Math.max(n(r,"expires"),now(ally)+ticks));r.addProperty("reduction",Math.max(n(r,"reduction"),reduction));r.remove("flat");r.addProperty("blocks",blocks);r.addProperty("knockback",Math.max(n(r,"knockback"),knockback));r.addProperty("recovery",Math.max(n(r,"recovery"),Math.min(1,.05*p)));r.addProperty("merciful",flag(r,"merciful")||merciful);r.addProperty("caster",caster.getUUID().toString());states(root).add("protection",r);EffectDetails.save(ally,root);
   status(ally,DP,(int)(n(r,"expires")-now(ally)),caster);if(n(r,"knockback")>0)status(ally,KR,(int)(n(r,"expires")-now(ally)),caster);
   if(improved){status(ally,GUARD,5,caster);cleanse(ally);}apply(ally,root);EffectDetails.update(ally);
  }
  var root=EffectDetails.data(caster);var power=new JsonObject();power.addProperty("expires",now(caster)+ticks);states(root).add("power",power);EffectDetails.save(caster,root);status(caster,POWER,ticks,caster);apply(caster,root);EffectDetails.update(caster);return true;
 }
 private static LivingEntity source(LivingEntity target,JsonObject r){try{var uuid=UUID.fromString(r.get("caster").getAsString());if(target.level() instanceof ServerLevel world){var p=world.getServer().getPlayerList().getPlayer(uuid);if(p!=null)return p;var e=world.getEntity(uuid);if(e instanceof LivingEntity living)return living;}}catch(RuntimeException ignored){}return target;}
 private static void healAmount(LivingEntity source,LivingEntity target,double amount){var h=spell(target,"paladins:paladin_restore");var context=new SpellExecution.ImpactContext().channeled(1).power(new SpellPower.Result(SpellSchools.HEALING,amount,0,1));SpellImpacts.performImpacts(target.level(),source,target,target,h,h.value().impacts,context,false,null);}
 public static boolean launchOath(Level world,Holder<Spell> h,LivingEntity caster,List<SpellExecution.DeliveryTarget> targets,SpellExecution.ImpactContext context,Vec3 location){
  if(world.isClientSide())return false;LivingEntity recipient=targets.stream().map(SpellExecution.DeliveryTarget::entity).filter(e->e instanceof LivingEntity&&e.isAlive()&&PartyRules.allies(caster,e)).map(e->(LivingEntity)e).findFirst().orElse(caster);
  if(caster.distanceToSqr(recipient)>225)return false;double p=pSnapshot(caster),m=melee(caster),armor=armorSnapshot(caster);boolean decree=node(caster,"tier_3_spell_2_modifier_1"),judgement=node(caster,"tier_3_spell_2_modifier_2");
  // Reuse native meteor delivery for the actual falling hammer; context holds the caster snapshot.
  var meteor=spell(caster,"paladins:oath_hammer");OATH_LAUNCH.put(caster,new OathLaunch(recipient,p,m,armor,SpellPower.getSpellPower(net.spell_engine.api.spell.ExternalSpellSchools.PHYSICAL_MELEE,caster).nonCriticalValue(),decree,judgement));
  try{net.spell_engine.internals.delivery.ProjectileLauncher.fallProjectile(world,caster,recipient,recipient.position(),meteor,context.channeled(1).power(new SpellPower.Result(SpellSchools.HEALING,p,0,1)));}finally{OATH_LAUNCH.remove(caster);}return true;
 }
 private record OathLaunch(LivingEntity target,double p,double m,double armor,double impactM,boolean decree,boolean judgement){}
 private static final Map<LivingEntity,OathLaunch> OATH_LAUNCH=new WeakHashMap<>();
 public static Entity launched(Entity entity,LivingEntity caster,Holder<Spell> h,SpellExecution.ImpactContext context){
  var launch=OATH_LAUNCH.get(caster);if(launch!=null&&entity instanceof net.spell_engine.entity.SpellProjectile projectile&&h.is(Identifier.parse("paladins:oath_hammer"))){var root=EffectDetails.data(projectile);var pending=new JsonObject();pending.addProperty("p",launch.p);pending.addProperty("m",launch.m);pending.addProperty("armor",launch.armor);pending.addProperty("impact_m",launch.impactM);pending.addProperty("decree",launch.decree);pending.addProperty("judgement",launch.judgement);pending.addProperty("caster",caster.getUUID().toString());pending.addProperty("target",launch.target.getUUID().toString());root.add("oath_snapshot",pending);EffectDetails.save(projectile,root);projectile.setFollowedTarget(launch.target);}
  return entity;
 }
 public static void fallImpact(LivingEntity caster,Entity projectile,Holder<Spell> h,SpellExecution.ImpactContext context){if(h.is(Identifier.parse("paladins:oath_hammer")))projectile(caster,projectile,null,h,context);else SpellImpacts.fallImpact(caster,projectile,h,context);}
 public static boolean projectile(LivingEntity caster,Entity projectile,Entity hit,Holder<Spell> h,SpellExecution.ImpactContext context){
  if(!h.is(Identifier.parse("paladins:oath_hammer")))return PriestRules.projectile(caster,projectile,hit,h,context);
  if(caster.level().isClientSide())return false;var root=EffectDetails.data(projectile);if(!root.has("oath_snapshot"))return true;var snapshot=root.getAsJsonObject("oath_snapshot");root.remove("oath_snapshot");EffectDetails.save(projectile,root);LivingEntity recipient=null;
  if(caster.level() instanceof ServerLevel world){var uuid=UUID.fromString(snapshot.get("target").getAsString());var entity=world.getEntity(uuid);if(uuid.equals(caster.getUUID()))recipient=caster;else if(entity instanceof LivingEntity living)recipient=living;else recipient=world.getServer().getPlayerList().getPlayer(uuid);}
  if(recipient!=null&&recipient.isAlive()&&PartyRules.allies(caster,recipient))landOath(caster,recipient,snapshot);return true;
 }

 public static void landOath(LivingEntity caster,LivingEntity recipient,JsonObject snapshot){
  double p=n(snapshot,"p"),m=n(snapshot,"m"),armor=n(snapshot,"armor");boolean decree=flag(snapshot,"decree"),judgement=flag(snapshot,"judgement");
  var h=spell(caster,judgement?"paladins:oath_landing_judgement":"paladins:oath_landing");var context=new SpellExecution.ImpactContext().channeled(1).power(new SpellPower.Result(net.spell_engine.api.spell.ExternalSpellSchools.PHYSICAL_MELEE,snapshot.has("impact_m")?n(snapshot,"impact_m"):m,0,1));
  for(var enemy:PriestRules.nearby(caster,recipient.position(),judgement?6:4))if(PartyRules.harmfulAllowed(caster,enemy))SpellImpacts.performImpacts(caster.level(),caster,enemy,recipient,h,h.value().impacts,context,false,null);
  var root=EffectDetails.data(recipient);var old=record(root,"oath");var r=old==null?new JsonObject():old;double oldCapacity=n(r,"capacity"),base=SupportScaling.shield(p,decree),delta=Math.max(0,base-n(r,"base_capacity")),grant=delta*(1+PriestPassives.absorptionBonus(recipient));
  r.addProperty("expires",Math.max(n(r,"expires"),now(recipient)+140));r.addProperty("damage",Math.max(n(r,"damage"),SupportScaling.oathDamage(m,decree)));r.addProperty("armor",Math.max(n(r,"armor"),SupportScaling.oathArmor(armor,decree)));r.addProperty("base_capacity",Math.max(n(r,"base_capacity"),base));r.addProperty("capacity",oldCapacity+grant);r.addProperty("caster",caster.getUUID().toString());states(root).add("oath",r);EffectDetails.save(recipient,root);
  status(recipient,OATH,(int)(n(r,"expires")-now(recipient)),caster);apply(recipient,root);recipient.setAbsorptionAmount(Math.min((float)recipient.getAttributeValue(Attributes.MAX_ABSORPTION),recipient.getAbsorptionAmount()+(float)grant));EffectDetails.update(recipient);
 }
 private static void apply(LivingEntity e,JsonObject root){
  var dp=record(root,"protection");if(dp!=null){double dr=n(dp,"reduction");attribute(e,damageTaken(),DP_DR,-dr,AttributeModifier.Operation.ADD_MULTIPLIED_BASE);attribute(e,damageTaken(),DP_OVERLAP,Math.min(dr,banner(e)),AttributeModifier.Operation.ADD_MULTIPLIED_BASE);attribute(e,Attributes.KNOCKBACK_RESISTANCE,DP_KR,n(dp,"knockback"),AttributeModifier.Operation.ADD_VALUE);}
  if(record(root,"power")!=null)attribute(e,SpellSchools.HEALING.attributeEntry,DP_POWER,.1,AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
  var oath=record(root,"oath");if(oath!=null){attribute(e,Attributes.ARMOR,OATH_ARMOR,n(oath,"armor"),AttributeModifier.Operation.ADD_VALUE);attribute(e,Attributes.MAX_ABSORPTION,OATH_ABS,n(oath,"capacity"),AttributeModifier.Operation.ADD_VALUE);}
 }
 private static void expire(LivingEntity e,JsonObject root,String key){states(root).remove(key);switch(key){
  case "protection"->{remove(e,damageTaken(),DP_DR);remove(e,damageTaken(),DP_OVERLAP);remove(e,Attributes.KNOCKBACK_RESISTANCE,DP_KR);e.removeEffect(effect(DP));e.removeEffect(effect(KR));}
  case "power"->{remove(e,SpellSchools.HEALING.attributeEntry,DP_POWER);e.removeEffect(effect(POWER));}
  case "oath"->{remove(e,Attributes.ARMOR,OATH_ARMOR);remove(e,Attributes.MAX_ABSORPTION,OATH_ABS);e.setAbsorptionAmount(Math.min(e.getAbsorptionAmount(),(float)e.getAttributeValue(Attributes.MAX_ABSORPTION)));e.removeEffect(effect(OATH));}
  case "hot"->e.removeEffect(effect(HOT));
 }}
 public static void tick(LivingEntity e){
  var root=EffectDetails.data(e);var all=states(root);if(all.size()==0)return;boolean changed=false;
  for(String key:List.copyOf(all.keySet())){var r=all.getAsJsonObject(key);String effectId=switch(key){case "protection"->DP;case "power"->POWER;case "oath"->OATH;case "hot"->HOT;default->null;};
   if(key.equals("hot")&&e.isAlive()&&e.hasEffect(effect(HOT))&&now(e)>=n(r,"next")&&n(r,"pulses")<5){healAmount(source(e,r),e,n(r,"amount"));r.addProperty("pulses",n(r,"pulses")+1);r.addProperty("next",n(r,"next")+20);changed=true;}
   if(!e.isAlive()||now(e)>n(r,"expires")||(effectId!=null&&!e.hasEffect(effect(effectId))&&now(e)<n(r,"expires"))){expire(e,root,key);changed=true;}
  }
  apply(e,root);if(changed)EffectDetails.save(e,root);
 }
 public static void clear(LivingEntity e){if(e.level().isClientSide())return;var root=EffectDetails.data(e);for(String key:List.copyOf(states(root).keySet()))expire(e,root,key);e.removeEffect(effect(READY));EffectDetails.save(e,root);EffectDetails.update(e);}
 private static void incoming(LivingEntity victim,LivingEntity attacker){
  var root=EffectDetails.data(victim);var dp=record(root,"protection");if(dp!=null&&flag(dp,"merciful")&&!flag(dp,"used")){dp.addProperty("used",true);var hot=new JsonObject();hot.addProperty("amount",n(dp,"recovery"));hot.addProperty("next",now(victim)+20);hot.addProperty("expires",now(victim)+100);hot.addProperty("pulses",0);hot.addProperty("caster",dp.get("caster").getAsString());states(root).add("hot",hot);EffectDetails.save(victim,root);status(victim,HOT,100,source(victim,dp));}
  if(victim.hasEffect(effect(READY))){victim.removeEffect(effect(READY));PartyRules.addEffect(attacker,new MobEffectInstance(effect(MARK),160),victim);PartyRules.addEffect(attacker,new MobEffectInstance(MobEffects.GLOWING,160),victim);attacker.igniteForSeconds(4);}
 }
 private static void extra(LivingEntity attacker,LivingEntity victim){
  if(EXTRA.get())return;var root=EffectDetails.data(attacker);var r=record(root,"oath");if(r==null||!attacker.hasEffect(effect(OATH))||now(attacker)<n(r,"next_hit"))return;r.addProperty("next_hit",now(attacker)+10);EffectDetails.save(attacker,root);
  EXTRA.set(true);try{var h=spell(attacker,"paladins:oath_extra");var context=new SpellExecution.ImpactContext().channeled(1).power(new SpellPower.Result(net.spell_engine.api.spell.ExternalSpellSchools.PHYSICAL_MELEE,n(r,"damage"),0,1));SpellImpacts.performImpacts(attacker.level(),attacker,victim,attacker,h,h.value().impacts,context,false,null);}finally{EXTRA.set(false);}
 }
 public static List<String> details(LivingEntity e,String id){
  var root=EffectDetails.data(e);var out=new ArrayList<String>();JsonObject dp=record(root,"protection"),oath=record(root,"oath"),hot=record(root,"hot");
  if(id.equals(DP)&&dp!=null){out.add("Damage reduction: "+EffectDetails.fmt(n(dp,"reduction")*100)+"%");out.add("Attack blocks remaining: "+EffectDetails.fmt(Math.clamp(dp.has("blocks")?n(dp,"blocks"):n(dp,"flat"),0,3)));if(banner(e)>n(dp,"reduction"))out.add("A stronger Paladin protection is active.");if(flag(dp,"merciful"))out.add(flag(dp,"used")?"Recovery already triggered.":"Next hostile hit starts Merciful Recovery.");}
  if(id.equals(KR)&&dp!=null)out.add("Knockback resistance: +"+EffectDetails.fmt(n(dp,"knockback")*100)+"%");
  if(id.equals("skill_tree_rpgs:vengeance")&&e instanceof ServerPlayer player)out.add("Armor and Healing Power: +"+EffectDetails.fmt(net.skill_tree_rpgs.compat.PaladinPassives.bonus(player)*100)+"%");
  if(id.equals(POWER))out.add("Healing Power: +10%");
  if(id.equals(OATH)&&oath!=null){out.add("Bonus damage: +"+EffectDetails.fmt(n(oath,"damage"))+" per hit (0.5s interval).");out.add("Armor: +"+EffectDetails.fmt(n(oath,"armor")));out.add("Absorption granted: "+EffectDetails.fmt(n(oath,"capacity"))+" HP");out.add("Total absorption remaining: "+EffectDetails.fmt(e.getAbsorptionAmount())+" HP");}
  if(id.equals(HOT)&&hot!=null){out.add("Recovery: "+EffectDetails.fmt(SupportScaling.receivedHealing(e,n(hot,"amount")))+" HP each second.");out.add("Pulses remaining: "+EffectDetails.fmt(5-n(hot,"pulses")));}
  if(id.equals(GUARD))out.add("Immune to movement-impairing effects.");if(id.equals(READY))out.add("The next hostile hit marks and burns its attacker.");
  return out;
 }
}
