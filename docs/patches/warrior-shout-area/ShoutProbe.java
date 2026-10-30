package local.paladinprobe;
import java.nio.file.*;import java.util.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.spell_engine.api.spell.*;
import net.spell_engine.api.spell.fx.*;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.internals.*;
import net.spell_engine.internals.container.SpellContainerSource;
import net.spell_engine.fx.ReleaseFx;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
@Mod("paladin_doctrine_probe")
public class ShoutProbe {
 public static final List<ParticleGroup> visuals=new ArrayList<>();List<String> checks=new ArrayList<>();int failures;
 public static void capture(Entity e,List<ParticleGroup> groups){visuals.addAll(groups);}
 void check(boolean b,String label){checks.add((b?"PASS ":"FAIL ")+label);if(!b)failures++;}
 void equip(ServerPlayer p,String...ids){var owner=(SpellContainerSource.Owner)p;var old=owner.getSpellContainers();var container=new net.spell_engine.api.spell.container.SpellContainer(net.spell_engine.api.spell.container.SpellContainer.ContentType.NONE,"","",0,List.of(ids));var all=Arrays.stream(ids).map(id->(net.minecraft.core.Holder<Spell>)SpellRegistry.from(p.level()).get(Identifier.parse(id)).orElseThrow()).toList();owner.setSpellContainers(new SpellContainerSource.Result(old.activeContainer(),List.of(),all.stream().filter(h->h.value().type==Spell.Type.PASSIVE).toList(),all.stream().filter(h->h.value().type==Spell.Type.MODIFIER).toList(),List.of(new SpellContainerSource.SourcedContainer("visual_probe",ItemStack.EMPTY,container))));owner.spellModifierCache().clear();}
 public ShoutProbe(){NeoForge.EVENT_BUS.addListener(this::run);}
 void run(ServerStartedEvent e){var server=e.getServer();try{
  var p=net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(server.overworld());var h=SpellRegistry.from(p.level()).get(Identifier.parse("rogues:shout")).orElseThrow();
  for(boolean improved:new boolean[]{false,true})for(String branch:new String[]{"","skill_tree_rpgs:warrior_tier_3_spell_2_modifier_1","skill_tree_rpgs:warrior_tier_3_spell_2_modifier_2"}){
   var ids=new ArrayList<String>();if(improved)ids.add("skill_tree_rpgs:warrior_tier_3_spell_2_root");if(!branch.isEmpty())ids.add(branch);equip(p,ids.toArray(String[]::new));
   double radius=improved?16:12;check(SpellParameters.getRange(p,h)==radius,"actual radius "+radius+" branch "+branch);
   visuals.clear();ReleaseFx.send(p.level(),p,h,1);
   var rings=visuals.stream().filter(g->g.id.equals("spell_engine:area_circle_1")).toList();check(rings.size()==1,"one red perimeter on native release, no targets required");
   if(!rings.isEmpty()){var g=rings.getFirst();check(g.appearance.scale==radius,"perimeter matches modified range");check(g.appearance.color==0xD94C45FFL&&Boolean.TRUE.equals(g.appearance.glow),"red emissive contrast");check(g.appearance.facing==ParticleGroup.Facing.GROUND&&g.batch.anchor==ParticleGroup.Anchor.GROUND,"ground facing and anchored");check(g.appearance.attachment==ParticleGroup.Attachment.NONE,"instant Shout footprint stays at cast location");check(g.appearance.scale_with==Fx.ScaleWith.NONE,"range scaling resolved exactly once");}
   check(visuals.stream().anyMatch(g->g.id.equals("spell_engine:area_effect_293")&&g.appearance.scale==radius),"soft red ground illumination follows range");
  }
  check(h.value().range==12&&h.value().cost.cooldown.duration==16,"base range/cooldown preserved");check(h.value().release.sound.id().equals("rogues:shout_release"),"native shout sound preserved");check(h.value().impacts.size()==3,"native debuff/control impacts preserved");
 }catch(Throwable t){failures++;checks.add("ERROR "+t);t.printStackTrace();}finally{try{Files.write(Path.of("paladin-checks.txt"),checks);Files.writeString(Path.of("paladin-result.txt"),(failures==0?"PASS ":"FAIL ")+checks.size()+" checks; failures="+failures);}catch(Exception x){throw new RuntimeException(x);}server.halt(false);}}
}
