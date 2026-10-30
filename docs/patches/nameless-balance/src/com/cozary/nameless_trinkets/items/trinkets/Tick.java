package com.cozary.nameless_trinkets.items.trinkets;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;

public class Tick extends TickBase implements ICurioItem {


    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public void onEquipFromUse(SlotContext slotContext, ItemStack stack) {
        slotContext.entity().playSound(SoundEvents.ARMOR_EQUIP_ELYTRA.value(), 1.0F, 1.0F);
    }

    @Override
    public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
        LivingEntity livingEntity = slotContext.entity();
        Player player = (Player) livingEntity;
        return livingEntity.isOnFire() || player.getAbilities().instabuild;
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {

        Stats config = TickBase.INSTANCE.getTrinketConfig();
        if (!config.isEnable)
            return;

        if (!(slotContext.entity() instanceof ServerPlayer player) || player.isSpectator())
            return;


        if(stack.isEmpty() || (player.getHealth()<5 && player.getFoodData().getFoodLevel()<5))return;
        var targets=player.level().getEntitiesOfClass(LivingEntity.class,player.getBoundingBox().inflate(8),
            target->TickBase.isValidTarget(target) && com.cozary.nameless_trinkets.pack.AccessoryCombat.enemy(player,target)
                && player.distanceToSqr(target)<=64 && target.getMaxHealth()>50 && target.getHealth()>target.getMaxHealth()/2);
        if(targets.isEmpty() || !com.cozary.nameless_trinkets.pack.AccessoryCombat.ready(player,"tick",20))return;
        boolean hit=false;
        for(var target:targets)hit|=com.cozary.nameless_trinkets.pack.AccessoryCombat.damage(player,target,
            com.cozary.nameless_trinkets.pack.AccessoryCombat.scaled(player,1,.1,6));
        if(hit){
            if(player.getHealth()>=5)player.hurtServer((net.minecraft.server.level.ServerLevel)player.level(),player.damageSources().generic(),player.getMaxHealth()*.05f);
            else player.causeFoodExhaustion(player.getFoodData().getFoodLevel()*.1f);
        }
    }
}
