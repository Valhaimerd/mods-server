package artifacts.pack;
import java.lang.reflect.Method;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** Shared bounds for native accessory handlers; optional APIs stay optional. */
public final class AccessoryCombat {
 private AccessoryCombat() {}
 private static final TagKey<net.minecraft.world.damagesource.DamageType> PROC=TagKey.create(Registries.DAMAGE_TYPE,Identifier.parse("modpack:accessory_damage"));
 private static final Method PARTY=partyApi();
 private static Method partyApi() {
  try {return Class.forName("dev.betterparty.api.v1.server.BetterPartyServerApi").getMethod("arePartyMembers",MinecraftServer.class,UUID.class,UUID.class);}
  catch(ClassNotFoundException absent){return null;}
  catch(ReflectiveOperationException broken){throw new IllegalStateException("Better Party API changed",broken);}
 }
 private static UUID principal(Entity entity) {
  if(entity instanceof OwnableEntity owned && owned.getOwnerReference()!=null)return owned.getOwnerReference().getUUID();
  return entity.getUUID();
 }
 public static boolean friendly(LivingEntity wearer,Entity target) {
  if(target==null || wearer==target || wearer.isAlliedTo(target))return true;
  UUID first=principal(wearer),second=principal(target);
  if(first.equals(second))return true;
  if(PARTY!=null && wearer.level() instanceof ServerLevel level){
   try{return (boolean)PARTY.invoke(null,level.getServer(),first,second);}
   catch(ReflectiveOperationException broken){throw new IllegalStateException("Better Party lookup failed",broken);}
  }
  return false;
 }
 public static boolean enemy(LivingEntity wearer,Entity target) {
  return target instanceof LivingEntity living && living.isAlive() && !living.isSpectator() && !living.isInvulnerable() && !friendly(wearer,target);
 }
 public static boolean proc(DamageSource source){return source.is(PROC);}
 public static boolean ready(LivingEntity wearer,String effect,int ticks){
  long now=wearer.level().getGameTime();String key="artifacts:proc_"+effect;
  if(wearer.getPersistentData().getLongOr(key,Long.MIN_VALUE)>now)return false;
  wearer.getPersistentData().putLong(key,now+ticks);return true;
 }
 public static double power(LivingEntity wearer){
  double value=Math.max(0,wearer.getAttributeValue(Attributes.ATTACK_DAMAGE));
  for(String id:new String[]{"ranged_weapon:damage","spell_power:arcane","spell_power:fire","spell_power:frost","spell_power:lightning"}){
   var attribute=BuiltInRegistries.ATTRIBUTE.get(Identifier.parse(id));
   if(attribute.isPresent() && wearer.getAttribute(attribute.get())!=null)value=Math.max(value,wearer.getAttributeValue(attribute.get()));
  }
  return Double.isFinite(value)?value:0;
 }
 public static float scaled(LivingEntity wearer,double base,double ratio,double cap){return (float)Math.min(cap,base+ratio*power(wearer));}
 public static void absorb(net.minecraft.world.entity.LivingEntity victim,DamageSource source,float healthDamage){
  var attacker=artifacts.util.DamageSourceHelper.getAttacker(source);
  if(attacker==null || attacker.level().isClientSide() || !attacker.isAlive() || proc(source)
   || !artifacts.util.DamageSourceHelper.isMeleeAttack(source) || friendly(attacker,victim)
   || !(healthDamage>0) || attacker.getHealth()>=attacker.getMaxHealth())return;
  // Native hook supplies actual health lost after absorption and limits overkill.
  float actual=Math.min(healthDamage,victim.getHealth());
  final boolean[] healed={false};
  artifacts.registry.ModDataComponents.DAMAGE_ABSORPTION.on(attacker).iterate((ability,slot)->{
   if(healed[0] || slot.isDisabledOrBroken() || attacker.getRandom().nextDouble()>=ability.absorptionChance().get())return;
   float healing=(float)Math.min(2,Math.min(ability.maxDamageAbsorbed().get(),actual*Math.min(.05,ability.absorptionRatio().get())));
   if(healing>0 && ready(attacker,"vampiric_glove",20)){
    healed[0]=true;attacker.heal(healing);slot.hurtAndBreak(ability.itemDamage().get());
   }
  });
 }
 public static boolean damage(LivingEntity wearer,LivingEntity target,float amount){
  if(!(wearer.level() instanceof ServerLevel level) || !enemy(wearer,target) || !(amount>0))return false;
  var key=ResourceKey.create(Registries.DAMAGE_TYPE,Identifier.parse("artifacts:accessory"));
  var type=level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(key);
  return target.hurtServer(level,new DamageSource(type,wearer),amount);
 }
}
