package local.aurapreview;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import vazkii.patchouli.api.PatchouliAPI;
@Mod("aura_preview_probe")
public class PreviewProbe {
 int ticks;boolean hidden,done;List<String> checks=new ArrayList<>();
 String[] pages={"animals/animal_cow","animals/animal_pig","animals/animal_sheep","animals/animal_wolf","flying_animals/animal_bee","water_animals/fish_tropical_fish","water_animals/animal_turtle","animals/animal_goat"};
 Identifier book=Identifier.parse("animal_pen:guide_book");
 public PreviewProbe(){NeoForge.EVENT_BUS.addListener(this::tick);}
 void check(boolean ok,String label){if(!ok)throw new AssertionError(label);checks.add("PASS "+label);}
 Entity create(String id){return vazkii.patchouli.common.util.EntityUtil.loadEntity(id).apply(Minecraft.getInstance().level);}
 boolean unassigned(Entity entity){try{entity.getId();return false;}catch(IllegalStateException e){return e.getMessage().contains("before ID assignment");}}
 @SuppressWarnings({"rawtypes","unchecked"})
 void tick(ClientTickEvent.Post event){var mc=Minecraft.getInstance();if(!hidden){org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());hidden=true;}if(done||mc.player==null||mc.level==null)return;ticks++;
 try{
  if(ticks==40){
   mc.setScreenAndShow(null);var preview=create("minecraft:cow");check(!preview.isAddedToLevel(),"book preview cow stays outside the world");
   EntityRenderer renderer=mc.getEntityRenderDispatcher().getRenderer(preview);EntityRenderState state=renderer.createRenderState(preview,0);
   check(state!=null,"unspawned cow preview renders safely without Auras");check(preview.getId()<0,"preview uses an isolated negative ID");check(mc.level.getEntity(preview.getId())==null,"preview is not registered or ticking in the world");check(create("minecraft:cow").getId()!=preview.getId(),"separate book previews receive distinct IDs");int previewId=preview.getId();for(int n=0;n<30;n++)renderer.createRenderState(preview,0);check(preview.getId()==previewId,"preview identity stays stable across repeated frames");
   var worldCow=create("minecraft:cow");worldCow.setId(900001);mc.level.addEntity(worldCow);check(worldCow.isAddedToLevel(),"world cow fixture is registered");
   EntityRenderState reused=renderer.createRenderState(worldCow,0);check(reused!=null&&worldCow.getId()==900001,"real world entity still renders with its assigned ID");renderer.extractRenderState(preview,reused,0);check(reused!=null,"world render state can be reused for an unspawned preview");mc.level.removeEntity(900001,Entity.RemovalReason.DISCARDED);
   EntityRenderer playerRenderer=mc.getEntityRenderDispatcher().getRenderer(mc.player);check(playerRenderer.createRenderState(mc.player,0)!=null,"normal player rendering preserved");
   for(String id:List.of("minecraft:pig","minecraft:sheep","minecraft:wolf","minecraft:bee","minecraft:tropical_fish","minecraft:turtle","minecraft:goat","minecraft:chicken","minecraft:horse","minecraft:axolotl","minecraft:armor_stand")){
    var entity=create(id);check(!entity.isAddedToLevel()&&entity.getId()<0,id+" preview remains detached with a negative ID");EntityRenderer r=mc.getEntityRenderDispatcher().getRenderer(entity);check(r.createRenderState(entity,0)!=null,id+" preview renders safely");
   }
  }
  for(int n=0;n<pages.length;n++){
   if(ticks==70+n*20)PatchouliAPI.get().openBookEntry(book,Identifier.parse("animal_pen:"+pages[n]),0);
   if(ticks==85+n*20){check(mc.gui.screen() instanceof vazkii.patchouli.client.book.gui.GuiBookEntry,"actual Animal Pen guidebook entry rendered "+pages[n]);check(book.equals(PatchouliAPI.get().getOpenBookGui()),"correct guidebook remains open "+pages[n]);if(n==0)Screenshot.grab(mc,false);}
  }
  if(ticks==240)finish("PASS "+checks.size()+" preview/world-ID/guidebook checks");
 }catch(Throwable t){t.printStackTrace();finish("FAIL "+t);}}
 void finish(String result){done=true;var mc=Minecraft.getInstance();try{Files.writeString(mc.gameDirectory.toPath().resolve("preview-result.txt"),result);Files.write(mc.gameDirectory.toPath().resolve("preview-checks.txt"),checks);}catch(Exception e){throw new RuntimeException(e);}mc.stop();}
}
