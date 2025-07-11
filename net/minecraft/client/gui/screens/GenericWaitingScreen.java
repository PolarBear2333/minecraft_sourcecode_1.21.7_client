/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens;

import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.LoadingDotsText;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class GenericWaitingScreen
extends Screen {
    private static final int TITLE_Y = 80;
    private static final int MESSAGE_Y = 120;
    private static final int MESSAGE_MAX_WIDTH = 360;
    @Nullable
    private final Component messageText;
    private final Component buttonLabel;
    private final Runnable buttonCallback;
    @Nullable
    private MultiLineLabel message;
    private Button button;
    private int disableButtonTicks;

    public static GenericWaitingScreen createWaiting(Component component, Component component2, Runnable runnable) {
        return new GenericWaitingScreen(component, null, component2, runnable, 0);
    }

    public static GenericWaitingScreen createCompleted(Component component, Component component2, Component component3, Runnable runnable) {
        return new GenericWaitingScreen(component, component2, component3, runnable, 20);
    }

    protected GenericWaitingScreen(Component component, @Nullable Component component2, Component component3, Runnable runnable, int n) {
        super(component);
        this.messageText = component2;
        this.buttonLabel = component3;
        this.buttonCallback = runnable;
        this.disableButtonTicks = n;
    }

    @Override
    protected void init() {
        super.init();
        if (this.messageText != null) {
            this.message = MultiLineLabel.create(this.font, this.messageText, 360);
        }
        int n = 150;
        int n2 = 20;
        int n3 = this.message != null ? this.message.getLineCount() : 1;
        int n4 = Math.max(n3, 5) * this.font.lineHeight;
        int n5 = Math.min(120 + n4, this.height - 40);
        this.button = this.addRenderableWidget(Button.builder(this.buttonLabel, button -> this.onClose()).bounds((this.width - 150) / 2, n5, 150, 20).build());
    }

    @Override
    public void tick() {
        if (this.disableButtonTicks > 0) {
            --this.disableButtonTicks;
        }
        this.button.active = this.disableButtonTicks == 0;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        super.render(guiGraphics, n, n2, f);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 80, -1);
        if (this.message == null) {
            String string = LoadingDotsText.get(Util.getMillis());
            guiGraphics.drawCenteredString(this.font, string, this.width / 2, 120, -6250336);
        } else {
            this.message.renderCentered(guiGraphics, this.width / 2, 120);
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return this.message != null && this.button.active;
    }

    @Override
    public void onClose() {
        this.buttonCallback.run();
    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinForNarration(this.title, this.messageText != null ? this.messageText : CommonComponents.EMPTY);
    }
}

