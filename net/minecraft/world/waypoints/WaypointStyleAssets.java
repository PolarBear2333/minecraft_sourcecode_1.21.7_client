/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.waypoints;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.waypoints.WaypointStyleAsset;

public interface WaypointStyleAssets {
    public static final ResourceKey<? extends Registry<WaypointStyleAsset>> ROOT_ID = ResourceKey.createRegistryKey(ResourceLocation.withDefaultNamespace("waypoint_style_asset"));
    public static final ResourceKey<WaypointStyleAsset> DEFAULT = WaypointStyleAssets.createId("default");
    public static final ResourceKey<WaypointStyleAsset> BOWTIE = WaypointStyleAssets.createId("bowtie");

    public static ResourceKey<WaypointStyleAsset> createId(String string) {
        return ResourceKey.create(ROOT_ID, ResourceLocation.withDefaultNamespace(string));
    }
}

