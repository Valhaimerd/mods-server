package com.cozary.nameless_trinkets.items.subTrinket;

import net.minecraft.world.item.Item;


public abstract class TrinketItem<T extends TrinketsStats> extends Item implements top.theillusivec4.curios.api.type.capability.ICurioItem {

    @Override
    public boolean canEquip(top.theillusivec4.curios.api.SlotContext context,net.minecraft.world.item.ItemStack stack) {
        return com.cozary.nameless_trinkets.pack.PackUniqueness.canEquip(context,stack);
    }

    protected TrinketData trinketData;
    protected T trinketConfig;

    public TrinketItem(TrinketData trinketData) {
        super(trinketData.getItemRarity() == null
                ? trinketData.getItemProperties()
                : trinketData.getItemProperties().rarity(trinketData.getItemRarity()));
        setTrinketData(trinketData);
        try {
            setTrinketConfig(trinketData.getStatsClass().getDeclaredConstructor().newInstance());
        } catch (Exception e) {
            throw new RuntimeException("Error instantiating config for Trinket Item", e);
        }
    }

    public TrinketData getTrinketData() {
        return trinketData;
    }

    public void setTrinketData(TrinketData trinketData) {
        this.trinketData = trinketData;
    }

    public T getTrinketConfig() {
        return trinketConfig;
    }

    public void setTrinketConfig(TrinketsStats trinketConfig) {
        com.cozary.nameless_trinkets.pack.PackBalance.apply(trinketConfig);
        this.trinketConfig = (T) trinketConfig;
    }
}
