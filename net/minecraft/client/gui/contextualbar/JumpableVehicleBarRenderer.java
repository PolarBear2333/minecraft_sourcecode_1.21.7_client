/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.contextualbar;

import java.util.Objects;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.contextualbar.ContextualBarRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.PlayerRideableJumping;

public class JumpableVehicleBarRenderer
implements ContextualBarRenderer {
    private static final ResourceLocation JUMP_BAR_BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("hud/jump_bar_background");
    private static final ResourceLocation JUMP_BAR_COOLDOWN_SPRITE = ResourceLocation.withDefaultNamespace("hud/jump_bar_cooldown");
    private static final ResourceLocation JUMP_BAR_PROGRESS_SPRITE = ResourceLocation.withDefaultNamespace("hud/jump_bar_progress");
    private final Minecraft minecraft;
    private final PlayerRideableJumping playerJumpableVehicle;

    public JumpableVehicleBarRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
        this.playerJumpableVehicle = Objects.requireNonNull(minecraft.player).jumpableVehicle();
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        int n = this.left(this.minecraft.getWindow());
        int n2 = this.top(this.minecraft.getWindow());
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, JUMP_BAR_BACKGROUND_SPRITE, n, n2, 182, 5);
        if (this.playerJumpableVehicle.getJumpCooldown() > 0) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, JUMP_BAR_COOLDOWN_SPRITE, n, n2, 182, 5);
            return;
        }
        int n3 = (int)(this.minecraft.player.getJumpRidingScale() * 183.0f);
        if (n3 > 0) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, JUMP_BAR_PROGRESS_SPRITE, 182, 5, 0, 0, n, n2, n3, 5);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
    }
}

