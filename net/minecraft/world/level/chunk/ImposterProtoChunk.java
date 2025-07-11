/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.chunk;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.lighting.ChunkSkyLightSources;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.ticks.BlackholeTickAccess;
import net.minecraft.world.ticks.TickContainerAccess;

public class ImposterProtoChunk
extends ProtoChunk {
    private final LevelChunk wrapped;
    private final boolean allowWrites;

    public ImposterProtoChunk(LevelChunk levelChunk, boolean bl) {
        super(levelChunk.getPos(), UpgradeData.EMPTY, levelChunk.levelHeightAccessor, (Registry<Biome>)levelChunk.getLevel().registryAccess().lookupOrThrow(Registries.BIOME), levelChunk.getBlendingData());
        this.wrapped = levelChunk;
        this.allowWrites = bl;
    }

    @Override
    @Nullable
    public BlockEntity getBlockEntity(BlockPos blockPos) {
        return this.wrapped.getBlockEntity(blockPos);
    }

    @Override
    public BlockState getBlockState(BlockPos blockPos) {
        return this.wrapped.getBlockState(blockPos);
    }

    @Override
    public FluidState getFluidState(BlockPos blockPos) {
        return this.wrapped.getFluidState(blockPos);
    }

    @Override
    public LevelChunkSection getSection(int n) {
        if (this.allowWrites) {
            return this.wrapped.getSection(n);
        }
        return super.getSection(n);
    }

    @Override
    @Nullable
    public BlockState setBlockState(BlockPos blockPos, BlockState blockState, int n) {
        if (this.allowWrites) {
            return this.wrapped.setBlockState(blockPos, blockState, n);
        }
        return null;
    }

    @Override
    public void setBlockEntity(BlockEntity blockEntity) {
        if (this.allowWrites) {
            this.wrapped.setBlockEntity(blockEntity);
        }
    }

    @Override
    public void addEntity(Entity entity) {
        if (this.allowWrites) {
            this.wrapped.addEntity(entity);
        }
    }

    @Override
    public void setPersistedStatus(ChunkStatus chunkStatus) {
        if (this.allowWrites) {
            super.setPersistedStatus(chunkStatus);
        }
    }

    @Override
    public LevelChunkSection[] getSections() {
        return this.wrapped.getSections();
    }

    @Override
    public void setHeightmap(Heightmap.Types types, long[] lArray) {
    }

    private Heightmap.Types fixType(Heightmap.Types types) {
        if (types == Heightmap.Types.WORLD_SURFACE_WG) {
            return Heightmap.Types.WORLD_SURFACE;
        }
        if (types == Heightmap.Types.OCEAN_FLOOR_WG) {
            return Heightmap.Types.OCEAN_FLOOR;
        }
        return types;
    }

    @Override
    public Heightmap getOrCreateHeightmapUnprimed(Heightmap.Types types) {
        return this.wrapped.getOrCreateHeightmapUnprimed(types);
    }

    @Override
    public int getHeight(Heightmap.Types types, int n, int n2) {
        return this.wrapped.getHeight(this.fixType(types), n, n2);
    }

    @Override
    public Holder<Biome> getNoiseBiome(int n, int n2, int n3) {
        return this.wrapped.getNoiseBiome(n, n2, n3);
    }

    @Override
    public ChunkPos getPos() {
        return this.wrapped.getPos();
    }

    @Override
    @Nullable
    public StructureStart getStartForStructure(Structure structure) {
        return this.wrapped.getStartForStructure(structure);
    }

    @Override
    public void setStartForStructure(Structure structure, StructureStart structureStart) {
    }

    @Override
    public Map<Structure, StructureStart> getAllStarts() {
        return this.wrapped.getAllStarts();
    }

    @Override
    public void setAllStarts(Map<Structure, StructureStart> map) {
    }

    @Override
    public LongSet getReferencesForStructure(Structure structure) {
        return this.wrapped.getReferencesForStructure(structure);
    }

    @Override
    public void addReferenceForStructure(Structure structure, long l) {
    }

    @Override
    public Map<Structure, LongSet> getAllReferences() {
        return this.wrapped.getAllReferences();
    }

    @Override
    public void setAllReferences(Map<Structure, LongSet> map) {
    }

    @Override
    public void markUnsaved() {
        this.wrapped.markUnsaved();
    }

    @Override
    public boolean canBeSerialized() {
        return false;
    }

    @Override
    public boolean tryMarkSaved() {
        return false;
    }

    @Override
    public boolean isUnsaved() {
        return false;
    }

    @Override
    public ChunkStatus getPersistedStatus() {
        return this.wrapped.getPersistedStatus();
    }

    @Override
    public void removeBlockEntity(BlockPos blockPos) {
    }

    @Override
    public void markPosForPostprocessing(BlockPos blockPos) {
    }

    @Override
    public void setBlockEntityNbt(CompoundTag compoundTag) {
    }

    @Override
    @Nullable
    public CompoundTag getBlockEntityNbt(BlockPos blockPos) {
        return this.wrapped.getBlockEntityNbt(blockPos);
    }

    @Override
    @Nullable
    public CompoundTag getBlockEntityNbtForSaving(BlockPos blockPos, HolderLookup.Provider provider) {
        return this.wrapped.getBlockEntityNbtForSaving(blockPos, provider);
    }

    @Override
    public void findBlocks(Predicate<BlockState> predicate, BiConsumer<BlockPos, BlockState> biConsumer) {
        this.wrapped.findBlocks(predicate, biConsumer);
    }

    @Override
    public TickContainerAccess<Block> getBlockTicks() {
        if (this.allowWrites) {
            return this.wrapped.getBlockTicks();
        }
        return BlackholeTickAccess.emptyContainer();
    }

    @Override
    public TickContainerAccess<Fluid> getFluidTicks() {
        if (this.allowWrites) {
            return this.wrapped.getFluidTicks();
        }
        return BlackholeTickAccess.emptyContainer();
    }

    @Override
    public ChunkAccess.PackedTicks getTicksForSerialization(long l) {
        return this.wrapped.getTicksForSerialization(l);
    }

    @Override
    @Nullable
    public BlendingData getBlendingData() {
        return this.wrapped.getBlendingData();
    }

    @Override
    public CarvingMask getCarvingMask() {
        if (this.allowWrites) {
            return super.getCarvingMask();
        }
        throw Util.pauseInIde(new UnsupportedOperationException("Meaningless in this context"));
    }

    @Override
    public CarvingMask getOrCreateCarvingMask() {
        if (this.allowWrites) {
            return super.getOrCreateCarvingMask();
        }
        throw Util.pauseInIde(new UnsupportedOperationException("Meaningless in this context"));
    }

    public LevelChunk getWrapped() {
        return this.wrapped;
    }

    @Override
    public boolean isLightCorrect() {
        return this.wrapped.isLightCorrect();
    }

    @Override
    public void setLightCorrect(boolean bl) {
        this.wrapped.setLightCorrect(bl);
    }

    @Override
    public void fillBiomesFromNoise(BiomeResolver biomeResolver, Climate.Sampler sampler) {
        if (this.allowWrites) {
            this.wrapped.fillBiomesFromNoise(biomeResolver, sampler);
        }
    }

    @Override
    public void initializeLightSources() {
        this.wrapped.initializeLightSources();
    }

    @Override
    public ChunkSkyLightSources getSkyLightSources() {
        return this.wrapped.getSkyLightSources();
    }
}

