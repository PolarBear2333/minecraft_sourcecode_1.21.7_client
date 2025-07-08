/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Maps
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.longs.LongArrayList
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.StructureFeatureIndexSavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class LegacyStructureDataHandler {
    private static final Map<String, String> CURRENT_TO_LEGACY_MAP = Util.make(Maps.newHashMap(), hashMap -> {
        hashMap.put("Village", "Village");
        hashMap.put("Mineshaft", "Mineshaft");
        hashMap.put("Mansion", "Mansion");
        hashMap.put("Igloo", "Temple");
        hashMap.put("Desert_Pyramid", "Temple");
        hashMap.put("Jungle_Pyramid", "Temple");
        hashMap.put("Swamp_Hut", "Temple");
        hashMap.put("Stronghold", "Stronghold");
        hashMap.put("Monument", "Monument");
        hashMap.put("Fortress", "Fortress");
        hashMap.put("EndCity", "EndCity");
    });
    private static final Map<String, String> LEGACY_TO_CURRENT_MAP = Util.make(Maps.newHashMap(), hashMap -> {
        hashMap.put("Iglu", "Igloo");
        hashMap.put("TeDP", "Desert_Pyramid");
        hashMap.put("TeJP", "Jungle_Pyramid");
        hashMap.put("TeSH", "Swamp_Hut");
    });
    private static final Set<String> OLD_STRUCTURE_REGISTRY_KEYS = Set.of("pillager_outpost", "mineshaft", "mansion", "jungle_pyramid", "desert_pyramid", "igloo", "ruined_portal", "shipwreck", "swamp_hut", "stronghold", "monument", "ocean_ruin", "fortress", "endcity", "buried_treasure", "village", "nether_fossil", "bastion_remnant");
    private final boolean hasLegacyData;
    private final Map<String, Long2ObjectMap<CompoundTag>> dataMap = Maps.newHashMap();
    private final Map<String, StructureFeatureIndexSavedData> indexMap = Maps.newHashMap();
    private final List<String> legacyKeys;
    private final List<String> currentKeys;

    public LegacyStructureDataHandler(@Nullable DimensionDataStorage dimensionDataStorage, List<String> list, List<String> list2) {
        this.legacyKeys = list;
        this.currentKeys = list2;
        this.populateCaches(dimensionDataStorage);
        boolean bl = false;
        for (String string : this.currentKeys) {
            bl |= this.dataMap.get(string) != null;
        }
        this.hasLegacyData = bl;
    }

    public void removeIndex(long l) {
        for (String string : this.legacyKeys) {
            StructureFeatureIndexSavedData structureFeatureIndexSavedData = this.indexMap.get(string);
            if (structureFeatureIndexSavedData == null || !structureFeatureIndexSavedData.hasUnhandledIndex(l)) continue;
            structureFeatureIndexSavedData.removeIndex(l);
        }
    }

    public CompoundTag updateFromLegacy(CompoundTag compoundTag) {
        CompoundTag compoundTag2 = compoundTag.getCompoundOrEmpty("Level");
        ChunkPos chunkPos = new ChunkPos(compoundTag2.getIntOr("xPos", 0), compoundTag2.getIntOr("zPos", 0));
        if (this.isUnhandledStructureStart(chunkPos.x, chunkPos.z)) {
            compoundTag = this.updateStructureStart(compoundTag, chunkPos);
        }
        CompoundTag compoundTag3 = compoundTag2.getCompoundOrEmpty("Structures");
        CompoundTag compoundTag4 = compoundTag3.getCompoundOrEmpty("References");
        for (String string : this.currentKeys) {
            boolean bl = OLD_STRUCTURE_REGISTRY_KEYS.contains(string.toLowerCase(Locale.ROOT));
            if (compoundTag4.getLongArray(string).isPresent() || !bl) continue;
            int n = 8;
            LongArrayList longArrayList = new LongArrayList();
            for (int i = chunkPos.x - 8; i <= chunkPos.x + 8; ++i) {
                for (int j = chunkPos.z - 8; j <= chunkPos.z + 8; ++j) {
                    if (!this.hasLegacyStart(i, j, string)) continue;
                    longArrayList.add(ChunkPos.asLong(i, j));
                }
            }
            compoundTag4.putLongArray(string, longArrayList.toLongArray());
        }
        compoundTag3.put("References", compoundTag4);
        compoundTag2.put("Structures", compoundTag3);
        compoundTag.put("Level", compoundTag2);
        return compoundTag;
    }

    private boolean hasLegacyStart(int n, int n2, String string) {
        if (!this.hasLegacyData) {
            return false;
        }
        return this.dataMap.get(string) != null && this.indexMap.get(CURRENT_TO_LEGACY_MAP.get(string)).hasStartIndex(ChunkPos.asLong(n, n2));
    }

    private boolean isUnhandledStructureStart(int n, int n2) {
        if (!this.hasLegacyData) {
            return false;
        }
        for (String string : this.currentKeys) {
            if (this.dataMap.get(string) == null || !this.indexMap.get(CURRENT_TO_LEGACY_MAP.get(string)).hasUnhandledIndex(ChunkPos.asLong(n, n2))) continue;
            return true;
        }
        return false;
    }

    private CompoundTag updateStructureStart(CompoundTag compoundTag, ChunkPos chunkPos) {
        CompoundTag compoundTag2 = compoundTag.getCompoundOrEmpty("Level");
        CompoundTag compoundTag3 = compoundTag2.getCompoundOrEmpty("Structures");
        CompoundTag compoundTag4 = compoundTag3.getCompoundOrEmpty("Starts");
        for (String string : this.currentKeys) {
            CompoundTag compoundTag5;
            Long2ObjectMap<CompoundTag> long2ObjectMap = this.dataMap.get(string);
            if (long2ObjectMap == null) continue;
            long l = chunkPos.toLong();
            if (!this.indexMap.get(CURRENT_TO_LEGACY_MAP.get(string)).hasUnhandledIndex(l) || (compoundTag5 = (CompoundTag)long2ObjectMap.get(l)) == null) continue;
            compoundTag4.put(string, compoundTag5);
        }
        compoundTag3.put("Starts", compoundTag4);
        compoundTag2.put("Structures", compoundTag3);
        compoundTag.put("Level", compoundTag2);
        return compoundTag;
    }

    private void populateCaches(@Nullable DimensionDataStorage dimensionDataStorage) {
        if (dimensionDataStorage == null) {
            return;
        }
        for (String string2 : this.legacyKeys) {
            CompoundTag compoundTag = new CompoundTag();
            try {
                compoundTag = dimensionDataStorage.readTagFromDisk(string2, DataFixTypes.SAVED_DATA_STRUCTURE_FEATURE_INDICES, 1493).getCompoundOrEmpty("data").getCompoundOrEmpty("Features");
                if (compoundTag.isEmpty()) {
                    continue;
                }
            }
            catch (IOException iOException) {
                // empty catch block
            }
            compoundTag.forEach((string3, tag) -> {
                if (!(tag instanceof CompoundTag)) {
                    return;
                }
                CompoundTag compoundTag2 = (CompoundTag)tag;
                long l = ChunkPos.asLong(compoundTag2.getIntOr("ChunkX", 0), compoundTag2.getIntOr("ChunkZ", 0));
                ListTag listTag = compoundTag2.getListOrEmpty("Children");
                if (!listTag.isEmpty()) {
                    Optional<String> optional = listTag.getCompound(0).flatMap(compoundTag -> compoundTag.getString("id"));
                    optional.map(LEGACY_TO_CURRENT_MAP::get).ifPresent(string -> compoundTag2.putString("id", (String)string));
                }
                compoundTag2.getString("id").ifPresent(string2 -> this.dataMap.computeIfAbsent((String)string2, string -> new Long2ObjectOpenHashMap()).put(l, (Object)compoundTag2));
            });
            String string4 = string2 + "_index";
            StructureFeatureIndexSavedData structureFeatureIndexSavedData = dimensionDataStorage.computeIfAbsent(StructureFeatureIndexSavedData.type(string4));
            if (structureFeatureIndexSavedData.getAll().isEmpty()) {
                StructureFeatureIndexSavedData structureFeatureIndexSavedData2 = new StructureFeatureIndexSavedData();
                this.indexMap.put(string2, structureFeatureIndexSavedData2);
                compoundTag.forEach((string, tag) -> {
                    if (tag instanceof CompoundTag) {
                        CompoundTag compoundTag = (CompoundTag)tag;
                        structureFeatureIndexSavedData2.addIndex(ChunkPos.asLong(compoundTag.getIntOr("ChunkX", 0), compoundTag.getIntOr("ChunkZ", 0)));
                    }
                });
                continue;
            }
            this.indexMap.put(string2, structureFeatureIndexSavedData);
        }
    }

    public static LegacyStructureDataHandler getLegacyStructureHandler(ResourceKey<Level> resourceKey, @Nullable DimensionDataStorage dimensionDataStorage) {
        if (resourceKey == Level.OVERWORLD) {
            return new LegacyStructureDataHandler(dimensionDataStorage, (List<String>)ImmutableList.of((Object)"Monument", (Object)"Stronghold", (Object)"Village", (Object)"Mineshaft", (Object)"Temple", (Object)"Mansion"), (List<String>)ImmutableList.of((Object)"Village", (Object)"Mineshaft", (Object)"Mansion", (Object)"Igloo", (Object)"Desert_Pyramid", (Object)"Jungle_Pyramid", (Object)"Swamp_Hut", (Object)"Stronghold", (Object)"Monument"));
        }
        if (resourceKey == Level.NETHER) {
            ImmutableList immutableList = ImmutableList.of((Object)"Fortress");
            return new LegacyStructureDataHandler(dimensionDataStorage, (List<String>)immutableList, (List<String>)immutableList);
        }
        if (resourceKey == Level.END) {
            ImmutableList immutableList = ImmutableList.of((Object)"EndCity");
            return new LegacyStructureDataHandler(dimensionDataStorage, (List<String>)immutableList, (List<String>)immutableList);
        }
        throw new RuntimeException(String.format(Locale.ROOT, "Unknown dimension type : %s", resourceKey));
    }
}

