/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 */
package net.minecraft.client.gui.screens.options;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;

public class UnsupportedGraphicsWarningScreen
extends Screen {
    private static final int BUTTON_PADDING = 20;
    private static final int BUTTON_MARGIN = 5;
    private static final int BUTTON_HEIGHT = 20;
    private final Component narrationMessage;
    private final List<Component> message;
    private final ImmutableList<ButtonOption> buttonOptions;
    private MultiLineLabel messageLines = MultiLineLabel.EMPTY;
    private int contentTop;
    private int buttonWidth;

    protected UnsupportedGraphicsWarningScreen(Component component, List<Component> list, ImmutableList<ButtonOption> immutableList) {
        super(component);
        this.message = list;
        this.narrationMessage = CommonComponents.joinForNarration(component, ComponentUtils.formatList(list, CommonComponents.EMPTY));
        this.buttonOptions = immutableList;
    }

    @Override
    public Component getNarrationMessage() {
        return this.narrationMessage;
    }

    @Override
    public void init() {
        for (ButtonOption buttonOption : this.buttonOptions) {
            this.buttonWidth = Math.max(this.buttonWidth, 20 + this.font.width(buttonOption.message) + 20);
        }
        int n = 5 + this.buttonWidth + 5;
        int n2 = n * this.buttonOptions.size();
        this.messageLines = MultiLineLabel.create(this.font, n2, this.message.toArray(new Component[0]));
        int n3 = this.messageLines.getLineCount() * this.font.lineHeight;
        this.contentTop = (int)((double)this.height / 2.0 - (double)n3 / 2.0);
        int n4 = this.contentTop + n3 + this.font.lineHeight * 2;
        int n5 = (int)((double)this.width / 2.0 - (double)n2 / 2.0);
        for (ButtonOption buttonOption : this.buttonOptions) {
            this.addRenderableWidget(Button.builder(buttonOption.message, buttonOption.onPress).bounds(n5, n4, this.buttonWidth, 20).build());
            n5 += n;
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        super.render(guiGraphics, n, n2, f);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.contentTop - this.font.lineHeight * 2, -1);
        this.messageLines.renderCentered(guiGraphics, this.width / 2, this.contentTop);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    public static final class ButtonOption {
        final Component message;
        final Button.OnPress onPress;

        public ButtonOption(Component component, Button.OnPress onPress) {
            this.message = component;
            this.onPress = onPress;
        }
    }
}

