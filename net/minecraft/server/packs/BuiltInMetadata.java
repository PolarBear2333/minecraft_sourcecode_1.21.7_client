/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.packs;

import java.util.Map;
import net.minecraft.server.packs.metadata.MetadataSectionType;

public class BuiltInMetadata {
    private static final BuiltInMetadata EMPTY = new BuiltInMetadata(Map.of());
    private final Map<MetadataSectionType<?>, ?> values;

    private BuiltInMetadata(Map<MetadataSectionType<?>, ?> map) {
        this.values = map;
    }

    public <T> T get(MetadataSectionType<T> metadataSectionType) {
        return (T)this.values.get(metadataSectionType);
    }

    public static BuiltInMetadata of() {
        return EMPTY;
    }

    public static <T> BuiltInMetadata of(MetadataSectionType<T> metadataSectionType, T t) {
        return new BuiltInMetadata(Map.of(metadataSectionType, t));
    }

    public static <T1, T2> BuiltInMetadata of(MetadataSectionType<T1> metadataSectionType, T1 T1, MetadataSectionType<T2> metadataSectionType2, T2 T2) {
        return new BuiltInMetadata(Map.of(metadataSectionType, T1, metadataSectionType2, T2));
    }
}

