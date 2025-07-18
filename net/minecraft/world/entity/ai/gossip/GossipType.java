/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.entity.ai.gossip;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum GossipType implements StringRepresentable
{
    MAJOR_NEGATIVE("major_negative", -5, 100, 10, 10),
    MINOR_NEGATIVE("minor_negative", -1, 200, 20, 20),
    MINOR_POSITIVE("minor_positive", 1, 25, 1, 5),
    MAJOR_POSITIVE("major_positive", 5, 20, 0, 20),
    TRADING("trading", 1, 25, 2, 20);

    public static final int REPUTATION_CHANGE_PER_EVENT = 25;
    public static final int REPUTATION_CHANGE_PER_EVERLASTING_MEMORY = 20;
    public static final int REPUTATION_CHANGE_PER_TRADE = 2;
    public final String id;
    public final int weight;
    public final int max;
    public final int decayPerDay;
    public final int decayPerTransfer;
    public static final Codec<GossipType> CODEC;

    private GossipType(String string2, int n2, int n3, int n4, int n5) {
        this.id = string2;
        this.weight = n2;
        this.max = n3;
        this.decayPerDay = n4;
        this.decayPerTransfer = n5;
    }

    @Override
    public String getSerializedName() {
        return this.id;
    }

    static {
        CODEC = StringRepresentable.fromEnum(GossipType::values);
    }
}

