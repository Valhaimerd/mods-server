package local.skilltooltips;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.spell_engine.client.gui.SpellTooltip;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.neoforged.fml.common.Mod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
@Mod(value="skill_tooltip_probe",dist=Dist.CLIENT)
public class ClientTooltipProbe {
 int ticks;boolean done,hidden;List<String> results=new ArrayList<>();
 public ClientTooltipProbe(){NeoForge.EVENT_BUS.addListener(this::tick);}
 void check(boolean ok,String text){if(!ok)throw new AssertionError(text);results.add("PASS "+text);}
 void tick(ClientTickEvent.Post event){
  var mc=Minecraft.getInstance();if(!hidden){org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());hidden=true;}
  if(done||mc.player==null||mc.level==null||++ticks<40)return;done=true;
  try{
   var lang=mc.getResourceManager();
   String[] ids={"warrior_tier_3_passive_2","warrior_tier_1_passive_2","warrior_tier_2_passive_2","warrior_tier_3_passive_1","rogue_tier_2_passive_2","archer_tier_3_passive_1","fire_tier_1_passive_2","arcane_tier_3_passive_1"};
   for(String id:ids){
    var lines=SpellTooltip.spellDescriptionWithDetails(Identifier.parse("skill_tree_rpgs:"+id),mc.player,ItemStack.EMPTY,0);
    var text=lines.stream().map(Component::getString).toList();results.add(id+" "+text);
    check(text.stream().noneMatch(s->s.contains("Format error")||s.contains("percentage")||s.contains("{impact")),id+" readable formatted text");
    check(text.stream().filter(s->s.startsWith("On ")||s.startsWith("At ")).count()==1,id+" exactly one custom trigger");
    check(text.stream().noneMatch(s->s.contains("Triggered by")),id+" no generic duplicate trigger");
    check(text.stream().filter(s->s.startsWith("Cooldown:")).count()<=1,id+" at most one dynamic cooldown");
   }
   for(String[] pair:new String[][]{{"warrior_tier_3_passive_2","Tenacity"},{"warrior_tier_1_passive_2","Endured Vitality"},{"warrior_tier_2_passive_1","Warm Up"},{"warrior_tier_2_passive_2","Momentum"},{"warrior_tier_4_spell_1_root","Improved Berserker"},{"warrior_tier_4_spell_2_modifier_2","Sentinel"},{"archer_tier_4_spell_2_root","Improved Magic Arrows"},{"rogue_tier_3_passive_2","Prepared Counter"}}){
    check(I18n.get("skill.skill_tree_rpgs."+pair[0]+".title").equals(pair[1]),"Native tree title "+pair[1]);
   }
   String supercharge=I18n.get("spell.skill_tree_rpgs.archer_tier_3_passive_1.description");check(supercharge.contains("125%"),"Supercharge renders 125%");
   var spell=SpellRegistry.from(mc.level).get(Identifier.parse("skill_tree_rpgs:archer_tier_3_passive_1")).orElseThrow();check(Math.abs(spell.value().arrow_perks.damage_multiplier-1.25)<.0001,"Native Supercharge multiplier 1.25");
   String tenacity=String.join("\n",SpellTooltip.spellDescriptionWithDetails(Identifier.parse("skill_tree_rpgs:warrior_tier_3_passive_2"),mc.player,ItemStack.EMPTY,0).stream().map(Component::getString).toList());
   check(tenacity.contains("At 1-30% HP"),"Tenacity native health trigger");
   Files.write(mc.gameDirectory.toPath().resolve("tooltip-results.txt"),results);Files.writeString(mc.gameDirectory.toPath().resolve("tooltip-result.txt"),"PASS "+results.stream().filter(s->s.startsWith("PASS")).count()+" native client tooltip checks\n");
  }catch(Throwable t){t.printStackTrace();try{Files.writeString(mc.gameDirectory.toPath().resolve("tooltip-result.txt"),"FAIL "+t);Files.write(mc.gameDirectory.toPath().resolve("tooltip-results.txt"),results);}catch(Exception ignored){}}
  mc.stop();
 }
}
