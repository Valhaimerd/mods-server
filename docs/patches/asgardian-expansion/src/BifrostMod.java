package com.emandev.bifrost;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.*;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod("bifrost") public final class BifrostMod {
    static final String MOD_ID="bifrost";
    public BifrostMod(IEventBus bus){
        AsgardianAccess.verifyIntegration();ModBlocks.register(bus);ModItems.register(bus);TransitSounds.register(bus);
        bus.addListener(BifrostNetworking::register);bus.addListener(AsgardNetwork::register);
        NeoForge.EVENT_BUS.addListener(BifrostCommands::register);
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post e)->{WeaponOwnership.tick(e.getServer());AsgardAbilities.tick(e.getServer());BifrostTeleportManager.tick(e.getServer());});
        NeoForge.EVENT_BUS.addListener((ServerStartedEvent e)->{BifrostMenuService.reset();AsgardAbilities.reset();BifrostTeleportManager.start(e.getServer());});
        NeoForge.EVENT_BUS.addListener((ServerStoppingEvent e)->{BifrostMenuService.reset();AsgardAbilities.reset();BifrostTeleportManager.stop(e.getServer());});
        if(FMLEnvironment.getDist()==Dist.CLIENT){BifrostClient.register(bus);AsgardClient.register(bus);}
    }
    static Identifier id(String name){return Identifier.fromNamespaceAndPath("bifrost",name);}
}
