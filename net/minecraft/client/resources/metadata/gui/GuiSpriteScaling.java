/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.client.resources.metadata.gui;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.OptionalInt;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;

public interface GuiSpriteScaling {
    public static final Codec<GuiSpriteScaling> CODEC = Type.CODEC.dispatch(GuiSpriteScaling::type, Type::codec);
    public static final GuiSpriteScaling DEFAULT = new Stretch();

    public Type type();

    public static enum Type implements StringRepresentable
    {
        STRETCH("stretch", Stretch.CODEC),
        TILE("tile", Tile.CODEC),
        NINE_SLICE("nine_slice", NineSlice.CODEC);

        public static final Codec<Type> CODEC;
        private final String key;
        private final MapCodec<? extends GuiSpriteScaling> codec;

        private Type(String string2, MapCodec<? extends GuiSpriteScaling> mapCodec) {
            this.key = string2;
            this.codec = mapCodec;
        }

        @Override
        public String getSerializedName() {
            return this.key;
        }

        public MapCodec<? extends GuiSpriteScaling> codec() {
            return this.codec;
        }

        static {
            CODEC = StringRepresentable.fromEnum(Type::values);
        }
    }

    public record Stretch() implements GuiSpriteScaling
    {
        public static final MapCodec<Stretch> CODEC = MapCodec.unit(Stretch::new);

        @Override
        public Type type() {
            return Type.STRETCH;
        }
    }

    public record NineSlice(int width, int height, Border border, boolean stretchInner) implements GuiSpriteScaling
    {
        public static final MapCodec<NineSlice> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ExtraCodecs.POSITIVE_INT.fieldOf("width").forGetter(NineSlice::width), (App)ExtraCodecs.POSITIVE_INT.fieldOf("height").forGetter(NineSlice::height), (App)Border.CODEC.fieldOf("border").forGetter(NineSlice::border), (App)Codec.BOOL.optionalFieldOf("stretch_inner", (Object)false).forGetter(NineSlice::stretchInner)).apply((Applicative)instance, NineSlice::new)).validate(NineSlice::validate);

        private static DataResult<NineSlice> validate(NineSlice nineSlice) {
            Border border = nineSlice.border();
            if (border.left() + border.right() >= nineSlice.width()) {
                return DataResult.error(() -> "Nine-sliced texture has no horizontal center slice: " + border.left() + " + " + border.right() + " >= " + nineSlice.width());
            }
            if (border.top() + border.bottom() >= nineSlice.height()) {
                return DataResult.error(() -> "Nine-sliced texture has no vertical center slice: " + border.top() + " + " + border.bottom() + " >= " + nineSlice.height());
            }
            return DataResult.success((Object)nineSlice);
        }

        @Override
        public Type type() {
            return Type.NINE_SLICE;
        }

        public record Border(int left, int top, int right, int bottom) {
            private static final Codec<Border> VALUE_CODEC = ExtraCodecs.POSITIVE_INT.flatComapMap(n -> new Border((int)n, (int)n, (int)n, (int)n), border -> {
                OptionalInt optionalInt = border.unpackValue();
                if (optionalInt.isPresent()) {
                    return DataResult.success((Object)optionalInt.getAsInt());
                }
                return DataResult.error(() -> "Border has different side sizes");
            });
            private static final Codec<Border> RECORD_CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ExtraCodecs.NON_NEGATIVE_INT.fieldOf("left").forGetter(Border::left), (App)ExtraCodecs.NON_NEGATIVE_INT.fieldOf("top").forGetter(Border::top), (App)ExtraCodecs.NON_NEGATIVE_INT.fieldOf("right").forGetter(Border::right), (App)ExtraCodecs.NON_NEGATIVE_INT.fieldOf("bottom").forGetter(Border::bottom)).apply((Applicative)instance, Border::new));
            static final Codec<Border> CODEC = Codec.either(VALUE_CODEC, RECORD_CODEC).xmap(Either::unwrap, border -> {
                if (border.unpackValue().isPresent()) {
                    return Either.left((Object)border);
                }
                return Either.right((Object)border);
            });

            private OptionalInt unpackValue() {
                if (this.left() == this.top() && this.top() == this.right() && this.right() == this.bottom()) {
                    return OptionalInt.of(this.left());
                }
                return OptionalInt.empty();
            }
        }
    }

    public record Tile(int width, int height) implements GuiSpriteScaling
    {
        public static final MapCodec<Tile> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ExtraCodecs.POSITIVE_INT.fieldOf("width").forGetter(Tile::width), (App)ExtraCodecs.POSITIVE_INT.fieldOf("height").forGetter(Tile::height)).apply((Applicative)instance, Tile::new));

        @Override
        public Type type() {
            return Type.TILE;
        }
    }
}

