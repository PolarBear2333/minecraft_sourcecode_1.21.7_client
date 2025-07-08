/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;

public class InBedChatScreen
extends ChatScreen {
    private Button leaveBedButton;

    public InBedChatScreen() {
        super("");
    }

    @Override
    protected void init() {
        super.init();
        this.leaveBedButton = Button.builder(Component.translatable("multiplayer.stopSleeping"), button -> this.sendWakeUp()).bounds(this.width / 2 - 100, this.height - 40, 200, 20).build();
        this.addRenderableWidget(this.leaveBedButton);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        if (!this.minecraft.getChatStatus().isChatAllowed(this.minecraft.isLocalServer())) {
            this.leaveBedButton.render(guiGraphics, n, n2, f);
            return;
        }
        super.render(guiGraphics, n, n2, f);
    }

    @Override
    public void onClose() {
        this.sendWakeUp();
    }

    @Override
    public boolean charTyped(char c, int n) {
        if (!this.minecraft.getChatStatus().isChatAllowed(this.minecraft.isLocalServer())) {
            return true;
        }
        return super.charTyped(c, n);
    }

    @Override
    public boolean keyPressed(int n, int n2, int n3) {
        if (n == 256) {
            this.sendWakeUp();
        }
        if (!this.minecraft.getChatStatus().isChatAllowed(this.minecraft.isLocalServer())) {
            return true;
        }
        if (n == 257 || n == 335) {
            this.handleChatInput(this.input.getValue(), true);
            this.input.setValue("");
            this.minecraft.gui.getChat().resetChatScroll();
            return true;
        }
        return super.keyPressed(n, n2, n3);
    }

    private void sendWakeUp() {
        ClientPacketListener clientPacketListener = this.minecraft.player.connection;
        clientPacketListener.send(new ServerboundPlayerCommandPacket(this.minecraft.player, ServerboundPlayerCommandPacket.Action.STOP_SLEEPING));
    }

    public void onPlayerWokeUp() {
        if (this.input.getValue().isEmpty()) {
            this.minecraft.setScreen(null);
        } else {
            this.minecraft.setScreen(new ChatScreen(this.input.getValue()));
        }
    }
}

