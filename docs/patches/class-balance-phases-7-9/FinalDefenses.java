package net.spell_engine.internals;
import java.util.*;import com.google.gson.JsonObject;import net.minecraft.world.entity.*;import net.minecraft.world.damagesource.*;import net.minecraft.tags.DamageTypeTags;import net.minecraft.world.effect.MobEffectInstance;import net.spell_engine.api.effect.Protection;
public final class FinalDefenses {
 public static final ThreadLocal<Boolean> PASSIVE=ThreadLocal.withInitial(()->false);
 public static final ThreadLocal<Boolean> PERIODIC=ThreadLocal.withInitial(()->false);
 static final ThreadLocal<Boolean> DECREMENT=ThreadLocal.withInitial(()->false);
 public static void init(){net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.entity.living.MobEffectEvent.Remove e)->{if(!DECREMENT.get()&&(e.getEffect().is(net.minecraft.resources.Identifier.parse("skill_tree_rpgs:deflection"))||e.getEffect().is(net.minecraft.resources.Identifier.parse("paladins:divine_protection"))))WINDOWS.remove(e.getEntity());});}
 static final Map<LivingEntity,Window> WINDOWS=new WeakHashMap<>();record Window(long until,long parentEnd,String parent,boolean finalCharge){}
 public static boolean qualifying(LivingEntity victim,DamageSource source){
  if(PERIODIC.get()||source==null||!(source.getEntity() instanceof LivingEntity attacker)||attacker==victim||!PartyRules.harmfulAllowed(attacker,victim)||source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)||source.is(DamageTypes.FELL_OUT_OF_WORLD)||source.is(DamageTypes.GENERIC_KILL)||source.is(DamageTypes.ON_FIRE)||source.is(DamageTypes.IN_FIRE)||source.is(DamageTypes.LAVA)||source.is(DamageTypes.WITHER)||source.is(DamageTypes.FREEZE)||source.is(DamageTypes.DROWN)||source.is(DamageTypes.FALL)||source.is(DamageTypes.STARVE))return false;
  String id=source.typeHolder().unwrapKey().orElseThrow().identifier().toString();return !id.contains("bleed")&&!id.contains("frostbite")&&!id.contains("poison")&&!id.contains("purgatory")&&!id.contains("sinful");
 }
 public static JsonObject protection(LivingEntity e){var all=EffectDetails.data(e);if(!all.has("support")||!all.getAsJsonObject("support").has("protection"))return null;return all.getAsJsonObject("support").getAsJsonObject("protection");}
 public static boolean block(LivingEntity e,DamageSource source){
  if(e.level().isClientSide()||!e.isAlive()||!qualifying(e,source)||Protection.isHitAlreadyBlocked(e,(net.minecraft.server.level.ServerLevel)e.level(),source))return false;
  long now=e.level().getGameTime();var window=WINDOWS.get(e);
  if(!FinalRules.owns(e,"archer_tier_3_passive_2")){if(e.hasEffect(FinalRules.effect("deflection")))e.removeEffect(FinalRules.effect("deflection"));if(window!=null&&window.parent.equals("skill_tree_rpgs:deflection")){WINDOWS.remove(e);window=null;}}
  if(window!=null&&now<window.until&&now<window.parentEnd&&(e.hasEffect(FinalRules.effect(window.parent))||window.finalCharge&&FinalRules.owns(e,"archer_tier_3_passive_2")))return true;
  var dp=protection(e);if(dp!=null&&e.hasEffect(FinalRules.effect("paladins:divine_protection"))&&dp.get("expires").getAsLong()>now){int left=dp.has("blocks")?dp.get("blocks").getAsInt():dp.has("flat")?dp.get("flat").getAsInt():0;if(left>0){var all=EffectDetails.data(e);var state=all.getAsJsonObject("support").getAsJsonObject("protection");state.addProperty("blocks",left-1);state.remove("flat");EffectDetails.save(e,all);EffectDetails.update(e);WINDOWS.put(e,new Window(now+5,dp.get("expires").getAsLong(),"paladins:divine_protection",false));return true;}}
  var fx=e.getEffect(FinalRules.effect("deflection"));if(fx==null)return false;int left=fx.getAmplifier();long end=now+fx.getDuration();try{DECREMENT.set(true);e.removeEffect(FinalRules.effect("deflection"));if(left>0)e.addEffect(new MobEffectInstance(FinalRules.effect("deflection"),fx.getDuration(),left-1),e);}finally{DECREMENT.set(false);}WINDOWS.put(e,new Window(now+5,end,"skill_tree_rpgs:deflection",left==0));FinalRules.status(e,"deflection_speed",40,0,e);return true;
 }
 public static void incoming(net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent e){if(!e.isCanceled()&&e.getAmount()>0&&block(e.getEntity(),e.getSource()))e.setCanceled(true);}
}
