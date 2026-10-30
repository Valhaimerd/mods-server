package local.vanishprobe;
import java.nio.file.*;import java.util.*;
import net.minecraft.client.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.*;
import net.spell_engine.internals.*;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.internals.container.SpellContainerSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
@Mod(value="vanish_render_probe",dist=Dist.CLIENT)
public class ClientShoutProbe {
 int ticks;boolean capture;volatile String failure;String label;List<String> captures=new ArrayList<>();
 public ClientShoutProbe(){NeoForge.EVENT_BUS.addListener(this::tick);NeoForge.EVENT_BUS.addListener(this::frame);}
 void frame(RenderFrameEvent.Post e){if(capture){Screenshot.grab(Minecraft.getInstance(),false);captures.add(label);capture=false;}}
 void equip(net.minecraft.server.level.ServerPlayer p,String...ids){var owner=(SpellContainerSource.Owner)p;var old=owner.getSpellContainers();var container=new net.spell_engine.api.spell.container.SpellContainer(net.spell_engine.api.spell.container.SpellContainer.ContentType.NONE,"","",0,List.of(ids));var modifiers=Arrays.stream(ids).map(id->(net.minecraft.core.Holder<net.spell_engine.api.spell.Spell>)SpellRegistry.from(p.level()).get(Identifier.parse(id)).orElseThrow()).toList();owner.setSpellContainers(new SpellContainerSource.Result(old.activeContainer(),List.of(),modifiers.stream().filter(h->h.value().type==net.spell_engine.api.spell.Spell.Type.PASSIVE).toList(),modifiers.stream().filter(h->h.value().type==net.spell_engine.api.spell.Spell.Type.MODIFIER).toList(),List.of(new SpellContainerSource.SourcedContainer("visual_probe",ItemStack.EMPTY,container))));owner.spellModifierCache().clear();}
 void server(java.util.function.Consumer<net.minecraft.server.level.ServerPlayer> action){var mc=Minecraft.getInstance();mc.getSingleplayerServer().execute(()->{try{action.accept(mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID()));}catch(Throwable t){failure=t.toString();t.printStackTrace();}});}
 void cast(boolean improved,String branch){server(p->{var nodes=new ArrayList<String>();if(improved)nodes.add("skill_tree_rpgs:warrior_tier_3_spell_2_root");if(!branch.isEmpty())nodes.add(branch);equip(p,nodes.toArray(String[]::new));var h=SpellRegistry.from(p.level()).get(Identifier.parse("rogues:shout")).orElseThrow();double expected=improved?16:12;if(SpellParameters.getRange(p,h)!=expected)throw new AssertionError("Range fixture");net.spell_engine.fx.ReleaseFx.send(p.level(),p,h,1);});}
 void shot(String name){label=name;capture=true;}
 void tick(ClientTickEvent.Post e){var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null)return;ticks++;
  if(ticks==30){mc.options.pauseOnLostFocus=false;mc.options.chatVisibility().set(net.minecraft.world.entity.player.ChatVisiblity.HIDDEN);mc.options.setCameraType(CameraType.FIRST_PERSON);server(p->{
   var w=p.level();p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);p.getAbilities().flying=false;p.onUpdateAbilities();p.setInvulnerable(true);p.setNoGravity(false);p.teleportTo(0,100,0);p.setYRot(0);p.setXRot(0);
   for(int x=-21;x<=21;x++)for(int z=-21;z<=21;z++){w.setBlock(new net.minecraft.core.BlockPos(x,99,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3);for(int y=100;y<105;y++)w.setBlock(new net.minecraft.core.BlockPos(x,y,z),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);}
   w.getServer().getCommands().performPrefixedCommand(w.getServer().createCommandSourceStack(),"time set day");w.getServer().getCommands().performPrefixedCommand(w.getServer().createCommandSourceStack(),"weather clear");
   p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(net.minecraft.world.item.Items.IRON_AXE));
  });}
  if(ticks==60){var camera=net.minecraft.world.entity.EntityTypes.ARMOR_STAND.create(mc.level,net.minecraft.world.entity.EntitySpawnReason.COMMAND);camera.setId(-1773);camera.setPos(0,119,-17);camera.setYRot(0);camera.setXRot(48);camera.setNoGravity(true);mc.level.addEntity(camera);mc.setCameraEntity(camera);}
  if(ticks==80)cast(false,"");if(ticks==87)shot("shout-radius12");
  if(ticks==140)cast(true,"");if(ticks==147)shot("improved-shout-radius16");
  if(ticks==200)cast(true,"skill_tree_rpgs:warrior_tier_3_spell_2_modifier_1");if(ticks==207)shot("battle-shout-radius16");
  if(ticks==260)cast(true,"skill_tree_rpgs:warrior_tier_3_spell_2_modifier_2");if(ticks==267)shot("challenging-shout-radius16");
  if(ticks==310)shot("expired-shout-footprint");
  if(ticks==335){try{Files.writeString(mc.gameDirectory.toPath().resolve("shout-client-result.txt"),failure==null?"PASS native red Shout releases at12/16blocks, both branches and expiry; inspect captures":"FAIL "+failure);Files.write(mc.gameDirectory.toPath().resolve("shout-captures.txt"),captures);}catch(Exception x){throw new RuntimeException(x);}mc.stop();}
 }
}
