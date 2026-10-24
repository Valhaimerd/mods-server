package com.emandev.bifrost;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Unit;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.spell_engine.api.spell.SpellDataComponents;
import net.spell_engine.api.spell.container.SpellContainer;
import net.spell_engine.rpg_series.item.Weapons;
import net.spell_engine.rpg_series.item.Weapon;
import net.paladins.PaladinsMod;

final class ModItems {
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems("bifrost");
    static final DeferredItem<BlockItem> BIFROST_MARKER=ITEMS.registerSimpleBlockItem(ModBlocks.BIFROST_MARKER);
    static final DeferredItem<BifrostFocusItem> HEIMDALLS_SWORD=ITEMS.registerItem("heimdalls_sword",p->new BifrostFocusItem(p,"Hofund"),()->new Item.Properties().sword(ToolMaterial.DIAMOND,3,-2.4f).stacksTo(1));
    static final DeferredItem<BifrostFocusItem> THORS_HAMMER=ITEMS.registerItem("thors_hammer",p->new BifrostFocusItem(p,"Mjolnir"),()->weapon(9,1.2));
    static final DeferredItem<BifrostFocusItem> STORMBREAKER=ITEMS.registerItem("stormbreaker",p->new BifrostFocusItem(p,"Stormbreaker"),()->weapon(10,0.8));
    static Item.Properties weapon(double damage,double speed){return new Item.Properties().stacksTo(1).component(DataComponents.UNBREAKABLE,Unit.INSTANCE).attributes(ItemAttributeModifiers.builder().add(Attributes.ATTACK_DAMAGE,new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID,damage-1,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND).add(Attributes.ATTACK_SPEED,new AttributeModifier(Item.BASE_ATTACK_SPEED_ID,speed-4,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND).build());}
    static void register(IEventBus bus){ITEMS.register(bus);bus.addListener(ModItems::weaponDefaults);}
    private static void weaponDefaults(ModifyDefaultComponentsEvent event){
        match(event,HEIMDALLS_SWORD.get(),"minecraft:diamond_sword",Weapons.SWORD_CONTAINER);
        match(event,THORS_HAMMER.get(),"paladins:netherite_mace",Weapons.MACE_CONTAINER);
        match(event,STORMBREAKER.get(),"paladins:netherite_great_hammer",Weapons.HAMMER_CONTAINER);
    }
    private static void match(ModifyDefaultComponentsEvent event,Item item,String reference,SpellContainer spells){
        event.modify(item,(builder,registries,changedItem)->{
            // Item components are not bound yet at this reload stage. Use the same
            // configured attribute factory as Paladins, rather than reading Item.components().
            if(reference.startsWith("paladins:")){
                var config=PaladinsMod.itemConfig.value.weapons.get(reference.substring("paladins:".length()));
                if(config==null)throw new IllegalStateException("Missing RPG reference weapon config: "+reference);
                builder.set(DataComponents.ATTRIBUTE_MODIFIERS,Weapon.attributesFrom(config));
            }
            builder.set(SpellDataComponents.SPELL_CONTAINER,spells);
        });
    }
}
