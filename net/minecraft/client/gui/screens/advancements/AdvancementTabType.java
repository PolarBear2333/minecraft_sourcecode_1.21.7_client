/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens.advancements;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

enum AdvancementTabType {
    ABOVE(new Sprites(ResourceLocation.withDefaultNamespace("advancements/tab_above_left_selected"), ResourceLocation.withDefaultNamespace("advancements/tab_above_middle_selected"), ResourceLocation.withDefaultNamespace("advancements/tab_above_right_selected")), new Sprites(ResourceLocation.withDefaultNamespace("advancements/tab_above_left"), ResourceLocation.withDefaultNamespace("advancements/tab_above_middle"), ResourceLocation.withDefaultNamespace("advancements/tab_above_right")), 28, 32, 8),
    BELOW(new Sprites(ResourceLocation.withDefaultNamespace("advancements/tab_below_left_selected"), ResourceLocation.withDefaultNamespace("advancements/tab_below_middle_selected"), ResourceLocation.withDefaultNamespace("advancements/tab_below_right_selected")), new Sprites(ResourceLocation.withDefaultNamespace("advancements/tab_below_left"), ResourceLocation.withDefaultNamespace("advancements/tab_below_middle"), ResourceLocation.withDefaultNamespace("advancements/tab_below_right")), 28, 32, 8),
    LEFT(new Sprites(ResourceLocation.withDefaultNamespace("advancements/tab_left_top_selected"), ResourceLocation.withDefaultNamespace("advancements/tab_left_middle_selected"), ResourceLocation.withDefaultNamespace("advancements/tab_left_bottom_selected")), new Sprites(ResourceLocation.withDefaultNamespace("advancements/tab_left_top"), ResourceLocation.withDefaultNamespace("advancements/tab_left_middle"), ResourceLocation.withDefaultNamespace("advancements/tab_left_bottom")), 32, 28, 5),
    RIGHT(new Sprites(ResourceLocation.withDefaultNamespace("advancements/tab_right_top_selected"), ResourceLocation.withDefaultNamespace("advancements/tab_right_middle_selected"), ResourceLocation.withDefaultNamespace("advancements/tab_right_bottom_selected")), new Sprites(ResourceLocation.withDefaultNamespace("advancements/tab_right_top"), ResourceLocation.withDefaultNamespace("advancements/tab_right_middle"), ResourceLocation.withDefaultNamespace("advancements/tab_right_bottom")), 32, 28, 5);

    private final Sprites selectedSprites;
    private final Sprites unselectedSprites;
    private final int width;
    private final int height;
    private final int max;

    private AdvancementTabType(Sprites sprites, Sprites sprites2, int n2, int n3, int n4) {
        this.selectedSprites = sprites;
        this.unselectedSprites = sprites2;
        this.width = n2;
        this.height = n3;
        this.max = n4;
    }

    public int getMax() {
        return this.max;
    }

    public void draw(GuiGraphics guiGraphics, int n, int n2, boolean bl, int n3) {
        Sprites sprites;
        Sprites sprites2 = sprites = bl ? this.selectedSprites : this.unselectedSprites;
        ResourceLocation resourceLocation = n3 == 0 ? sprites.first() : (n3 == this.max - 1 ? sprites.last() : sprites.middle());
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocation, n + this.getX(n3), n2 + this.getY(n3), this.width, this.height);
    }

    public void drawIcon(GuiGraphics guiGraphics, int n, int n2, int n3, ItemStack itemStack) {
        int n4 = n + this.getX(n3);
        int n5 = n2 + this.getY(n3);
        switch (this.ordinal()) {
            case 0: {
                n4 += 6;
                n5 += 9;
                break;
            }
            case 1: {
                n4 += 6;
                n5 += 6;
                break;
            }
            case 2: {
                n4 += 10;
                n5 += 5;
                break;
            }
            case 3: {
                n4 += 6;
                n5 += 5;
            }
        }
        guiGraphics.renderFakeItem(itemStack, n4, n5);
    }

    public int getX(int n) {
        switch (this.ordinal()) {
            case 0: {
                return (this.width + 4) * n;
            }
            case 1: {
                return (this.width + 4) * n;
            }
            case 2: {
                return -this.width + 4;
            }
            case 3: {
                return 248;
            }
        }
        throw new UnsupportedOperationException("Don't know what this tab type is!" + String.valueOf((Object)this));
    }

    public int getY(int n) {
        switch (this.ordinal()) {
            case 0: {
                return -this.height + 4;
            }
            case 1: {
                return 136;
            }
            case 2: {
                return this.height * n;
            }
            case 3: {
                return this.height * n;
            }
        }
        throw new UnsupportedOperationException("Don't know what this tab type is!" + String.valueOf((Object)this));
    }

    public boolean isMouseOver(int n, int n2, int n3, double d, double d2) {
        int n4 = n + this.getX(n3);
        int n5 = n2 + this.getY(n3);
        return d > (double)n4 && d < (double)(n4 + this.width) && d2 > (double)n5 && d2 < (double)(n5 + this.height);
    }

    record Sprites(ResourceLocation first, ResourceLocation middle, ResourceLocation last) {
    }
}

