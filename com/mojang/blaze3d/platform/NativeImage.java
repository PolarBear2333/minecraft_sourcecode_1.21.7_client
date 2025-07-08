/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.jtracy.MemoryPool
 *  com.mojang.jtracy.TracyClient
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.apache.commons.io.IOUtils
 *  org.lwjgl.stb.STBIWriteCallback
 *  org.lwjgl.stb.STBImage
 *  org.lwjgl.stb.STBImageResize
 *  org.lwjgl.stb.STBImageWrite
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.system.MemoryUtil
 *  org.lwjgl.util.freetype.FT_Bitmap
 *  org.lwjgl.util.freetype.FT_Face
 *  org.lwjgl.util.freetype.FT_GlyphSlot
 *  org.lwjgl.util.freetype.FreeType
 *  org.slf4j.Logger
 */
package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.platform.DebugMemoryUntracker;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.jtracy.MemoryPool;
import com.mojang.jtracy.TracyClient;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.IntUnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.client.gui.font.providers.FreeTypeUtil;
import net.minecraft.util.ARGB;
import net.minecraft.util.PngInfo;
import org.apache.commons.io.IOUtils;
import org.lwjgl.stb.STBIWriteCallback;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBImageResize;
import org.lwjgl.stb.STBImageWrite;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.freetype.FT_Bitmap;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FT_GlyphSlot;
import org.lwjgl.util.freetype.FreeType;
import org.slf4j.Logger;

public final class NativeImage
implements AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final MemoryPool MEMORY_POOL = TracyClient.createMemoryPool((String)"NativeImage");
    private static final Set<StandardOpenOption> OPEN_OPTIONS = EnumSet.of(StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    private final Format format;
    private final int width;
    private final int height;
    private final boolean useStbFree;
    private long pixels;
    private final long size;

    public NativeImage(int n, int n2, boolean bl) {
        this(Format.RGBA, n, n2, bl);
    }

    public NativeImage(Format format, int n, int n2, boolean bl) {
        if (n <= 0 || n2 <= 0) {
            throw new IllegalArgumentException("Invalid texture size: " + n + "x" + n2);
        }
        this.format = format;
        this.width = n;
        this.height = n2;
        this.size = (long)n * (long)n2 * (long)format.components();
        this.useStbFree = false;
        this.pixels = bl ? MemoryUtil.nmemCalloc((long)1L, (long)this.size) : MemoryUtil.nmemAlloc((long)this.size);
        MEMORY_POOL.malloc(this.pixels, (int)this.size);
        if (this.pixels == 0L) {
            throw new IllegalStateException("Unable to allocate texture of size " + n + "x" + n2 + " (" + format.components() + " channels)");
        }
    }

    public NativeImage(Format format, int n, int n2, boolean bl, long l) {
        if (n <= 0 || n2 <= 0) {
            throw new IllegalArgumentException("Invalid texture size: " + n + "x" + n2);
        }
        this.format = format;
        this.width = n;
        this.height = n2;
        this.useStbFree = bl;
        this.pixels = l;
        this.size = (long)n * (long)n2 * (long)format.components();
    }

    public String toString() {
        return "NativeImage[" + String.valueOf((Object)this.format) + " " + this.width + "x" + this.height + "@" + this.pixels + (this.useStbFree ? "S" : "N") + "]";
    }

    private boolean isOutsideBounds(int n, int n2) {
        return n < 0 || n >= this.width || n2 < 0 || n2 >= this.height;
    }

    public static NativeImage read(InputStream inputStream) throws IOException {
        return NativeImage.read(Format.RGBA, inputStream);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static NativeImage read(@Nullable Format format, InputStream inputStream) throws IOException {
        ByteBuffer byteBuffer = null;
        try {
            byteBuffer = TextureUtil.readResource(inputStream);
            byteBuffer.rewind();
            NativeImage nativeImage = NativeImage.read(format, byteBuffer);
            return nativeImage;
        }
        finally {
            MemoryUtil.memFree((Buffer)byteBuffer);
            IOUtils.closeQuietly((InputStream)inputStream);
        }
    }

    public static NativeImage read(ByteBuffer byteBuffer) throws IOException {
        return NativeImage.read(Format.RGBA, byteBuffer);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static NativeImage read(byte[] byArray) throws IOException {
        MemoryStack memoryStack = MemoryStack.stackGet();
        int n = memoryStack.getPointer();
        if (n < byArray.length) {
            ByteBuffer byteBuffer = MemoryUtil.memAlloc((int)byArray.length);
            try {
                NativeImage nativeImage = NativeImage.putAndRead(byteBuffer, byArray);
                return nativeImage;
            }
            finally {
                MemoryUtil.memFree((Buffer)byteBuffer);
            }
        }
        try (MemoryStack memoryStack2 = MemoryStack.stackPush();){
            ByteBuffer byteBuffer = memoryStack2.malloc(byArray.length);
            NativeImage nativeImage = NativeImage.putAndRead(byteBuffer, byArray);
            return nativeImage;
        }
    }

    private static NativeImage putAndRead(ByteBuffer byteBuffer, byte[] byArray) throws IOException {
        byteBuffer.put(byArray);
        byteBuffer.rewind();
        return NativeImage.read(byteBuffer);
    }

    public static NativeImage read(@Nullable Format format, ByteBuffer byteBuffer) throws IOException {
        if (format != null && !format.supportedByStb()) {
            throw new UnsupportedOperationException("Don't know how to read format " + String.valueOf((Object)format));
        }
        if (MemoryUtil.memAddress((ByteBuffer)byteBuffer) == 0L) {
            throw new IllegalArgumentException("Invalid buffer");
        }
        PngInfo.validateHeader(byteBuffer);
        try (MemoryStack memoryStack = MemoryStack.stackPush();){
            IntBuffer intBuffer = memoryStack.mallocInt(1);
            IntBuffer intBuffer2 = memoryStack.mallocInt(1);
            IntBuffer intBuffer3 = memoryStack.mallocInt(1);
            ByteBuffer byteBuffer2 = STBImage.stbi_load_from_memory((ByteBuffer)byteBuffer, (IntBuffer)intBuffer, (IntBuffer)intBuffer2, (IntBuffer)intBuffer3, (int)(format == null ? 0 : format.components));
            if (byteBuffer2 == null) {
                throw new IOException("Could not load image: " + STBImage.stbi_failure_reason());
            }
            long l = MemoryUtil.memAddress((ByteBuffer)byteBuffer2);
            MEMORY_POOL.malloc(l, byteBuffer2.limit());
            NativeImage nativeImage = new NativeImage(format == null ? Format.getStbFormat(intBuffer3.get(0)) : format, intBuffer.get(0), intBuffer2.get(0), true, l);
            return nativeImage;
        }
    }

    private void checkAllocated() {
        if (this.pixels == 0L) {
            throw new IllegalStateException("Image is not allocated.");
        }
    }

    @Override
    public void close() {
        if (this.pixels != 0L) {
            if (this.useStbFree) {
                STBImage.nstbi_image_free((long)this.pixels);
            } else {
                MemoryUtil.nmemFree((long)this.pixels);
            }
            MEMORY_POOL.free(this.pixels);
        }
        this.pixels = 0L;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public Format format() {
        return this.format;
    }

    private int getPixelABGR(int n, int n2) {
        if (this.format != Format.RGBA) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "getPixelRGBA only works on RGBA images; have %s", new Object[]{this.format}));
        }
        if (this.isOutsideBounds(n, n2)) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "(%s, %s) outside of image bounds (%s, %s)", n, n2, this.width, this.height));
        }
        this.checkAllocated();
        long l = ((long)n + (long)n2 * (long)this.width) * 4L;
        return MemoryUtil.memGetInt((long)(this.pixels + l));
    }

    public int getPixel(int n, int n2) {
        return ARGB.fromABGR(this.getPixelABGR(n, n2));
    }

    public void setPixelABGR(int n, int n2, int n3) {
        if (this.format != Format.RGBA) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "setPixelRGBA only works on RGBA images; have %s", new Object[]{this.format}));
        }
        if (this.isOutsideBounds(n, n2)) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "(%s, %s) outside of image bounds (%s, %s)", n, n2, this.width, this.height));
        }
        this.checkAllocated();
        long l = ((long)n + (long)n2 * (long)this.width) * 4L;
        MemoryUtil.memPutInt((long)(this.pixels + l), (int)n3);
    }

    public void setPixel(int n, int n2, int n3) {
        this.setPixelABGR(n, n2, ARGB.toABGR(n3));
    }

    public NativeImage mappedCopy(IntUnaryOperator intUnaryOperator) {
        if (this.format != Format.RGBA) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "function application only works on RGBA images; have %s", new Object[]{this.format}));
        }
        this.checkAllocated();
        NativeImage nativeImage = new NativeImage(this.width, this.height, false);
        int n = this.width * this.height;
        IntBuffer intBuffer = MemoryUtil.memIntBuffer((long)this.pixels, (int)n);
        IntBuffer intBuffer2 = MemoryUtil.memIntBuffer((long)nativeImage.pixels, (int)n);
        for (int i = 0; i < n; ++i) {
            int n2 = ARGB.fromABGR(intBuffer.get(i));
            int n3 = intUnaryOperator.applyAsInt(n2);
            intBuffer2.put(i, ARGB.toABGR(n3));
        }
        return nativeImage;
    }

    public int[] getPixelsABGR() {
        if (this.format != Format.RGBA) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "getPixels only works on RGBA images; have %s", new Object[]{this.format}));
        }
        this.checkAllocated();
        int[] nArray = new int[this.width * this.height];
        MemoryUtil.memIntBuffer((long)this.pixels, (int)(this.width * this.height)).get(nArray);
        return nArray;
    }

    public int[] getPixels() {
        int[] nArray = this.getPixelsABGR();
        for (int i = 0; i < nArray.length; ++i) {
            nArray[i] = ARGB.fromABGR(nArray[i]);
        }
        return nArray;
    }

    public byte getLuminanceOrAlpha(int n, int n2) {
        if (!this.format.hasLuminanceOrAlpha()) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "no luminance or alpha in %s", new Object[]{this.format}));
        }
        if (this.isOutsideBounds(n, n2)) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "(%s, %s) outside of image bounds (%s, %s)", n, n2, this.width, this.height));
        }
        int n3 = (n + n2 * this.width) * this.format.components() + this.format.luminanceOrAlphaOffset() / 8;
        return MemoryUtil.memGetByte((long)(this.pixels + (long)n3));
    }

    @Deprecated
    public int[] makePixelArray() {
        if (this.format != Format.RGBA) {
            throw new UnsupportedOperationException("can only call makePixelArray for RGBA images.");
        }
        this.checkAllocated();
        int[] nArray = new int[this.getWidth() * this.getHeight()];
        for (int i = 0; i < this.getHeight(); ++i) {
            for (int j = 0; j < this.getWidth(); ++j) {
                nArray[j + i * this.getWidth()] = this.getPixel(j, i);
            }
        }
        return nArray;
    }

    public void writeToFile(File file) throws IOException {
        this.writeToFile(file.toPath());
    }

    public boolean copyFromFont(FT_Face fT_Face, int n) {
        if (this.format.components() != 1) {
            throw new IllegalArgumentException("Can only write fonts into 1-component images.");
        }
        if (FreeTypeUtil.checkError(FreeType.FT_Load_Glyph((FT_Face)fT_Face, (int)n, (int)4), "Loading glyph")) {
            return false;
        }
        FT_GlyphSlot fT_GlyphSlot = Objects.requireNonNull(fT_Face.glyph(), "Glyph not initialized");
        FT_Bitmap fT_Bitmap = fT_GlyphSlot.bitmap();
        if (fT_Bitmap.pixel_mode() != 2) {
            throw new IllegalStateException("Rendered glyph was not 8-bit grayscale");
        }
        if (fT_Bitmap.width() != this.getWidth() || fT_Bitmap.rows() != this.getHeight()) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "Glyph bitmap of size %sx%s does not match image of size: %sx%s", fT_Bitmap.width(), fT_Bitmap.rows(), this.getWidth(), this.getHeight()));
        }
        int n2 = fT_Bitmap.width() * fT_Bitmap.rows();
        ByteBuffer byteBuffer = Objects.requireNonNull(fT_Bitmap.buffer(n2), "Glyph has no bitmap");
        MemoryUtil.memCopy((long)MemoryUtil.memAddress((ByteBuffer)byteBuffer), (long)this.pixels, (long)n2);
        return true;
    }

    public void writeToFile(Path path) throws IOException {
        if (!this.format.supportedByStb()) {
            throw new UnsupportedOperationException("Don't know how to write format " + String.valueOf((Object)this.format));
        }
        this.checkAllocated();
        try (SeekableByteChannel seekableByteChannel = Files.newByteChannel(path, OPEN_OPTIONS, new FileAttribute[0]);){
            if (!this.writeToChannel(seekableByteChannel)) {
                throw new IOException("Could not write image to the PNG file \"" + String.valueOf(path.toAbsolutePath()) + "\": " + STBImage.stbi_failure_reason());
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean writeToChannel(WritableByteChannel writableByteChannel) throws IOException {
        WriteCallback writeCallback = new WriteCallback(writableByteChannel);
        try {
            int n = Math.min(this.getHeight(), Integer.MAX_VALUE / this.getWidth() / this.format.components());
            if (n < this.getHeight()) {
                LOGGER.warn("Dropping image height from {} to {} to fit the size into 32-bit signed int", (Object)this.getHeight(), (Object)n);
            }
            if (STBImageWrite.nstbi_write_png_to_func((long)writeCallback.address(), (long)0L, (int)this.getWidth(), (int)n, (int)this.format.components(), (long)this.pixels, (int)0) == 0) {
                boolean bl = false;
                return bl;
            }
            writeCallback.throwIfException();
            boolean bl = true;
            return bl;
        }
        finally {
            writeCallback.free();
        }
    }

    public void copyFrom(NativeImage nativeImage) {
        if (nativeImage.format() != this.format) {
            throw new UnsupportedOperationException("Image formats don't match.");
        }
        int n = this.format.components();
        this.checkAllocated();
        nativeImage.checkAllocated();
        if (this.width == nativeImage.width) {
            MemoryUtil.memCopy((long)nativeImage.pixels, (long)this.pixels, (long)Math.min(this.size, nativeImage.size));
        } else {
            int n2 = Math.min(this.getWidth(), nativeImage.getWidth());
            int n3 = Math.min(this.getHeight(), nativeImage.getHeight());
            for (int i = 0; i < n3; ++i) {
                int n4 = i * nativeImage.getWidth() * n;
                int n5 = i * this.getWidth() * n;
                MemoryUtil.memCopy((long)(nativeImage.pixels + (long)n4), (long)(this.pixels + (long)n5), (long)n2);
            }
        }
    }

    public void fillRect(int n, int n2, int n3, int n4, int n5) {
        for (int i = n2; i < n2 + n4; ++i) {
            for (int j = n; j < n + n3; ++j) {
                this.setPixel(j, i, n5);
            }
        }
    }

    public void copyRect(int n, int n2, int n3, int n4, int n5, int n6, boolean bl, boolean bl2) {
        this.copyRect(this, n, n2, n + n3, n2 + n4, n5, n6, bl, bl2);
    }

    public void copyRect(NativeImage nativeImage, int n, int n2, int n3, int n4, int n5, int n6, boolean bl, boolean bl2) {
        for (int i = 0; i < n6; ++i) {
            for (int j = 0; j < n5; ++j) {
                int n7 = bl ? n5 - 1 - j : j;
                int n8 = bl2 ? n6 - 1 - i : i;
                int n9 = this.getPixelABGR(n + j, n2 + i);
                nativeImage.setPixelABGR(n3 + n7, n4 + n8, n9);
            }
        }
    }

    public void resizeSubRectTo(int n, int n2, int n3, int n4, NativeImage nativeImage) {
        this.checkAllocated();
        if (nativeImage.format() != this.format) {
            throw new UnsupportedOperationException("resizeSubRectTo only works for images of the same format.");
        }
        int n5 = this.format.components();
        STBImageResize.nstbir_resize_uint8((long)(this.pixels + (long)((n + n2 * this.getWidth()) * n5)), (int)n3, (int)n4, (int)(this.getWidth() * n5), (long)nativeImage.pixels, (int)nativeImage.getWidth(), (int)nativeImage.getHeight(), (int)0, (int)n5);
    }

    public void untrack() {
        DebugMemoryUntracker.untrack(this.pixels);
    }

    public long getPointer() {
        return this.pixels;
    }

    public static enum Format {
        RGBA(4, true, true, true, false, true, 0, 8, 16, 255, 24, true),
        RGB(3, true, true, true, false, false, 0, 8, 16, 255, 255, true),
        LUMINANCE_ALPHA(2, false, false, false, true, true, 255, 255, 255, 0, 8, true),
        LUMINANCE(1, false, false, false, true, false, 0, 0, 0, 0, 255, true);

        final int components;
        private final boolean hasRed;
        private final boolean hasGreen;
        private final boolean hasBlue;
        private final boolean hasLuminance;
        private final boolean hasAlpha;
        private final int redOffset;
        private final int greenOffset;
        private final int blueOffset;
        private final int luminanceOffset;
        private final int alphaOffset;
        private final boolean supportedByStb;

        private Format(int n2, boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5, int n3, int n4, int n5, int n6, int n7, boolean bl6) {
            this.components = n2;
            this.hasRed = bl;
            this.hasGreen = bl2;
            this.hasBlue = bl3;
            this.hasLuminance = bl4;
            this.hasAlpha = bl5;
            this.redOffset = n3;
            this.greenOffset = n4;
            this.blueOffset = n5;
            this.luminanceOffset = n6;
            this.alphaOffset = n7;
            this.supportedByStb = bl6;
        }

        public int components() {
            return this.components;
        }

        public boolean hasRed() {
            return this.hasRed;
        }

        public boolean hasGreen() {
            return this.hasGreen;
        }

        public boolean hasBlue() {
            return this.hasBlue;
        }

        public boolean hasLuminance() {
            return this.hasLuminance;
        }

        public boolean hasAlpha() {
            return this.hasAlpha;
        }

        public int redOffset() {
            return this.redOffset;
        }

        public int greenOffset() {
            return this.greenOffset;
        }

        public int blueOffset() {
            return this.blueOffset;
        }

        public int luminanceOffset() {
            return this.luminanceOffset;
        }

        public int alphaOffset() {
            return this.alphaOffset;
        }

        public boolean hasLuminanceOrRed() {
            return this.hasLuminance || this.hasRed;
        }

        public boolean hasLuminanceOrGreen() {
            return this.hasLuminance || this.hasGreen;
        }

        public boolean hasLuminanceOrBlue() {
            return this.hasLuminance || this.hasBlue;
        }

        public boolean hasLuminanceOrAlpha() {
            return this.hasLuminance || this.hasAlpha;
        }

        public int luminanceOrRedOffset() {
            return this.hasLuminance ? this.luminanceOffset : this.redOffset;
        }

        public int luminanceOrGreenOffset() {
            return this.hasLuminance ? this.luminanceOffset : this.greenOffset;
        }

        public int luminanceOrBlueOffset() {
            return this.hasLuminance ? this.luminanceOffset : this.blueOffset;
        }

        public int luminanceOrAlphaOffset() {
            return this.hasLuminance ? this.luminanceOffset : this.alphaOffset;
        }

        public boolean supportedByStb() {
            return this.supportedByStb;
        }

        static Format getStbFormat(int n) {
            switch (n) {
                case 1: {
                    return LUMINANCE;
                }
                case 2: {
                    return LUMINANCE_ALPHA;
                }
                case 3: {
                    return RGB;
                }
            }
            return RGBA;
        }
    }

    static class WriteCallback
    extends STBIWriteCallback {
        private final WritableByteChannel output;
        @Nullable
        private IOException exception;

        WriteCallback(WritableByteChannel writableByteChannel) {
            this.output = writableByteChannel;
        }

        public void invoke(long l, long l2, int n) {
            ByteBuffer byteBuffer = WriteCallback.getData((long)l2, (int)n);
            try {
                this.output.write(byteBuffer);
            }
            catch (IOException iOException) {
                this.exception = iOException;
            }
        }

        public void throwIfException() throws IOException {
            if (this.exception != null) {
                throw this.exception;
            }
        }
    }
}

