/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.components;

import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;

public class StateSwitchingButton
extends AbstractWidget {
    @Nullable
    protected WidgetSprites sprites;
    protected boolean isStateTriggered;

    public StateSwitchingButton(int n, int n2, int n3, int n4, boolean bl) {
        super(n, n2, n3, n4, CommonComponents.EMPTY);
        this.isStateTriggered = bl;
    }

    public void initTextureValues(WidgetSprites widgetSprites) {
        this.sprites = widgetSprites;
    }

    public void setStateTriggered(boolean bl) {
        this.isStateTriggered = bl;
    }

    public boolean isStateTriggered() {
        return this.isStateTriggered;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int n, int n2, float f) {
        if (this.sprites == null) {
            return;
        }
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.sprites.get(this.isStateTriggered, this.isHoveredOrFocused()), this.getX(), this.getY(), this.width, this.height);
    }
}

