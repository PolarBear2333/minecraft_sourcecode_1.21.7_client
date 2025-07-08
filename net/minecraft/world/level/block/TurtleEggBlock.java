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
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TurtleEggBlock
extends Block {
    public static final MapCodec<TurtleEggBlock> CODEC = TurtleEggBlock.simpleCodec(TurtleEggBlock::new);
    public static final IntegerProperty HATCH = BlockStateProperties.HATCH;
    public static final IntegerProperty EGGS = BlockStateProperties.EGGS;
    public static final int MAX_HATCH_LEVEL = 2;
    public static final int MIN_EGGS = 1;
    public static final int MAX_EGGS = 4;
    private static final VoxelShape SHAPE_SINGLE = Block.box(3.0, 0.0, 3.0, 12.0, 7.0, 12.0);
    private static final VoxelShape SHAPE_MULTIPLE = Block.column(14.0, 0.0, 7.0);

    public MapCodec<TurtleEggBlock> codec() {
        return CODEC;
    }

    public TurtleEggBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(HATCH, 0)).setValue(EGGS, 1));
    }

    @Override
    public void stepOn(Level level, BlockPos blockPos, BlockState blockState, Entity entity) {
        if (!entity.isSteppingCarefully()) {
            this.destroyEgg(level, blockState, blockPos, entity, 100);
        }
        super.stepOn(level, blockPos, blockState, entity);
    }

    @Override
    public void fallOn(Level level, BlockState blockState, BlockPos blockPos, Entity entity, double d) {
        if (!(entity instanceof Zombie)) {
            this.destroyEgg(level, blockState, blockPos, entity, 3);
        }
        super.fallOn(level, blockState, blockPos, entity, d);
    }

    private void destroyEgg(Level level, BlockState blockState, BlockPos blockPos, Entity entity, int n) {
        ServerLevel serverLevel;
        if (blockState.is(Blocks.TURTLE_EGG) && level instanceof ServerLevel && this.canDestroyEgg(serverLevel = (ServerLevel)level, entity) && level.random.nextInt(n) == 0) {
            this.decreaseEggs(serverLevel, blockPos, blockState);
        }
    }

    private void decreaseEggs(Level level, BlockPos blockPos, BlockState blockState) {
        level.playSound(null, blockPos, SoundEvents.TURTLE_EGG_BREAK, SoundSource.BLOCKS, 0.7f, 0.9f + level.random.nextFloat() * 0.2f);
        int n = blockState.getValue(EGGS);
        if (n <= 1) {
            level.destroyBlock(blockPos, false);
        } else {
            level.setBlock(blockPos, (BlockState)blockState.setValue(EGGS, n - 1), 2);
            level.gameEvent(GameEvent.BLOCK_DESTROY, blockPos, GameEvent.Context.of(blockState));
            level.levelEvent(2001, blockPos, Block.getId(blockState));
        }
    }

    @Override
    protected void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (this.shouldUpdateHatchLevel(serverLevel) && TurtleEggBlock.onSand(serverLevel, blockPos)) {
            int n = blockState.getValue(HATCH);
            if (n < 2) {
                serverLevel.playSound(null, blockPos, SoundEvents.TURTLE_EGG_CRACK, SoundSource.BLOCKS, 0.7f, 0.9f + randomSource.nextFloat() * 0.2f);
                serverLevel.setBlock(blockPos, (BlockState)blockState.setValue(HATCH, n + 1), 2);
                serverLevel.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(blockState));
            } else {
                serverLevel.playSound(null, blockPos, SoundEvents.TURTLE_EGG_HATCH, SoundSource.BLOCKS, 0.7f, 0.9f + randomSource.nextFloat() * 0.2f);
                serverLevel.removeBlock(blockPos, false);
                serverLevel.gameEvent(GameEvent.BLOCK_DESTROY, blockPos, GameEvent.Context.of(blockState));
                for (int i = 0; i < blockState.getValue(EGGS); ++i) {
                    serverLevel.levelEvent(2001, blockPos, Block.getId(blockState));
                    Turtle turtle = EntityType.TURTLE.create(serverLevel, EntitySpawnReason.BREEDING);
                    if (turtle == null) continue;
                    turtle.setAge(-24000);
                    turtle.setHomePos(blockPos);
                    turtle.snapTo((double)blockPos.getX() + 0.3 + (double)i * 0.2, blockPos.getY(), (double)blockPos.getZ() + 0.3, 0.0f, 0.0f);
                    serverLevel.addFreshEntity(turtle);
                }
            }
        }
    }

    public static boolean onSand(BlockGetter blockGetter, BlockPos blockPos) {
        return TurtleEggBlock.isSand(blockGetter, blockPos.below());
    }

    public static boolean isSand(BlockGetter blockGetter, BlockPos blockPos) {
        return blockGetter.getBlockState(blockPos).is(BlockTags.SAND);
    }

    @Override
    protected void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (TurtleEggBlock.onSand(level, blockPos) && !level.isClientSide) {
            level.levelEvent(2012, blockPos, 15);
        }
    }

    private boolean shouldUpdateHatchLevel(Level level) {
        float f = level.getTimeOfDay(1.0f);
        if ((double)f < 0.69 && (double)f > 0.65) {
            return true;
        }
        return level.random.nextInt(500) == 0;
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity, ItemStack itemStack) {
        super.playerDestroy(level, player, blockPos, blockState, blockEntity, itemStack);
        this.decreaseEggs(level, blockPos, blockState);
    }

    @Override
    protected boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
        if (!blockPlaceContext.isSecondaryUseActive() && blockPlaceContext.getItemInHand().is(this.asItem()) && blockState.getValue(EGGS) < 4) {
            return true;
        }
        return super.canBeReplaced(blockState, blockPlaceContext);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockState blockState = blockPlaceContext.getLevel().getBlockState(blockPlaceContext.getClickedPos());
        if (blockState.is(this)) {
            return (BlockState)blockState.setValue(EGGS, Math.min(4, blockState.getValue(EGGS) + 1));
        }
        return super.getStateForPlacement(blockPlaceContext);
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return blockState.getValue(EGGS) == 1 ? SHAPE_SINGLE : SHAPE_MULTIPLE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HATCH, EGGS);
    }

    private boolean canDestroyEgg(ServerLevel serverLevel, Entity entity) {
        if (entity instanceof Turtle || entity instanceof Bat) {
            return false;
        }
        if (entity instanceof LivingEntity) {
            return entity instanceof Player || serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
        }
        return false;
    }
}

