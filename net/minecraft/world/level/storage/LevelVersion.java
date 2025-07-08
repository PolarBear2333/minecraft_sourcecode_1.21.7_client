/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.OptionalDynamic
 */
package net.minecraft.world.level.storage;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import net.minecraft.SharedConstants;
import net.minecraft.world.level.storage.DataVersion;

public class LevelVersion {
    private final int levelDataVersion;
    private final long lastPlayed;
    private final String minecraftVersionName;
    private final DataVersion minecraftVersion;
    private final boolean snapshot;

    private LevelVersion(int n, long l, String string, int n2, String string2, boolean bl) {
        this.levelDataVersion = n;
        this.lastPlayed = l;
        this.minecraftVersionName = string;
        this.minecraftVersion = new DataVersion(n2, string2);
        this.snapshot = bl;
    }

    public static LevelVersion parse(Dynamic<?> dynamic) {
        int n = dynamic.get("version").asInt(0);
        long l = dynamic.get("LastPlayed").asLong(0L);
        OptionalDynamic optionalDynamic = dynamic.get("Version");
        if (optionalDynamic.result().isPresent()) {
            return new LevelVersion(n, l, optionalDynamic.get("Name").asString(SharedConstants.getCurrentVersion().name()), optionalDynamic.get("Id").asInt(SharedConstants.getCurrentVersion().dataVersion().version()), optionalDynamic.get("Series").asString("main"), optionalDynamic.get("Snapshot").asBoolean(!SharedConstants.getCurrentVersion().stable()));
        }
        return new LevelVersion(n, l, "", 0, "main", false);
    }

    public int levelDataVersion() {
        return this.levelDataVersion;
    }

    public long lastPlayed() {
        return this.lastPlayed;
    }

    public String minecraftVersionName() {
        return this.minecraftVersionName;
    }

    public DataVersion minecraftVersion() {
        return this.minecraftVersion;
    }

    public boolean snapshot() {
        return this.snapshot;
    }
}

