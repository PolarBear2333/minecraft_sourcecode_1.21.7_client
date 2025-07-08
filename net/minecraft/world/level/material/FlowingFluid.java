/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap
 *  it.unimi.dsi.fastutil.shorts.Short2BooleanMap
 *  it.unimi.dsi.fastutil.shorts.Short2BooleanOpenHashMap
 *  it.unimi.dsi.fastutil.shorts.Short2ObjectMap
 *  it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap
 */
package net.minecraft.world.level.material;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class FlowingFluid
extends Fluid {
    public static final BooleanProperty FALLING = BlockStateProperties.FALLING;
    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_FLOWING;
    private static final int CACHE_SIZE = 200;
    private static final ThreadLocal<Object2ByteLinkedOpenHashMap<BlockStatePairKey>> OCCLUSION_CACHE = ThreadLocal.withInitial(() -> {
        Object2ByteLinkedOpenHashMap<BlockStatePairKey> object2ByteLinkedOpenHashMap = new Object2ByteLinkedOpenHashMap<BlockStatePairKey>(200){

            protected void rehash(int n) {
            }
        };
        object2ByteLinkedOpenHashMap.defaultReturnValue((byte)127);
        return object2ByteLinkedOpenHashMap;
    });
    private final Map<FluidState, VoxelShape> shapes = Maps.newIdentityHashMap();

    @Override
    protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
        builder.add(FALLING);
    }

    @Override
    public Vec3 getFlow(BlockGetter blockGetter, BlockPos blockPos, FluidState fluidState) {
        double d = 0.0;
        double d2 = 0.0;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (Object object : Direction.Plane.HORIZONTAL) {
            mutableBlockPos.setWithOffset((Vec3i)blockPos, (Direction)object);
            Object object2 = blockGetter.getFluidState(mutableBlockPos);
            if (!this.affectsFlow((FluidState)object2)) continue;
            float f = ((FluidState)object2).getOwnHeight();
            float f2 = 0.0f;
            if (f == 0.0f) {
                Vec3i vec3i;
                FluidState fluidState2;
                if (!blockGetter.getBlockState(mutableBlockPos).blocksMotion() && this.affectsFlow(fluidState2 = blockGetter.getFluidState((BlockPos)(vec3i = mutableBlockPos.below()))) && (f = fluidState2.getOwnHeight()) > 0.0f) {
                    f2 = fluidState.getOwnHeight() - (f - 0.8888889f);
                }
            } else if (f > 0.0f) {
                f2 = fluidState.getOwnHeight() - f;
            }
            if (f2 == 0.0f) continue;
            d += (double)((float)((Direction)object).getStepX() * f2);
            d2 += (double)((float)((Direction)object).getStepZ() * f2);
        }
        Object object = new Vec3(d, 0.0, d2);
        if (fluidState.getValue(FALLING).booleanValue()) {
            for (Object object2 : Direction.Plane.HORIZONTAL) {
                mutableBlockPos.setWithOffset((Vec3i)blockPos, (Direction)object2);
                if (!this.isSolidFace(blockGetter, mutableBlockPos, (Direction)object2) && !this.isSolidFace(blockGetter, (BlockPos)mutableBlockPos.above(), (Direction)object2)) continue;
                object = ((Vec3)object).normalize().add(0.0, -6.0, 0.0);
                break;
            }
        }
        return ((Vec3)object).normalize();
    }

    private boolean affectsFlow(FluidState fluidState) {
        return fluidState.isEmpty() || fluidState.getType().isSame(this);
    }

    protected boolean isSolidFace(BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        BlockState blockState = blockGetter.getBlockState(blockPos);
        FluidState fluidState = blockGetter.getFluidState(blockPos);
        if (fluidState.getType().isSame(this)) {
            return false;
        }
        if (direction == Direction.UP) {
            return true;
        }
        if (blockState.getBlock() instanceof IceBlock) {
            return false;
        }
        return blockState.isFaceSturdy(blockGetter, blockPos, direction);
    }

    protected void spread(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
        FluidState fluidState2;
        Fluid fluid;
        FluidState fluidState3;
        BlockState blockState2;
        if (fluidState.isEmpty()) {
            return;
        }
        BlockPos blockPos2 = blockPos.below();
        if (this.canMaybePassThrough(serverLevel, blockPos, blockState, Direction.DOWN, blockPos2, blockState2 = serverLevel.getBlockState(blockPos2), fluidState3 = blockState2.getFluidState()) && fluidState3.canBeReplacedWith(serverLevel, blockPos2, fluid = (fluidState2 = this.getNewLiquid(serverLevel, blockPos2, blockState2)).getType(), Direction.DOWN) && FlowingFluid.canHoldSpecificFluid(serverLevel, blockPos2, blockState2, fluid)) {
            this.spreadTo(serverLevel, blockPos2, blockState2, Direction.DOWN, fluidState2);
            if (this.sourceNeighborCount(serverLevel, blockPos) >= 3) {
                this.spreadToSides(serverLevel, blockPos, fluidState, blockState);
            }
            return;
        }
        if (fluidState.isSource() || !this.isWaterHole(serverLevel, blockPos, blockState, blockPos2, blockState2)) {
            this.spreadToSides(serverLevel, blockPos, fluidState, blockState);
        }
    }

    private void spreadToSides(ServerLevel serverLevel, BlockPos blockPos, FluidState fluidState, BlockState blockState) {
        int n = fluidState.getAmount() - this.getDropOff(serverLevel);
        if (fluidState.getValue(FALLING).booleanValue()) {
            n = 7;
        }
        if (n <= 0) {
            return;
        }
        Map<Direction, FluidState> map = this.getSpread(serverLevel, blockPos, blockState);
        for (Map.Entry<Direction, FluidState> entry : map.entrySet()) {
            Direction direction = entry.getKey();
            FluidState fluidState2 = entry.getValue();
            BlockPos blockPos2 = blockPos.relative(direction);
            this.spreadTo(serverLevel, blockPos2, serverLevel.getBlockState(blockPos2), direction, fluidState2);
        }
    }

    protected FluidState getNewLiquid(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState) {
        Object object;
        Object object2;
        int n = 0;
        int n2 = 0;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        Object object3 = Direction.Plane.HORIZONTAL.iterator();
        while (object3.hasNext()) {
            object2 = object3.next();
            object = mutableBlockPos.setWithOffset((Vec3i)blockPos, (Direction)object2);
            BlockState blockState2 = serverLevel.getBlockState((BlockPos)object);
            FluidState fluidState = blockState2.getFluidState();
            if (!fluidState.getType().isSame(this) || !FlowingFluid.canPassThroughWall((Direction)object2, serverLevel, blockPos, blockState, (BlockPos)object, blockState2)) continue;
            if (fluidState.isSource()) {
                ++n2;
            }
            n = Math.max(n, fluidState.getAmount());
        }
        if (n2 >= 2 && this.canConvertToSource(serverLevel)) {
            object3 = serverLevel.getBlockState(mutableBlockPos.setWithOffset((Vec3i)blockPos, Direction.DOWN));
            object2 = ((BlockBehaviour.BlockStateBase)object3).getFluidState();
            if (((BlockBehaviour.BlockStateBase)object3).isSolid() || this.isSourceBlockOfThisType((FluidState)object2)) {
                return this.getSource(false);
            }
        }
        if (!((FluidState)(object = ((BlockBehaviour.BlockStateBase)(object2 = serverLevel.getBlockState((BlockPos)(object3 = mutableBlockPos.setWithOffset((Vec3i)blockPos, Direction.UP))))).getFluidState())).isEmpty() && ((FluidState)object).getType().isSame(this) && FlowingFluid.canPassThroughWall(Direction.UP, serverLevel, blockPos, blockState, (BlockPos)object3, (BlockState)object2)) {
            return this.getFlowing(8, true);
        }
        int n3 = n - this.getDropOff(serverLevel);
        if (n3 <= 0) {
            return Fluids.EMPTY.defaultFluidState();
        }
        return this.getFlowing(n3, false);
    }

    private static boolean canPassThroughWall(Direction direction, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, BlockPos blockPos2, BlockState blockState2) {
        int n;
        BlockStatePairKey blockStatePairKey;
        VoxelShape voxelShape = blockState2.getCollisionShape(blockGetter, blockPos2);
        if (voxelShape == Shapes.block()) {
            return false;
        }
        VoxelShape voxelShape2 = blockState.getCollisionShape(blockGetter, blockPos);
        if (voxelShape2 == Shapes.block()) {
            return false;
        }
        if (voxelShape2 == Shapes.empty() && voxelShape == Shapes.empty()) {
            return true;
        }
        Object2ByteLinkedOpenHashMap<BlockStatePairKey> object2ByteLinkedOpenHashMap = blockState.getBlock().hasDynamicShape() || blockState2.getBlock().hasDynamicShape() ? null : OCCLUSION_CACHE.get();
        if (object2ByteLinkedOpenHashMap != null) {
            blockStatePairKey = new BlockStatePairKey(blockState, blockState2, direction);
            n = object2ByteLinkedOpenHashMap.getAndMoveToFirst((Object)blockStatePairKey);
            if (n != 127) {
                return n != 0;
            }
        } else {
            blockStatePairKey = null;
        }
        int n2 = n = !Shapes.mergedFaceOccludes(voxelShape2, voxelShape, direction) ? 1 : 0;
        if (object2ByteLinkedOpenHashMap != null) {
            if (object2ByteLinkedOpenHashMap.size() == 200) {
                object2ByteLinkedOpenHashMap.removeLastByte();
            }
            object2ByteLinkedOpenHashMap.putAndMoveToFirst((Object)blockStatePairKey, (byte)(n != 0 ? 1 : 0));
        }
        return n != 0;
    }

    public abstract Fluid getFlowing();

    public FluidState getFlowing(int n, boolean bl) {
        return (FluidState)((FluidState)this.getFlowing().defaultFluidState().setValue(LEVEL, n)).setValue(FALLING, bl);
    }

    public abstract Fluid getSource();

    public FluidState getSource(boolean bl) {
        return (FluidState)this.getSource().defaultFluidState().setValue(FALLING, bl);
    }

    protected abstract boolean canConvertToSource(ServerLevel var1);

    protected void spreadTo(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, Direction direction, FluidState fluidState) {
        Block block = blockState.getBlock();
        if (block instanceof LiquidBlockContainer) {
            LiquidBlockContainer liquidBlockContainer = (LiquidBlockContainer)((Object)block);
            liquidBlockContainer.placeLiquid(levelAccessor, blockPos, blockState, fluidState);
        } else {
            if (!blockState.isAir()) {
                this.beforeDestroyingBlock(levelAccessor, blockPos, blockState);
            }
            levelAccessor.setBlock(blockPos, fluidState.createLegacyBlock(), 3);
        }
    }

    protected abstract void beforeDestroyingBlock(LevelAccessor var1, BlockPos var2, BlockState var3);

    protected int getSlopeDistance(LevelReader levelReader, BlockPos blockPos, int n, Direction direction, BlockState blockState, SpreadContext spreadContext) {
        int n2 = 1000;
        for (Direction direction2 : Direction.Plane.HORIZONTAL) {
            int n3;
            if (direction2 == direction) continue;
            BlockPos blockPos2 = blockPos.relative(direction2);
            BlockState blockState2 = spreadContext.getBlockState(blockPos2);
            FluidState fluidState = blockState2.getFluidState();
            if (!this.canPassThrough(levelReader, this.getFlowing(), blockPos, blockState, direction2, blockPos2, blockState2, fluidState)) continue;
            if (spreadContext.isHole(blockPos2)) {
                return n;
            }
            if (n >= this.getSlopeFindDistance(levelReader) || (n3 = this.getSlopeDistance(levelReader, blockPos2, n + 1, direction2.getOpposite(), blockState2, spreadContext)) >= n2) continue;
            n2 = n3;
        }
        return n2;
    }

    boolean isWaterHole(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, BlockPos blockPos2, BlockState blockState2) {
        if (!FlowingFluid.canPassThroughWall(Direction.DOWN, blockGetter, blockPos, blockState, blockPos2, blockState2)) {
            return false;
        }
        if (blockState2.getFluidState().getType().isSame(this)) {
            return true;
        }
        return FlowingFluid.canHoldFluid(blockGetter, blockPos2, blockState2, this.getFlowing());
    }

    private boolean canPassThrough(BlockGetter blockGetter, Fluid fluid, BlockPos blockPos, BlockState blockState, Direction direction, BlockPos blockPos2, BlockState blockState2, FluidState fluidState) {
        return this.canMaybePassThrough(blockGetter, blockPos, blockState, direction, blockPos2, blockState2, fluidState) && FlowingFluid.canHoldSpecificFluid(blockGetter, blockPos2, blockState2, fluid);
    }

    private boolean canMaybePassThrough(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, Direction direction, BlockPos blockPos2, BlockState blockState2, FluidState fluidState) {
        return !this.isSourceBlockOfThisType(fluidState) && FlowingFluid.canHoldAnyFluid(blockState2) && FlowingFluid.canPassThroughWall(direction, blockGetter, blockPos, blockState, blockPos2, blockState2);
    }

    private boolean isSourceBlockOfThisType(FluidState fluidState) {
        return fluidState.getType().isSame(this) && fluidState.isSource();
    }

    protected abstract int getSlopeFindDistance(LevelReader var1);

    private int sourceNeighborCount(LevelReader levelReader, BlockPos blockPos) {
        int n = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos blockPos2 = blockPos.relative(direction);
            FluidState fluidState = levelReader.getFluidState(blockPos2);
            if (!this.isSourceBlockOfThisType(fluidState)) continue;
            ++n;
        }
        return n;
    }

    protected Map<Direction, FluidState> getSpread(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState) {
        int n = 1000;
        EnumMap enumMap = Maps.newEnumMap(Direction.class);
        SpreadContext spreadContext = null;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            int n2;
            FluidState fluidState;
            FluidState fluidState2;
            BlockState blockState2;
            BlockPos blockPos2;
            if (!this.canMaybePassThrough(serverLevel, blockPos, blockState, direction, blockPos2 = blockPos.relative(direction), blockState2 = serverLevel.getBlockState(blockPos2), fluidState2 = blockState2.getFluidState()) || !FlowingFluid.canHoldSpecificFluid(serverLevel, blockPos2, blockState2, (fluidState = this.getNewLiquid(serverLevel, blockPos2, blockState2)).getType())) continue;
            if (spreadContext == null) {
                spreadContext = new SpreadContext(serverLevel, blockPos);
            }
            if ((n2 = spreadContext.isHole(blockPos2) ? 0 : this.getSlopeDistance(serverLevel, blockPos2, 1, direction.getOpposite(), blockState2, spreadContext)) < n) {
                enumMap.clear();
            }
            if (n2 > n) continue;
            if (fluidState2.canBeReplacedWith(serverLevel, blockPos2, fluidState.getType(), direction)) {
                enumMap.put(direction, fluidState);
            }
            n = n2;
        }
        return enumMap;
    }

    private static boolean canHoldAnyFluid(BlockState blockState) {
        Block block = blockState.getBlock();
        if (block instanceof LiquidBlockContainer) {
            return true;
        }
        if (blockState.blocksMotion()) {
            return false;
        }
        return !(block instanceof DoorBlock) && !blockState.is(BlockTags.SIGNS) && !blockState.is(Blocks.LADDER) && !blockState.is(Blocks.SUGAR_CANE) && !blockState.is(Blocks.BUBBLE_COLUMN) && !blockState.is(Blocks.NETHER_PORTAL) && !blockState.is(Blocks.END_PORTAL) && !blockState.is(Blocks.END_GATEWAY) && !blockState.is(Blocks.STRUCTURE_VOID);
    }

    private static boolean canHoldFluid(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, Fluid fluid) {
        return FlowingFluid.canHoldAnyFluid(blockState) && FlowingFluid.canHoldSpecificFluid(blockGetter, blockPos, blockState, fluid);
    }

    private static boolean canHoldSpecificFluid(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, Fluid fluid) {
        Block block = blockState.getBlock();
        if (block instanceof LiquidBlockContainer) {
            LiquidBlockContainer liquidBlockContainer = (LiquidBlockContainer)((Object)block);
            return liquidBlockContainer.canPlaceLiquid(null, blockGetter, blockPos, blockState, fluid);
        }
        return true;
    }

    protected abstract int getDropOff(LevelReader var1);

    protected int getSpreadDelay(Level level, BlockPos blockPos, FluidState fluidState, FluidState fluidState2) {
        return this.getTickDelay(level);
    }

    @Override
    public void tick(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
        if (!fluidState.isSource()) {
            FluidState fluidState2 = this.getNewLiquid(serverLevel, blockPos, serverLevel.getBlockState(blockPos));
            int n = this.getSpreadDelay(serverLevel, blockPos, fluidState, fluidState2);
            if (fluidState2.isEmpty()) {
                fluidState = fluidState2;
                blockState = Blocks.AIR.defaultBlockState();
                serverLevel.setBlock(blockPos, blockState, 3);
            } else if (fluidState2 != fluidState) {
                fluidState = fluidState2;
                blockState = fluidState.createLegacyBlock();
                serverLevel.setBlock(blockPos, blockState, 3);
                serverLevel.scheduleTick(blockPos, fluidState.getType(), n);
            }
        }
        this.spread(serverLevel, blockPos, blockState, fluidState);
    }

    protected static int getLegacyLevel(FluidState fluidState) {
        if (fluidState.isSource()) {
            return 0;
        }
        return 8 - Math.min(fluidState.getAmount(), 8) + (fluidState.getValue(FALLING) != false ? 8 : 0);
    }

    private static boolean hasSameAbove(FluidState fluidState, BlockGetter blockGetter, BlockPos blockPos) {
        return fluidState.getType().isSame(blockGetter.getFluidState(blockPos.above()).getType());
    }

    @Override
    public float getHeight(FluidState fluidState, BlockGetter blockGetter, BlockPos blockPos) {
        if (FlowingFluid.hasSameAbove(fluidState, blockGetter, blockPos)) {
            return 1.0f;
        }
        return fluidState.getOwnHeight();
    }

    @Override
    public float getOwnHeight(FluidState fluidState) {
        return (float)fluidState.getAmount() / 9.0f;
    }

    @Override
    public abstract int getAmount(FluidState var1);

    @Override
    public VoxelShape getShape(FluidState fluidState2, BlockGetter blockGetter, BlockPos blockPos) {
        if (fluidState2.getAmount() == 9 && FlowingFluid.hasSameAbove(fluidState2, blockGetter, blockPos)) {
            return Shapes.block();
        }
        return this.shapes.computeIfAbsent(fluidState2, fluidState -> Shapes.box(0.0, 0.0, 0.0, 1.0, fluidState.getHeight(blockGetter, blockPos), 1.0));
    }

    record BlockStatePairKey(BlockState first, BlockState second, Direction direction) {
        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        @Override
        public boolean equals(Object object) {
            if (!(object instanceof BlockStatePairKey)) return false;
            BlockStatePairKey blockStatePairKey = (BlockStatePairKey)object;
            if (this.first != blockStatePairKey.first) return false;
            if (this.second != blockStatePairKey.second) return false;
            if (this.direction != blockStatePairKey.direction) return false;
            return true;
        }

        @Override
        public int hashCode() {
            int n = System.identityHashCode(this.first);
            n = 31 * n + System.identityHashCode(this.second);
            n = 31 * n + this.direction.hashCode();
            return n;
        }
    }

    protected class SpreadContext {
        private final BlockGetter level;
        private final BlockPos origin;
        private final Short2ObjectMap<BlockState> stateCache = new Short2ObjectOpenHashMap();
        private final Short2BooleanMap holeCache = new Short2BooleanOpenHashMap();

        SpreadContext(BlockGetter blockGetter, BlockPos blockPos) {
            this.level = blockGetter;
            this.origin = blockPos;
        }

        public BlockState getBlockState(BlockPos blockPos) {
            return this.getBlockState(blockPos, this.getCacheKey(blockPos));
        }

        private BlockState getBlockState(BlockPos blockPos, short s2) {
            return (BlockState)this.stateCache.computeIfAbsent(s2, s -> this.level.getBlockState(blockPos));
        }

        public boolean isHole(BlockPos blockPos) {
            return this.holeCache.computeIfAbsent(this.getCacheKey(blockPos), s -> {
                BlockState blockState = this.getBlockState(blockPos, s);
                BlockPos blockPos2 = blockPos.below();
                BlockState blockState2 = this.level.getBlockState(blockPos2);
                return FlowingFluid.this.isWaterHole(this.level, blockPos, blockState, blockPos2, blockState2);
            });
        }

        private short getCacheKey(BlockPos blockPos) {
            int n = blockPos.getX() - this.origin.getX();
            int n2 = blockPos.getZ() - this.origin.getZ();
            return (short)((n + 128 & 0xFF) << 8 | n2 + 128 & 0xFF);
        }
    }
}

