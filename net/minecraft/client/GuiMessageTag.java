/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client;

import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public record GuiMessageTag(int indicatorColor, @Nullable Icon icon, @Nullable Component text, @Nullable String logTag) {
    private static final Component SYSTEM_TEXT = Component.translatable("chat.tag.system");
    private static final Component SYSTEM_TEXT_SINGLE_PLAYER = Component.translatable("chat.tag.system_single_player");
    private static final Component CHAT_NOT_SECURE_TEXT = Component.translatable("chat.tag.not_secure");
    private static final Component CHAT_MODIFIED_TEXT = Component.translatable("chat.tag.modified");
    private static final Component CHAT_ERROR_TEXT = Component.translatable("chat.tag.error");
    private static final int CHAT_NOT_SECURE_INDICATOR_COLOR = 0xD0D0D0;
    private static final int CHAT_MODIFIED_INDICATOR_COLOR = 0x606060;
    private static final GuiMessageTag SYSTEM = new GuiMessageTag(0xD0D0D0, null, SYSTEM_TEXT, "System");
    private static final GuiMessageTag SYSTEM_SINGLE_PLAYER = new GuiMessageTag(0xD0D0D0, null, SYSTEM_TEXT_SINGLE_PLAYER, "System");
    private static final GuiMessageTag CHAT_NOT_SECURE = new GuiMessageTag(0xD0D0D0, null, CHAT_NOT_SECURE_TEXT, "Not Secure");
    private static final GuiMessageTag CHAT_ERROR = new GuiMessageTag(0xFF5555, null, CHAT_ERROR_TEXT, "Chat Error");

    public static GuiMessageTag system() {
        return SYSTEM;
    }

    public static GuiMessageTag systemSinglePlayer() {
        return SYSTEM_SINGLE_PLAYER;
    }

    public static GuiMessageTag chatNotSecure() {
        return CHAT_NOT_SECURE;
    }

    public static GuiMessageTag chatModified(String string) {
        MutableComponent mutableComponent = Component.literal(string).withStyle(ChatFormatting.GRAY);
        MutableComponent mutableComponent2 = Component.empty().append(CHAT_MODIFIED_TEXT).append(CommonComponents.NEW_LINE).append(mutableComponent);
        return new GuiMessageTag(0x606060, Icon.CHAT_MODIFIED, mutableComponent2, "Modified");
    }

    public static GuiMessageTag chatError() {
        return CHAT_ERROR;
    }

    public static enum Icon {
        CHAT_MODIFIED(ResourceLocation.withDefaultNamespace("icon/chat_modified"), 9, 9);

        public final ResourceLocation sprite;
        public final int width;
        public final int height;

        private Icon(ResourceLocation resourceLocation, int n2, int n3) {
            this.sprite = resourceLocation;
            this.width = n2;
            this.height = n3;
        }

        public void draw(GuiGraphics guiGraphics, int n, int n2) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.sprite, n, n2, this.width, this.height);
        }
    }
}

