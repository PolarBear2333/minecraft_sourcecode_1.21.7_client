/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.ResourceHandle;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;

public class LevelTargetBundle
implements PostChain.TargetBundle {
    public static final ResourceLocation MAIN_TARGET_ID = PostChain.MAIN_TARGET_ID;
    public static final ResourceLocation TRANSLUCENT_TARGET_ID = ResourceLocation.withDefaultNamespace("translucent");
    public static final ResourceLocation ITEM_ENTITY_TARGET_ID = ResourceLocation.withDefaultNamespace("item_entity");
    public static final ResourceLocation PARTICLES_TARGET_ID = ResourceLocation.withDefaultNamespace("particles");
    public static final ResourceLocation WEATHER_TARGET_ID = ResourceLocation.withDefaultNamespace("weather");
    public static final ResourceLocation CLOUDS_TARGET_ID = ResourceLocation.withDefaultNamespace("clouds");
    public static final ResourceLocation ENTITY_OUTLINE_TARGET_ID = ResourceLocation.withDefaultNamespace("entity_outline");
    public static final Set<ResourceLocation> MAIN_TARGETS = Set.of(MAIN_TARGET_ID);
    public static final Set<ResourceLocation> OUTLINE_TARGETS = Set.of(MAIN_TARGET_ID, ENTITY_OUTLINE_TARGET_ID);
    public static final Set<ResourceLocation> SORTING_TARGETS = Set.of(MAIN_TARGET_ID, TRANSLUCENT_TARGET_ID, ITEM_ENTITY_TARGET_ID, PARTICLES_TARGET_ID, WEATHER_TARGET_ID, CLOUDS_TARGET_ID);
    public ResourceHandle<RenderTarget> main = ResourceHandle.invalid();
    @Nullable
    public ResourceHandle<RenderTarget> translucent;
    @Nullable
    public ResourceHandle<RenderTarget> itemEntity;
    @Nullable
    public ResourceHandle<RenderTarget> particles;
    @Nullable
    public ResourceHandle<RenderTarget> weather;
    @Nullable
    public ResourceHandle<RenderTarget> clouds;
    @Nullable
    public ResourceHandle<RenderTarget> entityOutline;

    @Override
    public void replace(ResourceLocation resourceLocation, ResourceHandle<RenderTarget> resourceHandle) {
        if (resourceLocation.equals(MAIN_TARGET_ID)) {
            this.main = resourceHandle;
        } else if (resourceLocation.equals(TRANSLUCENT_TARGET_ID)) {
            this.translucent = resourceHandle;
        } else if (resourceLocation.equals(ITEM_ENTITY_TARGET_ID)) {
            this.itemEntity = resourceHandle;
        } else if (resourceLocation.equals(PARTICLES_TARGET_ID)) {
            this.particles = resourceHandle;
        } else if (resourceLocation.equals(WEATHER_TARGET_ID)) {
            this.weather = resourceHandle;
        } else if (resourceLocation.equals(CLOUDS_TARGET_ID)) {
            this.clouds = resourceHandle;
        } else if (resourceLocation.equals(ENTITY_OUTLINE_TARGET_ID)) {
            this.entityOutline = resourceHandle;
        } else {
            throw new IllegalArgumentException("No target with id " + String.valueOf(resourceLocation));
        }
    }

    @Override
    @Nullable
    public ResourceHandle<RenderTarget> get(ResourceLocation resourceLocation) {
        if (resourceLocation.equals(MAIN_TARGET_ID)) {
            return this.main;
        }
        if (resourceLocation.equals(TRANSLUCENT_TARGET_ID)) {
            return this.translucent;
        }
        if (resourceLocation.equals(ITEM_ENTITY_TARGET_ID)) {
            return this.itemEntity;
        }
        if (resourceLocation.equals(PARTICLES_TARGET_ID)) {
            return this.particles;
        }
        if (resourceLocation.equals(WEATHER_TARGET_ID)) {
            return this.weather;
        }
        if (resourceLocation.equals(CLOUDS_TARGET_ID)) {
            return this.clouds;
        }
        if (resourceLocation.equals(ENTITY_OUTLINE_TARGET_ID)) {
            return this.entityOutline;
        }
        return null;
    }

    public void clear() {
        this.main = ResourceHandle.invalid();
        this.translucent = null;
        this.itemEntity = null;
        this.particles = null;
        this.weather = null;
        this.clouds = null;
        this.entityOutline = null;
    }
}

