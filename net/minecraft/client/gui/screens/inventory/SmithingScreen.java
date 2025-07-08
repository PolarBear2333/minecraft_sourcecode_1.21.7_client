/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  org.joml.Quaternionf
 *  org.joml.Vector3f
 */
package net.minecraft.client.gui.screens.inventory;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CyclingSlotBackground;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.equipment.Equippable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class SmithingScreen
extends ItemCombinerScreen<SmithingMenu> {
    private static final ResourceLocation ERROR_SPRITE = ResourceLocation.withDefaultNamespace("container/smithing/error");
    private static final ResourceLocation EMPTY_SLOT_SMITHING_TEMPLATE_ARMOR_TRIM = ResourceLocation.withDefaultNamespace("container/slot/smithing_template_armor_trim");
    private static final ResourceLocation EMPTY_SLOT_SMITHING_TEMPLATE_NETHERITE_UPGRADE = ResourceLocation.withDefaultNamespace("container/slot/smithing_template_netherite_upgrade");
    private static final Component MISSING_TEMPLATE_TOOLTIP = Component.translatable("container.upgrade.missing_template_tooltip");
    private static final Component ERROR_TOOLTIP = Component.translatable("container.upgrade.error_tooltip");
    private static final List<ResourceLocation> EMPTY_SLOT_SMITHING_TEMPLATES = List.of(EMPTY_SLOT_SMITHING_TEMPLATE_ARMOR_TRIM, EMPTY_SLOT_SMITHING_TEMPLATE_NETHERITE_UPGRADE);
    private static final int TITLE_LABEL_X = 44;
    private static final int TITLE_LABEL_Y = 15;
    private static final int ERROR_ICON_WIDTH = 28;
    private static final int ERROR_ICON_HEIGHT = 21;
    private static final int ERROR_ICON_X = 65;
    private static final int ERROR_ICON_Y = 46;
    private static final int TOOLTIP_WIDTH = 115;
    private static final int ARMOR_STAND_Y_ROT = 210;
    private static final int ARMOR_STAND_X_ROT = 25;
    private static final Vector3f ARMOR_STAND_TRANSLATION = new Vector3f(0.0f, 1.0f, 0.0f);
    private static final Quaternionf ARMOR_STAND_ANGLE = new Quaternionf().rotationXYZ(0.43633232f, 0.0f, (float)Math.PI);
    private static final int ARMOR_STAND_SCALE = 25;
    private static final int ARMOR_STAND_LEFT = 121;
    private static final int ARMOR_STAND_TOP = 20;
    private static final int ARMOR_STAND_RIGHT = 161;
    private static final int ARMOR_STAND_BOTTOM = 80;
    private final CyclingSlotBackground templateIcon = new CyclingSlotBackground(0);
    private final CyclingSlotBackground baseIcon = new CyclingSlotBackground(1);
    private final CyclingSlotBackground additionalIcon = new CyclingSlotBackground(2);
    @Nullable
    private ArmorStand armorStandPreview;

    public SmithingScreen(SmithingMenu smithingMenu, Inventory inventory, Component component) {
        super(smithingMenu, inventory, component, ResourceLocation.withDefaultNamespace("textures/gui/container/smithing.png"));
        this.titleLabelX = 44;
        this.titleLabelY = 15;
    }

    @Override
    protected void subInit() {
        this.armorStandPreview = new ArmorStand(this.minecraft.level, 0.0, 0.0, 0.0);
        this.armorStandPreview.setNoBasePlate(true);
        this.armorStandPreview.setShowArms(true);
        this.armorStandPreview.yBodyRot = 210.0f;
        this.armorStandPreview.setXRot(25.0f);
        this.armorStandPreview.yHeadRot = this.armorStandPreview.getYRot();
        this.armorStandPreview.yHeadRotO = this.armorStandPreview.getYRot();
        this.updateArmorStandPreview(((SmithingMenu)this.menu).getSlot(3).getItem());
    }

    @Override
    public void containerTick() {
        super.containerTick();
        Optional<SmithingTemplateItem> optional = this.getTemplateItem();
        this.templateIcon.tick(EMPTY_SLOT_SMITHING_TEMPLATES);
        this.baseIcon.tick(optional.map(SmithingTemplateItem::getBaseSlotEmptyIcons).orElse(List.of()));
        this.additionalIcon.tick(optional.map(SmithingTemplateItem::getAdditionalSlotEmptyIcons).orElse(List.of()));
    }

    private Optional<SmithingTemplateItem> getTemplateItem() {
        Item item;
        ItemStack itemStack = ((SmithingMenu)this.menu).getSlot(0).getItem();
        if (!itemStack.isEmpty() && (item = itemStack.getItem()) instanceof SmithingTemplateItem) {
            SmithingTemplateItem smithingTemplateItem = (SmithingTemplateItem)item;
            return Optional.of(smithingTemplateItem);
        }
        return Optional.empty();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        super.render(guiGraphics, n, n2, f);
        this.renderOnboardingTooltips(guiGraphics, n, n2);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float f, int n, int n2) {
        super.renderBg(guiGraphics, f, n, n2);
        this.templateIcon.render(this.menu, guiGraphics, f, this.leftPos, this.topPos);
        this.baseIcon.render(this.menu, guiGraphics, f, this.leftPos, this.topPos);
        this.additionalIcon.render(this.menu, guiGraphics, f, this.leftPos, this.topPos);
        int n3 = this.leftPos + 121;
        int n4 = this.topPos + 20;
        int n5 = this.leftPos + 161;
        int n6 = this.topPos + 80;
        InventoryScreen.renderEntityInInventory(guiGraphics, n3, n4, n5, n6, 25.0f, ARMOR_STAND_TRANSLATION, ARMOR_STAND_ANGLE, null, this.armorStandPreview);
    }

    @Override
    public void slotChanged(AbstractContainerMenu abstractContainerMenu, int n, ItemStack itemStack) {
        if (n == 3) {
            this.updateArmorStandPreview(itemStack);
        }
    }

    private void updateArmorStandPreview(ItemStack itemStack) {
        if (this.armorStandPreview == null) {
            return;
        }
        for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
            this.armorStandPreview.setItemSlot(equipmentSlot, ItemStack.EMPTY);
        }
        if (!itemStack.isEmpty()) {
            EquipmentSlot equipmentSlot;
            Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
            equipmentSlot = equippable != null ? equippable.slot() : EquipmentSlot.OFFHAND;
            this.armorStandPreview.setItemSlot(equipmentSlot, itemStack.copy());
        }
    }

    @Override
    protected void renderErrorIcon(GuiGraphics guiGraphics, int n, int n2) {
        if (this.hasRecipeError()) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, ERROR_SPRITE, n + 65, n2 + 46, 28, 21);
        }
    }

    private void renderOnboardingTooltips(GuiGraphics guiGraphics, int n, int n2) {
        Optional<Component> optional = Optional.empty();
        if (this.hasRecipeError() && this.isHovering(65, 46, 28, 21, n, n2)) {
            optional = Optional.of(ERROR_TOOLTIP);
        }
        if (this.hoveredSlot != null) {
            ItemStack itemStack = ((SmithingMenu)this.menu).getSlot(0).getItem();
            ItemStack itemStack2 = this.hoveredSlot.getItem();
            if (itemStack.isEmpty()) {
                if (this.hoveredSlot.index == 0) {
                    optional = Optional.of(MISSING_TEMPLATE_TOOLTIP);
                }
            } else {
                Item item = itemStack.getItem();
                if (item instanceof SmithingTemplateItem) {
                    SmithingTemplateItem smithingTemplateItem = (SmithingTemplateItem)item;
                    if (itemStack2.isEmpty()) {
                        if (this.hoveredSlot.index == 1) {
                            optional = Optional.of(smithingTemplateItem.getBaseSlotDescription());
                        } else if (this.hoveredSlot.index == 2) {
                            optional = Optional.of(smithingTemplateItem.getAdditionSlotDescription());
                        }
                    }
                }
            }
        }
        optional.ifPresent(component -> guiGraphics.setTooltipForNextFrame(this.font, this.font.split((FormattedText)component, 115), n, n2));
    }

    private boolean hasRecipeError() {
        return ((SmithingMenu)this.menu).hasRecipeError();
    }
}

