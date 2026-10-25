package io.redspace.pvp_flagging;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.common.util.FakePlayer;

/** Player-persisted data: no entity maps, executor jobs or separate save files. */
public final class CombatRules {
 public static final String KEY="pvp_flagging:combat";
 public static CombatState state(Player p){
  var t=p.getPersistentData().getCompoundOrEmpty(KEY);
  return new CombatState(t.getLongOr("until",0),t.getLongOr("break",0),t.getLongOr("place",0),t.getIntOr("broken",0),t.getIntOr("placed",0));
 }
 public static void save(Player p,CombatState s){
  var t=p.getPersistentData().getCompoundOrEmpty(KEY);t.putLong("until",s.until);t.putLong("break",s.breakUntil);t.putLong("place",s.placeUntil);t.putInt("broken",s.broken);t.putInt("placed",s.placed);p.getPersistentData().put(KEY,t);
 }
 public static boolean flagged(Player p){return !p.level().isClientSide()&&state(p).active(System.currentTimeMillis());}
 public static void flag(ServerPlayer p){
  long now=System.currentTimeMillis();var s=state(p);boolean fresh=s.flag(now);save(p,s);
  if(fresh)p.sendSystemMessage(Component.literal("PvP combat: building and mining limits apply for 40s."));
  sync(p);
 }
 public static boolean blockAdvanced(Player p){
  if(!flagged(p))return false;
  warn(p,"Combat: building tools and OneKeyMiner are disabled.");return true;
 }
 public static void warn(Player p,String message){
  long now=System.currentTimeMillis();var t=p.getPersistentData().getCompoundOrEmpty(KEY);
  if(now<t.getLongOr("warning",0))return;
  t.putLong("warning",now+1000);p.getPersistentData().put(KEY,t);p.sendOverlayMessage(Component.literal(message));
 }
 /** Start the appropriate window on an attempt, charge only after native success. */
 public static boolean attempt(Player p,boolean place,int count){
  long now=System.currentTimeMillis();var s=state(p);if(!s.active(now))return true;
  boolean starting=s.window(place,now)==0;s.start(place,now);save(p,s);if(starting&&p instanceof ServerPlayer sp)sync(sp);
  if(s.allowed(place,count,now))return true;
  warn(p,(place?"Placement":"Breaking")+" limit reached. Wait "+((s.window(place,now)+999)/1000)+"s.");return false;
 }
 public static void completed(Player p,boolean place,int count){
  long now=System.currentTimeMillis();var s=state(p);if(!s.active(now))return;
  s.start(place,now);s.consume(place,count);save(p,s);
  if(p instanceof ServerPlayer sp)sync(sp);
 }
 public static void sync(ServerPlayer p){
  long now=System.currentTimeMillis();var s=state(p);boolean active=s.active(now);
  if(!active)p.getPersistentData().remove(KEY);
  if(!(p instanceof FakePlayer)&&p.connection!=null&&p.connection.getConnection().channel()!=null&&net.neoforged.neoforge.network.registration.NetworkRegistry.hasChannel(p.connection,CombatStatus.TYPE.id()))PacketDistributor.sendToPlayer(p,new CombatStatus(active?(int)Math.min(40000,s.until-now):0,active?(int)s.window(false,now):0,active?(int)s.window(true,now):0));
 }
 public static void tick(ServerPlayer p){
  if(p.tickCount%20!=0||!p.getPersistentData().contains(KEY))return;sync(p);
 }
 private CombatRules(){}
}
