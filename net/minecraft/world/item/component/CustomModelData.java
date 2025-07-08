/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 *  javax.annotation.Nullable
 */
package net.minecraft.world.item.component;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

public record CustomModelData(List<Float> floats, List<Boolean> flags, List<String> strings, List<Integer> colors) {
    public static final CustomModelData EMPTY = new CustomModelData(List.of(), List.of(), List.of(), List.of());
    public static final Codec<CustomModelData> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.FLOAT.listOf().optionalFieldOf("floats", List.of()).forGetter(CustomModelData::floats), (App)Codec.BOOL.listOf().optionalFieldOf("flags", List.of()).forGetter(CustomModelData::flags), (App)Codec.STRING.listOf().optionalFieldOf("strings", List.of()).forGetter(CustomModelData::strings), (App)ExtraCodecs.RGB_COLOR_CODEC.listOf().optionalFieldOf("colors", List.of()).forGetter(CustomModelData::colors)).apply((Applicative)instance, CustomModelData::new));
    public static final StreamCodec<ByteBuf, CustomModelData> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.FLOAT.apply(ByteBufCodecs.list()), CustomModelData::floats, ByteBufCodecs.BOOL.apply(ByteBufCodecs.list()), CustomModelData::flags, ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), CustomModelData::strings, ByteBufCodecs.INT.apply(ByteBufCodecs.list()), CustomModelData::colors, CustomModelData::new);

    @Nullable
    private static <T> T getSafe(List<T> list, int n) {
        if (n < 0 || n >= list.size()) {
            return null;
        }
        return list.get(n);
    }

    @Nullable
    public Float getFloat(int n) {
        return CustomModelData.getSafe(this.floats, n);
    }

    @Nullable
    public Boolean getBoolean(int n) {
        return CustomModelData.getSafe(this.flags, n);
    }

    @Nullable
    public String getString(int n) {
        return CustomModelData.getSafe(this.strings, n);
    }

    @Nullable
    public Integer getColor(int n) {
        return CustomModelData.getSafe(this.colors, n);
    }
}

