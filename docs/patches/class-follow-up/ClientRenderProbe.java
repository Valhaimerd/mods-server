package local.vanishprobe;
import java.util.*;import java.nio.file.*;import java.lang.reflect.*;
import net.minecraft.client.*;import net.minecraft.world.item.*;import net.minecraft.world.item.component.ChargedProjectiles;import net.minecraft.core.component.DataComponents;import net.minecraft.core.registries.BuiltInRegistries;import net.minecraft.resources.Identifier;import net.minecraft.world.entity.*;import net.minecraft.world.InteractionHand;
import net.neoforged.fml.common.Mod;import net.neoforged.api.distmarker.Dist;import net.neoforged.neoforge.common.NeoForge;import net.neoforged.neoforge.client.event.*;
@Mod(value="vanish_render_probe",dist=Dist.CLIENT) public class ClientRenderProbe{
 int ticks,phase;boolean pending;List<String> results=new ArrayList<>();
 public ClientRenderProbe(){NeoForge.EVENT_BUS.addListener(this::tick);NeoForge.EVENT_BUS.addListener(this::frame);}
 void check(boolean value,String text){results.add((value?"PASS ":"FAIL ")+text);}
 void frame(RenderFrameEvent.Post event){if(pending){Screenshot.grab(Minecraft.getInstance(),false);pending=false;}}
 @SuppressWarnings({"rawtypes","unchecked"})void proof()throws Exception{
  var mc=Minecraft.getInstance();var p=mc.player;
  var method=net.minecraft.client.renderer.entity.player.AvatarRenderer.class.getDeclaredMethod("getArmPose",Avatar.class,ItemStack.class,InteractionHand.class);method.setAccessible(true);
  var stack=new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse("archers:heavy_crossbow")));stack.set(DataComponents.CHARGED_PROJECTILES,ChargedProjectiles.of(new ItemStackTemplate(Items.ARROW)));
  check(method.invoke(null,p,stack,InteractionHand.MAIN_HAND)==net.minecraft.client.model.HumanoidModel.ArmPose.ITEM,"loaded RPG crossbow uses natural idle carrying arms");
  var manager=((com.zigythebird.playeranim.accessors.IAnimatedAvatar)p).playerAnimLib$getAnimManager();
  var flag=manager.getClass().getDeclaredField("classPackLastFirstPersonPass");flag.setAccessible(true);
  manager.handleAnimations(.5f,false,true);check(flag.getBoolean(manager),"first-person animation pass recorded");
  manager.handleAnimations(.5f,false,false);check(!flag.getBoolean(manager),"same-frame third-person pass reevaluated");
  manager.handleAnimations(.5f,false,true);check(flag.getBoolean(manager),"world first-person pass restored after doll");
  var extract=fuzs.paperdoll.common.client.util.PaperDollRenderer.class.getDeclaredMethod("extractRenderState",LivingEntity.class,float.class);extract.setAccessible(true);
  var state=(net.minecraft.client.renderer.entity.state.AvatarRenderState)extract.invoke(null,p,.5f);
  check(((com.zigythebird.playeranim.accessors.IAvatarAnimationState)state).playerAnimLib$getAnimManager()==manager,"doll shares live third-person animation source");
 }
 void tick(ClientTickEvent.Post event){var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null)return;ticks++;
  if(mc.gameDirectory.getName().equals("observer")){observer();return;}
  if(ticks==70){mc.getSingleplayerServer().execute(()->{var server=mc.getSingleplayerServer();server.setUsesAuthentication(false);server.publishServer(net.minecraft.server.MinecraftServer.MultiplayerScope.LAN,net.minecraft.world.level.GameType.CREATIVE,false,25578);var p=server.getPlayerList().getPlayer(mc.player.getUUID());p.setInvulnerable(true);p.teleportTo(0,100,0);server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),"time set day");});}
  if(ticks==400){var server=mc.getSingleplayerServer();server.execute(()->{var p=server.getPlayerList().getPlayer(mc.player.getUUID());var c=net.spell_engine.internals.PaladinUltimates.channelOf(p);net.spell_engine.internals.PaladinUltimates.startChannel(p,server.overworld().getGameTime()-420);c=net.spell_engine.internals.PaladinUltimates.channelOf(p);c.pulses=42;net.spell_engine.internals.PaladinUltimates.end(p,net.spell_engine.internals.PaladinUltimates.End.RELEASE);});}
  if(ticks>=402&&ticks<=412&&ticks%5==2){pending=true;results.add("SCREENSHOT Meteor Purgatory "+ticks);}
  if(ticks>350&&ticks%20==0){var server=mc.getSingleplayerServer();server.execute(()->{var host=server.getPlayerList().getPlayer(mc.player.getUUID());host.setGameMode(net.minecraft.world.level.GameType.CREATIVE);host.getAbilities().flying=true;host.onUpdateAbilities();host.teleportTo(0,100,0);for(var other:server.getPlayerList().getPlayers())if(!other.getUUID().equals(mc.player.getUUID())){other.setInvulnerable(true);other.setGameMode(net.minecraft.world.level.GameType.CREATIVE);other.getAbilities().flying=true;other.onUpdateAbilities();other.teleportTo(0,100,5);other.setYRot(180);other.setXRot(0);}});}

  if(ticks==60){try{proof();}catch(Throwable t){t.printStackTrace();results.add("FAIL "+t);}try{Files.write(mc.gameDirectory.toPath().resolve("render-checks.txt"),results);}catch(Exception e){throw new RuntimeException(e);}mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);}
  if(ticks>=80&&ticks<320&&ticks%60==20){
   phase++;String item=phase<=2?"archers:heavy_crossbow":"";
   if(phase==3||phase==4)item="arsenal:unique_hammer_1";
   final String key=item;final boolean loaded=phase==2;
   var server=mc.getSingleplayerServer();server.execute(()->{var p=server.getPlayerList().getPlayer(mc.player.getUUID());var stack=new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(key)));if(loaded)stack.set(DataComponents.CHARGED_PROJECTILES,ChargedProjectiles.of(new ItemStackTemplate(Items.ARROW)));p.setItemSlot(EquipmentSlot.MAINHAND,stack);});
   mc.options.setCameraType(phase==4?CameraType.FIRST_PERSON:CameraType.THIRD_PERSON_FRONT);mc.player.setYRot(0);mc.player.setXRot(0);
  }
  if(ticks>=100&&ticks<340&&ticks%60==40){pending=true;results.add("SCREENSHOT phase "+phase);
   var renderer=mc.getEntityRenderDispatcher().getRenderer(mc.player);var state=(net.minecraft.client.renderer.entity.state.AvatarRenderState)renderer.createRenderState(mc.player,.5f);results.add("STATE phase "+phase+" arms="+state.rightArmPose+","+state.leftArmPose+" BC="+net.bettercombat.logic.PlayerAttackHelper.poseForPlayer(mc.player));}
  if(ticks==5000){try{Files.write(mc.gameDirectory.toPath().resolve("render-checks.txt"),results);Files.writeString(mc.gameDirectory.toPath().resolve("render-result.txt"),results.stream().anyMatch(s->s.startsWith("FAIL"))?"FAIL":"PASS");}catch(Exception e){throw new RuntimeException(e);}mc.stop();}
 }
 void observer(){var mc=Minecraft.getInstance();mc.options.setCameraType(CameraType.FIRST_PERSON);mc.player.setYRot(180);mc.player.setXRot(0);
  var remote=mc.level.players().stream().filter(p->p!=mc.player).findFirst().orElse(null);if(remote!=null&&ticks%60==0){var renderer=mc.getEntityRenderDispatcher().getRenderer(remote);var state=(net.minecraft.client.renderer.entity.state.AvatarRenderState)renderer.createRenderState(remote,.5f);check(((com.zigythebird.playeranim.accessors.IAvatarAnimationState)state).playerAnimLib$getAnimManager()!=null,"remote player has third-person animation manager");results.add("REMOTE pose="+((net.bettercombat.api.EntityPlayer_BetterCombat)remote).getMainHandIdleAnimation());pending=true;}
  if(ticks==600){try{Files.write(mc.gameDirectory.toPath().resolve("observer-checks.txt"),results);Files.writeString(mc.gameDirectory.toPath().resolve("observer-result.txt"),results.stream().anyMatch(s->s.startsWith("FAIL"))?"FAIL":"PASS");}catch(Exception e){throw new RuntimeException(e);}mc.stop();}
 }

}
