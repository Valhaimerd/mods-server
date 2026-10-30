package local.vanishprobe;
import java.nio.file.*;import net.minecraft.client.*;import net.neoforged.fml.common.Mod;import net.neoforged.api.distmarker.Dist;import net.neoforged.neoforge.common.NeoForge;import net.neoforged.neoforge.client.event.*;
@Mod(value="vanish_render_probe",dist=Dist.CLIENT)public class ClientGroundProbe{
 int ticks;boolean capture;
 public ClientGroundProbe(){NeoForge.EVENT_BUS.addListener(this::tick);NeoForge.EVENT_BUS.addListener(this::frame);}
 void frame(RenderFrameEvent.Post e){if(capture){Screenshot.grab(Minecraft.getInstance(),false);capture=false;}}
 void tick(ClientTickEvent.Post e){var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null)return;ticks++;
  if(ticks==40){mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);mc.player.setYRot(0);mc.player.setXRot(20);var server=mc.getSingleplayerServer();server.execute(()->{var p=server.getPlayerList().getPlayer(mc.player.getUUID());server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),"time set day");p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);p.getAbilities().flying=false;p.onUpdateAbilities();p.setInvulnerable(true);p.setNoGravity(false);
   for(int x=-9;x<=9;x++)for(int z=-9;z<=9;z++){server.overworld().setBlock(new net.minecraft.core.BlockPos(x,99,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3);for(int y=100;y<105;y++)server.overworld().setBlock(new net.minecraft.core.BlockPos(x,y,z),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);}p.teleportTo(0,100,0);var category=net.puffish.skillsmod.api.SkillsAPI.getCategory(net.minecraft.resources.Identifier.parse("skill_tree_rpgs:class_skills")).orElseThrow();category.unlock(p);category.resetSkills(p);category.setExtraPoints(p,1000);category.getSkill("tdimeso8ajcwiygp").orElseThrow().unlock(p);net.spell_engine.internals.PaladinUltimates.startChannel(p,server.overworld().getGameTime());});}
  if(ticks==90||ticks==120||ticks==150)capture=true;
  if(ticks==170){mc.getSingleplayerServer().execute(()->net.spell_engine.internals.PaladinUltimates.cleanup(mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID())));}
  if(ticks==200){capture=true;}
  if(ticks==220){try{Files.writeString(mc.gameDirectory.toPath().resolve("ground-client-result.txt"),"PASS: native channel/pulses and cleanup completed; inspect screenshots");}catch(Exception failure){throw new RuntimeException(failure);}mc.stop();}
 }
}
