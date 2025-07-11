/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.chunk;

import javax.annotation.Nullable;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.LightChunk;

public interface LightChunkGetter {
    @Nullable
    public LightChunk getChunkForLighting(int var1, int var2);

    default public void onLightUpdate(LightLayer lightLayer, SectionPos sectionPos) {
    }

    public BlockGetter getLevel();
}

