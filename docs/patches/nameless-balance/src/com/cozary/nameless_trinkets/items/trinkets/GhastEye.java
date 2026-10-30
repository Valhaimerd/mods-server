package com.cozary.nameless_trinkets.items.trinkets;

import com.cozary.nameless_trinkets.NamelessTrinkets;
import com.cozary.nameless_trinkets.utils.CommonUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.Objects;

public class GhastEye extends GhastEyeBase implements ICurioItem {

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public void onEquipFromUse(SlotContext slotContext, ItemStack stack) {
        slotContext.entity().playSound(SoundEvents.ARMOR_EQUIP_ELYTRA.value(), 1.0F, 1.0F);
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        Stats config = GhastEyeBase.INSTANCE.getTrinketConfig();
        if(!config.isEnable){
            if(!slotContext.entity().level().isClientSide()){
                slotContext.entity().getAttribute(Attributes.MAX_HEALTH).removeModifier(Identifier.parse("nameless_trinkets:ghast_eye_extra_health"));
                com.cozary.nameless_trinkets.pack.CharacterScaling.clampHealth(slotContext.entity());
            }
            return;
        }

        LivingEntity livingEntity = slotContext.entity();
        Level world = livingEntity.level();

        if (world.isClientSide())
            return;

        com.cozary.nameless_trinkets.pack.CharacterScaling.update(livingEntity,Attributes.MAX_HEALTH,
            "nameless_trinkets:ghast_eye_extra_health",.075,6);

    }


    @Override
    public void onUnequip(SlotContext slotContext, ItemStack pstack, ItemStack stack) {
        CommonUtils.removeAttributeModifier(Objects.requireNonNull(slotContext.entity().getAttribute(Attributes.MAX_HEALTH)),
                new AttributeModifier(
                        Identifier.fromNamespaceAndPath(NamelessTrinkets.MOD_ID, "ghast_eye_extra_health"),
                        trinketConfig.extraHearts,
                        AttributeModifier.Operation.ADD_VALUE));
        com.cozary.nameless_trinkets.pack.CharacterScaling.clampHealth(slotContext.entity());
    }
}
