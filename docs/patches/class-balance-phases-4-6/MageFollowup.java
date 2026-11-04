package net.spell_engine.internals;
import java.util.*;
import com.google.gson.*;
import net.minecraft.core.*;import net.minecraft.core.registries.*;import net.minecraft.resources.Identifier;
import net.minecraft.server.level.*;import net.minecraft.world.entity.*;import net.minecraft.world.entity.ai.attributes.*;import net.minecraft.world.entity.player.Player;import net.minecraft.world.effect.*;import net.minecraft.world.level.*;import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;import net.neoforged.neoforge.event.entity.living.*;import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.spell_engine.api.effect.*;import net.spell_engine.api.spell.*;import net.spell_engine.api.spell.registry.SpellRegistry;import net.spell_engine.api.entity.*;import net.spell_engine.entity.*;import net.spell_engine.internals.casting.*;import net.spell_engine.internals.container.*;import net.spell_engine.internals.impact.*;import net.spell_engine.internals.target.*;import net.spell_engine.rpg_series.config.ConfigFile;import net.spell_engine.rpg_series.config.EffectConfig;import net.spell_power.api.*;
public final class MageFollowup {
 public static final Gson JSON=new Gson();public static final String WINTER="skill_tree_rpgs:winters_chill_ready",COLD="skill_tree_rpgs:frostbite_exposure",WINTER_MARK="magic_pack:winter_chill";
 private static boolean installed;
 private static final Map<LivingEntity,LivingEntity> WARD_ATTACKER=new WeakHashMap<>();
 private static final Map<LivingEntity,net.minecraft.world.damagesource.DamageSource> FIRE_HITS=new WeakHashMap<>();
 private static final Map<LivingEntity,Long> FIRE_CLOCK=new WeakHashMap<>();
 private static final ThreadLocal<Boolean> RETALIATING=ThreadLocal.withInitial(()->false);
 private static final Map<String,Long> WINTER_USED=new HashMap<>();
 private static final Map<Player,Boolean> BLINK_OK=new WeakHashMap<>();
 public static Identifier id(String s){return Identifier.parse(s);}
 public static Holder<MobEffect> effect(String s){return BuiltInRegistries.MOB_EFFECT.get(id(s)).orElseThrow();}
 public static Holder<Spell> spell(LivingEntity e,String s){return SpellRegistry.from(e.level()).get(id(s)).orElseThrow();}
 public static boolean node(LivingEntity e,String id){return e instanceof SpellContainerSource.Owner owner&&owner.getSpellContainers().modifiers().stream().anyMatch(h->h.is(id(id)));}
 public static boolean owns(LivingEntity e,String id){return e instanceof Player p&&SpellContainerSource.passiveSpellsOf(p).stream().anyMatch(h->h.is(id(id)));}
 public static boolean basicFrost(Holder<Spell> h){return h.is(id("wizards:frostbolt"))||h.is(id("wizards:frost_shard"));}
 public static <T> T copy(T v,Class<T> cls){return JSON.fromJson(JSON.toJson(v),cls);}
 public static void configure(ConfigFile.Effects c){
  var entries=net.skill_tree_rpgs.effect.SkillEffects.entries;
  for(var key:List.of(WINTER,COLD))if(entries.stream().noneMatch(e->e.id.equals(id(key)))){var fx=new CustomStatusEffect(key.equals(COLD)?MobEffectCategory.HARMFUL:MobEffectCategory.BENEFICIAL,0x88CCFF);Synchronized.configure(fx,true);entries.add(new Effects.Entry(id(key),key.equals(COLD)?"Frostbite":"Winter's Chill",key.equals(COLD)?"Damaging freezing exposure.":"Next Frost weapon spell freezes for 0.5s.",fx,new EffectConfig(List.of())));c.effects.put(key,new EffectConfig(List.of()));}
  // Retired critical-vulnerability stacks must not keep affecting existing targets.
  ((net.spell_power.api.statuseffects.SpellVulnerabilityStatusEffect)net.skill_tree_rpgs.effect.SkillEffects.FROST_VULNERABILITY.effect).setVulnerability(SpellSchools.FROST,new SpellPower.Vulnerability(0,0,0));
  if(installed)return;installed=true;FrostExposure.init();net.spell_engine.internals.cost.BlinkCharges.init();
  net.spell_engine.api.spell.event.SpellHandlers.registerCustomDelivery(id("spell_engine:targeted_frost_spike"),TargetedSpike::deliver);
  NeoForge.EVENT_BUS.addListener(MageFollowup::beforeDamage);NeoForge.EVENT_BUS.addListener(MageFollowup::afterDamage);
 }
 public static void wizardConfig(ConfigFile.Effects c){c.effects.put("wizards:arcane_evocation",new EffectConfig(List.of(new net.spell_engine.rpg_series.config.AttributeModifier("spell_power:critical_chance",.05f,AttributeModifier.Operation.ADD_MULTIPLIED_BASE),new net.spell_engine.rpg_series.config.AttributeModifier("spell_power:haste",.03f,AttributeModifier.Operation.ADD_MULTIPLIED_BASE))));}
 public static float pulseOffset(float original,Holder<Spell> h){return h.is(id("wizards:arcane_evocation"))?0:original;}
 public static boolean marker(SpellExecution.ImpactContext ctx,String expected){return ctx.chargeModifier()!=null&&ctx.chargeModifier().spell_pattern!=null&&Arrays.stream(ctx.chargeModifier().spell_pattern.split("\\|",-1)).anyMatch(x->x.equals(expected)||x.startsWith(expected+":"));}
 public static SpellExecution.ImpactContext snapshot(SpellExecution.ImpactContext ctx,LivingEntity caster,Holder<Spell> h){
  if(!basicFrost(h)||!caster.hasEffect(effect(WINTER)))return ctx;
  caster.removeEffect(effect(WINTER));var m=ctx.chargeModifier()==null?new Spell.Modifier():copy(ctx.chargeModifier(),Spell.Modifier.class);String token=WINTER_MARK+":"+UUID.randomUUID();m.spell_pattern=m.spell_pattern==null?token:m.spell_pattern+"|"+token;WINTER_USED.entrySet().removeIf(e->e.getValue()<caster.level().getGameTime());return ctx.chargeModifier(m);
 }
 public static Spell.Impact impact(Spell.Impact original,LivingEntity caster,Holder<Spell> h,SpellExecution.ImpactContext ctx,Entity target){
  var result=original;
  // Native area delivery rereads registered impacts; choose branch coefficients here.
  if(original.action.type==Spell.Impact.Action.Type.DAMAGE&&h.is(id("wizards:frost_spikes"))){
   result=copy(original,Spell.Impact.class);result.action.damage.spell_power_coefficient=(node(caster,"skill_tree_rpgs:frost_tier_2_spell_2_modifier_1")?1f:.8f)*(marker(ctx,"magic_pack:reflex_spell")?1.2f:1f);
  }
  if(original.action.type==Spell.Impact.Action.Type.DAMAGE&&h.is(id("wizards:burst_blink_explosion"))&&node(caster,"skill_tree_rpgs:arcane_tier_4_spell_1_modifier_2")){
   result=copy(original,Spell.Impact.class);result.action.damage.spell_power_coefficient=.2f;
  }
  if(h.is(id("wizards:arcane_blink"))&&original.action.type==Spell.Impact.Action.Type.TELEPORT&&node(caster,"skill_tree_rpgs:arcane_tier_4_spell_1_modifier_2")){
   result=copy(original,Spell.Impact.class);float extra=0;for(var m:SpellModifiers.of(caster,h,ctx.chargeModifier()))extra+=m.teleport_distance_add;result.action.teleport.forward.distance=7.5f-extra;
  }
  if(original.action.type==Spell.Impact.Action.Type.STATUS_EFFECT&&"wizards:frozen".equals(original.action.status_effect.effect_id)&&h.value().school==SpellSchools.FROST){
   result=copy(result,Spell.Impact.class);var fx=result.action.status_effect;fx.amplifier=0;fx.amplifier_cap=0;fx.apply_mode=Spell.Impact.Action.StatusEffect.ApplyMode.SET;
   if(h.is(id("wizards:frost_spikes")))fx.duration=1;
  }return result;
 }
 public static boolean after(boolean ok,LivingEntity caster,Entity target,Holder<Spell> h,Spell.Impact impact){
  if(!ok||caster.level().isClientSide())return ok;
  if(h.is(id("skill_tree_rpgs:arcane_tier_3_passive_2"))&&impact.action.type==Spell.Impact.Action.Type.STATUS_EFFECT){
   var enemy=WARD_ATTACKER.remove(caster);if(enemy!=null&&caster.hasEffect(effect("skill_tree_rpgs:arcane_ward"))&&caster.getAbsorptionAmount()>0&&enemy.isAlive()&&enemy.level()==caster.level()&&PartyRules.harmfulAllowed(enemy,caster)){
    Vec3 away=caster.position().subtract(enemy.position()).multiply(1,0,1);if(away.lengthSqr()<.0001)away=caster.getLookAngle().multiply(-1,0,-1);Vec3 destination=caster.position().add(away.normalize().scale(4));if(safe(caster,destination)){clearChannel(caster);caster.teleportTo(destination.x,destination.y,destination.z);}
   }
  }
  if(!(target instanceof LivingEntity victim)||!PartyRules.harmfulAllowed(caster,target))return ok;
  if(h.is(id("skill_tree_rpgs:fire_tier_3_passive_1"))&&impact.action.type==Spell.Impact.Action.Type.DAMAGE)victim.push(0,1.0*(1-victim.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)),0);
  if(impact.action.type==Spell.Impact.Action.Type.DAMAGE){
   if(h.is(id("wizards:fire_breath"))){Vec3 look=caster.getLookAngle().multiply(1,0,1).normalize();double forward=Math.max(0,victim.position().subtract(caster.position()).dot(look));Vec3 point=caster.position().add(look.scale(forward));pull(victim,point,.35);}
   if(frostSource(caster,h))FrostExposure.apply(victim,caster,100);
   if(h.is(id("wizards:frost_blizzard")))FrostExposure.blizzard(victim,caster);
  }
  if(impact.action.type==Spell.Impact.Action.Type.STATUS_EFFECT&&"wizards:frozen".equals(impact.action.status_effect.effect_id)&&(frostSource(caster,h)||h.is(id("skill_tree_rpgs:frost_tier_1_passive_2"))))FrostExposure.apply(victim,caster,200);
  return ok;
 }
 public static boolean afterContext(boolean ok,LivingEntity caster,Entity target,Holder<Spell> h,Spell.Impact impact,SpellExecution.ImpactContext ctx){
  if(ok&&impact.action.type==Spell.Impact.Action.Type.DAMAGE&&basicFrost(h)&&marker(ctx,WINTER_MARK)&&target instanceof LivingEntity victim&&PartyRules.harmfulAllowed(caster,target)){
   // A cast's native direct projectile can freeze once; ricochets do not duplicate the charge.
   String key=Arrays.stream(ctx.chargeModifier().spell_pattern.split("\\|",-1)).filter(x->x.startsWith(WINTER_MARK+":")).findFirst().orElse(WINTER_MARK);
   if(!WINTER_USED.containsKey(key)){WINTER_USED.put(key,caster.level().getGameTime()+600);PartyRules.addEffect(victim,new MobEffectInstance(effect("wizards:frozen"),10),caster);FrostExposure.apply(victim,caster,200);}
  }return ok;
 }
 public static boolean frostSource(LivingEntity caster,Holder<Spell> h){return h.value().school==SpellSchools.FROST&&(caster instanceof Player||caster instanceof SummonedEntity s&&s.getOwner() instanceof Player)&&h.unwrapKey().orElseThrow().identifier().getNamespace().equals("wizards");}
 public static void pull(LivingEntity victim,Vec3 center,double strength){Vec3 toward=center.subtract(victim.position()).multiply(1,0,1);if(toward.lengthSqr()<.01)return;Vec3 force=toward.normalize().scale(strength*(1-victim.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)));victim.push(force.x,0,force.z);victim.needsSync=true;if(victim instanceof ServerPlayer p)p.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(victim));}
 public static boolean safe(LivingEntity e,Vec3 destination){Vec3 delta=destination.subtract(e.position());return e.level().getWorldBorder().isWithinBounds(net.minecraft.core.BlockPos.containing(destination))&&e.level().clip(new ClipContext(e.position().add(0,.8,0),destination.add(0,.8,0),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,e)).getType()==HitResult.Type.MISS&&e.level().noCollision(e,e.getBoundingBox().move(delta))&&!e.level().getBlockState(net.minecraft.core.BlockPos.containing(destination.add(0,-.1,0))).getCollisionShape(e.level(),net.minecraft.core.BlockPos.containing(destination.add(0,-.1,0))).isEmpty()&&PurgatoryBoundary.clip(e,delta).distanceToSqr(delta)<.000001;}
 public static void clearChannel(LivingEntity e){if(e instanceof SpellCaster.Player player&&player.getSpellCastProcess()!=null)player.getInteractor().requestClear();}
 public static boolean impaired(LivingEntity e,EntityActionsAllowed.Player action,boolean show,Holder<Spell> h){if(action==EntityActionsAllowed.Player.CAST_SPELL&&h.is(id("wizards:arcane_blink"))&&active(e))return false;return PaladinRevamp.impaired(e,action,show,h);}
 public static boolean instant(boolean old,Holder<Spell> h,LivingEntity caster){return old||h.is(id("wizards:frost_spikes"))&&node(caster,"skill_tree_rpgs:frost_tier_2_spell_2_modifier_1");}
 public static boolean active(LivingEntity e){if(!e.isAlive()||e.isSleeping()||e.isSpectator())return false;if(e instanceof ServerPlayer p){if(net.neoforged.fml.ModList.get().isLoaded("playerrevive")&&team.creative.playerrevive.server.PlayerReviveServer.isBleeding(p))return false;if(net.neoforged.fml.ModList.get().isLoaded("better_revive")&&de.mrstilu2k6.better_revive.util.DownedPlayerManager.isDowned(p.getUUID()))return false;}return true;}
 public static boolean blink(Level world,Holder<Spell> h,LivingEntity caster,List<SpellExecution.DeliveryTarget> targets,SpellExecution.ImpactContext ctx,Vec3 ignored){
  if(caster instanceof Player p)BLINK_OK.remove(p);
  if(world.isClientSide()||!active(caster))return false;Vec3 before=caster.position();boolean ok=SpellImpacts.performImpacts(world,caster,caster,caster,h,h.value().impacts,ctx,false,Spell.Impact.Action.Type.TELEPORT);Vec3 after=caster.position();if(!ok||before.distanceToSqr(after)<.001)return false;
  if(node(caster,"skill_tree_rpgs:arcane_tier_4_spell_1_modifier_2")){
   Vec3 exact=before.add(caster.getLookAngle().normalize().scale(7.5));
   // Native destination search advances in whole blocks. Finish the fractional step
   // only when its full route, supported landing and existing boundaries remain legal.
   if(safe(caster,exact)){caster.teleportTo(exact.x,exact.y,exact.z);after=caster.position();}
  }
  Vec3 landing=after;PaladinRevamp.cleanse(caster);if(caster instanceof Player p)BLINK_OK.put(p,true);burst(world,caster,before,ctx);((net.spell_engine.utils.WorldScheduler)world).schedule(5,()->{if(caster.isAlive()&&caster.level()==world)burst(world,caster,landing,ctx);});return true;
 }
 public static boolean consumeBlink(Player p){return Boolean.TRUE.equals(BLINK_OK.remove(p));}
 public static void burst(Level world,LivingEntity caster,Vec3 where,SpellExecution.ImpactContext ctx){var h=spell(caster,"wizards:burst_blink_explosion");var area=copy(h.value().area_impact,Spell.AreaImpact.class);var impacts=h.value().impacts;if(node(caster,"skill_tree_rpgs:arcane_tier_4_spell_1_modifier_2")){area.radius=2.5f;impacts=new ArrayList<>(impacts);for(int i=0;i<impacts.size();i++){var x=copy(impacts.get(i),Spell.Impact.class);if(x.action.type==Spell.Impact.Action.Type.DAMAGE)x.action.damage.spell_power_coefficient=.2f;impacts.set(i,x);}}
  SpellImpacts.lookupAndPerformAreaImpact(area,h,caster,caster,null,impacts,ctx.position(where.add(0,.9,0)),false);
 }
 public static boolean wardAllowed(boolean ok,Holder<Spell> h,SpellTriggers.Event event){
  if(h.is(id("skill_tree_rpgs:fire_tier_3_passive_1")))return ok&&event.damageSource!=null&&event.damageSource.getEntity() instanceof LivingEntity enemy&&enemy!=event.player&&PartyRules.harmfulAllowed(event.player,enemy);
  if(ok&&h.is(id("skill_tree_rpgs:arcane_tier_3_passive_2"))&&event.damageSource!=null&&event.damageSource.getEntity() instanceof LivingEntity enemy)WARD_ATTACKER.put(event.player,enemy);return ok;
 }
 private static void beforeDamage(LivingDamageEvent.Pre e){if(!RETALIATING.get()&&e.getNewDamage()>0&&e.getEntity().hasEffect(effect("skill_tree_rpgs:fire_ward"))&&e.getEntity().getAbsorptionAmount()>0)FIRE_HITS.put(e.getEntity(),e.getSource());}
 private static void afterDamage(LivingDamageEvent.Post e){var owner=e.getEntity();var source=FIRE_HITS.remove(owner);if(RETALIATING.get()||source!=e.getSource()||!(source.getEntity() instanceof LivingEntity enemy)||!PartyRules.harmfulAllowed(owner,enemy)||e.getInflictedDamage()+e.getReduction(net.neoforged.neoforge.common.damagesource.DamageContainer.Reduction.ABSORPTION)<=0)return;long now=owner.level().getGameTime();if(now<FIRE_CLOCK.getOrDefault(owner,Long.MIN_VALUE))return;FIRE_CLOCK.put(owner,now+10);try{RETALIATING.set(true);var h=spell(owner,"skill_tree_rpgs:fire_tier_3_passive_2");SpellImpacts.performImpacts(owner.level(),owner,enemy,enemy,h,h.value().impacts,new SpellExecution.ImpactContext().power(SpellPower.getSpellPower(SpellSchools.FIRE,owner)),false,null);}finally{RETALIATING.set(false);}}
}
