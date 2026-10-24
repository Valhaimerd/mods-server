package com.emandev.bifrost;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

final class ModBlocks {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks("bifrost");
    // Its inset base, stem and cap do not fill a cube. Solid-cube occlusion hides
    // neighbouring terrain faces through the model's open gaps.
    static final DeferredBlock<BifrostMarkerBlock> BIFROST_MARKER=BLOCKS.registerBlock(
        "bifrost_marker",BifrostMarkerBlock::new,
        ()->BlockBehaviour.Properties.ofFullCopy(Blocks.CRYING_OBSIDIAN).noOcclusion());
    private ModBlocks(){}
    static void register(IEventBus bus){BLOCKS.register(bus);}
}
