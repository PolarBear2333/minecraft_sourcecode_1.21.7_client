/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.data.info;

import com.google.gson.JsonElement;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

public class DatapackStructureReport
implements DataProvider {
    private final PackOutput output;
    private static final Entry PSEUDO_REGISTRY = new Entry(true, false, true);
    private static final Entry STABLE_DYNAMIC_REGISTRY = new Entry(true, true, true);
    private static final Entry UNSTABLE_DYNAMIC_REGISTRY = new Entry(true, true, false);
    private static final Entry BUILT_IN_REGISTRY = new Entry(false, true, true);
    private static final Map<ResourceKey<? extends Registry<?>>, Entry> MANUAL_ENTRIES = Map.of(Registries.RECIPE, PSEUDO_REGISTRY, Registries.ADVANCEMENT, PSEUDO_REGISTRY, Registries.LOOT_TABLE, STABLE_DYNAMIC_REGISTRY, Registries.ITEM_MODIFIER, STABLE_DYNAMIC_REGISTRY, Registries.PREDICATE, STABLE_DYNAMIC_REGISTRY);
    private static final Map<String, CustomPackEntry> NON_REGISTRY_ENTRIES = Map.of("structure", new CustomPackEntry(Format.STRUCTURE, new Entry(true, false, true)), "function", new CustomPackEntry(Format.MCFUNCTION, new Entry(true, true, true)));
    static final Codec<ResourceKey<? extends Registry<?>>> REGISTRY_KEY_CODEC = ResourceLocation.CODEC.xmap(ResourceKey::createRegistryKey, ResourceKey::location);

    public DatapackStructureReport(PackOutput packOutput) {
        this.output = packOutput;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        Report report = new Report(this.listRegistries(), NON_REGISTRY_ENTRIES);
        Path path = this.output.getOutputFolder(PackOutput.Target.REPORTS).resolve("datapack.json");
        return DataProvider.saveStable(cachedOutput, (JsonElement)Report.CODEC.encodeStart((DynamicOps)JsonOps.INSTANCE, (Object)report).getOrThrow(), path);
    }

    @Override
    public String getName() {
        return "Datapack Structure";
    }

    private void putIfNotPresent(Map<ResourceKey<? extends Registry<?>>, Entry> map, ResourceKey<? extends Registry<?>> resourceKey, Entry entry) {
        Entry entry2 = map.putIfAbsent(resourceKey, entry);
        if (entry2 != null) {
            throw new IllegalStateException("Duplicate entry for key " + String.valueOf(resourceKey.location()));
        }
    }

    private Map<ResourceKey<? extends Registry<?>>, Entry> listRegistries() {
        HashMap hashMap = new HashMap();
        BuiltInRegistries.REGISTRY.forEach(registry -> this.putIfNotPresent(hashMap, registry.key(), BUILT_IN_REGISTRY));
        RegistryDataLoader.WORLDGEN_REGISTRIES.forEach(registryData -> this.putIfNotPresent(hashMap, registryData.key(), UNSTABLE_DYNAMIC_REGISTRY));
        RegistryDataLoader.DIMENSION_REGISTRIES.forEach(registryData -> this.putIfNotPresent(hashMap, registryData.key(), UNSTABLE_DYNAMIC_REGISTRY));
        MANUAL_ENTRIES.forEach((resourceKey, entry) -> this.putIfNotPresent(hashMap, (ResourceKey<? extends Registry<?>>)resourceKey, (Entry)entry));
        return hashMap;
    }

    record Report(Map<ResourceKey<? extends Registry<?>>, Entry> registries, Map<String, CustomPackEntry> others) {
        public static final Codec<Report> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.unboundedMap(REGISTRY_KEY_CODEC, Entry.CODEC).fieldOf("registries").forGetter(Report::registries), (App)Codec.unboundedMap((Codec)Codec.STRING, CustomPackEntry.CODEC).fieldOf("others").forGetter(Report::others)).apply((Applicative)instance, Report::new));
    }

    record Entry(boolean elements, boolean tags, boolean stable) {
        public static final MapCodec<Entry> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.BOOL.fieldOf("elements").forGetter(Entry::elements), (App)Codec.BOOL.fieldOf("tags").forGetter(Entry::tags), (App)Codec.BOOL.fieldOf("stable").forGetter(Entry::stable)).apply((Applicative)instance, Entry::new));
        public static final Codec<Entry> CODEC = MAP_CODEC.codec();
    }

    record CustomPackEntry(Format format, Entry entry) {
        public static final Codec<CustomPackEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Format.CODEC.fieldOf("format").forGetter(CustomPackEntry::format), (App)Entry.MAP_CODEC.forGetter(CustomPackEntry::entry)).apply((Applicative)instance, CustomPackEntry::new));
    }

    static enum Format implements StringRepresentable
    {
        STRUCTURE("structure"),
        MCFUNCTION("mcfunction");

        public static final Codec<Format> CODEC;
        private final String name;

        private Format(String string2) {
            this.name = string2;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        static {
            CODEC = StringRepresentable.fromEnum(Format::values);
        }
    }
}

