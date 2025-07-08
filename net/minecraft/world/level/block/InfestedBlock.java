/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.block;

import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class InfestedBlock
extends Block {
    public static final MapCodec<InfestedBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)BuiltInRegistries.BLOCK.byNameCodec().fieldOf("host").forGetter(InfestedBlock::getHostBlock), InfestedBlock.propertiesCodec()).apply((Applicative)instance, InfestedBlock::new));
    private final Block hostBlock;
    private static final Map<Block, Block> BLOCK_BY_HOST_BLOCK = Maps.newIdentityHashMap();
    private static final Map<BlockState, BlockState> HOST_TO_INFESTED_STATES = Maps.newIdentityHashMap();
    private static final Map<BlockState, BlockState> INFESTED_TO_HOST_STATES = Maps.newIdentityHashMap();

    public MapCodec<? extends InfestedBlock> codec() {
        return CODEC;
    }

    public InfestedBlock(Block block, BlockBehaviour.Properties properties) {
        super(properties.destroyTime(block.defaultDestroyTime() / 2.0f).explosionResistance(0.75f));
        this.hostBlock = block;
        BLOCK_BY_HOST_BLOCK.put(block, this);
    }

    public Block getHostBlock() {
        return this.hostBlock;
    }

    public static boolean isCompatibleHostBlock(BlockState blockState) {
        return BLOCK_BY_HOST_BLOCK.containsKey(blockState.getBlock());
    }

    private void spawnInfestation(ServerLevel serverLevel, BlockPos blockPos) {
        Silverfish silverfish = EntityType.SILVERFISH.create(serverLevel, EntitySpawnReason.TRIGGERED);
        if (silverfish != null) {
            silverfish.snapTo((double)blockPos.getX() + 0.5, blockPos.getY(), (double)blockPos.getZ() + 0.5, 0.0f, 0.0f);
            serverLevel.addFreshEntity(silverfish);
            silverfish.spawnAnim();
        }
    }

    @Override
    protected void spawnAfterBreak(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack, boolean bl) {
        super.spawnAfterBreak(blockState, serverLevel, blockPos, itemStack, bl);
        if (serverLevel.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS) && !EnchantmentHelper.hasTag(itemStack, EnchantmentTags.PREVENTS_INFESTED_SPAWNS)) {
            this.spawnInfestation(serverLevel, blockPos);
        }
    }

    public static BlockState infestedStateByHost(BlockState blockState) {
        return InfestedBlock.getNewStateWithProperties(HOST_TO_INFESTED_STATES, blockState, () -> BLOCK_BY_HOST_BLOCK.get(blockState.getBlock()).defaultBlockState());
    }

    public BlockState hostStateByInfested(BlockState blockState) {
        return InfestedBlock.getNewStateWithProperties(INFESTED_TO_HOST_STATES, blockState, () -> this.getHostBlock().defaultBlockState());
    }

    private static BlockState getNewStateWithProperties(Map<BlockState, BlockState> map, BlockState blockState2, Supplier<BlockState> supplier) {
        return map.computeIfAbsent(blockState2, blockState -> {
            BlockState blockState2 = (BlockState)supplier.get();
            for (Property<?> property : blockState.getProperties()) {
                blockState2 = blockState2.hasProperty(property) ? (BlockState)blockState2.setValue(property, blockState.getValue(property)) : blockState2;
            }
            return blockState2;
        });
    }
}

