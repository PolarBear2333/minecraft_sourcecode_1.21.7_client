/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.contextualbar;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.contextualbar.ContextualBarRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.WaypointStyle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.waypoints.TrackedWaypoint;
import net.minecraft.world.waypoints.Waypoint;

public class LocatorBarRenderer
implements ContextualBarRenderer {
    private static final ResourceLocation LOCATOR_BAR_BACKGROUND = ResourceLocation.withDefaultNamespace("hud/locator_bar_background");
    private static final ResourceLocation LOCATOR_BAR_ARROW_UP = ResourceLocation.withDefaultNamespace("hud/locator_bar_arrow_up");
    private static final ResourceLocation LOCATOR_BAR_ARROW_DOWN = ResourceLocation.withDefaultNamespace("hud/locator_bar_arrow_down");
    private static final int DOT_SIZE = 9;
    private static final int VISIBLE_DEGREE_RANGE = 60;
    private static final int ARROW_WIDTH = 7;
    private static final int ARROW_HEIGHT = 5;
    private static final int ARROW_LEFT = 1;
    private static final int ARROW_PADDING = 1;
    private final Minecraft minecraft;

    public LocatorBarRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, LOCATOR_BAR_BACKGROUND, this.left(this.minecraft.getWindow()), this.top(this.minecraft.getWindow()), 182, 5);
    }

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        int n = this.top(this.minecraft.getWindow());
        Level level = this.minecraft.cameraEntity.level();
        this.minecraft.player.connection.getWaypointManager().forEachWaypoint(this.minecraft.cameraEntity, trackedWaypoint -> {
            if (trackedWaypoint.id().left().map(uUID -> uUID.equals(this.minecraft.cameraEntity.getUUID())).orElse(false).booleanValue()) {
                return;
            }
            double d = trackedWaypoint.yawAngleToCamera(level, this.minecraft.gameRenderer.getMainCamera());
            if (d <= -61.0 || d > 60.0) {
                return;
            }
            int n2 = Mth.ceil((float)(guiGraphics.guiWidth() - 9) / 2.0f);
            Waypoint.Icon icon = trackedWaypoint.icon();
            WaypointStyle waypointStyle = this.minecraft.getWaypointStyles().get(icon.style);
            float f = Mth.sqrt((float)trackedWaypoint.distanceSquared(this.minecraft.cameraEntity));
            ResourceLocation resourceLocation = waypointStyle.sprite(f);
            int n3 = icon.color.orElseGet(() -> (Integer)trackedWaypoint.id().map(uUID -> ARGB.setBrightness(ARGB.color(255, uUID.hashCode()), 0.9f), string -> ARGB.setBrightness(ARGB.color(255, string.hashCode()), 0.9f)));
            int n4 = (int)(d * 173.0 / 2.0 / 60.0);
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocation, n2 + n4, n - 2, 9, 9, n3);
            TrackedWaypoint.PitchDirection pitchDirection = trackedWaypoint.pitchDirectionToCamera(level, this.minecraft.gameRenderer);
            if (pitchDirection != TrackedWaypoint.PitchDirection.NONE) {
                ResourceLocation resourceLocation2;
                int n5;
                if (pitchDirection == TrackedWaypoint.PitchDirection.DOWN) {
                    n5 = 6;
                    resourceLocation2 = LOCATOR_BAR_ARROW_DOWN;
                } else {
                    n5 = -6;
                    resourceLocation2 = LOCATOR_BAR_ARROW_UP;
                }
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocation2, n2 + n4 + 1, n + n5, 7, 5);
            }
        });
    }
}

