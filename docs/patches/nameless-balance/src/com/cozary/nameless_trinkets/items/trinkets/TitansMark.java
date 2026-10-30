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

public class TitansMark extends TitansMarkBase implements ICurioItem {

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public void onEquipFromUse(SlotContext slotContext, ItemStack stack) {
        slotContext.entity().playSound(SoundEvents.ARMOR_EQUIP_ELYTRA.value(), 1.0F, 1.0F);
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack pstack, ItemStack stack) {
        Stats config = TitansMarkBase.INSTANCE.getTrinketConfig();
        if (!config.isEnable)
            return;

        LivingEntity livingEntity = slotContext.entity();
        Level world = livingEntity.level();

        if (world.isClientSide())
            return;

        refreshAttributes(livingEntity, config);
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        var entity = slotContext.entity();
        if(entity.level().isClientSide())return;
        if(!INSTANCE.getTrinketConfig().isEnable){
            entity.getAttribute(Attributes.MAX_HEALTH).removeModifier(Identifier.parse("nameless_trinkets:titans_mark_health"));
            entity.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(Identifier.parse("nameless_trinkets:titans_mark_attack_damage"));
            entity.getAttribute(Attributes.SCALE).removeModifier(Identifier.parse("nameless_trinkets:titans_mark_scale"));
            com.cozary.nameless_trinkets.pack.CharacterScaling.clampHealth(entity);return;
        }
        refreshAttributes(entity, INSTANCE.getTrinketConfig());
        entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.SLOWNESS, 1200, 0, true, false));
        entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.RESISTANCE, 100, 0, true, false));
    }

    private static void refreshAttributes(LivingEntity entity, Stats config) {
        com.cozary.nameless_trinkets.pack.CharacterScaling.update(entity,Attributes.MAX_HEALTH,"nameless_trinkets:titans_mark_health",.15,10);
        update(entity.getAttribute(Attributes.SCALE),"titans_mark_scale",config.extraScalePercentage/100D);
        update(entity.getAttribute(Attributes.ATTACK_DAMAGE),"titans_mark_attack_damage",config.attackDamagePercentage/100D);
    }
    private static void update(AttributeInstance instance,String key,double amount) {
        if(instance==null)return;
        var id=Identifier.fromNamespaceAndPath(NamelessTrinkets.MOD_ID,key);
        var old=instance.getModifier(id);
        if(old!=null && old.amount()==amount && old.operation()==AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)return;
        instance.removeModifier(id);
        instance.addTransientModifier(new AttributeModifier(id,amount,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack pstack, ItemStack stack) {
        LivingEntity livingEntity = slotContext.entity();
        livingEntity.getAttribute(Attributes.MAX_HEALTH).removeModifier(Identifier.parse("nameless_trinkets:titans_mark_health"));
        com.cozary.nameless_trinkets.pack.CharacterScaling.clampHealth(livingEntity);

        CommonUtils.removeAttributeModifier(Objects.requireNonNull(livingEntity.getAttribute(Attributes.SCALE)),
                new AttributeModifier(
                        Identifier.fromNamespaceAndPath(NamelessTrinkets.MOD_ID, "titans_mark_scale"),
                        trinketConfig.extraScalePercentage / 100,
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

        CommonUtils.removeAttributeModifier(Objects.requireNonNull(livingEntity.getAttribute(Attributes.ATTACK_DAMAGE)),
                new AttributeModifier(
                        Identifier.fromNamespaceAndPath(NamelessTrinkets.MOD_ID, "titans_mark_attack_damage"),
                        trinketConfig.attackDamagePercentage / 100,
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

}
