/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.layouts;

import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.util.Mth;

public abstract class AbstractLayout
implements Layout {
    private int x;
    private int y;
    protected int width;
    protected int height;

    public AbstractLayout(int n, int n2, int n3, int n4) {
        this.x = n;
        this.y = n2;
        this.width = n3;
        this.height = n4;
    }

    @Override
    public void setX(int n) {
        this.visitChildren(layoutElement -> {
            int n2 = layoutElement.getX() + (n - this.getX());
            layoutElement.setX(n2);
        });
        this.x = n;
    }

    @Override
    public void setY(int n) {
        this.visitChildren(layoutElement -> {
            int n2 = layoutElement.getY() + (n - this.getY());
            layoutElement.setY(n2);
        });
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

    protected static abstract class AbstractChildWrapper {
        public final LayoutElement child;
        public final LayoutSettings.LayoutSettingsImpl layoutSettings;

        protected AbstractChildWrapper(LayoutElement layoutElement, LayoutSettings layoutSettings) {
            this.child = layoutElement;
            this.layoutSettings = layoutSettings.getExposed();
        }

        public int getHeight() {
            return this.child.getHeight() + this.layoutSettings.paddingTop + this.layoutSettings.paddingBottom;
        }

        public int getWidth() {
            return this.child.getWidth() + this.layoutSettings.paddingLeft + this.layoutSettings.paddingRight;
        }

        public void setX(int n, int n2) {
            float f = this.layoutSettings.paddingLeft;
            float f2 = n2 - this.child.getWidth() - this.layoutSettings.paddingRight;
            int n3 = (int)Mth.lerp(this.layoutSettings.xAlignment, f, f2);
            this.child.setX(n3 + n);
        }

        public void setY(int n, int n2) {
            float f = this.layoutSettings.paddingTop;
            float f2 = n2 - this.child.getHeight() - this.layoutSettings.paddingBottom;
            int n3 = Math.round(Mth.lerp(this.layoutSettings.yAlignment, f, f2));
            this.child.setY(n3 + n);
        }
    }
}

