package local.artifacts;

import java.nio.file.*;
import artifacts.Artifacts;
import artifacts.config.value.ConfigValue;
import artifacts.registry.ModDataComponents;
import artifacts.registry.ModItems;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@Mod("artifacts_probe")
public class ArtifactsProbe {
    int checks;
    public ArtifactsProbe(){NeoForge.EVENT_BUS.addListener(this::run);}
    void check(boolean result,String label){if(!result)throw new AssertionError(label);checks++;System.out.println("ARTIFACTS_PASS "+label);}
    @SuppressWarnings({"rawtypes","unchecked"})
    void value(ConfigValue value,Number expected){
        check(Math.abs(((Number)value.get()).doubleValue()-expected.doubleValue())<1e-8,value.getKey()+" effective value");
        if(expected instanceof Integer)value.set(Integer.valueOf(999));else value.set(Double.valueOf(999));
        check(Math.abs(((Number)value.get()).doubleValue()-expected.doubleValue())<1e-8,value.getKey()+" overrides stale config");
        check(Math.abs(((Number)value.getDefaultValue()).doubleValue()-expected.doubleValue())<1e-8,value.getKey()+" default");
    }
    void run(ServerStartedEvent event){
        try {
            var c=Artifacts.CONFIG.items;
            value(c.crossNecklace.cooldown,5);
            value(c.panicNecklace.speedLevel,1);value(c.panicNecklace.speedDuration,5);value(c.panicNecklace.cooldown,7);
            value(c.shockPendant.cooldown,3);value(c.thornPendant.cooldown,3);
            value(c.crystalHeart.healthBonus,8D);value(c.feralClaws.attackSpeedBonus,.15D);
            value(c.vampiricGlove.maxHealingPerHit,4);value(c.vampiricGlove.absorptionRatio,.1D);
            value(c.runningShoes.sprintingSpeedBonus,.2D);value(c.powerGlove.attackDamageBonus,2D);
            value(c.pocketPiston.attackKnockbackBonus,.3D);value(c.steadfastSpikes.knockbackResistance,.5D);
            value(c.charmOfShrinking.scaleModifier,-.5D);
            value(c.obsidianSkull.fireResistanceDuration,30);value(c.obsidianSkull.cooldown,60);
            value(c.steadfastSpikes.slipperinessReduction,1D);
            c.flippers.swimSpeedBonus.set(.33D);check(c.flippers.swimSpeedBonus.get()==.33D,"unrelated config remains configurable");
            var charm=new ItemStack(ModItems.CHARM_OF_SHRINKING.get());
            var modifiers=charm.get(ModDataComponents.ATTRIBUTE_MODIFIERS.get()).entries();
            check(modifiers.stream().anyMatch(m->m.attribute().equals(Attributes.SCALE)&&m.amount().get()==-.5D),"half scale retained");
            check(modifiers.stream().anyMatch(m->m.attribute().equals(Attributes.MOVEMENT_SPEED)&&Math.abs(m.amount().get()-.2D)<1e-8&&m.operation()==AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),"charm movement x1.2");
            var effects=charm.get(ModDataComponents.MOB_EFFECTS.get());
            check(effects!=null,"charm has refreshing effect component");
            var weak=effects.entries().stream().filter(e->e.provider().mobEffect().equals(MobEffects.WEAKNESS)).findFirst().orElseThrow();
            check(weak.provider().createEffect().getAmplifier()==0,"Weakness I");
            check(weak.provider().createEffect().getDuration()==1219,"Weakness 60 seconds plus native 19-tick refresh padding");
            var player=net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(event.getServer().overworld());
            var ticker=new artifacts.component.ability.mobeffect.EquipmentMobEffect.Ticker();
            ticker.wornTick(weak,null,player,false,false);
            check(player.getEffect(MobEffects.WEAKNESS).getDuration()==1219,"active charm applies Weakness");
            player.removeEffect(MobEffects.WEAKNESS);
            ticker.wornTick(weak,null,player,false,true);
            check(!player.hasEffect(MobEffects.WEAKNESS),"disabled charm does not apply Weakness");
            ticker.wornTick(weak,null,player,true,false);
            check(!player.hasEffect(MobEffects.WEAKNESS),"cooldown charm does not apply Weakness");
            var skull=new ItemStack(ModItems.OBSIDIAN_SKULL.get());
            var fire=skull.get(ModDataComponents.POST_DAMAGE_EFFECTS.get()).entries().getFirst().provider().createEffect();
            check(fire.getAmplifier()==1&&fire.getDuration()==619,"Fire Resistance II 30 seconds plus native padding");
            Files.writeString(Path.of("result.txt"),"PASS "+checks+" checks\n");
        }catch(Throwable t){t.printStackTrace();try{Files.writeString(Path.of("result.txt"),"FAIL "+t);}catch(Exception ignored){}}
        finally{event.getServer().halt(false);}
    }
}
