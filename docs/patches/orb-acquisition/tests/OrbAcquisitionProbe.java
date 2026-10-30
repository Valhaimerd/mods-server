package local.orbacquisition;
import java.nio.file.*;
import java.util.*;
import com.eiden.iourusraces.race.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.GameType;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.puffish.skillsmod.api.SkillsAPI;
@Mod("orb_acquisition_probe")
public class OrbAcquisitionProbe {
 boolean started,hidden; volatile boolean done; volatile String result; List<String> checks=new ArrayList<>();
 public OrbAcquisitionProbe(){NeoForge.EVENT_BUS.addListener(this::tick);}
 void check(boolean b,String s){if(!b)throw new AssertionError(s);checks.add("PASS "+s);}
 void tick(ClientTickEvent.Post e){var mc=Minecraft.getInstance();if(!hidden){org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());hidden=true;}if(done){try{Files.writeString(mc.gameDirectory.toPath().resolve("orb-result.txt"),result);Files.write(mc.gameDirectory.toPath().resolve("orb-checks.txt"),checks);}catch(Exception ex){throw new RuntimeException(ex);}mc.stop();return;}if(started||mc.player==null||mc.getSingleplayerServer()==null)return;started=true;
 mc.getSingleplayerServer().execute(()->{try{run(mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID()));result="PASS "+checks.size()+" native Orb acquisition/use checks";}catch(Throwable t){t.printStackTrace();result="FAIL "+t;}done=true;});}
 void run(net.minecraft.server.level.ServerPlayer p)throws Exception{
 p.setGameMode(GameType.SURVIVAL);for(var id:net.skill_tree_rpgs.utils.SkillHelper.RESET_CATEGORIES)SkillsAPI.getCategory(id).ifPresent(c->c.resetSkills(p));
 var category=SkillsAPI.getCategory(Identifier.parse("adventurer_skills:adventurer_skills")).orElseThrow();category.setExtraPoints(p,1000);category.getSkill("6xmqgm3uw97hrynh").orElseThrow().unlock(p);check(category.getSpentPoints(p)>0,"skill fixture unlocked");
 var orb=BuiltInRegistries.ITEM.getValue(Identifier.parse("skill_tree_rpgs:orb_of_oblivion"));p.getInventory().setSelectedSlot(0);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(orb));PlayerRaceData.setRace(p,Race.ELF);p.experienceLevel=29;var stack=p.getMainHandItem();
 check(!orb.use(p.level(),p,InteractionHand.MAIN_HAND).consumesAction(),"29 levels block reset");check(p.experienceLevel==29,"insufficient reset costs zero");check(category.getSpentPoints(p)>0,"insufficient reset preserves skills");check(PlayerRaceData.getRace(p)==Race.ELF,"insufficient reset preserves race");check(!stack.isEmpty()&&stack.getDamageValue()==0,"insufficient reset preserves orb");
 p.experienceLevel=47;check(orb.use(p.level(),p,InteractionHand.MAIN_HAND).consumesAction(),"combined reset succeeds");check(p.experienceLevel==17,"combined reset debits exactly30levels");check(category.getSpentPoints(p)==0,"combined reset clears skills");check(PlayerRaceData.getRace(p)==Race.NONE,"combined reset clears race");
 p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(orb));p.experienceLevel=40;check(!orb.use(p.level(),p,InteractionHand.MAIN_HAND).consumesAction(),"no-op reset fails");check(p.experienceLevel==40&&p.getMainHandItem().getDamageValue()==0,"no-op spends zero levels or durability");
 PlayerRaceData.setRace(p,Race.DWARF);p.experienceLevel=30;check(orb.use(p.level(),p,InteractionHand.MAIN_HAND).consumesAction(),"race-only reset succeeds");check(PlayerRaceData.getRace(p)==Race.NONE&&p.experienceLevel==0,"race-only reset clears race and costs30levels");
 category.getSkill("6xmqgm3uw97hrynh").orElseThrow().unlock(p);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(orb));p.experienceLevel=30;check(orb.use(p.level(),p,InteractionHand.MAIN_HAND).consumesAction(),"skill-only reset succeeds");check(category.getSpentPoints(p)==0&&p.experienceLevel==0,"skill-only reset costs30levels");
 p.setGameMode(GameType.CREATIVE);PlayerRaceData.setRace(p,Race.HUMAN);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(orb));check(orb.use(p.level(),p,InteractionHand.MAIN_HAND).consumesAction(),"Creative bypass succeeds at0levels");check(p.experienceLevel==0&&p.getMainHandItem().getDamageValue()==0,"Creative preserves levels and orb durability");
 var manager=p.level().getServer().getRecipeManager();var key=ResourceKey.create(Registries.RECIPE,Identifier.parse("skill_tree_rpgs:orb_of_oblivion"));var holder=manager.byKey(key).orElseThrow();check(holder.value() instanceof ShapedRecipe,"native recipe manager exposes shaped Orb");var recipe=(ShapedRecipe)holder.value();check(recipe.getWidth()==3&&recipe.getHeight()==3,"native recipe dimensions3x3");
 var input=CraftingInput.of(3,3,List.of(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE),new ItemStack(Items.TOTEM_OF_UNDYING),new ItemStack(Items.ENCHANTED_GOLDEN_APPLE),new ItemStack(Items.TOTEM_OF_UNDYING),new ItemStack(Items.NETHERITE_INGOT),new ItemStack(Items.TOTEM_OF_UNDYING),new ItemStack(Items.ENCHANTED_GOLDEN_APPLE),new ItemStack(Items.TOTEM_OF_UNDYING),new ItemStack(Items.ENCHANTED_GOLDEN_APPLE)));
 check(recipe.matches(input,p.level()),"native recipe matches4apples4totems1netherite");check(recipe.assemble(input).is(orb),"native recipe crafts Orb");check(manager.getRecipeFor(RecipeType.CRAFTING,input,p.level()).orElseThrow().id().equals(key),"native recipe lookup discovers new craft");
 var old=CraftingInput.of(3,3,List.of(ItemStack.EMPTY,new ItemStack(Items.EXPERIENCE_BOTTLE),ItemStack.EMPTY,new ItemStack(Items.EXPERIENCE_BOTTLE),new ItemStack(Items.DIAMOND),new ItemStack(Items.EXPERIENCE_BOTTLE),ItemStack.EMPTY,new ItemStack(Items.EXPERIENCE_BOTTLE),ItemStack.EMPTY));check(!recipe.matches(old,p.level()),"old diamondXP recipe rejected");check(manager.getRecipeFor(RecipeType.CRAFTING,old,p.level()).isEmpty(),"old recipe absent from native manager");check(!recipe.display().isEmpty(),"native recipe display available to recipe UI integrations");
 var advancement=p.level().getServer().getAdvancements().get(Identifier.parse("skill_tree_rpgs:recipes/combat/orb_of_oblivion"));check(advancement!=null,"native recipe advancement registered");p.getRecipeBook().remove(key);for(var criterion:advancement.value().criteria().keySet())p.getAdvancements().revoke(advancement,criterion);p.setGameMode(GameType.SURVIVAL);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.NETHERITE_INGOT));net.minecraft.advancements.triggers.CriteriaTriggers.INVENTORY_CHANGED.trigger(p,p.getInventory(),p.getMainHandItem());check(p.getRecipeBook().contains(key),"netherite inventory trigger unlocks native recipe book");

 }
}
