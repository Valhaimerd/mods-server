package net.skill_tree_rpgs.compat;

import java.util.*;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.puffish.skillsmod.api.*;
import net.skill_tree_rpgs.mixin.PaladinEquipmentAccess;

/** Server-authoritative hand restriction; inventory contents remain owned by the player. */
@EventBusSubscriber(modid="skill_tree_rpgs")
public final class PaladinDoctrine {
    private static final Identifier CATEGORY=Identifier.parse("skill_tree_rpgs:class_skills");
    private static final String RESERVE="skill_tree_rpgs:paladin_displaced_weapons";
    private static final Map<ServerPlayer,Long> WARNED=new WeakHashMap<>();

    public static boolean isPaladin(ServerPlayer player) {
        return SkillsAPI.getCategory(CATEGORY).flatMap(c->c.getSkill("tdimeso8ajcwiygp"))
                .map(skill->skill.getState(player)==Skill.State.UNLOCKED).orElse(false);
    }
    public static boolean prohibited(ItemStack stack) {
        return stack.is(ItemTags.create(Identifier.parse("rpg_series:weapon_type/healing_staff")))
                ||stack.is(ItemTags.create(Identifier.parse("rpg_series:weapon_type/healing_wand")));
    }
    public static boolean forbiddenHands(ServerPlayer player) {
        return isPaladin(player)&&(prohibited(player.getMainHandItem())||prohibited(player.getOffhandItem()));
    }
    public static List<ItemStack> reserved(ServerPlayer player) {
        var data=player.getPersistentData().getCompoundOrEmpty(Player.PERSISTED_NBT_TAG);
        if(!data.contains(RESERVE)) return new ArrayList<>();
        var ops=player.registryAccess().createSerializationContext(NbtOps.INSTANCE);
        return new ArrayList<>(ItemStack.CODEC.listOf().parse(ops,data.get(RESERVE)).getOrThrow());
    }
    private static void store(ServerPlayer player,List<ItemStack> items) {
        var data=player.getPersistentData().getCompoundOrEmpty(Player.PERSISTED_NBT_TAG).copy();
        if(items.isEmpty()) data.remove(RESERVE);
        else data.store(RESERVE,ItemStack.CODEC.listOf(),player.registryAccess().createSerializationContext(NbtOps.INSTANCE),items);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG,data);
    }
    private static void warn(ServerPlayer player,boolean waiting) {
        long now=player.level().getGameTime();Long last=WARNED.get(player);
        if(last!=null&&now-last<40&&now>=last) return;
        WARNED.put(player,now);
        player.sendOverlayMessage(Component.translatable(waiting?"skill_tree_rpgs.paladin_oath.full":"skill_tree_rpgs.paladin_oath"));
    }
    public static boolean enforce(ServerPlayer player) {return enforce(player,true);}
    /** Called before native equipment collection; do not recursively start another scan. */
    public static void beforeEquipmentScan(ServerPlayer player) {enforce(player,false);}
    private static boolean enforce(ServerPlayer player,boolean scan) {
        boolean paladin=isPaladin(player),changed=false,rejected=false;
        var inventory=player.getInventory();int selected=inventory.getSelectedSlot();
        var reserve=reserved(player);
        // Restore previously displaced items only into genuinely empty legal inventory slots.
        for(int slot=0;slot<36&&!reserve.isEmpty();slot++) {
            if((paladin&&slot==selected)||!inventory.getItem(slot).isEmpty()) continue;
            ItemStack returned=reserve.removeFirst();store(player,reserve);inventory.setItem(slot,returned);changed=true;
        }
        if(paladin)for(var hand:InteractionHand.values()) {
            ItemStack held=player.getItemInHand(hand);
            if(!prohibited(held)) continue;
            rejected=true;player.stopUsingItem();int destination=-1;
            for(int slot=0;slot<36;slot++) if(slot!=selected&&inventory.getItem(slot).isEmpty()){destination=slot;break;}
            if(destination<0) for(int slot=0;slot<36;slot++) if(slot!=selected&&!prohibited(inventory.getItem(slot))){destination=slot;break;}
            if(destination>=0) {
                ItemStack replacement=inventory.getItem(destination);
                inventory.setItem(destination,held);player.setItemInHand(hand,replacement);
            } else {
                // Every storage slot is another prohibited weapon: persist before clearing the hand.
                reserve.add(held.copy());store(player,reserve);player.setItemInHand(hand,ItemStack.EMPTY);
            }
            changed=true;
        }
        if(rejected)warn(player,!reserve.isEmpty());
        if(changed) {
            inventory.setChanged();
            if(player.connection!=null){player.inventoryMenu.broadcastChanges();player.containerMenu.broadcastChanges();}
        }
        // A role/slot change can leave old hand modifiers even when the current hand is legal.
        if(scan&&(changed||paladin))((PaladinEquipmentAccess)player).paladin$detectEquipmentUpdates();
        return rejected;
    }
    /** Reject the initiating action even after the weapon has been moved to storage. */
    public static boolean rejectAction(ServerPlayer player,ItemStack source) {
        boolean rejected=isPaladin(player)&&(prohibited(source)||forbiddenHands(player));
        enforce(player);
        if(rejected)warn(player,!reserved(player).isEmpty());
        return rejected;
    }
    @SubscribeEvent public static void attack(AttackEntityEvent event) {
        if(event.getEntity() instanceof ServerPlayer p&&rejectAction(p,p.getMainHandItem()))event.setCanceled(true);
    }
    @SubscribeEvent public static void useItem(PlayerInteractEvent.RightClickItem event) {
        if(event.getEntity() instanceof ServerPlayer p&&rejectAction(p,event.getItemStack()))event.setCanceled(true);
    }
    @SubscribeEvent public static void useBlock(PlayerInteractEvent.RightClickBlock event) {
        if(event.getEntity() instanceof ServerPlayer p&&rejectAction(p,event.getItemStack()))event.setCanceled(true);
    }
    @SubscribeEvent public static void useEntity(PlayerInteractEvent.EntityInteract event) {
        if(event.getEntity() instanceof ServerPlayer p&&rejectAction(p,event.getItemStack()))event.setCanceled(true);
    }
    @SubscribeEvent public static void mine(PlayerInteractEvent.LeftClickBlock event) {
        if(event.getEntity() instanceof ServerPlayer p&&rejectAction(p,event.getItemStack()))event.setCanceled(true);
    }
    @SubscribeEvent public static void deathDrops(LivingDropsEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer p)||p.level().getGameRules().get(GameRules.KEEP_INVENTORY))return;
        var items=reserved(p);
        if(items.isEmpty())return;
        store(p,List.of());
        for(ItemStack item:items)event.getDrops().add(new ItemEntity(p.level(),p.getX(),p.getY(),p.getZ(),item));
    }
}
