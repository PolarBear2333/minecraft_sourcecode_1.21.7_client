/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.resources;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.resources.WaypointStyle;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.waypoints.WaypointStyleAsset;
import net.minecraft.world.waypoints.WaypointStyleAssets;

public class WaypointStyleManager
extends SimpleJsonResourceReloadListener<WaypointStyle> {
    private static final FileToIdConverter ASSET_LISTER = FileToIdConverter.json("waypoint_style");
    private static final WaypointStyle MISSING = new WaypointStyle(0, 1, List.of(MissingTextureAtlasSprite.getLocation()));
    private Map<ResourceKey<WaypointStyleAsset>, WaypointStyle> waypointStyles = Map.of();

    public WaypointStyleManager() {
        super(WaypointStyle.CODEC, ASSET_LISTER);
    }

    @Override
    protected void apply(Map<ResourceLocation, WaypointStyle> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        this.waypointStyles = map.entrySet().stream().collect(Collectors.toUnmodifiableMap(entry -> ResourceKey.create(WaypointStyleAssets.ROOT_ID, (ResourceLocation)entry.getKey()), Map.Entry::getValue));
    }

    public WaypointStyle get(ResourceKey<WaypointStyleAsset> resourceKey) {
        return this.waypointStyles.getOrDefault(resourceKey, MISSING);
    }
}

