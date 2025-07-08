/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens.advancements;

import com.google.common.collect.Maps;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.resources.ResourceLocation;

public class AdvancementsScreen
extends Screen
implements ClientAdvancements.Listener {
    private static final ResourceLocation WINDOW_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/advancements/window.png");
    public static final int WINDOW_WIDTH = 252;
    public static final int WINDOW_HEIGHT = 140;
    private static final int WINDOW_INSIDE_X = 9;
    private static final int WINDOW_INSIDE_Y = 18;
    public static final int WINDOW_INSIDE_WIDTH = 234;
    public static final int WINDOW_INSIDE_HEIGHT = 113;
    private static final int WINDOW_TITLE_X = 8;
    private static final int WINDOW_TITLE_Y = 6;
    private static final int BACKGROUND_TEXTURE_WIDTH = 256;
    private static final int BACKGROUND_TEXTURE_HEIGHT = 256;
    public static final int BACKGROUND_TILE_WIDTH = 16;
    public static final int BACKGROUND_TILE_HEIGHT = 16;
    public static final int BACKGROUND_TILE_COUNT_X = 14;
    public static final int BACKGROUND_TILE_COUNT_Y = 7;
    private static final double SCROLL_SPEED = 16.0;
    private static final Component VERY_SAD_LABEL = Component.translatable("advancements.sad_label");
    private static final Component NO_ADVANCEMENTS_LABEL = Component.translatable("advancements.empty");
    private static final Component TITLE = Component.translatable("gui.advancements");
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    @Nullable
    private final Screen lastScreen;
    private final ClientAdvancements advancements;
    private final Map<AdvancementHolder, AdvancementTab> tabs = Maps.newLinkedHashMap();
    @Nullable
    private AdvancementTab selectedTab;
    private boolean isScrolling;

    public AdvancementsScreen(ClientAdvancements clientAdvancements) {
        this(clientAdvancements, null);
    }

    public AdvancementsScreen(ClientAdvancements clientAdvancements, @Nullable Screen screen) {
        super(TITLE);
        this.advancements = clientAdvancements;
        this.lastScreen = screen;
    }

    @Override
    protected void init() {
        this.layout.addTitleHeader(TITLE, this.font);
        this.tabs.clear();
        this.selectedTab = null;
        this.advancements.setListener(this);
        if (this.selectedTab == null && !this.tabs.isEmpty()) {
            AdvancementTab advancementTab = this.tabs.values().iterator().next();
            this.advancements.setSelectedTab(advancementTab.getRootNode().holder(), true);
        } else {
            this.advancements.setSelectedTab(this.selectedTab == null ? null : this.selectedTab.getRootNode().holder(), true);
        }
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).width(200).build());
        this.layout.visitWidgets(guiEventListener -> {
            AbstractWidget cfr_ignored_0 = (AbstractWidget)this.addRenderableWidget(guiEventListener);
        });
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    @Override
    public void removed() {
        this.advancements.setListener(null);
        ClientPacketListener clientPacketListener = this.minecraft.getConnection();
        if (clientPacketListener != null) {
            clientPacketListener.send(ServerboundSeenAdvancementsPacket.closedScreen());
        }
    }

    @Override
    public boolean mouseClicked(double d, double d2, int n) {
        if (n == 0) {
            int n2 = (this.width - 252) / 2;
            int n3 = (this.height - 140) / 2;
            for (AdvancementTab advancementTab : this.tabs.values()) {
                if (!advancementTab.isMouseOver(n2, n3, d, d2)) continue;
                this.advancements.setSelectedTab(advancementTab.getRootNode().holder(), true);
                break;
            }
        }
        return super.mouseClicked(d, d2, n);
    }

    @Override
    public boolean keyPressed(int n, int n2, int n3) {
        if (this.minecraft.options.keyAdvancements.matches(n, n2)) {
            this.minecraft.setScreen(null);
            this.minecraft.mouseHandler.grabMouse();
            return true;
        }
        return super.keyPressed(n, n2, n3);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        super.render(guiGraphics, n, n2, f);
        int n3 = (this.width - 252) / 2;
        int n4 = (this.height - 140) / 2;
        guiGraphics.nextStratum();
        this.renderInside(guiGraphics, n3, n4);
        guiGraphics.nextStratum();
        this.renderWindow(guiGraphics, n3, n4);
        this.renderTooltips(guiGraphics, n, n2, n3, n4);
    }

    @Override
    public boolean mouseDragged(double d, double d2, int n, double d3, double d4) {
        if (n != 0) {
            this.isScrolling = false;
            return false;
        }
        if (!this.isScrolling) {
            this.isScrolling = true;
        } else if (this.selectedTab != null) {
            this.selectedTab.scroll(d3, d4);
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double d, double d2, double d3, double d4) {
        if (this.selectedTab != null) {
            this.selectedTab.scroll(d3 * 16.0, d4 * 16.0);
            return true;
        }
        return false;
    }

    private void renderInside(GuiGraphics guiGraphics, int n, int n2) {
        AdvancementTab advancementTab = this.selectedTab;
        if (advancementTab == null) {
            guiGraphics.fill(n + 9, n2 + 18, n + 9 + 234, n2 + 18 + 113, -16777216);
            int n3 = n + 9 + 117;
            guiGraphics.drawCenteredString(this.font, NO_ADVANCEMENTS_LABEL, n3, n2 + 18 + 56 - this.font.lineHeight / 2, -1);
            guiGraphics.drawCenteredString(this.font, VERY_SAD_LABEL, n3, n2 + 18 + 113 - this.font.lineHeight, -1);
            return;
        }
        advancementTab.drawContents(guiGraphics, n + 9, n2 + 18);
    }

    public void renderWindow(GuiGraphics guiGraphics, int n, int n2) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, WINDOW_LOCATION, n, n2, 0.0f, 0.0f, 252, 140, 256, 256);
        if (this.tabs.size() > 1) {
            for (AdvancementTab advancementTab : this.tabs.values()) {
                advancementTab.drawTab(guiGraphics, n, n2, advancementTab == this.selectedTab);
            }
            for (AdvancementTab advancementTab : this.tabs.values()) {
                advancementTab.drawIcon(guiGraphics, n, n2);
            }
        }
        guiGraphics.drawString(this.font, this.selectedTab != null ? this.selectedTab.getTitle() : TITLE, n + 8, n2 + 6, -12566464, false);
    }

    private void renderTooltips(GuiGraphics guiGraphics, int n, int n2, int n3, int n4) {
        if (this.selectedTab != null) {
            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().translate((float)(n3 + 9), (float)(n4 + 18));
            guiGraphics.nextStratum();
            this.selectedTab.drawTooltips(guiGraphics, n - n3 - 9, n2 - n4 - 18, n3, n4);
            guiGraphics.pose().popMatrix();
        }
        if (this.tabs.size() > 1) {
            for (AdvancementTab advancementTab : this.tabs.values()) {
                if (!advancementTab.isMouseOver(n3, n4, n, n2)) continue;
                guiGraphics.setTooltipForNextFrame(this.font, advancementTab.getTitle(), n, n2);
            }
        }
    }

    @Override
    public void onAddAdvancementRoot(AdvancementNode advancementNode) {
        AdvancementTab advancementTab = AdvancementTab.create(this.minecraft, this, this.tabs.size(), advancementNode);
        if (advancementTab == null) {
            return;
        }
        this.tabs.put(advancementNode.holder(), advancementTab);
    }

    @Override
    public void onRemoveAdvancementRoot(AdvancementNode advancementNode) {
    }

    @Override
    public void onAddAdvancementTask(AdvancementNode advancementNode) {
        AdvancementTab advancementTab = this.getTab(advancementNode);
        if (advancementTab != null) {
            advancementTab.addAdvancement(advancementNode);
        }
    }

    @Override
    public void onRemoveAdvancementTask(AdvancementNode advancementNode) {
    }

    @Override
    public void onUpdateAdvancementProgress(AdvancementNode advancementNode, AdvancementProgress advancementProgress) {
        AdvancementWidget advancementWidget = this.getAdvancementWidget(advancementNode);
        if (advancementWidget != null) {
            advancementWidget.setProgress(advancementProgress);
        }
    }

    @Override
    public void onSelectedTabChanged(@Nullable AdvancementHolder advancementHolder) {
        this.selectedTab = this.tabs.get(advancementHolder);
    }

    @Override
    public void onAdvancementsCleared() {
        this.tabs.clear();
        this.selectedTab = null;
    }

    @Nullable
    public AdvancementWidget getAdvancementWidget(AdvancementNode advancementNode) {
        AdvancementTab advancementTab = this.getTab(advancementNode);
        return advancementTab == null ? null : advancementTab.getWidget(advancementNode.holder());
    }

    @Nullable
    private AdvancementTab getTab(AdvancementNode advancementNode) {
        AdvancementNode advancementNode2 = advancementNode.root();
        return this.tabs.get(advancementNode2.holder());
    }
}

