/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.contextualbar;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public interface ContextualBarRenderer {
    public static final int WIDTH = 182;
    public static final int HEIGHT = 5;
    public static final int MARGIN_BOTTOM = 24;
    public static final ContextualBarRenderer EMPTY = new ContextualBarRenderer(){

        @Override
        public void renderBackground(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        }

        @Override
        public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        }
    };

    default public int left(Window window) {
        return (window.getGuiScaledWidth() - 182) / 2;
    }

    default public int top(Window window) {
        return window.getGuiScaledHeight() - 24 - 5;
    }

    public void renderBackground(GuiGraphics var1, DeltaTracker var2);

    public void render(GuiGraphics var1, DeltaTracker var2);

    public static void renderExperienceLevel(GuiGraphics guiGraphics, Font font, int n) {
        MutableComponent mutableComponent = Component.translatable("gui.experience.level", n);
        int n2 = (guiGraphics.guiWidth() - font.width(mutableComponent)) / 2;
        int n3 = guiGraphics.guiHeight() - 24 - font.lineHeight - 2;
        guiGraphics.drawString(font, mutableComponent, n2 + 1, n3, -16777216, false);
        guiGraphics.drawString(font, mutableComponent, n2 - 1, n3, -16777216, false);
        guiGraphics.drawString(font, mutableComponent, n2, n3 + 1, -16777216, false);
        guiGraphics.drawString(font, mutableComponent, n2, n3 - 1, -16777216, false);
        guiGraphics.drawString(font, mutableComponent, n2, n3, -8323296, false);
    }
}

