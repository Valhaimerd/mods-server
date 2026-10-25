package com.eiden.iourusraces.race;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
/** Named, removable modifiers feed both native/Better Combat attacks and RPG power. */
@EventBusSubscriber(modid="iourusraces")
public final class RaceBalanceRules {
 static final Identifier SPEED=Identifier.parse("iourusraces:orc_attack_speed"),MELEE=Identifier.parse("iourusraces:orc_melee"),RANGED=Identifier.parse("iourusraces:orc_ranged"),DRAW=Identifier.parse("iourusraces:orc_draw_speed"),ARMOR=Identifier.parse("iourusraces:dwarf_armor");
 static final String MIGRATION="iourusraces:orc_attributes_v2",SAVED_BASE="iourusraces:legacy_orc_attack_base";
 static AttributeInstance ranged(Player p){return BuiltInRegistries.ATTRIBUTE.get(Identifier.parse("ranged_weapon:damage")).map(p::getAttribute).orElse(null);}
 static AttributeInstance draw(Player p){return BuiltInRegistries.ATTRIBUTE.get(Identifier.parse("ranged_weapon:haste")).map(p::getAttribute).orElse(null);}
 static void set(AttributeInstance a,Identifier id,double value,AttributeModifier.Operation op){
  if(a==null)return;var old=a.getModifier(id);
  if(old!=null&&old.amount()==value&&old.operation()==op)return;
  if(old!=null)a.removeModifier(id);
  if(value!=0)a.addTransientModifier(new AttributeModifier(id,value,op));
 }
 public static int band(double ratio){
  if(ratio<=.2+.000001)return 4;if(ratio<=.3+.000001)return 3;if(ratio<=.4+.000001)return 2;return 1;
 }
 public static void migrate(Player p,Race race){
  if(!(p instanceof ServerPlayer)||p.getPersistentData().getBooleanOr(MIGRATION,false))return;
  // Only an existing Orc can carry this handler's old 4..10 raw-base writes.
  var a=p.getAttribute(Attributes.ATTACK_DAMAGE);double value=a==null?1:a.getBaseValue();
  if(race==Race.ORC&&value>=4&&value<=10){p.getPersistentData().putDouble(SAVED_BASE,value);a.setBaseValue(1);}
  p.getPersistentData().putBoolean(MIGRATION,true);
 }
 public static void apply(Player p,Race race){
  migrate(p,race);
  set(p.getAttribute(Attributes.ATTACK_SPEED),SPEED,race==Race.ORC?-.1:0,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
  set(p.getAttribute(Attributes.ARMOR),ARMOR,race==Race.DWARF?2:0,AttributeModifier.Operation.ADD_VALUE);
  set(draw(p),DRAW,race==Race.ORC?-.1:0,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
  update(p,race);
 }
 public static void update(Player p,Race race){
  int n=race==Race.ORC?band((double)p.getHealth()/p.getMaxHealth()):0;
  set(p.getAttribute(Attributes.ATTACK_DAMAGE),MELEE,n==0?0:n+1,AttributeModifier.Operation.ADD_VALUE);
  set(ranged(p),RANGED,n==0?0:(n+1)*.5,AttributeModifier.Operation.ADD_VALUE);
 }
 public static void remove(Player p){
  set(draw(p),DRAW,0,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
  set(p.getAttribute(Attributes.ATTACK_SPEED),SPEED,0,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
  set(p.getAttribute(Attributes.ARMOR),ARMOR,0,AttributeModifier.Operation.ADD_VALUE);
  set(p.getAttribute(Attributes.ATTACK_DAMAGE),MELEE,0,AttributeModifier.Operation.ADD_VALUE);
  set(ranged(p),RANGED,0,AttributeModifier.Operation.ADD_VALUE);
 }
 public static void slowFall(ServerPlayer p){
  if(p.onGround()||p.isFallFlying()||p.getAbilities().flying||p.isInWater()||p.isInLava()||p.onClimbable())return;
  var motion=p.getDeltaMovement();
  if(p.fallDistance>=2.5 && motion.y<-.2){p.setDeltaMovement(motion.x,-.2,motion.z);p.fallDistance=2.5;p.hurtMarked=true;}
 }
 @SubscribeEvent public static void tick(PlayerTickEvent.Post event){
  if(event.getEntity() instanceof ServerPlayer p){Race race=PlayerRaceData.getRace(p);apply(p,race);}
 }
 @SubscribeEvent public static void clone(net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone event){
  var old=event.getOriginal().getPersistentData();var next=event.getEntity().getPersistentData();
  if(old.getBooleanOr(MIGRATION,false))next.putBoolean(MIGRATION,true);
  if(old.contains(SAVED_BASE))next.putDouble(SAVED_BASE,old.getDoubleOr(SAVED_BASE,1));
 }
}
