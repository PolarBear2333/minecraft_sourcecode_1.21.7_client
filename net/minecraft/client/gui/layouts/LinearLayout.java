/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.layouts;

import java.util.function.Consumer;
import net.minecraft.Util;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;

public class LinearLayout
implements Layout {
    private final GridLayout wrapped;
    private final Orientation orientation;
    private int nextChildIndex = 0;

    private LinearLayout(Orientation orientation) {
        this(0, 0, orientation);
    }

    public LinearLayout(int n, int n2, Orientation orientation) {
        this.wrapped = new GridLayout(n, n2);
        this.orientation = orientation;
    }

    public LinearLayout spacing(int n) {
        this.orientation.setSpacing(this.wrapped, n);
        return this;
    }

    public LayoutSettings newCellSettings() {
        return this.wrapped.newCellSettings();
    }

    public LayoutSettings defaultCellSetting() {
        return this.wrapped.defaultCellSetting();
    }

    public <T extends LayoutElement> T addChild(T t, LayoutSettings layoutSettings) {
        return this.orientation.addChild(this.wrapped, t, this.nextChildIndex++, layoutSettings);
    }

    public <T extends LayoutElement> T addChild(T t) {
        return this.addChild(t, this.newCellSettings());
    }

    public <T extends LayoutElement> T addChild(T t, Consumer<LayoutSettings> consumer) {
        return this.orientation.addChild(this.wrapped, t, this.nextChildIndex++, Util.make(this.newCellSettings(), consumer));
    }

    @Override
    public void visitChildren(Consumer<LayoutElement> consumer) {
        this.wrapped.visitChildren(consumer);
    }

    @Override
    public void arrangeElements() {
        this.wrapped.arrangeElements();
    }

    @Override
    public int getWidth() {
        return this.wrapped.getWidth();
    }

    @Override
    public int getHeight() {
        return this.wrapped.getHeight();
    }

    @Override
    public void setX(int n) {
        this.wrapped.setX(n);
    }

    @Override
    public void setY(int n) {
        this.wrapped.setY(n);
    }

    @Override
    public int getX() {
        return this.wrapped.getX();
    }

    @Override
    public int getY() {
        return this.wrapped.getY();
    }

    public static LinearLayout vertical() {
        return new LinearLayout(Orientation.VERTICAL);
    }

    public static LinearLayout horizontal() {
        return new LinearLayout(Orientation.HORIZONTAL);
    }

    public static enum Orientation {
        HORIZONTAL,
        VERTICAL;


        void setSpacing(GridLayout gridLayout, int n) {
            switch (this.ordinal()) {
                case 0: {
                    gridLayout.columnSpacing(n);
                    break;
                }
                case 1: {
                    gridLayout.rowSpacing(n);
                }
            }
        }

        public <T extends LayoutElement> T addChild(GridLayout gridLayout, T t, int n, LayoutSettings layoutSettings) {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> gridLayout.addChild(t, 0, n, layoutSettings);
                case 1 -> gridLayout.addChild(t, n, 0, layoutSettings);
            };
        }
    }
}

