/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package com.mojang.realmsclient.gui.screens;

import javax.annotation.Nullable;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.util.CommonLinks;

public class RealmsParentalConsentScreen
extends RealmsScreen {
    private static final Component MESSAGE = Component.translatable("mco.account.privacy.information");
    private static final int SPACING = 15;
    private final LinearLayout layout = LinearLayout.vertical();
    private final Screen lastScreen;
    @Nullable
    private MultiLineTextWidget textWidget;

    public RealmsParentalConsentScreen(Screen screen) {
        super(GameNarrator.NO_TITLE);
        this.lastScreen = screen;
    }

    @Override
    public void init() {
        this.layout.spacing(15).defaultCellSetting().alignHorizontallyCenter();
        this.textWidget = new MultiLineTextWidget(MESSAGE, this.font).setCentered(true);
        this.layout.addChild(this.textWidget);
        LinearLayout linearLayout = this.layout.addChild(LinearLayout.horizontal().spacing(8));
        MutableComponent mutableComponent = Component.translatable("mco.account.privacy.info.button");
        linearLayout.addChild(Button.builder(mutableComponent, ConfirmLinkScreen.confirmLink((Screen)this, CommonLinks.GDPR)).build());
        linearLayout.addChild(Button.builder(CommonComponents.GUI_BACK, button -> this.onClose()).build());
        this.layout.visitWidgets(guiEventListener -> {
            AbstractWidget cfr_ignored_0 = (AbstractWidget)this.addRenderableWidget(guiEventListener);
        });
        this.repositionElements();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    @Override
    protected void repositionElements() {
        if (this.textWidget != null) {
            this.textWidget.setMaxWidth(this.width - 15);
        }
        this.layout.arrangeElements();
        FrameLayout.centerInRectangle(this.layout, this.getRectangle());
    }

    @Override
    public Component getNarrationMessage() {
        return MESSAGE;
    }
}

