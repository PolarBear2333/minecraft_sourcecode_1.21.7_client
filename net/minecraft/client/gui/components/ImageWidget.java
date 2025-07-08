/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.components;

import javax.annotation.Nullable;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;

public abstract class ImageWidget
extends AbstractWidget {
    ImageWidget(int n, int n2, int n3, int n4) {
        super(n, n2, n3, n4, CommonComponents.EMPTY);
    }

    public static ImageWidget texture(int n, int n2, ResourceLocation resourceLocation, int n3, int n4) {
        return new Texture(0, 0, n, n2, resourceLocation, n3, n4);
    }

    public static ImageWidget sprite(int n, int n2, ResourceLocation resourceLocation) {
        return new Sprite(0, 0, n, n2, resourceLocation);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
    }

    @Override
    public boolean isActive() {
        return false;
    }

    public abstract void updateResource(ResourceLocation var1);

    @Override
    @Nullable
    public ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
        return null;
    }

    static class Texture
    extends ImageWidget {
        private ResourceLocation texture;
        private final int textureWidth;
        private final int textureHeight;

        public Texture(int n, int n2, int n3, int n4, ResourceLocation resourceLocation, int n5, int n6) {
            super(n, n2, n3, n4);
            this.texture = resourceLocation;
            this.textureWidth = n5;
            this.textureHeight = n6;
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int n, int n2, float f) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, this.texture, this.getX(), this.getY(), 0.0f, 0.0f, this.getWidth(), this.getHeight(), this.textureWidth, this.textureHeight);
        }

        @Override
        public void updateResource(ResourceLocation resourceLocation) {
            this.texture = resourceLocation;
        }
    }

    static class Sprite
    extends ImageWidget {
        private ResourceLocation sprite;

        public Sprite(int n, int n2, int n3, int n4, ResourceLocation resourceLocation) {
            super(n, n2, n3, n4);
            this.sprite = resourceLocation;
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int n, int n2, float f) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.sprite, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        }

        @Override
        public void updateResource(ResourceLocation resourceLocation) {
            this.sprite = resourceLocation;
        }
    }
}

