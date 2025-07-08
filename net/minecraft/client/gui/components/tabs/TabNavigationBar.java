/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.components.tabs;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.TabButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class TabNavigationBar
extends AbstractContainerEventHandler
implements Renderable,
NarratableEntry {
    private static final int NO_TAB = -1;
    private static final int MAX_WIDTH = 400;
    private static final int HEIGHT = 24;
    private static final int MARGIN = 14;
    private static final Component USAGE_NARRATION = Component.translatable("narration.tab_navigation.usage");
    private final LinearLayout layout = LinearLayout.horizontal();
    private int width;
    private final TabManager tabManager;
    private final ImmutableList<Tab> tabs;
    private final ImmutableList<TabButton> tabButtons;

    TabNavigationBar(int n, TabManager tabManager, Iterable<Tab> iterable) {
        this.width = n;
        this.tabManager = tabManager;
        this.tabs = ImmutableList.copyOf(iterable);
        this.layout.defaultCellSetting().alignHorizontallyCenter();
        ImmutableList.Builder builder = ImmutableList.builder();
        for (Tab tab : iterable) {
            builder.add((Object)this.layout.addChild(new TabButton(tabManager, tab, 0, 24)));
        }
        this.tabButtons = builder.build();
    }

    public static Builder builder(TabManager tabManager, int n) {
        return new Builder(tabManager, n);
    }

    public void setWidth(int n) {
        this.width = n;
    }

    @Override
    public boolean isMouseOver(double d, double d2) {
        return d >= (double)this.layout.getX() && d2 >= (double)this.layout.getY() && d < (double)(this.layout.getX() + this.layout.getWidth()) && d2 < (double)(this.layout.getY() + this.layout.getHeight());
    }

    @Override
    public void setFocused(boolean bl) {
        super.setFocused(bl);
        if (this.getFocused() != null) {
            this.getFocused().setFocused(bl);
        }
    }

    @Override
    public void setFocused(@Nullable GuiEventListener guiEventListener) {
        TabButton tabButton;
        if (guiEventListener instanceof TabButton && (tabButton = (TabButton)guiEventListener).isActive()) {
            super.setFocused(guiEventListener);
            this.tabManager.setCurrentTab(tabButton.tab(), true);
        }
    }

    @Override
    @Nullable
    public ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
        TabButton tabButton;
        if (!this.isFocused() && (tabButton = this.currentTabButton()) != null) {
            return ComponentPath.path(this, ComponentPath.leaf(tabButton));
        }
        if (focusNavigationEvent instanceof FocusNavigationEvent.TabNavigation) {
            return null;
        }
        return super.nextFocusPath(focusNavigationEvent);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return this.tabButtons;
    }

    public List<Tab> getTabs() {
        return this.tabs;
    }

    @Override
    public NarratableEntry.NarrationPriority narrationPriority() {
        return this.tabButtons.stream().map(AbstractWidget::narrationPriority).max(Comparator.naturalOrder()).orElse(NarratableEntry.NarrationPriority.NONE);
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
        Optional<TabButton> optional = this.tabButtons.stream().filter(AbstractWidget::isHovered).findFirst().or(() -> Optional.ofNullable(this.currentTabButton()));
        optional.ifPresent(tabButton -> {
            this.narrateListElementPosition(narrationElementOutput.nest(), (TabButton)tabButton);
            tabButton.updateNarration(narrationElementOutput);
        });
        if (this.isFocused()) {
            narrationElementOutput.add(NarratedElementType.USAGE, USAGE_NARRATION);
        }
    }

    protected void narrateListElementPosition(NarrationElementOutput narrationElementOutput, TabButton tabButton) {
        int n;
        if (this.tabs.size() > 1 && (n = this.tabButtons.indexOf((Object)tabButton)) != -1) {
            narrationElementOutput.add(NarratedElementType.POSITION, (Component)Component.translatable("narrator.position.tab", n + 1, this.tabs.size()));
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, Screen.HEADER_SEPARATOR, 0, this.layout.getY() + this.layout.getHeight() - 2, 0.0f, 0.0f, ((TabButton)this.tabButtons.get(0)).getX(), 2, 32, 2);
        int n3 = ((TabButton)this.tabButtons.get(this.tabButtons.size() - 1)).getRight();
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, Screen.HEADER_SEPARATOR, n3, this.layout.getY() + this.layout.getHeight() - 2, 0.0f, 0.0f, this.width, 2, 32, 2);
        for (TabButton tabButton : this.tabButtons) {
            tabButton.render(guiGraphics, n, n2, f);
        }
    }

    @Override
    public ScreenRectangle getRectangle() {
        return this.layout.getRectangle();
    }

    public void arrangeElements() {
        int n = Math.min(400, this.width) - 28;
        int n2 = Mth.roundToward(n / this.tabs.size(), 2);
        for (TabButton tabButton : this.tabButtons) {
            tabButton.setWidth(n2);
        }
        this.layout.arrangeElements();
        this.layout.setX(Mth.roundToward((this.width - n) / 2, 2));
        this.layout.setY(0);
    }

    public void selectTab(int n, boolean bl) {
        if (this.isFocused()) {
            this.setFocused((GuiEventListener)this.tabButtons.get(n));
        } else if (((TabButton)this.tabButtons.get(n)).isActive()) {
            this.tabManager.setCurrentTab((Tab)this.tabs.get(n), bl);
        }
    }

    public void setTabActiveState(int n, boolean bl) {
        if (n >= 0 && n < this.tabButtons.size()) {
            ((TabButton)this.tabButtons.get((int)n)).active = bl;
        }
    }

    public void setTabTooltip(int n, @Nullable Tooltip tooltip) {
        if (n >= 0 && n < this.tabButtons.size()) {
            ((TabButton)this.tabButtons.get(n)).setTooltip(tooltip);
        }
    }

    public boolean keyPressed(int n) {
        int n2;
        if (Screen.hasControlDown() && (n2 = this.getNextTabIndex(n)) != -1) {
            this.selectTab(Mth.clamp(n2, 0, this.tabs.size() - 1), true);
            return true;
        }
        return false;
    }

    private int getNextTabIndex(int n) {
        return this.getNextTabIndex(this.currentTabIndex(), n);
    }

    private int getNextTabIndex(int n, int n2) {
        if (n2 >= 49 && n2 <= 57) {
            return n2 - 49;
        }
        if (n2 == 258 && n != -1) {
            int n3 = Screen.hasShiftDown() ? n - 1 : n + 1;
            int n4 = Math.floorMod(n3, this.tabs.size());
            if (((TabButton)this.tabButtons.get((int)n4)).active) {
                return n4;
            }
            return this.getNextTabIndex(n4, n2);
        }
        return -1;
    }

    private int currentTabIndex() {
        Tab tab = this.tabManager.getCurrentTab();
        int n = this.tabs.indexOf((Object)tab);
        return n != -1 ? n : -1;
    }

    @Nullable
    private TabButton currentTabButton() {
        int n = this.currentTabIndex();
        return n != -1 ? (TabButton)this.tabButtons.get(n) : null;
    }

    public static class Builder {
        private final int width;
        private final TabManager tabManager;
        private final List<Tab> tabs = new ArrayList<Tab>();

        Builder(TabManager tabManager, int n) {
            this.tabManager = tabManager;
            this.width = n;
        }

        public Builder addTabs(Tab ... tabArray) {
            Collections.addAll(this.tabs, tabArray);
            return this;
        }

        public TabNavigationBar build() {
            return new TabNavigationBar(this.width, this.tabManager, this.tabs);
        }
    }
}

