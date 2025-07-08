/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.HorseInventoryMenu;

public class HorseInventoryScreen
extends AbstractContainerScreen<HorseInventoryMenu> {
    private static final ResourceLocation SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot");
    private static final ResourceLocation CHEST_SLOTS_SPRITE = ResourceLocation.withDefaultNamespace("container/horse/chest_slots");
    private static final ResourceLocation HORSE_INVENTORY_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/container/horse.png");
    private final AbstractHorse horse;
    private final int inventoryColumns;
    private float xMouse;
    private float yMouse;

    public HorseInventoryScreen(HorseInventoryMenu horseInventoryMenu, Inventory inventory, AbstractHorse abstractHorse, int n) {
        super(horseInventoryMenu, inventory, abstractHorse.getDisplayName());
        this.horse = abstractHorse;
        this.inventoryColumns = n;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float f, int n, int n2) {
        int n3 = (this.width - this.imageWidth) / 2;
        int n4 = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, HORSE_INVENTORY_LOCATION, n3, n4, 0.0f, 0.0f, this.imageWidth, this.imageHeight, 256, 256);
        if (this.inventoryColumns > 0) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, CHEST_SLOTS_SPRITE, 90, 54, 0, 0, n3 + 79, n4 + 17, this.inventoryColumns * 18, 54);
        }
        if (this.horse.canUseSlot(EquipmentSlot.SADDLE) && this.horse.getType().is(EntityTypeTags.CAN_EQUIP_SADDLE)) {
            this.drawSlot(guiGraphics, n3 + 7, n4 + 35 - 18);
        }
        boolean bl = this.horse instanceof Llama;
        if (this.horse.canUseSlot(EquipmentSlot.BODY) && (this.horse.getType().is(EntityTypeTags.CAN_WEAR_HORSE_ARMOR) || bl)) {
            this.drawSlot(guiGraphics, n3 + 7, n4 + 35);
        }
        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, n3 + 26, n4 + 18, n3 + 78, n4 + 70, 17, 0.25f, this.xMouse, this.yMouse, this.horse);
    }

    private void drawSlot(GuiGraphics guiGraphics, int n, int n2) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_SPRITE, n, n2, 18, 18);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        this.xMouse = n;
        this.yMouse = n2;
        super.render(guiGraphics, n, n2, f);
        this.renderTooltip(guiGraphics, n, n2);
    }
}

