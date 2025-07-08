/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.components;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public abstract class ContainerObjectSelectionList<E extends Entry<E>>
extends AbstractSelectionList<E> {
    public ContainerObjectSelectionList(Minecraft minecraft, int n, int n2, int n3, int n4) {
        super(minecraft, n, n2, n3, n4);
    }

    public ContainerObjectSelectionList(Minecraft minecraft, int n, int n2, int n3, int n4, int n5) {
        super(minecraft, n, n2, n3, n4, n5);
    }

    @Override
    @Nullable
    public ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
        if (this.getItemCount() == 0) {
            return null;
        }
        if (focusNavigationEvent instanceof FocusNavigationEvent.ArrowNavigation) {
            ComponentPath componentPath;
            FocusNavigationEvent.ArrowNavigation arrowNavigation = (FocusNavigationEvent.ArrowNavigation)focusNavigationEvent;
            Entry entry2 = (Entry)this.getFocused();
            if (arrowNavigation.direction().getAxis() == ScreenAxis.HORIZONTAL && entry2 != null) {
                return ComponentPath.path(this, entry2.nextFocusPath(focusNavigationEvent));
            }
            int n = -1;
            ScreenDirection screenDirection = arrowNavigation.direction();
            if (entry2 != null) {
                n = entry2.children().indexOf(entry2.getFocused());
            }
            if (n == -1) {
                switch (screenDirection) {
                    case LEFT: {
                        n = Integer.MAX_VALUE;
                        screenDirection = ScreenDirection.DOWN;
                        break;
                    }
                    case RIGHT: {
                        n = 0;
                        screenDirection = ScreenDirection.DOWN;
                        break;
                    }
                    default: {
                        n = 0;
                    }
                }
            }
            Entry entry3 = entry2;
            do {
                if ((entry3 = this.nextEntry(screenDirection, entry -> !entry.children().isEmpty(), entry3)) != null) continue;
                return null;
            } while ((componentPath = entry3.focusPathAtIndex(arrowNavigation, n)) == null);
            return ComponentPath.path(this, componentPath);
        }
        return super.nextFocusPath(focusNavigationEvent);
    }

    @Override
    public void setFocused(@Nullable GuiEventListener guiEventListener) {
        if (this.getFocused() == guiEventListener) {
            return;
        }
        super.setFocused(guiEventListener);
        if (guiEventListener == null) {
            this.setSelected(null);
        }
    }

    @Override
    public NarratableEntry.NarrationPriority narrationPriority() {
        if (this.isFocused()) {
            return NarratableEntry.NarrationPriority.FOCUSED;
        }
        return super.narrationPriority();
    }

    @Override
    protected boolean isSelectedItem(int n) {
        return false;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        Entry entry = (Entry)this.getHovered();
        if (entry != null) {
            entry.updateNarration(narrationElementOutput.nest());
            this.narrateListElementPosition(narrationElementOutput, entry);
        } else {
            Entry entry2 = (Entry)this.getFocused();
            if (entry2 != null) {
                entry2.updateNarration(narrationElementOutput.nest());
                this.narrateListElementPosition(narrationElementOutput, entry2);
            }
        }
        narrationElementOutput.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.component_list.usage"));
    }

    public static abstract class Entry<E extends Entry<E>>
    extends AbstractSelectionList.Entry<E>
    implements ContainerEventHandler {
        @Nullable
        private GuiEventListener focused;
        @Nullable
        private NarratableEntry lastNarratable;
        private boolean dragging;

        @Override
        public boolean isDragging() {
            return this.dragging;
        }

        @Override
        public void setDragging(boolean bl) {
            this.dragging = bl;
        }

        @Override
        public boolean mouseClicked(double d, double d2, int n) {
            return ContainerEventHandler.super.mouseClicked(d, d2, n);
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
        public GuiEventListener getFocused() {
            return this.focused;
        }

        @Nullable
        public ComponentPath focusPathAtIndex(FocusNavigationEvent focusNavigationEvent, int n) {
            if (this.children().isEmpty()) {
                return null;
            }
            ComponentPath componentPath = this.children().get(Math.min(n, this.children().size() - 1)).nextFocusPath(focusNavigationEvent);
            return ComponentPath.path(this, componentPath);
        }

        @Override
        @Nullable
        public ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
            if (focusNavigationEvent instanceof FocusNavigationEvent.ArrowNavigation) {
                int n;
                int n2;
                FocusNavigationEvent.ArrowNavigation arrowNavigation = (FocusNavigationEvent.ArrowNavigation)focusNavigationEvent;
                switch (arrowNavigation.direction()) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case UP: 
                    case DOWN: {
                        int n3 = 0;
                        break;
                    }
                    case LEFT: {
                        int n3 = -1;
                        break;
                    }
                    case RIGHT: {
                        int n3 = n2 = 1;
                    }
                }
                if (n2 == 0) {
                    return null;
                }
                for (int i = n = Mth.clamp(n2 + this.children().indexOf(this.getFocused()), 0, this.children().size() - 1); i >= 0 && i < this.children().size(); i += n2) {
                    GuiEventListener guiEventListener = this.children().get(i);
                    ComponentPath componentPath = guiEventListener.nextFocusPath(focusNavigationEvent);
                    if (componentPath == null) continue;
                    return ComponentPath.path(this, componentPath);
                }
            }
            return ContainerEventHandler.super.nextFocusPath(focusNavigationEvent);
        }

        public abstract List<? extends NarratableEntry> narratables();

        void updateNarration(NarrationElementOutput narrationElementOutput) {
            List<NarratableEntry> list = this.narratables();
            Screen.NarratableSearchResult narratableSearchResult = Screen.findNarratableWidget(list, this.lastNarratable);
            if (narratableSearchResult != null) {
                if (narratableSearchResult.priority.isTerminal()) {
                    this.lastNarratable = narratableSearchResult.entry;
                }
                if (list.size() > 1) {
                    narrationElementOutput.add(NarratedElementType.POSITION, (Component)Component.translatable("narrator.position.object_list", narratableSearchResult.index + 1, list.size()));
                    if (narratableSearchResult.priority == NarratableEntry.NarrationPriority.FOCUSED) {
                        narrationElementOutput.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.component_list.usage"));
                    }
                }
                narratableSearchResult.entry.updateNarration(narrationElementOutput.nest());
            }
        }
    }
}

