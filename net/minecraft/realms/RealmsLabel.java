/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.realms;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.network.chat.Component;

public class RealmsLabel
implements Renderable {
    private final Component text;
    private final int x;
    private final int y;
    private final int color;

    public RealmsLabel(Component component, int n, int n2, int n3) {
        this.text = component;
        this.x = n;
        this.y = n2;
        this.color = n3;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, this.text, this.x, this.y, this.color);
    }

    public Component getText() {
        return this.text;
    }
}

