/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DataFixer
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  org.slf4j.Logger
 */
package net.minecraft.client;

import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.nio.file.Path;
import net.minecraft.client.player.inventory.Hotbar;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.util.datafix.DataFixTypes;
import org.slf4j.Logger;

public class HotbarManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int NUM_HOTBAR_GROUPS = 9;
    private final Path optionsFile;
    private final DataFixer fixerUpper;
    private final Hotbar[] hotbars = new Hotbar[9];
    private boolean loaded;

    public HotbarManager(Path path, DataFixer dataFixer) {
        this.optionsFile = path.resolve("hotbar.nbt");
        this.fixerUpper = dataFixer;
        for (int i = 0; i < 9; ++i) {
            this.hotbars[i] = new Hotbar();
        }
    }

    private void load() {
        try {
            CompoundTag compoundTag = NbtIo.read(this.optionsFile);
            if (compoundTag == null) {
                return;
            }
            int n = NbtUtils.getDataVersion(compoundTag, 1343);
            compoundTag = DataFixTypes.HOTBAR.updateToCurrentVersion(this.fixerUpper, compoundTag, n);
            for (int i = 0; i < 9; ++i) {
                this.hotbars[i] = Hotbar.CODEC.parse((DynamicOps)NbtOps.INSTANCE, (Object)compoundTag.get(String.valueOf(i))).resultOrPartial(string -> LOGGER.warn("Failed to parse hotbar: {}", string)).orElseGet(Hotbar::new);
            }
        }
        catch (Exception exception) {
            LOGGER.error("Failed to load creative mode options", (Throwable)exception);
        }
    }

    public void save() {
        try {
            CompoundTag compoundTag = NbtUtils.addCurrentDataVersion(new CompoundTag());
            for (int i = 0; i < 9; ++i) {
                Hotbar hotbar = this.get(i);
                DataResult dataResult = Hotbar.CODEC.encodeStart((DynamicOps)NbtOps.INSTANCE, (Object)hotbar);
                compoundTag.put(String.valueOf(i), (Tag)dataResult.getOrThrow());
            }
            NbtIo.write(compoundTag, this.optionsFile);
        }
        catch (Exception exception) {
            LOGGER.error("Failed to save creative mode options", (Throwable)exception);
        }
    }

    public Hotbar get(int n) {
        if (!this.loaded) {
            this.load();
            this.loaded = true;
        }
        return this.hotbars[n];
    }
}

