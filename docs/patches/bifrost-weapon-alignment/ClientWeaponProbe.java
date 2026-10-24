package local.bifrostweapons;

import java.nio.file.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.spell_engine.api.spell.container.SpellContainerHelper;
import net.bettercombat.logic.WeaponRegistry;
import net.neoforged.fml.common.Mod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@Mod(value="bifrost_weapon_probe",dist=Dist.CLIENT)
public class ClientWeaponProbe {
    int ticks;boolean done,hidden;List<String> results=new ArrayList<>();
    public ClientWeaponProbe(){NeoForge.EVENT_BUS.addListener(this::tick);}
    void check(boolean ok,String label){results.add((ok?"PASS ":"FAIL ")+label);}
    ItemStack stack(String id){return new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(id)));}
    double value(ItemStack s,net.minecraft.core.Holder<Attribute> attr,double base){
        for(var e:s.get(DataComponents.ATTRIBUTE_MODIFIERS).modifiers())
            if(e.attribute().equals(attr)&&e.modifier().operation()==AttributeModifier.Operation.ADD_VALUE)base+=e.modifier().amount();
        return base;
    }
    void tick(ClientTickEvent.Post event){
        var mc=Minecraft.getInstance();
        if(!hidden){org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());hidden=true;}
        if(done||mc.player==null||mc.level==null||++ticks<40)return;
        try{
            if(ticks==40){
                for(String[] row:new String[][]{
                    {"bifrost:stormbreaker","paladins:netherite_great_hammer","rpg_series:ground_slam"},
                    {"bifrost:thors_hammer","paladins:netherite_mace","rpg_series:smash"},
                    {"bifrost:heimdalls_sword","minecraft:diamond_sword","rpg_series:swift_strikes"}}){
                    var s=stack(row[0]);var ref=stack(row[1]);
                    check(s.get(DataComponents.ATTRIBUTE_MODIFIERS).equals(ref.get(DataComponents.ATTRIBUTE_MODIFIERS)),row[0]+" matches reference attribute components");
                    var weapon=WeaponRegistry.getAttributes(s);var target=WeaponRegistry.getAttributes(ref);
                    // WeaponAttributes contains an array; its equals compares array identities.
                    var gson=new com.google.gson.Gson();
                    check(weapon!=null&&gson.toJson(weapon).equals(gson.toJson(target)),row[0]+" matches native combat profile");
                    var container=SpellContainerHelper.containerFromItemStack(s);
                    check(container!=null&&container.spell_ids().contains(row[2]),row[0]+" grants "+row[2]);
                    results.add("STATS "+row[0]+" damage="+value(s,Attributes.ATTACK_DAMAGE,1)+" speed="+value(s,Attributes.ATTACK_SPEED,4)+" combat="+weapon);
                }
                var storm=stack("bifrost:stormbreaker");
                check(Math.abs(value(storm,Attributes.ATTACK_SPEED,4)-0.8)<0.0001,"Stormbreaker 0.8 attack speed");
                check(WeaponRegistry.getAttributes(storm)!=null&&WeaponRegistry.getAttributes(storm).isTwoHanded(),"Stormbreaker two-handed hammer");
                check(storm.has(DataComponents.UNBREAKABLE)&&stack("bifrost:thors_hammer").has(DataComponents.UNBREAKABLE),"bound weapons remain unbreakable");
                mc.setScreenAndShow(null);mc.player.setItemInHand(InteractionHand.MAIN_HAND,storm);mc.player.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
                mc.player.setXRot(0);mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);
            }
            if(ticks==60)Screenshot.grab(mc,false);
            if(ticks<70)return;
        }catch(Throwable t){t.printStackTrace();results.add("FAIL "+t);}
        done=true;
        try{
            Files.write(mc.gameDirectory.toPath().resolve("weapon-results.txt"),results);
            Files.writeString(mc.gameDirectory.toPath().resolve("weapon-result.txt"),(results.stream().anyMatch(s->s.startsWith("FAIL"))?"FAIL":"PASS")+" "+results.stream().filter(s->s.startsWith("PASS")).count()+" checks\n");
        }catch(Exception e){throw new RuntimeException(e);}
        mc.stop();
    }
}
