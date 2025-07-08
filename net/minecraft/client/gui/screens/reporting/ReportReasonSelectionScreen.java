/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens.reporting;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.Optionull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.chat.report.ReportReason;
import net.minecraft.client.multiplayer.chat.report.ReportType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonLinks;

public class ReportReasonSelectionScreen
extends Screen {
    private static final Component REASON_TITLE = Component.translatable("gui.abuseReport.reason.title");
    private static final Component REASON_DESCRIPTION = Component.translatable("gui.abuseReport.reason.description");
    private static final Component READ_INFO_LABEL = Component.translatable("gui.abuseReport.read_info");
    private static final int DESCRIPTION_BOX_WIDTH = 320;
    private static final int DESCRIPTION_BOX_HEIGHT = 62;
    private static final int PADDING = 4;
    @Nullable
    private final Screen lastScreen;
    @Nullable
    private ReasonSelectionList reasonSelectionList;
    @Nullable
    ReportReason currentlySelectedReason;
    private final Consumer<ReportReason> onSelectedReason;
    final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    final ReportType reportType;

    public ReportReasonSelectionScreen(@Nullable Screen screen, @Nullable ReportReason reportReason, ReportType reportType, Consumer<ReportReason> consumer) {
        super(REASON_TITLE);
        this.lastScreen = screen;
        this.currentlySelectedReason = reportReason;
        this.onSelectedReason = consumer;
        this.reportType = reportType;
    }

    @Override
    protected void init() {
        this.layout.addTitleHeader(REASON_TITLE, this.font);
        LinearLayout linearLayout = this.layout.addToContents(LinearLayout.vertical().spacing(4));
        this.reasonSelectionList = linearLayout.addChild(new ReasonSelectionList(this.minecraft));
        ReasonSelectionList.Entry entry = Optionull.map(this.currentlySelectedReason, this.reasonSelectionList::findEntry);
        this.reasonSelectionList.setSelected(entry);
        linearLayout.addChild(SpacerElement.height(this.descriptionHeight()));
        LinearLayout linearLayout2 = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        linearLayout2.addChild(Button.builder(READ_INFO_LABEL, ConfirmLinkScreen.confirmLink((Screen)this, CommonLinks.REPORTING_HELP)).build());
        linearLayout2.addChild(Button.builder(CommonComponents.GUI_DONE, button -> {
            ReasonSelectionList.Entry entry = (ReasonSelectionList.Entry)this.reasonSelectionList.getSelected();
            if (entry != null) {
                this.onSelectedReason.accept(entry.getReason());
            }
            this.minecraft.setScreen(this.lastScreen);
        }).build());
        this.layout.visitWidgets(guiEventListener -> {
            AbstractWidget cfr_ignored_0 = (AbstractWidget)this.addRenderableWidget(guiEventListener);
        });
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        if (this.reasonSelectionList != null) {
            this.reasonSelectionList.updateSizeAndPosition(this.width, this.listHeight(), this.layout.getHeaderHeight());
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        super.render(guiGraphics, n, n2, f);
        guiGraphics.fill(this.descriptionLeft(), this.descriptionTop(), this.descriptionRight(), this.descriptionBottom(), -16777216);
        guiGraphics.renderOutline(this.descriptionLeft(), this.descriptionTop(), this.descriptionWidth(), this.descriptionHeight(), -1);
        guiGraphics.drawString(this.font, REASON_DESCRIPTION, this.descriptionLeft() + 4, this.descriptionTop() + 4, -1);
        ReasonSelectionList.Entry entry = (ReasonSelectionList.Entry)this.reasonSelectionList.getSelected();
        if (entry != null) {
            int n3 = this.descriptionLeft() + 4 + 16;
            int n4 = this.descriptionRight() - 4;
            int n5 = this.descriptionTop() + 4 + this.font.lineHeight + 2;
            int n6 = this.descriptionBottom() - 4;
            int n7 = n4 - n3;
            int n8 = n6 - n5;
            int n9 = this.font.wordWrapHeight(entry.reason.description(), n7);
            guiGraphics.drawWordWrap(this.font, entry.reason.description(), n3, n5 + (n8 - n9) / 2, n7, -1);
        }
    }

    private int descriptionLeft() {
        return (this.width - 320) / 2;
    }

    private int descriptionRight() {
        return (this.width + 320) / 2;
    }

    private int descriptionTop() {
        return this.descriptionBottom() - this.descriptionHeight();
    }

    private int descriptionBottom() {
        return this.height - this.layout.getFooterHeight() - 4;
    }

    private int descriptionWidth() {
        return 320;
    }

    private int descriptionHeight() {
        return 62;
    }

    int listHeight() {
        return this.layout.getContentHeight() - this.descriptionHeight() - 8;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    public class ReasonSelectionList
    extends ObjectSelectionList<Entry> {
        public ReasonSelectionList(Minecraft minecraft) {
            super(minecraft, ReportReasonSelectionScreen.this.width, ReportReasonSelectionScreen.this.listHeight(), ReportReasonSelectionScreen.this.layout.getHeaderHeight(), 18);
            for (ReportReason reportReason : ReportReason.values()) {
                if (ReportReason.getIncompatibleCategories(ReportReasonSelectionScreen.this.reportType).contains((Object)reportReason)) continue;
                this.addEntry(new Entry(reportReason));
            }
        }

        @Nullable
        public Entry findEntry(ReportReason reportReason) {
            return this.children().stream().filter(entry -> entry.reason == reportReason).findFirst().orElse(null);
        }

        @Override
        public int getRowWidth() {
            return 320;
        }

        @Override
        public void setSelected(@Nullable Entry entry) {
            super.setSelected(entry);
            ReportReasonSelectionScreen.this.currentlySelectedReason = entry != null ? entry.getReason() : null;
        }

        public class Entry
        extends ObjectSelectionList.Entry<Entry> {
            final ReportReason reason;

            public Entry(ReportReason reportReason) {
                this.reason = reportReason;
            }

            @Override
            public void render(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, int n5, int n6, int n7, boolean bl, float f) {
                int n8 = n3 + 1;
                int n9 = n2 + (n5 - ((ReportReasonSelectionScreen)ReportReasonSelectionScreen.this).font.lineHeight) / 2 + 1;
                guiGraphics.drawString(ReportReasonSelectionScreen.this.font, this.reason.title(), n8, n9, -1);
            }

            @Override
            public Component getNarration() {
                return Component.translatable("gui.abuseReport.reason.narration", this.reason.title(), this.reason.description());
            }

            @Override
            public boolean mouseClicked(double d, double d2, int n) {
                ReasonSelectionList.this.setSelected(this);
                return super.mouseClicked(d, d2, n);
            }

            public ReportReason getReason() {
                return this.reason;
            }
        }
    }
}

