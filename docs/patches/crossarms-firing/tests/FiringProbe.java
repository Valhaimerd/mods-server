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
 void setup(String item,boolean frenzy){command("scoreboard players reset @s heat");command("scoreboard players reset @s HS_delay");command("scoreboard players reset @s frenzy_hurt");p.removeEffect(net.minecraft.world.effect.MobEffects.SLOWNESS);p.getInventory().clearContent();p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);p.getInventory().setItem(1,new ItemStack(Items.ARROW,64));var b=new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(item)));p.setItemInHand(hand,b);if(frenzy)b.enchant(l.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(Identifier.parse("crossarms:frenzy")).orElseThrow(),1);}
 void fire(){((CrossbowItem)bow().getItem()).use(l,p,hand);}
 void load(){var b=bow();var cb=(CrossbowItem)b.getItem();cb.use(l,p,hand);cb.onUseTick(l,p,b,b.getUseDuration(p)-200);cb.releaseUsing(b,l,p,b.getUseDuration(p)-200);p.stopUsingItem();}
 double shootDamage(){int before=arrows.size();fire();check(arrows.size()==before+1,"one real arrow spawned");return ((net.rpg_foundation.ranged_weapon.internal.ArrowExtension)arrows.getLast()).rwa_getDamage();}
 double impact()throws Exception{
  var arrow=arrows.getLast();arrow.setCritArrow(false);
  var target=(net.minecraft.world.entity.LivingEntity)BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse("minecraft:zombie")).create(l,net.minecraft.world.entity.EntitySpawnReason.COMMAND);
  target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR).setBaseValue(0);
  target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(1000);target.setHealth(1000);target.setPos(0,80,5);l.addFreshEntity(target);
  var hit=net.minecraft.world.entity.projectile.arrow.AbstractArrow.class.getDeclaredMethod("onHitEntity",net.minecraft.world.phys.EntityHitResult.class);hit.setAccessible(true);hit.invoke(arrow,new net.minecraft.world.phys.EntityHitResult(target));
  double lost=1000-target.getHealth();target.discard();return lost;
 }
 void spread(boolean frenzy)throws Exception{
  var hook=Arrays.stream(ProjectileWeaponItem.class.getDeclaredMethods()).filter(m->m.getName().contains("tightSpread")).findFirst().orElseThrow();hook.setAccessible(true);
  float actual=(Float)hook.invoke(bow().getItem(),1f,l,p,hand,bow(),List.of(new ItemStack(Items.ARROW)),3f,1f,false,null);
  near(actual,frenzy?.10:1,"loaded accuracy hook "+(frenzy?"10% spread":"ordinary unchanged"));
 }
 public FiringProbe(){NeoForge.EVENT_BUS.addListener(this::start);}
 void start(ServerStartedEvent e){var s=e.getServer();s.execute(()->{try{
  l=s.overworld();p=FakePlayerFactory.getMinecraft(l);p.setPos(0,80,0);l.getChunk(0,0);command("function crossarms:load");
  check(l.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(Identifier.parse("crossarms:capacity")).isEmpty(),"Capacity absent from enchantment registry");
  NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.entity.EntityJoinLevelEvent event)->{if(event.getEntity() instanceof net.minecraft.world.entity.projectile.arrow.AbstractArrow arrow)arrows.add(arrow);});
  for(var testHand:InteractionHand.values()){hand=testHand;for(var item:List.of("minecraft:crossbow","arsenal:unique_heavy_crossbow_1")){for(double damageAttribute:new double[]{0,64}){
   p.getAttribute(net.rpg_foundation.ranged_weapon.api.EntityAttributes_RangedWeapon.DAMAGE.entry).setBaseValue(damageAttribute);
   setup(item,false);spread(false);load();double normal=shootDamage();double normalHit=impact();System.out.println("RAW_HIT "+item+" "+hand+" rangedAttribute="+damageAttribute+" damage="+normalHit+" coefficient="+normal);check(!CrossbowItem.isCharged(bow()),item+" ordinary requires reload");check(!p.hasEffect(net.minecraft.world.effect.MobEffects.SLOWNESS),"ordinary shot does not add Frenzy slowdown");
   int[] heat={0,320,480,650};double[] factor={.5,.6,.75,1};
   for(int i=0;i<heat.length;i++){setup(item,true);spread(true);ticks(8);check(CrossbowItem.isCharged(bow()),item+" Frenzy auto-feeds");command("scoreboard players set @s heat "+heat[i]);near(shootDamage()/normal,factor[i],item+" "+hand+" heat="+heat[i]+" damage ratio");double hit=impact();check(Math.abs(hit-normalHit*factor[i])<=1,item+" first/tapped impact ratio heat="+heat[i]+" actual="+hit+" ordinary="+normalHit);
    var slow=p.getEffect(net.minecraft.world.effect.MobEffects.SLOWNESS);check(slow!=null&&slow.getAmplifier()==1&&slow.getDuration()==20,"first shot immediately slows 30% for 20 ticks");p.removeEffect(net.minecraft.world.effect.MobEffects.SLOWNESS);}
   setup(item,true);ticks(8);for(int i=0;i<6;i++){check(CrossbowItem.isCharged(bow()),"Frenzy repeat ready");shootDamage();ticks(8);}check(p.getInventory().countItem(Items.ARROW)==57,"one arrow consumed per feed");check(bow().getDamageValue()==6,"normal durability wear retained");command("scoreboard players set @s heat 1000");fire();ticks(8);check(bow().getDamageValue()==7,"no forced heat durability reduction");
  }}}
  p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.setInvulnerable(false);p.connection.markClientLoaded();p.getAbilities().invulnerable=false;p.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(100);p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE,10000,0));
  for(var sample:Map.of(200,0,400,1,550,2,750,4).entrySet()){
   p.setHealth(100);p.invulnerableTime=0;command("scoreboard players set @s frenzy_hurt 0");command("scoreboard players set @s heat "+sample.getKey());
   for(int i=0;i<39;i++)command("function crossarms:enchantment/frenzy/heat/tick");near(p.getHealth(),100,"no heat damage before 40 ticks heat="+sample.getKey());command("function crossarms:enchantment/frenzy/heat/tick");System.out.println("HEAT_DIAG "+sample+" score="+l.getScoreboard().getOrCreatePlayerScore(p,l.getScoreboard().getObjective("heat")).get()+" timer="+l.getScoreboard().getOrCreatePlayerScore(p,l.getScoreboard().getObjective("frenzy_hurt")).get());near(p.getHealth(),100-sample.getValue(),"magic heat damage at 40 ticks heat="+sample.getKey());
  }
  Files.writeString(Path.of("result.txt"),"PASS "+checks+" current Frenzy checks\n");
 }catch(Throwable t){t.printStackTrace();try{Files.writeString(Path.of("result.txt"),"FAIL "+t);}catch(Exception ignored){}}s.halt(false);});}
}
