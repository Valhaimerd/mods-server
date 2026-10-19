package local.worldbosses;

import java.util.Set;
import java.util.HashSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.slf4j.LoggerFactory;

/** Migration at ItemStack construction covers loaded containers, equipment and old loot. */
public final class GearRetirement {
    private GearRetirement() {}
    private static final Set<String> SUFFIXES=Set.of("sword","axe","pickaxe","shovel","hoe","helmet","chestplate","leggings","boots","shield");
    private static final Set<String> LOGGED=new HashSet<>();
    public static boolean retired(Identifier id) {
        if(!id.getNamespace().equals("worldbosses")) return false;
        String p=id.getPath();
        return (p.startsWith("worldbreaker_")||p.startsWith("shadowbreaker_")) && SUFFIXES.contains(p.substring(p.indexOf('_')+1));
    }
    public static boolean hidden(ItemStack stack) {
        Identifier id=BuiltInRegistries.ITEM.getKey(stack.getItem());
        return hiddenId(id);
    }
    public static boolean hiddenId(Identifier id) {
        if(retired(id))return true;
        return id.getNamespace().equals("worldbosses") && Set.of("worldbreaker_alloy","shadowbreaker_alloy","worldbreaker_upgrade_template","shadowbreaker_upgrade_template","worldbreaker_fragment","shadow_fragment").contains(id.getPath());
    }
    public static ItemStack convert(ItemStack old) {
        if(old.isEmpty())return old;
        Identifier id=BuiltInRegistries.ITEM.getKey(old.getItem());
        if(!retired(id)) return old;
        String suffix=id.getPath().substring(id.getPath().indexOf('_')+1);
        Item target=suffix.equals("shield")?Items.SHIELD:BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace("netherite_"+suffix));
        ItemStack result=new ItemStack(target,old.getCount());
        result.copyFrom(DataComponents.CUSTOM_NAME,old);
        result.copyFrom(DataComponents.LORE,old);
        ItemEnchantments.Mutable enchants=new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        for(var entry:old.getOrDefault(DataComponents.ENCHANTMENTS,ItemEnchantments.EMPTY).entrySet()) {
            if(entry.getKey().value().canEnchant(result)) enchants.set(entry.getKey(),entry.getIntValue());
        }
        result.set(DataComponents.ENCHANTMENTS,enchants.toImmutable());
        if(old.isDamageableItem()&&result.isDamageableItem())result.setDamageValue(Math.min(result.getMaxDamage()-1,(int)((long)old.getDamageValue()*result.getMaxDamage()/Math.max(1,old.getMaxDamage()))));
        synchronized(LOGGED) {
            if(LOGGED.add(id.toString()))LoggerFactory.getLogger("WorldBossesRPG").info("Retired gear migration: {} -> {}; custom combat and upgrade components removed",id,BuiltInRegistries.ITEM.getKey(target));
        }
        return result;
    }
}
