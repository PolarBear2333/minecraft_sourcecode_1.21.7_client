/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

public class PageButton
extends Button {
    private static final ResourceLocation PAGE_FORWARD_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("widget/page_forward_highlighted");
    private static final ResourceLocation PAGE_FORWARD_SPRITE = ResourceLocation.withDefaultNamespace("widget/page_forward");
    private static final ResourceLocation PAGE_BACKWARD_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("widget/page_backward_highlighted");
    private static final ResourceLocation PAGE_BACKWARD_SPRITE = ResourceLocation.withDefaultNamespace("widget/page_backward");
    private static final Component PAGE_BUTTON_NEXT = Component.translatable("book.page_button.next");
    private static final Component PAGE_BUTTON_PREVIOUS = Component.translatable("book.page_button.previous");
    private final boolean isForward;
    private final boolean playTurnSound;

    public PageButton(int n, int n2, boolean bl, Button.OnPress onPress, boolean bl2) {
        super(n, n2, 23, 13, bl ? PAGE_BUTTON_NEXT : PAGE_BUTTON_PREVIOUS, onPress, DEFAULT_NARRATION);
        this.isForward = bl;
        this.playTurnSound = bl2;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int n, int n2, float f) {
        ResourceLocation resourceLocation = this.isForward ? (this.isHoveredOrFocused() ? PAGE_FORWARD_HIGHLIGHTED_SPRITE : PAGE_FORWARD_SPRITE) : (this.isHoveredOrFocused() ? PAGE_BACKWARD_HIGHLIGHTED_SPRITE : PAGE_BACKWARD_SPRITE);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocation, this.getX(), this.getY(), 23, 13);
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
        if (this.playTurnSound) {
            soundManager.play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0f));
        }
    }
}

