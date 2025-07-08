/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.bytes.ByteArrayList
 *  it.unimi.dsi.fastutil.bytes.ByteList
 *  it.unimi.dsi.fastutil.ints.IntSet
 *  javax.annotation.Nullable
 *  org.lwjgl.system.MemoryUtil
 *  org.slf4j.Logger
 */
package net.minecraft.client.gui.font.providers;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.nio.Buffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.annotation.Nullable;
import net.minecraft.client.gui.font.CodepointMap;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.providers.GlyphProviderDefinition;
import net.minecraft.client.gui.font.providers.GlyphProviderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.FastBufferedInputStream;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

public class UnihexProvider
implements GlyphProvider {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int GLYPH_HEIGHT = 16;
    private static final int DIGITS_PER_BYTE = 2;
    private static final int DIGITS_FOR_WIDTH_8 = 32;
    private static final int DIGITS_FOR_WIDTH_16 = 64;
    private static final int DIGITS_FOR_WIDTH_24 = 96;
    private static final int DIGITS_FOR_WIDTH_32 = 128;
    private final CodepointMap<Glyph> glyphs;

    UnihexProvider(CodepointMap<Glyph> codepointMap) {
        this.glyphs = codepointMap;
    }

    @Override
    @Nullable
    public GlyphInfo getGlyph(int n) {
        return this.glyphs.get(n);
    }

    @Override
    public IntSet getSupportedGlyphs() {
        return this.glyphs.keySet();
    }

    @VisibleForTesting
    static void unpackBitsToBytes(IntBuffer intBuffer, int n, int n2, int n3) {
        int n4 = 32 - n2 - 1;
        int n5 = 32 - n3 - 1;
        for (int i = n4; i >= n5; --i) {
            if (i >= 32 || i < 0) {
                intBuffer.put(0);
                continue;
            }
            boolean bl = (n >> i & 1) != 0;
            intBuffer.put(bl ? -1 : 0);
        }
    }

    static void unpackBitsToBytes(IntBuffer intBuffer, LineData lineData, int n, int n2) {
        for (int i = 0; i < 16; ++i) {
            int n3 = lineData.line(i);
            UnihexProvider.unpackBitsToBytes(intBuffer, n3, n, n2);
        }
    }

    @VisibleForTesting
    static void readFromStream(InputStream inputStream, ReaderOutput readerOutput) throws IOException {
        int n = 0;
        ByteArrayList byteArrayList = new ByteArrayList(128);
        while (true) {
            int n2;
            boolean bl = UnihexProvider.copyUntil(inputStream, (ByteList)byteArrayList, 58);
            int n3 = byteArrayList.size();
            if (n3 == 0 && !bl) break;
            if (!bl || n3 != 4 && n3 != 5 && n3 != 6) {
                throw new IllegalArgumentException("Invalid entry at line " + n + ": expected 4, 5 or 6 hex digits followed by a colon");
            }
            int n4 = 0;
            for (n2 = 0; n2 < n3; ++n2) {
                n4 = n4 << 4 | UnihexProvider.decodeHex(n, byteArrayList.getByte(n2));
            }
            byteArrayList.clear();
            UnihexProvider.copyUntil(inputStream, (ByteList)byteArrayList, 10);
            n2 = byteArrayList.size();
            LineData lineData = switch (n2) {
                case 32 -> ByteContents.read(n, (ByteList)byteArrayList);
                case 64 -> ShortContents.read(n, (ByteList)byteArrayList);
                case 96 -> IntContents.read24(n, (ByteList)byteArrayList);
                case 128 -> IntContents.read32(n, (ByteList)byteArrayList);
                default -> throw new IllegalArgumentException("Invalid entry at line " + n + ": expected hex number describing (8,16,24,32) x 16 bitmap, followed by a new line");
            };
            readerOutput.accept(n4, lineData);
            ++n;
            byteArrayList.clear();
        }
    }

    static int decodeHex(int n, ByteList byteList, int n2) {
        return UnihexProvider.decodeHex(n, byteList.getByte(n2));
    }

    private static int decodeHex(int n, byte by) {
        return switch (by) {
            case 48 -> 0;
            case 49 -> 1;
            case 50 -> 2;
            case 51 -> 3;
            case 52 -> 4;
            case 53 -> 5;
            case 54 -> 6;
            case 55 -> 7;
            case 56 -> 8;
            case 57 -> 9;
            case 65 -> 10;
            case 66 -> 11;
            case 67 -> 12;
            case 68 -> 13;
            case 69 -> 14;
            case 70 -> 15;
            default -> throw new IllegalArgumentException("Invalid entry at line " + n + ": expected hex digit, got " + (char)by);
        };
    }

    private static boolean copyUntil(InputStream inputStream, ByteList byteList, int n) throws IOException {
        int n2;
        while ((n2 = inputStream.read()) != -1) {
            if (n2 == n) {
                return true;
            }
            byteList.add((byte)n2);
        }
        return false;
    }

    public static interface LineData {
        public int line(int var1);

        public int bitWidth();

        default public int mask() {
            int n = 0;
            for (int i = 0; i < 16; ++i) {
                n |= this.line(i);
            }
            return n;
        }

        default public int calculateWidth() {
            int n;
            int n2;
            int n3 = this.mask();
            int n4 = this.bitWidth();
            if (n3 == 0) {
                n2 = 0;
                n = n4;
            } else {
                n2 = Integer.numberOfLeadingZeros(n3);
                n = 32 - Integer.numberOfTrailingZeros(n3) - 1;
            }
            return Dimensions.pack(n2, n);
        }
    }

    record ByteContents(byte[] contents) implements LineData
    {
        @Override
        public int line(int n) {
            return this.contents[n] << 24;
        }

        static LineData read(int n, ByteList byteList) {
            byte[] byArray = new byte[16];
            int n2 = 0;
            for (int i = 0; i < 16; ++i) {
                byte by;
                int n3 = UnihexProvider.decodeHex(n, byteList, n2++);
                int n4 = UnihexProvider.decodeHex(n, byteList, n2++);
                byArray[i] = by = (byte)(n3 << 4 | n4);
            }
            return new ByteContents(byArray);
        }

        @Override
        public int bitWidth() {
            return 8;
        }
    }

    record ShortContents(short[] contents) implements LineData
    {
        @Override
        public int line(int n) {
            return this.contents[n] << 16;
        }

        static LineData read(int n, ByteList byteList) {
            short[] sArray = new short[16];
            int n2 = 0;
            for (int i = 0; i < 16; ++i) {
                short s;
                int n3 = UnihexProvider.decodeHex(n, byteList, n2++);
                int n4 = UnihexProvider.decodeHex(n, byteList, n2++);
                int n5 = UnihexProvider.decodeHex(n, byteList, n2++);
                int n6 = UnihexProvider.decodeHex(n, byteList, n2++);
                sArray[i] = s = (short)(n3 << 12 | n4 << 8 | n5 << 4 | n6);
            }
            return new ShortContents(sArray);
        }

        @Override
        public int bitWidth() {
            return 16;
        }
    }

    record IntContents(int[] contents, int bitWidth) implements LineData
    {
        private static final int SIZE_24 = 24;

        @Override
        public int line(int n) {
            return this.contents[n];
        }

        static LineData read24(int n, ByteList byteList) {
            int[] nArray = new int[16];
            int n2 = 0;
            int n3 = 0;
            for (int i = 0; i < 16; ++i) {
                int n4 = UnihexProvider.decodeHex(n, byteList, n3++);
                int n5 = UnihexProvider.decodeHex(n, byteList, n3++);
                int n6 = UnihexProvider.decodeHex(n, byteList, n3++);
                int n7 = UnihexProvider.decodeHex(n, byteList, n3++);
                int n8 = UnihexProvider.decodeHex(n, byteList, n3++);
                int n9 = UnihexProvider.decodeHex(n, byteList, n3++);
                int n10 = n4 << 20 | n5 << 16 | n6 << 12 | n7 << 8 | n8 << 4 | n9;
                nArray[i] = n10 << 8;
                n2 |= n10;
            }
            return new IntContents(nArray, 24);
        }

        public static LineData read32(int n, ByteList byteList) {
            int[] nArray = new int[16];
            int n2 = 0;
            int n3 = 0;
            for (int i = 0; i < 16; ++i) {
                int n4;
                int n5 = UnihexProvider.decodeHex(n, byteList, n3++);
                int n6 = UnihexProvider.decodeHex(n, byteList, n3++);
                int n7 = UnihexProvider.decodeHex(n, byteList, n3++);
                int n8 = UnihexProvider.decodeHex(n, byteList, n3++);
                int n9 = UnihexProvider.decodeHex(n, byteList, n3++);
                int n10 = UnihexProvider.decodeHex(n, byteList, n3++);
                int n11 = UnihexProvider.decodeHex(n, byteList, n3++);
                int n12 = UnihexProvider.decodeHex(n, byteList, n3++);
                nArray[i] = n4 = n5 << 28 | n6 << 24 | n7 << 20 | n8 << 16 | n9 << 12 | n10 << 8 | n11 << 4 | n12;
                n2 |= n4;
            }
            return new IntContents(nArray, 32);
        }
    }

    @FunctionalInterface
    public static interface ReaderOutput {
        public void accept(int var1, LineData var2);
    }

    static final class Glyph
    extends Record
    implements GlyphInfo {
        final LineData contents;
        final int left;
        final int right;

        Glyph(LineData lineData, int n, int n2) {
            this.contents = lineData;
            this.left = n;
            this.right = n2;
        }

        public int width() {
            return this.right - this.left + 1;
        }

        @Override
        public float getAdvance() {
            return this.width() / 2 + 1;
        }

        @Override
        public float getShadowOffset() {
            return 0.5f;
        }

        @Override
        public float getBoldOffset() {
            return 0.5f;
        }

        @Override
        public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> function) {
            return function.apply(new SheetGlyphInfo(){

                @Override
                public float getOversample() {
                    return 2.0f;
                }

                @Override
                public int getPixelWidth() {
                    return this.width();
                }

                @Override
                public int getPixelHeight() {
                    return 16;
                }

                @Override
                public void upload(int n, int n2, GpuTexture gpuTexture) {
                    IntBuffer intBuffer = MemoryUtil.memAllocInt((int)(this.width() * 16));
                    UnihexProvider.unpackBitsToBytes(intBuffer, contents, left, right);
                    intBuffer.rewind();
                    RenderSystem.getDevice().createCommandEncoder().writeToTexture(gpuTexture, intBuffer, NativeImage.Format.RGBA, 0, 0, n, n2, this.width(), 16);
                    MemoryUtil.memFree((Buffer)intBuffer);
                }

                @Override
                public boolean isColored() {
                    return true;
                }
            });
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Glyph.class, "contents;left;right", "contents", "left", "right"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Glyph.class, "contents;left;right", "contents", "left", "right"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Glyph.class, "contents;left;right", "contents", "left", "right"}, this, object);
        }

        public LineData contents() {
            return this.contents;
        }

        public int left() {
            return this.left;
        }

        public int right() {
            return this.right;
        }
    }

    public static class Definition
    implements GlyphProviderDefinition {
        public static final MapCodec<Definition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ResourceLocation.CODEC.fieldOf("hex_file").forGetter(definition -> definition.hexFile), (App)OverrideRange.CODEC.listOf().optionalFieldOf("size_overrides", List.of()).forGetter(definition -> definition.sizeOverrides)).apply((Applicative)instance, Definition::new));
        private final ResourceLocation hexFile;
        private final List<OverrideRange> sizeOverrides;

        private Definition(ResourceLocation resourceLocation, List<OverrideRange> list) {
            this.hexFile = resourceLocation;
            this.sizeOverrides = list;
        }

        @Override
        public GlyphProviderType type() {
            return GlyphProviderType.UNIHEX;
        }

        @Override
        public Either<GlyphProviderDefinition.Loader, GlyphProviderDefinition.Reference> unpack() {
            return Either.left(this::load);
        }

        private GlyphProvider load(ResourceManager resourceManager) throws IOException {
            try (InputStream inputStream = resourceManager.open(this.hexFile);){
                UnihexProvider unihexProvider = this.loadData(inputStream);
                return unihexProvider;
            }
        }

        private UnihexProvider loadData(InputStream inputStream) throws IOException {
            CodepointMap<LineData> codepointMap = new CodepointMap<LineData>(LineData[]::new, n -> new LineData[n][]);
            ReaderOutput readerOutput = codepointMap::put;
            try (ZipInputStream zipInputStream = new ZipInputStream(inputStream);){
                Object object;
                ZipEntry zipEntry;
                while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                    object = zipEntry.getName();
                    if (!((String)object).endsWith(".hex")) continue;
                    LOGGER.info("Found {}, loading", object);
                    UnihexProvider.readFromStream(new FastBufferedInputStream(zipInputStream), readerOutput);
                }
                object = new CodepointMap(Glyph[]::new, n -> new Glyph[n][]);
                for (OverrideRange overrideRange : this.sizeOverrides) {
                    int n2 = overrideRange.from;
                    int n3 = overrideRange.to;
                    Dimensions dimensions = overrideRange.dimensions;
                    for (int i = n2; i <= n3; ++i) {
                        LineData lineData = (LineData)codepointMap.remove(i);
                        if (lineData == null) continue;
                        ((CodepointMap)object).put(i, new Glyph(lineData, dimensions.left, dimensions.right));
                    }
                }
                codepointMap.forEach((arg_0, arg_1) -> Definition.lambda$loadData$7((CodepointMap)object, arg_0, arg_1));
                UnihexProvider unihexProvider = new UnihexProvider((CodepointMap<Glyph>)object);
                return unihexProvider;
            }
        }

        private static /* synthetic */ void lambda$loadData$7(CodepointMap codepointMap, int n, LineData lineData) {
            int n2 = lineData.calculateWidth();
            int n3 = Dimensions.left(n2);
            int n4 = Dimensions.right(n2);
            codepointMap.put(n, new Glyph(lineData, n3, n4));
        }
    }

    public static final class Dimensions
    extends Record {
        final int left;
        final int right;
        public static final MapCodec<Dimensions> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.INT.fieldOf("left").forGetter(Dimensions::left), (App)Codec.INT.fieldOf("right").forGetter(Dimensions::right)).apply((Applicative)instance, Dimensions::new));
        public static final Codec<Dimensions> CODEC = MAP_CODEC.codec();

        public Dimensions(int n, int n2) {
            this.left = n;
            this.right = n2;
        }

        public int pack() {
            return Dimensions.pack(this.left, this.right);
        }

        public static int pack(int n, int n2) {
            return (n & 0xFF) << 8 | n2 & 0xFF;
        }

        public static int left(int n) {
            return (byte)(n >> 8);
        }

        public static int right(int n) {
            return (byte)n;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Dimensions.class, "left;right", "left", "right"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Dimensions.class, "left;right", "left", "right"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Dimensions.class, "left;right", "left", "right"}, this, object);
        }

        public int left() {
            return this.left;
        }

        public int right() {
            return this.right;
        }
    }

    static final class OverrideRange
    extends Record {
        final int from;
        final int to;
        final Dimensions dimensions;
        private static final Codec<OverrideRange> RAW_CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ExtraCodecs.CODEPOINT.fieldOf("from").forGetter(OverrideRange::from), (App)ExtraCodecs.CODEPOINT.fieldOf("to").forGetter(OverrideRange::to), (App)Dimensions.MAP_CODEC.forGetter(OverrideRange::dimensions)).apply((Applicative)instance, OverrideRange::new));
        public static final Codec<OverrideRange> CODEC = RAW_CODEC.validate(overrideRange -> {
            if (overrideRange.from >= overrideRange.to) {
                return DataResult.error(() -> "Invalid range: [" + overrideRange.from + ";" + overrideRange.to + "]");
            }
            return DataResult.success((Object)overrideRange);
        });

        private OverrideRange(int n, int n2, Dimensions dimensions) {
            this.from = n;
            this.to = n2;
            this.dimensions = dimensions;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{OverrideRange.class, "from;to;dimensions", "from", "to", "dimensions"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{OverrideRange.class, "from;to;dimensions", "from", "to", "dimensions"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{OverrideRange.class, "from;to;dimensions", "from", "to", "dimensions"}, this, object);
        }

        public int from() {
            return this.from;
        }

        public int to() {
            return this.to;
        }

        public Dimensions dimensions() {
            return this.dimensions;
        }
    }
}

