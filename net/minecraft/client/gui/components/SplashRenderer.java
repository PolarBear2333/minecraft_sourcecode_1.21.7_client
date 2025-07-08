/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.components;

import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public class SplashRenderer {
    public static final SplashRenderer CHRISTMAS = new SplashRenderer("Merry X-mas!");
    public static final SplashRenderer NEW_YEAR = new SplashRenderer("Happy new year!");
    public static final SplashRenderer HALLOWEEN = new SplashRenderer("OOoooOOOoooo! Spooky!");
    private static final int WIDTH_OFFSET = 123;
    private static final int HEIGH_OFFSET = 69;
    private final String splash;

    public SplashRenderer(String string) {
        this.splash = string;
    }

    public void render(GuiGraphics guiGraphics, int n, Font font, float f) {
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate((float)n / 2.0f + 123.0f, 69.0f);
        guiGraphics.pose().rotate(-0.34906584f);
        float f2 = 1.8f - Mth.abs(Mth.sin((float)(Util.getMillis() % 1000L) / 1000.0f * ((float)Math.PI * 2)) * 0.1f);
        f2 = f2 * 100.0f / (float)(font.width(this.splash) + 32);
        guiGraphics.pose().scale(f2, f2);
        guiGraphics.drawCenteredString(font, this.splash, 0, -8, ARGB.color(f, -256));
        guiGraphics.pose().popMatrix();
    }
}

