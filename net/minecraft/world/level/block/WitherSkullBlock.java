/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;

public class WitherSkullBlock
extends SkullBlock {
    public static final MapCodec<WitherSkullBlock> CODEC = WitherSkullBlock.simpleCodec(WitherSkullBlock::new);
    @Nullable
    private static BlockPattern witherPatternFull;
    @Nullable
    private static BlockPattern witherPatternBase;

    public MapCodec<WitherSkullBlock> codec() {
        return CODEC;
    }

    protected WitherSkullBlock(BlockBehaviour.Properties properties) {
        super(SkullBlock.Types.WITHER_SKELETON, properties);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
        WitherSkullBlock.checkSpawn(level, blockPos);
    }

    public static void checkSpawn(Level level, BlockPos blockPos) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof SkullBlockEntity) {
            SkullBlockEntity skullBlockEntity = (SkullBlockEntity)blockEntity;
            WitherSkullBlock.checkSpawn(level, blockPos, skullBlockEntity);
        }
    }

    public static void checkSpawn(Level level, BlockPos blockPos, SkullBlockEntity skullBlockEntity) {
        boolean bl;
        if (level.isClientSide) {
            return;
        }
        BlockState blockState = skullBlockEntity.getBlockState();
        boolean bl2 = bl = blockState.is(Blocks.WITHER_SKELETON_SKULL) || blockState.is(Blocks.WITHER_SKELETON_WALL_SKULL);
        if (!bl || blockPos.getY() < level.getMinY() || level.getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }
        BlockPattern.BlockPatternMatch blockPatternMatch = WitherSkullBlock.getOrCreateWitherFull().find(level, blockPos);
        if (blockPatternMatch == null) {
            return;
        }
        WitherBoss witherBoss = EntityType.WITHER.create(level, EntitySpawnReason.TRIGGERED);
        if (witherBoss != null) {
            CarvedPumpkinBlock.clearPatternBlocks(level, blockPatternMatch);
            BlockPos blockPos2 = blockPatternMatch.getBlock(1, 2, 0).getPos();
            witherBoss.snapTo((double)blockPos2.getX() + 0.5, (double)blockPos2.getY() + 0.55, (double)blockPos2.getZ() + 0.5, blockPatternMatch.getForwards().getAxis() == Direction.Axis.X ? 0.0f : 90.0f, 0.0f);
            witherBoss.yBodyRot = blockPatternMatch.getForwards().getAxis() == Direction.Axis.X ? 0.0f : 90.0f;
            witherBoss.makeInvulnerable();
            for (ServerPlayer serverPlayer : level.getEntitiesOfClass(ServerPlayer.class, witherBoss.getBoundingBox().inflate(50.0))) {
                CriteriaTriggers.SUMMONED_ENTITY.trigger(serverPlayer, witherBoss);
            }
            level.addFreshEntity(witherBoss);
            CarvedPumpkinBlock.updatePatternBlocks(level, blockPatternMatch);
        }
    }

    public static boolean canSpawnMob(Level level, BlockPos blockPos, ItemStack itemStack) {
        if (itemStack.is(Items.WITHER_SKELETON_SKULL) && blockPos.getY() >= level.getMinY() + 2 && level.getDifficulty() != Difficulty.PEACEFUL && !level.isClientSide) {
            return WitherSkullBlock.getOrCreateWitherBase().find(level, blockPos) != null;
        }
        return false;
    }

    private static BlockPattern getOrCreateWitherFull() {
        if (witherPatternFull == null) {
            witherPatternFull = BlockPatternBuilder.start().aisle("^^^", "###", "~#~").where('#', blockInWorld -> blockInWorld.getState().is(BlockTags.WITHER_SUMMON_BASE_BLOCKS)).where('^', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.WITHER_SKELETON_SKULL).or(BlockStatePredicate.forBlock(Blocks.WITHER_SKELETON_WALL_SKULL)))).where('~', blockInWorld -> blockInWorld.getState().isAir()).build();
        }
        return witherPatternFull;
    }

    private static BlockPattern getOrCreateWitherBase() {
        if (witherPatternBase == null) {
            witherPatternBase = BlockPatternBuilder.start().aisle("   ", "###", "~#~").where('#', blockInWorld -> blockInWorld.getState().is(BlockTags.WITHER_SUMMON_BASE_BLOCKS)).where('~', blockInWorld -> blockInWorld.getState().isAir()).build();
        }
        return witherPatternBase;
    }
}

