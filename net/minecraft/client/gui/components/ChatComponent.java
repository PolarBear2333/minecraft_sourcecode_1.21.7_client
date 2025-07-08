/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Optionull;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.Style;
import net.minecraft.util.ARGB;
import net.minecraft.util.ArrayListDeque;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.ChatVisiblity;
import org.slf4j.Logger;

public class ChatComponent {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_CHAT_HISTORY = 100;
    private static final int MESSAGE_NOT_FOUND = -1;
    private static final int MESSAGE_INDENT = 4;
    private static final int MESSAGE_TAG_MARGIN_LEFT = 4;
    private static final int BOTTOM_MARGIN = 40;
    private static final int TIME_BEFORE_MESSAGE_DELETION = 60;
    private static final Component DELETED_CHAT_MESSAGE = Component.translatable("chat.deleted_marker").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
    private final Minecraft minecraft;
    private final ArrayListDeque<String> recentChat = new ArrayListDeque(100);
    private final List<GuiMessage> allMessages = Lists.newArrayList();
    private final List<GuiMessage.Line> trimmedMessages = Lists.newArrayList();
    private int chatScrollbarPos;
    private boolean newMessageSinceScroll;
    private final List<DelayedMessageDeletion> messageDeletionQueue = new ArrayList<DelayedMessageDeletion>();

    public ChatComponent(Minecraft minecraft) {
        this.minecraft = minecraft;
        this.recentChat.addAll(minecraft.commandHistory().history());
    }

    public void tick() {
        if (!this.messageDeletionQueue.isEmpty()) {
            this.processMessageDeletionQueue();
        }
    }

    private int forEachLine(int n, int n2, boolean bl, int n3, LineConsumer lineConsumer) {
        int n4 = this.getLineHeight();
        int n5 = 0;
        for (int i = Math.min(this.trimmedMessages.size() - this.chatScrollbarPos, n) - 1; i >= 0; --i) {
            float f;
            int n6 = i + this.chatScrollbarPos;
            GuiMessage.Line line = this.trimmedMessages.get(n6);
            if (line == null) continue;
            int n7 = n2 - line.addedTime();
            float f2 = f = bl ? 1.0f : (float)ChatComponent.getTimeFactor(n7);
            if (!(f > 1.0E-5f)) continue;
            ++n5;
            int n8 = n3 - i * n4;
            int n9 = n8 - n4;
            lineConsumer.accept(0, n9, n8, line, i, f);
        }
        return n5;
    }

    public void render(GuiGraphics guiGraphics, int n, int n8, int n9, boolean bl) {
        int n10;
        int n11;
        if (this.isChatHidden()) {
            return;
        }
        int n12 = this.getLinesPerPage();
        int n13 = this.trimmedMessages.size();
        if (n13 <= 0) {
            return;
        }
        ProfilerFiller profilerFiller = Profiler.get();
        profilerFiller.push("chat");
        float f = (float)this.getScale();
        int n14 = Mth.ceil((float)this.getWidth() / f);
        int n15 = guiGraphics.guiHeight();
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().scale(f, f);
        guiGraphics.pose().translate(4.0f, 0.0f);
        int n16 = Mth.floor((float)(n15 - 40) / f);
        int n17 = this.getMessageEndIndexAt(this.screenToChatX(n8), this.screenToChatY(n9));
        float f4 = this.minecraft.options.chatOpacity().get().floatValue() * 0.9f + 0.1f;
        float f5 = this.minecraft.options.textBackgroundOpacity().get().floatValue();
        double d = this.minecraft.options.chatLineSpacing().get();
        int n18 = (int)Math.round(-8.0 * (d + 1.0) + 4.0 * d);
        this.forEachLine(n12, n, bl, n16, (n4, n5, n6, line, n7, f3) -> {
            guiGraphics.fill(n4 - 4, n5, n4 + n14 + 4 + 4, n6, ARGB.color(f3 * f5, -16777216));
            GuiMessageTag guiMessageTag = line.tag();
            if (guiMessageTag != null) {
                int n8 = ARGB.color(f3 * f4, guiMessageTag.indicatorColor());
                guiGraphics.fill(n4 - 4, n5, n4 - 2, n6, n8);
                if (n7 == n17 && guiMessageTag.icon() != null) {
                    int n9 = this.getTagIconLeft(line);
                    int n10 = n6 + n18 + this.minecraft.font.lineHeight;
                    this.drawTagIcon(guiGraphics, n9, n10, guiMessageTag.icon());
                }
            }
        });
        int n19 = this.forEachLine(n12, n, bl, n16, (n2, n3, n4, line, n5, f2) -> {
            int n6 = n4 + n18;
            guiGraphics.drawString(this.minecraft.font, line.content(), n2, n6, ARGB.color(f2 * f4, -1));
        });
        long l = this.minecraft.getChatListener().queueSize();
        if (l > 0L) {
            n11 = (int)(128.0f * f4);
            n10 = (int)(255.0f * f5);
            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().translate(0.0f, (float)n16);
            guiGraphics.fill(-2, 0, n14 + 4, 9, n10 << 24);
            guiGraphics.drawString(this.minecraft.font, Component.translatable("chat.queue", l), 0, 1, ARGB.color(n11, -1));
            guiGraphics.pose().popMatrix();
        }
        if (bl) {
            n11 = this.getLineHeight();
            n10 = n13 * n11;
            int n20 = n19 * n11;
            int n21 = this.chatScrollbarPos * n20 / n13 - n16;
            int n22 = n20 * n20 / n10;
            if (n10 != n20) {
                int n23 = n21 > 0 ? 170 : 96;
                int n24 = this.newMessageSinceScroll ? 0xCC3333 : 0x3333AA;
                int n25 = n14 + 4;
                guiGraphics.fill(n25, -n21, n25 + 2, -n21 - n22, ARGB.color(n23, n24));
                guiGraphics.fill(n25 + 2, -n21, n25 + 1, -n21 - n22, ARGB.color(n23, 0xCCCCCC));
            }
        }
        guiGraphics.pose().popMatrix();
        profilerFiller.pop();
    }

    private void drawTagIcon(GuiGraphics guiGraphics, int n, int n2, GuiMessageTag.Icon icon) {
        int n3 = n2 - icon.height - 1;
        icon.draw(guiGraphics, n, n3);
    }

    private int getTagIconLeft(GuiMessage.Line line) {
        return this.minecraft.font.width(line.content()) + 4;
    }

    private boolean isChatHidden() {
        return this.minecraft.options.chatVisibility().get() == ChatVisiblity.HIDDEN;
    }

    private static double getTimeFactor(int n) {
        double d = (double)n / 200.0;
        d = 1.0 - d;
        d *= 10.0;
        d = Mth.clamp(d, 0.0, 1.0);
        d *= d;
        return d;
    }

    public void clearMessages(boolean bl) {
        this.minecraft.getChatListener().clearQueue();
        this.messageDeletionQueue.clear();
        this.trimmedMessages.clear();
        this.allMessages.clear();
        if (bl) {
            this.recentChat.clear();
            this.recentChat.addAll(this.minecraft.commandHistory().history());
        }
    }

    public void addMessage(Component component) {
        this.addMessage(component, null, this.minecraft.isSingleplayer() ? GuiMessageTag.systemSinglePlayer() : GuiMessageTag.system());
    }

    public void addMessage(Component component, @Nullable MessageSignature messageSignature, @Nullable GuiMessageTag guiMessageTag) {
        GuiMessage guiMessage = new GuiMessage(this.minecraft.gui.getGuiTicks(), component, messageSignature, guiMessageTag);
        this.logChatMessage(guiMessage);
        this.addMessageToDisplayQueue(guiMessage);
        this.addMessageToQueue(guiMessage);
    }

    private void logChatMessage(GuiMessage guiMessage) {
        String string = guiMessage.content().getString().replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n");
        String string2 = Optionull.map(guiMessage.tag(), GuiMessageTag::logTag);
        if (string2 != null) {
            LOGGER.info("[{}] [CHAT] {}", (Object)string2, (Object)string);
        } else {
            LOGGER.info("[CHAT] {}", (Object)string);
        }
    }

    private void addMessageToDisplayQueue(GuiMessage guiMessage) {
        int n = Mth.floor((double)this.getWidth() / this.getScale());
        GuiMessageTag.Icon icon = guiMessage.icon();
        if (icon != null) {
            n -= icon.width + 4 + 2;
        }
        List<FormattedCharSequence> list = ComponentRenderUtils.wrapComponents(guiMessage.content(), n, this.minecraft.font);
        boolean bl = this.isChatFocused();
        for (int i = 0; i < list.size(); ++i) {
            FormattedCharSequence formattedCharSequence = list.get(i);
            if (bl && this.chatScrollbarPos > 0) {
                this.newMessageSinceScroll = true;
                this.scrollChat(1);
            }
            boolean bl2 = i == list.size() - 1;
            this.trimmedMessages.add(0, new GuiMessage.Line(guiMessage.addedTime(), formattedCharSequence, guiMessage.tag(), bl2));
        }
        while (this.trimmedMessages.size() > 100) {
            this.trimmedMessages.remove(this.trimmedMessages.size() - 1);
        }
    }

    private void addMessageToQueue(GuiMessage guiMessage) {
        this.allMessages.add(0, guiMessage);
        while (this.allMessages.size() > 100) {
            this.allMessages.remove(this.allMessages.size() - 1);
        }
    }

    private void processMessageDeletionQueue() {
        int n = this.minecraft.gui.getGuiTicks();
        this.messageDeletionQueue.removeIf(delayedMessageDeletion -> {
            if (n >= delayedMessageDeletion.deletableAfter()) {
                return this.deleteMessageOrDelay(delayedMessageDeletion.signature()) == null;
            }
            return false;
        });
    }

    public void deleteMessage(MessageSignature messageSignature) {
        DelayedMessageDeletion delayedMessageDeletion = this.deleteMessageOrDelay(messageSignature);
        if (delayedMessageDeletion != null) {
            this.messageDeletionQueue.add(delayedMessageDeletion);
        }
    }

    @Nullable
    private DelayedMessageDeletion deleteMessageOrDelay(MessageSignature messageSignature) {
        int n = this.minecraft.gui.getGuiTicks();
        ListIterator<GuiMessage> listIterator = this.allMessages.listIterator();
        while (listIterator.hasNext()) {
            GuiMessage guiMessage = listIterator.next();
            if (!messageSignature.equals(guiMessage.signature())) continue;
            int n2 = guiMessage.addedTime() + 60;
            if (n >= n2) {
                listIterator.set(this.createDeletedMarker(guiMessage));
                this.refreshTrimmedMessages();
                return null;
            }
            return new DelayedMessageDeletion(messageSignature, n2);
        }
        return null;
    }

    private GuiMessage createDeletedMarker(GuiMessage guiMessage) {
        return new GuiMessage(guiMessage.addedTime(), DELETED_CHAT_MESSAGE, null, GuiMessageTag.system());
    }

    public void rescaleChat() {
        this.resetChatScroll();
        this.refreshTrimmedMessages();
    }

    private void refreshTrimmedMessages() {
        this.trimmedMessages.clear();
        for (GuiMessage guiMessage : Lists.reverse(this.allMessages)) {
            this.addMessageToDisplayQueue(guiMessage);
        }
    }

    public ArrayListDeque<String> getRecentChat() {
        return this.recentChat;
    }

    public void addRecentChat(String string) {
        if (!string.equals(this.recentChat.peekLast())) {
            if (this.recentChat.size() >= 100) {
                this.recentChat.removeFirst();
            }
            this.recentChat.addLast(string);
        }
        if (string.startsWith("/")) {
            this.minecraft.commandHistory().addCommand(string);
        }
    }

    public void resetChatScroll() {
        this.chatScrollbarPos = 0;
        this.newMessageSinceScroll = false;
    }

    public void scrollChat(int n) {
        this.chatScrollbarPos += n;
        int n2 = this.trimmedMessages.size();
        if (this.chatScrollbarPos > n2 - this.getLinesPerPage()) {
            this.chatScrollbarPos = n2 - this.getLinesPerPage();
        }
        if (this.chatScrollbarPos <= 0) {
            this.chatScrollbarPos = 0;
            this.newMessageSinceScroll = false;
        }
    }

    public boolean handleChatQueueClicked(double d, double d2) {
        if (!this.isChatFocused() || this.minecraft.options.hideGui || this.isChatHidden()) {
            return false;
        }
        ChatListener chatListener = this.minecraft.getChatListener();
        if (chatListener.queueSize() == 0L) {
            return false;
        }
        double d3 = d - 2.0;
        double d4 = (double)this.minecraft.getWindow().getGuiScaledHeight() - d2 - 40.0;
        if (d3 <= (double)Mth.floor((double)this.getWidth() / this.getScale()) && d4 < 0.0 && d4 > (double)Mth.floor(-9.0 * this.getScale())) {
            chatListener.acceptNextDelayedMessage();
            return true;
        }
        return false;
    }

    @Nullable
    public Style getClickedComponentStyleAt(double d, double d2) {
        double d3;
        double d4 = this.screenToChatX(d);
        int n = this.getMessageLineIndexAt(d4, d3 = this.screenToChatY(d2));
        if (n >= 0 && n < this.trimmedMessages.size()) {
            GuiMessage.Line line = this.trimmedMessages.get(n);
            return this.minecraft.font.getSplitter().componentStyleAtWidth(line.content(), Mth.floor(d4));
        }
        return null;
    }

    @Nullable
    public GuiMessageTag getMessageTagAt(double d, double d2) {
        GuiMessage.Line line;
        GuiMessageTag guiMessageTag;
        double d3;
        double d4 = this.screenToChatX(d);
        int n = this.getMessageEndIndexAt(d4, d3 = this.screenToChatY(d2));
        if (n >= 0 && n < this.trimmedMessages.size() && (guiMessageTag = (line = this.trimmedMessages.get(n)).tag()) != null && this.hasSelectedMessageTag(d4, line, guiMessageTag)) {
            return guiMessageTag;
        }
        return null;
    }

    private boolean hasSelectedMessageTag(double d, GuiMessage.Line line, GuiMessageTag guiMessageTag) {
        if (d < 0.0) {
            return true;
        }
        GuiMessageTag.Icon icon = guiMessageTag.icon();
        if (icon != null) {
            int n = this.getTagIconLeft(line);
            int n2 = n + icon.width;
            return d >= (double)n && d <= (double)n2;
        }
        return false;
    }

    private double screenToChatX(double d) {
        return d / this.getScale() - 4.0;
    }

    private double screenToChatY(double d) {
        double d2 = (double)this.minecraft.getWindow().getGuiScaledHeight() - d - 40.0;
        return d2 / (this.getScale() * (double)this.getLineHeight());
    }

    private int getMessageEndIndexAt(double d, double d2) {
        int n = this.getMessageLineIndexAt(d, d2);
        if (n == -1) {
            return -1;
        }
        while (n >= 0) {
            if (this.trimmedMessages.get(n).endOfEntry()) {
                return n;
            }
            --n;
        }
        return n;
    }

    private int getMessageLineIndexAt(double d, double d2) {
        int n;
        if (!this.isChatFocused() || this.isChatHidden()) {
            return -1;
        }
        if (d < -4.0 || d > (double)Mth.floor((double)this.getWidth() / this.getScale())) {
            return -1;
        }
        int n2 = Math.min(this.getLinesPerPage(), this.trimmedMessages.size());
        if (d2 >= 0.0 && d2 < (double)n2 && (n = Mth.floor(d2 + (double)this.chatScrollbarPos)) >= 0 && n < this.trimmedMessages.size()) {
            return n;
        }
        return -1;
    }

    public boolean isChatFocused() {
        return this.minecraft.screen instanceof ChatScreen;
    }

    public int getWidth() {
        return ChatComponent.getWidth(this.minecraft.options.chatWidth().get());
    }

    public int getHeight() {
        return ChatComponent.getHeight(this.isChatFocused() ? this.minecraft.options.chatHeightFocused().get() : this.minecraft.options.chatHeightUnfocused().get());
    }

    public double getScale() {
        return this.minecraft.options.chatScale().get();
    }

    public static int getWidth(double d) {
        int n = 320;
        int n2 = 40;
        return Mth.floor(d * 280.0 + 40.0);
    }

    public static int getHeight(double d) {
        int n = 180;
        int n2 = 20;
        return Mth.floor(d * 160.0 + 20.0);
    }

    public static double defaultUnfocusedPct() {
        int n = 180;
        int n2 = 20;
        return 70.0 / (double)(ChatComponent.getHeight(1.0) - 20);
    }

    public int getLinesPerPage() {
        return this.getHeight() / this.getLineHeight();
    }

    private int getLineHeight() {
        return (int)((double)this.minecraft.font.lineHeight * (this.minecraft.options.chatLineSpacing().get() + 1.0));
    }

    public State storeState() {
        return new State(List.copyOf(this.allMessages), List.copyOf(this.recentChat), List.copyOf(this.messageDeletionQueue));
    }

    public void restoreState(State state) {
        this.recentChat.clear();
        this.recentChat.addAll(state.history);
        this.messageDeletionQueue.clear();
        this.messageDeletionQueue.addAll(state.delayedMessageDeletions);
        this.allMessages.clear();
        this.allMessages.addAll(state.messages);
        this.refreshTrimmedMessages();
    }

    @FunctionalInterface
    static interface LineConsumer {
        public void accept(int var1, int var2, int var3, GuiMessage.Line var4, int var5, float var6);
    }

    record DelayedMessageDeletion(MessageSignature signature, int deletableAfter) {
    }

    public static class State {
        final List<GuiMessage> messages;
        final List<String> history;
        final List<DelayedMessageDeletion> delayedMessageDeletions;

        public State(List<GuiMessage> list, List<String> list2, List<DelayedMessageDeletion> list3) {
            this.messages = list;
            this.history = list2;
            this.delayedMessageDeletions = list3;
        }
    }
}

