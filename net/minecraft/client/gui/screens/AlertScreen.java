/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class AlertScreen
extends Screen {
    private static final int LABEL_Y = 90;
    private final Component messageText;
    private MultiLineLabel message = MultiLineLabel.EMPTY;
    private final Runnable callback;
    private final Component okButton;
    private final boolean shouldCloseOnEsc;

    public AlertScreen(Runnable runnable, Component component, Component component2) {
        this(runnable, component, component2, CommonComponents.GUI_BACK, true);
    }

    public AlertScreen(Runnable runnable, Component component, Component component2, Component component3, boolean bl) {
        super(component);
        this.callback = runnable;
        this.messageText = component2;
        this.okButton = component3;
        this.shouldCloseOnEsc = bl;
    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinForNarration(super.getNarrationMessage(), this.messageText);
    }

    @Override
    protected void init() {
        super.init();
        this.message = MultiLineLabel.create(this.font, this.messageText, this.width - 50);
        int n = this.message.getLineCount() * this.font.lineHeight;
        int n2 = Mth.clamp(90 + n + 12, this.height / 6 + 96, this.height - 24);
        int n3 = 150;
        this.addRenderableWidget(Button.builder(this.okButton, button -> this.callback.run()).bounds((this.width - 150) / 2, n2, 150, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        super.render(guiGraphics, n, n2, f);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 70, -1);
        this.message.renderCentered(guiGraphics, this.width / 2, 90);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return this.shouldCloseOnEsc;
    }
}

