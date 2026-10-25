package local.priestultimates;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.entity.*;
import net.spell_engine.internals.*;
import net.spell_engine.internals.impact.SpellImpacts;
import net.spell_engine.internals.target.SpellTarget;
import net.spell_power.api.*;
import net.paladins.entity.BarrierEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
@Mod(value="priest_ultimate_client_probe",dist=Dist.CLIENT)
public class ClientPriestUltimateProbe {
 int ticks;boolean hidden,done;List<String> results=Collections.synchronizedList(new ArrayList<>());
 public ClientPriestUltimateProbe(){NeoForge.EVENT_BUS.addListener(this::tick);}
 void check(boolean ok,String text){results.add((ok?"PASS ":"FAIL ")+text);}
 void server(java.util.function.Consumer<net.minecraft.server.level.ServerPlayer> task){var mc=Minecraft.getInstance();mc.getSingleplayerServer().execute(()->{try{task.accept(mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID()));}catch(Throwable t){results.add("FAIL server "+t);t.printStackTrace();}});}
 void cast(net.minecraft.server.level.ServerPlayer p,String id){var h=SpellRegistry.from(p.level()).get(Identifier.parse(id)).orElseThrow();var c=new SpellExecution.ImpactContext(1,1,p.position(),SpellPower.getSpellPower(h.value().school,p),SpellTarget.FocusMode.AREA,0);SpellImpacts.performImpacts(p.level(),p,p,p,h,h.value().impacts,c,false,null);}
 List<SummonedEntity> wells(){var mc=Minecraft.getInstance();return mc.level.getEntitiesOfClass(SummonedEntity.class,mc.player.getBoundingBox().inflate(30),e->BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString().equals("paladins:lightwell"));}
 void tick(ClientTickEvent.Post event){
  var mc=Minecraft.getInstance();if(!hidden){org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());hidden=true;}
  if(done||mc.player==null||mc.level==null||++ticks<40)return;
  try{
   if(ticks==40){mc.setScreenAndShow(null);mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);server(p->{p.getInventory().clearContent();p.removeAllEffects();p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);p.level().getServer().getCommands().performPrefixedCommand(p.createCommandSourceStack(),"tp @s 200 -60 200 180 0");p.level().getServer().getCommands().performPrefixedCommand(p.createCommandSourceStack(),"time set noon");p.getAttribute(SpellSchools.HEALING.getAttributeEntry()).setBaseValue(40);p.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);p.getFoodData().setFoodLevel(0);p.setHealth(80);});}
   if(ticks==65)server(p->cast(p,"paladins:lightwell"));
   if(ticks==90){check(wells().size()==1,"native Lightwell summon synchronized");var well=wells().getFirst();check(Math.abs(well.getX()-mc.player.getX())<.2,"well hovers over caster x");check(well.getY()>mc.player.getY()+mc.player.getBbHeight(),"well visible above caster head");Screenshot.grab(mc,false);server(p->{var list=p.level().getEntitiesOfClass(SummonedEntity.class,p.getBoundingBox().inflate(30),e->BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString().equals("paladins:lightwell"));var e=list.getFirst();check(e.getTarget()==p,"native summon controller targets injured caster");check(Math.abs(SpellPower.getSpellPower(SpellSchools.HEALING,e).nonCriticalValue()-2)<.001,"connected summon has exact five percent inheritance");p.teleportTo(p.getX()+6,p.getY(),p.getZ());});}
   if(ticks==120){var well=wells().getFirst();check(Math.abs(well.getX()-mc.player.getX())<.2,"well follows caster movement across network");Screenshot.grab(mc,false);server(p->cast(p,"paladins:barrier"));}
   if(ticks==145)server(p->{var all=p.level().getEntitiesOfClass(SummonedEntity.class,p.getBoundingBox().inflate(30));for(var e:all)results.add("TRACE well target="+e.getTarget()+" active="+e.isActive()+" cast="+e.getSpellCastProcess()+" cooldown="+e.cooldownManager.spellsOnCooldown());for(var e:p.level().getEntitiesOfClass(SpellProjectile.class,p.getBoundingBox().inflate(32)))results.add("TRACE mote owner="+e.getOwner()+" follows="+e.getFollowedTarget()+" pos="+e.position()+" velocity="+e.getDeltaMovement());results.add("TRACE owner health="+p.getHealth());check(p.getHealth()>80,"native well AI actually fires healing motes");});
   if(ticks==155){var barriers=mc.level.getEntitiesOfClass(BarrierEntity.class,mc.player.getBoundingBox().inflate(30));check(barriers.size()==1,"native Barrier synchronized");check(Math.abs(barriers.getFirst().getBbWidth()-20)<.01,"Barrier displays twenty-block diameter");check(Math.abs(barriers.getFirst().getBbHeight()-10)<.01,"Barrier height follows ten-block radius");Screenshot.grab(mc,false);server(p->{var b=p.level().getEntitiesOfClass(BarrierEntity.class,p.getBoundingBox().inflate(30)).getFirst();check(PriestUltimates.protectedNow(p),"connected owner receives live-volume protection");p.teleportTo(b.getX()+11,b.getY(),b.getZ());});}
   if(ticks==185)server(p->check(!PriestUltimates.protectedNow(p),"connected owner loses protection outside radius"));
   if(ticks==440){check(wells().isEmpty(),"well expires after sixteen active seconds plus animation phases");server(p->check(!PriestUltimates.protectedNow(p),"expired Barrier leaves no immunity"));}
   if(ticks>=460)finish();
  }catch(Throwable t){results.add("FAIL "+t);t.printStackTrace();finish();}
 }
 void finish(){done=true;var mc=Minecraft.getInstance();try{Files.write(mc.gameDirectory.toPath().resolve("visual-results.txt"),results);Files.writeString(mc.gameDirectory.toPath().resolve("visual-result.txt"),(results.stream().anyMatch(s->s.startsWith("FAIL"))?"FAIL":"PASS")+" "+results.stream().filter(s->s.startsWith("PASS")).count()+" checks\n");}catch(Exception e){throw new RuntimeException(e);}mc.stop();}
}
