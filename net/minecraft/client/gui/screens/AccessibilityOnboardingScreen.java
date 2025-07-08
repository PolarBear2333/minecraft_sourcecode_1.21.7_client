/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.text2speech.Narrator
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens;

import com.mojang.text2speech.Narrator;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CommonButtons;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.FocusableTextWidget;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class AccessibilityOnboardingScreen
extends Screen {
    private static final Component TITLE = Component.translatable("accessibility.onboarding.screen.title");
    private static final Component ONBOARDING_NARRATOR_MESSAGE = Component.translatable("accessibility.onboarding.screen.narrator");
    private static final int PADDING = 4;
    private static final int TITLE_PADDING = 16;
    private static final float FADE_OUT_TIME = 1000.0f;
    private final LogoRenderer logoRenderer;
    private final Options options;
    private final boolean narratorAvailable;
    private boolean hasNarrated;
    private float timer;
    private final Runnable onClose;
    @Nullable
    private FocusableTextWidget textWidget;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, this.initTitleYPos(), 33);
    private float fadeInStart;
    private boolean fadingIn = true;
    private float fadeOutStart;

    public AccessibilityOnboardingScreen(Options options, Runnable runnable) {
        super(TITLE);
        this.options = options;
        this.onClose = runnable;
        this.logoRenderer = new LogoRenderer(true);
        this.narratorAvailable = Minecraft.getInstance().getNarrator().isActive();
    }

    @Override
    public void init() {
        LinearLayout linearLayout = this.layout.addToContents(LinearLayout.vertical());
        linearLayout.defaultCellSetting().alignHorizontallyCenter().padding(4);
        this.textWidget = linearLayout.addChild(new FocusableTextWidget(this.width, this.title, this.font), layoutSettings -> layoutSettings.padding(8));
        AbstractWidget abstractWidget = this.options.narrator().createButton(this.options);
        if (abstractWidget instanceof CycleButton) {
            CycleButton cycleButton;
            this.narratorButton = cycleButton = (CycleButton)abstractWidget;
            this.narratorButton.active = this.narratorAvailable;
            linearLayout.addChild(this.narratorButton);
        }
        linearLayout.addChild(CommonButtons.accessibility(150, button -> this.closeAndSetScreen(new AccessibilityOptionsScreen(this, this.minecraft.options)), false));
        linearLayout.addChild(CommonButtons.language(150, button -> this.closeAndSetScreen(new LanguageSelectScreen((Screen)this, this.minecraft.options, this.minecraft.getLanguageManager())), false));
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_CONTINUE, button -> this.onClose()).build());
        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        if (this.textWidget != null) {
            this.textWidget.containWithin(this.width);
        }
        this.layout.arrangeElements();
    }

    @Override
    protected void setInitialFocus() {
        if (this.narratorAvailable && this.narratorButton != null) {
            this.setInitialFocus(this.narratorButton);
        } else {
            super.setInitialFocus();
        }
    }

    private int initTitleYPos() {
        return 90;
    }

    @Override
    public void onClose() {
        this.fadeOutStart = Util.getMillis();
    }

    private void closeAndSetScreen(Screen screen) {
        this.close(false, () -> this.minecraft.setScreen(screen));
    }

    private void close(boolean bl, Runnable runnable) {
        if (bl) {
            this.options.onboardingAccessibilityFinished();
        }
        Narrator.getNarrator().clear();
        runnable.run();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        float f2;
        float f3;
        super.render(guiGraphics, n, n2, f);
        this.handleInitialNarrationDelay();
        if (this.fadeInStart == 0.0f && this.fadingIn) {
            this.fadeInStart = Util.getMillis();
        }
        if (this.fadeInStart > 0.0f) {
            f3 = ((float)Util.getMillis() - this.fadeInStart) / 2000.0f;
            f2 = 1.0f;
            if (f3 >= 1.0f) {
                this.fadingIn = false;
                this.fadeInStart = 0.0f;
            } else {
                f3 = Mth.clamp(f3, 0.0f, 1.0f);
                f2 = Mth.clampedMap(f3, 0.5f, 1.0f, 0.0f, 1.0f);
            }
            this.fadeWidgets(f2);
        }
        if (this.fadeOutStart > 0.0f) {
            f3 = 1.0f - ((float)Util.getMillis() - this.fadeOutStart) / 1000.0f;
            f2 = 0.0f;
            if (f3 <= 0.0f) {
                this.fadeOutStart = 0.0f;
                this.close(true, this.onClose);
            } else {
                f3 = Mth.clamp(f3, 0.0f, 1.0f);
                f2 = Mth.clampedMap(f3, 0.5f, 1.0f, 0.0f, 1.0f);
            }
            this.fadeWidgets(f2);
        }
        this.logoRenderer.renderLogo(guiGraphics, this.width, 1.0f);
    }

    @Override
    protected void renderPanorama(GuiGraphics guiGraphics, float f) {
        this.minecraft.gameRenderer.getPanorama().render(guiGraphics, this.width, this.height, false);
    }

    private void handleInitialNarrationDelay() {
        if (!this.hasNarrated && this.narratorAvailable) {
            if (this.timer < 40.0f) {
                this.timer += 1.0f;
            } else if (this.minecraft.isWindowActive()) {
                Narrator.getNarrator().say(ONBOARDING_NARRATOR_MESSAGE.getString(), true, 1.0f);
                this.hasNarrated = true;
            }
        }
    }
}

