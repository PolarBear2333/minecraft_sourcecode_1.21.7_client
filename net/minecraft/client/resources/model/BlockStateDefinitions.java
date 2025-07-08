/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.resources.model;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class BlockStateDefinitions {
    private static final StateDefinition<Block, BlockState> ITEM_FRAME_FAKE_DEFINITION = BlockStateDefinitions.createItemFrameFakeState();
    private static final StateDefinition<Block, BlockState> GLOW_ITEM_FRAME_FAKE_DEFINITION = BlockStateDefinitions.createItemFrameFakeState();
    private static final ResourceLocation GLOW_ITEM_FRAME_LOCATION = ResourceLocation.withDefaultNamespace("glow_item_frame");
    private static final ResourceLocation ITEM_FRAME_LOCATION = ResourceLocation.withDefaultNamespace("item_frame");
    private static final Map<ResourceLocation, StateDefinition<Block, BlockState>> STATIC_DEFINITIONS = Map.of(ITEM_FRAME_LOCATION, ITEM_FRAME_FAKE_DEFINITION, GLOW_ITEM_FRAME_LOCATION, GLOW_ITEM_FRAME_FAKE_DEFINITION);

    private static StateDefinition<Block, BlockState> createItemFrameFakeState() {
        return new StateDefinition.Builder(Blocks.AIR).add(BlockStateProperties.MAP).create(Block::defaultBlockState, BlockState::new);
    }

    public static BlockState getItemFrameFakeState(boolean bl, boolean bl2) {
        return (BlockState)(bl ? GLOW_ITEM_FRAME_FAKE_DEFINITION : ITEM_FRAME_FAKE_DEFINITION).any().setValue(BlockStateProperties.MAP, bl2);
    }

    static Function<ResourceLocation, StateDefinition<Block, BlockState>> definitionLocationToBlockStateMapper() {
        HashMap<ResourceLocation, StateDefinition<Block, BlockState>> hashMap = new HashMap<ResourceLocation, StateDefinition<Block, BlockState>>(STATIC_DEFINITIONS);
        for (Block block : BuiltInRegistries.BLOCK) {
            hashMap.put(block.builtInRegistryHolder().key().location(), block.getStateDefinition());
        }
        return hashMap::get;
    }
}

