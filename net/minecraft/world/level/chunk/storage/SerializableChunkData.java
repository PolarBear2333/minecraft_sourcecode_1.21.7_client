/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DynamicOps
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  it.unimi.dsi.fastutil.shorts.ShortArrayList
 *  it.unimi.dsi.fastutil.shorts.ShortList
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.Optionull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkType;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.ProtoChunkTicks;
import net.minecraft.world.ticks.SavedTick;
import org.slf4j.Logger;

public record SerializableChunkData(Registry<Biome> biomeRegistry, ChunkPos chunkPos, int minSectionY, long lastUpdateTime, long inhabitedTime, ChunkStatus chunkStatus, @Nullable BlendingData.Packed blendingData, @Nullable BelowZeroRetrogen belowZeroRetrogen, UpgradeData upgradeData, @Nullable long[] carvingMask, Map<Heightmap.Types, long[]> heightmaps, ChunkAccess.PackedTicks packedTicks, ShortList[] postProcessingSections, boolean lightCorrect, List<SectionData> sectionData, List<CompoundTag> entities, List<CompoundTag> blockEntities, CompoundTag structureData) {
    private static final Codec<PalettedContainer<BlockState>> BLOCK_STATE_CODEC = PalettedContainer.codecRW(Block.BLOCK_STATE_REGISTRY, BlockState.CODEC, PalettedContainer.Strategy.SECTION_STATES, Blocks.AIR.defaultBlockState());
    private static final Codec<List<SavedTick<Block>>> BLOCK_TICKS_CODEC = SavedTick.codec(BuiltInRegistries.BLOCK.byNameCodec()).listOf();
    private static final Codec<List<SavedTick<Fluid>>> FLUID_TICKS_CODEC = SavedTick.codec(BuiltInRegistries.FLUID.byNameCodec()).listOf();
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TAG_UPGRADE_DATA = "UpgradeData";
    private static final String BLOCK_TICKS_TAG = "block_ticks";
    private static final String FLUID_TICKS_TAG = "fluid_ticks";
    public static final String X_POS_TAG = "xPos";
    public static final String Z_POS_TAG = "zPos";
    public static final String HEIGHTMAPS_TAG = "Heightmaps";
    public static final String IS_LIGHT_ON_TAG = "isLightOn";
    public static final String SECTIONS_TAG = "sections";
    public static final String BLOCK_LIGHT_TAG = "BlockLight";
    public static final String SKY_LIGHT_TAG = "SkyLight";

    @Nullable
    public static SerializableChunkData parse(LevelHeightAccessor levelHeightAccessor, RegistryAccess registryAccess, CompoundTag compoundTag2) {
        Object object;
        List<Tag> list;
        if (compoundTag2.getString("Status").isEmpty()) {
            return null;
        }
        ChunkPos chunkPos = new ChunkPos(compoundTag2.getIntOr(X_POS_TAG, 0), compoundTag2.getIntOr(Z_POS_TAG, 0));
        long l = compoundTag2.getLongOr("LastUpdate", 0L);
        long l2 = compoundTag2.getLongOr("InhabitedTime", 0L);
        ChunkStatus chunkStatus = compoundTag2.read("Status", ChunkStatus.CODEC).orElse(ChunkStatus.EMPTY);
        UpgradeData upgradeData = compoundTag2.getCompound(TAG_UPGRADE_DATA).map(compoundTag -> new UpgradeData((CompoundTag)compoundTag, levelHeightAccessor)).orElse(UpgradeData.EMPTY);
        boolean bl = compoundTag2.getBooleanOr(IS_LIGHT_ON_TAG, false);
        BlendingData.Packed packed = compoundTag2.read("blending_data", BlendingData.Packed.CODEC).orElse(null);
        BelowZeroRetrogen belowZeroRetrogen = compoundTag2.read("below_zero_retrogen", BelowZeroRetrogen.CODEC).orElse(null);
        long[] lArray = compoundTag2.getLongArray("carving_mask").orElse(null);
        EnumMap<Heightmap.Types, long[]> enumMap = new EnumMap<Heightmap.Types, long[]>(Heightmap.Types.class);
        compoundTag2.getCompound(HEIGHTMAPS_TAG).ifPresent(compoundTag -> {
            for (Heightmap.Types types : chunkStatus.heightmapsAfter()) {
                compoundTag.getLongArray(types.getSerializationKey()).ifPresent(lArray -> enumMap.put(types, (long[])lArray));
            }
        });
        List<SavedTick<Block>> list2 = SavedTick.filterTickListForChunk(compoundTag2.read(BLOCK_TICKS_TAG, BLOCK_TICKS_CODEC).orElse(List.of()), chunkPos);
        List<SavedTick<Fluid>> list3 = SavedTick.filterTickListForChunk(compoundTag2.read(FLUID_TICKS_TAG, FLUID_TICKS_CODEC).orElse(List.of()), chunkPos);
        ChunkAccess.PackedTicks packedTicks = new ChunkAccess.PackedTicks(list2, list3);
        ListTag listTag = compoundTag2.getListOrEmpty("PostProcessing");
        ShortList[] shortListArray = new ShortList[listTag.size()];
        for (int i = 0; i < listTag.size(); ++i) {
            list = listTag.getListOrEmpty(i);
            object = new ShortArrayList(((ListTag)list).size());
            for (int j = 0; j < ((ListTag)list).size(); ++j) {
                object.add(((ListTag)list).getShortOr(j, (short)0));
            }
            shortListArray[i] = object;
        }
        List<CompoundTag> list4 = compoundTag2.getList("entities").stream().flatMap(ListTag::compoundStream).toList();
        list = compoundTag2.getList("block_entities").stream().flatMap(ListTag::compoundStream).toList();
        object = compoundTag2.getCompoundOrEmpty("structures");
        ListTag listTag2 = compoundTag2.getListOrEmpty(SECTIONS_TAG);
        ArrayList<SectionData> arrayList = new ArrayList<SectionData>(listTag2.size());
        HolderLookup.RegistryLookup registryLookup = registryAccess.lookupOrThrow(Registries.BIOME);
        Codec<PalettedContainerRO<Holder<Biome>>> codec = SerializableChunkData.makeBiomeCodec((Registry<Biome>)registryLookup);
        for (int i = 0; i < listTag2.size(); ++i) {
            LevelChunkSection levelChunkSection;
            Object object2;
            Object object3;
            Optional<CompoundTag> optional = listTag2.getCompound(i);
            if (optional.isEmpty()) continue;
            CompoundTag compoundTag3 = optional.get();
            byte by = compoundTag3.getByteOr("Y", (byte)0);
            if (by >= levelHeightAccessor.getMinSectionY() && by <= levelHeightAccessor.getMaxSectionY()) {
                object3 = compoundTag3.getCompound("block_states").map(compoundTag -> (PalettedContainer)BLOCK_STATE_CODEC.parse((DynamicOps)NbtOps.INSTANCE, compoundTag).promotePartial(string -> SerializableChunkData.logErrors(chunkPos, by, string)).getOrThrow(ChunkReadException::new)).orElseGet(() -> new PalettedContainer<BlockState>(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES));
                object2 = compoundTag3.getCompound("biomes").map(compoundTag -> (PalettedContainerRO)codec.parse((DynamicOps)NbtOps.INSTANCE, compoundTag).promotePartial(string -> SerializableChunkData.logErrors(chunkPos, by, string)).getOrThrow(ChunkReadException::new)).orElseGet(() -> SerializableChunkData.lambda$parse$8((Registry)registryLookup));
                levelChunkSection = new LevelChunkSection((PalettedContainer<BlockState>)object3, (PalettedContainerRO<Holder<Biome>>)object2);
            } else {
                levelChunkSection = null;
            }
            object3 = compoundTag3.getByteArray(BLOCK_LIGHT_TAG).map(DataLayer::new).orElse(null);
            object2 = compoundTag3.getByteArray(SKY_LIGHT_TAG).map(DataLayer::new).orElse(null);
            arrayList.add(new SectionData(by, levelChunkSection, (DataLayer)object3, (DataLayer)object2));
        }
        return new SerializableChunkData((Registry<Biome>)registryLookup, chunkPos, levelHeightAccessor.getMinSectionY(), l, l2, chunkStatus, packed, belowZeroRetrogen, upgradeData, lArray, enumMap, packedTicks, shortListArray, bl, arrayList, list4, list, (CompoundTag)object);
    }

    /*
     * WARNING - void declaration
     */
    public ProtoChunk read(ServerLevel serverLevel, PoiManager poiManager, RegionStorageInfo regionStorageInfo, ChunkPos chunkPos) {
        void var13_16;
        Object object;
        if (!Objects.equals(chunkPos, this.chunkPos)) {
            LOGGER.error("Chunk file at {} is in the wrong location; relocating. (Expected {}, got {})", new Object[]{chunkPos, chunkPos, this.chunkPos});
            serverLevel.getServer().reportMisplacedChunk(this.chunkPos, chunkPos, regionStorageInfo);
        }
        int n = serverLevel.getSectionsCount();
        LevelChunkSection[] levelChunkSectionArray = new LevelChunkSection[n];
        boolean bl = serverLevel.dimensionType().hasSkyLight();
        ServerChunkCache serverChunkCache = serverLevel.getChunkSource();
        LevelLightEngine levelLightEngine = ((ChunkSource)serverChunkCache).getLightEngine();
        HolderLookup.RegistryLookup registryLookup = serverLevel.registryAccess().lookupOrThrow(Registries.BIOME);
        boolean bl2 = false;
        for (SectionData object22 : this.sectionData) {
            boolean bl3;
            object = SectionPos.of(chunkPos, object22.y);
            if (object22.chunkSection != null) {
                levelChunkSectionArray[serverLevel.getSectionIndexFromSectionY((int)object22.y)] = object22.chunkSection;
                poiManager.checkConsistencyWithBlocks((SectionPos)object, object22.chunkSection);
            }
            boolean protoChunk = object22.blockLight != null;
            boolean bl4 = bl3 = bl && object22.skyLight != null;
            if (!protoChunk && !bl3) continue;
            if (!bl2) {
                levelLightEngine.retainData(chunkPos, true);
                bl2 = true;
            }
            if (protoChunk) {
                levelLightEngine.queueSectionData(LightLayer.BLOCK, (SectionPos)object, object22.blockLight);
            }
            if (!bl3) continue;
            levelLightEngine.queueSectionData(LightLayer.SKY, (SectionPos)object, object22.skyLight);
        }
        Object object3 = this.chunkStatus.getChunkType();
        if (object3 == ChunkType.LEVELCHUNK) {
            object = new LevelChunkTicks<Block>(this.packedTicks.blocks());
            var15_19 = new LevelChunkTicks(this.packedTicks.fluids());
            LevelChunk levelChunk = new LevelChunk(serverLevel.getLevel(), chunkPos, this.upgradeData, (LevelChunkTicks<Block>)object, (LevelChunkTicks<Fluid>)var15_19, this.inhabitedTime, levelChunkSectionArray, SerializableChunkData.postLoadChunk(serverLevel, this.entities, this.blockEntities), BlendingData.unpack(this.blendingData));
        } else {
            ProtoChunk protoChunk;
            object = ProtoChunkTicks.load(this.packedTicks.blocks());
            var15_19 = ProtoChunkTicks.load(this.packedTicks.fluids());
            ProtoChunk protoChunk2 = protoChunk = new ProtoChunk(chunkPos, this.upgradeData, levelChunkSectionArray, (ProtoChunkTicks<Block>)object, (ProtoChunkTicks<Fluid>)var15_19, serverLevel, (Registry<Biome>)registryLookup, BlendingData.unpack(this.blendingData));
            protoChunk2.setInhabitedTime(this.inhabitedTime);
            if (this.belowZeroRetrogen != null) {
                protoChunk.setBelowZeroRetrogen(this.belowZeroRetrogen);
            }
            protoChunk.setPersistedStatus(this.chunkStatus);
            if (this.chunkStatus.isOrAfter(ChunkStatus.INITIALIZE_LIGHT)) {
                protoChunk.setLightEngine(levelLightEngine);
            }
        }
        var13_16.setLightCorrect(this.lightCorrect);
        object = EnumSet.noneOf(Heightmap.Types.class);
        for (Heightmap.Types types : var13_16.getPersistedStatus().heightmapsAfter()) {
            long[] lArray = this.heightmaps.get(types);
            if (lArray != null) {
                var13_16.setHeightmap(types, lArray);
                continue;
            }
            ((AbstractCollection)object).add(types);
        }
        Heightmap.primeHeightmaps((ChunkAccess)var13_16, (Set<Heightmap.Types>)object);
        var13_16.setAllStarts(SerializableChunkData.unpackStructureStart(StructurePieceSerializationContext.fromLevel(serverLevel), this.structureData, serverLevel.getSeed()));
        var13_16.setAllReferences(SerializableChunkData.unpackStructureReferences(serverLevel.registryAccess(), chunkPos, this.structureData));
        for (int i = 0; i < this.postProcessingSections.length; ++i) {
            var13_16.addPackedPostProcess(this.postProcessingSections[i], i);
        }
        if (object3 == ChunkType.LEVELCHUNK) {
            return new ImposterProtoChunk((LevelChunk)var13_16, false);
        }
        ProtoChunk protoChunk = (ProtoChunk)var13_16;
        for (CompoundTag compoundTag : this.entities) {
            protoChunk.addEntity(compoundTag);
        }
        for (CompoundTag compoundTag : this.blockEntities) {
            protoChunk.setBlockEntityNbt(compoundTag);
        }
        if (this.carvingMask != null) {
            protoChunk.setCarvingMask(new CarvingMask(this.carvingMask, var13_16.getMinY()));
        }
        return protoChunk;
    }

    private static void logErrors(ChunkPos chunkPos, int n, String string) {
        LOGGER.error("Recoverable errors when loading section [{}, {}, {}]: {}", new Object[]{chunkPos.x, n, chunkPos.z, string});
    }

    private static Codec<PalettedContainerRO<Holder<Biome>>> makeBiomeCodec(Registry<Biome> registry) {
        return PalettedContainer.codecRO(registry.asHolderIdMap(), registry.holderByNameCodec(), PalettedContainer.Strategy.SECTION_BIOMES, registry.getOrThrow(Biomes.PLAINS));
    }

    public static SerializableChunkData copyOf(ServerLevel serverLevel, ChunkAccess chunkAccess) {
        Object object;
        Object object2;
        Object object3;
        if (!chunkAccess.canBeSerialized()) {
            throw new IllegalArgumentException("Chunk can't be serialized: " + String.valueOf(chunkAccess));
        }
        ChunkPos chunkPos = chunkAccess.getPos();
        ArrayList<SectionData> arrayList = new ArrayList<SectionData>();
        LevelChunkSection[] levelChunkSectionArray = chunkAccess.getSections();
        ThreadedLevelLightEngine threadedLevelLightEngine = serverLevel.getChunkSource().getLightEngine();
        for (int i = threadedLevelLightEngine.getMinLightSection(); i < threadedLevelLightEngine.getMaxLightSection(); ++i) {
            int n = chunkAccess.getSectionIndexFromSectionY(i);
            boolean bl = n >= 0 && n < levelChunkSectionArray.length;
            object3 = threadedLevelLightEngine.getLayerListener(LightLayer.BLOCK).getDataLayerData(SectionPos.of(chunkPos, i));
            object2 = threadedLevelLightEngine.getLayerListener(LightLayer.SKY).getDataLayerData(SectionPos.of(chunkPos, i));
            DataLayer shortListArray2 = object3 != null && !((DataLayer)object3).isEmpty() ? ((DataLayer)object3).copy() : null;
            Object object4 = object = object2 != null && !((DataLayer)object2).isEmpty() ? ((DataLayer)object2).copy() : null;
            if (!bl && shortListArray2 == null && object == null) continue;
            LevelChunkSection levelChunkSection = bl ? levelChunkSectionArray[n].copy() : null;
            arrayList.add(new SectionData(i, levelChunkSection, shortListArray2, (DataLayer)object));
        }
        ArrayList<CompoundTag> arrayList2 = new ArrayList<CompoundTag>(chunkAccess.getBlockEntitiesPos().size());
        for (BlockPos blockPos : chunkAccess.getBlockEntitiesPos()) {
            object3 = chunkAccess.getBlockEntityNbtForSaving(blockPos, serverLevel.registryAccess());
            if (object3 == null) continue;
            arrayList2.add((CompoundTag)object3);
        }
        ArrayList arrayList3 = new ArrayList();
        long[] lArray = null;
        if (chunkAccess.getPersistedStatus().getChunkType() == ChunkType.PROTOCHUNK) {
            object3 = (ProtoChunk)chunkAccess;
            arrayList3.addAll(((ProtoChunk)object3).getEntities());
            object2 = ((ProtoChunk)object3).getCarvingMask();
            if (object2 != null) {
                lArray = ((CarvingMask)object2).toArray();
            }
        }
        object3 = new EnumMap<Heightmap.Types, long[]>(Heightmap.Types.class);
        for (Map.Entry entry : chunkAccess.getHeightmaps()) {
            if (!chunkAccess.getPersistedStatus().heightmapsAfter().contains(entry.getKey())) continue;
            object = ((Heightmap)entry.getValue()).getRawData();
            object3.put((Heightmap.Types)((Heightmap.Types)entry.getKey()), (long[])object.clone());
        }
        object2 = chunkAccess.getTicksForSerialization(serverLevel.getGameTime());
        ShortList[] shortListArray = (ShortList[])Arrays.stream(chunkAccess.getPostProcessing()).map(shortList -> shortList != null ? new ShortArrayList(shortList) : null).toArray(ShortList[]::new);
        object = SerializableChunkData.packStructureData(StructurePieceSerializationContext.fromLevel(serverLevel), chunkPos, chunkAccess.getAllStarts(), chunkAccess.getAllReferences());
        return new SerializableChunkData((Registry<Biome>)serverLevel.registryAccess().lookupOrThrow(Registries.BIOME), chunkPos, chunkAccess.getMinSectionY(), serverLevel.getGameTime(), chunkAccess.getInhabitedTime(), chunkAccess.getPersistedStatus(), Optionull.map(chunkAccess.getBlendingData(), BlendingData::pack), chunkAccess.getBelowZeroRetrogen(), chunkAccess.getUpgradeData().copy(), lArray, (Map<Heightmap.Types, long[]>)object3, (ChunkAccess.PackedTicks)object2, shortListArray, chunkAccess.isLightCorrect(), (List<SectionData>)arrayList, arrayList3, (List<CompoundTag>)arrayList2, (CompoundTag)object);
    }

    public CompoundTag write() {
        CompoundTag compoundTag = NbtUtils.addCurrentDataVersion(new CompoundTag());
        compoundTag.putInt(X_POS_TAG, this.chunkPos.x);
        compoundTag.putInt("yPos", this.minSectionY);
        compoundTag.putInt(Z_POS_TAG, this.chunkPos.z);
        compoundTag.putLong("LastUpdate", this.lastUpdateTime);
        compoundTag.putLong("InhabitedTime", this.inhabitedTime);
        compoundTag.putString("Status", BuiltInRegistries.CHUNK_STATUS.getKey(this.chunkStatus).toString());
        compoundTag.storeNullable("blending_data", BlendingData.Packed.CODEC, this.blendingData);
        compoundTag.storeNullable("below_zero_retrogen", BelowZeroRetrogen.CODEC, this.belowZeroRetrogen);
        if (!this.upgradeData.isEmpty()) {
            compoundTag.put(TAG_UPGRADE_DATA, this.upgradeData.write());
        }
        ListTag listTag = new ListTag();
        Codec<PalettedContainerRO<Holder<Biome>>> codec = SerializableChunkData.makeBiomeCodec(this.biomeRegistry);
        for (SectionData object2 : this.sectionData) {
            CompoundTag compoundTag2 = new CompoundTag();
            LevelChunkSection levelChunkSection = object2.chunkSection;
            if (levelChunkSection != null) {
                compoundTag2.store("block_states", BLOCK_STATE_CODEC, levelChunkSection.getStates());
                compoundTag2.store("biomes", codec, levelChunkSection.getBiomes());
            }
            if (object2.blockLight != null) {
                compoundTag2.putByteArray(BLOCK_LIGHT_TAG, object2.blockLight.getData());
            }
            if (object2.skyLight != null) {
                compoundTag2.putByteArray(SKY_LIGHT_TAG, object2.skyLight.getData());
            }
            if (compoundTag2.isEmpty()) continue;
            compoundTag2.putByte("Y", (byte)object2.y);
            listTag.add(compoundTag2);
        }
        compoundTag.put(SECTIONS_TAG, listTag);
        if (this.lightCorrect) {
            compoundTag.putBoolean(IS_LIGHT_ON_TAG, true);
        }
        ListTag listTag2 = new ListTag();
        listTag2.addAll(this.blockEntities);
        compoundTag.put("block_entities", listTag2);
        if (this.chunkStatus.getChunkType() == ChunkType.PROTOCHUNK) {
            ListTag listTag3 = new ListTag();
            listTag3.addAll(this.entities);
            compoundTag.put("entities", listTag3);
            if (this.carvingMask != null) {
                compoundTag.putLongArray("carving_mask", this.carvingMask);
            }
        }
        SerializableChunkData.saveTicks(compoundTag, this.packedTicks);
        compoundTag.put("PostProcessing", SerializableChunkData.packOffsets(this.postProcessingSections));
        CompoundTag compoundTag3 = new CompoundTag();
        this.heightmaps.forEach((types, lArray) -> compoundTag3.put(types.getSerializationKey(), new LongArrayTag((long[])lArray)));
        compoundTag.put(HEIGHTMAPS_TAG, compoundTag3);
        compoundTag.put("structures", this.structureData);
        return compoundTag;
    }

    private static void saveTicks(CompoundTag compoundTag, ChunkAccess.PackedTicks packedTicks) {
        compoundTag.store(BLOCK_TICKS_TAG, BLOCK_TICKS_CODEC, packedTicks.blocks());
        compoundTag.store(FLUID_TICKS_TAG, FLUID_TICKS_CODEC, packedTicks.fluids());
    }

    public static ChunkStatus getChunkStatusFromTag(@Nullable CompoundTag compoundTag) {
        return compoundTag != null ? compoundTag.read("Status", ChunkStatus.CODEC).orElse(ChunkStatus.EMPTY) : ChunkStatus.EMPTY;
    }

    @Nullable
    private static LevelChunk.PostLoadProcessor postLoadChunk(ServerLevel serverLevel, List<CompoundTag> list, List<CompoundTag> list2) {
        if (list.isEmpty() && list2.isEmpty()) {
            return null;
        }
        return levelChunk -> {
            if (!list.isEmpty()) {
                try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(levelChunk.problemPath(), LOGGER);){
                    serverLevel.addLegacyChunkEntities(EntityType.loadEntitiesRecursive(TagValueInput.create((ProblemReporter)scopedCollector, (HolderLookup.Provider)serverLevel.registryAccess(), list), serverLevel, EntitySpawnReason.LOAD));
                }
            }
            for (CompoundTag compoundTag : list2) {
                boolean bl = compoundTag.getBooleanOr("keepPacked", false);
                if (bl) {
                    levelChunk.setBlockEntityNbt(compoundTag);
                    continue;
                }
                BlockPos blockPos = BlockEntity.getPosFromTag(levelChunk.getPos(), compoundTag);
                BlockEntity blockEntity = BlockEntity.loadStatic(blockPos, levelChunk.getBlockState(blockPos), compoundTag, serverLevel.registryAccess());
                if (blockEntity == null) continue;
                levelChunk.setBlockEntity(blockEntity);
            }
        };
    }

    private static CompoundTag packStructureData(StructurePieceSerializationContext structurePieceSerializationContext, ChunkPos chunkPos, Map<Structure, StructureStart> map, Map<Structure, LongSet> map2) {
        CompoundTag compoundTag = new CompoundTag();
        CompoundTag compoundTag2 = new CompoundTag();
        HolderLookup.RegistryLookup registryLookup = structurePieceSerializationContext.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        for (Map.Entry<Structure, StructureStart> object : map.entrySet()) {
            ResourceLocation resourceLocation = registryLookup.getKey(object.getKey());
            compoundTag2.put(resourceLocation.toString(), object.getValue().createTag(structurePieceSerializationContext, chunkPos));
        }
        compoundTag.put("starts", compoundTag2);
        CompoundTag compoundTag3 = new CompoundTag();
        for (Map.Entry<Structure, LongSet> entry : map2.entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            ResourceLocation resourceLocation = registryLookup.getKey(entry.getKey());
            compoundTag3.putLongArray(resourceLocation.toString(), entry.getValue().toLongArray());
        }
        compoundTag.put("References", compoundTag3);
        return compoundTag;
    }

    private static Map<Structure, StructureStart> unpackStructureStart(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag, long l) {
        HashMap hashMap = Maps.newHashMap();
        HolderLookup.RegistryLookup registryLookup = structurePieceSerializationContext.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        CompoundTag compoundTag2 = compoundTag.getCompoundOrEmpty("starts");
        for (String string : compoundTag2.keySet()) {
            ResourceLocation resourceLocation = ResourceLocation.tryParse(string);
            Structure structure = (Structure)registryLookup.getValue(resourceLocation);
            if (structure == null) {
                LOGGER.error("Unknown structure start: {}", (Object)resourceLocation);
                continue;
            }
            StructureStart structureStart = StructureStart.loadStaticStart(structurePieceSerializationContext, compoundTag2.getCompoundOrEmpty(string), l);
            if (structureStart == null) continue;
            hashMap.put(structure, structureStart);
        }
        return hashMap;
    }

    private static Map<Structure, LongSet> unpackStructureReferences(RegistryAccess registryAccess, ChunkPos chunkPos, CompoundTag compoundTag) {
        HashMap hashMap = Maps.newHashMap();
        HolderLookup.RegistryLookup registryLookup = registryAccess.lookupOrThrow(Registries.STRUCTURE);
        CompoundTag compoundTag2 = compoundTag.getCompoundOrEmpty("References");
        compoundTag2.forEach((arg_0, arg_1) -> SerializableChunkData.lambda$unpackStructureReferences$14((Registry)registryLookup, chunkPos, hashMap, arg_0, arg_1));
        return hashMap;
    }

    private static ListTag packOffsets(ShortList[] shortListArray) {
        ListTag listTag = new ListTag();
        for (ShortList shortList : shortListArray) {
            ListTag listTag2 = new ListTag();
            if (shortList != null) {
                for (int i = 0; i < shortList.size(); ++i) {
                    listTag2.add(ShortTag.valueOf(shortList.getShort(i)));
                }
            }
            listTag.add(listTag2);
        }
        return listTag;
    }

    private static /* synthetic */ void lambda$unpackStructureReferences$14(Registry registry, ChunkPos chunkPos, Map map, String string, Tag tag) {
        ResourceLocation resourceLocation = ResourceLocation.tryParse(string);
        Structure structure = (Structure)registry.getValue(resourceLocation);
        if (structure == null) {
            LOGGER.warn("Found reference to unknown structure '{}' in chunk {}, discarding", (Object)resourceLocation, (Object)chunkPos);
            return;
        }
        Optional<long[]> optional = tag.asLongArray();
        if (optional.isEmpty()) {
            return;
        }
        map.put(structure, new LongOpenHashSet(Arrays.stream(optional.get()).filter(l -> {
            ChunkPos chunkPos2 = new ChunkPos(l);
            if (chunkPos2.getChessboardDistance(chunkPos) > 8) {
                LOGGER.warn("Found invalid structure reference [ {} @ {} ] for chunk {}.", new Object[]{resourceLocation, chunkPos2, chunkPos});
                return false;
            }
            return true;
        }).toArray()));
    }

    private static /* synthetic */ PalettedContainerRO lambda$parse$8(Registry registry) {
        return new PalettedContainer<Holder.Reference>(registry.asHolderIdMap(), registry.getOrThrow(Biomes.PLAINS), PalettedContainer.Strategy.SECTION_BIOMES);
    }

    public static final class SectionData
    extends Record {
        final int y;
        @Nullable
        final LevelChunkSection chunkSection;
        @Nullable
        final DataLayer blockLight;
        @Nullable
        final DataLayer skyLight;

        public SectionData(int n, @Nullable LevelChunkSection levelChunkSection, @Nullable DataLayer dataLayer, @Nullable DataLayer dataLayer2) {
            this.y = n;
            this.chunkSection = levelChunkSection;
            this.blockLight = dataLayer;
            this.skyLight = dataLayer2;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{SectionData.class, "y;chunkSection;blockLight;skyLight", "y", "chunkSection", "blockLight", "skyLight"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{SectionData.class, "y;chunkSection;blockLight;skyLight", "y", "chunkSection", "blockLight", "skyLight"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{SectionData.class, "y;chunkSection;blockLight;skyLight", "y", "chunkSection", "blockLight", "skyLight"}, this, object);
        }

        public int y() {
            return this.y;
        }

        @Nullable
        public LevelChunkSection chunkSection() {
            return this.chunkSection;
        }

        @Nullable
        public DataLayer blockLight() {
            return this.blockLight;
        }

        @Nullable
        public DataLayer skyLight() {
            return this.skyLight;
        }
    }

    public static class ChunkReadException
    extends NbtException {
        public ChunkReadException(String string) {
            super(string);
        }
    }
}

