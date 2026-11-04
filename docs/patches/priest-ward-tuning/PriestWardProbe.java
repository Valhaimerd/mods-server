package local.spatialclient;
import java.util.*;import java.nio.file.*;import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.client.*;import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.api.distmarker.Dist;import net.neoforged.fml.common.Mod;import net.neoforged.neoforge.common.NeoForge;import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.valhaimerd.orboblivioncompat.*;import net.puffish.skillsmod.api.*;
@Mod(value="spatial_client_probe",dist=Dist.CLIENT) public class PriestWardProbe {
 int ticks,phase,at;boolean pending,hidden,joined;final List<String> lines=new ArrayList<>();final AtomicReference<Throwable> error=new AtomicReference<>();
 net.puffish.skillsmod.client.gui.SkillsScreen screen;
 public PriestWardProbe(){NeoForge.EVENT_BUS.addListener(this::tick);NeoForge.EVENT_BUS.addListener(this::opening);}
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
  p.removeAllEffects();p.getInventory().clearContent();p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.setInvulnerable(false);p.getAbilities().invulnerable=false;
  p.getPersistentData().putBoolean("skill_overhaul_v1",true);p.getPersistentData().putBoolean("utilities_v1_migrated",true);p.getPersistentData().putBoolean("defense_v2_refunded",true);
  var category=SkillsAPI.getCategory(Utilities.CATEGORY).orElseThrow();category.unlock(p);category.resetSkills(p);
  p.getInventory().add(new net.minecraft.world.item.ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.Identifier.parse("runes:healing_stone")).orElseThrow(),64));
  var w=p.level();for(int x=-10;x<=10;x++)for(int z=-10;z<=10;z++)w.setBlock(new net.minecraft.core.BlockPos(x,99,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3);
  p.teleportTo(0,100,0);p.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);p.setHealth(100);
  var healing=net.spell_power.api.SpellSchools.HEALING;p.getAttribute(healing.attributeEntry).setBaseValue(20);
  p.getAttribute(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.get(net.minecraft.resources.Identifier.parse("spell_power:critical_chance")).orElseThrow()).setBaseValue(0);
  var haste=p.getAttribute(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.get(net.minecraft.resources.Identifier.parse("spell_power:haste")).orElseThrow());haste.setBaseValue(100);
  var holy=spell(p,"paladins:holy_beam");var penance=spell(p,"paladins:penance");var circle=spell(p,"paladins:circle_of_healing");
  equip(p,List.of("paladins:holy_beam"),List.of(),List.of());
  check(!holy.value().cost.cooldown.proportional,"Holy Light proportional metadata disabled");
  for(float progress:new float[]{0,.04f,.5f,.96f,1}){
   near(net.spell_engine.internals.PriestTuning.cooldown(p,holy,progress),12,"Holy Light flat cooldown progress="+progress);
   if(progress==0)continue; // No delivery/no cast is not an executed skill.
   ((net.spell_engine.internals.casting.SpellCaster.Player)p).getCooldownManager().reset(null);
   net.spell_engine.internals.SpellExecution.performSpell(w,p,holy,net.spell_engine.internals.target.SpellTarget.SearchResult.empty(),net.spell_engine.internals.casting.SpellCast.Action.RELEASE,progress);
   near(((net.spell_engine.internals.casting.SpellCaster.Player)p).getCooldownManager().getCooldownDuration(holy),240,"Actual release cooldown ticks progress="+progress);
  }
  haste.setBaseValue(200);near(net.spell_engine.internals.PriestTuning.cooldown(p,holy,.5f),6,"Holy Light still affected by haste");haste.setBaseValue(100);
  near(net.spell_engine.internals.PriestTuning.cooldown(p,penance,0),8,"Penance minimum cooldown retained");near(net.spell_engine.internals.PriestTuning.cooldown(p,penance,1),12,"Penance full cooldown retained");
  var ctx=new net.spell_engine.internals.SpellExecution.ImpactContext(1,1,p.position(),new net.spell_power.api.SpellPower.Result(healing,20,0,1.5),net.spell_engine.internals.target.SpellTarget.FocusMode.DIRECT,0);
  for(boolean improved:new boolean[]{false,true}){
   equip(p,List.of(),List.of(),improved?List.of("skill_tree_rpgs:priest_tier_3_spell_2_root"):List.of());
   var impact=net.spell_engine.internals.PriestRules.impact(p,penance,penance.value().impacts.getFirst(),ctx,null);near(impact.action.damage.spell_power_coefficient,improved?.5:.4,"Penance runtime coefficient improved="+improved);
   var target=enemy(p,5);net.spell_engine.internals.PriestTuning.projectile(p,p,target,penance,ctx.position(target.position()));near(1000-target.getHealth(),20*(improved?.5:.4)*.8,"Actual Penance explosion damage improved="+improved);target.discard();p.removeAllEffects();p.setAbsorptionAmount(0);
   equip(p,List.of(),List.of(),improved?List.of("skill_tree_rpgs:priest_tier_3_spell_1_root"):List.of());p.setHealth(1);
   net.spell_engine.internals.impact.SpellImpacts.performImpacts(w,p,p,p,circle,circle.value().impacts,ctx,false,null);near(p.getHealth()-1,20*(improved?.9:.65),"Actual Circle healing improved="+improved);
  }
  equip(p,List.of(),List.of("skill_tree_rpgs:priest_tier_3_passive_2"),List.of());p.removeAllEffects();p.setHealth(100);
  var orb=spell(p,"skill_tree_rpgs:priest_tier_3_passive_2");near(orb.value().impacts.getFirst().action.damage.spell_power_coefficient,.4,"Celestial Orb coefficient40%");near(orb.value().impacts.getFirst().action.damage.knockback,1,"Celestial Orb strong knockback1.0");
  net.spell_engine.internals.SpellTriggers.onSpellImpactSpecific(p,p,holy,holy.value().impacts.getFirst(),true,net.spell_engine.api.spell.Spell.Trigger.Stage.POST);
  var orbFx=net.spell_engine.internals.PriestRules.effect("skill_tree_rpgs:celestial_orb");check(p.hasEffect(orbFx)&&p.getEffect(orbFx).getAmplifier()==4,"Five celestial orbs readied");
  var attacker=enemy(p,3);p.invulnerableTime=0;check(!p.isInvulnerableTo(w,p.damageSources().mobAttack(attacker)),"Joined player accepts combat damage");check(p.hurtServer(w,p.damageSources().mobAttack(attacker),1),"Actual incoming hit accepted");
  near(1000-attacker.getHealth(),6.4,"Actual own-hit Orb retaliation40% P after PvE retention");check(attacker.getDeltaMovement().horizontalDistance()>.2,"Orb applies strong physical knockback");check(p.getEffect(orbFx).getAmplifier()==3,"Retaliation consumes exactly one Orb");attacker.discard();
  for(double distance:new double[]{3,4,4.01,8}){
   p.removeAllEffects();p.setAbsorptionAmount(0);p.teleportTo(0,100,0);p.setHealth(100);p.invulnerableTime=0;
   p.getAttribute(net.spell_power.api.SpellSchools.ARCANE.attributeEntry).setBaseValue(20);
   equip(p,List.of(),List.of("skill_tree_rpgs:arcane_tier_3_passive_2"),List.of());var e=enemy(p,distance);var before=p.position();p.hurtServer(w,p.damageSources().mobAttack(e),1);
   check(p.hasEffect(net.spell_engine.internals.PriestRules.effect("skill_tree_rpgs:arcane_ward"))&&p.getAbsorptionAmount()>0,"Ward still shields at distance="+distance);
   near(p.position().distanceTo(before),distance<=4?4:0,"Ward teleport distance, attacker range="+distance);e.discard();
  }
 }
 void tick(ClientTickEvent.Post event){var mc=Minecraft.getInstance();ticks++;if(!hidden&&mc.getWindow()!=null){org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());hidden=true;mc.options.pauseOnLostFocus=false;}if(phase==99)return;try{
  if(error.get()!=null)throw new RuntimeException(error.get());if(pending||mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;if(!joined){joined=true;at=ticks;return;}if(ticks-at>2400)throw new AssertionError("Timeout phase"+phase);if(ticks-at<100)return;
  if(phase==0){server(p->{audit(p);});step();return;}
  if(phase==1)finish();
 }catch(Throwable e){e.printStackTrace();lines.add("FAIL "+e);finish();}}
}
