/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 */
package net.minecraft.server.packs.resources;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Optional;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.util.GsonHelper;

public interface ResourceMetadata {
    public static final ResourceMetadata EMPTY = new ResourceMetadata(){

        @Override
        public <T> Optional<T> getSection(MetadataSectionType<T> metadataSectionType) {
            return Optional.empty();
        }
    };
    public static final IoSupplier<ResourceMetadata> EMPTY_SUPPLIER = () -> EMPTY;

    public static ResourceMetadata fromJsonStream(InputStream inputStream) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));){
            final JsonObject jsonObject = GsonHelper.parse(bufferedReader);
            ResourceMetadata resourceMetadata = new ResourceMetadata(){

                @Override
                public <T> Optional<T> getSection(MetadataSectionType<T> metadataSectionType) {
                    String string = metadataSectionType.name();
                    if (jsonObject.has(string)) {
                        Object object = metadataSectionType.codec().parse((DynamicOps)JsonOps.INSTANCE, (Object)jsonObject.get(string)).getOrThrow(JsonParseException::new);
                        return Optional.of(object);
                    }
                    return Optional.empty();
                }
            };
            return resourceMetadata;
        }
    }

    public <T> Optional<T> getSection(MetadataSectionType<T> var1);

    default public ResourceMetadata copySections(Collection<MetadataSectionType<?>> collection) {
        Builder builder = new Builder();
        for (MetadataSectionType<?> metadataSectionType : collection) {
            this.copySection(builder, metadataSectionType);
        }
        return builder.build();
    }

    private <T> void copySection(Builder builder, MetadataSectionType<T> metadataSectionType) {
        this.getSection(metadataSectionType).ifPresent(object -> builder.put(metadataSectionType, object));
    }

    public static class Builder {
        private final ImmutableMap.Builder<MetadataSectionType<?>, Object> map = ImmutableMap.builder();

        public <T> Builder put(MetadataSectionType<T> metadataSectionType, T t) {
            this.map.put(metadataSectionType, t);
            return this;
        }

        public ResourceMetadata build() {
            final ImmutableMap immutableMap = this.map.build();
            if (immutableMap.isEmpty()) {
                return EMPTY;
            }
            return new ResourceMetadata(){

                @Override
                public <T> Optional<T> getSection(MetadataSectionType<T> metadataSectionType) {
                    return Optional.ofNullable(immutableMap.get(metadataSectionType));
                }
            };
        }
    }
}

