/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;

public class PlayerFaceRenderer {
    public static final int SKIN_HEAD_U = 8;
    public static final int SKIN_HEAD_V = 8;
    public static final int SKIN_HEAD_WIDTH = 8;
    public static final int SKIN_HEAD_HEIGHT = 8;
    public static final int SKIN_HAT_U = 40;
    public static final int SKIN_HAT_V = 8;
    public static final int SKIN_HAT_WIDTH = 8;
    public static final int SKIN_HAT_HEIGHT = 8;
    public static final int SKIN_TEX_WIDTH = 64;
    public static final int SKIN_TEX_HEIGHT = 64;

    public static void draw(GuiGraphics guiGraphics, PlayerSkin playerSkin, int n, int n2, int n3) {
        PlayerFaceRenderer.draw(guiGraphics, playerSkin, n, n2, n3, -1);
    }

    public static void draw(GuiGraphics guiGraphics, PlayerSkin playerSkin, int n, int n2, int n3, int n4) {
        PlayerFaceRenderer.draw(guiGraphics, playerSkin.texture(), n, n2, n3, true, false, n4);
    }

    public static void draw(GuiGraphics guiGraphics, ResourceLocation resourceLocation, int n, int n2, int n3, boolean bl, boolean bl2, int n4) {
        int n5 = 8 + (bl2 ? 8 : 0);
        int n6 = 8 * (bl2 ? -1 : 1);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, resourceLocation, n, n2, 8.0f, n5, n3, n3, 8, n6, 64, 64, n4);
        if (bl) {
            PlayerFaceRenderer.drawHat(guiGraphics, resourceLocation, n, n2, n3, bl2, n4);
        }
    }

    private static void drawHat(GuiGraphics guiGraphics, ResourceLocation resourceLocation, int n, int n2, int n3, boolean bl, int n4) {
        int n5 = 8 + (bl ? 8 : 0);
        int n6 = 8 * (bl ? -1 : 1);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, resourceLocation, n, n2, 40.0f, n5, n3, n3, 8, n6, 64, 64, n4);
    }
}

