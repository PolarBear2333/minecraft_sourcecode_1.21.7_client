/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.ArrayUtils
 */
package net.minecraft.client.gui.screens.options.controls;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.apache.commons.lang3.ArrayUtils;

public class KeyBindsList
extends ContainerObjectSelectionList<Entry> {
    private static final int ITEM_HEIGHT = 20;
    final KeyBindsScreen keyBindsScreen;
    private int maxNameWidth;

    public KeyBindsList(KeyBindsScreen keyBindsScreen, Minecraft minecraft) {
        super(minecraft, keyBindsScreen.width, keyBindsScreen.layout.getContentHeight(), keyBindsScreen.layout.getHeaderHeight(), 20);
        this.keyBindsScreen = keyBindsScreen;
        Object[] objectArray = (KeyMapping[])ArrayUtils.clone((Object[])minecraft.options.keyMappings);
        Arrays.sort(objectArray);
        String string = null;
        for (Object object : objectArray) {
            MutableComponent mutableComponent;
            int n;
            String string2 = ((KeyMapping)object).getCategory();
            if (!string2.equals(string)) {
                string = string2;
                this.addEntry(new CategoryEntry(Component.translatable(string2)));
            }
            if ((n = minecraft.font.width(mutableComponent = Component.translatable(((KeyMapping)object).getName()))) > this.maxNameWidth) {
                this.maxNameWidth = n;
            }
            this.addEntry(new KeyEntry((KeyMapping)object, mutableComponent));
        }
    }

    public void resetMappingAndUpdateButtons() {
        KeyMapping.resetMapping();
        this.refreshEntries();
    }

    public void refreshEntries() {
        this.children().forEach(Entry::refreshEntry);
    }

    @Override
    public int getRowWidth() {
        return 340;
    }

    public class CategoryEntry
    extends Entry {
        final Component name;
        private final int width;

        public CategoryEntry(Component component) {
            this.name = component;
            this.width = ((KeyBindsList)KeyBindsList.this).minecraft.font.width(this.name);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, int n5, int n6, int n7, boolean bl, float f) {
            guiGraphics.drawString(((KeyBindsList)KeyBindsList.this).minecraft.font, this.name, KeyBindsList.this.width / 2 - this.width / 2, n2 + n5 - ((KeyBindsList)KeyBindsList.this).minecraft.font.lineHeight - 1, -1);
        }

        @Override
        @Nullable
        public ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
            return null;
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return Collections.emptyList();
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of((Object)new NarratableEntry(){

                @Override
                public NarratableEntry.NarrationPriority narrationPriority() {
                    return NarratableEntry.NarrationPriority.HOVERED;
                }

                @Override
                public void updateNarration(NarrationElementOutput narrationElementOutput) {
                    narrationElementOutput.add(NarratedElementType.TITLE, CategoryEntry.this.name);
                }
            });
        }

        @Override
        protected void refreshEntry() {
        }
    }

    public class KeyEntry
    extends Entry {
        private static final Component RESET_BUTTON_TITLE = Component.translatable("controls.reset");
        private static final int PADDING = 10;
        private final KeyMapping key;
        private final Component name;
        private final Button changeButton;
        private final Button resetButton;
        private boolean hasCollision = false;

        KeyEntry(KeyMapping keyMapping, Component component) {
            this.key = keyMapping;
            this.name = component;
            this.changeButton = Button.builder(component, button -> {
                KeyBindsList.this.keyBindsScreen.selectedKey = keyMapping;
                KeyBindsList.this.resetMappingAndUpdateButtons();
            }).bounds(0, 0, 75, 20).createNarration(supplier -> {
                if (keyMapping.isUnbound()) {
                    return Component.translatable("narrator.controls.unbound", component);
                }
                return Component.translatable("narrator.controls.bound", component, supplier.get());
            }).build();
            this.resetButton = Button.builder(RESET_BUTTON_TITLE, button -> {
                keyMapping.setKey(keyMapping.getDefaultKey());
                KeyBindsList.this.resetMappingAndUpdateButtons();
            }).bounds(0, 0, 50, 20).createNarration(supplier -> Component.translatable("narrator.controls.reset", component)).build();
            this.refreshEntry();
        }

        @Override
        public void render(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, int n5, int n6, int n7, boolean bl, float f) {
            int n8 = KeyBindsList.this.scrollBarX() - this.resetButton.getWidth() - 10;
            int n9 = n2 - 2;
            this.resetButton.setPosition(n8, n9);
            this.resetButton.render(guiGraphics, n6, n7, f);
            int n10 = n8 - 5 - this.changeButton.getWidth();
            this.changeButton.setPosition(n10, n9);
            this.changeButton.render(guiGraphics, n6, n7, f);
            guiGraphics.drawString(((KeyBindsList)KeyBindsList.this).minecraft.font, this.name, n3, n2 + n5 / 2 - ((KeyBindsList)KeyBindsList.this).minecraft.font.lineHeight / 2, -1);
            if (this.hasCollision) {
                int n11 = 3;
                int n12 = this.changeButton.getX() - 6;
                guiGraphics.fill(n12, n2 - 1, n12 + 3, n2 + n5, -65536);
            }
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of((Object)this.changeButton, (Object)this.resetButton);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of((Object)this.changeButton, (Object)this.resetButton);
        }

        @Override
        protected void refreshEntry() {
            this.changeButton.setMessage(this.key.getTranslatedKeyMessage());
            this.resetButton.active = !this.key.isDefault();
            this.hasCollision = false;
            MutableComponent mutableComponent = Component.empty();
            if (!this.key.isUnbound()) {
                for (KeyMapping keyMapping : ((KeyBindsList)KeyBindsList.this).minecraft.options.keyMappings) {
                    if (keyMapping == this.key || !this.key.same(keyMapping)) continue;
                    if (this.hasCollision) {
                        mutableComponent.append(", ");
                    }
                    this.hasCollision = true;
                    mutableComponent.append(Component.translatable(keyMapping.getName()));
                }
            }
            if (this.hasCollision) {
                this.changeButton.setMessage(Component.literal("[ ").append(this.changeButton.getMessage().copy().withStyle(ChatFormatting.WHITE)).append(" ]").withStyle(ChatFormatting.RED));
                this.changeButton.setTooltip(Tooltip.create(Component.translatable("controls.keybinds.duplicateKeybinds", mutableComponent)));
            } else {
                this.changeButton.setTooltip(null);
            }
            if (KeyBindsList.this.keyBindsScreen.selectedKey == this.key) {
                this.changeButton.setMessage(Component.literal("> ").append(this.changeButton.getMessage().copy().withStyle(ChatFormatting.WHITE, ChatFormatting.UNDERLINE)).append(" <").withStyle(ChatFormatting.YELLOW));
            }
        }
    }

    public static abstract class Entry
    extends ContainerObjectSelectionList.Entry<Entry> {
        abstract void refreshEntry();
    }
}

