/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.layouts;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.Util;
import net.minecraft.client.gui.layouts.AbstractLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.util.Mth;

public class FrameLayout
extends AbstractLayout {
    private final List<ChildContainer> children = new ArrayList<ChildContainer>();
    private int minWidth;
    private int minHeight;
    private final LayoutSettings defaultChildLayoutSettings = LayoutSettings.defaults().align(0.5f, 0.5f);

    public FrameLayout() {
        this(0, 0, 0, 0);
    }

    public FrameLayout(int n, int n2) {
        this(0, 0, n, n2);
    }

    public FrameLayout(int n, int n2, int n3, int n4) {
        super(n, n2, n3, n4);
        this.setMinDimensions(n3, n4);
    }

    public FrameLayout setMinDimensions(int n, int n2) {
        return this.setMinWidth(n).setMinHeight(n2);
    }

    public FrameLayout setMinHeight(int n) {
        this.minHeight = n;
        return this;
    }

    public FrameLayout setMinWidth(int n) {
        this.minWidth = n;
        return this;
    }

    public LayoutSettings newChildLayoutSettings() {
        return this.defaultChildLayoutSettings.copy();
    }

    public LayoutSettings defaultChildLayoutSetting() {
        return this.defaultChildLayoutSettings;
    }

    @Override
    public void arrangeElements() {
        super.arrangeElements();
        int n = this.minWidth;
        int n2 = this.minHeight;
        for (ChildContainer childContainer : this.children) {
            n = Math.max(n, childContainer.getWidth());
            n2 = Math.max(n2, childContainer.getHeight());
        }
        for (ChildContainer childContainer : this.children) {
            childContainer.setX(this.getX(), n);
            childContainer.setY(this.getY(), n2);
        }
        this.width = n;
        this.height = n2;
    }

    public <T extends LayoutElement> T addChild(T t) {
        return this.addChild(t, this.newChildLayoutSettings());
    }

    public <T extends LayoutElement> T addChild(T t, LayoutSettings layoutSettings) {
        this.children.add(new ChildContainer(t, layoutSettings));
        return t;
    }

    public <T extends LayoutElement> T addChild(T t, Consumer<LayoutSettings> consumer) {
        return this.addChild(t, Util.make(this.newChildLayoutSettings(), consumer));
    }

    @Override
    public void visitChildren(Consumer<LayoutElement> consumer) {
        this.children.forEach(childContainer -> consumer.accept(childContainer.child));
    }

    public static void centerInRectangle(LayoutElement layoutElement, int n, int n2, int n3, int n4) {
        FrameLayout.alignInRectangle(layoutElement, n, n2, n3, n4, 0.5f, 0.5f);
    }

    public static void centerInRectangle(LayoutElement layoutElement, ScreenRectangle screenRectangle) {
        FrameLayout.centerInRectangle(layoutElement, screenRectangle.position().x(), screenRectangle.position().y(), screenRectangle.width(), screenRectangle.height());
    }

    public static void alignInRectangle(LayoutElement layoutElement, ScreenRectangle screenRectangle, float f, float f2) {
        FrameLayout.alignInRectangle(layoutElement, screenRectangle.left(), screenRectangle.top(), screenRectangle.width(), screenRectangle.height(), f, f2);
    }

    public static void alignInRectangle(LayoutElement layoutElement, int n, int n2, int n3, int n4, float f, float f2) {
        FrameLayout.alignInDimension(n, n3, layoutElement.getWidth(), layoutElement::setX, f);
        FrameLayout.alignInDimension(n2, n4, layoutElement.getHeight(), layoutElement::setY, f2);
    }

    public static void alignInDimension(int n, int n2, int n3, Consumer<Integer> consumer, float f) {
        int n4 = (int)Mth.lerp(f, 0.0f, n2 - n3);
        consumer.accept(n + n4);
    }

    static class ChildContainer
    extends AbstractLayout.AbstractChildWrapper {
        protected ChildContainer(LayoutElement layoutElement, LayoutSettings layoutSettings) {
            super(layoutElement, layoutSettings);
        }
    }
}

