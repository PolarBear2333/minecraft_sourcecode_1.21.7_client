/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  org.joml.Vector3f
 */
package net.minecraft.client.gui.screens.inventory;

import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.blockentity.AbstractSignRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.joml.Vector3f;

public abstract class AbstractSignEditScreen
extends Screen {
    protected final SignBlockEntity sign;
    private SignText text;
    private final String[] messages;
    private final boolean isFrontText;
    protected final WoodType woodType;
    private int frame;
    private int line;
    @Nullable
    private TextFieldHelper signField;

    public AbstractSignEditScreen(SignBlockEntity signBlockEntity, boolean bl, boolean bl2) {
        this(signBlockEntity, bl, bl2, Component.translatable("sign.edit"));
    }

    public AbstractSignEditScreen(SignBlockEntity signBlockEntity, boolean bl, boolean bl2, Component component) {
        super(component);
        this.sign = signBlockEntity;
        this.text = signBlockEntity.getText(bl);
        this.isFrontText = bl;
        this.woodType = SignBlock.getWoodType(signBlockEntity.getBlockState().getBlock());
        this.messages = (String[])IntStream.range(0, 4).mapToObj(n -> this.text.getMessage(n, bl2)).map(Component::getString).toArray(String[]::new);
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onDone()).bounds(this.width / 2 - 100, this.height / 4 + 144, 200, 20).build());
        this.signField = new TextFieldHelper(() -> this.messages[this.line], this::setMessage, TextFieldHelper.createClipboardGetter(this.minecraft), TextFieldHelper.createClipboardSetter(this.minecraft), string -> this.minecraft.font.width((String)string) <= this.sign.getMaxTextLineWidth());
    }

    @Override
    public void tick() {
        ++this.frame;
        if (!this.isValid()) {
            this.onDone();
        }
    }

    private boolean isValid() {
        return this.minecraft != null && this.minecraft.player != null && !this.sign.isRemoved() && !this.sign.playerIsTooFarAwayToEdit(this.minecraft.player.getUUID());
    }

    @Override
    public boolean keyPressed(int n, int n2, int n3) {
        if (n == 265) {
            this.line = this.line - 1 & 3;
            this.signField.setCursorToEnd();
            return true;
        }
        if (n == 264 || n == 257 || n == 335) {
            this.line = this.line + 1 & 3;
            this.signField.setCursorToEnd();
            return true;
        }
        if (this.signField.keyPressed(n)) {
            return true;
        }
        return super.keyPressed(n, n2, n3);
    }

    @Override
    public boolean charTyped(char c, int n) {
        this.signField.charTyped(c);
        return true;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        super.render(guiGraphics, n, n2, f);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 40, -1);
        this.renderSign(guiGraphics);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int n, int n2, float f) {
        this.renderTransparentBackground(guiGraphics);
    }

    @Override
    public void onClose() {
        this.onDone();
    }

    @Override
    public void removed() {
        ClientPacketListener clientPacketListener = this.minecraft.getConnection();
        if (clientPacketListener != null) {
            clientPacketListener.send(new ServerboundSignUpdatePacket(this.sign.getBlockPos(), this.isFrontText, this.messages[0], this.messages[1], this.messages[2], this.messages[3]));
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    protected abstract void renderSignBackground(GuiGraphics var1);

    protected abstract Vector3f getSignTextScale();

    protected abstract float getSignYOffset();

    private void renderSign(GuiGraphics guiGraphics) {
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate((float)this.width / 2.0f, this.getSignYOffset());
        guiGraphics.pose().pushMatrix();
        this.renderSignBackground(guiGraphics);
        guiGraphics.pose().popMatrix();
        this.renderSignText(guiGraphics);
        guiGraphics.pose().popMatrix();
    }

    private void renderSignText(GuiGraphics guiGraphics) {
        int n;
        int n2;
        int n3;
        String string;
        int n4;
        Vector3f vector3f = this.getSignTextScale();
        guiGraphics.pose().scale(vector3f.x(), vector3f.y());
        int n5 = this.text.hasGlowingText() ? this.text.getColor().getTextColor() : AbstractSignRenderer.getDarkColor(this.text);
        boolean bl = this.frame / 6 % 2 == 0;
        int n6 = this.signField.getCursorPos();
        int n7 = this.signField.getSelectionPos();
        int n8 = 4 * this.sign.getTextLineHeight() / 2;
        int n9 = this.line * this.sign.getTextLineHeight() - n8;
        for (n4 = 0; n4 < this.messages.length; ++n4) {
            string = this.messages[n4];
            if (string == null) continue;
            if (this.font.isBidirectional()) {
                string = this.font.bidirectionalShaping(string);
            }
            n3 = -this.font.width(string) / 2;
            guiGraphics.drawString(this.font, string, n3, n4 * this.sign.getTextLineHeight() - n8, n5, false);
            if (n4 != this.line || n6 < 0 || !bl) continue;
            n2 = this.font.width(string.substring(0, Math.max(Math.min(n6, string.length()), 0)));
            n = n2 - this.font.width(string) / 2;
            if (n6 < string.length()) continue;
            guiGraphics.drawString(this.font, "_", n, n9, n5, false);
        }
        for (n4 = 0; n4 < this.messages.length; ++n4) {
            string = this.messages[n4];
            if (string == null || n4 != this.line || n6 < 0) continue;
            n3 = this.font.width(string.substring(0, Math.max(Math.min(n6, string.length()), 0)));
            n2 = n3 - this.font.width(string) / 2;
            if (bl && n6 < string.length()) {
                guiGraphics.fill(n2, n9 - 1, n2 + 1, n9 + this.sign.getTextLineHeight(), ARGB.opaque(n5));
            }
            if (n7 == n6) continue;
            n = Math.min(n6, n7);
            int n10 = Math.max(n6, n7);
            int n11 = this.font.width(string.substring(0, n)) - this.font.width(string) / 2;
            int n12 = this.font.width(string.substring(0, n10)) - this.font.width(string) / 2;
            int n13 = Math.min(n11, n12);
            int n14 = Math.max(n11, n12);
            guiGraphics.textHighlight(n13, n9, n14, n9 + this.sign.getTextLineHeight());
        }
    }

    private void setMessage(String string) {
        this.messages[this.line] = string;
        this.text = this.text.setMessage(this.line, Component.literal(string));
        this.sign.setText(this.text, this.isFrontText);
    }

    private void onDone() {
        this.minecraft.setScreen(null);
    }
}

