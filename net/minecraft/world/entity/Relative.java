/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.world.entity;

import io.netty.buffer.ByteBuf;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum Relative {
    X(0),
    Y(1),
    Z(2),
    Y_ROT(3),
    X_ROT(4),
    DELTA_X(5),
    DELTA_Y(6),
    DELTA_Z(7),
    ROTATE_DELTA(8);

    public static final Set<Relative> ALL;
    public static final Set<Relative> ROTATION;
    public static final Set<Relative> DELTA;
    public static final StreamCodec<ByteBuf, Set<Relative>> SET_STREAM_CODEC;
    private final int bit;

    @SafeVarargs
    public static Set<Relative> union(Set<Relative> ... setArray) {
        HashSet<Relative> hashSet = new HashSet<Relative>();
        for (Set<Relative> set : setArray) {
            hashSet.addAll(set);
        }
        return hashSet;
    }

    private Relative(int n2) {
        this.bit = n2;
    }

    private int getMask() {
        return 1 << this.bit;
    }

    private boolean isSet(int n) {
        return (n & this.getMask()) == this.getMask();
    }

    public static Set<Relative> unpack(int n) {
        EnumSet<Relative> enumSet = EnumSet.noneOf(Relative.class);
        for (Relative relative : Relative.values()) {
            if (!relative.isSet(n)) continue;
            enumSet.add(relative);
        }
        return enumSet;
    }

    public static int pack(Set<Relative> set) {
        int n = 0;
        for (Relative relative : set) {
            n |= relative.getMask();
        }
        return n;
    }

    static {
        ALL = Set.of(Relative.values());
        ROTATION = Set.of(X_ROT, Y_ROT);
        DELTA = Set.of(DELTA_X, DELTA_Y, DELTA_Z, ROTATE_DELTA);
        SET_STREAM_CODEC = ByteBufCodecs.INT.map(Relative::unpack, Relative::pack);
    }
}

