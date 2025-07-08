/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.layouts;

public interface LayoutSettings {
    public LayoutSettings padding(int var1);

    public LayoutSettings padding(int var1, int var2);

    public LayoutSettings padding(int var1, int var2, int var3, int var4);

    public LayoutSettings paddingLeft(int var1);

    public LayoutSettings paddingTop(int var1);

    public LayoutSettings paddingRight(int var1);

    public LayoutSettings paddingBottom(int var1);

    public LayoutSettings paddingHorizontal(int var1);

    public LayoutSettings paddingVertical(int var1);

    public LayoutSettings align(float var1, float var2);

    public LayoutSettings alignHorizontally(float var1);

    public LayoutSettings alignVertically(float var1);

    default public LayoutSettings alignHorizontallyLeft() {
        return this.alignHorizontally(0.0f);
    }

    default public LayoutSettings alignHorizontallyCenter() {
        return this.alignHorizontally(0.5f);
    }

    default public LayoutSettings alignHorizontallyRight() {
        return this.alignHorizontally(1.0f);
    }

    default public LayoutSettings alignVerticallyTop() {
        return this.alignVertically(0.0f);
    }

    default public LayoutSettings alignVerticallyMiddle() {
        return this.alignVertically(0.5f);
    }

    default public LayoutSettings alignVerticallyBottom() {
        return this.alignVertically(1.0f);
    }

    public LayoutSettings copy();

    public LayoutSettingsImpl getExposed();

    public static LayoutSettings defaults() {
        return new LayoutSettingsImpl();
    }

    public static class LayoutSettingsImpl
    implements LayoutSettings {
        public int paddingLeft;
        public int paddingTop;
        public int paddingRight;
        public int paddingBottom;
        public float xAlignment;
        public float yAlignment;

        public LayoutSettingsImpl() {
        }

        public LayoutSettingsImpl(LayoutSettingsImpl layoutSettingsImpl) {
            this.paddingLeft = layoutSettingsImpl.paddingLeft;
            this.paddingTop = layoutSettingsImpl.paddingTop;
            this.paddingRight = layoutSettingsImpl.paddingRight;
            this.paddingBottom = layoutSettingsImpl.paddingBottom;
            this.xAlignment = layoutSettingsImpl.xAlignment;
            this.yAlignment = layoutSettingsImpl.yAlignment;
        }

        @Override
        public LayoutSettingsImpl padding(int n) {
            return this.padding(n, n);
        }

        @Override
        public LayoutSettingsImpl padding(int n, int n2) {
            return this.paddingHorizontal(n).paddingVertical(n2);
        }

        @Override
        public LayoutSettingsImpl padding(int n, int n2, int n3, int n4) {
            return this.paddingLeft(n).paddingRight(n3).paddingTop(n2).paddingBottom(n4);
        }

        @Override
        public LayoutSettingsImpl paddingLeft(int n) {
            this.paddingLeft = n;
            return this;
        }

        @Override
        public LayoutSettingsImpl paddingTop(int n) {
            this.paddingTop = n;
            return this;
        }

        @Override
        public LayoutSettingsImpl paddingRight(int n) {
            this.paddingRight = n;
            return this;
        }

        @Override
        public LayoutSettingsImpl paddingBottom(int n) {
            this.paddingBottom = n;
            return this;
        }

        @Override
        public LayoutSettingsImpl paddingHorizontal(int n) {
            return this.paddingLeft(n).paddingRight(n);
        }

        @Override
        public LayoutSettingsImpl paddingVertical(int n) {
            return this.paddingTop(n).paddingBottom(n);
        }

        @Override
        public LayoutSettingsImpl align(float f, float f2) {
            this.xAlignment = f;
            this.yAlignment = f2;
            return this;
        }

        @Override
        public LayoutSettingsImpl alignHorizontally(float f) {
            this.xAlignment = f;
            return this;
        }

        @Override
        public LayoutSettingsImpl alignVertically(float f) {
            this.yAlignment = f;
            return this;
        }

        @Override
        public LayoutSettingsImpl copy() {
            return new LayoutSettingsImpl(this);
        }

        @Override
        public LayoutSettingsImpl getExposed() {
            return this;
        }

        @Override
        public /* synthetic */ LayoutSettings copy() {
            return this.copy();
        }

        @Override
        public /* synthetic */ LayoutSettings alignVertically(float f) {
            return this.alignVertically(f);
        }

        @Override
        public /* synthetic */ LayoutSettings alignHorizontally(float f) {
            return this.alignHorizontally(f);
        }

        @Override
        public /* synthetic */ LayoutSettings align(float f, float f2) {
            return this.align(f, f2);
        }

        @Override
        public /* synthetic */ LayoutSettings paddingVertical(int n) {
            return this.paddingVertical(n);
        }

        @Override
        public /* synthetic */ LayoutSettings paddingHorizontal(int n) {
            return this.paddingHorizontal(n);
        }

        @Override
        public /* synthetic */ LayoutSettings paddingBottom(int n) {
            return this.paddingBottom(n);
        }

        @Override
        public /* synthetic */ LayoutSettings paddingRight(int n) {
            return this.paddingRight(n);
        }

        @Override
        public /* synthetic */ LayoutSettings paddingTop(int n) {
            return this.paddingTop(n);
        }

        @Override
        public /* synthetic */ LayoutSettings paddingLeft(int n) {
            return this.paddingLeft(n);
        }

        @Override
        public /* synthetic */ LayoutSettings padding(int n, int n2, int n3, int n4) {
            return this.padding(n, n2, n3, n4);
        }

        @Override
        public /* synthetic */ LayoutSettings padding(int n, int n2) {
            return this.padding(n, n2);
        }

        @Override
        public /* synthetic */ LayoutSettings padding(int n) {
            return this.padding(n);
        }
    }
}

