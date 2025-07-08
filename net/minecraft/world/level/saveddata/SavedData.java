/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.saveddata;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;

public abstract class SavedData {
    private boolean dirty;

    public void setDirty() {
        this.setDirty(true);
    }

    public void setDirty(boolean bl) {
        this.dirty = bl;
    }

    public boolean isDirty() {
        return this.dirty;
    }

    public record Context(@Nullable ServerLevel level, long worldSeed) {
        public Context(ServerLevel serverLevel) {
            this(serverLevel, serverLevel.getSeed());
        }

        public ServerLevel levelOrThrow() {
            return Objects.requireNonNull(this.level);
        }
    }
}

