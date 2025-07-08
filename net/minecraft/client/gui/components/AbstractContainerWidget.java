/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.components;

import javax.annotation.Nullable;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.network.chat.Component;

public abstract class AbstractContainerWidget
extends AbstractScrollArea
implements ContainerEventHandler {
    @Nullable
    private GuiEventListener focused;
    private boolean isDragging;

    public AbstractContainerWidget(int n, int n2, int n3, int n4, Component component) {
        super(n, n2, n3, n4, component);
    }

    @Override
    public final boolean isDragging() {
        return this.isDragging;
    }

    @Override
    public final void setDragging(boolean bl) {
        this.isDragging = bl;
    }

    @Override
    @Nullable
    public GuiEventListener getFocused() {
        return this.focused;
    }

    @Override
    public void setFocused(@Nullable GuiEventListener guiEventListener) {
        if (this.focused != null) {
            this.focused.setFocused(false);
        }
        if (guiEventListener != null) {
            guiEventListener.setFocused(true);
        }
        this.focused = guiEventListener;
    }

    @Override
    @Nullable
    public ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
        return ContainerEventHandler.super.nextFocusPath(focusNavigationEvent);
    }

    @Override
    public boolean mouseClicked(double d, double d2, int n) {
        boolean bl = this.updateScrolling(d, d2, n);
        return ContainerEventHandler.super.mouseClicked(d, d2, n) || bl;
    }

    @Override
    public boolean mouseReleased(double d, double d2, int n) {
        super.mouseReleased(d, d2, n);
        return ContainerEventHandler.super.mouseReleased(d, d2, n);
    }

    @Override
    public boolean mouseDragged(double d, double d2, int n, double d3, double d4) {
        super.mouseDragged(d, d2, n, d3, d4);
        return ContainerEventHandler.super.mouseDragged(d, d2, n, d3, d4);
    }

    @Override
    public boolean isFocused() {
        return ContainerEventHandler.super.isFocused();
    }

    @Override
    public void setFocused(boolean bl) {
        ContainerEventHandler.super.setFocused(bl);
    }
}

