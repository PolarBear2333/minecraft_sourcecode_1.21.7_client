/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.resources;

import java.io.IOException;
import net.minecraft.client.resources.LegacyStuffWrapper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.DryFoliageColor;

public class DryFoliageColorReloadListener
extends SimplePreparableReloadListener<int[]> {
    private static final ResourceLocation LOCATION = ResourceLocation.withDefaultNamespace("textures/colormap/dry_foliage.png");

    @Override
    protected int[] prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        try {
            return LegacyStuffWrapper.getPixels(resourceManager, LOCATION);
        }
        catch (IOException iOException) {
            throw new IllegalStateException("Failed to load dry foliage color texture", iOException);
        }
    }

    @Override
    protected void apply(int[] nArray, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        DryFoliageColor.init(nArray);
    }

    @Override
    protected /* synthetic */ Object prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        return this.prepare(resourceManager, profilerFiller);
    }
}

