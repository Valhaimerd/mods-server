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
public class ClientGoldProbe {
 int ticks;boolean capture;volatile String failure;String label;List<String> captures=new ArrayList<>();
 public ClientGoldProbe(){NeoForge.EVENT_BUS.addListener(this::tick);NeoForge.EVENT_BUS.addListener(this::frame);}
 void frame(RenderFrameEvent.Post e){if(capture){Screenshot.grab(Minecraft.getInstance(),false);captures.add(label);capture=false;}}
 void equip(net.minecraft.server.level.ServerPlayer p,String...ids){var owner=(SpellContainerSource.Owner)p;var old=owner.getSpellContainers();var container=new net.spell_engine.api.spell.container.SpellContainer(net.spell_engine.api.spell.container.SpellContainer.ContentType.NONE,"","",0,List.of(ids));var modifiers=Arrays.stream(ids).map(id->(net.minecraft.core.Holder<net.spell_engine.api.spell.Spell>)SpellRegistry.from(p.level()).get(Identifier.parse(id)).orElseThrow()).toList();owner.setSpellContainers(new SpellContainerSource.Result(old.activeContainer(),List.of(),List.of(),modifiers,List.of(new SpellContainerSource.SourcedContainer("visual_probe",ItemStack.EMPTY,container))));owner.spellModifierCache().clear();}
 void server(java.util.function.Consumer<net.minecraft.server.level.ServerPlayer> action){var mc=Minecraft.getInstance();mc.getSingleplayerServer().execute(()->{try{action.accept(mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID()));}catch(Throwable t){failure=t.toString();t.printStackTrace();}});}
 void shot(String name){label=name;capture=true;}
 void tick(ClientTickEvent.Post e){var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null)return;ticks++;
  if(ticks==30){mc.options.pauseOnLostFocus=false;mc.options.setCameraType(CameraType.FIRST_PERSON);server(p->{
   var w=p.level();p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);p.getAbilities().flying=false;p.onUpdateAbilities();p.setInvulnerable(true);p.setNoGravity(false);p.teleportTo(0,100,0);p.setYRot(0);p.setXRot(0);
   for(int x=-13;x<=13;x++)for(int z=-13;z<=13;z++){w.setBlock(new net.minecraft.core.BlockPos(x,99,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3);for(int y=100;y<108;y++)w.setBlock(new net.minecraft.core.BlockPos(x,y,z),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);}
   w.getServer().getCommands().performPrefixedCommand(w.getServer().createCommandSourceStack(),"time set day");
   var c=net.puffish.skillsmod.api.SkillsAPI.getCategory(Identifier.parse("skill_tree_rpgs:class_skills")).orElseThrow();c.resetSkills(p);c.setExtraPoints(p,1000);c.getSkill("tdimeso8ajcwiygp").orElseThrow().unlock(p);
   p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(Identifier.parse("arsenal:hammer_of_destiny"))));
  });}
  if(ticks==60){var camera=net.minecraft.world.entity.EntityTypes.ARMOR_STAND.create(mc.level,net.minecraft.world.entity.EntitySpawnReason.COMMAND);camera.setId(-1773);camera.setPos(0,115,-12);camera.setYRot(0);camera.setXRot(50);camera.setNoGravity(true);mc.level.addEntity(camera);mc.setCameraEntity(camera);}
  if(ticks==80)server(p->PaladinRevamp.protect(p.level(),PaladinRevamp.spell(p,"paladins:divine_protection"),p,List.of(),new SpellExecution.ImpactContext(),null));
  if(ticks==87)shot("divine-protection-radius8");
  if(ticks==120)server(p->PaladinUltimates.startBanner(p));
  if(ticks==140)shot("battle-banner-radius6");
  if(ticks==155)server(p->p.teleportTo(3,100,0));
  if(ticks==170)shot("battle-banner-moved");
  if(ticks==185)server(p->{PaladinUltimates.cleanup(p);p.teleportTo(0,100,0);equip(p,"skill_tree_rpgs:paladin_tier_4_spell_1_root");PaladinUltimates.startBanner(p);});
  if(ticks==200)shot("improved-banner-radius8");
  if(ticks==220)server(p->{PaladinUltimates.cleanup(p);equip(p,"skill_tree_rpgs:paladin_tier_2_spell_1_modifier_1");var target=net.minecraft.world.entity.EntityTypes.HUSK.create(p.level(),net.minecraft.world.entity.EntitySpawnReason.COMMAND);target.setPos(0,100,3);target.setNoAi(true);target.setInvulnerable(false);p.level().addFreshEntity(target);PaladinTierOne.purge(p.level(),PaladinRevamp.spell(p,"paladins:flash_heal"),p,List.of(new SpellExecution.DeliveryTarget(target,new SpellExecution.ImpactContext())),new SpellExecution.ImpactContext(),null);});
  if(ticks==227)shot("divine-strength-radius4");
  if(ticks==260)server(p->{equip(p,"skill_tree_rpgs:paladin_tier_2_spell_2_modifier_2");PaladinTierOne.prepare(p.level(),PaladinRevamp.spell(p,"paladins:blessed_strikes"),p,List.of(),new SpellExecution.ImpactContext(1,1,null,null,net.spell_engine.internals.target.SpellTarget.FocusMode.DIRECT,4),null);});
  if(ticks==267)shot("seal-of-light-final-radius4");
  if(ticks==300)server(p->{equip(p);PaladinRevamp.launchOath(p.level(),PaladinRevamp.spell(p,"paladins:judgement"),p,List.of(),new SpellExecution.ImpactContext(),null);});
  if(ticks==308)shot("divine-oath-falling-hammer");
  if(ticks==320)shot("divine-oath-impact");
  if(ticks==345)server(p->{equip(p,"skill_tree_rpgs:paladin_tier_3_spell_2_modifier_2");PaladinRevamp.launchOath(p.level(),PaladinRevamp.spell(p,"paladins:judgement"),p,List.of(),new SpellExecution.ImpactContext(),null);});
  if(ticks==365)shot("judgement-impact-radius6");
  if(ticks==395)shot("expired-areas");
  if(ticks==420){try{Files.writeString(mc.gameDirectory.toPath().resolve("gold-client-result.txt"),failure==null?"PASS native Paladin gold events and cleanup; inspect captures":"FAIL "+failure);Files.write(mc.gameDirectory.toPath().resolve("gold-captures.txt"),captures);}catch(Exception x){throw new RuntimeException(x);}mc.stop();}
 }
}
