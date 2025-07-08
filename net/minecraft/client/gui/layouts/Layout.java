/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.layouts;

import java.util.function.Consumer;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.LayoutElement;

public interface Layout
extends LayoutElement {
    public void visitChildren(Consumer<LayoutElement> var1);

    @Override
    default public void visitWidgets(Consumer<AbstractWidget> consumer) {
        this.visitChildren(layoutElement -> layoutElement.visitWidgets(consumer));
    }

    default public void arrangeElements() {
        this.visitChildren(layoutElement -> {
            if (layoutElement instanceof Layout) {
                Layout layout = (Layout)layoutElement;
                layout.arrangeElements();
            }
        });
    }
}

