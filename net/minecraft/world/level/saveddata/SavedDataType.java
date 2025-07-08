/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.saveddata;

import com.mojang.serialization.Codec;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

public record SavedDataType<T extends SavedData>(String id, Function<SavedData.Context, T> constructor, Function<SavedData.Context, Codec<T>> codec, DataFixTypes dataFixType) {
    public SavedDataType(String string, Supplier<T> supplier, Codec<T> codec, DataFixTypes dataFixTypes) {
        this(string, (SavedData.Context context) -> (SavedData)supplier.get(), (SavedData.Context context) -> codec, dataFixTypes);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof SavedDataType)) return false;
        SavedDataType savedDataType = (SavedDataType)object;
        if (!this.id.equals(savedDataType.id)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public String toString() {
        return "SavedDataType[" + this.id + "]";
    }
}

