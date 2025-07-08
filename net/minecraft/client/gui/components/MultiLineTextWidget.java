/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.components;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.OptionalInt;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractStringWidget;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.SingleKeyCache;

public class MultiLineTextWidget
extends AbstractStringWidget {
    private OptionalInt maxWidth = OptionalInt.empty();
    private OptionalInt maxRows = OptionalInt.empty();
    private final SingleKeyCache<CacheKey, MultiLineLabel> cache = Util.singleKeyCache(cacheKey -> {
        if (cacheKey.maxRows.isPresent()) {
            return MultiLineLabel.create(font, cacheKey.maxWidth, cacheKey.maxRows.getAsInt(), cacheKey.message);
        }
        return MultiLineLabel.create(font, cacheKey.message, cacheKey.maxWidth);
    });
    private boolean centered = false;
    private boolean allowHoverComponents = false;
    @Nullable
    private Consumer<Style> componentClickHandler = null;

    public MultiLineTextWidget(Component component, Font font) {
        this(0, 0, component, font);
    }

    public MultiLineTextWidget(int n, int n2, Component component, Font font) {
        super(n, n2, 0, 0, component, font);
        this.active = false;
    }

    @Override
    public MultiLineTextWidget setColor(int n) {
        super.setColor(n);
        return this;
    }

    public MultiLineTextWidget setMaxWidth(int n) {
        this.maxWidth = OptionalInt.of(n);
        return this;
    }

    public MultiLineTextWidget setMaxRows(int n) {
        this.maxRows = OptionalInt.of(n);
        return this;
    }

    public MultiLineTextWidget setCentered(boolean bl) {
        this.centered = bl;
        return this;
    }

    public MultiLineTextWidget configureStyleHandling(boolean bl, @Nullable Consumer<Style> consumer) {
        this.allowHoverComponents = bl;
        this.componentClickHandler = consumer;
        return this;
    }

    @Override
    public int getWidth() {
        return this.cache.getValue(this.getFreshCacheKey()).getWidth();
    }

    @Override
    public int getHeight() {
        return this.cache.getValue(this.getFreshCacheKey()).getLineCount() * this.getFont().lineHeight;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int n, int n2, float f) {
        MultiLineLabel multiLineLabel = this.cache.getValue(this.getFreshCacheKey());
        int n3 = this.getX();
        int n4 = this.getY();
        int n5 = this.getFont().lineHeight;
        int n6 = this.getColor();
        if (this.centered) {
            multiLineLabel.renderCentered(guiGraphics, n3 + this.getWidth() / 2, n4, n5, n6);
        } else {
            multiLineLabel.renderLeftAligned(guiGraphics, n3, n4, n5, n6);
        }
        if (this.allowHoverComponents) {
            Style style = this.getComponentStyleAt(n, n2);
            if (this.isHovered()) {
                guiGraphics.renderComponentHoverEffect(this.getFont(), style, n, n2);
            }
        }
    }

    @Nullable
    private Style getComponentStyleAt(double d, double d2) {
        MultiLineLabel multiLineLabel = this.cache.getValue(this.getFreshCacheKey());
        int n = this.getX();
        int n2 = this.getY();
        int n3 = this.getFont().lineHeight;
        if (this.centered) {
            return multiLineLabel.getStyleAtCentered(n + this.getWidth() / 2, n2, n3, d, d2);
        }
        return multiLineLabel.getStyleAtLeftAligned(n, n2, n3, d, d2);
    }

    @Override
    public void onClick(double d, double d2) {
        Style style;
        if (this.componentClickHandler != null && (style = this.getComponentStyleAt(d, d2)) != null) {
            this.componentClickHandler.accept(style);
            return;
        }
        super.onClick(d, d2);
    }

    private CacheKey getFreshCacheKey() {
        return new CacheKey(this.getMessage(), this.maxWidth.orElse(Integer.MAX_VALUE), this.maxRows);
    }

    @Override
    public /* synthetic */ AbstractStringWidget setColor(int n) {
        return this.setColor(n);
    }

    static final class CacheKey
    extends Record {
        final Component message;
        final int maxWidth;
        final OptionalInt maxRows;

        CacheKey(Component component, int n, OptionalInt optionalInt) {
            this.message = component;
            this.maxWidth = n;
            this.maxRows = optionalInt;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{CacheKey.class, "message;maxWidth;maxRows", "message", "maxWidth", "maxRows"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{CacheKey.class, "message;maxWidth;maxRows", "message", "maxWidth", "maxRows"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{CacheKey.class, "message;maxWidth;maxRows", "message", "maxWidth", "maxRows"}, this, object);
        }

        public Component message() {
            return this.message;
        }

        public int maxWidth() {
            return this.maxWidth;
        }

        public OptionalInt maxRows() {
            return this.maxRows;
        }
    }
}

