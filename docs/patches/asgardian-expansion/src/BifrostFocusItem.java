package com.emandev.bifrost;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;

final class BifrostFocusItem extends Item {
    private final String displayName;
    BifrostFocusItem(Item.Properties properties,String name){super(properties);displayName=name;}
    @Override public InteractionResult useOn(UseOnContext context){
        var p=context.getPlayer();if(!AsgardianAccess.allow(p))return InteractionResult.FAIL;
        if(!(p instanceof ServerPlayer player))return context.getLevel().getBlockState(context.getClickedPos()).is(ModBlocks.BIFROST_MARKER.get())?InteractionResult.SUCCESS:use(context.getLevel(),p,context.getHand());
        if(!WeaponOwnership.canUse(player,context.getItemInHand()))return InteractionResult.FAIL;
        if(context.getLevel().getBlockState(context.getClickedPos()).is(ModBlocks.BIFROST_MARKER.get())){
            var marker=BifrostMarkerService.register(player.level(),context.getClickedPos());BifrostMenuService.open(player,marker.id(),displayName);return InteractionResult.SUCCESS_SERVER;
        }
        return use(context.getLevel(),p,context.getHand());
    }
    @Override public InteractionResult use(Level level,Player given,InteractionHand hand){
        // Weapon use is available to Spell Engine's native melee skill. Bifrost
        // itself is an appended hotbar skill, never an item-use channel.
        return InteractionResult.PASS;
    }
    @Override public int getUseDuration(ItemStack stack,LivingEntity entity){return 0;}
    @Override public ItemUseAnimation getUseAnimation(ItemStack stack){return ItemUseAnimation.NONE;}
    @Override public boolean releaseUsing(ItemStack stack,Level level,LivingEntity entity,int remaining){if(BifrostSkills.customAbilitiesEnabled()&&entity instanceof ServerPlayer p)AsgardAbilities.endPrimary(p);return true;}
}
