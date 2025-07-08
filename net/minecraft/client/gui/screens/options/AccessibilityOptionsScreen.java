/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens.options;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonLinks;
import net.minecraft.world.flag.FeatureFlags;

public class AccessibilityOptionsScreen
extends OptionsSubScreen {
    public static final Component TITLE = Component.translatable("options.accessibility.title");

    private static OptionInstance<?>[] options(Options options) {
        return new OptionInstance[]{options.narrator(), options.showSubtitles(), options.highContrast(), options.autoJump(), options.menuBackgroundBlurriness(), options.textBackgroundOpacity(), options.backgroundForChatOnly(), options.chatOpacity(), options.chatLineSpacing(), options.chatDelay(), options.notificationDisplayTime(), options.bobView(), options.toggleCrouch(), options.toggleSprint(), options.screenEffectScale(), options.fovEffectScale(), options.darknessEffectScale(), options.damageTiltStrength(), options.glintSpeed(), options.glintStrength(), options.hideLightningFlash(), options.darkMojangStudiosBackground(), options.panoramaSpeed(), options.hideSplashTexts(), options.narratorHotkey(), options.rotateWithMinecart(), options.highContrastBlockOutline()};
    }

    public AccessibilityOptionsScreen(Screen screen, Options options) {
        super(screen, options, TITLE);
    }

    @Override
    protected void init() {
        AbstractWidget abstractWidget;
        super.init();
        AbstractWidget abstractWidget2 = this.list.findOption(this.options.highContrast());
        if (abstractWidget2 != null && !this.minecraft.getResourcePackRepository().getAvailableIds().contains("high_contrast")) {
            abstractWidget2.active = false;
            abstractWidget2.setTooltip(Tooltip.create(Component.translatable("options.accessibility.high_contrast.error.tooltip")));
        }
        if ((abstractWidget = this.list.findOption(this.options.rotateWithMinecart())) != null) {
            abstractWidget.active = this.isMinecartOptionEnabled();
        }
    }

    @Override
    protected void addOptions() {
        this.list.addSmall(AccessibilityOptionsScreen.options(this.options));
    }

    @Override
    protected void addFooter() {
        LinearLayout linearLayout = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        linearLayout.addChild(Button.builder(Component.translatable("options.accessibility.link"), ConfirmLinkScreen.confirmLink((Screen)this, CommonLinks.ACCESSIBILITY_HELP)).build());
        linearLayout.addChild(Button.builder(CommonComponents.GUI_DONE, button -> this.minecraft.setScreen(this.lastScreen)).build());
    }

    private boolean isMinecartOptionEnabled() {
        return this.minecraft.level != null && this.minecraft.level.enabledFeatures().contains(FeatureFlags.MINECART_IMPROVEMENTS);
    }
}

