package com.emandev.bifrost;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Unit;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

final class ModItems {
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems("bifrost");
    static final DeferredItem<BlockItem> BIFROST_MARKER=ITEMS.registerSimpleBlockItem(ModBlocks.BIFROST_MARKER);
    static final DeferredItem<BifrostFocusItem> HEIMDALLS_SWORD=ITEMS.registerItem("heimdalls_sword",p->new BifrostFocusItem(p,"Hofund"),()->new Item.Properties().sword(ToolMaterial.NETHERITE,3,-2.5f).stacksTo(1));
    static final DeferredItem<BifrostFocusItem> THORS_HAMMER=ITEMS.registerItem("thors_hammer",p->new BifrostFocusItem(p,"Mjolnir"),()->weapon(9,1.2));
    static final DeferredItem<BifrostFocusItem> STORMBREAKER=ITEMS.registerItem("stormbreaker",p->new BifrostFocusItem(p,"Stormbreaker"),()->weapon(10,1).axe(ToolMaterial.NETHERITE,5,-3).component(DataComponents.UNBREAKABLE,Unit.INSTANCE));
    static Item.Properties weapon(double damage,double speed){return new Item.Properties().stacksTo(1).component(DataComponents.UNBREAKABLE,Unit.INSTANCE).attributes(ItemAttributeModifiers.builder().add(Attributes.ATTACK_DAMAGE,new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID,damage-1,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND).add(Attributes.ATTACK_SPEED,new AttributeModifier(Item.BASE_ATTACK_SPEED_ID,speed-4,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND).build());}
    static void register(IEventBus bus){ITEMS.register(bus);}
}
