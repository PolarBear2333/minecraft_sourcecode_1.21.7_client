/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.layouts;

import java.util.function.Consumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class HeaderAndFooterLayout
implements Layout {
    public static final int DEFAULT_HEADER_AND_FOOTER_HEIGHT = 33;
    private static final int CONTENT_MARGIN_TOP = 30;
    private final FrameLayout headerFrame = new FrameLayout();
    private final FrameLayout footerFrame = new FrameLayout();
    private final FrameLayout contentsFrame = new FrameLayout();
    private final Screen screen;
    private int headerHeight;
    private int footerHeight;

    public HeaderAndFooterLayout(Screen screen) {
        this(screen, 33);
    }

    public HeaderAndFooterLayout(Screen screen, int n) {
        this(screen, n, n);
    }

    public HeaderAndFooterLayout(Screen screen, int n, int n2) {
        this.screen = screen;
        this.headerHeight = n;
        this.footerHeight = n2;
        this.headerFrame.defaultChildLayoutSetting().align(0.5f, 0.5f);
        this.footerFrame.defaultChildLayoutSetting().align(0.5f, 0.5f);
    }

    @Override
    public void setX(int n) {
    }

    @Override
    public void setY(int n) {
    }

    @Override
    public int getX() {
        return 0;
    }

    @Override
    public int getY() {
        return 0;
    }

    @Override
    public int getWidth() {
        return this.screen.width;
    }

    @Override
    public int getHeight() {
        return this.screen.height;
    }

    public int getFooterHeight() {
        return this.footerHeight;
    }

    public void setFooterHeight(int n) {
        this.footerHeight = n;
    }

    public void setHeaderHeight(int n) {
        this.headerHeight = n;
    }

    public int getHeaderHeight() {
        return this.headerHeight;
    }

    public int getContentHeight() {
        return this.screen.height - this.getHeaderHeight() - this.getFooterHeight();
    }

    @Override
    public void visitChildren(Consumer<LayoutElement> consumer) {
        this.headerFrame.visitChildren(consumer);
        this.contentsFrame.visitChildren(consumer);
        this.footerFrame.visitChildren(consumer);
    }

    @Override
    public void arrangeElements() {
        int n = this.getHeaderHeight();
        int n2 = this.getFooterHeight();
        this.headerFrame.setMinWidth(this.screen.width);
        this.headerFrame.setMinHeight(n);
        this.headerFrame.setPosition(0, 0);
        this.headerFrame.arrangeElements();
        this.footerFrame.setMinWidth(this.screen.width);
        this.footerFrame.setMinHeight(n2);
        this.footerFrame.arrangeElements();
        this.footerFrame.setY(this.screen.height - n2);
        this.contentsFrame.setMinWidth(this.screen.width);
        this.contentsFrame.arrangeElements();
        int n3 = n + 30;
        int n4 = this.screen.height - n2 - this.contentsFrame.getHeight();
        this.contentsFrame.setPosition(0, Math.min(n3, n4));
    }

    public <T extends LayoutElement> T addToHeader(T t) {
        return this.headerFrame.addChild(t);
    }

    public <T extends LayoutElement> T addToHeader(T t, Consumer<LayoutSettings> consumer) {
        return this.headerFrame.addChild(t, consumer);
    }

    public void addTitleHeader(Component component, Font font) {
        this.headerFrame.addChild(new StringWidget(component, font));
    }

    public <T extends LayoutElement> T addToFooter(T t) {
        return this.footerFrame.addChild(t);
    }

    public <T extends LayoutElement> T addToFooter(T t, Consumer<LayoutSettings> consumer) {
        return this.footerFrame.addChild(t, consumer);
    }

    public <T extends LayoutElement> T addToContents(T t) {
        return this.contentsFrame.addChild(t);
    }

    public <T extends LayoutElement> T addToContents(T t, Consumer<LayoutSettings> consumer) {
        return this.contentsFrame.addChild(t, consumer);
    }
}

