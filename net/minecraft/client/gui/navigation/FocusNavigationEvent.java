/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.navigation;

import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenDirection;

public interface FocusNavigationEvent {
    public ScreenDirection getVerticalDirectionForInitialFocus();

    public record ArrowNavigation(ScreenDirection direction) implements FocusNavigationEvent
    {
        @Override
        public ScreenDirection getVerticalDirectionForInitialFocus() {
            return this.direction.getAxis() == ScreenAxis.VERTICAL ? this.direction : ScreenDirection.DOWN;
        }
    }

    public static class InitialFocus
    implements FocusNavigationEvent {
        @Override
        public ScreenDirection getVerticalDirectionForInitialFocus() {
            return ScreenDirection.DOWN;
        }
    }

    public record TabNavigation(boolean forward) implements FocusNavigationEvent
    {
        @Override
        public ScreenDirection getVerticalDirectionForInitialFocus() {
            return this.forward ? ScreenDirection.DOWN : ScreenDirection.UP;
        }
    }
}

