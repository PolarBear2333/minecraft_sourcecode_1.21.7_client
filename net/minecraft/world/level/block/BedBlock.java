/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.ArrayUtils
 */
package net.minecraft.world.level.block;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.math.OctahedralGroup;
import com.mojang.math.Quadrant;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.ArrayUtils;

public class BedBlock
extends HorizontalDirectionalBlock
implements EntityBlock {
    public static final MapCodec<BedBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)DyeColor.CODEC.fieldOf("color").forGetter(BedBlock::getColor), BedBlock.propertiesCodec()).apply((Applicative)instance, BedBlock::new));
    public static final EnumProperty<BedPart> PART = BlockStateProperties.BED_PART;
    public static final BooleanProperty OCCUPIED = BlockStateProperties.OCCUPIED;
    private static final Map<Direction, VoxelShape> SHAPES = Util.make(() -> {
        VoxelShape voxelShape = Block.box(0.0, 0.0, 0.0, 3.0, 3.0, 3.0);
        VoxelShape voxelShape2 = Shapes.rotate(voxelShape, OctahedralGroup.fromXYAngles(Quadrant.R0, Quadrant.R90));
        return Shapes.rotateHorizontal(Shapes.or(Block.column(16.0, 3.0, 9.0), voxelShape, voxelShape2));
    });
    private final DyeColor color;

    public MapCodec<BedBlock> codec() {
        return CODEC;
    }

    public BedBlock(DyeColor dyeColor, BlockBehaviour.Properties properties) {
        super(properties);
        this.color = dyeColor;
        this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(PART, BedPart.FOOT)).setValue(OCCUPIED, false));
    }

    @Nullable
    public static Direction getBedOrientation(BlockGetter blockGetter, BlockPos blockPos) {
        BlockState blockState = blockGetter.getBlockState(blockPos);
        return blockState.getBlock() instanceof BedBlock ? (Direction)blockState.getValue(FACING) : null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS_SERVER;
        }
        if (blockState.getValue(PART) != BedPart.HEAD && !(blockState = level.getBlockState(blockPos = blockPos.relative((Direction)blockState.getValue(FACING)))).is(this)) {
            return InteractionResult.CONSUME;
        }
        if (!BedBlock.canSetSpawn(level)) {
            level.removeBlock(blockPos, false);
            BlockPos blockPos2 = blockPos.relative(((Direction)blockState.getValue(FACING)).getOpposite());
            if (level.getBlockState(blockPos2).is(this)) {
                level.removeBlock(blockPos2, false);
            }
            Vec3 vec3 = blockPos.getCenter();
            level.explode(null, level.damageSources().badRespawnPointExplosion(vec3), null, vec3, 5.0f, true, Level.ExplosionInteraction.BLOCK);
            return InteractionResult.SUCCESS_SERVER;
        }
        if (blockState.getValue(OCCUPIED).booleanValue()) {
            if (!this.kickVillagerOutOfBed(level, blockPos)) {
                player.displayClientMessage(Component.translatable("block.minecraft.bed.occupied"), true);
            }
            return InteractionResult.SUCCESS_SERVER;
        }
        player.startSleepInBed(blockPos).ifLeft(bedSleepingProblem -> {
            if (bedSleepingProblem.getMessage() != null) {
                player.displayClientMessage(bedSleepingProblem.getMessage(), true);
            }
        });
        return InteractionResult.SUCCESS_SERVER;
    }

    public static boolean canSetSpawn(Level level) {
        return level.dimensionType().bedWorks();
    }

    private boolean kickVillagerOutOfBed(Level level, BlockPos blockPos) {
        List<Villager> list = level.getEntitiesOfClass(Villager.class, new AABB(blockPos), LivingEntity::isSleeping);
        if (list.isEmpty()) {
            return false;
        }
        list.get(0).stopSleeping();
        return true;
    }

    @Override
    public void fallOn(Level level, BlockState blockState, BlockPos blockPos, Entity entity, double d) {
        super.fallOn(level, blockState, blockPos, entity, d * 0.5);
    }

    @Override
    public void updateEntityMovementAfterFallOn(BlockGetter blockGetter, Entity entity) {
        if (entity.isSuppressingBounce()) {
            super.updateEntityMovementAfterFallOn(blockGetter, entity);
        } else {
            this.bounceUp(entity);
        }
    }

    private void bounceUp(Entity entity) {
        Vec3 vec3 = entity.getDeltaMovement();
        if (vec3.y < 0.0) {
            double d = entity instanceof LivingEntity ? 1.0 : 0.8;
            entity.setDeltaMovement(vec3.x, -vec3.y * (double)0.66f * d, vec3.z);
        }
    }

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        if (direction == BedBlock.getNeighbourDirection(blockState.getValue(PART), (Direction)blockState.getValue(FACING))) {
            if (blockState2.is(this) && blockState2.getValue(PART) != blockState.getValue(PART)) {
                return (BlockState)blockState.setValue(OCCUPIED, blockState2.getValue(OCCUPIED));
            }
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
    }

    private static Direction getNeighbourDirection(BedPart bedPart, Direction direction) {
        return bedPart == BedPart.FOOT ? direction : direction.getOpposite();
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
        BlockPos blockPos2;
        BlockState blockState2;
        BedPart bedPart;
        if (!level.isClientSide && player.preventsBlockDrops() && (bedPart = blockState.getValue(PART)) == BedPart.FOOT && (blockState2 = level.getBlockState(blockPos2 = blockPos.relative(BedBlock.getNeighbourDirection(bedPart, (Direction)blockState.getValue(FACING))))).is(this) && blockState2.getValue(PART) == BedPart.HEAD) {
            level.setBlock(blockPos2, Blocks.AIR.defaultBlockState(), 35);
            level.levelEvent(player, 2001, blockPos2, Block.getId(blockState2));
        }
        return super.playerWillDestroy(level, blockPos, blockState, player);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        Direction direction = blockPlaceContext.getHorizontalDirection();
        BlockPos blockPos = blockPlaceContext.getClickedPos();
        BlockPos blockPos2 = blockPos.relative(direction);
        Level level = blockPlaceContext.getLevel();
        if (level.getBlockState(blockPos2).canBeReplaced(blockPlaceContext) && level.getWorldBorder().isWithinBounds(blockPos2)) {
            return (BlockState)this.defaultBlockState().setValue(FACING, direction);
        }
        return null;
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPES.get(BedBlock.getConnectedDirection(blockState).getOpposite());
    }

    public static Direction getConnectedDirection(BlockState blockState) {
        Direction direction = (Direction)blockState.getValue(FACING);
        return blockState.getValue(PART) == BedPart.HEAD ? direction.getOpposite() : direction;
    }

    public static DoubleBlockCombiner.BlockType getBlockType(BlockState blockState) {
        BedPart bedPart = blockState.getValue(PART);
        if (bedPart == BedPart.HEAD) {
            return DoubleBlockCombiner.BlockType.FIRST;
        }
        return DoubleBlockCombiner.BlockType.SECOND;
    }

    private static boolean isBunkBed(BlockGetter blockGetter, BlockPos blockPos) {
        return blockGetter.getBlockState(blockPos.below()).getBlock() instanceof BedBlock;
    }

    public static Optional<Vec3> findStandUpPosition(EntityType<?> entityType, CollisionGetter collisionGetter, BlockPos blockPos, Direction direction, float f) {
        Direction direction2;
        Direction direction3 = direction.getClockWise();
        Direction direction4 = direction2 = direction3.isFacingAngle(f) ? direction3.getOpposite() : direction3;
        if (BedBlock.isBunkBed(collisionGetter, blockPos)) {
            return BedBlock.findBunkBedStandUpPosition(entityType, collisionGetter, blockPos, direction, direction2);
        }
        int[][] nArray = BedBlock.bedStandUpOffsets(direction, direction2);
        Optional<Vec3> optional = BedBlock.findStandUpPositionAtOffset(entityType, collisionGetter, blockPos, nArray, true);
        if (optional.isPresent()) {
            return optional;
        }
        return BedBlock.findStandUpPositionAtOffset(entityType, collisionGetter, blockPos, nArray, false);
    }

    private static Optional<Vec3> findBunkBedStandUpPosition(EntityType<?> entityType, CollisionGetter collisionGetter, BlockPos blockPos, Direction direction, Direction direction2) {
        int[][] nArray = BedBlock.bedSurroundStandUpOffsets(direction, direction2);
        Optional<Vec3> optional = BedBlock.findStandUpPositionAtOffset(entityType, collisionGetter, blockPos, nArray, true);
        if (optional.isPresent()) {
            return optional;
        }
        BlockPos blockPos2 = blockPos.below();
        Optional<Vec3> optional2 = BedBlock.findStandUpPositionAtOffset(entityType, collisionGetter, blockPos2, nArray, true);
        if (optional2.isPresent()) {
            return optional2;
        }
        int[][] nArray2 = BedBlock.bedAboveStandUpOffsets(direction);
        Optional<Vec3> optional3 = BedBlock.findStandUpPositionAtOffset(entityType, collisionGetter, blockPos, nArray2, true);
        if (optional3.isPresent()) {
            return optional3;
        }
        Optional<Vec3> optional4 = BedBlock.findStandUpPositionAtOffset(entityType, collisionGetter, blockPos, nArray, false);
        if (optional4.isPresent()) {
            return optional4;
        }
        Optional<Vec3> optional5 = BedBlock.findStandUpPositionAtOffset(entityType, collisionGetter, blockPos2, nArray, false);
        if (optional5.isPresent()) {
            return optional5;
        }
        return BedBlock.findStandUpPositionAtOffset(entityType, collisionGetter, blockPos, nArray2, false);
    }

    private static Optional<Vec3> findStandUpPositionAtOffset(EntityType<?> entityType, CollisionGetter collisionGetter, BlockPos blockPos, int[][] nArray, boolean bl) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int[] nArray2 : nArray) {
            mutableBlockPos.set(blockPos.getX() + nArray2[0], blockPos.getY(), blockPos.getZ() + nArray2[1]);
            Vec3 vec3 = DismountHelper.findSafeDismountLocation(entityType, collisionGetter, mutableBlockPos, bl);
            if (vec3 == null) continue;
            return Optional.of(vec3);
        }
        return Optional.empty();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART, OCCUPIED);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new BedBlockEntity(blockPos, blockState, this.color);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
        super.setPlacedBy(level, blockPos, blockState, livingEntity, itemStack);
        if (!level.isClientSide) {
            BlockPos blockPos2 = blockPos.relative((Direction)blockState.getValue(FACING));
            level.setBlock(blockPos2, (BlockState)blockState.setValue(PART, BedPart.HEAD), 3);
            level.updateNeighborsAt(blockPos, Blocks.AIR);
            blockState.updateNeighbourShapes(level, blockPos, 3);
        }
    }

    public DyeColor getColor() {
        return this.color;
    }

    @Override
    protected long getSeed(BlockState blockState, BlockPos blockPos) {
        BlockPos blockPos2 = blockPos.relative((Direction)blockState.getValue(FACING), blockState.getValue(PART) == BedPart.HEAD ? 0 : 1);
        return Mth.getSeed(blockPos2.getX(), blockPos.getY(), blockPos2.getZ());
    }

    @Override
    protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
        return false;
    }

    private static int[][] bedStandUpOffsets(Direction direction, Direction direction2) {
        return (int[][])ArrayUtils.addAll((Object[])BedBlock.bedSurroundStandUpOffsets(direction, direction2), (Object[])BedBlock.bedAboveStandUpOffsets(direction));
    }

    private static int[][] bedSurroundStandUpOffsets(Direction direction, Direction direction2) {
        return new int[][]{{direction2.getStepX(), direction2.getStepZ()}, {direction2.getStepX() - direction.getStepX(), direction2.getStepZ() - direction.getStepZ()}, {direction2.getStepX() - direction.getStepX() * 2, direction2.getStepZ() - direction.getStepZ() * 2}, {-direction.getStepX() * 2, -direction.getStepZ() * 2}, {-direction2.getStepX() - direction.getStepX() * 2, -direction2.getStepZ() - direction.getStepZ() * 2}, {-direction2.getStepX() - direction.getStepX(), -direction2.getStepZ() - direction.getStepZ()}, {-direction2.getStepX(), -direction2.getStepZ()}, {-direction2.getStepX() + direction.getStepX(), -direction2.getStepZ() + direction.getStepZ()}, {direction.getStepX(), direction.getStepZ()}, {direction2.getStepX() + direction.getStepX(), direction2.getStepZ() + direction.getStepZ()}};
    }

    private static int[][] bedAboveStandUpOffsets(Direction direction) {
        return new int[][]{{0, 0}, {-direction.getStepX(), -direction.getStepZ()}};
    }
}

