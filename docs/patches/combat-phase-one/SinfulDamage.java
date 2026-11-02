package net.spell_engine.internals;
import com.google.gson.JsonObject;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.core.registries.Registries;

/** Finite, attributed HP drain. No ordinary on-hit event can recursively generate another proc. */
public final class SinfulDamage {
 private static final String KEY="sinful_damage";
 public static final String EFFECT="skill_tree_rpgs:sinful_suffering";
 public static void registerEffects(java.util.List<net.spell_engine.api.effect.Effects.Entry> entries){
  entries.add(new net.spell_engine.api.effect.Effects.Entry(net.minecraft.resources.Identifier.parse(EFFECT),"Sinful Suffering","Loses health to holy retaliation.",new net.spell_engine.api.effect.CustomStatusEffect(net.minecraft.world.effect.MobEffectCategory.HARMFUL,0xC89248)));
 }
 private static net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect(){return net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.get(net.minecraft.resources.Identifier.parse(EFFECT)).orElseThrow();}
 public static void start(LivingEntity target,float ignoredBurnSeconds,LivingEntity caster){
  if(target.level().isClientSide()||!PartyRules.harmfulAllowed(caster,target))return;
  var root=EffectDetails.data(target);var r=new JsonObject();r.addProperty("caster",caster.getUUID().toString());r.addProperty("left",8);r.addProperty("next",target.level().getGameTime()+10);root.add(KEY,r);EffectDetails.save(target,root);PartyRules.addEffect(target,new net.minecraft.world.effect.MobEffectInstance(effect(),80),caster);EffectDetails.update(target);
 }
 public static void tick(LivingEntity target){
  if(!(target.level() instanceof ServerLevel world)||!target.hasData(EffectDetails.DATA))return;
  var root=EffectDetails.data(target);if(!root.has(KEY))return;var r=root.getAsJsonObject(KEY);
  if(!target.isAlive()||!target.hasEffect(effect())){root.remove(KEY);EffectDetails.save(target,root);return;}
  if(world.getGameTime()<r.get("next").getAsLong())return;
  var entity=world.getEntity(UUID.fromString(r.get("caster").getAsString()));
  if(entity==null)entity=world.getServer().getPlayerList().getPlayer(UUID.fromString(r.get("caster").getAsString()));
  if(!(entity instanceof LivingEntity caster)||!caster.isAlive()||!PartyRules.harmfulAllowed(caster,target)){root.remove(KEY);EffectDetails.save(target,root);return;}
  int left=r.get("left").getAsInt()-1;r.addProperty("left",left);r.addProperty("next",world.getGameTime()+10);
  if(left==0){root.remove(KEY);target.removeEffect(effect());}EffectDetails.save(target,root);
  pulse(caster,target);
  world.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,target.getX(),target.getY()+target.getBbHeight()*.6,target.getZ(),2,.15,.15,.15,.015);
 }
 public static java.util.List<String> details(java.util.List<String> out,LivingEntity target,String id){
  if(id.equals(EFFECT)){var root=EffectDetails.data(target);var r=root.has(KEY)?root.getAsJsonObject(KEY):null;LivingEntity caster=null;if(r!=null&&target.level() instanceof ServerLevel world){var entity=world.getEntity(UUID.fromString(r.get("caster").getAsString()));if(entity instanceof LivingEntity living)caster=living;else caster=world.getServer().getPlayerList().getPlayer(UUID.fromString(r.get("caster").getAsString()));}out.add("Damage: "+EffectDetails.fmt(caster==null?1:retention(caster,target))+" HP every 0.5s; absorption is consumed first.");}
  return out;
 }
 public static void pulse(LivingEntity caster,LivingEntity target){
  if(!(target.level() instanceof ServerLevel world)||!target.isAlive()||target.isSpectator()||target.isInvulnerable()||(target instanceof ServerPlayer player&&player.getAbilities().instabuild)||!PartyRules.harmfulAllowed(caster,target))return;
  var type=world.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypes.MAGIC);
  var damageSource=new DamageSource(type,caster,caster);
  if(target.isInvulnerableTo(world,damageSource))return;
  ServerPlayer protectedPlayer=target instanceof ServerPlayer p?p:target instanceof net.minecraft.world.entity.OwnableEntity own&&own.getRootOwner() instanceof ServerPlayer p?p:null;
  if(caster instanceof ServerPlayer owner&&protectedPlayer!=null&&(owner==protectedPlayer||!owner.canHarmPlayer(protectedPlayer)))return;
  // Exactly the pack's player-source retention, once; defenses and criticals do not alter true damage.
  float amount=retention(caster,target);
  float absorbed=Math.min(amount,target.getAbsorptionAmount());target.setAbsorptionAmount(target.getAbsorptionAmount()-absorbed);amount-=absorbed;
  if(amount<=0)return;
  target.setLastHurtByMob(caster);
  if(target.getHealth()>amount){target.setHealth(target.getHealth()-amount);return;}
  try{if((boolean)DeathProtection.CHECK.invoke(target,damageSource))return;}catch(ReflectiveOperationException failure){throw new IllegalStateException("Native death protection unavailable",failure);}
  target.setHealth(0);target.die(damageSource);
 }
 private static float retention(LivingEntity caster,LivingEntity target){
  if(!(caster instanceof ServerPlayer p)||!net.neoforged.fml.ModList.get().isLoaded("pvp_flagging"))return 1;
  try{var policy=Policy.GET.invoke(null,p.level().getServer());return ((Number)Policy.PERCENT.invoke(policy,target instanceof ServerPlayer)).floatValue()/100f;}
  catch(ReflectiveOperationException failure){throw new IllegalStateException("Player damage policy unavailable",failure);}
 }
 private static final class Policy {
  static final java.lang.reflect.Method GET,PERCENT;
  static{try{var c=Class.forName("io.redspace.pvp_flagging.DamageCommands");GET=c.getMethod("get",net.minecraft.server.MinecraftServer.class);PERCENT=c.getMethod("percent",boolean.class);}catch(ReflectiveOperationException e){throw new ExceptionInInitializerError(e);}}
 }
 private static final class DeathProtection {
  static final java.lang.reflect.Method CHECK;
  static{try{CHECK=LivingEntity.class.getDeclaredMethod("checkTotemDeathProtection",DamageSource.class);CHECK.setAccessible(true);}catch(ReflectiveOperationException e){throw new ExceptionInInitializerError(e);}}
 }
}
