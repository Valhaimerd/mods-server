package local.crossarms;
import java.nio.file.*;import java.util.*;
import net.neoforged.fml.common.Mod;import net.neoforged.neoforge.common.NeoForge;import net.neoforged.neoforge.event.server.ServerStartedEvent;import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.minecraft.core.component.DataComponents;import net.minecraft.core.registries.*;import net.minecraft.resources.Identifier;import net.minecraft.world.InteractionHand;import net.minecraft.world.item.*;import net.minecraft.world.item.enchantment.EnchantmentHelper;
@Mod("crossarms_probe") public class FiringProbe {
 int checks; net.minecraft.server.level.ServerPlayer p;net.minecraft.server.level.ServerLevel l; int arrows; InteractionHand hand=InteractionHand.MAIN_HAND;
 void check(boolean b,String s){if(!b)throw new AssertionError(s);checks++;System.out.println("FIRING_PASS "+s);}
 void ticks(int n){for(int i=0;i<n;i++)EnchantmentHelper.tickEffects(l,p);}
 ItemStack bow(){return p.getItemInHand(hand);}
 void command(String command){l.getServer().getCommands().performPrefixedCommand(l.getServer().createCommandSourceStack().withEntity(p).withPosition(p.position()).withLevel(l),command);}
 void setup(String item,String ench,int level){command("scoreboard players reset @s heat");command("scoreboard players reset @s HS_delay");p.getInventory().clearContent();p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);p.getInventory().setItem(1,new ItemStack(Items.ARROW,64));var b=new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(item)));p.setItemInHand(hand,b);if(!ench.isEmpty())b.enchant(l.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(Identifier.parse("crossarms:"+ench)).orElseThrow(),level);}
 void fire(){((CrossbowItem)bow().getItem()).use(l,p,hand);}
 void load(){var b=bow();var cb=(CrossbowItem)b.getItem();cb.use(l,p,hand);cb.onUseTick(l,p,b,b.getUseDuration(p)-200);cb.releaseUsing(b,l,p,b.getUseDuration(p)-200);p.stopUsingItem();ticks(2);}
 public FiringProbe(){NeoForge.EVENT_BUS.addListener(this::start);}
 void start(ServerStartedEvent e){var s=e.getServer();s.execute(()->{try{
 l=s.overworld();p=FakePlayerFactory.getMinecraft(l);p.setPos(0,80,0);l.getChunk(0,0);command("function crossarms:load");
 NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.entity.EntityJoinLevelEvent event)->{if(event.getEntity() instanceof net.minecraft.world.entity.projectile.arrow.AbstractArrow){arrows++;event.getEntity().discard();}});
 for(InteractionHand testHand:InteractionHand.values()){hand=testHand;for(String item:List.of("minecraft:crossbow","arsenal:unique_heavy_crossbow_1")){
 check(BuiltInRegistries.ITEM.getValue(Identifier.parse(item)) instanceof CrossbowItem,"registered crossbow "+item);
 for(int level=1;level<=4;level++){
 setup(item,"capacity",level);ticks(2);load();int before=arrows;
 for(int shot=0;shot<level*4;shot++){check(CrossbowItem.isCharged(bow()),item+" capacity"+level+" ready shot"+(shot+1));fire();ticks(2);}
 check(arrows-before==level*4,item+" capacity"+level+" spawned "+(level*4));check(!CrossbowItem.isCharged(bow()),item+" capacity"+level+" empty after magazine");
 }
 setup(item,"frenzy",1);ticks(8);int before=arrows;
 for(int shot=0;shot<6;shot++){check(CrossbowItem.isCharged(bow()),item+" frenzy ready "+shot);fire();ticks(8);}
 check(arrows-before==6,item+" frenzy six real arrows");
 check(p.getInventory().countItem(Items.ARROW)==57,item+" frenzy consumes exactly one arrow per feed");
 check(bow().getDamageValue()==6,item+" frenzy loses one durability per shot");
 check(l.getScoreboard().getOrCreatePlayerScore(p,l.getScoreboard().getObjective("heat")).get()>0,item+" frenzy heat rises");
 int max=bow().getMaxDamage();command("function crossarms:enchantment/frenzy/break {slot:\""+(hand==InteractionHand.MAIN_HAND?"mainhand":"offhand")+"\"}");
 check(bow().getMaxDamage()==max&&bow().getDamageValue()==max-2,item+" frenzy overheat leaves exactly two durability max="+max);
 fire();ticks(8);check(bow().getDamageValue()==max-1&&!CrossbowItem.isCharged(bow()),item+" frenzy stops at one durability");
 setup(item,"capacity",1);p.getInventory().setItem(1,new ItemStack(Items.TIPPED_ARROW,16));ticks(2);load();before=arrows;check(CrossbowItem.isCharged(bow()),item+" tipped arrow loads");fire();ticks(8);check(arrows-before==1&&!CrossbowItem.isCharged(bow()),item+" tipped arrow remains single shot");
 setup(item,"",0);ticks(2);load();check(CrossbowItem.isCharged(bow()),item+" ordinary loaded");before=arrows;fire();ticks(8);check(arrows-before==1&&!CrossbowItem.isCharged(bow()),item+" ordinary one shot then manual reload");
 }
 }
 Files.writeString(Path.of("result.txt"),"PASS "+checks);
 }catch(Throwable t){t.printStackTrace();try{Files.writeString(Path.of("result.txt"),"FAIL "+t);}catch(Exception ignored){}}s.halt(false);});}
}
