package net.spell_engine.internals;

import java.util.*;
import com.google.gson.*;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.spell_engine.api.effect.*;
import net.spell_engine.api.spell.*;
import net.spell_engine.api.spell.event.SpellHandlers;
import net.spell_engine.internals.casting.*;
import net.spell_engine.internals.impact.SpellImpacts;
import net.spell_engine.entity.*;
import net.spell_engine.rpg_series.config.ConfigFile;
import net.spell_engine.rpg_series.config.EffectConfig;
import net.spell_power.api.*;

/** Per-cast owners are transient; recipient amounts have independent saved expiry leases. */
public final class PaladinUltimates {
 public static final String BANNER="paladins:battle_banner",PURGATORY="paladins:immolation",STANCE="skill_tree_rpgs:purgatory",IMMUNITY="skill_tree_rpgs:paladin_sanctity",WARD="skill_tree_rpgs:purgatory_ward",ARDENT="skill_tree_rpgs:ardent_defender",REINFORCED="skill_tree_rpgs:reinforced";
 public static final String ARDENT_SKILL="skill_tree_rpgs:paladin_tier_3_passive_1",REINFORCED_SKILL="skill_tree_rpgs:paladin_tier_3_passive_2";
 public static Banner bannerOf(LivingEntity e){return BANNERS.get(e);}public static Channel channelOf(LivingEntity e){return CHANNELS.get(e);}
 static final Map<LivingEntity,Banner> BANNERS=new WeakHashMap<>();static final Map<LivingEntity,Channel> CHANNELS=new WeakHashMap<>();static boolean installed;static final ThreadLocal<Boolean> COST=ThreadLocal.withInitial(()->false);
 static Identifier id(String s){return Identifier.parse(s);}static Holder<MobEffect> effect(String s){return PaladinRevamp.effect(s);}static long now(LivingEntity e){return e.level().getGameTime();}
 static boolean node(LivingEntity e,String tail){return PaladinRevamp.node(e,"tier_4_"+tail);}static boolean owns(LivingEntity e,String s){return PaladinRevamp.passive(e,s);}
 static double power(LivingEntity e,SpellSchool s){return SpellPower.getSpellPower(s,e).nonCriticalValue();}
 static double p(LivingEntity e){return power(e,SpellSchools.HEALING);}static double m(LivingEntity e){return power(e,ExternalSpellSchools.PHYSICAL_MELEE);}
 static JsonObject data(LivingEntity e){return EffectDetails.data(e);}static JsonObject object(JsonObject o,String k){return EffectDetails.object(o,k);}static double n(JsonObject o,String k){return EffectDetails.number(o,k);}
 static void save(LivingEntity e,JsonObject r){EffectDetails.save(e,r);}static Holder<Attribute> attr(String s){return BuiltInRegistries.ATTRIBUTE.get(id(s)).orElseThrow();}
 static void status(LivingEntity e,String s,int ticks,int level,LivingEntity caster){e.addEffect(new MobEffectInstance(effect(s),ticks,level),caster);if(s.equals(STANCE))((EntityActionsAllowed.ControlledEntity)e).updateEntityActionsAllowed();}
 static void modifier(LivingEntity e,Holder<Attribute> a,String key,double value,AttributeModifier.Operation operation){var x=e.getAttribute(a);if(x==null)return;var identifier=id("paladin_pack:"+key);var old=x.getModifier(identifier);if(old!=null&&Math.abs(old.amount()-value)<1e-8&&old.operation()==operation)return;if(old!=null)x.removeModifier(identifier);if(Math.abs(value)>1e-8)x.addTransientModifier(new AttributeModifier(identifier,value,operation));}
 static void heal(LivingEntity caster,LivingEntity target,double amount){var h=PaladinRevamp.spell(caster,"paladins:paladin_restore");var c=new SpellExecution.ImpactContext().channeled(1).power(new SpellPower.Result(SpellSchools.HEALING,amount,0,1));SpellImpacts.performImpacts(caster.level(),caster,target,target,h,h.value().impacts,c,false,null);}
 public static final class Banner {
  public final UUID token=UUID.randomUUID();public final LivingEntity caster;public final long start;public final int duration,radius;public final double p,m,armor;public final boolean intense;public int pulses;public long next;public SpellCloud model;public final Set<UUID> rescued=new HashSet<>();
  Banner(LivingEntity e){caster=e;start=now(e);next=start+10;duration=node(e,"spell_1_modifier_1")?500:400;radius=node(e,"spell_1_root")?8:6;p=PaladinRevamp.pSnapshot(e);m=SupportScaling.melee(e);armor=PaladinRevamp.armorSnapshot(e);intense=node(e,"spell_1_modifier_2");}
 }
 public static final class Channel {
  public final long start;public final double p,m;public final int radius,outer;public final boolean damnation,resolute;public Vec3 previous;public int pulses;public boolean ended;
  Channel(LivingEntity e,long start){this.start=start;p=p(e);m=m(e);radius=node(e,"spell_2_root")?7:6;outer=node(e,"spell_2_root")?14:12;damnation=node(e,"spell_2_modifier_1");resolute=node(e,"spell_2_modifier_2");previous=e.position();}
 }
 public enum End { RELEASE,COMPLETE,DEATH,RESOLUTE,CLEANUP }
 public static int stage(int pulse){return pulse>=40?2:pulse>=20?1:0;}
 public static double pulseCoefficient(int pulse,boolean damnation){return .1+.1*stage(pulse)+(damnation?.05:0);}
 public static double explosionCoefficient(double seconds,boolean damnation){return Math.clamp(2+(seconds-20)*(damnation?.7:.5),2,damnation?9:7);}
 public static int protectionTicks(double p){return Math.min(14,4+2*(int)Math.floor(Math.max(0,p)/10));}
 public static float pulseOffset(float original,Holder<Spell> spell){return spell.is(id(PURGATORY))?0:original;}
 public static boolean rollPermitted(boolean original,Player player){return original&&!player.hasEffect(effect(STANCE));}
 public static int reinforcedBand(float health,float maximum){if(health<=0||maximum<=0)return 0;double f=health/maximum;return f<.4?4:f<.5?3:f<.6?2:f<.7?1:0;}
 static boolean hardControl(LivingEntity e){if(PaladinFollowup.focused(e))return false;return e.getActiveEffects().stream().anyMatch(f->{if(f.getEffect().is(id("spell_engine:stun"))||f.getEffect().is(id("wizards:frozen")))return true;var a=((ActionImpairing)f.getEffect().value()).actionsAllowed();return a!=null&&!a.players().canCastSpell()&&!f.getEffect().is(id(STANCE));});}
 public static void configure(ConfigFile.Effects config){
  for(String[] row:new String[][]{{STANCE,"Purgatory"},{IMMUNITY,"Sanctity"},{WARD,"Purgatory Aegis"},{REINFORCED,"Reinforced"}})WarriorTierTwo.entry(row[0],row[1],List.of(),config);
  config.effects.put(ARDENT,new EffectConfig(List.of(PriestRules.attr("minecraft:armor",10,AttributeModifier.Operation.ADD_VALUE),PriestRules.attr("spell_engine:damage_taken",-.05f,AttributeModifier.Operation.ADD_MULTIPLIED_BASE))));
  config.effects.put(BANNER,new EffectConfig(List.of()));config.effects.put("skill_tree_rpgs:banner_protection",new EffectConfig(List.of()));
  OnRemoval.configure(net.skill_tree_rpgs.effect.SkillEffects.entries.stream().filter(e->e.id.toString().equals(WARD)).findFirst().orElseThrow().effect,c->clearWard(c.entity()));
  var stance=net.skill_tree_rpgs.effect.SkillEffects.entries.stream().filter(e->e.id.toString().equals(STANCE)).findFirst().orElseThrow().effect;
  OnRemoval.configure(stance,c->end(c.entity(),End.CLEANUP));
  GlowingItemStatusEffect.register(stance,new net.spell_engine.client.util.Color(1,.32f,.03f),.5f);
  ActionImpairing.configure(stance,new EntityActionsAllowed(false,false,new EntityActionsAllowed.PlayersAllowed(false,false,true),new EntityActionsAllowed.MobsAllowed(true),EntityActionsAllowed.SemanticType.NONE));
 }
 public static void configurePaladin(ConfigFile.Effects config){config.effects.put(BANNER,new EffectConfig(List.of()));}
 public static void init(){if(installed)return;installed=true;
  SpellHandlers.registerCustomDelivery(id("paladin_pack:banner"),(world,h,caster,targets,c,pos)->{if(world.isClientSide())return false;startBanner(caster);return true;});
  SpellHandlers.registerCustomDelivery(id("paladin_pack:purgatory"),(world,h,caster,targets,c,pos)->!world.isClientSide());
  NeoForge.EVENT_BUS.addListener((EntityTickEvent.Pre event)->{if(event.getEntity() instanceof LivingEntity e&&!e.level().isClientSide())tick(e);});
  NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,(LivingDamageEvent.Pre event)->damage(event));
  NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST,(LivingIncomingDamageEvent event)->{if(!COST.get()&&ordinary(event.getSource())&&event.getEntity().hasEffect(effect(IMMUNITY)))event.setCanceled(true);});
  NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,(LivingDamageEvent.Post event)->{if(event.getInflictedDamage()>0&&event.getEntity().isAlive())low(event.getEntity());});
  NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,(LivingDeathEvent event)->{if(!event.isCanceled()){end(event.getEntity(),End.DEATH);cleanup(event.getEntity());}});
  NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event)->cleanup(event.getEntity()));
  NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerChangedDimensionEvent event)->cleanup(event.getEntity()));
 }
 public static void startBanner(LivingEntity e){stopBanner(e);Banner b=new Banner(e);BANNERS.put(e,b);PaladinVisuals.bannerModel(b);PaladinVisuals.ring(e,b.radius,0xFFE58AFFL);}
 static void stopBanner(LivingEntity e){var b=BANNERS.remove(e);if(b!=null&&b.model!=null)b.model.discard();var r=data(e);if(r.has("banner_carrier")){r.remove("banner_carrier");save(e,r);}}
 public static void bannerPulse(Banner b){b.pulses++;
  for(var ally:PriestRules.nearby(b.caster,b.caster.position(),b.radius))if(ally.isAlive()&&PartyRules.allies(b.caster,ally)){
   var r=data(ally);var sources=object(r,"banner_sources");String token=b.token.toString();var s=sources.has(token)?sources.getAsJsonObject(token):new JsonObject();int stacks=(int)n(s,"stacks")+1;s.addProperty("stacks",stacks);s.addProperty("expires",now(ally)+60);s.addProperty("owner",b.caster.getUUID().toString());
   double cap=b.intense?.05:0;
   s.addProperty("attack",Math.min(.2+cap,stacks*(.002+.0004*b.m)));s.addProperty("draw",Math.min(.2+cap,stacks*(.004+.0003*b.m)));s.addProperty("haste",Math.min(.1+cap,stacks*(.001+.00025*b.p)));
   s.addProperty("reduction",Math.min(.1+cap,stacks*(.001+.0002*b.armor)));s.addProperty("magic",n(s,"reduction"));s.addProperty("knockback",Math.min(.3+cap,stacks*(.004+.00055*b.m)));s.addProperty("healing",Math.min(2,1+.05*b.p));sources.add(token,s);save(ally,r);status(ally,BANNER,60,0,b.caster);applyBanner(ally);if(b.pulses%2==0)healBanner(ally);if(ally.getHealth()<ally.getMaxHealth()*.3)rescue(ally);EffectDetails.update(ally);
  }
  PaladinVisuals.ring(b.caster,b.radius,0xFFE58AFFL);
 }
 static void healBanner(LivingEntity e){var root=data(e);if(now(e)<n(root,"banner_heal_next"))return;var best=BANNERS.values().stream().filter(b->b.caster.isAlive()&&b.caster.level()==e.level()&&now(e)<=b.start+b.duration&&b.caster.distanceToSqr(e)<=b.radius*b.radius&&PartyRules.allies(b.caster,e)).max(java.util.Comparator.comparingDouble(b->b.p)).orElse(null);if(best==null)return;root.addProperty("banner_heal_next",now(e)+20);save(e,root);heal(best.caster,e,Math.min(2,1+.05*best.p));}
 public static JsonObject bannerValues(LivingEntity e){var r=data(e);var sources=object(r,"banner_sources");var values=new JsonObject();boolean dirty=false;
  for(String key:List.copyOf(sources.keySet())){var s=sources.getAsJsonObject(key);if(!e.isAlive()||now(e)>=n(s,"expires")||!e.hasEffect(effect(BANNER))){sources.remove(key);dirty=true;continue;}for(String stat:List.of("attack","draw","haste","reduction","magic","knockback","healing"))values.addProperty(stat,Math.max(n(values,stat),n(s,stat)));}
  if(dirty)save(e,r);return values;
 }
 public static void applyBanner(LivingEntity e){var v=bannerValues(e);modifier(e,Attributes.ATTACK_SPEED,"banner_attack",n(v,"attack"),AttributeModifier.Operation.ADD_MULTIPLIED_BASE);modifier(e,attr("ranged_weapon:haste"),"banner_draw",n(v,"draw"),AttributeModifier.Operation.ADD_MULTIPLIED_BASE);modifier(e,SpellPowerMechanics.HASTE.attributeEntry,"banner_haste",n(v,"haste"),AttributeModifier.Operation.ADD_MULTIPLIED_BASE);modifier(e,Attributes.KNOCKBACK_RESISTANCE,"banner_knockback",n(v,"knockback"),AttributeModifier.Operation.ADD_VALUE);}
 public static boolean rescue(LivingEntity target){if(!target.isAlive())return false;
  for(var b:List.copyOf(BANNERS.values()))if(b.caster.isAlive()&&b.caster.level()==target.level()&&now(target)<b.start+b.duration&&b.caster.distanceToSqr(target)<=b.radius*b.radius&&PartyRules.allies(b.caster,target)&&b.rescued.add(target.getUUID())){status(target,IMMUNITY,40,0,b.caster);PaladinVisuals.rescue(target);return true;}return false;
 }
 public static void process(Player player,SpellCast.Process old,SpellCast.Process next){
  WarriorUltimates.changeProcess(player,old,next);if(player.level().isClientSide())return;
  if(next!=null&&next.id().toString().equals(PURGATORY)&&(old==null||old.startedAt()!=next.startedAt()))startChannel(player,next.startedAt());
  if(old!=null&&old.id().toString().equals(PURGATORY)&&next==null){var c=CHANNELS.get(player);if(c!=null){advance(player,now(player));end(player,now(player)-c.start>=600?End.COMPLETE:End.RELEASE);}}
 }
 public static boolean interrupt(Player player,SpellCast.Process process){if(process!=null&&process.id().toString().equals(PURGATORY)&&!player.level().isClientSide()){
  if(hardControl(player)){end(player,End.RELEASE);return true;}
 }return WarriorUltimates.interrupt(player,process);}
 public static void startChannel(LivingEntity e,long start){end(e,End.CLEANUP);CHANNELS.put(e,new Channel(e,start));status(e,STANCE,602,0,e);}
 public static void advance(LivingEntity e,long time){var c=CHANNELS.get(e);if(c==null||c.ended)return;
  if(!e.hasEffect(effect(STANCE))){end(e,End.CLEANUP);return;}PaladinFollowup.cleanseFocused(e);if(hardControl(e)){end(e,End.RELEASE);return;}
  if(c.resolute&&c.pulses>=40&&e.getHealth()<e.getMaxHealth()*.05){end(e,End.RESOLUTE);return;}
  int due=(int)Math.min(60,Math.max(0,(time-c.start)/10));while(c.pulses<due&&!c.ended&&e.isAlive()){c.pulses++;pulse(e,c,c.pulses);}
  if(!c.ended&&c.pulses==60)end(e,End.COMPLETE);
 }
 public static void pulse(LivingEntity e,Channel c,int number){int stage=stage(number);double coefficient=pulseCoefficient(number,c.damnation);var h=PaladinRevamp.spell(e,"paladins:purgatory_pulse");var context=new SpellExecution.ImpactContext().channeled(1).power(new SpellPower.Result(SpellSchools.HEALING,(.75*c.p+.25*c.m)*coefficient,0,1));
  for(var enemy:PriestRules.nearby(e,e.position(),c.outer))if(PartyRules.harmfulAllowed(e,enemy)){
   if(enemy instanceof Mob mob&&mob.canAttack(e))mob.setTarget(e);Vec3 delta=e.position().subtract(enemy.position());double distance=delta.length();if(distance>.9){double strength=new double[]{.08,.18,.32}[stage]*(1-enemy.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));Vec3 velocity=enemy.getDeltaMovement().add(delta.normalize().scale(strength));if(velocity.length()>.65)velocity=velocity.normalize().scale(.65);enemy.setDeltaMovement(velocity);enemy.needsSync=true;if(enemy instanceof ServerPlayer p)p.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(enemy));}
   if(e.distanceToSqr(enemy)<=c.radius*c.radius)SpellImpacts.performImpacts(e.level(),e,enemy,e,h,h.value().impacts,context,false,null);
  }
  if(stage>0)for(var ally:PriestRules.nearby(e,e.position(),c.radius))if(ally!=e&&PartyRules.allies(e,ally))ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION,80,stage==1?1:2),e);
  if(stage==2)status(e,IMMUNITY,protectionTicks(c.p),0,e);PaladinVisuals.channelPulse(e,c,stage);
  if(stage>0)cost(e,c,stage==1?2:3);
 }
 public static void cost(LivingEntity e,Channel c,float amount){if(c.ended)return;float absorbed=Math.min(e.getAbsorptionAmount(),amount);e.setAbsorptionAmount(e.getAbsorptionAmount()-absorbed);amount-=absorbed;if(amount<=0)return;boolean fatal=e.getHealth()<=amount;if(c.resolute&&c.pulses>=40&&(fatal||e.getHealth()-amount<e.getMaxHealth()*.05)){if(!fatal)e.setHealth(e.getHealth()-amount);end(e,End.RESOLUTE);return;}
  if(e instanceof ServerPlayer player&&(fatal||(e.getHealth()>e.getMaxHealth()*.01&&e.getHealth()<e.getMaxHealth()*.3)))tryArdent(player);
  COST.set(true);try{if(e.getHealth()<=amount){e.setHealth(0);end(e,End.DEATH);e.die(e.damageSources().genericKill());}else e.setHealth(e.getHealth()-amount);}finally{COST.set(false);}
 }
 public static void end(LivingEntity e,End reason){var c=CHANNELS.remove(e);if(c==null||c.ended)return;c.ended=true;e.removeEffect(effect(STANCE));((EntityActionsAllowed.ControlledEntity)e).updateEntityActionsAllowed();
  double seconds=Math.clamp((now(e)-c.start)/20d,0,30);if(reason!=End.CLEANUP&&c.pulses>=40){boolean full=reason==End.COMPLETE||reason==End.DEATH;double coefficient=full?(c.damnation?9:7):explosionCoefficient(seconds,c.damnation);burst(e,c,coefficient,full||reason==End.RESOLUTE,full);}
  if(reason==End.RESOLUTE)status(e,IMMUNITY,60,0,e);
  if(e instanceof Player player){if(reason!=End.CLEANUP)((SpellCaster.Player)player).getCooldownManager().set(PaladinRevamp.spell(e,PURGATORY),Math.round(SpellParameters.getCooldownDuration(player,PaladinRevamp.spell(e,PURGATORY))*20));if(((SpellCaster.Player)player).getSpellCastProcess()!=null&&((SpellCaster.Player)player).getSpellCastProcess().id().toString().equals(PURGATORY))((SpellCaster.Player)player).getInteractor().requestClear();}
 }
 public static void burst(LivingEntity e,Channel c,double coefficient,boolean shield,boolean stun){var h=PaladinRevamp.spell(e,"paladins:purgatory_burst");var context=new SpellExecution.ImpactContext().channeled(1).power(new SpellPower.Result(ExternalSpellSchools.PHYSICAL_MELEE,c.m*coefficient,0,1));
  for(var target:PriestRules.nearby(e,e.position(),c.outer)){if(PartyRules.harmfulAllowed(e,target)){SpellImpacts.performImpacts(e.level(),e,target,e,h,h.value().impacts,context,false,null);if(stun)PaladinRevamp.addControl(target,new MobEffectInstance(effect("spell_engine:stun"),20),e);}else if(shield&&target!=e)grantWard(e,target,Math.min(20,10+Math.floor(c.p/2)));}
  PaladinVisuals.explosion(e,c.outer,coefficient);
 }
 public static void grantWard(LivingEntity caster,LivingEntity target,double capacity){var r=data(target);var ward=object(r,"purgatory_ward");double delta=Math.max(0,capacity-n(ward,"base")),grant=delta*(1+PriestPassives.absorptionBonus(target));ward.addProperty("base",Math.max(capacity,n(ward,"base")));ward.addProperty("capacity",n(ward,"capacity")+grant);ward.addProperty("expires",now(target)+160);save(target,r);modifier(target,Attributes.MAX_ABSORPTION,"purgatory_absorption",n(ward,"capacity"),AttributeModifier.Operation.ADD_VALUE);target.setAbsorptionAmount(target.getAbsorptionAmount()+(float)grant);status(target,WARD,160,0,caster);if(grant>0)PaladinVisuals.shield(target);}
 public static void clearWard(LivingEntity e){var r=data(e);r.remove("purgatory_ward");save(e,r);modifier(e,Attributes.MAX_ABSORPTION,"purgatory_absorption",0,AttributeModifier.Operation.ADD_VALUE);e.setAbsorptionAmount(Math.min(e.getAbsorptionAmount(),e.getMaxAbsorption()));}
 static boolean ordinary(DamageSource source){return !source.is(DamageTypes.FELL_OUT_OF_WORLD)&&!source.is(DamageTypes.GENERIC_KILL);}
 static boolean magic(DamageSource source){String namespace=source.typeHolder().unwrapKey().orElseThrow().identifier().getNamespace();return namespace.equals("spell_power")||source.is(net.minecraft.tags.TagKey.create(Registries.DAMAGE_TYPE,id("c:is_magic")))||source.is(DamageTypes.WITHER)||source.is(DamageTypes.DRAGON_BREATH)||source.is(net.minecraft.tags.DamageTypeTags.WITCH_RESISTANT_TO)||source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE);}
 static boolean resolute(LivingEntity target){var c=CHANNELS.get(target);if(c!=null&&c.pulses>=40&&c.resolute){end(target,End.RESOLUTE);return true;}return false;}
 public static void damage(LivingDamageEvent.Pre event){var e=event.getEntity();if(!ordinary(event.getSource())||COST.get()||event.getNewDamage()<=0)return;
  var v=bannerValues(e);double protection=SupportScaling.activeReduction(e);double factor=(1-Math.max(protection,n(v,"reduction")))/Math.max(.001,1-protection);if(magic(event.getSource()))factor*=1-n(v,"magic");event.setNewDamage((float)(event.getNewDamage()*factor));float remaining=Math.max(0,event.getNewDamage()-e.getAbsorptionAmount());boolean fatal=remaining>=e.getHealth();
  if(fatal&&resolute(e)){event.setNewDamage(0);return;}if(e.hasEffect(effect(IMMUNITY))){event.setNewDamage(0);return;}
  if(fatal&&rescue(e)){event.setNewDamage(0);return;}if(e instanceof ServerPlayer player&&(fatal||(e.getHealth()>e.getMaxHealth()*.01&&e.getHealth()<e.getMaxHealth()*.3)))tryArdent(player);
 }
 static void low(LivingEntity target){var c=CHANNELS.get(target);if(c!=null&&c.resolute&&c.pulses>=40&&target.getHealth()<target.getMaxHealth()*.05){end(target,End.RESOLUTE);return;}if(target instanceof ServerPlayer p&&target.getHealth()>target.getMaxHealth()*.01&&target.getHealth()<target.getMaxHealth()*.3)tryArdent(p);if(target.getHealth()<target.getMaxHealth()*.3)rescue(target);}
 public static boolean tryArdent(ServerPlayer target){var candidates=new ArrayList<ServerPlayer>();candidates.add(target);for(var p:target.level().players())if(p!=target)candidates.add(p);
  for(var caster:candidates)if(caster.isAlive()&&owns(caster,ARDENT_SKILL)&&caster.distanceToSqr(target)<=36&&PartyRules.allies(caster,target)){
   var h=PaladinRevamp.spell(caster,ARDENT_SKILL);var cd=((SpellCaster.Player)caster).getCooldownManager();if(cd.isCoolingDown(h))continue;cd.set(h,Math.round(SpellParameters.getCooldownDuration(caster,h)*20));status(target,ARDENT,200,0,caster);target.addEffect(new MobEffectInstance(MobEffects.RESISTANCE,200,0),caster);heal(caster,target,target.getMaxHealth()*.4);PaladinVisuals.defender(target);return true;
  }return false;
 }
 public static void reinforced(ServerPlayer e){int band=e.isAlive()&&owns(e,REINFORCED_SKILL)?reinforcedBand(e.getHealth(),e.getMaxHealth()):0;modifier(e,Attributes.ARMOR,"reinforced_armor",band*.05,AttributeModifier.Operation.ADD_MULTIPLIED_BASE);modifier(e,SpellSchools.HEALING.attributeEntry,"reinforced_power",band*.05,AttributeModifier.Operation.ADD_MULTIPLIED_BASE);var old=e.getEffect(effect(REINFORCED));if(band==0)e.removeEffect(effect(REINFORCED));else{if(old!=null&&old.getAmplifier()!=band-1)e.removeEffect(effect(REINFORCED));status(e,REINFORCED,10,band-1,e);}}
 public static void tick(LivingEntity e){if(e instanceof ServerPlayer p&&p.tickCount%5==0){reinforced(p);PaladinVisuals.statusSignals(p);}
  if(e.hasData(EffectDetails.DATA)){applyBanner(e);var r=data(e);if(r.has("purgatory_ward")){var ward=r.getAsJsonObject("purgatory_ward");if(!e.hasEffect(effect(WARD))||now(e)>=n(ward,"expires"))clearWard(e);else modifier(e,Attributes.MAX_ABSORPTION,"purgatory_absorption",n(ward,"capacity"),AttributeModifier.Operation.ADD_VALUE);}}
  var b=BANNERS.get(e);if(b!=null){if(!e.isAlive()||e.isRemoved()||(e instanceof ServerPlayer p&&!net.skill_tree_rpgs.compat.PaladinDoctrine.isPaladin(p))||now(e)>b.start+b.duration)stopBanner(e);else{PaladinVisuals.moveBanner(b);if(now(e)>=b.next){b.next+=10;bannerPulse(b);}if(e.tickCount%5==0)for(var ally:PriestRules.nearby(e,e.position(),b.radius))if(PartyRules.allies(e,ally)&&ally.getHealth()<ally.getMaxHealth()*.3)rescue(ally);}}
  var c=CHANNELS.get(e);if(c!=null){if(!e.isAlive()){end(e,End.DEATH);return;}if(e.isRemoved()||e.position().distanceToSqr(c.previous)>64||(e instanceof ServerPlayer p&&!net.skill_tree_rpgs.compat.PaladinDoctrine.isPaladin(p))){end(e,End.CLEANUP);return;}c.previous=e.position();advance(e,now(e));}
 }
 public static void cleanup(LivingEntity e){stopBanner(e);end(e,End.CLEANUP);var r=data(e);r.remove("banner_sources");save(e,r);e.removeEffect(effect(BANNER));e.removeEffect(effect(STANCE));e.removeEffect(effect(IMMUNITY));e.removeEffect(effect(REINFORCED));applyBanner(e);modifier(e,Attributes.ARMOR,"reinforced_armor",0,AttributeModifier.Operation.ADD_MULTIPLIED_BASE);modifier(e,SpellSchools.HEALING.attributeEntry,"reinforced_power",0,AttributeModifier.Operation.ADD_MULTIPLIED_BASE);clearWard(e);}
 public static List<String> details(LivingEntity e,String key){var out=new ArrayList<String>();if(key.equals(BANNER)){var v=bannerValues(e);for(String[] row:new String[][]{{"attack","Attack speed"},{"draw","Draw speed"},{"haste","Spell haste"},{"reduction","Damage reduction"},{"magic","Additional magic reduction"},{"knockback","Knockback resistance"}})out.add(row[1]+": "+EffectDetails.fmt(n(v,row[0])*100)+"%");out.add("Healing in the aura: "+EffectDetails.fmt(SupportScaling.receivedHealing(e,n(v,"healing")))+" HP each second.");boolean available=BANNERS.values().stream().anyMatch(b->b.caster.level()==e.level()&&b.caster.distanceToSqr(e)<=b.radius*b.radius&&PartyRules.allies(b.caster,e)&&!b.rescued.contains(e.getUUID()));out.add(available?"Banner rescue available in the aura.":"No Banner rescue currently available.");}
  if(key.equals(STANCE)){if(PaladinFollowup.focused(e))out.add("Channel cannot be interrupted by control effects.");var c=CHANNELS.get(e);if(c!=null){out.add("Stage: "+(stage(c.pulses)+1));out.add("Pulse damage: "+EffectDetails.fmt(pulseCoefficient(c.pulses,c.damnation)*100)+"% holy hybrid");out.add("HP cost per pulse: "+(stage(c.pulses)==0?0:stage(c.pulses)==1?2:3));}}
  if(key.equals(IMMUNITY))out.add("Immune to ordinary damage; direct skill HP costs still apply.");if(key.equals(WARD))out.add("Total absorption remaining: "+EffectDetails.fmt(e.getAbsorptionAmount())+" HP");if(key.equals(REINFORCED)){var f=e.getEffect(effect(REINFORCED));if(f!=null)out.add("Healing Power and base armor: +"+5*(f.getAmplifier()+1)+"%");}return out;
 }
}
