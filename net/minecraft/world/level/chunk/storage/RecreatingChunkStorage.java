/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DataFixer
 *  org.apache.commons.io.FileUtils
 */
package net.minecraft.world.level.chunk.storage;

import com.mojang.datafixers.DataFixer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.chunk.storage.IOWorker;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import org.apache.commons.io.FileUtils;

public class RecreatingChunkStorage
extends ChunkStorage {
    private final IOWorker writeWorker;
    private final Path writeFolder;

    public RecreatingChunkStorage(RegionStorageInfo regionStorageInfo, Path path, RegionStorageInfo regionStorageInfo2, Path path2, DataFixer dataFixer, boolean bl) {
        super(regionStorageInfo, path, dataFixer, bl);
        this.writeFolder = path2;
        this.writeWorker = new IOWorker(regionStorageInfo2, path2, bl);
    }

    @Override
    public CompletableFuture<Void> write(ChunkPos chunkPos, Supplier<CompoundTag> supplier) {
        this.handleLegacyStructureIndex(chunkPos);
        return this.writeWorker.store(chunkPos, supplier);
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.writeWorker.close();
        if (this.writeFolder.toFile().exists()) {
            FileUtils.deleteDirectory((File)this.writeFolder.toFile());
        }
    }
}

