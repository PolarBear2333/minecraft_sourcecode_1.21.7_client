/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens.inventory;

import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

public class CyclingSlotBackground {
    private static final int ICON_CHANGE_TICK_RATE = 30;
    private static final int ICON_SIZE = 16;
    private static final int ICON_TRANSITION_TICK_DURATION = 4;
    private final int slotIndex;
    private List<ResourceLocation> icons = List.of();
    private int tick;
    private int iconIndex;

    public CyclingSlotBackground(int n) {
        this.slotIndex = n;
    }

    public void tick(List<ResourceLocation> list) {
        if (!this.icons.equals(list)) {
            this.icons = list;
            this.iconIndex = 0;
        }
        if (!this.icons.isEmpty() && ++this.tick % 30 == 0) {
            this.iconIndex = (this.iconIndex + 1) % this.icons.size();
        }
    }

    public void render(AbstractContainerMenu abstractContainerMenu, GuiGraphics guiGraphics, float f, int n, int n2) {
        float f2;
        Slot slot = abstractContainerMenu.getSlot(this.slotIndex);
        if (this.icons.isEmpty() || slot.hasItem()) {
            return;
        }
        boolean bl = this.icons.size() > 1 && this.tick >= 30;
        float f3 = f2 = bl ? this.getIconTransitionTransparency(f) : 1.0f;
        if (f2 < 1.0f) {
            int n3 = Math.floorMod(this.iconIndex - 1, this.icons.size());
            this.renderIcon(slot, this.icons.get(n3), 1.0f - f2, guiGraphics, n, n2);
        }
        this.renderIcon(slot, this.icons.get(this.iconIndex), f2, guiGraphics, n, n2);
    }

    private void renderIcon(Slot slot, ResourceLocation resourceLocation, float f, GuiGraphics guiGraphics, int n, int n2) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocation, n + slot.x, n2 + slot.y, 16, 16, ARGB.white(f));
    }

    private float getIconTransitionTransparency(float f) {
        float f2 = (float)(this.tick % 30) + f;
        return Math.min(f2, 4.0f) / 4.0f;
    }
}

