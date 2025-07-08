/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.CrafterMenu;
import net.minecraft.world.inventory.CrafterSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CrafterScreen
extends AbstractContainerScreen<CrafterMenu> {
    private static final ResourceLocation DISABLED_SLOT_LOCATION_SPRITE = ResourceLocation.withDefaultNamespace("container/crafter/disabled_slot");
    private static final ResourceLocation POWERED_REDSTONE_LOCATION_SPRITE = ResourceLocation.withDefaultNamespace("container/crafter/powered_redstone");
    private static final ResourceLocation UNPOWERED_REDSTONE_LOCATION_SPRITE = ResourceLocation.withDefaultNamespace("container/crafter/unpowered_redstone");
    private static final ResourceLocation CONTAINER_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/container/crafter.png");
    private static final Component DISABLED_SLOT_TOOLTIP = Component.translatable("gui.togglable_slot");
    private final Player player;

    public CrafterScreen(CrafterMenu crafterMenu, Inventory inventory, Component component) {
        super(crafterMenu, inventory, component);
        this.player = inventory.player;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    protected void slotClicked(Slot slot, int n, int n2, ClickType clickType) {
        if (slot instanceof CrafterSlot && !slot.hasItem() && !this.player.isSpectator()) {
            switch (clickType) {
                case PICKUP: {
                    if (((CrafterMenu)this.menu).isSlotDisabled(n)) {
                        this.enableSlot(n);
                        break;
                    }
                    if (!((CrafterMenu)this.menu).getCarried().isEmpty()) break;
                    this.disableSlot(n);
                    break;
                }
                case SWAP: {
                    ItemStack itemStack = this.player.getInventory().getItem(n2);
                    if (!((CrafterMenu)this.menu).isSlotDisabled(n) || itemStack.isEmpty()) break;
                    this.enableSlot(n);
                }
            }
        }
        super.slotClicked(slot, n, n2, clickType);
    }

    private void enableSlot(int n) {
        this.updateSlotState(n, true);
    }

    private void disableSlot(int n) {
        this.updateSlotState(n, false);
    }

    private void updateSlotState(int n, boolean bl) {
        ((CrafterMenu)this.menu).setSlotState(n, bl);
        super.handleSlotStateChanged(n, ((CrafterMenu)this.menu).containerId, bl);
        float f = bl ? 1.0f : 0.75f;
        this.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.4f, f);
    }

    /*
     * Enabled aggressive block sorting
     */
    @Override
    public void renderSlot(GuiGraphics guiGraphics, Slot slot) {
        if (slot instanceof CrafterSlot) {
            CrafterSlot crafterSlot = (CrafterSlot)slot;
            if (((CrafterMenu)this.menu).isSlotDisabled(slot.index)) {
                this.renderDisabledSlot(guiGraphics, crafterSlot);
                return;
            }
        }
        super.renderSlot(guiGraphics, slot);
    }

    private void renderDisabledSlot(GuiGraphics guiGraphics, CrafterSlot crafterSlot) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, DISABLED_SLOT_LOCATION_SPRITE, crafterSlot.x - 1, crafterSlot.y - 1, 18, 18);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        super.render(guiGraphics, n, n2, f);
        this.renderRedstone(guiGraphics);
        this.renderTooltip(guiGraphics, n, n2);
        if (this.hoveredSlot instanceof CrafterSlot && !((CrafterMenu)this.menu).isSlotDisabled(this.hoveredSlot.index) && ((CrafterMenu)this.menu).getCarried().isEmpty() && !this.hoveredSlot.hasItem() && !this.player.isSpectator()) {
            guiGraphics.setTooltipForNextFrame(this.font, DISABLED_SLOT_TOOLTIP, n, n2);
        }
    }

    private void renderRedstone(GuiGraphics guiGraphics) {
        int n = this.width / 2 + 9;
        int n2 = this.height / 2 - 48;
        ResourceLocation resourceLocation = ((CrafterMenu)this.menu).isPowered() ? POWERED_REDSTONE_LOCATION_SPRITE : UNPOWERED_REDSTONE_LOCATION_SPRITE;
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocation, n, n2, 16, 16);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float f, int n, int n2) {
        int n3 = (this.width - this.imageWidth) / 2;
        int n4 = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_LOCATION, n3, n4, 0.0f, 0.0f, this.imageWidth, this.imageHeight, 256, 256);
    }
}

