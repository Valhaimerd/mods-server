package local.spatialclient;
import java.util.*;import java.nio.file.*;import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.client.*;import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.api.distmarker.Dist;import net.neoforged.fml.common.Mod;import net.neoforged.neoforge.common.NeoForge;import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.valhaimerd.orboblivioncompat.*;import net.puffish.skillsmod.api.*;
@Mod(value="spatial_client_probe",dist=Dist.CLIENT) public class WandTreeProbe {
 int ticks,phase,at;boolean pending,hidden;final List<String> lines=new ArrayList<>();final AtomicReference<Throwable> error=new AtomicReference<>();
 net.puffish.skillsmod.client.gui.SkillsScreen screen;
 public WandTreeProbe(){NeoForge.EVENT_BUS.addListener(this::tick);NeoForge.EVENT_BUS.addListener(this::opening);}
 void opening(net.neoforged.neoforge.client.event.ScreenEvent.Opening e){if(e.getNewScreen() instanceof net.puffish.skillsmod.client.gui.SkillsScreen s)screen=s;}
 void check(boolean ok,String label){lines.add((ok?"PASS ":"FAIL ")+label);if(!ok)throw new AssertionError(label);}
 void server(java.util.function.Consumer<net.minecraft.server.level.ServerPlayer> job){var mc=Minecraft.getInstance();pending=true;mc.getSingleplayerServer().execute(()->{try{job.accept(mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID()));}catch(Throwable e){error.set(e);}finally{pending=false;}});}
 void step(){phase++;at=ticks;}
 void finish(){if(phase==99)return;phase=99;var mc=Minecraft.getInstance();try{Files.write(mc.gameDirectory.toPath().resolve("spatial-client-checks.txt"),lines);Files.writeString(mc.gameDirectory.toPath().resolve("spatial-client-result.txt"),lines.stream().anyMatch(s->s.startsWith("FAIL"))?"FAIL":"PASS");}catch(Exception ignored){}mc.stop();}
 void view(float scale,int x,int y)throws Exception{var field=screen.getClass().getDeclaredField("optActiveCategoryData");field.setAccessible(true);var category=(net.puffish.skillsmod.client.data.ClientCategoryData)((Optional<?>)field.get(screen)).orElseThrow();category.setScale(scale);category.setX(x);category.setY(y);}

 void near(double actual,double expected,String label){check(Math.abs(actual-expected)<.025,label+" actual="+actual+" expected="+expected);}
 double[] stats(net.minecraft.server.level.ServerPlayer p){return new double[]{p.getMaxHealth(),p.getAttributeValue(Attributes.ARMOR),net.puffish.attributesmod.api.DynamicModification.create().withPositive(net.puffish.attributesmod.api.PuffishAttributes.RESISTANCE,p).relativeTo(100),net.puffish.attributesmod.api.DynamicModification.create().withPositive(net.puffish.attributesmod.api.PuffishAttributes.HEALING,p).relativeTo(100),p.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)*100};}
 void audit(net.minecraft.server.level.ServerPlayer p){
  p.removeAllEffects();p.getInventory().clearContent();p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.setInvulnerable(false);p.getAbilities().invulnerable=false;
  p.getPersistentData().putBoolean("skill_overhaul_v1",true);p.getPersistentData().putBoolean("utilities_v1_migrated",true);p.getPersistentData().putBoolean("defense_v2_refunded",true);
  var c=SkillsAPI.getCategory(Utilities.CATEGORY).orElseThrow();c.unlock(p);c.resetSkills(p);c.setExtraPoints(p,1000);
  var w=p.level();p.teleportTo(0,101,0);p.setYRot(0);p.setXRot(0);
  var healing=net.spell_power.api.SpellSchools.HEALING;
  var h=net.spell_engine.internals.PaladinRevamp.spell(p,"paladins:heal");var staff=net.spell_engine.internals.PaladinRevamp.spell(p,"paladins:holy_shock");
  check(h.value().range==24,"Wand retains 24-block targeting");
  for(var id:List.of("acolyte_wand","holy_wand","diamond_holy_wand","netherite_holy_wand")){
   var item=net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.Identifier.parse("paladins:"+id)).orElseThrow();
   p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(item));
   net.spell_engine.internals.container.SpellContainerSource.setDirty(p,net.spell_engine.internals.container.SpellContainerSource.MAIN_HAND);net.spell_engine.internals.container.SpellContainerSource.update(p);
   var container=net.spell_engine.api.spell.container.SpellContainerHelper.containerFromItemStack(p.getMainHandItem());
   check(container!=null&&container.contains(net.minecraft.resources.Identifier.parse("paladins:heal")),"Actual item spell binding "+id+" container="+container);
   check(net.spell_engine.internals.container.SpellContainerSource.activeSpellsOf(p).contains(h),"Actual weapon right-click binding "+id);
  }
  var enemy=net.minecraft.world.entity.EntityTypes.CREEPER.create(w,net.minecraft.world.entity.EntitySpawnReason.COMMAND);enemy.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);enemy.setHealth(1000);enemy.setNoAi(true);enemy.setNoGravity(true);enemy.setPos(0,101,6);w.addFreshEntity(enemy);
  var targets=net.spell_engine.internals.target.SpellTarget.findTargets(p,h,net.spell_engine.internals.target.SpellTarget.SearchResult.empty(),false);
  check(targets.entities().contains(enemy),"Native Wand AIM acquires enemy");
  var ctx=new net.spell_engine.internals.SpellExecution.ImpactContext(1,1,p.position(),new net.spell_power.api.SpellPower.Result(healing,20,0,1.5),net.spell_engine.internals.target.SpellTarget.FocusMode.DIRECT,0);
  net.spell_engine.internals.impact.SpellImpacts.performImpacts(w,p,enemy,p,h,h.value().impacts,ctx,false,null);
  float damage=1000-enemy.getHealth();check(damage>0,"Wand actually damages enemy");
  enemy.invulnerableTime=0;enemy.setHealth(1000);
  net.spell_engine.internals.impact.SpellImpacts.performImpacts(w,p,enemy,p,staff,staff.value().impacts,ctx,false,null);
  near(damage,1000-enemy.getHealth(),"Wand 80% P matches Staff under same mitigation");enemy.discard();
  p.setHealth(1);net.spell_engine.internals.impact.SpellImpacts.performImpacts(w,p,p,p,h,h.value().impacts,ctx,false,null);near(p.getHealth(),11,"Wand self-heals 50% P without self-damage");
  var ally=net.minecraft.world.entity.EntityTypes.WOLF.create(w,net.minecraft.world.entity.EntitySpawnReason.COMMAND);ally.setOwnerReference(net.minecraft.world.entity.EntityReference.of(p.getUUID()));ally.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);ally.setHealth(1);ally.setPos(0,101,4);ally.setNoAi(true);w.addFreshEntity(ally);
  net.spell_engine.internals.impact.SpellImpacts.performImpacts(w,p,ally,p,h,h.value().impacts,ctx,false,null);near(ally.getHealth(),11,"Wand heals eligible ally and does not hurt it");ally.discard();
  p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,net.minecraft.world.item.ItemStack.EMPTY);net.spell_engine.internals.container.SpellContainerSource.setDirty(p,net.spell_engine.internals.container.SpellContainerSource.MAIN_HAND);net.spell_engine.internals.container.SpellContainerSource.update(p);
  double[][][] budget={
   {{2,0,0,0,0},{0,1,0,0,0},{0,0,2,0,0},{0,1,0,0,0},{0,0,2,0,0},{2,0,0,0,0},{0,0,0,4,0},{0,0,0,0,0}},
   {{2,0,0,0,0},{0,2,0,0,0},{0,0,2,0,0},{2,0,0,0,0},{0,0,0,4,0},{2,0,0,0,0},{0,0,0,4,0},{0,0,0,0,0}},
   {{2,0,0,0,0},{0,1,0,0,0},{0,0,2,0,0},{0,1,0,0,0},{0,0,2,0,0},{0,0,0,0,5},{0,0,0,0,5},{0,2,4,0,10}}
  };
  String[] labels={"HP","armor","physical/magic Resistance","healing received","knockback points"};
  for(int branch=0;branch<3;branch++){
   c.resetSkills(p);p.removeAllEffects();c.getSkill("ad8qet8e75ys5sek").orElseThrow().unlock(p);double[] base=stats(p),sum=new double[5];double outgoing=p.getAttributeValue(healing.attributeEntry);
   var path=DefenseV2.PATHS.get(branch);
   for(int i=0;i<8;i++){
    c.getSkill(path.get(i)).orElseThrow().unlock(p);double[] actual=stats(p);
    for(int k=0;k<5;k++){sum[k]+=budget[branch][i][k];near(actual[k]-base[k],sum[k],"Path "+branch+" node "+(i+1)+" cumulative "+labels[k]);}
   }
   near(p.getAttributeValue(healing.attributeEntry),outgoing,"Defense does not grant outgoing Healing Power "+branch);
   c.resetSkills(p);double[] reset=stats(p);for(int k=0;k<5;k++)near(reset[k],base[k],"Refund removes all path bonuses "+branch+" "+labels[k]);
  }
 }
 void tick(ClientTickEvent.Post event){var mc=Minecraft.getInstance();ticks++;if(!hidden&&mc.getWindow()!=null){org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());hidden=true;mc.options.pauseOnLostFocus=false;}if(phase==99)return;try{
  if(error.get()!=null)throw new RuntimeException(error.get());if(pending||mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;if(ticks-at>2400)throw new AssertionError("Timeout phase"+phase);if(ticks-at<35)return;
  if(phase==0){server(p->{audit(p);});step();return;}
  if(phase==1)finish();
 }catch(Throwable e){e.printStackTrace();lines.add("FAIL "+e);finish();}}
}
