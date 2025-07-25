/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.level;

import com.mojang.logging.LogUtils;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StaticCache2D;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkStep;
import net.minecraft.world.level.chunk.status.ChunkType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraft.world.ticks.WorldGenTickAccess;
import org.slf4j.Logger;

public class WorldGenRegion
implements WorldGenLevel {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final StaticCache2D<GenerationChunkHolder> cache;
    private final ChunkAccess center;
    private final ServerLevel level;
    private final long seed;
    private final LevelData levelData;
    private final RandomSource random;
    private final DimensionType dimensionType;
    private final WorldGenTickAccess<Block> blockTicks = new WorldGenTickAccess(blockPos -> this.getChunk((BlockPos)blockPos).getBlockTicks());
    private final WorldGenTickAccess<Fluid> fluidTicks = new WorldGenTickAccess(blockPos -> this.getChunk((BlockPos)blockPos).getFluidTicks());
    private final BiomeManager biomeManager;
    private final ChunkStep generatingStep;
    @Nullable
    private Supplier<String> currentlyGenerating;
    private final AtomicLong subTickCount = new AtomicLong();
    private static final ResourceLocation WORLDGEN_REGION_RANDOM = ResourceLocation.withDefaultNamespace("worldgen_region_random");

    public WorldGenRegion(ServerLevel serverLevel, StaticCache2D<GenerationChunkHolder> staticCache2D, ChunkStep chunkStep, ChunkAccess chunkAccess) {
        this.generatingStep = chunkStep;
        this.cache = staticCache2D;
        this.center = chunkAccess;
        this.level = serverLevel;
        this.seed = serverLevel.getSeed();
        this.levelData = serverLevel.getLevelData();
        this.random = serverLevel.getChunkSource().randomState().getOrCreateRandomFactory(WORLDGEN_REGION_RANDOM).at(this.center.getPos().getWorldPosition());
        this.dimensionType = serverLevel.dimensionType();
        this.biomeManager = new BiomeManager(this, BiomeManager.obfuscateSeed(this.seed));
    }

    public boolean isOldChunkAround(ChunkPos chunkPos, int n) {
        return this.level.getChunkSource().chunkMap.isOldChunkAround(chunkPos, n);
    }

    public ChunkPos getCenter() {
        return this.center.getPos();
    }

    @Override
    public void setCurrentlyGenerating(@Nullable Supplier<String> supplier) {
        this.currentlyGenerating = supplier;
    }

    @Override
    public ChunkAccess getChunk(int n, int n2) {
        return this.getChunk(n, n2, ChunkStatus.EMPTY);
    }

    @Override
    @Nullable
    public ChunkAccess getChunk(int n, int n2, ChunkStatus chunkStatus, boolean bl) {
        Object object;
        GenerationChunkHolder generationChunkHolder;
        ChunkStatus chunkStatus2;
        int n3 = this.center.getPos().getChessboardDistance(n, n2);
        ChunkStatus chunkStatus3 = chunkStatus2 = n3 >= this.generatingStep.directDependencies().size() ? null : this.generatingStep.directDependencies().get(n3);
        if (chunkStatus2 != null) {
            generationChunkHolder = this.cache.get(n, n2);
            if (chunkStatus.isOrBefore(chunkStatus2) && (object = generationChunkHolder.getChunkIfPresentUnchecked(chunkStatus2)) != null) {
                return object;
            }
        } else {
            generationChunkHolder = null;
        }
        object = CrashReport.forThrowable(new IllegalStateException("Requested chunk unavailable during world generation"), "Exception generating new chunk");
        CrashReportCategory crashReportCategory = ((CrashReport)object).addCategory("Chunk request details");
        crashReportCategory.setDetail("Requested chunk", String.format(Locale.ROOT, "%d, %d", n, n2));
        crashReportCategory.setDetail("Generating status", () -> this.generatingStep.targetStatus().getName());
        crashReportCategory.setDetail("Requested status", chunkStatus::getName);
        crashReportCategory.setDetail("Actual status", () -> generationChunkHolder == null ? "[out of cache bounds]" : generationChunkHolder.getPersistedStatus().getName());
        crashReportCategory.setDetail("Maximum allowed status", () -> chunkStatus2 == null ? "null" : chunkStatus2.getName());
        crashReportCategory.setDetail("Dependencies", this.generatingStep.directDependencies()::toString);
        crashReportCategory.setDetail("Requested distance", n3);
        crashReportCategory.setDetail("Generating chunk", this.center.getPos()::toString);
        throw new ReportedException((CrashReport)object);
    }

    @Override
    public boolean hasChunk(int n, int n2) {
        int n3 = this.center.getPos().getChessboardDistance(n, n2);
        return n3 < this.generatingStep.directDependencies().size();
    }

    @Override
    public BlockState getBlockState(BlockPos blockPos) {
        return this.getChunk(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getZ())).getBlockState(blockPos);
    }

    @Override
    public FluidState getFluidState(BlockPos blockPos) {
        return this.getChunk(blockPos).getFluidState(blockPos);
    }

    @Override
    @Nullable
    public Player getNearestPlayer(double d, double d2, double d3, double d4, Predicate<Entity> predicate) {
        return null;
    }

    @Override
    public int getSkyDarken() {
        return 0;
    }

    @Override
    public BiomeManager getBiomeManager() {
        return this.biomeManager;
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int n, int n2, int n3) {
        return this.level.getUncachedNoiseBiome(n, n2, n3);
    }

    @Override
    public float getShade(Direction direction, boolean bl) {
        return 1.0f;
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return this.level.getLightEngine();
    }

    @Override
    public boolean destroyBlock(BlockPos blockPos, boolean bl, @Nullable Entity entity, int n) {
        BlockState blockState = this.getBlockState(blockPos);
        if (blockState.isAir()) {
            return false;
        }
        if (bl) {
            BlockEntity blockEntity = blockState.hasBlockEntity() ? this.getBlockEntity(blockPos) : null;
            Block.dropResources(blockState, this.level, blockPos, blockEntity, entity, ItemStack.EMPTY);
        }
        return this.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3, n);
    }

    @Override
    @Nullable
    public BlockEntity getBlockEntity(BlockPos blockPos) {
        ChunkAccess chunkAccess = this.getChunk(blockPos);
        BlockEntity blockEntity = chunkAccess.getBlockEntity(blockPos);
        if (blockEntity != null) {
            return blockEntity;
        }
        CompoundTag compoundTag = chunkAccess.getBlockEntityNbt(blockPos);
        BlockState blockState = chunkAccess.getBlockState(blockPos);
        if (compoundTag != null) {
            if ("DUMMY".equals(compoundTag.getStringOr("id", ""))) {
                if (!blockState.hasBlockEntity()) {
                    return null;
                }
                blockEntity = ((EntityBlock)((Object)blockState.getBlock())).newBlockEntity(blockPos, blockState);
            } else {
                blockEntity = BlockEntity.loadStatic(blockPos, blockState, compoundTag, this.level.registryAccess());
            }
            if (blockEntity != null) {
                chunkAccess.setBlockEntity(blockEntity);
                return blockEntity;
            }
        }
        if (blockState.hasBlockEntity()) {
            LOGGER.warn("Tried to access a block entity before it was created. {}", (Object)blockPos);
        }
        return null;
    }

    @Override
    public boolean ensureCanWrite(BlockPos blockPos) {
        LevelHeightAccessor levelHeightAccessor;
        int n = SectionPos.blockToSectionCoord(blockPos.getX());
        int n2 = SectionPos.blockToSectionCoord(blockPos.getZ());
        ChunkPos chunkPos = this.getCenter();
        int n3 = Math.abs(chunkPos.x - n);
        int n4 = Math.abs(chunkPos.z - n2);
        if (n3 > this.generatingStep.blockStateWriteRadius() || n4 > this.generatingStep.blockStateWriteRadius()) {
            Util.logAndPauseIfInIde("Detected setBlock in a far chunk [" + n + ", " + n2 + "], pos: " + String.valueOf(blockPos) + ", status: " + String.valueOf(this.generatingStep.targetStatus()) + (String)(this.currentlyGenerating == null ? "" : ", currently generating: " + this.currentlyGenerating.get()));
            return false;
        }
        return !this.center.isUpgrading() || !(levelHeightAccessor = this.center.getHeightAccessorForGeneration()).isOutsideBuildHeight(blockPos.getY());
    }

    @Override
    public boolean setBlock(BlockPos blockPos, BlockState blockState, int n, int n2) {
        if (!this.ensureCanWrite(blockPos)) {
            return false;
        }
        ChunkAccess chunkAccess = this.getChunk(blockPos);
        BlockState blockState2 = chunkAccess.setBlockState(blockPos, blockState, n);
        if (blockState2 != null) {
            this.level.updatePOIOnBlockStateChange(blockPos, blockState2, blockState);
        }
        if (blockState.hasBlockEntity()) {
            if (chunkAccess.getPersistedStatus().getChunkType() == ChunkType.LEVELCHUNK) {
                BlockEntity blockEntity = ((EntityBlock)((Object)blockState.getBlock())).newBlockEntity(blockPos, blockState);
                if (blockEntity != null) {
                    chunkAccess.setBlockEntity(blockEntity);
                } else {
                    chunkAccess.removeBlockEntity(blockPos);
                }
            } else {
                CompoundTag compoundTag = new CompoundTag();
                compoundTag.putInt("x", blockPos.getX());
                compoundTag.putInt("y", blockPos.getY());
                compoundTag.putInt("z", blockPos.getZ());
                compoundTag.putString("id", "DUMMY");
                chunkAccess.setBlockEntityNbt(compoundTag);
            }
        } else if (blockState2 != null && blockState2.hasBlockEntity()) {
            chunkAccess.removeBlockEntity(blockPos);
        }
        if (blockState.hasPostProcess(this, blockPos) && (n & 0x10) == 0) {
            this.markPosForPostprocessing(blockPos);
        }
        return true;
    }

    private void markPosForPostprocessing(BlockPos blockPos) {
        this.getChunk(blockPos).markPosForPostprocessing(blockPos);
    }

    @Override
    public boolean addFreshEntity(Entity entity) {
        int n = SectionPos.blockToSectionCoord(entity.getBlockX());
        int n2 = SectionPos.blockToSectionCoord(entity.getBlockZ());
        this.getChunk(n, n2).addEntity(entity);
        return true;
    }

    @Override
    public boolean removeBlock(BlockPos blockPos, boolean bl) {
        return this.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
    }

    @Override
    public WorldBorder getWorldBorder() {
        return this.level.getWorldBorder();
    }

    @Override
    public boolean isClientSide() {
        return false;
    }

    @Override
    @Deprecated
    public ServerLevel getLevel() {
        return this.level;
    }

    @Override
    public RegistryAccess registryAccess() {
        return this.level.registryAccess();
    }

    @Override
    public FeatureFlagSet enabledFeatures() {
        return this.level.enabledFeatures();
    }

    @Override
    public LevelData getLevelData() {
        return this.levelData;
    }

    @Override
    public DifficultyInstance getCurrentDifficultyAt(BlockPos blockPos) {
        if (!this.hasChunk(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getZ()))) {
            throw new RuntimeException("We are asking a region for a chunk out of bound");
        }
        return new DifficultyInstance(this.level.getDifficulty(), this.level.getDayTime(), 0L, this.level.getMoonBrightness());
    }

    @Override
    @Nullable
    public MinecraftServer getServer() {
        return this.level.getServer();
    }

    @Override
    public ChunkSource getChunkSource() {
        return this.level.getChunkSource();
    }

    @Override
    public long getSeed() {
        return this.seed;
    }

    @Override
    public LevelTickAccess<Block> getBlockTicks() {
        return this.blockTicks;
    }

    @Override
    public LevelTickAccess<Fluid> getFluidTicks() {
        return this.fluidTicks;
    }

    @Override
    public int getSeaLevel() {
        return this.level.getSeaLevel();
    }

    @Override
    public RandomSource getRandom() {
        return this.random;
    }

    @Override
    public int getHeight(Heightmap.Types types, int n, int n2) {
        return this.getChunk(SectionPos.blockToSectionCoord(n), SectionPos.blockToSectionCoord(n2)).getHeight(types, n & 0xF, n2 & 0xF) + 1;
    }

    @Override
    public void playSound(@Nullable Entity entity, BlockPos blockPos, SoundEvent soundEvent, SoundSource soundSource, float f, float f2) {
    }

    @Override
    public void addParticle(ParticleOptions particleOptions, double d, double d2, double d3, double d4, double d5, double d6) {
    }

    @Override
    public void levelEvent(@Nullable Entity entity, int n, BlockPos blockPos, int n2) {
    }

    @Override
    public void gameEvent(Holder<GameEvent> holder, Vec3 vec3, GameEvent.Context context) {
    }

    @Override
    public DimensionType dimensionType() {
        return this.dimensionType;
    }

    @Override
    public boolean isStateAtPosition(BlockPos blockPos, Predicate<BlockState> predicate) {
        return predicate.test(this.getBlockState(blockPos));
    }

    @Override
    public boolean isFluidAtPosition(BlockPos blockPos, Predicate<FluidState> predicate) {
        return predicate.test(this.getFluidState(blockPos));
    }

    @Override
    public <T extends Entity> List<T> getEntities(EntityTypeTest<Entity, T> entityTypeTest, AABB aABB, Predicate<? super T> predicate) {
        return Collections.emptyList();
    }

    @Override
    public List<Entity> getEntities(@Nullable Entity entity, AABB aABB, @Nullable Predicate<? super Entity> predicate) {
        return Collections.emptyList();
    }

    public List<Player> players() {
        return Collections.emptyList();
    }

    @Override
    public int getMinY() {
        return this.level.getMinY();
    }

    @Override
    public int getHeight() {
        return this.level.getHeight();
    }

    @Override
    public long nextSubTickCount() {
        return this.subTickCount.getAndIncrement();
    }
}

