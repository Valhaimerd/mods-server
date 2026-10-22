package local.downedprobe;

import java.nio.file.*;
import java.lang.reflect.*;
import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.*;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.GameType;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems;
import net.p3pp3rf1y.sophisticatedcore.upgrades.feeding.FeedingUpgradeWrapper;
import net.p3pp3rf1y.sophisticatedcore.upgrades.alchemy.*;
import net.p3pp3rf1y.sophisticatedcore.compat.DownedConsumables;
import team.creative.playerrevive.server.PlayerReviveServer;
import de.mrstilu2k6.better_revive.util.DownedPlayerManager;

@Mod("downed_backpack_probe")
public final class ConsumablesProbe {
    int checks;
    final List<String> results=new ArrayList<>();
    void check(boolean ok,String text){if(!ok)throw new AssertionError(text);checks++;results.add("PASS "+text);}
    public ConsumablesProbe(){NeoForge.EVENT_BUS.addListener(this::started);}
    static Field field(String name) throws Exception {var f=AlchemyUpgradeWrapper.class.getDeclaredField(name);f.setAccessible(true);return f;}
    void started(ServerStartedEvent event){var server=event.getServer();server.execute(()->{
        try {
            var level=server.overworld();var player=FakePlayerFactory.getMinecraft(level);
            player.setGameMode(GameType.SURVIVAL);player.setPos(0,100,0);
            player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(40);player.setHealth(20);
            if(Boolean.getBoolean("downed.probe.health"))HealthProbe.run(player,this);
            var storage=BackpackWrapper.fromStack(new ItemStack(ModItems.BACKPACK.get()));
            var inventory=storage.getInventoryHandler();
            var feeding=new FeedingUpgradeWrapper(storage,new ItemStack(ModItems.FEEDING_UPGRADE.get()),ignored->{});
            var feed=FeedingUpgradeWrapper.class.getDeclaredMethod("feedPlayerAndGetHungry",Player.class,net.minecraft.world.level.Level.class);feed.setAccessible(true);
            inventory.setStackInSlot(0,new ItemStack(Items.APPLE,64));player.getFoodData().setFoodLevel(4);
            feed.invoke(feeding,player,level);
            check(inventory.getStackInSlot(0).getCount()==63,"healthy player consumes one apple");
            check(player.getFoodData().getFoodLevel()>4,"healthy feeding raises hunger");
            boolean absent=Boolean.getBoolean("downed.probe.absent");
            if(!absent){
                PlayerReviveServer.getBleeding(player).knockOut(player,level.damageSources().generic());
                check(PlayerReviveServer.isBleeding(player),"native PlayerRevive downed flag set");
                inventory.setStackInSlot(0,new ItemStack(Items.APPLE,64));player.getFoodData().setFoodLevel(4);
                for(int i=0;i<10;i++)feed.invoke(feeding,player,level);
                check(inventory.getStackInSlot(0).getCount()==64,"downed feeding consumes no food across repeated attempts");
                check(player.getFoodData().getFoodLevel()==4,"downed feeding leaves hunger untouched");
                check(DownedConsumables.isDowned(player),"guard recognizes PlayerRevive");
                PlayerReviveServer.getBleeding(player).revive(player);
                check(!DownedConsumables.isDowned(player),"guard clears after PlayerRevive rescue");
                var f=DownedPlayerManager.class.getDeclaredField("downed");f.setAccessible(true);
                @SuppressWarnings("unchecked") var downed=(Map<UUID,Object>)f.get(null);
                downed.put(player.getUUID(),new DownedPlayerManager.DownedState(System.currentTimeMillis(),Long.MAX_VALUE,player.position(),level.dimension(),GameType.SURVIVAL));
                check(DownedConsumables.isDowned(player),"guard recognizes Better Revive independently");
                feed.invoke(feeding,player,level);
                check(inventory.getStackInSlot(0).getCount()==64,"Better Revive state also preserves food");
                downed.remove(player.getUUID());
            } else check(!DownedConsumables.isDowned(player),"optional revive mods absent: healthy player allowed");
            feed.invoke(feeding,player,level);
            check(inventory.getStackInSlot(0).getCount()==(absent?62:63),"feeding works after state clears");
            inventory.setStackInSlot(0,ItemStack.EMPTY);
            var potion=new ItemStack(Items.POTION);potion.set(DataComponents.POTION_CONTENTS,new PotionContents(Potions.SWIFTNESS));
            inventory.setStackInSlot(1,potion.copy());
            var item=(AlchemyUpgradeItem)BuiltInRegistries.ITEM.getValue(Identifier.parse("sophisticatedbackpacks:alchemy_upgrade"));
            var alchemy=item.getType().create(storage,new ItemStack(item),ignored->{});
            alchemy.getFilterHandler().setStackInSlot(0,potion.copy());alchemy.setConditionValue(0,AlchemyCondition.ALWAYS,0);
            if(!absent)PlayerReviveServer.getBleeding(player).knockOut(player,level.damageSources().generic());
            if(!absent){
                alchemy.tick(player,level,player.blockPosition());
                check(inventory.getStackInSlot(1).is(Items.POTION),"downed holder does not start drinking");
                check(field("remainingApplyTime").getInt(alchemy)==0,"downed holder has no drink timer");
                var matches=AlchemyUpgradeWrapper.class.getDeclaredMethod("entityMatches",net.minecraft.world.entity.LivingEntity.class);matches.setAccessible(true);
                check(!(Boolean)matches.invoke(alchemy,player),"placed alchemy excludes downed target");
                PlayerReviveServer.getBleeding(player).revive(player);
            }
            alchemy.tick(player,level,player.blockPosition());
            int remaining=field("remainingApplyTime").getInt(alchemy);
            check(remaining>0,"healthy player begins drinking");
            if(!absent){
                PlayerReviveServer.getBleeding(player).knockOut(player,level.damageSources().generic());
                for(int i=0;i<50;i++)alchemy.tick(null,level,player.blockPosition());
                check(field("remainingApplyTime").getInt(alchemy)==remaining,"in-progress placed drink pauses when target becomes downed");
                check(!player.hasEffect(MobEffects.SPEED),"paused drink applies no buff");
                check(((ItemStack)field("stackBeingAplied").get(alchemy)).is(Items.POTION),"paused drink retains reserved potion");
                PlayerReviveServer.getBleeding(player).revive(player);
            }
            for(int i=0;i<remaining;i++)alchemy.tick(player,level,player.blockPosition());
            check(player.hasEffect(MobEffects.SPEED),"drink completes after rescue and grants its original buff");
            check(field("remainingApplyTime").getInt(alchemy)==0,"drink timer finishes normally");
            Files.write(Path.of("consumables-results.txt"),results);Files.writeString(Path.of("consumables-result.txt"),"PASS "+checks+" checks");
        }catch(Throwable failure){failure.printStackTrace();try{Files.write(Path.of("consumables-results.txt"),results);Files.writeString(Path.of("consumables-result.txt"),"FAIL "+failure);}catch(Exception ignored){}}
        finally{if(net.neoforged.fml.ModList.get().isLoaded("better_revive"))DownedPlayerManager.clearAll();server.halt(false);}
    });}
}
