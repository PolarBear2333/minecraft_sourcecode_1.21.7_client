/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterables
 *  com.mojang.datafixers.DataFixer
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.storage;

import com.google.common.collect.Iterables;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.FastBufferedInputStream;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.slf4j.Logger;

public class DimensionDataStorage
implements AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final SavedData.Context context;
    private final Map<SavedDataType<?>, Optional<SavedData>> cache = new HashMap();
    private final DataFixer fixerUpper;
    private final HolderLookup.Provider registries;
    private final Path dataFolder;
    private CompletableFuture<?> pendingWriteFuture = CompletableFuture.completedFuture(null);

    public DimensionDataStorage(SavedData.Context context, Path path, DataFixer dataFixer, HolderLookup.Provider provider) {
        this.context = context;
        this.fixerUpper = dataFixer;
        this.dataFolder = path;
        this.registries = provider;
    }

    private Path getDataFile(String string) {
        return this.dataFolder.resolve(string + ".dat");
    }

    public <T extends SavedData> T computeIfAbsent(SavedDataType<T> savedDataType) {
        T t = this.get(savedDataType);
        if (t != null) {
            return t;
        }
        SavedData savedData = (SavedData)savedDataType.constructor().apply(this.context);
        this.set(savedDataType, savedData);
        return (T)savedData;
    }

    @Nullable
    public <T extends SavedData> T get(SavedDataType<T> savedDataType) {
        Optional<SavedData> optional = this.cache.get(savedDataType);
        if (optional == null) {
            optional = Optional.ofNullable(this.readSavedData(savedDataType));
            this.cache.put(savedDataType, optional);
        }
        return (T)((SavedData)optional.orElse(null));
    }

    @Nullable
    private <T extends SavedData> T readSavedData(SavedDataType<T> savedDataType) {
        try {
            Path path = this.getDataFile(savedDataType.id());
            if (Files.exists(path, new LinkOption[0])) {
                CompoundTag compoundTag = this.readTagFromDisk(savedDataType.id(), savedDataType.dataFixType(), SharedConstants.getCurrentVersion().dataVersion().version());
                RegistryOps<Tag> registryOps = this.registries.createSerializationContext(NbtOps.INSTANCE);
                return (T)((SavedData)savedDataType.codec().apply(this.context).parse(registryOps, (Object)compoundTag.get("data")).resultOrPartial(string -> LOGGER.error("Failed to parse saved data for '{}': {}", (Object)savedDataType, string)).orElse(null));
            }
        }
        catch (Exception exception) {
            LOGGER.error("Error loading saved data: {}", savedDataType, (Object)exception);
        }
        return null;
    }

    public <T extends SavedData> void set(SavedDataType<T> savedDataType, T t) {
        this.cache.put(savedDataType, Optional.of(t));
        t.setDirty();
    }

    public CompoundTag readTagFromDisk(String string, DataFixTypes dataFixTypes, int n) throws IOException {
        try (InputStream inputStream = Files.newInputStream(this.getDataFile(string), new OpenOption[0]);){
            CompoundTag compoundTag;
            try (PushbackInputStream pushbackInputStream = new PushbackInputStream(new FastBufferedInputStream(inputStream), 2);){
                CompoundTag compoundTag2;
                if (this.isGzip(pushbackInputStream)) {
                    compoundTag2 = NbtIo.readCompressed(pushbackInputStream, NbtAccounter.unlimitedHeap());
                } else {
                    try (DataInputStream dataInputStream = new DataInputStream(pushbackInputStream);){
                        compoundTag2 = NbtIo.read(dataInputStream);
                    }
                }
                int n2 = NbtUtils.getDataVersion(compoundTag2, 1343);
                compoundTag = dataFixTypes.update(this.fixerUpper, compoundTag2, n2, n);
            }
            return compoundTag;
        }
    }

    private boolean isGzip(PushbackInputStream pushbackInputStream) throws IOException {
        int n;
        byte[] byArray = new byte[2];
        boolean bl = false;
        int n2 = pushbackInputStream.read(byArray, 0, 2);
        if (n2 == 2 && (n = (byArray[1] & 0xFF) << 8 | byArray[0] & 0xFF) == 35615) {
            bl = true;
        }
        if (n2 != 0) {
            pushbackInputStream.unread(byArray, 0, n2);
        }
        return bl;
    }

    public CompletableFuture<?> scheduleSave() {
        Map<SavedDataType<?>, CompoundTag> map = this.collectDirtyTagsToSave();
        if (map.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        int n = Util.maxAllowedExecutorThreads();
        int n2 = map.size();
        this.pendingWriteFuture = n2 > n ? this.pendingWriteFuture.thenCompose(object -> {
            ArrayList<CompletableFuture<Void>> arrayList = new ArrayList<CompletableFuture<Void>>(n);
            int n3 = Mth.positiveCeilDiv(n2, n);
            for (List list : Iterables.partition(map.entrySet(), (int)n3)) {
                arrayList.add(CompletableFuture.runAsync(() -> {
                    for (Map.Entry entry : list) {
                        this.tryWrite((SavedDataType)entry.getKey(), (CompoundTag)entry.getValue());
                    }
                }, Util.ioPool()));
            }
            return CompletableFuture.allOf((CompletableFuture[])arrayList.toArray(CompletableFuture[]::new));
        }) : this.pendingWriteFuture.thenCompose(object -> CompletableFuture.allOf((CompletableFuture[])map.entrySet().stream().map(entry -> CompletableFuture.runAsync(() -> this.tryWrite((SavedDataType)entry.getKey(), (CompoundTag)entry.getValue()), Util.ioPool())).toArray(CompletableFuture[]::new)));
        return this.pendingWriteFuture;
    }

    private Map<SavedDataType<?>, CompoundTag> collectDirtyTagsToSave() {
        Object2ObjectArrayMap object2ObjectArrayMap = new Object2ObjectArrayMap();
        RegistryOps<Tag> registryOps = this.registries.createSerializationContext(NbtOps.INSTANCE);
        this.cache.forEach((arg_0, arg_1) -> this.lambda$collectDirtyTagsToSave$9((Map)object2ObjectArrayMap, registryOps, arg_0, arg_1));
        return object2ObjectArrayMap;
    }

    private <T extends SavedData> CompoundTag encodeUnchecked(SavedDataType<T> savedDataType, SavedData savedData, RegistryOps<Tag> registryOps) {
        Codec<T> codec = savedDataType.codec().apply(this.context);
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.put("data", (Tag)codec.encodeStart(registryOps, (Object)savedData).getOrThrow());
        NbtUtils.addCurrentDataVersion(compoundTag);
        return compoundTag;
    }

    private void tryWrite(SavedDataType<?> savedDataType, CompoundTag compoundTag) {
        Path path = this.getDataFile(savedDataType.id());
        try {
            NbtIo.writeCompressed(compoundTag, path);
        }
        catch (IOException iOException) {
            LOGGER.error("Could not save data to {}", (Object)path.getFileName(), (Object)iOException);
        }
    }

    public void saveAndJoin() {
        this.scheduleSave().join();
    }

    @Override
    public void close() {
        this.saveAndJoin();
    }

    private /* synthetic */ void lambda$collectDirtyTagsToSave$9(Map map, RegistryOps registryOps, SavedDataType savedDataType, Optional optional) {
        optional.filter(SavedData::isDirty).ifPresent(savedData -> {
            map.put(savedDataType, this.encodeUnchecked(savedDataType, (SavedData)savedData, registryOps));
            savedData.setDirty(false);
        });
    }
}

