/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.contextualbar;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.contextualbar.ContextualBarRenderer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;

public class ExperienceBarRenderer
implements ContextualBarRenderer {
    private static final ResourceLocation EXPERIENCE_BAR_BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("hud/experience_bar_background");
    private static final ResourceLocation EXPERIENCE_BAR_PROGRESS_SPRITE = ResourceLocation.withDefaultNamespace("hud/experience_bar_progress");
    private final Minecraft minecraft;

    public ExperienceBarRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        LocalPlayer localPlayer = this.minecraft.player;
        int n = this.left(this.minecraft.getWindow());
        int n2 = this.top(this.minecraft.getWindow());
        int n3 = localPlayer.getXpNeededForNextLevel();
        if (n3 > 0) {
            int n4 = (int)(localPlayer.experienceProgress * 183.0f);
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, EXPERIENCE_BAR_BACKGROUND_SPRITE, n, n2, 182, 5);
            if (n4 > 0) {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, EXPERIENCE_BAR_PROGRESS_SPRITE, 182, 5, 0, 0, n, n2, n4, 5);
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
    }
}

