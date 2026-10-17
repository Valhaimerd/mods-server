package local.tradeprobe;

import java.nio.file.*;
import java.lang.reflect.*;
import java.util.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.npc.villager.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.trading.*;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.majo24.naturally_trimmed.config.Config;
import de.tradeview.PreviewVillager;

@Mod("trade_probe")
public class TradeProbe {
    int checks;
    public TradeProbe() { NeoForge.EVENT_BUS.addListener(this::started); }
    void check(boolean value, String name) {
        if (!value) throw new AssertionError(name);
        checks++;
        System.out.println("TRADE_PASS " + name);
    }
    MerchantOffer offer() {
        return new MerchantOffer(new ItemCost(Items.EMERALD, 1), new ItemStack(Items.IRON_CHESTPLATE), 12, 1, 0.05f);
    }
    void started(ServerStartedEvent event) {
        var server = event.getServer();
        server.execute(() -> {
            try {
                var config = Config.CONFIG_MANAGER.instance();
                config.enableTrimTrades = true;
                config.trimTrades.minLevel = 1;
                config.trimTrades.trimChance = 100;
                var villager = EntityTypes.VILLAGER.create(server.overworld(), EntitySpawnReason.COMMAND);
                villager.setVillagerData(villager.getVillagerData().withProfession(
                    BuiltInRegistries.VILLAGER_PROFESSION.getOrThrow(VillagerProfession.ARMORER)).withLevel(1));
                var initial = villager.getOffers();
                check(!initial.isEmpty(), "normal initial villager trades");
                var preview = ((PreviewVillager) villager).tradeview$preview();
                check(preview.get(2).size() == 1, "actual TradeView preview with one available trade and two requested");
                check(villager.getVillagerData().level() == 1, "preview restores villager level");
                check(villager.getOffers() == initial, "preview preserves current offers");
                Method wrapper = Arrays.stream(AbstractVillager.class.getDeclaredMethods())
                    .filter(m -> m.getName().contains("trimTradesWithoutDuplicates")).findFirst().orElseThrow();
                wrapper.setAccessible(true);
                for (int existing : new int[] {0, 2}) for (int added : new int[] {0, 1, 2, 4}) {
                    var offers = new MerchantOffers();
                    for (int i = 0; i < existing; i++) offers.add(offer());
                    Operation<Object> append = args -> { for (int i = 0; i < added; i++) offers.add(offer()); return null; };
                    wrapper.invoke(villager, null, offers, null, 2, append);
                    check(offers.size() == existing + added, "offer count " + existing + "+" + added);
                    for (int i = 0; i < offers.size(); i++)
                        check(offers.get(i).getResult().has(DataComponents.TRIM) == (i >= existing),
                            "trim only appended offer " + existing + "+" + added + " index " + i);
                }
                Files.writeString(Path.of("result.txt"), "PASS " + checks + " checks\n");
            } catch (Throwable ex) {
                ex.printStackTrace();
                try { Files.writeString(Path.of("result.txt"), "FAIL " + ex + "\n"); } catch (Exception ignored) { }
            } finally { server.halt(false); }
        });
    }
}
