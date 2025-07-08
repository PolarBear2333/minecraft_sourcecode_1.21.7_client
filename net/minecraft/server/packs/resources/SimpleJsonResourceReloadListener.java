/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonParseException
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  org.slf4j.Logger
 */
package net.minecraft.server.packs.resources;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.StrictJsonParser;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

public abstract class SimpleJsonResourceReloadListener<T>
extends SimplePreparableReloadListener<Map<ResourceLocation, T>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final DynamicOps<JsonElement> ops;
    private final Codec<T> codec;
    private final FileToIdConverter lister;

    protected SimpleJsonResourceReloadListener(HolderLookup.Provider provider, Codec<T> codec, ResourceKey<? extends Registry<T>> resourceKey) {
        this(provider.createSerializationContext(JsonOps.INSTANCE), codec, FileToIdConverter.registry(resourceKey));
    }

    protected SimpleJsonResourceReloadListener(Codec<T> codec, FileToIdConverter fileToIdConverter) {
        this((DynamicOps<JsonElement>)JsonOps.INSTANCE, codec, fileToIdConverter);
    }

    private SimpleJsonResourceReloadListener(DynamicOps<JsonElement> dynamicOps, Codec<T> codec, FileToIdConverter fileToIdConverter) {
        this.ops = dynamicOps;
        this.codec = codec;
        this.lister = fileToIdConverter;
    }

    @Override
    protected Map<ResourceLocation, T> prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        HashMap hashMap = new HashMap();
        SimpleJsonResourceReloadListener.scanDirectory(resourceManager, this.lister, this.ops, this.codec, hashMap);
        return hashMap;
    }

    public static <T> void scanDirectory(ResourceManager resourceManager, ResourceKey<? extends Registry<T>> resourceKey, DynamicOps<JsonElement> dynamicOps, Codec<T> codec, Map<ResourceLocation, T> map) {
        SimpleJsonResourceReloadListener.scanDirectory(resourceManager, FileToIdConverter.registry(resourceKey), dynamicOps, codec, map);
    }

    public static <T> void scanDirectory(ResourceManager resourceManager, FileToIdConverter fileToIdConverter, DynamicOps<JsonElement> dynamicOps, Codec<T> codec, Map<ResourceLocation, T> map) {
        for (Map.Entry<ResourceLocation, Resource> entry : fileToIdConverter.listMatchingResources(resourceManager).entrySet()) {
            ResourceLocation resourceLocation = entry.getKey();
            ResourceLocation resourceLocation2 = fileToIdConverter.fileToId(resourceLocation);
            try {
                BufferedReader bufferedReader = entry.getValue().openAsReader();
                try {
                    codec.parse(dynamicOps, (Object)StrictJsonParser.parse(bufferedReader)).ifSuccess(object -> {
                        if (map.putIfAbsent(resourceLocation2, object) != null) {
                            throw new IllegalStateException("Duplicate data file ignored with ID " + String.valueOf(resourceLocation2));
                        }
                    }).ifError(error -> LOGGER.error("Couldn't parse data file '{}' from '{}': {}", new Object[]{resourceLocation2, resourceLocation, error}));
                }
                finally {
                    if (bufferedReader == null) continue;
                    ((Reader)bufferedReader).close();
                }
            }
            catch (JsonParseException | IOException | IllegalArgumentException throwable) {
                LOGGER.error("Couldn't parse data file '{}' from '{}'", new Object[]{resourceLocation2, resourceLocation, throwable});
            }
        }
    }

    @Override
    protected /* synthetic */ Object prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        return this.prepare(resourceManager, profilerFiller);
    }
}

