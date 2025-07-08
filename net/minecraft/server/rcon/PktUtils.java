/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.rcon;

import java.nio.charset.StandardCharsets;

public class PktUtils {
    public static final int MAX_PACKET_SIZE = 1460;
    public static final char[] HEX_CHAR = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String stringFromByteArray(byte[] byArray, int n, int n2) {
        int n3;
        int n4 = n2 - 1;
        int n5 = n3 = n > n4 ? n4 : n;
        while (0 != byArray[n3] && n3 < n4) {
            ++n3;
        }
        return new String(byArray, n, n3 - n, StandardCharsets.UTF_8);
    }

    public static int intFromByteArray(byte[] byArray, int n) {
        return PktUtils.intFromByteArray(byArray, n, byArray.length);
    }

    public static int intFromByteArray(byte[] byArray, int n, int n2) {
        if (0 > n2 - n - 4) {
            return 0;
        }
        return byArray[n + 3] << 24 | (byArray[n + 2] & 0xFF) << 16 | (byArray[n + 1] & 0xFF) << 8 | byArray[n] & 0xFF;
    }

    public static int intFromNetworkByteArray(byte[] byArray, int n, int n2) {
        if (0 > n2 - n - 4) {
            return 0;
        }
        return byArray[n] << 24 | (byArray[n + 1] & 0xFF) << 16 | (byArray[n + 2] & 0xFF) << 8 | byArray[n + 3] & 0xFF;
    }

    public static String toHexString(byte by) {
        return "" + HEX_CHAR[(by & 0xF0) >>> 4] + HEX_CHAR[by & 0xF];
    }
}

