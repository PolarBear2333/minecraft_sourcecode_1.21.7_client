/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.chunk;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.IOException;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.lighting.LevelLightEngine;

public abstract class ChunkSource
implements LightChunkGetter,
AutoCloseable {
    @Nullable
    public LevelChunk getChunk(int n, int n2, boolean bl) {
        return (LevelChunk)this.getChunk(n, n2, ChunkStatus.FULL, bl);
    }

    @Nullable
    public LevelChunk getChunkNow(int n, int n2) {
        return this.getChunk(n, n2, false);
    }

    @Override
    @Nullable
    public LightChunk getChunkForLighting(int n, int n2) {
        return this.getChunk(n, n2, ChunkStatus.EMPTY, false);
    }

    public boolean hasChunk(int n, int n2) {
        return this.getChunk(n, n2, ChunkStatus.FULL, false) != null;
    }

    @Nullable
    public abstract ChunkAccess getChunk(int var1, int var2, ChunkStatus var3, boolean var4);

    public abstract void tick(BooleanSupplier var1, boolean var2);

    public void onSectionEmptinessChanged(int n, int n2, int n3, boolean bl) {
    }

    public abstract String gatherStats();

    public abstract int getLoadedChunksCount();

    @Override
    public void close() throws IOException {
    }

    public abstract LevelLightEngine getLightEngine();

    public void setSpawnSettings(boolean bl) {
    }

    public boolean updateChunkForced(ChunkPos chunkPos, boolean bl) {
        return false;
    }

    public LongSet getForceLoadedChunks() {
        return LongSet.of();
    }
}

