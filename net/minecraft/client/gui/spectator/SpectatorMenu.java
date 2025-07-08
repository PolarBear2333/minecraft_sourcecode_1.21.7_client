/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.MoreObjects
 *  com.google.common.collect.Lists
 */
package net.minecraft.client.gui.spectator;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.spectator.RootSpectatorMenuCategory;
import net.minecraft.client.gui.spectator.SpectatorMenuCategory;
import net.minecraft.client.gui.spectator.SpectatorMenuItem;
import net.minecraft.client.gui.spectator.SpectatorMenuListener;
import net.minecraft.client.gui.spectator.categories.SpectatorPage;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;

public class SpectatorMenu {
    static final ResourceLocation CLOSE_SPRITE = ResourceLocation.withDefaultNamespace("spectator/close");
    static final ResourceLocation SCROLL_LEFT_SPRITE = ResourceLocation.withDefaultNamespace("spectator/scroll_left");
    static final ResourceLocation SCROLL_RIGHT_SPRITE = ResourceLocation.withDefaultNamespace("spectator/scroll_right");
    private static final SpectatorMenuItem CLOSE_ITEM = new CloseSpectatorItem();
    private static final SpectatorMenuItem SCROLL_LEFT = new ScrollMenuItem(-1, true);
    private static final SpectatorMenuItem SCROLL_RIGHT_ENABLED = new ScrollMenuItem(1, true);
    private static final SpectatorMenuItem SCROLL_RIGHT_DISABLED = new ScrollMenuItem(1, false);
    private static final int MAX_PER_PAGE = 8;
    static final Component CLOSE_MENU_TEXT = Component.translatable("spectatorMenu.close");
    static final Component PREVIOUS_PAGE_TEXT = Component.translatable("spectatorMenu.previous_page");
    static final Component NEXT_PAGE_TEXT = Component.translatable("spectatorMenu.next_page");
    public static final SpectatorMenuItem EMPTY_SLOT = new SpectatorMenuItem(){

        @Override
        public void selectItem(SpectatorMenu spectatorMenu) {
        }

        @Override
        public Component getName() {
            return CommonComponents.EMPTY;
        }

        @Override
        public void renderIcon(GuiGraphics guiGraphics, float f, float f2) {
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
    };
    private final SpectatorMenuListener listener;
    private SpectatorMenuCategory category = new RootSpectatorMenuCategory();
    private int selectedSlot = -1;
    int page;

    public SpectatorMenu(SpectatorMenuListener spectatorMenuListener) {
        this.listener = spectatorMenuListener;
    }

    public SpectatorMenuItem getItem(int n) {
        int n2 = n + this.page * 6;
        if (this.page > 0 && n == 0) {
            return SCROLL_LEFT;
        }
        if (n == 7) {
            if (n2 < this.category.getItems().size()) {
                return SCROLL_RIGHT_ENABLED;
            }
            return SCROLL_RIGHT_DISABLED;
        }
        if (n == 8) {
            return CLOSE_ITEM;
        }
        if (n2 < 0 || n2 >= this.category.getItems().size()) {
            return EMPTY_SLOT;
        }
        return (SpectatorMenuItem)MoreObjects.firstNonNull((Object)this.category.getItems().get(n2), (Object)EMPTY_SLOT);
    }

    public List<SpectatorMenuItem> getItems() {
        ArrayList arrayList = Lists.newArrayList();
        for (int i = 0; i <= 8; ++i) {
            arrayList.add(this.getItem(i));
        }
        return arrayList;
    }

    public SpectatorMenuItem getSelectedItem() {
        return this.getItem(this.selectedSlot);
    }

    public SpectatorMenuCategory getSelectedCategory() {
        return this.category;
    }

    public void selectSlot(int n) {
        SpectatorMenuItem spectatorMenuItem = this.getItem(n);
        if (spectatorMenuItem != EMPTY_SLOT) {
            if (this.selectedSlot == n && spectatorMenuItem.isEnabled()) {
                spectatorMenuItem.selectItem(this);
            } else {
                this.selectedSlot = n;
            }
        }
    }

    public void exit() {
        this.listener.onSpectatorMenuClosed(this);
    }

    public int getSelectedSlot() {
        return this.selectedSlot;
    }

    public void selectCategory(SpectatorMenuCategory spectatorMenuCategory) {
        this.category = spectatorMenuCategory;
        this.selectedSlot = -1;
        this.page = 0;
    }

    public SpectatorPage getCurrentPage() {
        return new SpectatorPage(this.getItems(), this.selectedSlot);
    }

    static class CloseSpectatorItem
    implements SpectatorMenuItem {
        CloseSpectatorItem() {
        }

        @Override
        public void selectItem(SpectatorMenu spectatorMenu) {
            spectatorMenu.exit();
        }

        @Override
        public Component getName() {
            return CLOSE_MENU_TEXT;
        }

        @Override
        public void renderIcon(GuiGraphics guiGraphics, float f, float f2) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, CLOSE_SPRITE, 0, 0, 16, 16, ARGB.colorFromFloat(f2, f, f, f));
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    static class ScrollMenuItem
    implements SpectatorMenuItem {
        private final int direction;
        private final boolean enabled;

        public ScrollMenuItem(int n, boolean bl) {
            this.direction = n;
            this.enabled = bl;
        }

        @Override
        public void selectItem(SpectatorMenu spectatorMenu) {
            spectatorMenu.page += this.direction;
        }

        @Override
        public Component getName() {
            return this.direction < 0 ? PREVIOUS_PAGE_TEXT : NEXT_PAGE_TEXT;
        }

        @Override
        public void renderIcon(GuiGraphics guiGraphics, float f, float f2) {
            int n = ARGB.colorFromFloat(f2, f, f, f);
            if (this.direction < 0) {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLL_LEFT_SPRITE, 0, 0, 16, 16, n);
            } else {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLL_RIGHT_SPRITE, 0, 0, 16, 16, n);
            }
        }

        @Override
        public boolean isEnabled() {
            return this.enabled;
        }
    }
}

