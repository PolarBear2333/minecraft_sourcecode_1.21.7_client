/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.layouts;

import java.util.function.Consumer;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.navigation.ScreenRectangle;

public interface LayoutElement {
    public void setX(int var1);

    public void setY(int var1);

    public int getX();

    public int getY();

    public int getWidth();

    public int getHeight();

    default public ScreenRectangle getRectangle() {
        return new ScreenRectangle(this.getX(), this.getY(), this.getWidth(), this.getHeight());
    }

    default public void setPosition(int n, int n2) {
        this.setX(n);
        this.setY(n2);
    }

    public void visitWidgets(Consumer<AbstractWidget> var1);
}

