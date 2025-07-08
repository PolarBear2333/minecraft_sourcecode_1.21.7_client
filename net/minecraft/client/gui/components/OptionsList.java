/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableMap
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.components;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;

public class OptionsList
extends ContainerObjectSelectionList<Entry> {
    private static final int BIG_BUTTON_WIDTH = 310;
    private static final int DEFAULT_ITEM_HEIGHT = 25;
    private final OptionsSubScreen screen;

    public OptionsList(Minecraft minecraft, int n, OptionsSubScreen optionsSubScreen) {
        super(minecraft, n, optionsSubScreen.layout.getContentHeight(), optionsSubScreen.layout.getHeaderHeight(), 25);
        this.centerListVertically = false;
        this.screen = optionsSubScreen;
    }

    public void addBig(OptionInstance<?> optionInstance) {
        this.addEntry(OptionEntry.big(this.minecraft.options, optionInstance, this.screen));
    }

    public void addSmall(OptionInstance<?> ... optionInstanceArray) {
        for (int i = 0; i < optionInstanceArray.length; i += 2) {
            OptionInstance<?> optionInstance = i < optionInstanceArray.length - 1 ? optionInstanceArray[i + 1] : null;
            this.addEntry(OptionEntry.small(this.minecraft.options, optionInstanceArray[i], optionInstance, this.screen));
        }
    }

    public void addSmall(List<AbstractWidget> list) {
        for (int i = 0; i < list.size(); i += 2) {
            this.addSmall(list.get(i), i < list.size() - 1 ? list.get(i + 1) : null);
        }
    }

    public void addSmall(AbstractWidget abstractWidget, @Nullable AbstractWidget abstractWidget2) {
        this.addEntry(Entry.small(abstractWidget, abstractWidget2, this.screen));
    }

    @Override
    public int getRowWidth() {
        return 310;
    }

    @Nullable
    public AbstractWidget findOption(OptionInstance<?> optionInstance) {
        for (Entry entry : this.children()) {
            if (!(entry instanceof OptionEntry)) continue;
            OptionEntry optionEntry = (OptionEntry)entry;
            AbstractWidget abstractWidget = optionEntry.options.get(optionInstance);
            if (abstractWidget == null) continue;
            return abstractWidget;
        }
        return null;
    }

    public void applyUnsavedChanges() {
        for (Entry entry : this.children()) {
            if (!(entry instanceof OptionEntry)) continue;
            OptionEntry optionEntry = (OptionEntry)entry;
            for (AbstractWidget abstractWidget : optionEntry.options.values()) {
                if (!(abstractWidget instanceof OptionInstance.OptionInstanceSliderButton)) continue;
                OptionInstance.OptionInstanceSliderButton optionInstanceSliderButton = (OptionInstance.OptionInstanceSliderButton)abstractWidget;
                optionInstanceSliderButton.applyUnsavedValue();
            }
        }
    }

    public Optional<GuiEventListener> getMouseOver(double d, double d2) {
        for (Entry entry : this.children()) {
            for (GuiEventListener guiEventListener : entry.children()) {
                if (!guiEventListener.isMouseOver(d, d2)) continue;
                return Optional.of(guiEventListener);
            }
        }
        return Optional.empty();
    }

    protected static class OptionEntry
    extends Entry {
        final Map<OptionInstance<?>, AbstractWidget> options;

        private OptionEntry(Map<OptionInstance<?>, AbstractWidget> map, OptionsSubScreen optionsSubScreen) {
            super((List<AbstractWidget>)ImmutableList.copyOf(map.values()), optionsSubScreen);
            this.options = map;
        }

        public static OptionEntry big(Options options, OptionInstance<?> optionInstance, OptionsSubScreen optionsSubScreen) {
            return new OptionEntry((Map<OptionInstance<?>, AbstractWidget>)ImmutableMap.of(optionInstance, (Object)optionInstance.createButton(options, 0, 0, 310)), optionsSubScreen);
        }

        public static OptionEntry small(Options options, OptionInstance<?> optionInstance, @Nullable OptionInstance<?> optionInstance2, OptionsSubScreen optionsSubScreen) {
            AbstractWidget abstractWidget = optionInstance.createButton(options);
            if (optionInstance2 == null) {
                return new OptionEntry((Map<OptionInstance<?>, AbstractWidget>)ImmutableMap.of(optionInstance, (Object)abstractWidget), optionsSubScreen);
            }
            return new OptionEntry((Map<OptionInstance<?>, AbstractWidget>)ImmutableMap.of(optionInstance, (Object)abstractWidget, optionInstance2, (Object)optionInstance2.createButton(options)), optionsSubScreen);
        }
    }

    protected static class Entry
    extends ContainerObjectSelectionList.Entry<Entry> {
        private final List<AbstractWidget> children;
        private final Screen screen;
        private static final int X_OFFSET = 160;

        Entry(List<AbstractWidget> list, Screen screen) {
            this.children = ImmutableList.copyOf(list);
            this.screen = screen;
        }

        public static Entry big(List<AbstractWidget> list, Screen screen) {
            return new Entry(list, screen);
        }

        public static Entry small(AbstractWidget abstractWidget, @Nullable AbstractWidget abstractWidget2, Screen screen) {
            if (abstractWidget2 == null) {
                return new Entry((List<AbstractWidget>)ImmutableList.of((Object)abstractWidget), screen);
            }
            return new Entry((List<AbstractWidget>)ImmutableList.of((Object)abstractWidget, (Object)abstractWidget2), screen);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, int n5, int n6, int n7, boolean bl, float f) {
            int n8 = 0;
            int n9 = this.screen.width / 2 - 155;
            for (AbstractWidget abstractWidget : this.children) {
                abstractWidget.setPosition(n9 + n8, n2);
                abstractWidget.render(guiGraphics, n6, n7, f);
                n8 += 160;
            }
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return this.children;
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return this.children;
        }
    }
}

