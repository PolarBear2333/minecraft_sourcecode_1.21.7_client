/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.BaseCoralWallFanBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class BoneMealItem
extends Item {
    public static final int GRASS_SPREAD_WIDTH = 3;
    public static final int GRASS_SPREAD_HEIGHT = 1;
    public static final int GRASS_COUNT_MULTIPLIER = 3;

    public BoneMealItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        Level level = useOnContext.getLevel();
        BlockPos blockPos = useOnContext.getClickedPos();
        BlockPos blockPos2 = blockPos.relative(useOnContext.getClickedFace());
        if (BoneMealItem.growCrop(useOnContext.getItemInHand(), level, blockPos)) {
            if (!level.isClientSide) {
                useOnContext.getPlayer().gameEvent(GameEvent.ITEM_INTERACT_FINISH);
                level.levelEvent(1505, blockPos, 15);
            }
            return InteractionResult.SUCCESS;
        }
        BlockState blockState = level.getBlockState(blockPos);
        boolean bl = blockState.isFaceSturdy(level, blockPos, useOnContext.getClickedFace());
        if (bl && BoneMealItem.growWaterPlant(useOnContext.getItemInHand(), level, blockPos2, useOnContext.getClickedFace())) {
            if (!level.isClientSide) {
                useOnContext.getPlayer().gameEvent(GameEvent.ITEM_INTERACT_FINISH);
                level.levelEvent(1505, blockPos2, 15);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    public static boolean growCrop(ItemStack itemStack, Level level, BlockPos blockPos) {
        BonemealableBlock bonemealableBlock;
        BlockState blockState = level.getBlockState(blockPos);
        Block block = blockState.getBlock();
        if (block instanceof BonemealableBlock && (bonemealableBlock = (BonemealableBlock)((Object)block)).isValidBonemealTarget(level, blockPos, blockState)) {
            if (level instanceof ServerLevel) {
                if (bonemealableBlock.isBonemealSuccess(level, level.random, blockPos, blockState)) {
                    bonemealableBlock.performBonemeal((ServerLevel)level, level.random, blockPos, blockState);
                }
                itemStack.shrink(1);
            }
            return true;
        }
        return false;
    }

    public static boolean growWaterPlant(ItemStack itemStack, Level level, BlockPos blockPos, @Nullable Direction direction) {
        if (!level.getBlockState(blockPos).is(Blocks.WATER) || level.getFluidState(blockPos).getAmount() != 8) {
            return false;
        }
        if (!(level instanceof ServerLevel)) {
            return true;
        }
        RandomSource randomSource = level.getRandom();
        block0: for (int i = 0; i < 128; ++i) {
            BlockPos blockPos2 = blockPos;
            BlockState blockState = Blocks.SEAGRASS.defaultBlockState();
            for (int j = 0; j < i / 16; ++j) {
                if (level.getBlockState(blockPos2 = blockPos2.offset(randomSource.nextInt(3) - 1, (randomSource.nextInt(3) - 1) * randomSource.nextInt(3) / 2, randomSource.nextInt(3) - 1)).isCollisionShapeFullBlock(level, blockPos2)) continue block0;
            }
            Holder<Biome> holder2 = level.getBiome(blockPos2);
            if (holder2.is(BiomeTags.PRODUCES_CORALS_FROM_BONEMEAL)) {
                if (i == 0 && direction != null && direction.getAxis().isHorizontal()) {
                    blockState = BuiltInRegistries.BLOCK.getRandomElementOf(BlockTags.WALL_CORALS, level.random).map(holder -> ((Block)holder.value()).defaultBlockState()).orElse(blockState);
                    if (blockState.hasProperty(BaseCoralWallFanBlock.FACING)) {
                        blockState = (BlockState)blockState.setValue(BaseCoralWallFanBlock.FACING, direction);
                    }
                } else if (randomSource.nextInt(4) == 0) {
                    blockState = BuiltInRegistries.BLOCK.getRandomElementOf(BlockTags.UNDERWATER_BONEMEALS, level.random).map(holder -> ((Block)holder.value()).defaultBlockState()).orElse(blockState);
                }
            }
            if (blockState.is(BlockTags.WALL_CORALS, blockStateBase -> blockStateBase.hasProperty(BaseCoralWallFanBlock.FACING))) {
                for (int j = 0; !blockState.canSurvive(level, blockPos2) && j < 4; ++j) {
                    blockState = (BlockState)blockState.setValue(BaseCoralWallFanBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(randomSource));
                }
            }
            if (!blockState.canSurvive(level, blockPos2)) continue;
            BlockState blockState2 = level.getBlockState(blockPos2);
            if (blockState2.is(Blocks.WATER) && level.getFluidState(blockPos2).getAmount() == 8) {
                level.setBlock(blockPos2, blockState, 3);
                continue;
            }
            if (!blockState2.is(Blocks.SEAGRASS) || !((BonemealableBlock)((Object)Blocks.SEAGRASS)).isValidBonemealTarget(level, blockPos2, blockState2) || randomSource.nextInt(10) != 0) continue;
            ((BonemealableBlock)((Object)Blocks.SEAGRASS)).performBonemeal((ServerLevel)level, randomSource, blockPos2, blockState2);
        }
        itemStack.shrink(1);
        return true;
    }

    public static void addGrowthParticles(LevelAccessor levelAccessor, BlockPos blockPos, int n) {
        BlockState blockState = levelAccessor.getBlockState(blockPos);
        Object object = blockState.getBlock();
        if (object instanceof BonemealableBlock) {
            BonemealableBlock bonemealableBlock = (BonemealableBlock)object;
            object = bonemealableBlock.getParticlePos(blockPos);
            switch (bonemealableBlock.getType()) {
                case NEIGHBOR_SPREADER: {
                    ParticleUtils.spawnParticles(levelAccessor, (BlockPos)object, n * 3, 3.0, 1.0, false, ParticleTypes.HAPPY_VILLAGER);
                    break;
                }
                case GROWER: {
                    ParticleUtils.spawnParticleInBlock(levelAccessor, (BlockPos)object, n, ParticleTypes.HAPPY_VILLAGER);
                }
            }
        } else if (blockState.is(Blocks.WATER)) {
            ParticleUtils.spawnParticles(levelAccessor, blockPos, n * 3, 3.0, 1.0, false, ParticleTypes.HAPPY_VILLAGER);
        }
    }
}

