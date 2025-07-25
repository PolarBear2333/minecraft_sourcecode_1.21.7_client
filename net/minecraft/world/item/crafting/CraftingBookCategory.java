/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

public enum CraftingBookCategory implements StringRepresentable
{
    BUILDING("building", 0),
    REDSTONE("redstone", 1),
    EQUIPMENT("equipment", 2),
    MISC("misc", 3);

    public static final Codec<CraftingBookCategory> CODEC;
    public static final IntFunction<CraftingBookCategory> BY_ID;
    public static final StreamCodec<ByteBuf, CraftingBookCategory> STREAM_CODEC;
    private final String name;
    private final int id;

    private CraftingBookCategory(String string2, int n2) {
        this.name = string2;
        this.id = n2;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    private int id() {
        return this.id;
    }

    static {
        CODEC = StringRepresentable.fromEnum(CraftingBookCategory::values);
        BY_ID = ByIdMap.continuous(CraftingBookCategory::id, CraftingBookCategory.values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, CraftingBookCategory::id);
    }
}

