package local.bifrostdiscovery;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.world.item.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@Mod(value="bifrost_discovery_probe",dist=Dist.CLIENT)
public class ClientDiscoveryProbe {
 int ticks;boolean hidden,done,jeiShown,creativeShown;List<String> results=Collections.synchronizedList(new ArrayList<>());
 AtomicReference<String> serverStatus=new AtomicReference<>();
 List<String> names=List.of("bifrost_marker","heimdalls_sword","thors_hammer","stormbreaker");
 public ClientDiscoveryProbe(){NeoForge.EVENT_BUS.addListener(this::tick);}
 void check(boolean ok,String label){results.add((ok?"PASS ":"FAIL ")+label);}
 Item item(String name){return BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("bifrost",name));}
 void tick(ClientTickEvent.Post event){
  var mc=Minecraft.getInstance();if(!hidden){org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());hidden=true;}
  if(done||mc.player==null||mc.level==null||++ticks<40)return;
  try{
   if(ticks==40){
    mc.getSingleplayerServer().execute(()->{
     try{
      var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
      p.getInventory().clearContent();
      for(var ingredient:List.of(Items.OBSIDIAN,Items.CRYING_OBSIDIAN,Items.NETHERITE_INGOT)){
       var stack=new ItemStack(ingredient);p.getInventory().add(stack.copy());net.minecraft.advancements.triggers.CriteriaTriggers.INVENTORY_CHANGED.trigger(p,p.getInventory(),stack);
      }
      for(String name:names){
       var id=Identifier.fromNamespaceAndPath("bifrost",name);var key=ResourceKey.create(Registries.RECIPE,id);
       check(p.level().getServer().getRecipeManager().byKey(key).isPresent(),"native crafting recipe exists "+name);
       check(p.getRecipeBook().contains(key),"ingredient pickup unlocks recipe book "+name);
      }
      serverStatus.set("ready");
     }catch(Throwable t){results.add("FAIL server "+t);serverStatus.set("failed");}
    });
   }
   if(ticks==85){
    check("ready".equals(serverStatus.get()),"server recipe fixture ready");
    mc.setScreenAndShow(new net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen(mc.player,mc.level.enabledFeatures(),true));
    for(String name:names){
     var stack=new ItemStack(item(name));
     check(CreativeModeTabs.searchTab().getDisplayItems().stream().anyMatch(s->s.is(item(name))),"creative search includes "+name);
     var key=name.equals("bifrost_marker")?CreativeModeTabs.FUNCTIONAL_BLOCKS:CreativeModeTabs.COMBAT;
     var tab=BuiltInRegistries.CREATIVE_MODE_TAB.getValue(key.identifier());
     check(tab.getDisplayItems().stream().anyMatch(s->s.is(item(name))),"expected parent creative tab includes "+name);
    }
   }
   if(ticks>=100&&!jeiShown){
    var optional=(Optional<?>)Class.forName("mezz.jei.common.Internal").getMethod("getOptionalJeiRuntime").invoke(null);
    if(optional.isEmpty()){if(ticks>500)throw new AssertionError("JEI runtime timeout");return;}
    Object runtime=optional.get();var runtimeApi=Class.forName("mezz.jei.api.runtime.IJeiRuntime");
    Object manager=runtimeApi.getMethod("getRecipeManager").invoke(runtime);
    var managerApi=Class.forName("mezz.jei.api.recipe.IRecipeManager");var typeApi=Class.forName("mezz.jei.api.recipe.types.IRecipeType");
    Object type=Class.forName("mezz.jei.api.constants.RecipeTypes").getField("CRAFTING").get(null);
    Object lookup=managerApi.getMethod("createRecipeLookup",typeApi).invoke(manager,type);
    var recipes=((java.util.stream.Stream<?>)Class.forName("mezz.jei.api.recipe.IRecipeLookup").getMethod("get").invoke(lookup)).filter(r->((net.minecraft.world.item.crafting.RecipeHolder<?>)r).id().identifier().getNamespace().equals("bifrost")).toList();
    for(String name:names)check(recipes.stream().anyMatch(r->((net.minecraft.world.item.crafting.RecipeHolder<?>)r).id().identifier().getPath().equals(name)),"JEI visible crafting recipe "+name);
    var filter=runtimeApi.getMethod("getIngredientFilter").invoke(runtime);
    var filterApi=Class.forName("mezz.jei.api.runtime.IIngredientFilter");var ingredientTypeApi=Class.forName("mezz.jei.api.ingredients.IIngredientType");
    var itemType=Class.forName("mezz.jei.api.constants.VanillaTypes").getField("ITEM_STACK").get(null);
    filterApi.getMethod("setFilterText",String.class).invoke(filter,"@bifrost");
    var visible=(Collection<?>)filterApi.getMethod("getFilteredIngredients",ingredientTypeApi).invoke(filter,itemType);
    for(String name:names)check(visible.stream().anyMatch(s->((ItemStack)s).is(item(name))),"JEI ingredient list includes "+name);
    Object category=managerApi.getMethod("getRecipeCategory",typeApi).invoke(manager,type);
    Object gui=runtimeApi.getMethod("getRecipesGui").invoke(runtime);
    Class.forName("mezz.jei.api.runtime.IRecipesGui").getMethod("showRecipes",Class.forName("mezz.jei.api.recipe.category.IRecipeCategory"),List.class,List.class).invoke(gui,category,recipes,List.of());
    jeiShown=true;
   }
   if(ticks==140&&jeiShown){Screenshot.grab(mc,false);}
   if(ticks==155&&jeiShown){
    var screen=new net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen(mc.player,mc.level.enabledFeatures(),true);mc.setScreenAndShow(screen);
    var select=screen.getClass().getDeclaredMethod("selectTab",CreativeModeTab.class);select.setAccessible(true);select.invoke(screen,CreativeModeTabs.searchTab());
    var field=screen.getClass().getDeclaredField("searchBox");field.setAccessible(true);var box=(net.minecraft.client.gui.components.EditBox)field.get(screen);box.setValue("marker");
    var refresh=screen.getClass().getDeclaredMethod("refreshSearchResults");refresh.setAccessible(true);refresh.invoke(screen);creativeShown=true;
   }
   if(ticks==170&&creativeShown){
    check(mc.player.containerMenu.slots.stream().anyMatch(slot->slot.getItem().is(item("bifrost_marker"))),"creative marker text search returns Bifrost Marker");
    Screenshot.grab(mc,false);
   }
   if(ticks>=180&&creativeShown)finish();
  }catch(Throwable t){t.printStackTrace();results.add("FAIL "+t);finish();}
 }
 void finish(){var mc=Minecraft.getInstance();done=true;try{Files.write(mc.gameDirectory.toPath().resolve("discovery-results.txt"),results);Files.writeString(mc.gameDirectory.toPath().resolve("discovery-result.txt"),(results.stream().anyMatch(s->s.startsWith("FAIL"))?"FAIL":"PASS")+" "+results.stream().filter(s->s.startsWith("PASS")).count()+" checks\n");}catch(Exception ex){throw new RuntimeException(ex);}mc.stop();}
}
