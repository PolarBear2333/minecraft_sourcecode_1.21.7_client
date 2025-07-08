/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class LockIconButton
extends Button {
    private boolean locked;

    public LockIconButton(int n, int n2, Button.OnPress onPress) {
        super(n, n2, 20, 20, Component.translatable("narrator.button.difficulty_lock"), onPress, DEFAULT_NARRATION);
    }

    @Override
    protected MutableComponent createNarrationMessage() {
        return CommonComponents.joinForNarration(super.createNarrationMessage(), this.isLocked() ? Component.translatable("narrator.button.difficulty_lock.locked") : Component.translatable("narrator.button.difficulty_lock.unlocked"));
    }

    public boolean isLocked() {
        return this.locked;
    }

    public void setLocked(boolean bl) {
        this.locked = bl;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int n, int n2, float f) {
        Icon icon = !this.active ? (this.locked ? Icon.LOCKED_DISABLED : Icon.UNLOCKED_DISABLED) : (this.isHoveredOrFocused() ? (this.locked ? Icon.LOCKED_HOVER : Icon.UNLOCKED_HOVER) : (this.locked ? Icon.LOCKED : Icon.UNLOCKED));
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, icon.sprite, this.getX(), this.getY(), this.width, this.height);
    }

    static enum Icon {
        LOCKED(ResourceLocation.withDefaultNamespace("widget/locked_button")),
        LOCKED_HOVER(ResourceLocation.withDefaultNamespace("widget/locked_button_highlighted")),
        LOCKED_DISABLED(ResourceLocation.withDefaultNamespace("widget/locked_button_disabled")),
        UNLOCKED(ResourceLocation.withDefaultNamespace("widget/unlocked_button")),
        UNLOCKED_HOVER(ResourceLocation.withDefaultNamespace("widget/unlocked_button_highlighted")),
        UNLOCKED_DISABLED(ResourceLocation.withDefaultNamespace("widget/unlocked_button_disabled"));

        final ResourceLocation sprite;

        private Icon(ResourceLocation resourceLocation) {
            this.sprite = resourceLocation;
        }
    }
}

