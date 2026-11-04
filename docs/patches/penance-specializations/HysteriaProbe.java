package local.spatialclient;
import java.util.*;import java.nio.file.*;import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.client.*;import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.api.distmarker.Dist;import net.neoforged.fml.common.Mod;import net.neoforged.neoforge.common.NeoForge;import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.valhaimerd.orboblivioncompat.*;import net.puffish.skillsmod.api.*;
@Mod(value="spatial_client_probe",dist=Dist.CLIENT) public class HysteriaProbe {
 int ticks,phase,at;boolean pending,hidden,joined;final List<String> lines=new ArrayList<>();final AtomicReference<Throwable> error=new AtomicReference<>();
 net.puffish.skillsmod.client.gui.SkillsScreen screen;
 public HysteriaProbe(){NeoForge.EVENT_BUS.addListener(this::tick);NeoForge.EVENT_BUS.addListener(this::opening);}
 void opening(net.neoforged.neoforge.client.event.ScreenEvent.Opening e){if(e.getNewScreen() instanceof net.puffish.skillsmod.client.gui.SkillsScreen s)screen=s;}
 void check(boolean ok,String label){lines.add((ok?"PASS ":"FAIL ")+label);if(!ok)throw new AssertionError(label);}
 void server(java.util.function.Consumer<net.minecraft.server.level.ServerPlayer> job){var mc=Minecraft.getInstance();pending=true;mc.getSingleplayerServer().execute(()->{try{job.accept(mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID()));}catch(Throwable e){error.set(e);}finally{pending=false;}});}
 void step(){phase++;at=ticks;}
 void finish(){if(phase==99)return;phase=99;var mc=Minecraft.getInstance();try{Files.write(mc.gameDirectory.toPath().resolve("spatial-client-checks.txt"),lines);Files.writeString(mc.gameDirectory.toPath().resolve("spatial-client-result.txt"),lines.stream().anyMatch(s->s.startsWith("FAIL"))?"FAIL":"PASS");}catch(Exception ignored){}mc.stop();}
 void view(float scale,int x,int y)throws Exception{var field=screen.getClass().getDeclaredField("optActiveCategoryData");field.setAccessible(true);var category=(net.puffish.skillsmod.client.data.ClientCategoryData)((Optional<?>)field.get(screen)).orElseThrow();category.setScale(scale);category.setX(x);category.setY(y);}


 void near(double actual,double expected,String label){check(Math.abs(actual-expected)<.025,label+" actual="+actual+" expected="+expected);}
 net.minecraft.core.Holder<net.spell_engine.api.spell.Spell> spell(net.minecraft.server.level.ServerPlayer p,String id){return net.spell_engine.internals.PaladinRevamp.spell(p,id);}
 void equip(net.minecraft.server.level.ServerPlayer p,List<String> active,List<String> passive,List<String> modifiers){
  var owner=(net.spell_engine.internals.container.SpellContainerSource.Owner)p;var previous=owner.getSpellContainers();
  owner.setSpellContainers(new net.spell_engine.internals.container.SpellContainerSource.Result(previous.activeContainer(),active.stream().map(id->spell(p,id)).toList(),passive.stream().map(id->spell(p,id)).toList(),modifiers.stream().map(id->spell(p,id)).toList(),List.of(new net.spell_engine.internals.container.SpellContainerSource.SourcedContainer("probe",new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STICK),new net.spell_engine.api.spell.container.SpellContainer(net.spell_engine.api.spell.container.SpellContainer.ContentType.ANY,"","",0,java.util.stream.Stream.of(active,passive,modifiers).flatMap(List::stream).toList())))));owner.spellModifierCache().clear();
  ((net.spell_engine.internals.casting.SpellCaster.Player)p).getCooldownManager().reset(null);
 }
 net.minecraft.world.entity.LivingEntity enemy(net.minecraft.server.level.ServerPlayer p,double distance){
  var e=net.minecraft.world.entity.EntityTypes.CREEPER.create(p.level(),net.minecraft.world.entity.EntitySpawnReason.COMMAND);e.setPos(0,100,distance);e.setNoAi(true);e.setNoGravity(true);e.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);e.setHealth(1000);p.level().addFreshEntity(e);return e;
 }

 void audit(net.minecraft.server.level.ServerPlayer p){
  p.removeAllEffects();p.getInventory().clearContent();p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
  p.getPersistentData().putBoolean("skill_overhaul_v1",true);p.getPersistentData().putBoolean("utilities_v1_migrated",true);p.getPersistentData().putBoolean("defense_v2_refunded",true);
  var c=SkillsAPI.getCategory(Utilities.CATEGORY).orElseThrow();c.unlock(p);c.resetSkills(p);p.teleportTo(0,100,0);p.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);p.setHealth(100);
  String[] names={"minecraft:attack_speed","ranged_weapon:haste","spell_power:haste"};double[] base=new double[3];
  for(int i=0;i<3;i++)base[i]=p.getAttributeValue(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.get(net.minecraft.resources.Identifier.parse(names[i])).orElseThrow());
  var fx=net.spell_engine.internals.PriestRules.effect("skill_tree_rpgs:hysteria");
  for(int stacks:new int[]{1,10}){
   p.removeEffect(fx);p.addEffect(new net.minecraft.world.effect.MobEffectInstance(fx,80,stacks-1));
   for(int i=0;i<3;i++){double value=p.getAttributeValue(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.get(net.minecraft.resources.Identifier.parse(names[i])).orElseThrow());near(value/base[i],1+.015*stacks,"Hysteria "+names[i]+" at "+stacks+" stacks");}
  }
  p.removeAllEffects();equip(p,List.of(),List.of(),List.of("skill_tree_rpgs:priest_tier_3_spell_2_modifier_1"));
  var penance=spell(p,"paladins:penance");var target=enemy(p,5);
  var ctx=new net.spell_engine.internals.SpellExecution.ImpactContext(1,1,p.position(),new net.spell_power.api.SpellPower.Result(net.spell_power.api.SpellSchools.HEALING,20,0,1.5),net.spell_engine.internals.target.SpellTarget.FocusMode.DIRECT,0);
  for(int i=1;i<=11;i++){
   target.invulnerableTime=0;net.spell_engine.internals.PriestTuning.projectile(p,p,target,penance,ctx.position(target.position()));
   check(p.hasEffect(fx)&&p.getEffect(fx).getAmplifier()+1==Math.min(i,10),"Actual Penance remote caster Hysteria stack "+i);
  }
  check(p.getEffect(fx).getDuration()==80,"Hysteria refresh remains4s");
  for(int i=0;i<3;i++)near(p.getAttributeValue(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.get(net.minecraft.resources.Identifier.parse(names[i])).orElseThrow())/base[i],1.15,"Actual Penance maximum15% "+names[i]);
  p.removeAllEffects();for(int i=0;i<3;i++)near(p.getAttributeValue(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.get(net.minecraft.resources.Identifier.parse(names[i])).orElseThrow()),base[i],"Hysteria cleanup "+names[i]);target.discard();
  var victim=enemy(p,5);var chastise=net.spell_engine.internals.PriestRules.effect("skill_tree_rpgs:chastise");
  var damageTaken=net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.get(net.minecraft.resources.Identifier.parse("spell_engine:damage_taken")).orElseThrow();
  double baseline=victim.getAttributeValue(damageTaken);
  for(int stacks:new int[]{1,10}){victim.removeEffect(chastise);victim.addEffect(new net.minecraft.world.effect.MobEffectInstance(chastise,80,stacks-1));near(victim.getAttributeValue(damageTaken)/baseline,1+.015*stacks,"Chastise received damage at "+stacks+" stacks");}
  victim.removeAllEffects();equip(p,List.of(),List.of(),List.of("skill_tree_rpgs:priest_tier_3_spell_2_modifier_2"));
  for(int i=1;i<=11;i++){victim.invulnerableTime=0;net.spell_engine.internals.PriestTuning.projectile(p,p,victim,penance,ctx.position(victim.position()));check(victim.getEffect(chastise).getAmplifier()+1==Math.min(i,10),"Actual Penance Chastise stack "+i);}
  near(victim.getAttributeValue(damageTaken)/baseline,1.15,"Chastise native maximum15% damage taken");check(victim.getEffect(chastise).getDuration()==80,"Chastise refresh4s");
  var hit=spell(p,"paladins:heal");victim.removeEffect(chastise);victim.invulnerableTime=0;victim.setHealth(1000);net.spell_engine.internals.impact.SpellImpacts.performImpacts(p.level(),p,victim,p,hit,hit.value().impacts,ctx,false,null);float normal=1000-victim.getHealth();victim.invulnerableTime=0;victim.setHealth(1000);victim.addEffect(new net.minecraft.world.effect.MobEffectInstance(chastise,80,9));net.spell_engine.internals.impact.SpellImpacts.performImpacts(p.level(),p,victim,p,hit,hit.value().impacts,ctx,false,null);near(1000-victim.getHealth(),normal*1.15,"Actual spell damage amplified15%");
  victim.removeEffect(chastise);near(victim.getAttributeValue(damageTaken),baseline,"Chastise removal restores damage taken");victim.discard();
 }
 void tick(ClientTickEvent.Post event){var mc=Minecraft.getInstance();ticks++;if(!hidden&&mc.getWindow()!=null){org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());hidden=true;mc.options.pauseOnLostFocus=false;}if(phase==99)return;try{
  if(error.get()!=null)throw new RuntimeException(error.get());if(pending||mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;if(!joined){joined=true;at=ticks;return;}if(ticks-at>2400)throw new AssertionError("Timeout phase"+phase);if(ticks-at<100)return;
  if(phase==0){server(p->{audit(p);});step();return;}
  if(phase==1)finish();
 }catch(Throwable e){e.printStackTrace();lines.add("FAIL "+e);finish();}}
}
