package local.vanishprobe;

import java.nio.file.*;
import java.lang.reflect.Method;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.rogues.effect.RogueEffects;
import net.neoforged.fml.common.Mod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mod(value="vanish_render_probe",dist=Dist.CLIENT)
public class ClientVanishProbe {
    int ticks; boolean done, hidden; List<String> results=new ArrayList<>();
    public ClientVanishProbe(){NeoForge.EVENT_BUS.addListener(this::tick);}
    void check(boolean ok,String label){results.add((ok?"PASS ":"FAIL ")+label);}
    void tick(ClientTickEvent.Post event){
        var mc=Minecraft.getInstance();
        if(!hidden){org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());hidden=true;}
        if(done||mc.player==null||mc.level==null||++ticks<40)return;
        done=true;
        try {
            var player=mc.player;
            player.removeEffect(RogueEffects.STEALTH.entry);
            player.removeEffect(MobEffects.INVISIBILITY);
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_SWORD));
            player.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.SHIELD));
            var state=new ArmedEntityRenderState();
            ArmedEntityRenderState.extractArmedEntityRenderState(player,state,mc.getItemModelResolver(),0);
            check(!state.rightHandItemState.isEmpty()&&!state.leftHandItemState.isEmpty(),"normal held models present");
            player.addEffect(new MobEffectInstance(RogueEffects.STEALTH.entry,140));
            ArmedEntityRenderState.extractArmedEntityRenderState(player,state,mc.getItemModelResolver(),0);
            check(state.rightHandItemState.isEmpty(),"Vanish hides right-hand render model");
            check(state.leftHandItemState.isEmpty(),"Vanish hides left-hand render model");
            check(state.rightHandItemStack.isEmpty()&&state.leftHandItemStack.isEmpty(),"Vanish clears held render snapshots");
            check(player.getMainHandItem().is(Items.DIAMOND_SWORD)&&player.getOffhandItem().is(Items.SHIELD),"actual equipped items untouched");
            var renderer=mc.gameRenderer.itemInHandRenderer;
            Method hook=Arrays.stream(ItemInHandRenderer.class.getDeclaredMethods()).filter(m->m.getName().contains("hideStealthHands")).findFirst().orElse(null);
            check(hook!=null,"first-person cancellation injected");
            if(hook!=null){
                hook.setAccessible(true);
                var cb=new CallbackInfo("submitHandsWithItems",true);
                hook.invoke(renderer,0f,null,null,player,0,cb);
                check(cb.isCancelled(),"first-person both hands suppressed during Vanish");
                renderer.submitHandsWithItems(0f,null,null,player,0);
                check(true,"actual first-person entry exits before renderer submission");
            }
            player.removeEffect(RogueEffects.STEALTH.entry);
            ArmedEntityRenderState.extractArmedEntityRenderState(player,state,mc.getItemModelResolver(),0);
            check(!state.rightHandItemState.isEmpty()&&!state.leftHandItemState.isEmpty(),"same render state restores both models after Vanish");
            if(hook!=null){
                var cb=new CallbackInfo("submitHandsWithItems",true);hook.invoke(renderer,0f,null,null,player,0,cb);
                check(!cb.isCancelled(),"normal first-person rendering unaffected");
            }
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,140));
            ArmedEntityRenderState.extractArmedEntityRenderState(player,state,mc.getItemModelResolver(),0);
            check(!state.rightHandItemState.isEmpty()&&!state.leftHandItemState.isEmpty(),"ordinary Invisibility retains native held models");
            player.removeEffect(MobEffects.INVISIBILITY);
        }catch(Throwable t){t.printStackTrace();results.add("FAIL "+t);}
        try{
            Files.write(mc.gameDirectory.toPath().resolve("vanish-results.txt"),results);
            Files.writeString(mc.gameDirectory.toPath().resolve("vanish-result.txt"),
                (results.stream().anyMatch(s->s.startsWith("FAIL"))?"FAIL":"PASS")+" "+results.size()+" connected client checks\n");
        }catch(Exception e){throw new RuntimeException(e);}
        mc.stop();
    }
}
