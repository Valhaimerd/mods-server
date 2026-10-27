package local.delightclient;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import com.jkvin114.displaydelight.init.*;
import com.jkvin114.displaydelight.item.FoodBlockItem;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.constants.*;
import mezz.jei.api.recipe.*;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
@Mod(value="delight_client_probe",dist=Dist.CLIENT)
public class ClientDelightProbe {
 int ticks,phase,phaseTick;boolean hidden,done,all;
 List<String> results=Collections.synchronizedList(new ArrayList<>());
 AtomicReference<String> fixture=new AtomicReference<>();IJeiRuntime jei;
 void check(boolean ok,String text){results.add((ok?"PASS ":"FAIL ")+text);System.out.println("DELIGHT_CLIENT "+(ok?"PASS ":"FAIL ")+text);}
 public ClientDelightProbe(){NeoForge.EVENT_BUS.addListener(this::tick);}
 Item item(String id){return BuiltInRegistries.ITEM.getValue(Identifier.parse(id));}
 String id(Item item){return BuiltInRegistries.ITEM.getKey(item).toString();}
 boolean bad(ItemStack s){return s.getItem() instanceof FoodBlockItem&&!BlockAssociations.isAvailable(s.getItem());}
 void shot(String label){Screenshot.grab(Minecraft.getInstance(),false);results.add("SCREENSHOT "+label);}
 void next(){phase++;phaseTick=ticks;}
 void creative(String text)throws Exception{
  var mc=Minecraft.getInstance();var screen=new net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen(mc.player,mc.level.enabledFeatures(),true);mc.setScreenAndShow(screen);
  var select=screen.getClass().getDeclaredMethod("selectTab",CreativeModeTab.class);select.setAccessible(true);
  select.invoke(screen,text==null?BuiltInRegistries.CREATIVE_MODE_TAB.getValue(Identifier.parse("displaydelight:displaydelight")):CreativeModeTabs.searchTab());
  if(text!=null){var f=screen.getClass().getDeclaredField("searchBox");f.setAccessible(true);((net.minecraft.client.gui.components.EditBox)f.get(screen)).setValue(text);var refresh=screen.getClass().getDeclaredMethod("refreshSearchResults");refresh.setAccessible(true);refresh.invoke(screen);}
 }
 @SuppressWarnings({"rawtypes","unchecked"}) void inspectJei(){
  var mc=Minecraft.getInstance();var filter=jei.getIngredientFilter();filter.setFilterText("");var visible=filter.getFilteredIngredients(VanillaTypes.ITEM_STACK);
  check(visible.stream().noneMatch(this::bad),"JEI ingredients hide every unavailable display");
  check(visible.stream().noneMatch(s->id(s.getItem()).startsWith("displaydelight:bnc_")),"JEI no Brewin compatibility placeholders");
  var info=jei.getRecipeManager().createRecipeLookup(RecipeTypes.INFORMATION).get().toList();
  check(info.stream().flatMap(r->r.getIngredients().stream()).flatMap(i->i.getItemStack().stream()).noneMatch(this::bad),"JEI information recipes hide every unavailable display");
  for(String good:List.of("displaydelight:food_plate","displaydelight:small_food_plate","displaydelight:plated_cookie","displaydelight:beef_stew","displaydelight:ed_asparagus_soup")) {
   var target=item(good);check(target!=Items.AIR,"registered base representative "+good);if(target==Items.AIR)continue;
   check(visible.stream().anyMatch(s->s.is(target)),"JEI base representative visible "+good);
  }
  var categories=jei.getRecipeManager().createRecipeCategoryLookup().get().toList();
  Set<String> jeiRecipeIds=new HashSet<>();
  for(var category:categories){var type=(IRecipeType)category.getRecipeType();var recipes=jei.getRecipeManager().createRecipeLookup(type).get().toList();for(Object r:recipes)if(r instanceof net.minecraft.world.item.crafting.RecipeHolder<?> holder)jeiRecipeIds.add(holder.id().identifier().toString());}
  for(String ns:List.of("large_meals","oceansdelight","ends_delight")) {
   var foods=BuiltInRegistries.ITEM.stream().filter(i->BuiltInRegistries.ITEM.getKey(i).getNamespace().equals(ns)&&new ItemStack(i).has(DataComponents.FOOD)).toList();
   check(all?!foods.isEmpty():foods.isEmpty(),"addon presence "+ns+" foods="+foods.size());
   if(all){check(foods.stream().allMatch(i->visible.stream().anyMatch(s->s.is(i))),"JEI all edible addon ingredients visible "+ns);check(jeiRecipeIds.stream().anyMatch(id->id.startsWith(ns+":")),"JEI native recipe category populated "+ns);}
   var displays=DisplayItems.getRegisteredItems().stream().filter(i->i instanceof FoodBlockItem&&BuiltInRegistries.ITEM.getKey(BlockAssociations.sourceFood(i)).getNamespace().equals(ns)&&BlockAssociations.isAvailable(i)).toList();
   if(all){check(!displays.isEmpty(),"addon display aliases/categories resolve "+ns+" displays="+displays.size());check(displays.stream().allMatch(i->visible.stream().anyMatch(s->s.is(i))),"JEI addon displays visible "+ns);check(displays.stream().allMatch(i->info.stream().anyMatch(r->r.getIngredients().stream().flatMap(t->t.getItemStack().stream()).anyMatch(s->s.is(i)))),"JEI addon display info populated "+ns);}
  }
  int models=0;for(var s:visible)if(Set.of("displaydelight","large_meals","oceansdelight","ends_delight").contains(BuiltInRegistries.ITEM.getKey(s.getItem()).getNamespace())){
   var state=new net.minecraft.client.renderer.item.ItemStackRenderState();mc.getItemModelResolver().updateForTopItem(state,s,ItemDisplayContext.GUI,mc.level,mc.player,0);check(!state.isEmpty(),"native item render model "+id(s.getItem()));models++;
  }check(models>20,"native GUI item model sample populated count="+models);
  for(var map:List.of(BlockAssociations.blockMap,BlockAssociations.plateBlockMap,BlockAssociations.smallplateBlockMap))check(map.entrySet().stream().allMatch(e->e.getKey()!=Items.AIR&&BlockAssociations.sourceFood(e.getValue().asItem())==e.getKey()),"client category maps contain resolved original foods entries="+map.size());
 }
 void serverFixture(){
  var mc=Minecraft.getInstance();mc.getSingleplayerServer().execute(()->{try{
   var server=mc.getSingleplayerServer();var p=server.getPlayerList().getPlayer(mc.player.getUUID());p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);p.getInventory().clearContent();var world=p.level();
   for(String command:List.of("fill -8 99 -8 8 99 8 smooth_stone","fill -8 100 -8 8 106 8 air","tp @s 0.5 101 -4.5 0 22","time set noon","weather clear"))server.getCommands().performPrefixedCommand(p.createCommandSourceStack(),command);
   String[] ids=all?new String[]{"large_meals:omurice_block","large_meals:mushroom_pot_pie_block","large_meals:roasted_mutton_rack_block","ends_delight:end_stove","oceansdelight:guardian_soup"}:new String[]{"farmersdelight:stove","farmersdelight:roast_chicken_block","displaydelight:beef_stew","displaydelight:plated_cookie"};
   for(int i=0;i<ids.length;i++){var block=BuiltInRegistries.BLOCK.getValue(Identifier.parse(ids[i]));if(block!=Blocks.AIR)world.setBlock(new BlockPos(i-2,100,1),block.defaultBlockState(),3);}
   if(all){String[] ends={"grilled_shulker_block","dragon_leg_with_sauce_block","dragon_meat_stew_block","steamed_dragon_egg_block"};for(int i=0;i<ends.length;i++)world.setBlock(new BlockPos(i-2,100,2),BuiltInRegistries.BLOCK.getValue(Identifier.parse("ends_delight:"+ends[i])).defaultBlockState(),3);}
   if(all){var be=world.getBlockEntity(new BlockPos(1,100,1));if(be instanceof vectorwing.farmersdelight.common.block.entity.AbstractStoveBlockEntity stove){world.setBlock(be.getBlockPos(),be.getBlockState().setValue(vectorwing.farmersdelight.common.block.AbstractStoveBlock.LIT,false),3);for(int i=0;i<6;i++){var food=new ItemStack(i%2==0?Items.BEEF:Items.POTATO);check(stove.placeFood(p,food,stove.getCookingRecipe(food).orElseThrow()),"End Stove native placeFood slot "+i);}check(stove.streamItems().filter(stack->!stack.isEmpty()).count()==6,"End Stove server fixture six filled slots");stove.setChanged();world.sendBlockUpdated(be.getBlockPos(),be.getBlockState(),be.getBlockState(),3);check(stove.getItems().getSlots()==6,"End Stove server fixture six slots");}}
   int n=0;Map<String,Integer> shown=new HashMap<>();for(var display:DisplayItems.getRegisteredItems())if(display instanceof FoodBlockItem&&BlockAssociations.isAvailable(display)){
    String ns=BuiltInRegistries.ITEM.getKey(BlockAssociations.sourceFood(display)).getNamespace();
    boolean allowed=all?Set.of("large_meals","oceansdelight","ends_delight").contains(ns)&&shown.getOrDefault(ns,0)<2:n<7;
    if(allowed){world.setBlock(new BlockPos(n-3,100,3),((BlockItem)display).getBlock().defaultBlockState(),3);n++;shown.merge(ns,1,Integer::sum);}
   }
   if(all)for(String ns:List.of("large_meals","oceansdelight","ends_delight"))check(shown.getOrDefault(ns,0)>0,"world display fixture present "+ns);
   fixture.set("ready");
  }catch(Throwable t){check(false,"server fixture "+t);t.printStackTrace();fixture.set("failed");}});
 }
 void tick(ClientTickEvent.Post event){var mc=Minecraft.getInstance();if(!hidden){org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());hidden=true;}if(done||mc.player==null||mc.level==null)return;ticks++;
  try{
   if(ticks==30){all=net.neoforged.fml.ModList.get().isLoaded("large_meals");serverFixture();}
   if(ticks<80)return;
   if(Files.exists(mc.gameDirectory.toPath().resolve("visual-only.flag"))){
    if(ticks==80){mc.setScreenAndShow(null);mc.getSingleplayerServer().execute(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());mc.getSingleplayerServer().getCommands().performPrefixedCommand(p.createCommandSourceStack(),"tp @s 0.5 106 -2 0 58");p.getAbilities().flying=true;p.onUpdateAbilities();});}
    if(ticks==120){shot("08 overhead native stove six food items feasts and addon displays");}
    if(ticks>=145)finish();return;
   }
   if(phase==0){var runtime=mezz.jei.common.Internal.getOptionalJeiRuntime();if(runtime.isEmpty()){if(ticks>1200)throw new AssertionError("JEI runtime timeout");return;}jei=runtime.get();check("ready".equals(fixture.get()),"integrated server fixture ready");creative(null);
    var search=CreativeModeTabs.searchTab().getDisplayItems();check(search.stream().noneMatch(this::bad),"native creative search tab hides unavailable displays");check(search.stream().noneMatch(s->id(s.getItem()).startsWith("displaydelight:bnc_")),"native creative search tab no bnc placeholders");
    var tab=BuiltInRegistries.CREATIVE_MODE_TAB.getValue(Identifier.parse("displaydelight:displaydelight"));check(tab.getDisplayItems().stream().noneMatch(this::bad),"native Display Delight tab hides unavailable displays");check(tab.getDisplayItems().size()>20,"native Display Delight tab populated count="+tab.getDisplayItems().size());inspectJei();jei.getIngredientFilter().setFilterText("@displaydelight");next();return;}
   int age=ticks-phaseTick;if(age<25)return;
   if(phase==1){shot("01 Display Delight creative and JEI populated");creative("Brewin");next();return;}
   if(phase==2){check(mc.player.containerMenu.slots.stream().noneMatch(s->id(s.getItem().getItem()).startsWith("displaydelight:bnc_")),"actual native creative text search Brewin no bnc");shot("02 native creative missing addon search");creative(null);jei.getIngredientFilter().setFilterText(all?"@large_meals":"@farmersdelight");next();return;}
   if(phase==3){shot("03 JEI "+(all?"Large Meals":"Farmers Delight"));jei.getIngredientFilter().setFilterText(all?"@oceansdelight":"@expandeddelight");next();return;}
   if(phase==4){shot("04 JEI "+(all?"Oceans Delight":"Expanded Delight"));if(all){jei.getIngredientFilter().setFilterText("@ends_delight");}else{showInfo();}next();return;}
   if(phase==5){shot("05 JEI "+(all?"Ends Delight":"display information"));if(all)showInfo();else mc.setScreenAndShow(null);next();return;}
   if(phase==6){if(all)shot("06 JEI addon display information");mc.setScreenAndShow(null);next();return;}
   if(phase==7){shot("07 world feasts displays and stove");if(all){var be=mc.level.getBlockEntity(new BlockPos(1,100,1));check(be!=null,"End Stove client block entity synchronized");if(be!=null){if(be instanceof vectorwing.farmersdelight.common.block.entity.AbstractStoveBlockEntity stove)check(stove.streamItems().filter(stack->!stack.isEmpty()).count()==6,"End Stove client six filled slots actual="+stove.streamItems().filter(stack->!stack.isEmpty()).count());var renderer=mc.getBlockEntityRenderDispatcher().getRenderer(be);check(renderer!=null&&renderer.getClass().getName().contains("EndStove"),"End Stove native renderer registered "+(renderer==null?"null":renderer.getClass().getName()));var state=mc.getBlockEntityRenderDispatcher().tryExtractRenderState(be,0,null,false);check(state!=null,"End Stove native renderer extracts state");if(state instanceof vectorwing.farmersdelight.client.renderer.DefaultStoveRenderer.RenderState stoveState){check(stoveState.items.size()==6,"End Stove extracts all six food item render states actual="+stoveState.items.size());check(stoveState.offsets.size()==6,"End Stove extracts six original food offsets actual="+stoveState.offsets.size());check(stoveState.items.stream().noneMatch(net.minecraft.client.renderer.item.ItemStackRenderState::isEmpty),"End Stove all six food models nonempty");}}}
    mc.getSingleplayerServer().execute(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.getInventory().clearContent();p.level().setBlock(new BlockPos(6,100,0),BuiltInRegistries.BLOCK.getValue(Identifier.parse("displaydelight:beef_stew")).defaultBlockState(),3);mc.getSingleplayerServer().getCommands().performPrefixedCommand(p.createCommandSourceStack(),"tp @s 6.5 101 -2.5 0 22");});next();return;}
   if(phase==8){mc.gameMode.useItemOn(mc.player,net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.phys.BlockHitResult(Vec3.atCenterOf(new BlockPos(6,100,0)),Direction.UP,new BlockPos(6,100,0),false));next();return;}
   if(phase==9){check(mc.player.getMainHandItem().is(item("farmersdelight:beef_stew"))&&mc.level.isEmptyBlock(new BlockPos(6,100,0)),"real client empty-hand bowl retrieval through network returns FD beef stew");mc.getSingleplayerServer().execute(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());p.getInventory().clearContent();p.level().setBlock(new BlockPos(6,100,0),BuiltInRegistries.BLOCK.getValue(Identifier.parse("displaydelight:plated_cookie")).defaultBlockState().setValue(com.jkvin114.displaydelight.block.AbstractStackablePlatedFoodBlock.STACKS,1),3);});next();return;}
   if(phase==10){mc.gameMode.useItemOn(mc.player,net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.phys.BlockHitResult(Vec3.atCenterOf(new BlockPos(6,100,0)),Direction.UP,new BlockPos(6,100,0),false));next();return;}
   if(phase==11){check(mc.player.getMainHandItem().is(Items.COOKIE)&&mc.level.getBlockState(new BlockPos(6,100,0)).is(BuiltInRegistries.BLOCK.getValue(Identifier.parse("displaydelight:food_plate"))),"real client plate take-one through network retains original empty plate");mc.getSingleplayerServer().execute(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);p.getAbilities().flying=true;p.onUpdateAbilities();mc.getSingleplayerServer().getCommands().performPrefixedCommand(p.createCommandSourceStack(),"tp @s 0.5 106 -2 0 58");});next();return;}
   if(phase==12){shot("08 overhead native stove six food items feasts and addon displays");next();return;}
   if(phase==13){finish();}
  }catch(Throwable error){check(false,"exception "+error);error.printStackTrace();finish();}
 }
 void showInfo(){var infos=jei.getRecipeManager().createRecipeLookup(RecipeTypes.INFORMATION).get().filter(r->r.getIngredients().stream().flatMap(i->i.getItemStack().stream()).anyMatch(s->id(s.getItem()).startsWith(all?"displaydelight:lm_":"displaydelight:beef_stew"))).toList();check(!infos.isEmpty(),"representative display info page exists");if(!infos.isEmpty())jei.getRecipesGui().showRecipes(jei.getRecipeManager().getRecipeCategory(RecipeTypes.INFORMATION),infos,List.of());}
 void finish(){done=true;var mc=Minecraft.getInstance();try{Files.write(mc.gameDirectory.toPath().resolve("client-results.txt"),results);Files.writeString(mc.gameDirectory.toPath().resolve("client-result.txt"),(results.stream().anyMatch(s->s.startsWith("FAIL"))?"FAIL":"PASS")+" "+results.stream().filter(s->s.startsWith("PASS")).count()+" checks\n");}catch(Exception e){throw new RuntimeException(e);}mc.stop();}
}
