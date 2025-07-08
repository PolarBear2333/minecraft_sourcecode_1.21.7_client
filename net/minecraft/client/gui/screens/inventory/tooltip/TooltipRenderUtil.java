/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens.inventory.tooltip;

import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;

public class TooltipRenderUtil {
    private static final ResourceLocation BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("tooltip/background");
    private static final ResourceLocation FRAME_SPRITE = ResourceLocation.withDefaultNamespace("tooltip/frame");
    public static final int MOUSE_OFFSET = 12;
    private static final int PADDING = 3;
    public static final int PADDING_LEFT = 3;
    public static final int PADDING_RIGHT = 3;
    public static final int PADDING_TOP = 3;
    public static final int PADDING_BOTTOM = 3;
    private static final int MARGIN = 9;

    public static void renderTooltipBackground(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, @Nullable ResourceLocation resourceLocation) {
        int n5 = n - 3 - 9;
        int n6 = n2 - 3 - 9;
        int n7 = n3 + 3 + 3 + 18;
        int n8 = n4 + 3 + 3 + 18;
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, TooltipRenderUtil.getBackgroundSprite(resourceLocation), n5, n6, n7, n8);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, TooltipRenderUtil.getFrameSprite(resourceLocation), n5, n6, n7, n8);
    }

    private static ResourceLocation getBackgroundSprite(@Nullable ResourceLocation resourceLocation) {
        if (resourceLocation == null) {
            return BACKGROUND_SPRITE;
        }
        return resourceLocation.withPath(string -> "tooltip/" + string + "_background");
    }

    private static ResourceLocation getFrameSprite(@Nullable ResourceLocation resourceLocation) {
        if (resourceLocation == null) {
            return FRAME_SPRITE;
        }
        return resourceLocation.withPath(string -> "tooltip/" + string + "_frame");
    }
}

