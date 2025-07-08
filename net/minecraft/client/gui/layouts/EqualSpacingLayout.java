/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.layouts;

import com.mojang.math.Divisor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.Util;
import net.minecraft.client.gui.layouts.AbstractLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;

public class EqualSpacingLayout
extends AbstractLayout {
    private final Orientation orientation;
    private final List<ChildContainer> children = new ArrayList<ChildContainer>();
    private final LayoutSettings defaultChildLayoutSettings = LayoutSettings.defaults();

    public EqualSpacingLayout(int n, int n2, Orientation orientation) {
        this(0, 0, n, n2, orientation);
    }

    public EqualSpacingLayout(int n, int n2, int n3, int n4, Orientation orientation) {
        super(n, n2, n3, n4);
        this.orientation = orientation;
    }

    @Override
    public void arrangeElements() {
        super.arrangeElements();
        if (this.children.isEmpty()) {
            return;
        }
        int n = 0;
        int n2 = this.orientation.getSecondaryLength(this);
        for (ChildContainer childContainer : this.children) {
            n += this.orientation.getPrimaryLength(childContainer);
            n2 = Math.max(n2, this.orientation.getSecondaryLength(childContainer));
        }
        int n3 = this.orientation.getPrimaryLength(this) - n;
        int n4 = this.orientation.getPrimaryPosition(this);
        Iterator<ChildContainer> iterator = this.children.iterator();
        ChildContainer childContainer = iterator.next();
        this.orientation.setPrimaryPosition(childContainer, n4);
        n4 += this.orientation.getPrimaryLength(childContainer);
        if (this.children.size() >= 2) {
            Divisor divisor = new Divisor(n3, this.children.size() - 1);
            while (divisor.hasNext()) {
                ChildContainer childContainer2 = iterator.next();
                this.orientation.setPrimaryPosition(childContainer2, n4 += divisor.nextInt());
                n4 += this.orientation.getPrimaryLength(childContainer2);
            }
        }
        int n5 = this.orientation.getSecondaryPosition(this);
        for (ChildContainer childContainer3 : this.children) {
            this.orientation.setSecondaryPosition(childContainer3, n5, n2);
        }
        switch (this.orientation.ordinal()) {
            case 0: {
                this.height = n2;
                break;
            }
            case 1: {
                this.width = n2;
            }
        }
    }

    @Override
    public void visitChildren(Consumer<LayoutElement> consumer) {
        this.children.forEach(childContainer -> consumer.accept(childContainer.child));
    }

    public LayoutSettings newChildLayoutSettings() {
        return this.defaultChildLayoutSettings.copy();
    }

    public LayoutSettings defaultChildLayoutSetting() {
        return this.defaultChildLayoutSettings;
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

    public static enum Orientation {
        HORIZONTAL,
        VERTICAL;


        int getPrimaryLength(LayoutElement layoutElement) {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> layoutElement.getWidth();
                case 1 -> layoutElement.getHeight();
            };
        }

        int getPrimaryLength(ChildContainer childContainer) {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> childContainer.getWidth();
                case 1 -> childContainer.getHeight();
            };
        }

        int getSecondaryLength(LayoutElement layoutElement) {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> layoutElement.getHeight();
                case 1 -> layoutElement.getWidth();
            };
        }

        int getSecondaryLength(ChildContainer childContainer) {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> childContainer.getHeight();
                case 1 -> childContainer.getWidth();
            };
        }

        void setPrimaryPosition(ChildContainer childContainer, int n) {
            switch (this.ordinal()) {
                case 0: {
                    childContainer.setX(n, childContainer.getWidth());
                    break;
                }
                case 1: {
                    childContainer.setY(n, childContainer.getHeight());
                }
            }
        }

        void setSecondaryPosition(ChildContainer childContainer, int n, int n2) {
            switch (this.ordinal()) {
                case 0: {
                    childContainer.setY(n, n2);
                    break;
                }
                case 1: {
                    childContainer.setX(n, n2);
                }
            }
        }

        int getPrimaryPosition(LayoutElement layoutElement) {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> layoutElement.getX();
                case 1 -> layoutElement.getY();
            };
        }

        int getSecondaryPosition(LayoutElement layoutElement) {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> layoutElement.getY();
                case 1 -> layoutElement.getX();
            };
        }
    }

    static class ChildContainer
    extends AbstractLayout.AbstractChildWrapper {
        protected ChildContainer(LayoutElement layoutElement, LayoutSettings layoutSettings) {
            super(layoutElement, layoutSettings);
        }
    }
}

