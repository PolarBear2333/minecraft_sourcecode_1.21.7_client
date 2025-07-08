/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 */
package net.minecraft.client.resources;

import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class MapTextureManager
implements AutoCloseable {
    private final Int2ObjectMap<MapInstance> maps = new Int2ObjectOpenHashMap();
    final TextureManager textureManager;

    public MapTextureManager(TextureManager textureManager) {
        this.textureManager = textureManager;
    }

    public void update(MapId mapId, MapItemSavedData mapItemSavedData) {
        this.getOrCreateMapInstance(mapId, mapItemSavedData).forceUpload();
    }

    public ResourceLocation prepareMapTexture(MapId mapId, MapItemSavedData mapItemSavedData) {
        MapInstance mapInstance = this.getOrCreateMapInstance(mapId, mapItemSavedData);
        mapInstance.updateTextureIfNeeded();
        return mapInstance.location;
    }

    public void resetData() {
        for (MapInstance mapInstance : this.maps.values()) {
            mapInstance.close();
        }
        this.maps.clear();
    }

    private MapInstance getOrCreateMapInstance(MapId mapId, MapItemSavedData mapItemSavedData) {
        return (MapInstance)this.maps.compute(mapId.id(), (n, mapInstance) -> {
            if (mapInstance == null) {
                return new MapInstance(this, (int)n, mapItemSavedData);
            }
            mapInstance.replaceMapData(mapItemSavedData);
            return mapInstance;
        });
    }

    @Override
    public void close() {
        this.resetData();
    }

    class MapInstance
    implements AutoCloseable {
        private MapItemSavedData data;
        private final DynamicTexture texture;
        private boolean requiresUpload = true;
        final ResourceLocation location;

        MapInstance(MapTextureManager mapTextureManager, int n, MapItemSavedData mapItemSavedData) {
            this.data = mapItemSavedData;
            this.texture = new DynamicTexture(() -> "Map " + n, 128, 128, true);
            this.location = ResourceLocation.withDefaultNamespace("map/" + n);
            mapTextureManager.textureManager.register(this.location, this.texture);
        }

        void replaceMapData(MapItemSavedData mapItemSavedData) {
            boolean bl = this.data != mapItemSavedData;
            this.data = mapItemSavedData;
            this.requiresUpload |= bl;
        }

        public void forceUpload() {
            this.requiresUpload = true;
        }

        void updateTextureIfNeeded() {
            if (this.requiresUpload) {
                NativeImage nativeImage = this.texture.getPixels();
                if (nativeImage != null) {
                    for (int i = 0; i < 128; ++i) {
                        for (int j = 0; j < 128; ++j) {
                            int n = j + i * 128;
                            nativeImage.setPixel(j, i, MapColor.getColorFromPackedId(this.data.colors[n]));
                        }
                    }
                }
                this.texture.upload();
                this.requiresUpload = false;
            }
        }

        @Override
        public void close() {
            this.texture.close();
        }
    }
}

