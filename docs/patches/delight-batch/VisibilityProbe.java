package pack.delightprobe;

import com.jkvin114.displaydelight.DisplayDelight;
import com.jkvin114.displaydelight.events.DisplayVillagerEvents;
import com.jkvin114.displaydelight.init.BlockAssociations;
import com.jkvin114.displaydelight.init.DisplayItems;
import com.jkvin114.displaydelight.item.FoodBlockItem;
import com.jkvin114.displaydelight.trades.DisplayItemListing;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.*;
import net.minecraft.world.item.trading.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;
import com.jkvin114.displaydelight.init.DisplayBlocks;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import java.util.*;

@Mod("delight_probe")
public class VisibilityProbe {
    static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks("delight_probe");
    static final DeferredRegister.Items ITEMS=DeferredRegister.createItems("delight_probe");
    static final net.neoforged.neoforge.registries.DeferredBlock<net.minecraft.world.level.block.Block> MISSING_BLOCK=BLOCKS.registerSimpleBlock("ed_missing_fixture",net.minecraft.world.level.block.state.BlockBehaviour.Properties::of);
    static final DeferredItem<Item> MISSING=ITEMS.<Item>registerItem("ed_missing_fixture",props->new FoodBlockItem(MISSING_BLOCK.get(),props));
    public VisibilityProbe(IEventBus bus) {
        BLOCKS.register(bus); ITEMS.register(bus);
        DisplayItems.items.add(MISSING);
    }
    static int failed;
    static void check(String name, boolean ok) {
        System.out.println("DELIGHT_CHECK " + name + "=" + ok);
        if (!ok) failed++;
    }
    static Item item(String id) {return BuiltInRegistries.ITEM.getValue(Identifier.parse(id));}
    @EventBusSubscriber(modid="delight_probe")
    public static class Events {
        @SubscribeEvent public static void started(ServerStartedEvent event) {
            var server=event.getServer();
            try {
                var world=server.overworld();
                var all=new ArrayList<Item>(); DisplayItems.GetAll().forEach(all::add);
                // Build actual tab contents: this does not depend on association maps.
                BlockAssociations.blockMap.clear(); BlockAssociations.plateBlockMap.clear();
                BlockAssociations.smallplateBlockMap.clear();
                CreativeModeTabs.tryRebuildTabContents(FeatureFlags.DEFAULT_FLAGS,true,server.registryAccess());
                var tab=DisplayDelight.DISPLAY_DELIGHT_TAB.get();
                var exposed=tab.getDisplayItems();
                check("missing_addons_hidden_creative", exposed.stream().noneMatch(s->BuiltInRegistries.ITEM.getKey(s.getItem()).getPath().startsWith("bnc_")));
                check("missing_addons_hidden_search", tab.getSearchTabDisplayItems().stream().noneMatch(s->BuiltInRegistries.ITEM.getKey(s.getItem()).getPath().startsWith("bnc_")));
                check("empty_plate_visible",exposed.stream().anyMatch(s->s.is(DisplayItems.PLATE.get())));
                check("vanilla_cookie_visible_without_server_maps",exposed.stream().anyMatch(s->s.is(item("displaydelight:plated_cookie")))) ;
                for(var pair:Map.of("oceansdelight","od_","large_meals","lm_","ends_delight","edd_").entrySet()) {
                    boolean present=ModList.get().isLoaded(pair.getKey());
                    long count=exposed.stream().filter(s->BuiltInRegistries.ITEM.getKey(s.getItem()).getPath().startsWith(pair.getValue())).count();
                    check(pair.getKey()+"_visibility",present?count>0:count==0);
                    System.out.println("DELIGHT_VISIBLE "+pair.getKey()+"="+count);
                }
                BlockAssociations.initialize(all);
                var pools=List.of(BlockAssociations.TRADEABLE_FOODS,BlockAssociations.TRADEABLE_DRINKS,BlockAssociations.TRADEABLE_PLATES,BlockAssociations.TRADEABLE_SMALL_PLATES);
                check("missing_addons_hidden_trade_pools",pools.stream().flatMap(Collection::stream).noneMatch(i->BuiltInRegistries.ITEM.getKey(i).getPath().startsWith("bnc_")));
                var sizes=pools.stream().map(List::size).toList();
                BlockAssociations.initialize(all);
                check("initialization_idempotent",sizes.equals(pools.stream().map(List::size).toList()));
                BlockAssociations.TRADEABLE_DRINKS.clear();
                check("empty_category_omits_offer",DisplayItemListing.drink(world.getRandom())==null);
                BlockAssociations.initialize(all);
                var trader=new WanderingTrader(EntityTypes.WANDERING_TRADER,world);
                var offers=new MerchantOffers();
                var vanilla=new MerchantOffer(new ItemCost(Items.EMERALD,1),new ItemStack(Items.BREAD),3,1,.05f);
                var unavailable=all.stream().filter(i->BuiltInRegistries.ITEM.getKey(i).getPath().startsWith("bnc_")).findFirst().orElseThrow();
                offers.add(vanilla);
                var validSaved=new MerchantOffer(new ItemCost(Items.EMERALD,1),new ItemStack(item("displaydelight:plated_cookie")),3,1,.05f);
                offers.add(validSaved);
                offers.add(new MerchantOffer(new ItemCost(Items.EMERALD,1),new ItemStack(unavailable),3,1,.05f));
                trader.getOffers().clear(); trader.getOffers().addAll(offers);
                DisplayVillagerEvents.onWanderingTraderJoin(new EntityJoinLevelEvent(trader,world,true));
                check("saved_invalid_offer_removed",trader.getOffers().stream().noneMatch(o->o.getResult().is(unavailable)));
                check("saved_ordinary_trade_preserved",trader.getOffers().contains(vanilla));
                check("saved_valid_display_trade_preserved",trader.getOffers().contains(validSaved));
                int savedSize=trader.getOffers().size();
                DisplayVillagerEvents.onWanderingTraderJoin(new EntityJoinLevelEvent(trader,world,true));
                check("saved_load_does_not_add_offers",trader.getOffers().size()==savedSize);
                try {
                    var available=BlockAssociations.class.getMethod("isAvailable",Item.class);
                    var source=BlockAssociations.class.getMethod("sourceFood",Item.class);
                    int presentMissing=0, visible=0;
                    for(Item display:all) {
                        boolean valid=(Boolean)available.invoke(null,display);
                        boolean shown=exposed.stream().anyMatch(stack->stack.is(display));
                        if(valid)visible++;
                        check("shared_rule_"+BuiltInRegistries.ITEM.getKey(display).getPath(),shown==valid);
                        if(display instanceof FoodBlockItem && source.invoke(null,display)==Items.AIR
                                && BuiltInRegistries.ITEM.getKey(display).getPath().startsWith("ed_")) {
                            presentMissing++;
                            check("loaded_mod_missing_food_hidden_"+BuiltInRegistries.ITEM.getKey(display).getPath(),!shown);
                        }
                    }
                    System.out.println("DELIGHT_LOADED_MOD_MISSING_FOOD="+presentMissing);
                    check("loaded_mod_missing_food_fixture_exercised",presentMissing>0);
                    System.out.println("DELIGHT_VISIBLE_TOTAL="+visible);
                    var newTrader=new WanderingTrader(EntityTypes.WANDERING_TRADER,world);
                    DisplayVillagerEvents.onWanderingTraderJoin(new EntityJoinLevelEvent(newTrader,world,false));
                    boolean tradesValid=true;
                    for(var offer:newTrader.getOffers()) if(offer.getResult().getItem() instanceof FoodBlockItem
                            && !(Boolean)available.invoke(null,offer.getResult().getItem())) tradesValid=false;
                    check("new_offers_all_have_real_sources",tradesValid);
                    boolean poolsValid=true;
                    for(var pool:pools) for(Item display:pool) if(!(Boolean)available.invoke(null,display)) poolsValid=false;
                    check("all_pools_follow_shared_rule",poolsValid);
                } catch(NoSuchMethodException oldBuild) {check("shared_rule_present",false);}
                check("hidden_item_remains_registered",unavailable!=Items.AIR && unavailable instanceof FoodBlockItem);
                check("hidden_block_remains_registered",BuiltInRegistries.BLOCK.getValue(BuiltInRegistries.ITEM.getKey(unavailable))!=net.minecraft.world.level.block.Blocks.AIR);
                check("expanded_delight_visible",exposed.stream().anyMatch(s->BuiltInRegistries.ITEM.getKey(s.getItem()).getPath().startsWith("ed_")));
                System.out.println("DELIGHT_ASSOCIATIONS="+BlockAssociations.blockMap.size()+","+BlockAssociations.plateBlockMap.size()+","+BlockAssociations.smallplateBlockMap.size());
            } catch(Throwable ex) {ex.printStackTrace();check("uncaught_exception",false);}
            finally {System.out.println("DELIGHT_FAILED="+failed);server.halt(false);}
        }
    }
}
