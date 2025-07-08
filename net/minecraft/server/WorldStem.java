/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server;

import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.world.level.storage.WorldData;

public record WorldStem(CloseableResourceManager resourceManager, ReloadableServerResources dataPackResources, LayeredRegistryAccess<RegistryLayer> registries, WorldData worldData) implements AutoCloseable
{
    @Override
    public void close() {
        this.resourceManager.close();
    }
}

