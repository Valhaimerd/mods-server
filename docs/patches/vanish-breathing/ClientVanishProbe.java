package local.vanishprobe;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import com.mojang.blaze3d.vertex.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.*;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.*;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.effect.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.rogues.effect.RogueEffects;
import net.rogues.client.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
@Mod(value="vanish_render_probe",dist=Dist.CLIENT)
public class ClientVanishProbe {
    int ticks,shots; boolean done; String pendingShot; List<String> results=new ArrayList<>();
    public ClientVanishProbe(){NeoForge.EVENT_BUS.addListener(this::tick);NeoForge.EVENT_BUS.addListener(this::frame);}
    void frame(net.neoforged.neoforge.client.event.RenderFrameEvent.Post event){if(pendingShot!=null){Screenshot.grab(Minecraft.getInstance(),false);results.add(pendingShot);pendingShot=null;}}
    void check(boolean ok,String label){results.add((ok?"PASS ":"FAIL ")+label);}
    class Capture {
        int models,quads,vertices,glintModels,glintQuads; List<Integer> alphas=new ArrayList<>(),glintColors=new ArrayList<>(); boolean blended=true,glintGeometry;
        final SubmitNodeCollector collector=(SubmitNodeCollector)Proxy.newProxyInstance(getClass().getClassLoader(),new Class<?>[]{SubmitNodeCollector.class},(self,m,a)->{
            if(m.isDefault())return InvocationHandler.invokeDefault(self,m,a);
            if(m.getName().equals("order"))return self;
            if(m.getName().equals("submitModel")){models++;alphas.add((int)a[6]>>>24);blended&=((RenderType)a[3]).hasBlending();if(VanishVisuals.isGlint((RenderType)a[3])){glintModels++;glintColors.add((int)a[6]);}}
            if(m.getName().equals("submitCustomGeometry")){
                quads++;blended&=((RenderType)a[1]).hasBlending();glintGeometry=VanishVisuals.isGlint((RenderType)a[1]);if(glintGeometry)glintQuads++;
                ((SubmitNodeCollector.CustomGeometryRenderer)a[2]).render(((PoseStack)a[0]).last(),vertex());
                glintGeometry=false;
            }
            return null;
        });
        VertexConsumer vertex(){return (VertexConsumer)Proxy.newProxyInstance(getClass().getClassLoader(),new Class<?>[]{VertexConsumer.class},(self,m,a)->{
            if(m.isDefault())return InvocationHandler.invokeDefault(self,m,a);
            if(m.getName().equals("addVertex"))vertices++;
            if(m.getName().equals("setColor")){int color=a.length==1?(int)a[0]:((int)a[3]<<24)|((int)a[0]<<16)|((int)a[1]<<8)|(int)a[2];alphas.add(color>>>24);if(glintGeometry)glintColors.add(color);}
            return self;
        });}
    }
    @SuppressWarnings({"rawtypes","unchecked"}) void proof() throws Exception {
        var mc=Minecraft.getInstance();
        var other=new RemotePlayer(mc.level,new GameProfile(UUID.randomUUID(),"VanishObserver"));other.setId(123456);other.setPos(mc.player.position());
        other.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.DIAMOND_SWORD));other.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(Items.SHIELD));
        other.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.DIAMOND_HELMET));other.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.DIAMOND_CHESTPLATE));other.setItemSlot(EquipmentSlot.LEGS,new ItemStack(Items.DIAMOND_LEGGINGS));other.setItemSlot(EquipmentSlot.FEET,new ItemStack(Items.DIAMOND_BOOTS));
        var renderer=(LivingEntityRenderer)mc.getEntityRenderDispatcher().getRenderer(other);
        for(int elapsed:new int[]{0,20,40,60,80,100,120}){
            other.removeEffect(RogueEffects.STEALTH.entry);other.addEffect(new MobEffectInstance(RogueEffects.STEALTH.entry,140-elapsed));other.setInvisible(true);
            var state=(LivingEntityRenderState)renderer.createRenderState(other,0);
            float expected=VanishVisuals.cycle(elapsed);check(Math.abs(((VanishState)state).vanish$alpha()-expected)<.00001,"shared phase "+elapsed+" = "+expected);
            check(other.isInvisible(),"gameplay invisible retained "+elapsed);
            check(!state.isInvisible&&!state.isInvisibleToPlayer,"render body and armor available "+elapsed);
            var held=new ArmedEntityRenderState();ArmedEntityRenderState.extractArmedEntityRenderState(other,held,mc.getItemModelResolver(),0);
            check(!held.rightHandItemState.isEmpty()&&!held.leftHandItemState.isEmpty(),"both held snapshots preserved "+elapsed);
            var capture=new Capture();renderer.submit(state,new PoseStack(),capture.collector,new CameraRenderState());
            if(expected==0){check(capture.models==0&&capture.quads==0,"zero phase submits no body armor or weapons "+elapsed);}
            else{
                check(capture.models>=5,"native body and four armor submissions "+elapsed+" count="+capture.models);
                check(capture.quads>0&&capture.vertices>0,"native held item/shield geometry emitted "+elapsed+" quads="+capture.quads);
                check(capture.blended,"all materials use blending "+elapsed);
                check(!capture.alphas.isEmpty()&&capture.alphas.stream().allMatch(a->a>=0&&a<=Math.round(255*expected)),"all native body/armor/item alpha capped "+elapsed+" samples="+new HashSet<>(capture.alphas));
                check(capture.glintModels>=5&&capture.glintQuads>0,"native animated glint on body armor and held items "+elapsed);
                check(!capture.glintColors.isEmpty()&&capture.glintColors.stream().allMatch(c->(c&255)<=(int)(255*expected)&&((c>>8)&255)<=(int)(255*expected)&&((c>>16)&255)<=(int)(255*expected)),"additive glint RGB intensity follows same cycle "+elapsed+" samples="+new HashSet<>(capture.glintColors));
            }
        }
        other.addEffect(new MobEffectInstance(MobEffects.GLOWING,80));
        // Emulate received server metadata. setGlowingTag alone only owns the server-side tag.
        var syncedFlag=Entity.class.getDeclaredMethod("setSharedFlag",int.class,boolean.class);syncedFlag.setAccessible(true);syncedFlag.invoke(other,6,true);
        var revealed=(LivingEntityRenderState)renderer.createRenderState(other,0);
        check(((VanishState)revealed).vanish$alpha()<0&&revealed.outlineColor!=0,"later Glowing keeps native outline during Stealth");
        var revealCapture=new Capture();renderer.submit(revealed,new PoseStack(),revealCapture.collector,new CameraRenderState());
        check(revealCapture.models>0&&revealCapture.alphas.contains(255)&&revealCapture.glintModels==0,"later reveal bypasses breathing and violet glint suppression");
        other.removeEffect(MobEffects.GLOWING);
        check(VanishVisuals.alpha(other,0)<0,"synced glowing flag alone preserves native reveal");
        syncedFlag.invoke(other,6,false);
        var concealed=(LivingEntityRenderState)renderer.createRenderState(other,0);
        check(((VanishState)concealed).vanish$alpha()==0,"removing later reveal resumes original breathing phase");
        var concealedCapture=new Capture();renderer.submit(concealed,new PoseStack(),concealedCapture.collector,new CameraRenderState());
        check(concealedCapture.models==0&&concealedCapture.quads==0,"resumed zero phase hides body equipment and glint");
        other.removeEffect(RogueEffects.STEALTH.entry);other.setInvisible(false);
        var normal=(LivingEntityRenderState)renderer.createRenderState(other,0);
        check(((VanishState)normal).vanish$alpha()<0,"effect removal restores normal renderer");
        other.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,140));other.setInvisible(true);
        check(((VanishState)renderer.createRenderState(other,0)).vanish$alpha()<0,"ordinary invisibility unaffected");
        var player=mc.player;player.removeEffect(MobEffects.INVISIBILITY);player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_SWORD));player.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.SHIELD));
        mc.gameRenderer.itemInHandRenderer.tick();
        for(int elapsed:new int[]{0,40,80}){
            player.removeEffect(RogueEffects.STEALTH.entry);player.addEffect(new MobEffectInstance(RogueEffects.STEALTH.entry,140-elapsed));player.setInvisible(true);
            var capture=new Capture();mc.gameRenderer.itemInHandRenderer.submitHandsWithItems(0,new PoseStack(),capture.collector,player,15728880);
            if(elapsed==40)check(capture.models==0&&capture.quads==0,"Punchy/native first person at zero fully transparent");
            else{check(capture.models>0||capture.quads>0,"Punchy/native first-person retained at peak "+elapsed+" models="+capture.models+" quads="+capture.quads);check(capture.blended&&capture.alphas.stream().allMatch(a->a<=26),"Punchy/native first-person actual alpha "+elapsed+" values="+new HashSet<>(capture.alphas));check(capture.glintModels>0&&capture.glintQuads>0&&capture.glintColors.stream().allMatch(c->(c&255)<=25&&((c>>8)&255)<=25&&((c>>16)&255)<=25),"Punchy animated glint retained with capped additive intensity "+elapsed);}
        }
        player.removeEffect(RogueEffects.STEALTH.entry);player.setInvisible(false);
    }
    void tick(ClientTickEvent.Post event){
        var mc=Minecraft.getInstance();
        if(mc.player==null||mc.level==null||++ticks<60)return;
        if(done){
            if(++shots<=180){
                if(shots==1){long window=mc.getWindow().handle();org.lwjgl.glfw.GLFW.glfwRestoreWindow(window);org.lwjgl.glfw.GLFW.glfwShowWindow(window);org.lwjgl.glfw.GLFW.glfwSetWindowSize(window,1280,720);}
                int phase=(shots-1)/30;
                mc.setScreenAndShow(null);mc.options.setCameraType(phase<3?CameraType.THIRD_PERSON_FRONT:CameraType.FIRST_PERSON);
                mc.player.setYRot(0);mc.player.setXRot(0);
                mc.player.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.DIAMOND_HELMET));mc.player.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.DIAMOND_CHESTPLATE));mc.player.setItemSlot(EquipmentSlot.LEGS,new ItemStack(Items.DIAMOND_LEGGINGS));mc.player.setItemSlot(EquipmentSlot.FEET,new ItemStack(Items.DIAMOND_BOOTS));
                mc.player.removeEffect(RogueEffects.STEALTH.entry);
                if(phase%3!=2)mc.player.addEffect(new MobEffectInstance(RogueEffects.STEALTH.entry,phase%3==0?140:100));
                mc.player.setInvisible(phase%3!=2);
                if(shots%30==0)pendingShot="SCREENSHOT phase "+phase+" "+(phase<3?"third-person ":"first-person ")+(phase%3==0?"10percent":phase%3==1?"zero":"normal");
                return;
            }
            finish();return;
        }
        done=true;try {proof();}catch(Throwable t){t.printStackTrace();results.add("FAIL "+t);}
    }
    void finish(){
        var mc=Minecraft.getInstance();
        try{Files.write(mc.gameDirectory.toPath().resolve("vanish-results.txt"),results);Files.writeString(mc.gameDirectory.toPath().resolve("vanish-result.txt"),(results.stream().anyMatch(s->s.startsWith("FAIL"))?"FAIL":"PASS")+" "+results.stream().filter(s->s.startsWith("PASS ")).count()+" checks\n");}catch(Exception e){throw new RuntimeException(e);}
        mc.stop();
    }
}
