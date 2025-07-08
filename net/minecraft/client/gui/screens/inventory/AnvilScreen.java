/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class AnvilScreen
extends ItemCombinerScreen<AnvilMenu> {
    private static final ResourceLocation TEXT_FIELD_SPRITE = ResourceLocation.withDefaultNamespace("container/anvil/text_field");
    private static final ResourceLocation TEXT_FIELD_DISABLED_SPRITE = ResourceLocation.withDefaultNamespace("container/anvil/text_field_disabled");
    private static final ResourceLocation ERROR_SPRITE = ResourceLocation.withDefaultNamespace("container/anvil/error");
    private static final ResourceLocation ANVIL_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/container/anvil.png");
    private static final Component TOO_EXPENSIVE_TEXT = Component.translatable("container.repair.expensive");
    private EditBox name;
    private final Player player;

    public AnvilScreen(AnvilMenu anvilMenu, Inventory inventory, Component component) {
        super(anvilMenu, inventory, component, ANVIL_LOCATION);
        this.player = inventory.player;
        this.titleLabelX = 60;
    }

    @Override
    protected void subInit() {
        int n = (this.width - this.imageWidth) / 2;
        int n2 = (this.height - this.imageHeight) / 2;
        this.name = new EditBox(this.font, n + 62, n2 + 24, 103, 12, Component.translatable("container.repair"));
        this.name.setCanLoseFocus(false);
        this.name.setTextColor(-1);
        this.name.setTextColorUneditable(-1);
        this.name.setBordered(false);
        this.name.setMaxLength(50);
        this.name.setResponder(this::onNameChanged);
        this.name.setValue("");
        this.addRenderableWidget(this.name);
        this.name.setEditable(((AnvilMenu)this.menu).getSlot(0).hasItem());
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.minecraft.player.experienceDisplayStartTick = this.minecraft.player.tickCount;
    }

    @Override
    protected void setInitialFocus() {
        this.setInitialFocus(this.name);
    }

    @Override
    public void resize(Minecraft minecraft, int n, int n2) {
        String string = this.name.getValue();
        this.init(minecraft, n, n2);
        this.name.setValue(string);
    }

    @Override
    public boolean keyPressed(int n, int n2, int n3) {
        if (n == 256) {
            this.minecraft.player.closeContainer();
        }
        if (this.name.keyPressed(n, n2, n3) || this.name.canConsumeInput()) {
            return true;
        }
        return super.keyPressed(n, n2, n3);
    }

    private void onNameChanged(String string) {
        Slot slot = ((AnvilMenu)this.menu).getSlot(0);
        if (!slot.hasItem()) {
            return;
        }
        String string2 = string;
        if (!slot.getItem().has(DataComponents.CUSTOM_NAME) && string2.equals(slot.getItem().getHoverName().getString())) {
            string2 = "";
        }
        if (((AnvilMenu)this.menu).setItemName(string2)) {
            this.minecraft.player.connection.send(new ServerboundRenameItemPacket(string2));
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int n, int n2) {
        super.renderLabels(guiGraphics, n, n2);
        int n3 = ((AnvilMenu)this.menu).getCost();
        if (n3 > 0) {
            Component component;
            int n4 = -8323296;
            if (n3 >= 40 && !this.minecraft.player.hasInfiniteMaterials()) {
                component = TOO_EXPENSIVE_TEXT;
                n4 = -40864;
            } else if (!((AnvilMenu)this.menu).getSlot(2).hasItem()) {
                component = null;
            } else {
                component = Component.translatable("container.repair.cost", n3);
                if (!((AnvilMenu)this.menu).getSlot(2).mayPickup(this.player)) {
                    n4 = -40864;
                }
            }
            if (component != null) {
                int n5 = this.imageWidth - 8 - this.font.width(component) - 2;
                int n6 = 69;
                guiGraphics.fill(n5 - 2, 67, this.imageWidth - 8, 79, 0x4F000000);
                guiGraphics.drawString(this.font, component, n5, 69, n4);
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float f, int n, int n2) {
        super.renderBg(guiGraphics, f, n, n2);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, ((AnvilMenu)this.menu).getSlot(0).hasItem() ? TEXT_FIELD_SPRITE : TEXT_FIELD_DISABLED_SPRITE, this.leftPos + 59, this.topPos + 20, 110, 16);
    }

    @Override
    protected void renderErrorIcon(GuiGraphics guiGraphics, int n, int n2) {
        if ((((AnvilMenu)this.menu).getSlot(0).hasItem() || ((AnvilMenu)this.menu).getSlot(1).hasItem()) && !((AnvilMenu)this.menu).getSlot(((AnvilMenu)this.menu).getResultSlot()).hasItem()) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, ERROR_SPRITE, n + 99, n2 + 45, 28, 21);
        }
    }

    @Override
    public void slotChanged(AbstractContainerMenu abstractContainerMenu, int n, ItemStack itemStack) {
        if (n == 0) {
            this.name.setValue(itemStack.isEmpty() ? "" : itemStack.getHoverName().getString());
            this.name.setEditable(!itemStack.isEmpty());
            this.setFocused(this.name);
        }
    }
}

