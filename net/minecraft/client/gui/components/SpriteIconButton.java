/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.components;

import javax.annotation.Nullable;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public abstract class SpriteIconButton
extends Button {
    protected final ResourceLocation sprite;
    protected final int spriteWidth;
    protected final int spriteHeight;

    SpriteIconButton(int n, int n2, Component component, int n3, int n4, ResourceLocation resourceLocation, Button.OnPress onPress, @Nullable Button.CreateNarration createNarration) {
        super(0, 0, n, n2, component, onPress, createNarration == null ? DEFAULT_NARRATION : createNarration);
        this.spriteWidth = n3;
        this.spriteHeight = n4;
        this.sprite = resourceLocation;
    }

    public static Builder builder(Component component, Button.OnPress onPress, boolean bl) {
        return new Builder(component, onPress, bl);
    }

    public static class Builder {
        private final Component message;
        private final Button.OnPress onPress;
        private final boolean iconOnly;
        private int width = 150;
        private int height = 20;
        @Nullable
        private ResourceLocation sprite;
        private int spriteWidth;
        private int spriteHeight;
        @Nullable
        Button.CreateNarration narration;

        public Builder(Component component, Button.OnPress onPress, boolean bl) {
            this.message = component;
            this.onPress = onPress;
            this.iconOnly = bl;
        }

        public Builder width(int n) {
            this.width = n;
            return this;
        }

        public Builder size(int n, int n2) {
            this.width = n;
            this.height = n2;
            return this;
        }

        public Builder sprite(ResourceLocation resourceLocation, int n, int n2) {
            this.sprite = resourceLocation;
            this.spriteWidth = n;
            this.spriteHeight = n2;
            return this;
        }

        public Builder narration(Button.CreateNarration createNarration) {
            this.narration = createNarration;
            return this;
        }

        public SpriteIconButton build() {
            if (this.sprite == null) {
                throw new IllegalStateException("Sprite not set");
            }
            if (this.iconOnly) {
                return new CenteredIcon(this.width, this.height, this.message, this.spriteWidth, this.spriteHeight, this.sprite, this.onPress, this.narration);
            }
            return new TextAndIcon(this.width, this.height, this.message, this.spriteWidth, this.spriteHeight, this.sprite, this.onPress, this.narration);
        }
    }

    public static class TextAndIcon
    extends SpriteIconButton {
        protected TextAndIcon(int n, int n2, Component component, int n3, int n4, ResourceLocation resourceLocation, Button.OnPress onPress, @Nullable Button.CreateNarration createNarration) {
            super(n, n2, component, n3, n4, resourceLocation, onPress, createNarration);
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int n, int n2, float f) {
            super.renderWidget(guiGraphics, n, n2, f);
            int n3 = this.getX() + this.getWidth() - this.spriteWidth - 2;
            int n4 = this.getY() + this.getHeight() / 2 - this.spriteHeight / 2;
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.sprite, n3, n4, this.spriteWidth, this.spriteHeight, this.alpha);
        }

        @Override
        public void renderString(GuiGraphics guiGraphics, Font font, int n) {
            int n2 = this.getX() + 2;
            int n3 = this.getX() + this.getWidth() - this.spriteWidth - 4;
            int n4 = this.getX() + this.getWidth() / 2;
            TextAndIcon.renderScrollingString(guiGraphics, font, this.getMessage(), n4, n2, this.getY(), n3, this.getY() + this.getHeight(), n);
        }
    }

    public static class CenteredIcon
    extends SpriteIconButton {
        protected CenteredIcon(int n, int n2, Component component, int n3, int n4, ResourceLocation resourceLocation, Button.OnPress onPress, @Nullable Button.CreateNarration createNarration) {
            super(n, n2, component, n3, n4, resourceLocation, onPress, createNarration);
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int n, int n2, float f) {
            super.renderWidget(guiGraphics, n, n2, f);
            int n3 = this.getX() + this.getWidth() / 2 - this.spriteWidth / 2;
            int n4 = this.getY() + this.getHeight() / 2 - this.spriteHeight / 2;
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.sprite, n3, n4, this.spriteWidth, this.spriteHeight, this.alpha);
        }

        @Override
        public void renderString(GuiGraphics guiGraphics, Font font, int n) {
        }
    }
}

