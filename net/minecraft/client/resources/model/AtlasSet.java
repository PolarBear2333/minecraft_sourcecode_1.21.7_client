/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.resources.model;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

public class AtlasSet
implements AutoCloseable {
    private final Map<ResourceLocation, AtlasEntry> atlases;

    public AtlasSet(Map<ResourceLocation, ResourceLocation> map, TextureManager textureManager) {
        this.atlases = map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {
            TextureAtlas textureAtlas = new TextureAtlas((ResourceLocation)entry.getKey());
            textureManager.register((ResourceLocation)entry.getKey(), textureAtlas);
            return new AtlasEntry(textureAtlas, (ResourceLocation)entry.getValue());
        }));
    }

    public TextureAtlas getAtlas(ResourceLocation resourceLocation) {
        return this.atlases.get(resourceLocation).atlas();
    }

    @Override
    public void close() {
        this.atlases.values().forEach(AtlasEntry::close);
        this.atlases.clear();
    }

    public Map<ResourceLocation, CompletableFuture<StitchResult>> scheduleLoad(ResourceManager resourceManager, int n, Executor executor) {
        return Util.mapValues(this.atlases, atlasEntry -> SpriteLoader.create(atlasEntry.atlas).loadAndStitch(resourceManager, atlasEntry.atlasInfoLocation, n, executor).thenApply(preparations -> new StitchResult(atlasEntry.atlas, (SpriteLoader.Preparations)preparations)));
    }

    static final class AtlasEntry
    extends Record
    implements AutoCloseable {
        final TextureAtlas atlas;
        final ResourceLocation atlasInfoLocation;

        AtlasEntry(TextureAtlas textureAtlas, ResourceLocation resourceLocation) {
            this.atlas = textureAtlas;
            this.atlasInfoLocation = resourceLocation;
        }

        @Override
        public void close() {
            this.atlas.clearTextureData();
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{AtlasEntry.class, "atlas;atlasInfoLocation", "atlas", "atlasInfoLocation"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{AtlasEntry.class, "atlas;atlasInfoLocation", "atlas", "atlasInfoLocation"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{AtlasEntry.class, "atlas;atlasInfoLocation", "atlas", "atlasInfoLocation"}, this, object);
        }

        public TextureAtlas atlas() {
            return this.atlas;
        }

        public ResourceLocation atlasInfoLocation() {
            return this.atlasInfoLocation;
        }
    }

    public static class StitchResult {
        private final TextureAtlas atlas;
        private final SpriteLoader.Preparations preparations;

        public StitchResult(TextureAtlas textureAtlas, SpriteLoader.Preparations preparations) {
            this.atlas = textureAtlas;
            this.preparations = preparations;
        }

        @Nullable
        public TextureAtlasSprite getSprite(ResourceLocation resourceLocation) {
            return this.preparations.regions().get(resourceLocation);
        }

        public TextureAtlasSprite missing() {
            return this.preparations.missing();
        }

        public CompletableFuture<Void> readyForUpload() {
            return this.preparations.readyForUpload();
        }

        public void upload() {
            this.atlas.upload(this.preparations);
        }
    }
}

