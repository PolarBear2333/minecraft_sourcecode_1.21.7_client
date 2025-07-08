/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens.advancements;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementWidgetType;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

public class AdvancementWidget {
    private static final ResourceLocation TITLE_BOX_SPRITE = ResourceLocation.withDefaultNamespace("advancements/title_box");
    private static final int HEIGHT = 26;
    private static final int BOX_X = 0;
    private static final int BOX_WIDTH = 200;
    private static final int FRAME_WIDTH = 26;
    private static final int ICON_X = 8;
    private static final int ICON_Y = 5;
    private static final int ICON_WIDTH = 26;
    private static final int TITLE_PADDING_LEFT = 3;
    private static final int TITLE_PADDING_RIGHT = 5;
    private static final int TITLE_X = 32;
    private static final int TITLE_PADDING_TOP = 9;
    private static final int TITLE_PADDING_BOTTOM = 8;
    private static final int TITLE_MAX_WIDTH = 163;
    private static final int TITLE_MIN_WIDTH = 80;
    private static final int[] TEST_SPLIT_OFFSETS = new int[]{0, 10, -10, 25, -25};
    private final AdvancementTab tab;
    private final AdvancementNode advancementNode;
    private final DisplayInfo display;
    private final List<FormattedCharSequence> titleLines;
    private final int width;
    private final List<FormattedCharSequence> description;
    private final Minecraft minecraft;
    @Nullable
    private AdvancementWidget parent;
    private final List<AdvancementWidget> children = Lists.newArrayList();
    @Nullable
    private AdvancementProgress progress;
    private final int x;
    private final int y;

    public AdvancementWidget(AdvancementTab advancementTab, Minecraft minecraft, AdvancementNode advancementNode, DisplayInfo displayInfo) {
        this.tab = advancementTab;
        this.advancementNode = advancementNode;
        this.display = displayInfo;
        this.minecraft = minecraft;
        this.titleLines = minecraft.font.split(displayInfo.getTitle(), 163);
        this.x = Mth.floor(displayInfo.getX() * 28.0f);
        this.y = Mth.floor(displayInfo.getY() * 27.0f);
        int n = Math.max(this.titleLines.stream().mapToInt(minecraft.font::width).max().orElse(0), 80);
        int n2 = this.getMaxProgressWidth();
        int n3 = 29 + n + n2;
        this.description = Language.getInstance().getVisualOrder(this.findOptimalLines(ComponentUtils.mergeStyles(displayInfo.getDescription().copy(), Style.EMPTY.withColor(displayInfo.getType().getChatColor())), n3));
        for (FormattedCharSequence formattedCharSequence : this.description) {
            n3 = Math.max(n3, minecraft.font.width(formattedCharSequence));
        }
        this.width = n3 + 3 + 5;
    }

    private int getMaxProgressWidth() {
        int n = this.advancementNode.advancement().requirements().size();
        if (n <= 1) {
            return 0;
        }
        int n2 = 8;
        MutableComponent mutableComponent = Component.translatable("advancements.progress", n, n);
        return this.minecraft.font.width(mutableComponent) + 8;
    }

    private static float getMaxWidth(StringSplitter stringSplitter, List<FormattedText> list) {
        return (float)list.stream().mapToDouble(stringSplitter::stringWidth).max().orElse(0.0);
    }

    private List<FormattedText> findOptimalLines(Component component, int n) {
        StringSplitter stringSplitter = this.minecraft.font.getSplitter();
        List<FormattedText> list = null;
        float f = Float.MAX_VALUE;
        for (int n2 : TEST_SPLIT_OFFSETS) {
            List<FormattedText> list2 = stringSplitter.splitLines(component, n - n2, Style.EMPTY);
            float f2 = Math.abs(AdvancementWidget.getMaxWidth(stringSplitter, list2) - (float)n);
            if (f2 <= 10.0f) {
                return list2;
            }
            if (!(f2 < f)) continue;
            f = f2;
            list = list2;
        }
        return list;
    }

    @Nullable
    private AdvancementWidget getFirstVisibleParent(AdvancementNode advancementNode) {
        while ((advancementNode = advancementNode.parent()) != null && advancementNode.advancement().display().isEmpty()) {
        }
        if (advancementNode == null || advancementNode.advancement().display().isEmpty()) {
            return null;
        }
        return this.tab.getWidget(advancementNode.holder());
    }

    public void drawConnectivity(GuiGraphics guiGraphics, int n, int n2, boolean bl) {
        if (this.parent != null) {
            int n3;
            int n4 = n + this.parent.x + 13;
            int n5 = n + this.parent.x + 26 + 4;
            int n6 = n2 + this.parent.y + 13;
            int n7 = n + this.x + 13;
            int n8 = n2 + this.y + 13;
            int n9 = n3 = bl ? -16777216 : -1;
            if (bl) {
                guiGraphics.hLine(n5, n4, n6 - 1, n3);
                guiGraphics.hLine(n5 + 1, n4, n6, n3);
                guiGraphics.hLine(n5, n4, n6 + 1, n3);
                guiGraphics.hLine(n7, n5 - 1, n8 - 1, n3);
                guiGraphics.hLine(n7, n5 - 1, n8, n3);
                guiGraphics.hLine(n7, n5 - 1, n8 + 1, n3);
                guiGraphics.vLine(n5 - 1, n8, n6, n3);
                guiGraphics.vLine(n5 + 1, n8, n6, n3);
            } else {
                guiGraphics.hLine(n5, n4, n6, n3);
                guiGraphics.hLine(n7, n5, n8, n3);
                guiGraphics.vLine(n5, n8, n6, n3);
            }
        }
        for (AdvancementWidget advancementWidget : this.children) {
            advancementWidget.drawConnectivity(guiGraphics, n, n2, bl);
        }
    }

    /*
     * WARNING - void declaration
     */
    public void draw(GuiGraphics guiGraphics, int n, int n2) {
        if (!this.display.isHidden() || this.progress != null && this.progress.isDone()) {
            void var5_8;
            float f;
            float f2 = f = this.progress == null ? 0.0f : this.progress.getPercent();
            if (f >= 1.0f) {
                AdvancementWidgetType object = AdvancementWidgetType.OBTAINED;
            } else {
                AdvancementWidgetType advancementWidgetType = AdvancementWidgetType.UNOBTAINED;
            }
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, var5_8.frameSprite(this.display.getType()), n + this.x + 3, n2 + this.y, 26, 26);
            guiGraphics.renderFakeItem(this.display.getIcon(), n + this.x + 8, n2 + this.y + 5);
        }
        for (AdvancementWidget advancementWidget : this.children) {
            advancementWidget.draw(guiGraphics, n, n2);
        }
    }

    public int getWidth() {
        return this.width;
    }

    public void setProgress(AdvancementProgress advancementProgress) {
        this.progress = advancementProgress;
    }

    public void addChild(AdvancementWidget advancementWidget) {
        this.children.add(advancementWidget);
    }

    public void drawHover(GuiGraphics guiGraphics, int n, int n2, float f, int n3, int n4) {
        AdvancementWidgetType advancementWidgetType;
        AdvancementWidgetType advancementWidgetType2;
        AdvancementWidgetType advancementWidgetType3;
        Font font = this.minecraft.font;
        int n5 = font.lineHeight * this.titleLines.size() + 9 + 8;
        int n6 = n2 + this.y + (26 - n5) / 2;
        int n7 = n6 + n5;
        int n8 = this.description.size() * font.lineHeight;
        int n9 = 6 + n8;
        boolean bl = n3 + n + this.x + this.width + 26 >= this.tab.getScreen().width;
        Component component = this.progress == null ? null : this.progress.getProgressText();
        int n10 = component == null ? 0 : font.width(component);
        boolean bl2 = n7 + n9 >= 113;
        float f2 = this.progress == null ? 0.0f : this.progress.getPercent();
        int n11 = Mth.floor(f2 * (float)this.width);
        if (f2 >= 1.0f) {
            n11 = this.width / 2;
            advancementWidgetType3 = AdvancementWidgetType.OBTAINED;
            advancementWidgetType2 = AdvancementWidgetType.OBTAINED;
            advancementWidgetType = AdvancementWidgetType.OBTAINED;
        } else if (n11 < 2) {
            n11 = this.width / 2;
            advancementWidgetType3 = AdvancementWidgetType.UNOBTAINED;
            advancementWidgetType2 = AdvancementWidgetType.UNOBTAINED;
            advancementWidgetType = AdvancementWidgetType.UNOBTAINED;
        } else if (n11 > this.width - 2) {
            n11 = this.width / 2;
            advancementWidgetType3 = AdvancementWidgetType.OBTAINED;
            advancementWidgetType2 = AdvancementWidgetType.OBTAINED;
            advancementWidgetType = AdvancementWidgetType.UNOBTAINED;
        } else {
            advancementWidgetType3 = AdvancementWidgetType.OBTAINED;
            advancementWidgetType2 = AdvancementWidgetType.UNOBTAINED;
            advancementWidgetType = AdvancementWidgetType.UNOBTAINED;
        }
        int n12 = this.width - n11;
        int n13 = bl ? n + this.x - this.width + 26 + 6 : n + this.x;
        int n14 = n5 + n9;
        if (!this.description.isEmpty()) {
            if (bl2) {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, TITLE_BOX_SPRITE, n13, n7 - n14, this.width, n14);
            } else {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, TITLE_BOX_SPRITE, n13, n6, this.width, n14);
            }
        }
        if (advancementWidgetType3 != advancementWidgetType2) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, advancementWidgetType3.boxSprite(), 200, n5, 0, 0, n13, n6, n11, n5);
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, advancementWidgetType2.boxSprite(), 200, n5, 200 - n12, 0, n13 + n11, n6, n12, n5);
        } else {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, advancementWidgetType3.boxSprite(), n13, n6, this.width, n5);
        }
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, advancementWidgetType.frameSprite(this.display.getType()), n + this.x + 3, n2 + this.y, 26, 26);
        int n15 = n13 + 5;
        if (bl) {
            this.drawMultilineText(guiGraphics, this.titleLines, n15, n6 + 9, -1);
            if (component != null) {
                guiGraphics.drawString(font, component, n + this.x - n10, n6 + 9, -1);
            }
        } else {
            this.drawMultilineText(guiGraphics, this.titleLines, n + this.x + 32, n6 + 9, -1);
            if (component != null) {
                guiGraphics.drawString(font, component, n + this.x + this.width - n10 - 5, n6 + 9, -1);
            }
        }
        if (bl2) {
            this.drawMultilineText(guiGraphics, this.description, n15, n6 - n8 + 1, -16711936);
        } else {
            this.drawMultilineText(guiGraphics, this.description, n15, n7, -16711936);
        }
        guiGraphics.renderFakeItem(this.display.getIcon(), n + this.x + 8, n2 + this.y + 5);
    }

    private void drawMultilineText(GuiGraphics guiGraphics, List<FormattedCharSequence> list, int n, int n2, int n3) {
        Font font = this.minecraft.font;
        for (int i = 0; i < list.size(); ++i) {
            guiGraphics.drawString(font, list.get(i), n, n2 + i * font.lineHeight, n3);
        }
    }

    public boolean isMouseOver(int n, int n2, int n3, int n4) {
        if (this.display.isHidden() && (this.progress == null || !this.progress.isDone())) {
            return false;
        }
        int n5 = n + this.x;
        int n6 = n5 + 26;
        int n7 = n2 + this.y;
        int n8 = n7 + 26;
        return n3 >= n5 && n3 <= n6 && n4 >= n7 && n4 <= n8;
    }

    public void attachToParent() {
        if (this.parent == null && this.advancementNode.parent() != null) {
            this.parent = this.getFirstVisibleParent(this.advancementNode);
            if (this.parent != null) {
                this.parent.addChild(this);
            }
        }
    }

    public int getY() {
        return this.y;
    }

    public int getX() {
        return this.x;
    }
}

