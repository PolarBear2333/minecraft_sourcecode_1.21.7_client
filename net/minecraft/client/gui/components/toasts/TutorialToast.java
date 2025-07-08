/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.components.toasts;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

public class TutorialToast
implements Toast {
    private static final ResourceLocation BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("toast/tutorial");
    public static final int PROGRESS_BAR_WIDTH = 154;
    public static final int PROGRESS_BAR_HEIGHT = 1;
    public static final int PROGRESS_BAR_X = 3;
    public static final int PROGRESS_BAR_MARGIN_BOTTOM = 4;
    private static final int PADDING_TOP = 7;
    private static final int PADDING_BOTTOM = 3;
    private static final int LINE_SPACING = 11;
    private static final int TEXT_LEFT = 30;
    private static final int TEXT_WIDTH = 126;
    private final Icons icon;
    private final List<FormattedCharSequence> lines;
    private Toast.Visibility visibility = Toast.Visibility.SHOW;
    private long lastSmoothingTime;
    private float smoothedProgress;
    private float progress;
    private final boolean progressable;
    private final int timeToDisplayMs;

    public TutorialToast(Font font, Icons icons, Component component, @Nullable Component component2, boolean bl, int n) {
        this.icon = icons;
        this.lines = new ArrayList<FormattedCharSequence>(2);
        this.lines.addAll(font.split(component.copy().withColor(-11534256), 126));
        if (component2 != null) {
            this.lines.addAll(font.split(component2, 126));
        }
        this.progressable = bl;
        this.timeToDisplayMs = n;
    }

    public TutorialToast(Font font, Icons icons, Component component, @Nullable Component component2, boolean bl) {
        this(font, icons, component, component2, bl, 0);
    }

    @Override
    public Toast.Visibility getWantedVisibility() {
        return this.visibility;
    }

    @Override
    public void update(ToastManager toastManager, long l) {
        if (this.timeToDisplayMs > 0) {
            this.smoothedProgress = this.progress = Math.min((float)l / (float)this.timeToDisplayMs, 1.0f);
            this.lastSmoothingTime = l;
            if (l > (long)this.timeToDisplayMs) {
                this.hide();
            }
        } else if (this.progressable) {
            this.smoothedProgress = Mth.clampedLerp(this.smoothedProgress, this.progress, (float)(l - this.lastSmoothingTime) / 100.0f);
            this.lastSmoothingTime = l;
        }
    }

    @Override
    public int height() {
        return 7 + this.contentHeight() + 3;
    }

    private int contentHeight() {
        return Math.max(this.lines.size(), 2) * 11;
    }

    @Override
    public void render(GuiGraphics guiGraphics, Font font, long l) {
        int n;
        int n2 = this.height();
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND_SPRITE, 0, 0, this.width(), n2);
        this.icon.render(guiGraphics, 6, 6);
        int n3 = this.lines.size() * 11;
        int n4 = 7 + (this.contentHeight() - n3) / 2;
        for (n = 0; n < this.lines.size(); ++n) {
            guiGraphics.drawString(font, this.lines.get(n), 30, n4 + n * 11, -16777216, false);
        }
        if (this.progressable) {
            n = n2 - 4;
            guiGraphics.fill(3, n, 157, n + 1, -1);
            int n5 = this.progress >= this.smoothedProgress ? -16755456 : -11206656;
            guiGraphics.fill(3, n, (int)(3.0f + 154.0f * this.smoothedProgress), n + 1, n5);
        }
    }

    public void hide() {
        this.visibility = Toast.Visibility.HIDE;
    }

    public void updateProgress(float f) {
        this.progress = f;
    }

    public static enum Icons {
        MOVEMENT_KEYS(ResourceLocation.withDefaultNamespace("toast/movement_keys")),
        MOUSE(ResourceLocation.withDefaultNamespace("toast/mouse")),
        TREE(ResourceLocation.withDefaultNamespace("toast/tree")),
        RECIPE_BOOK(ResourceLocation.withDefaultNamespace("toast/recipe_book")),
        WOODEN_PLANKS(ResourceLocation.withDefaultNamespace("toast/wooden_planks")),
        SOCIAL_INTERACTIONS(ResourceLocation.withDefaultNamespace("toast/social_interactions")),
        RIGHT_CLICK(ResourceLocation.withDefaultNamespace("toast/right_click"));

        private final ResourceLocation sprite;

        private Icons(ResourceLocation resourceLocation) {
            this.sprite = resourceLocation;
        }

        public void render(GuiGraphics guiGraphics, int n, int n2) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.sprite, n, n2, 20, 20);
        }
    }
}

