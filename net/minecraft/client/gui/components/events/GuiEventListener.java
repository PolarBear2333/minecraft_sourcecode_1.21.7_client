/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.components.events;

import javax.annotation.Nullable;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.TabOrderedElement;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenRectangle;

public interface GuiEventListener
extends TabOrderedElement {
    public static final long DOUBLE_CLICK_THRESHOLD_MS = 250L;

    default public void mouseMoved(double d, double d2) {
    }

    default public boolean mouseClicked(double d, double d2, int n) {
        return false;
    }

    default public boolean mouseReleased(double d, double d2, int n) {
        return false;
    }

    default public boolean mouseDragged(double d, double d2, int n, double d3, double d4) {
        return false;
    }

    default public boolean mouseScrolled(double d, double d2, double d3, double d4) {
        return false;
    }

    default public boolean keyPressed(int n, int n2, int n3) {
        return false;
    }

    default public boolean keyReleased(int n, int n2, int n3) {
        return false;
    }

    default public boolean charTyped(char c, int n) {
        return false;
    }

    @Nullable
    default public ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
        return null;
    }

    default public boolean isMouseOver(double d, double d2) {
        return false;
    }

    public void setFocused(boolean var1);

    public boolean isFocused();

    @Nullable
    default public ComponentPath getCurrentFocusPath() {
        if (this.isFocused()) {
            return ComponentPath.leaf(this);
        }
        return null;
    }

    default public ScreenRectangle getRectangle() {
        return ScreenRectangle.empty();
    }

    default public ScreenRectangle getBorderForArrowNavigation(ScreenDirection screenDirection) {
        return this.getRectangle().getBorder(screenDirection);
    }
}

