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
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.advancements.AdvancementTabType;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.ClientAsset;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class AdvancementTab {
    private final Minecraft minecraft;
    private final AdvancementsScreen screen;
    private final AdvancementTabType type;
    private final int index;
    private final AdvancementNode rootNode;
    private final DisplayInfo display;
    private final ItemStack icon;
    private final Component title;
    private final AdvancementWidget root;
    private final Map<AdvancementHolder, AdvancementWidget> widgets = Maps.newLinkedHashMap();
    private double scrollX;
    private double scrollY;
    private int minX = Integer.MAX_VALUE;
    private int minY = Integer.MAX_VALUE;
    private int maxX = Integer.MIN_VALUE;
    private int maxY = Integer.MIN_VALUE;
    private float fade;
    private boolean centered;

    public AdvancementTab(Minecraft minecraft, AdvancementsScreen advancementsScreen, AdvancementTabType advancementTabType, int n, AdvancementNode advancementNode, DisplayInfo displayInfo) {
        this.minecraft = minecraft;
        this.screen = advancementsScreen;
        this.type = advancementTabType;
        this.index = n;
        this.rootNode = advancementNode;
        this.display = displayInfo;
        this.icon = displayInfo.getIcon();
        this.title = displayInfo.getTitle();
        this.root = new AdvancementWidget(this, minecraft, advancementNode, displayInfo);
        this.addWidget(this.root, advancementNode.holder());
    }

    public AdvancementTabType getType() {
        return this.type;
    }

    public int getIndex() {
        return this.index;
    }

    public AdvancementNode getRootNode() {
        return this.rootNode;
    }

    public Component getTitle() {
        return this.title;
    }

    public DisplayInfo getDisplay() {
        return this.display;
    }

    public void drawTab(GuiGraphics guiGraphics, int n, int n2, boolean bl) {
        this.type.draw(guiGraphics, n, n2, bl, this.index);
    }

    public void drawIcon(GuiGraphics guiGraphics, int n, int n2) {
        this.type.drawIcon(guiGraphics, n, n2, this.index, this.icon);
    }

    public void drawContents(GuiGraphics guiGraphics, int n, int n2) {
        if (!this.centered) {
            this.scrollX = 117 - (this.maxX + this.minX) / 2;
            this.scrollY = 56 - (this.maxY + this.minY) / 2;
            this.centered = true;
        }
        guiGraphics.enableScissor(n, n2, n + 234, n2 + 113);
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate((float)n, (float)n2);
        ResourceLocation resourceLocation = this.display.getBackground().map(ClientAsset::texturePath).orElse(TextureManager.INTENTIONAL_MISSING_TEXTURE);
        int n3 = Mth.floor(this.scrollX);
        int n4 = Mth.floor(this.scrollY);
        int n5 = n3 % 16;
        int n6 = n4 % 16;
        for (int i = -1; i <= 15; ++i) {
            for (int j = -1; j <= 8; ++j) {
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, resourceLocation, n5 + 16 * i, n6 + 16 * j, 0.0f, 0.0f, 16, 16, 16, 16);
            }
        }
        this.root.drawConnectivity(guiGraphics, n3, n4, true);
        this.root.drawConnectivity(guiGraphics, n3, n4, false);
        this.root.draw(guiGraphics, n3, n4);
        guiGraphics.pose().popMatrix();
        guiGraphics.disableScissor();
    }

    public void drawTooltips(GuiGraphics guiGraphics, int n, int n2, int n3, int n4) {
        guiGraphics.fill(0, 0, 234, 113, Mth.floor(this.fade * 255.0f) << 24);
        boolean bl = false;
        int n5 = Mth.floor(this.scrollX);
        int n6 = Mth.floor(this.scrollY);
        if (n > 0 && n < 234 && n2 > 0 && n2 < 113) {
            for (AdvancementWidget advancementWidget : this.widgets.values()) {
                if (!advancementWidget.isMouseOver(n5, n6, n, n2)) continue;
                bl = true;
                advancementWidget.drawHover(guiGraphics, n5, n6, this.fade, n3, n4);
                break;
            }
        }
        this.fade = bl ? Mth.clamp(this.fade + 0.02f, 0.0f, 0.3f) : Mth.clamp(this.fade - 0.04f, 0.0f, 1.0f);
    }

    public boolean isMouseOver(int n, int n2, double d, double d2) {
        return this.type.isMouseOver(n, n2, this.index, d, d2);
    }

    @Nullable
    public static AdvancementTab create(Minecraft minecraft, AdvancementsScreen advancementsScreen, int n, AdvancementNode advancementNode) {
        Optional<DisplayInfo> optional = advancementNode.advancement().display();
        if (optional.isEmpty()) {
            return null;
        }
        for (AdvancementTabType advancementTabType : AdvancementTabType.values()) {
            if (n >= advancementTabType.getMax()) {
                n -= advancementTabType.getMax();
                continue;
            }
            return new AdvancementTab(minecraft, advancementsScreen, advancementTabType, n, advancementNode, optional.get());
        }
        return null;
    }

    public void scroll(double d, double d2) {
        if (this.maxX - this.minX > 234) {
            this.scrollX = Mth.clamp(this.scrollX + d, (double)(-(this.maxX - 234)), 0.0);
        }
        if (this.maxY - this.minY > 113) {
            this.scrollY = Mth.clamp(this.scrollY + d2, (double)(-(this.maxY - 113)), 0.0);
        }
    }

    public void addAdvancement(AdvancementNode advancementNode) {
        Optional<DisplayInfo> optional = advancementNode.advancement().display();
        if (optional.isEmpty()) {
            return;
        }
        AdvancementWidget advancementWidget = new AdvancementWidget(this, this.minecraft, advancementNode, optional.get());
        this.addWidget(advancementWidget, advancementNode.holder());
    }

    private void addWidget(AdvancementWidget advancementWidget, AdvancementHolder advancementHolder) {
        this.widgets.put(advancementHolder, advancementWidget);
        int n = advancementWidget.getX();
        int n2 = n + 28;
        int n3 = advancementWidget.getY();
        int n4 = n3 + 27;
        this.minX = Math.min(this.minX, n);
        this.maxX = Math.max(this.maxX, n2);
        this.minY = Math.min(this.minY, n3);
        this.maxY = Math.max(this.maxY, n4);
        for (AdvancementWidget advancementWidget2 : this.widgets.values()) {
            advancementWidget2.attachToParent();
        }
    }

    @Nullable
    public AdvancementWidget getWidget(AdvancementHolder advancementHolder) {
        return this.widgets.get(advancementHolder);
    }

    public AdvancementsScreen getScreen() {
        return this.screen;
    }
}

