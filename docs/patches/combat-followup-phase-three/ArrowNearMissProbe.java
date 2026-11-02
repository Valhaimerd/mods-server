package local.audit;
import java.util.*;import java.nio.file.*;
import net.neoforged.fml.common.Mod;import net.neoforged.neoforge.common.NeoForge;import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.minecraft.resources.Identifier;import net.minecraft.world.entity.*;import net.minecraft.world.phys.Vec3;
import net.spell_engine.internals.ArrowAssist;
@Mod("phase_two_probe") public class ArrowNearMissProbe {
 List<String> lines=new ArrayList<>();int n;void check(boolean ok,String s){if(!ok)throw new AssertionError(s);n++;lines.add("PASS "+s);}
 public ArrowNearMissProbe(){NeoForge.EVENT_BUS.addListener(this::start);}
 void start(ServerStartedEvent e){var w=e.getServer().overworld();w.setChunkForced(0,0,true);w.setChunkForced(0,1,true);((net.spell_engine.utils.WorldScheduler)w).schedule(30,()->{try{
  var p=net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(w);p.setPos(0,100,0);var cat=net.puffish.skillsmod.api.SkillsAPI.getCategory(Identifier.parse("skill_tree_rpgs:class_skills")).orElseThrow();cat.unlock(p);cat.resetSkills(p);cat.setExtraPoints(p,1000);cat.getSkill("418rsvnd17egwkse").orElseThrow().unlock(p);check(ArrowAssist.archer(p),"Actual Archer root selected");
  int hits=0,cases=0;
  for(double speed:new double[]{3,4.5})for(double distance:new double[]{12,18,24})for(double offset:new double[]{.45,.6,.75}){
   var target=EntityTypes.CREEPER.create(w,EntitySpawnReason.COMMAND);target.setNoAi(true);target.setNoGravity(true);target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(1000);target.setHealth(1000);target.setPos(offset,100,distance);w.addFreshEntity(target);
   var arrow=EntityTypes.ARROW.create(w,EntitySpawnReason.COMMAND);arrow.setOwner(p);arrow.setPos(0,100.7,0);arrow.setNoGravity(true);arrow.setDeltaMovement(0,0,speed);arrow.setBaseDamage(5);w.addFreshEntity(arrow);
   double maxTurn=0,previousSpeed=speed;for(int t=0;t<15&&!arrow.isRemoved()&&arrow.getZ()<distance+2;t++){
    var before=arrow.getDeltaMovement();ArrowAssist.tick(new net.neoforged.neoforge.event.tick.EntityTickEvent.Pre(arrow));var after=arrow.getDeltaMovement();maxTurn=Math.max(maxTurn,Math.toDegrees(ArrowAssist.angle(before,after)));check(Math.abs(before.length()-after.length())<1e-8,"No steering speed change "+speed+"/"+distance+"/"+offset+"/"+t);arrow.tick();
   }
   boolean hit=target.getHealth()<1000;if(hit)hits++;cases++;lines.add("TRAJECTORY speed="+speed+" distance="+distance+" offset="+offset+" hit="+hit+" maxTurn="+maxTurn+" selected="+arrow.getPersistentData().getIntOr("class_pack_arrow_assist",0));check(maxTurn<=.50001,"Slight steering hard cap");arrow.discard();target.discard();
  }
  for(double offset:new double[]{3,5}){var target=EntityTypes.CREEPER.create(w,EntitySpawnReason.COMMAND);target.setNoAi(true);target.setPos(offset,100,12);w.addFreshEntity(target);var a=EntityTypes.ARROW.create(w,EntitySpawnReason.COMMAND);a.setOwner(p);a.setPos(0,100.7,0);a.setDeltaMovement(0,0,4.5);ArrowAssist.tick(new net.neoforged.neoforge.event.tick.EntityTickEvent.Pre(a));check(a.getDeltaMovement().x==0,"Large miss remains unassisted "+offset);a.discard();target.discard();}
  lines.add("TOTAL native near-miss hits="+hits+"/"+cases);Files.writeString(Path.of("phase-two-result.txt"),"PASS "+n+" near-miss checks; hits="+hits+"/"+cases+"\n");
 }catch(Throwable t){t.printStackTrace();try{Files.writeString(Path.of("phase-two-result.txt"),"FAIL "+t);}catch(Exception ignored){}}finally{try{Files.write(Path.of("phase-two-checks.txt"),lines);}catch(Exception ignored){}e.getServer().halt(false);}});}
}
