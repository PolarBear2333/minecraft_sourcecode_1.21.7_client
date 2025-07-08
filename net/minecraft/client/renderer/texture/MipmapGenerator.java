/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.Util;
import net.minecraft.util.ARGB;

public class MipmapGenerator {
    private static final int ALPHA_CUTOUT_CUTOFF = 96;
    private static final float[] POW22 = Util.make(new float[256], fArray -> {
        for (int i = 0; i < ((float[])fArray).length; ++i) {
            fArray[i] = (float)Math.pow((float)i / 255.0f, 2.2);
        }
    });

    private MipmapGenerator() {
    }

    public static NativeImage[] generateMipLevels(NativeImage[] nativeImageArray, int n) {
        if (n + 1 <= nativeImageArray.length) {
            return nativeImageArray;
        }
        NativeImage[] nativeImageArray2 = new NativeImage[n + 1];
        nativeImageArray2[0] = nativeImageArray[0];
        boolean bl = MipmapGenerator.hasTransparentPixel(nativeImageArray2[0]);
        for (int i = 1; i <= n; ++i) {
            if (i < nativeImageArray.length) {
                nativeImageArray2[i] = nativeImageArray[i];
                continue;
            }
            NativeImage nativeImage = nativeImageArray2[i - 1];
            NativeImage nativeImage2 = new NativeImage(nativeImage.getWidth() >> 1, nativeImage.getHeight() >> 1, false);
            int n2 = nativeImage2.getWidth();
            int n3 = nativeImage2.getHeight();
            for (int j = 0; j < n2; ++j) {
                for (int k = 0; k < n3; ++k) {
                    nativeImage2.setPixel(j, k, MipmapGenerator.alphaBlend(nativeImage.getPixel(j * 2 + 0, k * 2 + 0), nativeImage.getPixel(j * 2 + 1, k * 2 + 0), nativeImage.getPixel(j * 2 + 0, k * 2 + 1), nativeImage.getPixel(j * 2 + 1, k * 2 + 1), bl));
                }
            }
            nativeImageArray2[i] = nativeImage2;
        }
        return nativeImageArray2;
    }

    private static boolean hasTransparentPixel(NativeImage nativeImage) {
        for (int i = 0; i < nativeImage.getWidth(); ++i) {
            for (int j = 0; j < nativeImage.getHeight(); ++j) {
                if (ARGB.alpha(nativeImage.getPixel(i, j)) != 0) continue;
                return true;
            }
        }
        return false;
    }

    private static int alphaBlend(int n, int n2, int n3, int n4, boolean bl) {
        if (bl) {
            float f = 0.0f;
            float f2 = 0.0f;
            float f3 = 0.0f;
            float f4 = 0.0f;
            if (n >> 24 != 0) {
                f += MipmapGenerator.getPow22(n >> 24);
                f2 += MipmapGenerator.getPow22(n >> 16);
                f3 += MipmapGenerator.getPow22(n >> 8);
                f4 += MipmapGenerator.getPow22(n >> 0);
            }
            if (n2 >> 24 != 0) {
                f += MipmapGenerator.getPow22(n2 >> 24);
                f2 += MipmapGenerator.getPow22(n2 >> 16);
                f3 += MipmapGenerator.getPow22(n2 >> 8);
                f4 += MipmapGenerator.getPow22(n2 >> 0);
            }
            if (n3 >> 24 != 0) {
                f += MipmapGenerator.getPow22(n3 >> 24);
                f2 += MipmapGenerator.getPow22(n3 >> 16);
                f3 += MipmapGenerator.getPow22(n3 >> 8);
                f4 += MipmapGenerator.getPow22(n3 >> 0);
            }
            if (n4 >> 24 != 0) {
                f += MipmapGenerator.getPow22(n4 >> 24);
                f2 += MipmapGenerator.getPow22(n4 >> 16);
                f3 += MipmapGenerator.getPow22(n4 >> 8);
                f4 += MipmapGenerator.getPow22(n4 >> 0);
            }
            int n5 = (int)(Math.pow(f /= 4.0f, 0.45454545454545453) * 255.0);
            int n6 = (int)(Math.pow(f2 /= 4.0f, 0.45454545454545453) * 255.0);
            int n7 = (int)(Math.pow(f3 /= 4.0f, 0.45454545454545453) * 255.0);
            int n8 = (int)(Math.pow(f4 /= 4.0f, 0.45454545454545453) * 255.0);
            if (n5 < 96) {
                n5 = 0;
            }
            return ARGB.color(n5, n6, n7, n8);
        }
        int n9 = MipmapGenerator.gammaBlend(n, n2, n3, n4, 24);
        int n10 = MipmapGenerator.gammaBlend(n, n2, n3, n4, 16);
        int n11 = MipmapGenerator.gammaBlend(n, n2, n3, n4, 8);
        int n12 = MipmapGenerator.gammaBlend(n, n2, n3, n4, 0);
        return ARGB.color(n9, n10, n11, n12);
    }

    private static int gammaBlend(int n, int n2, int n3, int n4, int n5) {
        float f = MipmapGenerator.getPow22(n >> n5);
        float f2 = MipmapGenerator.getPow22(n2 >> n5);
        float f3 = MipmapGenerator.getPow22(n3 >> n5);
        float f4 = MipmapGenerator.getPow22(n4 >> n5);
        float f5 = (float)((double)((float)Math.pow((double)(f + f2 + f3 + f4) * 0.25, 0.45454545454545453)));
        return (int)((double)f5 * 255.0);
    }

    private static float getPow22(int n) {
        return POW22[n & 0xFF];
    }
}

