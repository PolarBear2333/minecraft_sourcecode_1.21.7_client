/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Sets
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  it.unimi.dsi.fastutil.ints.IntArrays
 *  it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
 *  it.unimi.dsi.fastutil.objects.ObjectSet
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.chunk;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction8;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.ticks.SavedTick;
import org.slf4j.Logger;

public class UpgradeData {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final UpgradeData EMPTY = new UpgradeData(EmptyBlockGetter.INSTANCE);
    private static final String TAG_INDICES = "Indices";
    private static final Direction8[] DIRECTIONS = Direction8.values();
    private static final Codec<List<SavedTick<Block>>> BLOCK_TICKS_CODEC = SavedTick.codec(BuiltInRegistries.BLOCK.byNameCodec().orElse((Object)Blocks.AIR)).listOf();
    private static final Codec<List<SavedTick<Fluid>>> FLUID_TICKS_CODEC = SavedTick.codec(BuiltInRegistries.FLUID.byNameCodec().orElse((Object)Fluids.EMPTY)).listOf();
    private final EnumSet<Direction8> sides = EnumSet.noneOf(Direction8.class);
    private final List<SavedTick<Block>> neighborBlockTicks = Lists.newArrayList();
    private final List<SavedTick<Fluid>> neighborFluidTicks = Lists.newArrayList();
    private final int[][] index;
    static final Map<Block, BlockFixer> MAP = new IdentityHashMap<Block, BlockFixer>();
    static final Set<BlockFixer> CHUNKY_FIXERS = Sets.newHashSet();

    private UpgradeData(LevelHeightAccessor levelHeightAccessor) {
        this.index = new int[levelHeightAccessor.getSectionsCount()][];
    }

    public UpgradeData(CompoundTag compoundTag2, LevelHeightAccessor levelHeightAccessor) {
        this(levelHeightAccessor);
        compoundTag2.getCompound(TAG_INDICES).ifPresent(compoundTag -> {
            for (int i = 0; i < this.index.length; ++i) {
                this.index[i] = compoundTag.getIntArray(String.valueOf(i)).orElse(null);
            }
        });
        int n = compoundTag2.getIntOr("Sides", 0);
        for (Direction8 direction8 : Direction8.values()) {
            if ((n & 1 << direction8.ordinal()) == 0) continue;
            this.sides.add(direction8);
        }
        compoundTag2.read("neighbor_block_ticks", BLOCK_TICKS_CODEC).ifPresent(this.neighborBlockTicks::addAll);
        compoundTag2.read("neighbor_fluid_ticks", FLUID_TICKS_CODEC).ifPresent(this.neighborFluidTicks::addAll);
    }

    private UpgradeData(UpgradeData upgradeData) {
        this.sides.addAll(upgradeData.sides);
        this.neighborBlockTicks.addAll(upgradeData.neighborBlockTicks);
        this.neighborFluidTicks.addAll(upgradeData.neighborFluidTicks);
        this.index = new int[upgradeData.index.length][];
        for (int i = 0; i < upgradeData.index.length; ++i) {
            int[] nArray = upgradeData.index[i];
            this.index[i] = nArray != null ? IntArrays.copy((int[])nArray) : null;
        }
    }

    public void upgrade(LevelChunk levelChunk) {
        this.upgradeInside(levelChunk);
        for (Direction8 direction8 : DIRECTIONS) {
            UpgradeData.upgradeSides(levelChunk, direction8);
        }
        Level level = levelChunk.getLevel();
        this.neighborBlockTicks.forEach(savedTick -> {
            Block block = savedTick.type() == Blocks.AIR ? level.getBlockState(savedTick.pos()).getBlock() : (Block)savedTick.type();
            level.scheduleTick(savedTick.pos(), block, savedTick.delay(), savedTick.priority());
        });
        this.neighborFluidTicks.forEach(savedTick -> {
            Fluid fluid = savedTick.type() == Fluids.EMPTY ? level.getFluidState(savedTick.pos()).getType() : (Fluid)savedTick.type();
            level.scheduleTick(savedTick.pos(), fluid, savedTick.delay(), savedTick.priority());
        });
        CHUNKY_FIXERS.forEach(blockFixer -> blockFixer.processChunk(level));
    }

    private static void upgradeSides(LevelChunk levelChunk, Direction8 direction8) {
        Level level = levelChunk.getLevel();
        if (!levelChunk.getUpgradeData().sides.remove((Object)direction8)) {
            return;
        }
        Set<Direction> set = direction8.getDirections();
        boolean bl = false;
        int n = 15;
        boolean bl2 = set.contains(Direction.EAST);
        boolean bl3 = set.contains(Direction.WEST);
        boolean bl4 = set.contains(Direction.SOUTH);
        boolean bl5 = set.contains(Direction.NORTH);
        boolean bl6 = set.size() == 1;
        ChunkPos chunkPos = levelChunk.getPos();
        int n2 = chunkPos.getMinBlockX() + (bl6 && (bl5 || bl4) ? 1 : (bl3 ? 0 : 15));
        int n3 = chunkPos.getMinBlockX() + (bl6 && (bl5 || bl4) ? 14 : (bl3 ? 0 : 15));
        int n4 = chunkPos.getMinBlockZ() + (bl6 && (bl2 || bl3) ? 1 : (bl5 ? 0 : 15));
        int n5 = chunkPos.getMinBlockZ() + (bl6 && (bl2 || bl3) ? 14 : (bl5 ? 0 : 15));
        Direction[] directionArray = Direction.values();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (BlockPos blockPos : BlockPos.betweenClosed(n2, level.getMinY(), n4, n3, level.getMaxY(), n5)) {
            BlockState blockState;
            BlockState blockState2 = blockState = level.getBlockState(blockPos);
            for (Direction direction : directionArray) {
                mutableBlockPos.setWithOffset((Vec3i)blockPos, direction);
                blockState2 = UpgradeData.updateState(blockState2, direction, level, blockPos, mutableBlockPos);
            }
            Block.updateOrDestroy(blockState, blockState2, level, blockPos, 18);
        }
    }

    private static BlockState updateState(BlockState blockState, Direction direction, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        return MAP.getOrDefault(blockState.getBlock(), BlockFixers.DEFAULT).updateShape(blockState, direction, levelAccessor.getBlockState(blockPos2), levelAccessor, blockPos, blockPos2);
    }

    private void upgradeInside(LevelChunk levelChunk) {
        int n;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();
        ChunkPos chunkPos = levelChunk.getPos();
        Level level = levelChunk.getLevel();
        for (n = 0; n < this.index.length; ++n) {
            LevelChunkSection levelChunkSection = levelChunk.getSection(n);
            int[] nArray = this.index[n];
            this.index[n] = null;
            if (nArray == null || nArray.length <= 0) continue;
            Direction[] directionArray = Direction.values();
            PalettedContainer<BlockState> palettedContainer = levelChunkSection.getStates();
            int n2 = levelChunk.getSectionYFromSectionIndex(n);
            int n3 = SectionPos.sectionToBlockCoord(n2);
            for (int n4 : nArray) {
                BlockState blockState;
                int n5 = n4 & 0xF;
                int n6 = n4 >> 8 & 0xF;
                int n7 = n4 >> 4 & 0xF;
                mutableBlockPos.set(chunkPos.getMinBlockX() + n5, n3 + n6, chunkPos.getMinBlockZ() + n7);
                BlockState blockState2 = blockState = palettedContainer.get(n4);
                for (Direction direction : directionArray) {
                    mutableBlockPos2.setWithOffset((Vec3i)mutableBlockPos, direction);
                    if (SectionPos.blockToSectionCoord(mutableBlockPos.getX()) != chunkPos.x || SectionPos.blockToSectionCoord(mutableBlockPos.getZ()) != chunkPos.z) continue;
                    blockState2 = UpgradeData.updateState(blockState2, direction, level, mutableBlockPos, mutableBlockPos2);
                }
                Block.updateOrDestroy(blockState, blockState2, level, mutableBlockPos, 18);
            }
        }
        for (n = 0; n < this.index.length; ++n) {
            if (this.index[n] != null) {
                LOGGER.warn("Discarding update data for section {} for chunk ({} {})", new Object[]{level.getSectionYFromSectionIndex(n), chunkPos.x, chunkPos.z});
            }
            this.index[n] = null;
        }
    }

    public boolean isEmpty() {
        for (int[] nArray : this.index) {
            if (nArray == null) continue;
            return false;
        }
        return this.sides.isEmpty();
    }

    public CompoundTag write() {
        int n;
        CompoundTag compoundTag = new CompoundTag();
        CompoundTag compoundTag2 = new CompoundTag();
        for (n = 0; n < this.index.length; ++n) {
            String string = String.valueOf(n);
            if (this.index[n] == null || this.index[n].length == 0) continue;
            compoundTag2.putIntArray(string, this.index[n]);
        }
        if (!compoundTag2.isEmpty()) {
            compoundTag.put(TAG_INDICES, compoundTag2);
        }
        n = 0;
        for (Direction8 direction8 : this.sides) {
            n |= 1 << direction8.ordinal();
        }
        compoundTag.putByte("Sides", (byte)n);
        if (!this.neighborBlockTicks.isEmpty()) {
            compoundTag.store("neighbor_block_ticks", BLOCK_TICKS_CODEC, this.neighborBlockTicks);
        }
        if (!this.neighborFluidTicks.isEmpty()) {
            compoundTag.store("neighbor_fluid_ticks", FLUID_TICKS_CODEC, this.neighborFluidTicks);
        }
        return compoundTag;
    }

    public UpgradeData copy() {
        if (this == EMPTY) {
            return EMPTY;
        }
        return new UpgradeData(this);
    }

    static enum BlockFixers implements BlockFixer
    {
        BLACKLIST(new Block[]{Blocks.OBSERVER, Blocks.NETHER_PORTAL, Blocks.WHITE_CONCRETE_POWDER, Blocks.ORANGE_CONCRETE_POWDER, Blocks.MAGENTA_CONCRETE_POWDER, Blocks.LIGHT_BLUE_CONCRETE_POWDER, Blocks.YELLOW_CONCRETE_POWDER, Blocks.LIME_CONCRETE_POWDER, Blocks.PINK_CONCRETE_POWDER, Blocks.GRAY_CONCRETE_POWDER, Blocks.LIGHT_GRAY_CONCRETE_POWDER, Blocks.CYAN_CONCRETE_POWDER, Blocks.PURPLE_CONCRETE_POWDER, Blocks.BLUE_CONCRETE_POWDER, Blocks.BROWN_CONCRETE_POWDER, Blocks.GREEN_CONCRETE_POWDER, Blocks.RED_CONCRETE_POWDER, Blocks.BLACK_CONCRETE_POWDER, Blocks.ANVIL, Blocks.CHIPPED_ANVIL, Blocks.DAMAGED_ANVIL, Blocks.DRAGON_EGG, Blocks.GRAVEL, Blocks.SAND, Blocks.RED_SAND, Blocks.OAK_SIGN, Blocks.SPRUCE_SIGN, Blocks.BIRCH_SIGN, Blocks.ACACIA_SIGN, Blocks.CHERRY_SIGN, Blocks.JUNGLE_SIGN, Blocks.DARK_OAK_SIGN, Blocks.PALE_OAK_SIGN, Blocks.OAK_WALL_SIGN, Blocks.SPRUCE_WALL_SIGN, Blocks.BIRCH_WALL_SIGN, Blocks.ACACIA_WALL_SIGN, Blocks.JUNGLE_WALL_SIGN, Blocks.DARK_OAK_WALL_SIGN, Blocks.PALE_OAK_WALL_SIGN, Blocks.OAK_HANGING_SIGN, Blocks.SPRUCE_HANGING_SIGN, Blocks.BIRCH_HANGING_SIGN, Blocks.ACACIA_HANGING_SIGN, Blocks.JUNGLE_HANGING_SIGN, Blocks.DARK_OAK_HANGING_SIGN, Blocks.PALE_OAK_HANGING_SIGN, Blocks.OAK_WALL_HANGING_SIGN, Blocks.SPRUCE_WALL_HANGING_SIGN, Blocks.BIRCH_WALL_HANGING_SIGN, Blocks.ACACIA_WALL_HANGING_SIGN, Blocks.JUNGLE_WALL_HANGING_SIGN, Blocks.DARK_OAK_WALL_HANGING_SIGN, Blocks.PALE_OAK_WALL_HANGING_SIGN}){

            @Override
            public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
                return blockState;
            }
        }
        ,
        DEFAULT(new Block[0]){

            @Override
            public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
                return blockState.updateShape(levelAccessor, levelAccessor, blockPos, direction, blockPos2, levelAccessor.getBlockState(blockPos2), levelAccessor.getRandom());
            }
        }
        ,
        CHEST(new Block[]{Blocks.CHEST, Blocks.TRAPPED_CHEST}){

            @Override
            public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
                if (blockState2.is(blockState.getBlock()) && direction.getAxis().isHorizontal() && blockState.getValue(ChestBlock.TYPE) == ChestType.SINGLE && blockState2.getValue(ChestBlock.TYPE) == ChestType.SINGLE) {
                    Direction direction2 = blockState.getValue(ChestBlock.FACING);
                    if (direction.getAxis() != direction2.getAxis() && direction2 == blockState2.getValue(ChestBlock.FACING)) {
                        ChestType chestType = direction == direction2.getClockWise() ? ChestType.LEFT : ChestType.RIGHT;
                        levelAccessor.setBlock(blockPos2, (BlockState)blockState2.setValue(ChestBlock.TYPE, chestType.getOpposite()), 18);
                        if (direction2 == Direction.NORTH || direction2 == Direction.EAST) {
                            BlockEntity blockEntity = levelAccessor.getBlockEntity(blockPos);
                            BlockEntity blockEntity2 = levelAccessor.getBlockEntity(blockPos2);
                            if (blockEntity instanceof ChestBlockEntity && blockEntity2 instanceof ChestBlockEntity) {
                                ChestBlockEntity.swapContents((ChestBlockEntity)blockEntity, (ChestBlockEntity)blockEntity2);
                            }
                        }
                        return (BlockState)blockState.setValue(ChestBlock.TYPE, chestType);
                    }
                }
                return blockState;
            }
        }
        ,
        LEAVES(true, new Block[]{Blocks.ACACIA_LEAVES, Blocks.CHERRY_LEAVES, Blocks.BIRCH_LEAVES, Blocks.PALE_OAK_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES}){
            private final ThreadLocal<List<ObjectSet<BlockPos>>> queue = ThreadLocal.withInitial(() -> Lists.newArrayListWithCapacity((int)7));

            @Override
            public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
                BlockState blockState3 = blockState.updateShape(levelAccessor, levelAccessor, blockPos, direction, blockPos2, levelAccessor.getBlockState(blockPos2), levelAccessor.getRandom());
                if (blockState != blockState3) {
                    int n = blockState3.getValue(BlockStateProperties.DISTANCE);
                    List<ObjectSet<BlockPos>> list = this.queue.get();
                    if (list.isEmpty()) {
                        for (int i = 0; i < 7; ++i) {
                            list.add((ObjectSet<BlockPos>)new ObjectOpenHashSet());
                        }
                    }
                    list.get(n).add((Object)blockPos.immutable());
                }
                return blockState;
            }

            @Override
            public void processChunk(LevelAccessor levelAccessor) {
                BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
                List<ObjectSet<BlockPos>> list = this.queue.get();
                for (int i = 2; i < list.size(); ++i) {
                    int n = i - 1;
                    ObjectSet<BlockPos> objectSet = list.get(n);
                    ObjectSet<BlockPos> objectSet2 = list.get(i);
                    for (BlockPos blockPos : objectSet) {
                        BlockState blockState = levelAccessor.getBlockState(blockPos);
                        if (blockState.getValue(BlockStateProperties.DISTANCE) < n) continue;
                        levelAccessor.setBlock(blockPos, (BlockState)blockState.setValue(BlockStateProperties.DISTANCE, n), 18);
                        if (i == 7) continue;
                        for (Direction direction : DIRECTIONS) {
                            mutableBlockPos.setWithOffset((Vec3i)blockPos, direction);
                            BlockState blockState2 = levelAccessor.getBlockState(mutableBlockPos);
                            if (!blockState2.hasProperty(BlockStateProperties.DISTANCE) || blockState.getValue(BlockStateProperties.DISTANCE) <= i) continue;
                            objectSet2.add((Object)mutableBlockPos.immutable());
                        }
                    }
                }
                list.clear();
            }
        }
        ,
        STEM_BLOCK(new Block[]{Blocks.MELON_STEM, Blocks.PUMPKIN_STEM}){

            @Override
            public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
                if (blockState.getValue(StemBlock.AGE) == 7) {
                    Block block;
                    Block block2 = block = blockState.is(Blocks.PUMPKIN_STEM) ? Blocks.PUMPKIN : Blocks.MELON;
                    if (blockState2.is(block)) {
                        return (BlockState)(blockState.is(Blocks.PUMPKIN_STEM) ? Blocks.ATTACHED_PUMPKIN_STEM : Blocks.ATTACHED_MELON_STEM).defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, direction);
                    }
                }
                return blockState;
            }
        };

        public static final Direction[] DIRECTIONS;

        BlockFixers(Block ... blockArray) {
            this(false, blockArray);
        }

        BlockFixers(boolean bl, Block ... blockArray) {
            for (Block block : blockArray) {
                MAP.put(block, this);
            }
            if (bl) {
                CHUNKY_FIXERS.add(this);
            }
        }

        static {
            DIRECTIONS = Direction.values();
        }
    }

    public static interface BlockFixer {
        public BlockState updateShape(BlockState var1, Direction var2, BlockState var3, LevelAccessor var4, BlockPos var5, BlockPos var6);

        default public void processChunk(LevelAccessor levelAccessor) {
        }
    }
}

