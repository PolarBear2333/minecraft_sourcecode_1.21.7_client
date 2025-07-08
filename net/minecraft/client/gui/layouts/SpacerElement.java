/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.layouts;

import java.util.function.Consumer;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.LayoutElement;

public class SpacerElement
implements LayoutElement {
    private int x;
    private int y;
    private final int width;
    private final int height;

    public SpacerElement(int n, int n2) {
        this(0, 0, n, n2);
    }

    public SpacerElement(int n, int n2, int n3, int n4) {
        this.x = n;
        this.y = n2;
        this.width = n3;
        this.height = n4;
    }

    public static SpacerElement width(int n) {
        return new SpacerElement(n, 0);
    }

    public static SpacerElement height(int n) {
        return new SpacerElement(0, n);
    }

    @Override
    public void setX(int n) {
        this.x = n;
    }

    @Override
    public void setY(int n) {
        this.y = n;
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getY() {
        return this.y;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public void visitWidgets(Consumer<AbstractWidget> consumer) {
    }
}

