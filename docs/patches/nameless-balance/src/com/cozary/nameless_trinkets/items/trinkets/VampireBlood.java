package com.cozary.nameless_trinkets.items.trinkets;

import com.cozary.nameless_trinkets.NamelessTrinkets;
import com.cozary.nameless_trinkets.utils.CommonUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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

public class VampireBlood extends VampireBloodBase implements ICurioItem {
    private static final String TIMER="nameless_vampire_ticks_remaining";
    private static final int LOCK_TICKS=48000;
    private static int remaining(ItemStack stack) {
        return stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.EMPTY).copyTag().getIntOr(TIMER,LOCK_TICKS);
    }
    private static void setRemaining(ItemStack stack,int ticks) {
        net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA,stack,tag->tag.putInt(TIMER,ticks));
    }
    @Override
    public boolean canUnequip(SlotContext context,ItemStack stack) {
        return !INSTANCE.getTrinketConfig().isEnable || !(context.entity() instanceof net.minecraft.world.entity.player.Player player)
                || player.getAbilities().instabuild || remaining(stack)<=0;
    }


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

        Stats config = VampireBloodBase.INSTANCE.getTrinketConfig();
        if (!config.isEnable)
            return;

        LivingEntity livingEntity = slotContext.entity();

        if(!livingEntity.level().isClientSide() && remaining(stack)>0)setRemaining(stack,remaining(stack)-1);

        if(!livingEntity.level().isClientSide()){
            applySpeed(livingEntity,config);
            CommonUtils.applyAttributeModifier(livingEntity.getAttribute(Attributes.ATTACK_DAMAGE),new AttributeModifier(
                Identifier.parse("nameless_trinkets:vampire_blood_attack_damage"),config.damageMultiplierPercentage/100D,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }

        if (!livingEntity.isSpectator()) {


            if (!stack.isEmpty() && livingEntity instanceof ServerPlayer) {

                if (livingEntity.level().isBrightOutside() && livingEntity.level().canSeeSky(livingEntity.blockPosition()) && !livingEntity.level().isClientSide()) {
                    if (livingEntity.fireImmune()) return;

                    ((ServerLevel) livingEntity.level()).sendParticles(ParticleTypes.FLAME, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), 1, 1D, 1D, 1D, 0.01);
                    livingEntity.hurt(livingEntity.damageSources().onFire(), (float) config.sunDamage);

                }
            }
        }
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack pstack, ItemStack stack) {
        Stats config = VampireBloodBase.INSTANCE.getTrinketConfig();
        if (!config.isEnable)
            return;

        LivingEntity livingEntity = slotContext.entity();
        Level world = livingEntity.level();

        if (world.isClientSide())
            return;

        if(!stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.EMPTY).copyTag().contains(TIMER))setRemaining(stack,LOCK_TICKS);

        AttributeInstance attribSpeed = livingEntity.getAttribute(Attributes.ATTACK_DAMAGE);
        AttributeModifier speedModifier = new AttributeModifier(Identifier.fromNamespaceAndPath(NamelessTrinkets.MOD_ID, "vampire_blood_attack_damage"),
                config.damageMultiplierPercentage / 100, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        assert attribSpeed != null;
        CommonUtils.applyAttributeModifier(attribSpeed, speedModifier);
        applySpeed(livingEntity,config);
    }

    private static void applySpeed(LivingEntity entity,Stats config) {
        var attribute=entity.getAttribute(Attributes.MOVEMENT_SPEED);if(attribute==null)return;
        var id=Identifier.fromNamespaceAndPath(NamelessTrinkets.MOD_ID,"vampire_blood_movement_speed");
        double amount=config.movementSpeedPercentage/100D;var old=attribute.getModifier(id);
        if(old!=null && old.amount()==amount && old.operation()==AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)return;
        attribute.removeModifier(id);attribute.addTransientModifier(new AttributeModifier(id,amount,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack pstack, ItemStack stack) {
        if(!slotContext.entity().level().isClientSide())
            net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA,stack,tag->tag.remove(TIMER));
        if(!com.cozary.nameless_trinkets.pack.PackUniqueness.unequip(slotContext,stack))return;
        var speed=slotContext.entity().getAttribute(Attributes.MOVEMENT_SPEED);
        if(speed!=null)speed.removeModifier(Identifier.fromNamespaceAndPath(NamelessTrinkets.MOD_ID,"vampire_blood_movement_speed"));
        CommonUtils.removeAttributeModifier(Objects.requireNonNull(slotContext.entity().getAttribute(Attributes.ATTACK_DAMAGE)),
                new AttributeModifier(Identifier.fromNamespaceAndPath(NamelessTrinkets.MOD_ID, "vampire_blood_attack_damage"),
                        trinketConfig.damageMultiplierPercentage / 100, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

}
