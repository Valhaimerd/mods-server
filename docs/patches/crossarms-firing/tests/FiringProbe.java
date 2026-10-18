package local.crossarms;
import java.nio.file.*;import java.util.*;
import net.neoforged.fml.common.Mod;import net.neoforged.neoforge.common.NeoForge;import net.neoforged.neoforge.event.server.ServerStartedEvent;import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.minecraft.core.registries.*;import net.minecraft.resources.Identifier;import net.minecraft.world.InteractionHand;import net.minecraft.world.item.*;import net.minecraft.world.item.enchantment.EnchantmentHelper;
@Mod("crossarms_probe") public class FiringProbe {
 int checks;net.minecraft.server.level.ServerPlayer p;net.minecraft.server.level.ServerLevel l;InteractionHand hand;final List<net.minecraft.world.entity.projectile.arrow.AbstractArrow> arrows=new ArrayList<>();
 void check(boolean b,String label){if(!b)throw new AssertionError(label);checks++;System.out.println("FIRING_PASS "+label);}
 void near(double a,double b,String label){check(Math.abs(a-b)<.0001,label+" actual="+a+" expected="+b);}
 void command(String c){l.getServer().getCommands().performPrefixedCommand(l.getServer().createCommandSourceStack().withEntity(p).withPosition(p.position()).withLevel(l),c);}
 void ticks(int n){for(int i=0;i<n;i++)EnchantmentHelper.tickEffects(l,p);}
 ItemStack bow(){return p.getItemInHand(hand);}
 void setup(String item,boolean frenzy){command("scoreboard players set @s frenzy_notice 0");command("scoreboard players set @s frenzy_shots 0");command("scoreboard players set @s frenzy_idle 0");command("scoreboard players set @s frenzy_empty 0");command("scoreboard players reset @s heat");command("scoreboard players reset @s HS_delay");command("scoreboard players reset @s frenzy_hurt");p.removeEffect(net.minecraft.world.effect.MobEffects.SLOWNESS);p.getInventory().clearContent();p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);p.getInventory().setItem(1,new ItemStack(Items.ARROW,64));var b=new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(item)));p.setItemInHand(hand,b);if(frenzy)b.enchant(l.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(Identifier.parse("crossarms:frenzy")).orElseThrow(),1);}
 void fire(){((CrossbowItem)bow().getItem()).use(l,p,hand);}
 void load(){var b=bow();var cb=(CrossbowItem)b.getItem();cb.use(l,p,hand);cb.onUseTick(l,p,b,b.getUseDuration(p)-200);cb.releaseUsing(b,l,p,b.getUseDuration(p)-200);p.stopUsingItem();}
 double shootDamage(){int before=arrows.size();fire();check(arrows.size()==before+1,"one real arrow spawned");return ((net.rpg_foundation.ranged_weapon.internal.ArrowExtension)arrows.getLast()).rwa_getDamage();}
 double impact()throws Exception{
  var arrow=arrows.getLast();arrow.setCritArrow(false);
  // Isolate damage scaling from random spread changing the launch-speed magnitude.
  arrow.setDeltaMovement(arrow.getDeltaMovement().normalize().scale(3.15));
  var target=(net.minecraft.world.entity.LivingEntity)BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse("minecraft:zombie")).create(l,net.minecraft.world.entity.EntitySpawnReason.COMMAND);
  target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR).setBaseValue(0);
  target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(1000);target.setHealth(1000);target.setPos(0,80,5);l.addFreshEntity(target);
  var hit=net.minecraft.world.entity.projectile.arrow.AbstractArrow.class.getDeclaredMethod("onHitEntity",net.minecraft.world.phys.EntityHitResult.class);hit.setAccessible(true);hit.invoke(arrow,new net.minecraft.world.phys.EntityHitResult(target));
  double lost=1000-target.getHealth();target.discard();return lost;
 }
 void spread(boolean frenzy)throws Exception{
  var hook=Arrays.stream(ProjectileWeaponItem.class.getDeclaredMethods()).filter(m->m.getName().contains("tightSpread")).findFirst().orElseThrow();hook.setAccessible(true);
  float actual=(Float)hook.invoke(bow().getItem(),1f,l,p,hand,bow(),List.of(new ItemStack(Items.ARROW)),3f,1f,false,null);
  int heat=score("heat");double multiplier=heat<=10?1.15:heat<=150?1.25:heat<=300?1.4:heat<=450?1.6:heat<=610?1.85:2.2;
  near(actual,frenzy?multiplier:1,"loaded accuracy hook heat="+heat);
 }
 double trajectory(int heat){
  setup("minecraft:crossbow",true);bow().enchant(l.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(Identifier.parse("minecraft:infinity")).orElseThrow(),1);ticks(8);double sumX=0,sumY=0,sumSquared=0;int before=arrows.size();
  for(int i=0;i<96;i++){command("scoreboard players set @s frenzy_shots 0");command("scoreboard players set @s heat "+heat);fire();var v=arrows.getLast().getDeltaMovement();double x=v.x/v.z,y=v.y/v.z;sumX+=x;sumY+=y;sumSquared+=x*x+y*y;ticks(8);}
  check(arrows.size()==before+96,"trajectory sample fired 96 real arrows");return 20*Math.sqrt(sumSquared/96-Math.pow(sumX/96,2)-Math.pow(sumY/96,2));
 }
 int score(String name){var objective=l.getScoreboard().getObjective(name);return objective==null?0:l.getScoreboard().getOrCreatePlayerScore(p,objective).get();}
 void worldTicks(int n){for(int i=0;i<n;i++){command("function crossarms:enchantment/frenzy/burst/tick");if(score("frenzy_notice")>0)command("function crossarms:enchantment/frenzy/notice/tick");}}
 void burst(String item,boolean infinity)throws Exception{
  setup(item,true);if(infinity)bow().enchant(l.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(Identifier.parse("minecraft:infinity")).orElseThrow(),1);ticks(8);
  for(int i=1;i<=26;i++){check(CrossbowItem.isCharged(bow()),"burst shot "+i+" ready");shootDamage();ticks(8);check(score("frenzy_shots")==i,"counts actual firing action "+i);}
  check(!CrossbowItem.isCharged(bow())&&score("frenzy_empty")==1,"26th arrow empties and locks Frenzy");int count=p.getInventory().countItem(Items.ARROW);int wear=bow().getDamageValue();int heat=score("heat");
  check(score("frenzy_notice")==60,"empty notice begins with 60 ticks");worldTicks(59);check(score("frenzy_notice")==1&&score("frenzy_empty")==1,"notice remains through tick 59 without unlocking");worldTicks(1);check(score("frenzy_notice")==0&&score("frenzy_empty")==1,"notice expires at 60 ticks while exhausted stays locked");worldTicks(40);ticks(100);check(!CrossbowItem.isCharged(bow())&&score("frenzy_empty")==1&&score("frenzy_shots")==26,"idle timeout cannot clear exhausted burst");
  check(p.getInventory().countItem(Items.ARROW)==count&&bow().getDamageValue()==wear&&score("heat")==heat,"locked auto-feed changes no ammo wear or heat");
  var other=hand==InteractionHand.MAIN_HAND?InteractionHand.OFF_HAND:InteractionHand.MAIN_HAND;var spare=bow().copy();spare.set(net.minecraft.core.component.DataComponents.CHARGED_PROJECTILES,net.minecraft.world.item.component.ChargedProjectiles.ofNonEmpty(List.of(new ItemStack(Items.ARROW))));p.setItemInHand(other,spare);int spawned=arrows.size();((CrossbowItem)spare.getItem()).use(l,p,other);check(arrows.size()==spawned&&CrossbowItem.isCharged(spare),"preloaded other Frenzy cannot bypass manual reload or lose ammo");p.setItemInHand(other,ItemStack.EMPTY);
  p.setItemInHand(other,new ItemStack(Items.FIREWORK_ROCKET));var blocked=(CrossbowItem)bow().getItem();blocked.use(l,p,hand);blocked.onUseTick(l,p,bow(),bow().getUseDuration(p)-200);p.stopUsingItem();check(!CrossbowItem.isCharged(bow())&&score("frenzy_empty")==1,"manual Frenzy crank accepts normal arrows only");p.setItemInHand(other,ItemStack.EMPTY);
  int duration=CrossbowItem.getChargeDuration(bow(),p);check(duration>0&&duration<200,"normal crank duration restored");var cb=(CrossbowItem)bow().getItem();cb.use(l,p,hand);cb.onUseTick(l,p,bow(),bow().getUseDuration(p)-duration/2);cb.releaseUsing(bow(),l,p,bow().getUseDuration(p)-duration/2);p.stopUsingItem();check(score("frenzy_empty")==1&&!CrossbowItem.isCharged(bow()),"interrupted crank stays locked");
  cb.use(l,p,hand);cb.onUseTick(l,p,bow(),bow().getUseDuration(p)-duration+1);check(!CrossbowItem.isCharged(bow())&&score("frenzy_empty")==1,"reload cannot finish a tick early");cb.onUseTick(l,p,bow(),bow().getUseDuration(p)-duration);p.stopUsingItem();check(CrossbowItem.isCharged(bow())&&score("frenzy_empty")==0&&score("frenzy_shots")==0,"full manual crank clears exhausted burst");check(p.getInventory().countItem(Items.ARROW)==count-(infinity?0:1),"manual reload respects Infinity and consumes one ordinary arrow");shootDamage();ticks(8);check(score("frenzy_shots")==1&&CrossbowItem.isCharged(bow()),"automatic firing resumes after crank");
  worldTicks(59);check(score("frenzy_shots")==1,"partial counter survives 59 idle ticks");worldTicks(1);check(score("frenzy_shots")==0,"partial counter resets at 60 idle ticks");shootDamage();ticks(8);worldTicks(59);shootDamage();ticks(8);worldTicks(1);check(score("frenzy_shots")==2,"another shot restarts partial idle timer");
 }
 void breakingShot(){
  hand=InteractionHand.MAIN_HAND;setup("minecraft:crossbow",true);ticks(8);var spare=bow().copy();spare.setDamageValue(0);bow().setDamageValue(bow().getMaxDamage()-1);command("scoreboard players set @s frenzy_shots 25");int before=arrows.size();fire();check(arrows.size()==before+1&&bow().isEmpty(),"26th shot fires even when the crossbow breaks");check(score("frenzy_shots")==26&&score("frenzy_empty")==1,"breaking 26th shot still exhausts burst");p.setItemInHand(hand,spare);fire();check(arrows.size()==before+1&&CrossbowItem.isCharged(spare),"spare cannot bypass exhaustion after weapon breaks");
 }
 public FiringProbe(){NeoForge.EVENT_BUS.addListener(this::start);}
 void start(ServerStartedEvent e){var s=e.getServer();s.execute(()->{try{
  l=s.overworld();p=FakePlayerFactory.getMinecraft(l);p.setPos(0,80,0);l.getChunk(0,0);command("function crossarms:load");
  check(l.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(Identifier.parse("crossarms:capacity")).isEmpty(),"Capacity absent from enchantment registry");
  NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.entity.EntityJoinLevelEvent event)->{if(event.getEntity() instanceof net.minecraft.world.entity.projectile.arrow.AbstractArrow arrow)arrows.add(arrow);});
  hand=InteractionHand.MAIN_HAND;setup("minecraft:crossbow",true);bow().enchant(l.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(Identifier.parse("minecraft:infinity")).orElseThrow(),1);ticks(8);check(p.getInventory().countItem(Items.ARROW)==64,"Infinity does not consume its first loaded arrow");
  breakingShot();
  for(var testHand:InteractionHand.values()){hand=testHand;for(var item:List.of("minecraft:crossbow","arsenal:unique_heavy_crossbow_1")){for(double damageAttribute:new double[]{0,64}){
   p.getAttribute(net.rpg_foundation.ranged_weapon.api.EntityAttributes_RangedWeapon.DAMAGE.entry).setBaseValue(damageAttribute);
   setup(item,false);spread(false);load();double normal=shootDamage();double normalHit=impact();System.out.println("RAW_HIT "+item+" "+hand+" rangedAttribute="+damageAttribute+" damage="+normalHit+" coefficient="+normal);check(!CrossbowItem.isCharged(bow()),item+" ordinary requires reload");check(!p.hasEffect(net.minecraft.world.effect.MobEffects.SLOWNESS),"ordinary shot does not add Frenzy slowdown");
   int[] heat={0,200,320,480,650};double[] factor={.3,.3,.4,.5,.6};
   for(int i=0;i<heat.length;i++){setup(item,true);spread(true);ticks(8);check(CrossbowItem.isCharged(bow()),item+" Frenzy auto-feeds");command("scoreboard players set @s heat "+heat[i]);near(shootDamage()/normal,factor[i],item+" "+hand+" heat="+heat[i]+" damage ratio");double hit=impact();check(Math.abs(hit-normalHit*factor[i])<=1,item+" first/tapped impact ratio heat="+heat[i]+" actual="+hit+" ordinary="+normalHit);
    var slow=p.getEffect(net.minecraft.world.effect.MobEffects.SLOWNESS);check(slow!=null&&slow.getAmplifier()==1&&slow.getDuration()==20,"first shot immediately slows 30% for 20 ticks");p.removeEffect(net.minecraft.world.effect.MobEffects.SLOWNESS);}
   setup(item,true);ticks(8);for(int i=0;i<6;i++){check(CrossbowItem.isCharged(bow()),"Frenzy repeat ready");shootDamage();ticks(8);}check(p.getInventory().countItem(Items.ARROW)==57,"one arrow consumed per feed");check(bow().getDamageValue()==6,"normal durability wear retained");command("scoreboard players set @s heat 1000");fire();ticks(8);check(bow().getDamageValue()==7,"no forced heat durability reduction");
   setup(item,true);bow().enchant(l.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(Identifier.parse("minecraft:infinity")).orElseThrow(),1);ticks(8);
   for(int i=0;i<6;i++){shootDamage();check(arrows.getLast().pickup==net.minecraft.world.entity.projectile.arrow.AbstractArrow.Pickup.CREATIVE_ONLY,"Infinity arrows cannot be collected");ticks(8);}
   check(p.getInventory().countItem(Items.ARROW)==64,"Infinity preserves arrows after repeated Frenzy shots");
   setup(item,true);bow().enchant(l.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(Identifier.parse("minecraft:infinity")).orElseThrow(),1);p.getInventory().setItem(1,new ItemStack(Items.ARROW));ticks(8);shootDamage();ticks(8);check(p.getInventory().countItem(Items.ARROW)==1&&CrossbowItem.isCharged(bow()),"Infinity works with one normal arrow");
   setup(item,true);bow().enchant(l.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(Identifier.parse("minecraft:infinity")).orElseThrow(),1);p.getInventory().setItem(1,ItemStack.EMPTY);ticks(8);check(!CrossbowItem.isCharged(bow()),"Infinity still requires an arrow in survival");
   setup(item,true);p.setItemInHand(hand==InteractionHand.MAIN_HAND?InteractionHand.OFF_HAND:InteractionHand.MAIN_HAND,new ItemStack(Items.CROSSBOW));p.getItemInHand(hand==InteractionHand.MAIN_HAND?InteractionHand.OFF_HAND:InteractionHand.MAIN_HAND).enchant(l.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(Identifier.parse("minecraft:infinity")).orElseThrow(),1);
   ticks(8);check(p.getInventory().countItem(Items.ARROW)==63,"Infinity on another hand does not grant free ammunition");
  }}}
  for(var testHand:InteractionHand.values()){hand=testHand;for(var item:List.of("minecraft:crossbow","arsenal:unique_heavy_crossbow_1")){for(boolean infinite:new boolean[]{false,true})burst(item,infinite);}}
  hand=InteractionHand.MAIN_HAND;setup("minecraft:crossbow",true);for(int heat:new int[]{-1,0,10,11,150,151,300,301,450,451,610,611}){command("scoreboard players set @s heat "+heat);spread(true);}command("scoreboard players reset @s heat");spread(true);
  double coldSpread=trajectory(0),hotSpread=trajectory(650);System.out.println("TRAJECTORY_RMS_AT_20_BLOCKS cold="+coldSpread+" hot="+hotSpread);check(hotSpread>coldSpread*1.3&&hotSpread<coldSpread*3,"actual hot projectiles spread more than cold projectiles");
  p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.setInvulnerable(false);p.connection.markClientLoaded();p.getAbilities().invulnerable=false;p.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(100);p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE,10000,0));
  for(var sample:Map.of(200,0,400,1,550,2,750,4).entrySet()){
   p.setHealth(100);p.invulnerableTime=0;command("scoreboard players set @s frenzy_hurt 0");command("scoreboard players set @s heat "+sample.getKey());
   for(int i=0;i<19;i++)command("function crossarms:enchantment/frenzy/heat/tick");near(p.getHealth(),100,"no heat damage before 20 ticks heat="+sample.getKey());command("function crossarms:enchantment/frenzy/heat/tick");System.out.println("HEAT_DIAG "+sample+" score="+l.getScoreboard().getOrCreatePlayerScore(p,l.getScoreboard().getObjective("heat")).get()+" timer="+l.getScoreboard().getOrCreatePlayerScore(p,l.getScoreboard().getObjective("frenzy_hurt")).get());near(p.getHealth(),100-sample.getValue(),"magic heat damage at 20 ticks heat="+sample.getKey());
  }
  Files.writeString(Path.of("result.txt"),"PASS "+checks+" current Frenzy checks\n");
 }catch(Throwable t){t.printStackTrace();try{Files.writeString(Path.of("result.txt"),"FAIL "+t);}catch(Exception ignored){}}s.halt(false);});}
}
