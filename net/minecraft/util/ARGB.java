/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector3f
 */
package net.minecraft.util;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class ARGB {
    public static int alpha(int n) {
        return n >>> 24;
    }

    public static int red(int n) {
        return n >> 16 & 0xFF;
    }

    public static int green(int n) {
        return n >> 8 & 0xFF;
    }

    public static int blue(int n) {
        return n & 0xFF;
    }

    public static int color(int n, int n2, int n3, int n4) {
        return n << 24 | n2 << 16 | n3 << 8 | n4;
    }

    public static int color(int n, int n2, int n3) {
        return ARGB.color(255, n, n2, n3);
    }

    public static int color(Vec3 vec3) {
        return ARGB.color(ARGB.as8BitChannel((float)vec3.x()), ARGB.as8BitChannel((float)vec3.y()), ARGB.as8BitChannel((float)vec3.z()));
    }

    public static int multiply(int n, int n2) {
        if (n == -1) {
            return n2;
        }
        if (n2 == -1) {
            return n;
        }
        return ARGB.color(ARGB.alpha(n) * ARGB.alpha(n2) / 255, ARGB.red(n) * ARGB.red(n2) / 255, ARGB.green(n) * ARGB.green(n2) / 255, ARGB.blue(n) * ARGB.blue(n2) / 255);
    }

    public static int scaleRGB(int n, float f) {
        return ARGB.scaleRGB(n, f, f, f);
    }

    public static int scaleRGB(int n, float f, float f2, float f3) {
        return ARGB.color(ARGB.alpha(n), Math.clamp((long)((int)((float)ARGB.red(n) * f)), 0, 255), Math.clamp((long)((int)((float)ARGB.green(n) * f2)), 0, 255), Math.clamp((long)((int)((float)ARGB.blue(n) * f3)), 0, 255));
    }

    public static int scaleRGB(int n, int n2) {
        return ARGB.color(ARGB.alpha(n), Math.clamp((long)ARGB.red(n) * (long)n2 / 255L, 0, 255), Math.clamp((long)ARGB.green(n) * (long)n2 / 255L, 0, 255), Math.clamp((long)ARGB.blue(n) * (long)n2 / 255L, 0, 255));
    }

    public static int greyscale(int n) {
        int n2 = (int)((float)ARGB.red(n) * 0.3f + (float)ARGB.green(n) * 0.59f + (float)ARGB.blue(n) * 0.11f);
        return ARGB.color(n2, n2, n2);
    }

    public static int lerp(float f, int n, int n2) {
        int n3 = Mth.lerpInt(f, ARGB.alpha(n), ARGB.alpha(n2));
        int n4 = Mth.lerpInt(f, ARGB.red(n), ARGB.red(n2));
        int n5 = Mth.lerpInt(f, ARGB.green(n), ARGB.green(n2));
        int n6 = Mth.lerpInt(f, ARGB.blue(n), ARGB.blue(n2));
        return ARGB.color(n3, n4, n5, n6);
    }

    public static int opaque(int n) {
        return n | 0xFF000000;
    }

    public static int transparent(int n) {
        return n & 0xFFFFFF;
    }

    public static int color(int n, int n2) {
        return n << 24 | n2 & 0xFFFFFF;
    }

    public static int color(float f, int n) {
        return ARGB.as8BitChannel(f) << 24 | n & 0xFFFFFF;
    }

    public static int white(float f) {
        return ARGB.as8BitChannel(f) << 24 | 0xFFFFFF;
    }

    public static int colorFromFloat(float f, float f2, float f3, float f4) {
        return ARGB.color(ARGB.as8BitChannel(f), ARGB.as8BitChannel(f2), ARGB.as8BitChannel(f3), ARGB.as8BitChannel(f4));
    }

    public static Vector3f vector3fFromRGB24(int n) {
        float f = (float)ARGB.red(n) / 255.0f;
        float f2 = (float)ARGB.green(n) / 255.0f;
        float f3 = (float)ARGB.blue(n) / 255.0f;
        return new Vector3f(f, f2, f3);
    }

    public static int average(int n, int n2) {
        return ARGB.color((ARGB.alpha(n) + ARGB.alpha(n2)) / 2, (ARGB.red(n) + ARGB.red(n2)) / 2, (ARGB.green(n) + ARGB.green(n2)) / 2, (ARGB.blue(n) + ARGB.blue(n2)) / 2);
    }

    public static int as8BitChannel(float f) {
        return Mth.floor(f * 255.0f);
    }

    public static float alphaFloat(int n) {
        return ARGB.from8BitChannel(ARGB.alpha(n));
    }

    public static float redFloat(int n) {
        return ARGB.from8BitChannel(ARGB.red(n));
    }

    public static float greenFloat(int n) {
        return ARGB.from8BitChannel(ARGB.green(n));
    }

    public static float blueFloat(int n) {
        return ARGB.from8BitChannel(ARGB.blue(n));
    }

    private static float from8BitChannel(int n) {
        return (float)n / 255.0f;
    }

    public static int toABGR(int n) {
        return n & 0xFF00FF00 | (n & 0xFF0000) >> 16 | (n & 0xFF) << 16;
    }

    public static int fromABGR(int n) {
        return ARGB.toABGR(n);
    }

    public static int setBrightness(int n, float f) {
        float f2;
        float f3;
        float f4;
        float f5;
        int n2 = ARGB.red(n);
        int n3 = ARGB.green(n);
        int n4 = ARGB.blue(n);
        int n5 = ARGB.alpha(n);
        int n6 = Math.max(Math.max(n2, n3), n4);
        int n7 = Math.min(Math.min(n2, n3), n4);
        float f6 = n6 - n7;
        float f7 = n6 != 0 ? f6 / (float)n6 : 0.0f;
        if (f7 == 0.0f) {
            f5 = 0.0f;
        } else {
            f4 = (float)(n6 - n2) / f6;
            f3 = (float)(n6 - n3) / f6;
            f2 = (float)(n6 - n4) / f6;
            f5 = n2 == n6 ? f2 - f3 : (n3 == n6 ? 2.0f + f4 - f2 : 4.0f + f3 - f4);
            if ((f5 /= 6.0f) < 0.0f) {
                f5 += 1.0f;
            }
        }
        if (f7 == 0.0f) {
            n3 = n4 = Math.round(f * 255.0f);
            n2 = n4;
            return ARGB.color(n5, n2, n3, n4);
        }
        f4 = (f5 - (float)Math.floor(f5)) * 6.0f;
        f3 = f4 - (float)Math.floor(f4);
        f2 = f * (1.0f - f7);
        float f8 = f * (1.0f - f7 * f3);
        float f9 = f * (1.0f - f7 * (1.0f - f3));
        switch ((int)f4) {
            case 0: {
                n2 = Math.round(f * 255.0f);
                n3 = Math.round(f9 * 255.0f);
                n4 = Math.round(f2 * 255.0f);
                break;
            }
            case 1: {
                n2 = Math.round(f8 * 255.0f);
                n3 = Math.round(f * 255.0f);
                n4 = Math.round(f2 * 255.0f);
                break;
            }
            case 2: {
                n2 = Math.round(f2 * 255.0f);
                n3 = Math.round(f * 255.0f);
                n4 = Math.round(f9 * 255.0f);
                break;
            }
            case 3: {
                n2 = Math.round(f2 * 255.0f);
                n3 = Math.round(f8 * 255.0f);
                n4 = Math.round(f * 255.0f);
                break;
            }
            case 4: {
                n2 = Math.round(f9 * 255.0f);
                n3 = Math.round(f2 * 255.0f);
                n4 = Math.round(f * 255.0f);
                break;
            }
            case 5: {
                n2 = Math.round(f * 255.0f);
                n3 = Math.round(f2 * 255.0f);
                n4 = Math.round(f8 * 255.0f);
            }
        }
        return ARGB.color(n5, n2, n3, n4);
    }
}

