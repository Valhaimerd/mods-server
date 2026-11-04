package local.audit;
import java.util.*;import java.nio.file.*;
import net.minecraft.resources.Identifier;import net.minecraft.server.level.*;import net.minecraft.world.entity.*;import net.minecraft.world.entity.ai.attributes.Attributes;import net.minecraft.world.entity.projectile.arrow.AbstractArrow;import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.common.Mod;import net.neoforged.neoforge.common.NeoForge;import net.neoforged.neoforge.event.server.ServerStartedEvent;import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.spell_engine.internals.*;import net.puffish.skillsmod.api.*;
@Mod("phase_two_probe") public class ArrowAssistProbe {
 List<String> lines=new ArrayList<>();int failures;ServerLevel w;ServerPlayer p;Category category;
 static final String ARCHER="418rsvnd17egwkse",WARRIOR="e5ljj7jx6pmdjatf";
 public ArrowAssistProbe(){NeoForge.EVENT_BUS.addListener(this::start);}
 void check(boolean ok,String text){lines.add((ok?"PASS ":"FAIL ")+text);if(!ok)failures++;}
 void near(double actual,double expected,String text){check(Math.abs(actual-expected)<.0001,text+" actual="+actual+" expected="+expected);}
 void select(String root){category.resetSkills(p);if(root!=null)category.getSkill(root).orElseThrow().unlock(p);}
 AbstractArrow arrow(){var a=EntityTypes.ARROW.create(w,EntitySpawnReason.COMMAND);a.setOwner(p);a.setPos(0,100.7,0);a.setDeltaMovement(0,0,3);return a;}
 LivingEntity mob(double x,double z){var e=EntityTypes.CREEPER.create(w,EntitySpawnReason.COMMAND);e.setNoAi(true);e.setNoGravity(true);e.setPos(x,100,z);e.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);e.setHealth(1000);w.addFreshEntity(e);return e;}
 void tick(AbstractArrow a){ArrowAssist.tick(new EntityTickEvent.Pre(a));}
 void start(ServerStartedEvent event){w=event.getServer().overworld();w.setChunkForced(0,0,true);w.setChunkForced(0,1,true);w.setChunkForced(-1,0,true);((net.spell_engine.utils.WorldScheduler)w).schedule(30,()->{try{run();}catch(Throwable t){t.printStackTrace();check(false,"Exception "+t);}finally{try{Files.write(Path.of("phase-two-checks.txt"),lines);Files.writeString(Path.of("phase-two-result.txt"),(failures==0?"PASS ":"FAIL ")+lines.size()+" checks; failures="+failures+"\n");}catch(Exception ignored){}event.getServer().halt(false);}});}
 void run()throws Exception{
  p=net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(w);p.setPos(0,100,0);category=SkillsAPI.getCategory(Identifier.parse("skill_tree_rpgs:class_skills")).orElseThrow();category.unlock(p);category.setExtraPoints(p,1000);
  for(String root:new String[]{WARRIOR,"ov6ag9tjdhsmldil","tdimeso8ajcwiygp","qr4fdarqqqm5ifoa","m1pc5vz73nx4b28c","akyr8k2dv9xsy70c","63gwys64onqg00ry"}){select(root);check(!ArrowAssist.archer(p),"Non-Archer class rejected "+root);var target=mob(.6,12);var a=arrow();tick(a);near(a.getDeltaMovement().x,0,"Non-Archer arrow stays ballistic "+root);a.discard();target.discard();}
  select(null);check(!ArrowAssist.archer(p),"No class rejected");select(ARCHER);check(ArrowAssist.archer(p),"Sole Archer accepted");
  var data=((net.valhaimerd.orboblivioncompat.mixin.SkillsDataAccess)(Object)net.puffish.skillsmod.SkillsMod.getInstance()).utilities$playerData(p);
  var saved=((net.valhaimerd.orboblivioncompat.mixin.PlayerDataAccess)(Object)data).utilities$categories().get(Identifier.parse("skill_tree_rpgs:class_skills"));saved.getUnlockedSkillIds().add(WARRIOR);
  check(!ArrowAssist.archer(p),"Stale Archer root alongside Warrior fails closed");saved.getUnlockedSkillIds().remove(WARRIOR);
  for(double speed:new double[]{1,3,4.5,9}){var v=new Vec3(0,0,speed);var next=ArrowAssist.steer(v,new Vec3(1,0,10),Math.toRadians(2));near(Math.toDegrees(ArrowAssist.angle(v,next)),2,"Requested two-degree hard turn "+speed);near(next.length(),speed,"No speed boost "+speed);}
  select(ARCHER);check(ArrowAssist.archer(p),"Archer remains sole active class before acquisition");var target=mob(.6,12);var a=arrow();tick(a);check(a.getDeltaMovement().x>0,"Aimed near miss gets correction");check(Math.toDegrees(ArrowAssist.angle(new Vec3(0,0,3),a.getDeltaMovement()))<=2.00001,"Arrow turn never exceeds2deg");var corrected=a.getDeltaMovement();target.setPos(-.6,100,12);a.tickCount=1;tick(a);near(a.getDeltaMovement().distanceTo(corrected),0,"Moving target is not continuously tracked");a.discard();target.discard();
  var first=mob(.5,8);var second=mob(.6,16);a=arrow();tick(a);check(a.getPersistentData().getStringOr("assist_target","").equals(first.getUUID().toString()),"Nearest eligible enemy chosen at launch");first.discard();var v=a.getDeltaMovement();tick(a);near(a.getDeltaMovement().distanceTo(v),0,"No retarget when original enemy disappears");a.discard();second.discard();
  for(double offset:new double[]{3,5,-5}){target=mob(offset,12);a=arrow();tick(a);near(a.getDeltaMovement().x,0,"Clearly missed aim remains miss "+offset);a.discard();target.discard();}
  target=mob(.6,-12);a=arrow();tick(a);near(a.getDeltaMovement().x,0,"Enemy behind bow excluded");a.discard();target.discard();
  target=mob(.6,12);a=arrow();a.tickCount=20;tick(a);near(a.getDeltaMovement().x,0,"Old airborne arrow cannot acquire new target");a.discard();target.discard();
  target=mob(.6,12);a=arrow();select(WARRIOR);tick(a);select(ARCHER);tick(a);near(a.getDeltaMovement().x,0,"Class change cannot retrofit an already launched arrow");a.discard();target.discard();
  target=mob(.6,12);for(int x=-1;x<=1;x++)for(int y=100;y<=102;y++)w.setBlock(new net.minecraft.core.BlockPos(x,y,6),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3);a=arrow();tick(a);near(a.getDeltaMovement().x,0,"Wall blocks acquisition");a.discard();target.discard();
  for(int x=-1;x<=1;x++)for(int y=100;y<=102;y++)w.setBlock(new net.minecraft.core.BlockPos(x,y,6),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);
  var ownPet=EntityTypes.WOLF.create(w,EntitySpawnReason.COMMAND);ownPet.setOwnerReference(EntityReference.of(p.getUUID()));ownPet.setPos(.5,100,12);w.addFreshEntity(ownPet);a=arrow();tick(a);near(a.getDeltaMovement().x,0,"Own pet never acquired");a.discard();ownPet.discard();
  var eligibility=ArrowAssist.class.getDeclaredMethod("eligible",AbstractArrow.class,ServerPlayer.class,LivingEntity.class);eligibility.setAccessible(true);
  var shooter=new ProbePlayer(w,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"AssistShooter"));var ally=new ProbePlayer(w,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"AssistAlly"));var opponent=new ProbePlayer(w,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"AssistEnemy"));
  var manager=dev.betterparty.party.PartyManager.forServer(w.getServer());var partyField=manager.getClass().getDeclaredField("parties");partyField.setAccessible(true);var membershipField=manager.getClass().getDeclaredField("playerParties");membershipField.setAccessible(true);
  var parties=(Map<UUID,dev.betterparty.party.Party>)partyField.get(manager);var memberships=(Map<UUID,UUID>)membershipField.get(manager);var partyId=UUID.randomUUID();var party=new dev.betterparty.party.Party(partyId);party.addMember(new dev.betterparty.party.PartyMember(shooter.getUUID(),"Shooter",dev.betterparty.party.PartyRole.LEADER));party.addMember(new dev.betterparty.party.PartyMember(ally.getUUID(),"Ally",dev.betterparty.party.PartyRole.MEMBER));parties.put(partyId,party);memberships.put(shooter.getUUID(),partyId);memberships.put(ally.getUUID(),partyId);a=arrow();a.setOwner(shooter);
  check(!(boolean)eligibility.invoke(null,a,shooter,ally),"Party player excluded");check((boolean)eligibility.invoke(null,a,shooter,opponent),"Eligible hostile player retained");memberships.put(opponent.getUUID(),partyId);party.addMember(new dev.betterparty.party.PartyMember(opponent.getUUID(),"Enemy",dev.betterparty.party.PartyRole.MEMBER));check(!(boolean)eligibility.invoke(null,a,shooter,opponent),"Party change respected");
  var pet=EntityTypes.WOLF.create(w,EntitySpawnReason.COMMAND);pet.setOwnerReference(EntityReference.of(ally.getUUID()));check(!(boolean)eligibility.invoke(null,a,shooter,pet),"Party pet excluded");parties.remove(partyId);memberships.remove(shooter.getUUID());memberships.remove(ally.getUUID());memberships.remove(opponent.getUUID());a.discard();
  target=mob(.6,12);a=arrow();NeoForge.EVENT_BUS.post(new EntityTickEvent.Pre(a));check(a.getDeltaMovement().x>0,"Actual registered native tick listener assists near miss");var once=a.getDeltaMovement();target.setPos(-.6,100,12);for(int t=1;t<=20;t++){a.tickCount=t;NeoForge.EVENT_BUS.post(new EntityTickEvent.Pre(a));near(a.getDeltaMovement().distanceTo(once),0,"No repeated tracking after launch tick "+t);}a.discard();target.discard();
  target=mob(.6,12);target.addEffect(new net.minecraft.world.effect.MobEffectInstance(PaladinRevamp.effect("rogues:stealth"),100));a=arrow();tick(a);near(a.getDeltaMovement().x,0,"Vanish enemy never acquired");a.discard();target.discard();
 }
 public static final class ProbePlayer extends net.neoforged.neoforge.common.util.FakePlayer {
  public ProbePlayer(ServerLevel world,com.mojang.authlib.GameProfile profile){super(world,profile);}
  @Override public boolean canHarmPlayer(net.minecraft.world.entity.player.Player other){return level().isPvpAllowed();}
 }

}
