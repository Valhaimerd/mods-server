package local.crossarms.clientprobe;

import java.nio.file.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Isolated client test: exhaust on server, then crank with the real client use key. */
@Mod("preview_probe")
public class ClientBurstProbe {
    int ticks;boolean hidden;
    public ClientBurstProbe(){NeoForge.EVENT_BUS.addListener(this::tick);}
    void result(String text){try{Files.writeString(Minecraft.getInstance().gameDirectory.toPath().resolve("frenzy-client-result.txt"),text);}catch(Exception e){throw new RuntimeException(e);}}
    void tick(ClientTickEvent.Post event){
        var mc=Minecraft.getInstance();
        if(!hidden){org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());hidden=true;}
        if(mc.player==null||mc.getSingleplayerServer()==null)return;
        ticks++;
        if(ticks==40){mc.setScreenAndShow(null);var server=mc.getSingleplayerServer();server.execute(()->{try{
            var p=server.getPlayerList().getPlayer(mc.player.getUUID());p.getInventory().clearContent();p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);p.getInventory().setItem(1,new ItemStack(Items.ARROW,64));
            var bow=new ItemStack(Items.CROSSBOW);p.setItemInHand(InteractionHand.MAIN_HAND,bow);bow.enchant(server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(Identifier.parse("crossarms:frenzy")).orElseThrow(),1);
            var commands=server.getCommands();var source=p.createCommandSourceStack().withSuppressedOutput();for(var key:new String[]{"frenzy_shots","frenzy_empty","frenzy_idle","HS_delay"})commands.performPrefixedCommand(source,"scoreboard players set @s "+key+" 0");
            for(int shot=0;shot<26;shot++){for(int i=0;i<8;i++)EnchantmentHelper.tickEffects(p.level(),p);((CrossbowItem)bow.getItem()).use(p.level(),p,InteractionHand.MAIN_HAND);}
            if(CrossbowItem.isCharged(bow))throw new AssertionError("26th shot did not empty crossbow");p.containerMenu.broadcastChanges();result("EMPTY: 26 shots fired; awaiting client crank");
        }catch(Throwable e){result("FAIL "+e);}});}
        // Wait over three seconds before trying to crank; an exhausted burst must stay empty.
        if(ticks==110){Screenshot.grab(mc,false);var server=mc.getSingleplayerServer();server.execute(()->{var p=server.getPlayerList().getPlayer(mc.player.getUUID());var board=server.getScoreboard();if(CrossbowItem.isCharged(p.getMainHandItem())||board.getOrCreatePlayerScore(p,board.getObjective("frenzy_empty")).get()!=1)result("FAIL idle cleared an exhausted burst");});}
        if(ticks==120)mc.options.keyUse.setDown(true);
        if(ticks==155)mc.options.keyUse.setDown(false);
        if(ticks==170){var server=mc.getSingleplayerServer();server.execute(()->{try{
            var p=server.getPlayerList().getPlayer(mc.player.getUUID());var board=server.getScoreboard();if(!CrossbowItem.isCharged(p.getMainHandItem())||board.getOrCreatePlayerScore(p,board.getObjective("frenzy_empty")).get()!=0)throw new AssertionError("client hold did not complete normal crank");result("PASS: client received empty crossbow after shot 26; stayed empty beyond 60 ticks; real client use key completed normal crank; loaded and burst unlocked");
        }catch(Throwable e){result("FAIL "+e);}});}
        if(ticks==180){mc.options.keyUse.setDown(false);mc.stop();}
    }
}
