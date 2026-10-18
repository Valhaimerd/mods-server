package net.bandit.many_bows.pack;
import java.util.*;
import java.util.function.Supplier;
import net.bandit.many_bows.config.bows.*;
import net.bandit.many_bows.relic.RelicConfig;
import net.minecraft.core.component.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.rpg_foundation.ranged_weapon.api.*;
import net.rpg_foundation.ranged_weapon.mixin.item.ComponentMapBuilderAccessor;

/** Publish native bow damage to RWA while retaining its original pull-time data. */
@EventBusSubscriber(modid="too_many_bows")
public final class BowRangedBridge {
    private static final Map<String,Supplier<Double>> DAMAGE=Map.ofEntries(
        Map.entry("aethers_call", () -> (double)AethersCallBowConfig.get().base_damage),
        Map.entry("ancient_sage_bow", () -> (double)AncientSageBowConfig.get().base_damage),
        Map.entry("arc_heavens", () -> (double)LightningBowConfig.get().base_damage),
        Map.entry("arcane_bow", () -> (double)ArcaneBowConfig.get().base_damage),
        Map.entry("astral_bound", () -> (double)AstralBoundBowConfig.get().base_damage),
        Map.entry("auroras_grace", () -> (double)AurorasGraceBowConfig.get().base_damage),
        Map.entry("beacon_beam_bow", () -> (double)BeaconBeamBowConfig.get().base_damage),
        Map.entry("burnt_relic", () -> (double)BurntRelicBowConfig.get().base_damage),
        Map.entry("crimson_nexus", () -> (double)CrimsonNexusBowConfig.get().base_damage),
        Map.entry("cyroheart_bow", () -> (double)IcicleJavelinBowConfig.get().base_damage),
        Map.entry("dragons_breath", () -> (double)DragonsBreathBowConfig.get().base_damage),
        Map.entry("necro_flame_bow", () -> (double)NecroFlameBowConfig.get().base_damage),
        Map.entry("dark_bow", () -> (double)SonicBoomBowConfig.get().base_damage),
        Map.entry("demons_grasp", () -> (double)DemonsGraspBowConfig.get().base_damage),
        Map.entry("dusk_reaper", () -> (double)DuskReaperBowConfig.get().base_damage),
        Map.entry("emerald_sage_bow", () -> (double)HunterXpBowConfig.get().base_damage),
        Map.entry("ethereal_hunter", () -> (double)EtherealHunterBowConfig.get().base_damage),
        Map.entry("flame_bow", () -> (double)FlameBowConfig.get().base_damage),
        Map.entry("frostbite", () -> (double)FrostbiteBowConfig.get().base_damage),
        Map.entry("hunter_bow", () -> (double)HunterBowConfig.get().base_damage),
        Map.entry("ironclad_bow", () -> (double)IroncladBowConfig.get().base_damage),
        Map.entry("radiance", () -> (double)RadiantBowConfig.get().base_damage),
        Map.entry("scatter_bow", () -> (double)ScatterBowConfig.get().pellet_damage_fallback),
        Map.entry("sentinels_wrath", () -> (double)SentinelWrathBowConfig.get().base_damage),
        Map.entry("shulker_blast", () -> (double)ShulkerBlastBowConfig.get().base_damage),
        Map.entry("solar_bow", () -> (double)SolarBowConfig.get().base_damage),
        Map.entry("spectral_whisper", () -> (double)SpectralWhisperBowConfig.get().base_damage),
        Map.entry("tidal_bow", () -> (double)TidalBowConfig.get().base_damage),
        Map.entry("torchbearer", () -> (double)TorchbearerBowConfig.get().base_damage),
        Map.entry("twin_shadows", () -> (double)TwinShadowsBowConfig.get().fallback_ranged_damage),
        Map.entry("verdant_vigor", () -> (double)VerdantVigorBowConfig.get().fallback_ranged_damage),
        Map.entry("verdant_viper", () -> (double)VenomBowConfig.get().base_damage),
        Map.entry("vitality_weaver", () -> (double)VitalityWeaverBowConfig.get().base_damage),
        Map.entry("webstring", () -> (double)WebstringBowConfig.get().base_damage),
        Map.entry("wind_bow", () -> (double)WindBowConfig.get().base_damage),
        Map.entry("gravewire_bow", () -> (double)GravewireBowConfig.get().direct_hit_damage),
        Map.entry("vaultpiercer", () -> (double)VaultpiercerBowConfig.get().direct_hit_damage),
        Map.entry("soulhoard", () -> (double)SoulhoardBowConfig.get().direct_hit_damage),
        Map.entry("eventide", () -> (double)RelicConfig.get().eventideDamage),
        Map.entry("worldeater", () -> (double)RelicConfig.get().worldeaterDamage),
        Map.entry("godsplitter", () -> (double)RelicConfig.get().godsplitterDamage),
        Map.entry("blunted_edge", () -> 5d)
    );
    @SubscribeEvent
    public static void components(ModifyDefaultComponentsEvent event){
        for(var item:BuiltInRegistries.ITEM){
            var id=BuiltInRegistries.ITEM.getKey(item);
            if(!id.getNamespace().equals("too_many_bows")||!DAMAGE.containsKey(id.getPath()))continue;
            event.modify(item,(ModifyDefaultComponentsEvent.Initializer)(builder,registries,ignored)->{
                double damage=DAMAGE.get(id.getPath()).get();
                if(!Double.isFinite(damage)||damage<0)throw new IllegalStateException("Invalid bow damage: "+id+"="+damage);
                var entries=((ComponentMapBuilderAccessor)builder).rwa_components();
                var old=(ItemAttributeModifiers)entries.get(DataComponents.ATTRIBUTE_MODIFIERS);
                var changed=ItemAttributeModifiers.builder();
                if(old!=null)for(var entry:old.modifiers())
                    if(!entry.attribute().equals(EntityAttributes_RangedWeapon.DAMAGE.entry)||!entry.modifier().id().equals(AttributeModifierIDs.WEAPON_DAMAGE_ID))
                        changed.add(entry.attribute(),entry.modifier(),entry.slot());
                changed.add(EntityAttributes_RangedWeapon.DAMAGE.entry,new AttributeModifier(AttributeModifierIDs.WEAPON_DAMAGE_ID,damage,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.HAND);
                builder.set(DataComponents.ATTRIBUTE_MODIFIERS,changed.build());
                var props=(RangedWeaponProperties)entries.get(RangedWeaponProperties.TYPE);
                if(props==null)throw new IllegalStateException("Missing RWA properties: "+id);
                builder.set(RangedWeaponProperties.TYPE,new RangedWeaponProperties(Optional.of(damage>0?(float)damage:1f),props.velocity(),props.pull_time()));
            });
        }
    }
    /** These native projectiles already read RWA damage before RWA's spawn hook. */
    public static boolean alreadyScales(String bow,String projectileNamespace){
        if(Set.of("scatter_bow","sentinels_wrath","solar_bow","shulker_blast").contains(bow))return true;
        return projectileNamespace.equals("too_many_bows")&&Set.of("tidal_bow","twin_shadows","verdant_vigor","verdant_viper","vitality_weaver","webstring","wind_bow","gravewire_bow","soulhoard","vaultpiercer").contains(bow);
    }
}
