/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  com.mojang.authlib.minecraft.report.AbuseReportLimits
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens.reporting;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.report.AbuseReportLimits;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.Optionull;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.reporting.ChatSelectionLogFiller;
import net.minecraft.client.multiplayer.chat.ChatTrustLevel;
import net.minecraft.client.multiplayer.chat.LoggedChatMessage;
import net.minecraft.client.multiplayer.chat.report.ChatReport;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

public class ChatSelectionScreen
extends Screen {
    static final ResourceLocation CHECKMARK_SPRITE = ResourceLocation.withDefaultNamespace("icon/checkmark");
    private static final Component TITLE = Component.translatable("gui.chatSelection.title");
    private static final Component CONTEXT_INFO = Component.translatable("gui.chatSelection.context");
    @Nullable
    private final Screen lastScreen;
    private final ReportingContext reportingContext;
    private Button confirmSelectedButton;
    private MultiLineLabel contextInfoLabel;
    @Nullable
    private ChatSelectionList chatSelectionList;
    final ChatReport.Builder report;
    private final Consumer<ChatReport.Builder> onSelected;
    private ChatSelectionLogFiller chatLogFiller;

    public ChatSelectionScreen(@Nullable Screen screen, ReportingContext reportingContext, ChatReport.Builder builder, Consumer<ChatReport.Builder> consumer) {
        super(TITLE);
        this.lastScreen = screen;
        this.reportingContext = reportingContext;
        this.report = builder.copy();
        this.onSelected = consumer;
    }

    @Override
    protected void init() {
        this.chatLogFiller = new ChatSelectionLogFiller(this.reportingContext, this::canReport);
        this.contextInfoLabel = MultiLineLabel.create(this.font, CONTEXT_INFO, this.width - 16);
        this.chatSelectionList = this.addRenderableWidget(new ChatSelectionList(this.minecraft, (this.contextInfoLabel.getLineCount() + 1) * this.font.lineHeight));
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, button -> this.onClose()).bounds(this.width / 2 - 155, this.height - 32, 150, 20).build());
        this.confirmSelectedButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
            this.onSelected.accept(this.report);
            this.onClose();
        }).bounds(this.width / 2 - 155 + 160, this.height - 32, 150, 20).build());
        this.updateConfirmSelectedButton();
        this.extendLog();
        this.chatSelectionList.setScrollAmount(this.chatSelectionList.maxScrollAmount());
    }

    private boolean canReport(LoggedChatMessage loggedChatMessage) {
        return loggedChatMessage.canReport(this.report.reportedProfileId());
    }

    private void extendLog() {
        int n = this.chatSelectionList.getMaxVisibleEntries();
        this.chatLogFiller.fillNextPage(n, this.chatSelectionList);
    }

    void onReachedScrollTop() {
        this.extendLog();
    }

    void updateConfirmSelectedButton() {
        this.confirmSelectedButton.active = !this.report.reportedMessages().isEmpty();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        super.render(guiGraphics, n, n2, f);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 10, -1);
        AbuseReportLimits abuseReportLimits = this.reportingContext.sender().reportLimits();
        int n3 = this.report.reportedMessages().size();
        int n4 = abuseReportLimits.maxReportedMessageCount();
        MutableComponent mutableComponent = Component.translatable("gui.chatSelection.selected", n3, n4);
        guiGraphics.drawCenteredString(this.font, mutableComponent, this.width / 2, 26, -1);
        this.contextInfoLabel.renderCentered(guiGraphics, this.width / 2, this.chatSelectionList.getFooterTop());
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinForNarration(super.getNarrationMessage(), CONTEXT_INFO);
    }

    public class ChatSelectionList
    extends ObjectSelectionList<Entry>
    implements ChatSelectionLogFiller.Output {
        @Nullable
        private Heading previousHeading;

        public ChatSelectionList(Minecraft minecraft, int n) {
            super(minecraft, ChatSelectionScreen.this.width, ChatSelectionScreen.this.height - n - 80, 40, 16);
        }

        @Override
        public void setScrollAmount(double d) {
            double d2 = this.scrollAmount();
            super.setScrollAmount(d);
            if ((float)this.maxScrollAmount() > 1.0E-5f && d <= (double)1.0E-5f && !Mth.equal(d, d2)) {
                ChatSelectionScreen.this.onReachedScrollTop();
            }
        }

        @Override
        public void acceptMessage(int n, LoggedChatMessage.Player player) {
            boolean bl = player.canReport(ChatSelectionScreen.this.report.reportedProfileId());
            ChatTrustLevel chatTrustLevel = player.trustLevel();
            GuiMessageTag guiMessageTag = chatTrustLevel.createTag(player.message());
            MessageEntry messageEntry = new MessageEntry(n, player.toContentComponent(), player.toNarrationComponent(), guiMessageTag, bl, true);
            this.addEntryToTop(messageEntry);
            this.updateHeading(player, bl);
        }

        private void updateHeading(LoggedChatMessage.Player player, boolean bl) {
            MessageHeadingEntry messageHeadingEntry = new MessageHeadingEntry(player.profile(), player.toHeadingComponent(), bl);
            this.addEntryToTop(messageHeadingEntry);
            Heading heading = new Heading(player.profileId(), messageHeadingEntry);
            if (this.previousHeading != null && this.previousHeading.canCombine(heading)) {
                this.removeEntryFromTop(this.previousHeading.entry());
            }
            this.previousHeading = heading;
        }

        @Override
        public void acceptDivider(Component component) {
            this.addEntryToTop(new PaddingEntry());
            this.addEntryToTop(new DividerEntry(component));
            this.addEntryToTop(new PaddingEntry());
            this.previousHeading = null;
        }

        @Override
        public int getRowWidth() {
            return Math.min(350, this.width - 50);
        }

        public int getMaxVisibleEntries() {
            return Mth.positiveCeilDiv(this.height, this.itemHeight);
        }

        @Override
        protected void renderItem(GuiGraphics guiGraphics, int n, int n2, float f, int n3, int n4, int n5, int n6, int n7) {
            Entry entry = (Entry)this.getEntry(n3);
            if (this.shouldHighlightEntry(entry)) {
                boolean bl = this.getSelected() == entry;
                int n8 = this.isFocused() && bl ? -1 : -8355712;
                this.renderSelection(guiGraphics, n5, n6, n7, n8, -16777216);
            }
            entry.render(guiGraphics, n3, n5, n4, n6, n7, n, n2, this.getHovered() == entry, f);
        }

        private boolean shouldHighlightEntry(Entry entry) {
            if (entry.canSelect()) {
                boolean bl = this.getSelected() == entry;
                boolean bl2 = this.getSelected() == null;
                boolean bl3 = this.getHovered() == entry;
                return bl || bl2 && bl3 && entry.canReport();
            }
            return false;
        }

        @Override
        @Nullable
        protected Entry nextEntry(ScreenDirection screenDirection) {
            return this.nextEntry(screenDirection, Entry::canSelect);
        }

        @Override
        public void setSelected(@Nullable Entry entry) {
            super.setSelected(entry);
            Entry entry2 = this.nextEntry(ScreenDirection.UP);
            if (entry2 == null) {
                ChatSelectionScreen.this.onReachedScrollTop();
            }
        }

        @Override
        public boolean keyPressed(int n, int n2, int n3) {
            Entry entry = (Entry)this.getSelected();
            if (entry != null && entry.keyPressed(n, n2, n3)) {
                return true;
            }
            return super.keyPressed(n, n2, n3);
        }

        public int getFooterTop() {
            return this.getBottom() + ((ChatSelectionScreen)ChatSelectionScreen.this).font.lineHeight;
        }

        @Override
        @Nullable
        protected /* synthetic */ AbstractSelectionList.Entry nextEntry(ScreenDirection screenDirection) {
            return this.nextEntry(screenDirection);
        }

        public class MessageEntry
        extends Entry {
            private static final int CHECKMARK_WIDTH = 9;
            private static final int CHECKMARK_HEIGHT = 8;
            private static final int INDENT_AMOUNT = 11;
            private static final int TAG_MARGIN_LEFT = 4;
            private final int chatId;
            private final FormattedText text;
            private final Component narration;
            @Nullable
            private final List<FormattedCharSequence> hoverText;
            @Nullable
            private final GuiMessageTag.Icon tagIcon;
            @Nullable
            private final List<FormattedCharSequence> tagHoverText;
            private final boolean canReport;
            private final boolean playerMessage;

            public MessageEntry(int n, Component component, @Nullable Component component2, GuiMessageTag guiMessageTag, boolean bl, boolean bl2) {
                this.chatId = n;
                this.tagIcon = Optionull.map(guiMessageTag, GuiMessageTag::icon);
                this.tagHoverText = guiMessageTag != null && guiMessageTag.text() != null ? ChatSelectionScreen.this.font.split(guiMessageTag.text(), ChatSelectionList.this.getRowWidth()) : null;
                this.canReport = bl;
                this.playerMessage = bl2;
                FormattedText formattedText = ChatSelectionScreen.this.font.substrByWidth(component, this.getMaximumTextWidth() - ChatSelectionScreen.this.font.width(CommonComponents.ELLIPSIS));
                if (component != formattedText) {
                    this.text = FormattedText.composite(formattedText, CommonComponents.ELLIPSIS);
                    this.hoverText = ChatSelectionScreen.this.font.split(component, ChatSelectionList.this.getRowWidth());
                } else {
                    this.text = component;
                    this.hoverText = null;
                }
                this.narration = component2;
            }

            @Override
            public void render(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, int n5, int n6, int n7, boolean bl, float f) {
                if (this.isSelected() && this.canReport) {
                    this.renderSelectedCheckmark(guiGraphics, n2, n3, n5);
                }
                int n8 = n3 + this.getTextIndent();
                int n9 = n2 + 1 + (n5 - ((ChatSelectionScreen)ChatSelectionScreen.this).font.lineHeight) / 2;
                guiGraphics.drawString(ChatSelectionScreen.this.font, Language.getInstance().getVisualOrder(this.text), n8, n9, this.canReport ? -1 : -1593835521);
                if (this.hoverText != null && bl) {
                    guiGraphics.setTooltipForNextFrame(this.hoverText, n6, n7);
                }
                int n10 = ChatSelectionScreen.this.font.width(this.text);
                this.renderTag(guiGraphics, n8 + n10 + 4, n2, n5, n6, n7);
            }

            private void renderTag(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, int n5) {
                if (this.tagIcon != null) {
                    int n6 = n2 + (n3 - this.tagIcon.height) / 2;
                    this.tagIcon.draw(guiGraphics, n, n6);
                    if (this.tagHoverText != null && n4 >= n && n4 <= n + this.tagIcon.width && n5 >= n6 && n5 <= n6 + this.tagIcon.height) {
                        guiGraphics.setTooltipForNextFrame(this.tagHoverText, n4, n5);
                    }
                }
            }

            private void renderSelectedCheckmark(GuiGraphics guiGraphics, int n, int n2, int n3) {
                int n4 = n2;
                int n5 = n + (n3 - 8) / 2;
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, CHECKMARK_SPRITE, n4, n5, 9, 8);
            }

            private int getMaximumTextWidth() {
                int n = this.tagIcon != null ? this.tagIcon.width + 4 : 0;
                return ChatSelectionList.this.getRowWidth() - this.getTextIndent() - 4 - n;
            }

            private int getTextIndent() {
                return this.playerMessage ? 11 : 0;
            }

            @Override
            public Component getNarration() {
                return this.isSelected() ? Component.translatable("narrator.select", this.narration) : this.narration;
            }

            @Override
            public boolean mouseClicked(double d, double d2, int n) {
                ChatSelectionList.this.setSelected((Entry)null);
                return this.toggleReport();
            }

            @Override
            public boolean keyPressed(int n, int n2, int n3) {
                if (CommonInputs.selected(n)) {
                    return this.toggleReport();
                }
                return false;
            }

            @Override
            public boolean isSelected() {
                return ChatSelectionScreen.this.report.isReported(this.chatId);
            }

            @Override
            public boolean canSelect() {
                return true;
            }

            @Override
            public boolean canReport() {
                return this.canReport;
            }

            private boolean toggleReport() {
                if (this.canReport) {
                    ChatSelectionScreen.this.report.toggleReported(this.chatId);
                    ChatSelectionScreen.this.updateConfirmSelectedButton();
                    return true;
                }
                return false;
            }
        }

        public class MessageHeadingEntry
        extends Entry {
            private static final int FACE_SIZE = 12;
            private static final int PADDING = 4;
            private final Component heading;
            private final Supplier<PlayerSkin> skin;
            private final boolean canReport;

            public MessageHeadingEntry(GameProfile gameProfile, Component component, boolean bl) {
                this.heading = component;
                this.canReport = bl;
                this.skin = ChatSelectionList.this.minecraft.getSkinManager().lookupInsecure(gameProfile);
            }

            @Override
            public void render(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, int n5, int n6, int n7, boolean bl, float f) {
                int n8 = n3 - 12 + 4;
                int n9 = n2 + (n5 - 12) / 2;
                PlayerFaceRenderer.draw(guiGraphics, this.skin.get(), n8, n9, 12);
                int n10 = n2 + 1 + (n5 - ((ChatSelectionScreen)ChatSelectionScreen.this).font.lineHeight) / 2;
                guiGraphics.drawString(ChatSelectionScreen.this.font, this.heading, n8 + 12 + 4, n10, this.canReport ? -1 : -1593835521);
            }
        }

        record Heading(UUID sender, Entry entry) {
            public boolean canCombine(Heading heading) {
                return heading.sender.equals(this.sender);
            }
        }

        public static abstract class Entry
        extends ObjectSelectionList.Entry<Entry> {
            @Override
            public Component getNarration() {
                return CommonComponents.EMPTY;
            }

            public boolean isSelected() {
                return false;
            }

            public boolean canSelect() {
                return false;
            }

            public boolean canReport() {
                return this.canSelect();
            }

            @Override
            public boolean mouseClicked(double d, double d2, int n) {
                return this.canSelect();
            }
        }

        public static class PaddingEntry
        extends Entry {
            @Override
            public void render(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, int n5, int n6, int n7, boolean bl, float f) {
            }
        }

        public class DividerEntry
        extends Entry {
            private final Component text;

            public DividerEntry(Component component) {
                this.text = component;
            }

            @Override
            public void render(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, int n5, int n6, int n7, boolean bl, float f) {
                int n8 = n2 + n5 / 2;
                int n9 = n3 + n4 - 8;
                int n10 = ChatSelectionScreen.this.font.width(this.text);
                int n11 = (n3 + n9 - n10) / 2;
                int n12 = n8 - ((ChatSelectionScreen)ChatSelectionScreen.this).font.lineHeight / 2;
                guiGraphics.drawString(ChatSelectionScreen.this.font, this.text, n11, n12, -6250336);
            }

            @Override
            public Component getNarration() {
                return this.text;
            }
        }
    }
}

