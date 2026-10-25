package local.ultimatearmor;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.spell_engine.api.effect.*;
import net.spell_engine.entity.*;
import net.spell_engine.client.render.extension.EntityRenderStateExtension;
import net.spell_engine.client.render.tint.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
@Mod(value="ultimate_armor_probe",dist=Dist.CLIENT)
public class ClientArmorProbe {
 int ticks;boolean hidden,done;List<String> results=Collections.synchronizedList(new ArrayList<>());
 public ClientArmorProbe(){NeoForge.EVENT_BUS.addListener(this::tick);}
 void check(boolean value,String text){results.add((value?"PASS ":"FAIL ")+text);}
 net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect(String id){return BuiltInRegistries.MOB_EFFECT.get(Identifier.parse(id)).orElseThrow();}
 void server(java.util.function.Consumer<net.minecraft.server.level.ServerPlayer> task){var mc=Minecraft.getInstance();mc.getSingleplayerServer().execute(()->{try{task.accept(mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID()));}catch(Throwable t){results.add("FAIL server "+t);}});}
 void armorTint(int expected){var mc=Minecraft.getInstance();check(EntityTints.currentTint(mc.player)==expected,"synchronized model tint "+Integer.toHexString(expected));var state=new LivingEntityRenderState();((EntityRenderStateExtension)state).spellEngine_setTint(expected);EntityTintPass.begin(state);check(EntityTintPass.apply(0xFFFFFFFF)==expected,"white armor layer receives model tint");check(EntityTintPass.apply(0xFF4080C0)==EntityTints.multiply(0xFF4080C0,expected),"dyed armor composes with tint");EntityTintPass.end();check(EntityTintPass.apply(-1)==-1,"equipment tint cannot leak to next entity");}
 void glint(boolean expected){var mc=Minecraft.getInstance();var state=new LivingEntityRenderState();LastStandGlint.capture(mc.player,state);check(LastStandGlint.active(state)==expected,"glint state active="+expected);check(!LastStandGlint.active(new LivingEntityRenderState()),"unrelated render state has no glint");
  var queue=new net.minecraft.client.renderer.SubmitNodeCollection();
  var base=net.minecraft.client.renderer.rendertype.RenderTypes.armorCutoutNoCull(Identifier.parse("minecraft:textures/entity/equipment/humanoid/iron.png"));
  queue.submitModel(null,state,new com.mojang.blaze3d.vertex.PoseStack(),base,15728880,0,-1,null,0,null);
  check(!queue.solid.isEmpty(),"native armor model is still submitted");
  check(queue.translucentModels.isEmpty()!=expected,"native armor submission appends gold glint="+expected);
  queue=new net.minecraft.client.renderer.SubmitNodeCollection();
  queue.submitModel(null,state,new com.mojang.blaze3d.vertex.PoseStack(),net.minecraft.client.renderer.rendertype.RenderTypes.armorEntityGlint(),15728880,0,-1,null,0,null);
  check(queue.translucentModels.isEmpty()==expected,"purple enchantment glint suppressed only during Last Stand="+expected);
 }
 void tick(ClientTickEvent.Post event){
  var mc=Minecraft.getInstance();if(!hidden){org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());hidden=true;}
  if(done||mc.player==null||mc.level==null||++ticks<40)return;
  try{
   if(ticks==40){mc.setScreenAndShow(null);mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);server(p->{p.removeAllEffects();p.getInventory().clearContent();p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);p.level().getServer().getCommands().performPrefixedCommand(p.createCommandSourceStack(),"tp @s 200 -60 200 180 0");p.level().getServer().getCommands().performPrefixedCommand(p.createCommandSourceStack(),"time set noon");p.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.IRON_HELMET));p.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.IRON_CHESTPLATE));p.setItemSlot(EquipmentSlot.LEGS,new ItemStack(Items.IRON_LEGGINGS));p.setItemSlot(EquipmentSlot.FEET,new ItemStack(Items.IRON_BOOTS));p.addEffect(new MobEffectInstance(effect(WarriorUltimates.BERSERKER),280,4));});}
   if(ticks==90){armorTint(BerserkerAppearance.RED_TINT);glint(false);Screenshot.grab(mc,false);server(p->{WarriorUltimates.fatal(p);p.removeAllEffects();p.addEffect(new MobEffectInstance(effect("rogues:last_stand"),400,4));});}
   if(ticks==130){armorTint(LastStandAppearance.GOLD_TINT);glint(true);check(CustomParticleStatusEffect.spawnerOf(effect("rogues:last_stand").value())==null,"Last Stand does not use Berserker sparkles");check(!mc.player.getItemBySlot(EquipmentSlot.CHEST).hasFoil(),"gold glint works without enchanting equipped armor");Screenshot.grab(mc,false);}
   if(ticks==145)Screenshot.grab(mc,false);
   if(ticks==150)server(p->p.removeEffect(effect("rogues:last_stand")));
   if(ticks==175){armorTint(-1);glint(false);Screenshot.grab(mc,false);server(p->p.addEffect(new MobEffectInstance(effect("rogues:last_stand"),12)));}
   if(ticks==215){armorTint(-1);glint(false);server(p->p.addEffect(new MobEffectInstance(effect("rogues:last_stand"),400,4)));}
   if(ticks==240){armorTint(LastStandAppearance.GOLD_TINT);glint(true);server(WarriorUltimates::clearOnDeath);}
   if(ticks>=265){armorTint(-1);glint(false);check(LastStandAppearance.GOLD_TINT==EntityTints.scaled(0xFFFFD700,.3f),"gold tint is fixed 30 percent");finish();}
  }catch(Throwable t){results.add("FAIL "+t);t.printStackTrace();finish();}
 }
 void finish(){done=true;var mc=Minecraft.getInstance();try{Files.write(mc.gameDirectory.toPath().resolve("visual-results.txt"),results);Files.writeString(mc.gameDirectory.toPath().resolve("visual-result.txt"),(results.stream().anyMatch(s->s.startsWith("FAIL"))?"FAIL":"PASS")+" "+results.stream().filter(s->s.startsWith("PASS")).count()+" checks\n");}catch(Exception e){throw new RuntimeException(e);}mc.stop();}
}
