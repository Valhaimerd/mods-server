package net.bandit.many_bows.loot;

import dev.architectury.event.events.common.LootEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.bandit.many_bows.config.BowLootConfig;
import net.bandit.many_bows.config.ManyBowsConfigHolder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public final class ModLootModifiers {
    private ModLootModifiers() {
    }

    public static void registerLootModifiers() {
        LootEvent.MODIFY_LOOT_TABLE.register((key, context, builtin) -> {
            LootPool.Builder pool;
            if (!builtin) {
                return;
            }
            BowLootConfig config = ManyBowsConfigHolder.get();
            if (Boolean.TRUE.equals(config.easyLootEnabled) && ModLootModifiers.parseLootTables(config.easyLootTables).contains(key) && (pool = ModLootModifiers.createConfigDrivenPool(config.easyLootDropChance.floatValue(), config.easyLootItems)) != null) {
                context.addPool(pool);
            }
            if (Boolean.TRUE.equals(config.mediumLootEnabled) && ModLootModifiers.parseLootTables(config.mediumLootTables).contains(key) && (pool = ModLootModifiers.createConfigDrivenPool(config.mediumLootDropChance.floatValue(), config.mediumLootItems)) != null) {
                context.addPool(pool);
            }
            if (Boolean.TRUE.equals(config.hardLootEnabled) && ModLootModifiers.parseLootTables(config.hardLootTables).contains(key) && (pool = ModLootModifiers.createConfigDrivenPool(config.hardLootDropChance.floatValue(), config.hardLootItems)) != null) {
                context.addPool(pool);
            }
            if (Boolean.TRUE.equals(config.endgameLootEnabled) && ModLootModifiers.parseLootTables(config.endgameLootTables).contains(key) && (pool = ModLootModifiers.createConfigDrivenPool(config.endgameLootDropChance.floatValue(), config.endgameLootItems)) != null) {
                context.addPool(pool);
            }
        });
    }

    private static Set<ResourceKey<LootTable>> parseLootTables(List<String> ids) {
        HashSet<ResourceKey<LootTable>> out = new HashSet<ResourceKey<LootTable>>();
        if (ids == null) {
            return out;
        }
        for (String value : ids) {
            Identifier id = Identifier.tryParse((String)value);
            if (id == null) continue;
            out.add((ResourceKey<LootTable>)ResourceKey.create((ResourceKey)Registries.LOOT_TABLE, (Identifier)id));
        }
        return out;
    }

    private static LootPool.Builder createConfigDrivenPool(float chance, List<String> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return null;
        }
        if ((chance = Math.max(0.0f, Math.min(1.0f, chance))) <= 0.0f) {
            return null;
        }
        LootPool.Builder pool = LootPool.lootPool().setRolls((NumberProvider)ConstantValue.exactly((float)1.0f)).when(LootItemRandomChanceCondition.randomChance((float)chance));
        boolean added = false;
        for (String value : itemIds) {
            if(net.bandit.many_bows.pack.PackPolicy.REMOVED.contains(value)) continue;
            Optional item;
            Identifier id = Identifier.tryParse((String)value);
            if (id == null || !(item = BuiltInRegistries.ITEM.get(id)).isPresent()) continue;
            pool.add((LootPoolEntryContainer.Builder)LootItem.lootTableItem((ItemLike)((ItemLike)((Holder.Reference)item.get()).value())));
            added = true;
        }
        return added ? pool : null;
    }
}
