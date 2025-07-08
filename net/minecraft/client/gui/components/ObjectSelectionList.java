/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.components;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarrationSupplier;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.network.chat.Component;

public abstract class ObjectSelectionList<E extends Entry<E>>
extends AbstractSelectionList<E> {
    private static final Component USAGE_NARRATION = Component.translatable("narration.selection.usage");

    public ObjectSelectionList(Minecraft minecraft, int n, int n2, int n3, int n4) {
        super(minecraft, n, n2, n3, n4);
    }

    public ObjectSelectionList(Minecraft minecraft, int n, int n2, int n3, int n4, int n5) {
        super(minecraft, n, n2, n3, n4, n5);
    }

    @Override
    @Nullable
    public ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
        if (this.getItemCount() == 0) {
            return null;
        }
        if (this.isFocused() && focusNavigationEvent instanceof FocusNavigationEvent.ArrowNavigation) {
            FocusNavigationEvent.ArrowNavigation arrowNavigation = (FocusNavigationEvent.ArrowNavigation)focusNavigationEvent;
            Entry entry = (Entry)this.nextEntry(arrowNavigation.direction());
            if (entry != null) {
                return ComponentPath.path(this, ComponentPath.leaf(entry));
            }
            this.setSelected(null);
            return null;
        }
        if (!this.isFocused()) {
            Entry entry = (Entry)this.getSelected();
            if (entry == null) {
                entry = (Entry)this.nextEntry(focusNavigationEvent.getVerticalDirectionForInitialFocus());
            }
            if (entry == null) {
                return null;
            }
            return ComponentPath.path(this, ComponentPath.leaf(entry));
        }
        return null;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        Entry entry = (Entry)this.getHovered();
        if (entry != null) {
            this.narrateListElementPosition(narrationElementOutput.nest(), entry);
            entry.updateNarration(narrationElementOutput);
        } else {
            Entry entry2 = (Entry)this.getSelected();
            if (entry2 != null) {
                this.narrateListElementPosition(narrationElementOutput.nest(), entry2);
                entry2.updateNarration(narrationElementOutput);
            }
        }
        if (this.isFocused()) {
            narrationElementOutput.add(NarratedElementType.USAGE, USAGE_NARRATION);
        }
    }

    public static abstract class Entry<E extends Entry<E>>
    extends AbstractSelectionList.Entry<E>
    implements NarrationSupplier {
        public abstract Component getNarration();

        @Override
        public boolean mouseClicked(double d, double d2, int n) {
            return true;
        }

        @Override
        public void updateNarration(NarrationElementOutput narrationElementOutput) {
            narrationElementOutput.add(NarratedElementType.TITLE, this.getNarration());
        }
    }
}

