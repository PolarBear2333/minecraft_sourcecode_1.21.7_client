/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens.worldselection;

import java.nio.file.Path;
import javax.annotation.Nullable;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.server.RegistryLayer;
import net.minecraft.world.level.storage.PrimaryLevelData;

@FunctionalInterface
public interface CreateWorldCallback {
    public boolean create(CreateWorldScreen var1, LayeredRegistryAccess<RegistryLayer> var2, PrimaryLevelData var3, @Nullable Path var4);
}

