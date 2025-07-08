/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.shorts.ShortList
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.chunk;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.ProtoChunkTicks;
import net.minecraft.world.ticks.TickContainerAccess;
import org.slf4j.Logger;

public class ProtoChunk
extends ChunkAccess {
    private static final Logger LOGGER = LogUtils.getLogger();
    @Nullable
    private volatile LevelLightEngine lightEngine;
    private volatile ChunkStatus status = ChunkStatus.EMPTY;
    private final List<CompoundTag> entities = Lists.newArrayList();
    @Nullable
    private CarvingMask carvingMask;
    @Nullable
    private BelowZeroRetrogen belowZeroRetrogen;
    private final ProtoChunkTicks<Block> blockTicks;
    private final ProtoChunkTicks<Fluid> fluidTicks;

    public ProtoChunk(ChunkPos chunkPos, UpgradeData upgradeData, LevelHeightAccessor levelHeightAccessor, Registry<Biome> registry, @Nullable BlendingData blendingData) {
        this(chunkPos, upgradeData, null, new ProtoChunkTicks<Block>(), new ProtoChunkTicks<Fluid>(), levelHeightAccessor, registry, blendingData);
    }

    public ProtoChunk(ChunkPos chunkPos, UpgradeData upgradeData, @Nullable LevelChunkSection[] levelChunkSectionArray, ProtoChunkTicks<Block> protoChunkTicks, ProtoChunkTicks<Fluid> protoChunkTicks2, LevelHeightAccessor levelHeightAccessor, Registry<Biome> registry, @Nullable BlendingData blendingData) {
        super(chunkPos, upgradeData, levelHeightAccessor, registry, 0L, levelChunkSectionArray, blendingData);
        this.blockTicks = protoChunkTicks;
        this.fluidTicks = protoChunkTicks2;
    }

    @Override
    public TickContainerAccess<Block> getBlockTicks() {
        return this.blockTicks;
    }

    @Override
    public TickContainerAccess<Fluid> getFluidTicks() {
        return this.fluidTicks;
    }

    @Override
    public ChunkAccess.PackedTicks getTicksForSerialization(long l) {
        return new ChunkAccess.PackedTicks(this.blockTicks.pack(l), this.fluidTicks.pack(l));
    }

    @Override
    public BlockState getBlockState(BlockPos blockPos) {
        int n = blockPos.getY();
        if (this.isOutsideBuildHeight(n)) {
            return Blocks.VOID_AIR.defaultBlockState();
        }
        LevelChunkSection levelChunkSection = this.getSection(this.getSectionIndex(n));
        if (levelChunkSection.hasOnlyAir()) {
            return Blocks.AIR.defaultBlockState();
        }
        return levelChunkSection.getBlockState(blockPos.getX() & 0xF, n & 0xF, blockPos.getZ() & 0xF);
    }

    @Override
    public FluidState getFluidState(BlockPos blockPos) {
        int n = blockPos.getY();
        if (this.isOutsideBuildHeight(n)) {
            return Fluids.EMPTY.defaultFluidState();
        }
        LevelChunkSection levelChunkSection = this.getSection(this.getSectionIndex(n));
        if (levelChunkSection.hasOnlyAir()) {
            return Fluids.EMPTY.defaultFluidState();
        }
        return levelChunkSection.getFluidState(blockPos.getX() & 0xF, n & 0xF, blockPos.getZ() & 0xF);
    }

    @Override
    @Nullable
    public BlockState setBlockState(BlockPos blockPos, BlockState blockState, int n) {
        int n2 = blockPos.getX();
        int n3 = blockPos.getY();
        int n4 = blockPos.getZ();
        if (this.isOutsideBuildHeight(n3)) {
            return Blocks.VOID_AIR.defaultBlockState();
        }
        int n5 = this.getSectionIndex(n3);
        LevelChunkSection levelChunkSection = this.getSection(n5);
        boolean bl = levelChunkSection.hasOnlyAir();
        if (bl && blockState.is(Blocks.AIR)) {
            return blockState;
        }
        int n6 = SectionPos.sectionRelative(n2);
        int n7 = SectionPos.sectionRelative(n3);
        int n8 = SectionPos.sectionRelative(n4);
        BlockState blockState2 = levelChunkSection.setBlockState(n6, n7, n8, blockState);
        if (this.status.isOrAfter(ChunkStatus.INITIALIZE_LIGHT)) {
            boolean bl2 = levelChunkSection.hasOnlyAir();
            if (bl2 != bl) {
                this.lightEngine.updateSectionStatus(blockPos, bl2);
            }
            if (LightEngine.hasDifferentLightProperties(blockState2, blockState)) {
                this.skyLightSources.update(this, n6, n3, n8);
                this.lightEngine.checkBlock(blockPos);
            }
        }
        EnumSet<Heightmap.Types> enumSet = this.getPersistedStatus().heightmapsAfter();
        EnumSet<Heightmap.Types> enumSet2 = null;
        for (Heightmap.Types types : enumSet) {
            Heightmap heightmap = (Heightmap)this.heightmaps.get(types);
            if (heightmap != null) continue;
            if (enumSet2 == null) {
                enumSet2 = EnumSet.noneOf(Heightmap.Types.class);
            }
            enumSet2.add(types);
        }
        if (enumSet2 != null) {
            Heightmap.primeHeightmaps(this, enumSet2);
        }
        for (Heightmap.Types types : enumSet) {
            ((Heightmap)this.heightmaps.get(types)).update(n6, n3, n8, blockState);
        }
        return blockState2;
    }

    @Override
    public void setBlockEntity(BlockEntity blockEntity) {
        this.pendingBlockEntities.remove(blockEntity.getBlockPos());
        this.blockEntities.put(blockEntity.getBlockPos(), blockEntity);
    }

    @Override
    @Nullable
    public BlockEntity getBlockEntity(BlockPos blockPos) {
        return (BlockEntity)this.blockEntities.get(blockPos);
    }

    public Map<BlockPos, BlockEntity> getBlockEntities() {
        return this.blockEntities;
    }

    public void addEntity(CompoundTag compoundTag) {
        this.entities.add(compoundTag);
    }

    @Override
    public void addEntity(Entity entity) {
        if (entity.isPassenger()) {
            return;
        }
        try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(entity.problemPath(), LOGGER);){
            TagValueOutput tagValueOutput = TagValueOutput.createWithContext(scopedCollector, entity.registryAccess());
            entity.save(tagValueOutput);
            this.addEntity(tagValueOutput.buildResult());
        }
    }

    @Override
    public void setStartForStructure(Structure structure, StructureStart structureStart) {
        BelowZeroRetrogen belowZeroRetrogen = this.getBelowZeroRetrogen();
        if (belowZeroRetrogen != null && structureStart.isValid()) {
            BoundingBox boundingBox = structureStart.getBoundingBox();
            LevelHeightAccessor levelHeightAccessor = this.getHeightAccessorForGeneration();
            if (boundingBox.minY() < levelHeightAccessor.getMinY() || boundingBox.maxY() > levelHeightAccessor.getMaxY()) {
                return;
            }
        }
        super.setStartForStructure(structure, structureStart);
    }

    public List<CompoundTag> getEntities() {
        return this.entities;
    }

    @Override
    public ChunkStatus getPersistedStatus() {
        return this.status;
    }

    public void setPersistedStatus(ChunkStatus chunkStatus) {
        this.status = chunkStatus;
        if (this.belowZeroRetrogen != null && chunkStatus.isOrAfter(this.belowZeroRetrogen.targetStatus())) {
            this.setBelowZeroRetrogen(null);
        }
        this.markUnsaved();
    }

    @Override
    public Holder<Biome> getNoiseBiome(int n, int n2, int n3) {
        if (this.getHighestGeneratedStatus().isOrAfter(ChunkStatus.BIOMES)) {
            return super.getNoiseBiome(n, n2, n3);
        }
        throw new IllegalStateException("Asking for biomes before we have biomes");
    }

    public static short packOffsetCoordinates(BlockPos blockPos) {
        int n = blockPos.getX();
        int n2 = blockPos.getY();
        int n3 = blockPos.getZ();
        int n4 = n & 0xF;
        int n5 = n2 & 0xF;
        int n6 = n3 & 0xF;
        return (short)(n4 | n5 << 4 | n6 << 8);
    }

    public static BlockPos unpackOffsetCoordinates(short s, int n, ChunkPos chunkPos) {
        int n2 = SectionPos.sectionToBlockCoord(chunkPos.x, s & 0xF);
        int n3 = SectionPos.sectionToBlockCoord(n, s >>> 4 & 0xF);
        int n4 = SectionPos.sectionToBlockCoord(chunkPos.z, s >>> 8 & 0xF);
        return new BlockPos(n2, n3, n4);
    }

    @Override
    public void markPosForPostprocessing(BlockPos blockPos) {
        if (!this.isOutsideBuildHeight(blockPos)) {
            ChunkAccess.getOrCreateOffsetList(this.postProcessing, this.getSectionIndex(blockPos.getY())).add(ProtoChunk.packOffsetCoordinates(blockPos));
        }
    }

    @Override
    public void addPackedPostProcess(ShortList shortList, int n) {
        ChunkAccess.getOrCreateOffsetList(this.postProcessing, n).addAll(shortList);
    }

    public Map<BlockPos, CompoundTag> getBlockEntityNbts() {
        return Collections.unmodifiableMap(this.pendingBlockEntities);
    }

    @Override
    @Nullable
    public CompoundTag getBlockEntityNbtForSaving(BlockPos blockPos, HolderLookup.Provider provider) {
        BlockEntity blockEntity = this.getBlockEntity(blockPos);
        if (blockEntity != null) {
            return blockEntity.saveWithFullMetadata(provider);
        }
        return (CompoundTag)this.pendingBlockEntities.get(blockPos);
    }

    @Override
    public void removeBlockEntity(BlockPos blockPos) {
        this.blockEntities.remove(blockPos);
        this.pendingBlockEntities.remove(blockPos);
    }

    @Nullable
    public CarvingMask getCarvingMask() {
        return this.carvingMask;
    }

    public CarvingMask getOrCreateCarvingMask() {
        if (this.carvingMask == null) {
            this.carvingMask = new CarvingMask(this.getHeight(), this.getMinY());
        }
        return this.carvingMask;
    }

    public void setCarvingMask(CarvingMask carvingMask) {
        this.carvingMask = carvingMask;
    }

    public void setLightEngine(LevelLightEngine levelLightEngine) {
        this.lightEngine = levelLightEngine;
    }

    public void setBelowZeroRetrogen(@Nullable BelowZeroRetrogen belowZeroRetrogen) {
        this.belowZeroRetrogen = belowZeroRetrogen;
    }

    @Override
    @Nullable
    public BelowZeroRetrogen getBelowZeroRetrogen() {
        return this.belowZeroRetrogen;
    }

    private static <T> LevelChunkTicks<T> unpackTicks(ProtoChunkTicks<T> protoChunkTicks) {
        return new LevelChunkTicks<T>(protoChunkTicks.scheduledTicks());
    }

    public LevelChunkTicks<Block> unpackBlockTicks() {
        return ProtoChunk.unpackTicks(this.blockTicks);
    }

    public LevelChunkTicks<Fluid> unpackFluidTicks() {
        return ProtoChunk.unpackTicks(this.fluidTicks);
    }

    @Override
    public LevelHeightAccessor getHeightAccessorForGeneration() {
        if (this.isUpgrading()) {
            return BelowZeroRetrogen.UPGRADE_HEIGHT_ACCESSOR;
        }
        return this;
    }
}

