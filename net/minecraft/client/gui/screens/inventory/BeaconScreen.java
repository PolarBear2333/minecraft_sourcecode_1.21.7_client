/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ServerboundSetBeaconPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;

public class BeaconScreen
extends AbstractContainerScreen<BeaconMenu> {
    private static final ResourceLocation BEACON_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/container/beacon.png");
    static final ResourceLocation BUTTON_DISABLED_SPRITE = ResourceLocation.withDefaultNamespace("container/beacon/button_disabled");
    static final ResourceLocation BUTTON_SELECTED_SPRITE = ResourceLocation.withDefaultNamespace("container/beacon/button_selected");
    static final ResourceLocation BUTTON_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("container/beacon/button_highlighted");
    static final ResourceLocation BUTTON_SPRITE = ResourceLocation.withDefaultNamespace("container/beacon/button");
    static final ResourceLocation CONFIRM_SPRITE = ResourceLocation.withDefaultNamespace("container/beacon/confirm");
    static final ResourceLocation CANCEL_SPRITE = ResourceLocation.withDefaultNamespace("container/beacon/cancel");
    private static final Component PRIMARY_EFFECT_LABEL = Component.translatable("block.minecraft.beacon.primary");
    private static final Component SECONDARY_EFFECT_LABEL = Component.translatable("block.minecraft.beacon.secondary");
    private final List<BeaconButton> beaconButtons = Lists.newArrayList();
    @Nullable
    Holder<MobEffect> primary;
    @Nullable
    Holder<MobEffect> secondary;

    public BeaconScreen(final BeaconMenu beaconMenu, Inventory inventory, Component component) {
        super(beaconMenu, inventory, component);
        this.imageWidth = 230;
        this.imageHeight = 219;
        beaconMenu.addSlotListener(new ContainerListener(){

            @Override
            public void slotChanged(AbstractContainerMenu abstractContainerMenu, int n, ItemStack itemStack) {
            }

            @Override
            public void dataChanged(AbstractContainerMenu abstractContainerMenu, int n, int n2) {
                BeaconScreen.this.primary = beaconMenu.getPrimaryEffect();
                BeaconScreen.this.secondary = beaconMenu.getSecondaryEffect();
            }
        });
    }

    private <T extends AbstractWidget> void addBeaconButton(T t) {
        this.addRenderableWidget(t);
        this.beaconButtons.add((BeaconButton)((Object)t));
    }

    @Override
    protected void init() {
        BeaconPowerButton beaconPowerButton;
        Object object;
        int n;
        int n2;
        int n3;
        int n4;
        super.init();
        this.beaconButtons.clear();
        this.addBeaconButton(new BeaconConfirmButton(this.leftPos + 164, this.topPos + 107));
        this.addBeaconButton(new BeaconCancelButton(this.leftPos + 190, this.topPos + 107));
        for (n4 = 0; n4 <= 2; ++n4) {
            n3 = BeaconBlockEntity.BEACON_EFFECTS.get(n4).size();
            n2 = n3 * 22 + (n3 - 1) * 2;
            for (n = 0; n < n3; ++n) {
                object = BeaconBlockEntity.BEACON_EFFECTS.get(n4).get(n);
                beaconPowerButton = new BeaconPowerButton(this.leftPos + 76 + n * 24 - n2 / 2, this.topPos + 22 + n4 * 25, (Holder<MobEffect>)object, true, n4);
                beaconPowerButton.active = false;
                this.addBeaconButton(beaconPowerButton);
            }
        }
        n4 = 3;
        n3 = BeaconBlockEntity.BEACON_EFFECTS.get(3).size() + 1;
        n2 = n3 * 22 + (n3 - 1) * 2;
        for (n = 0; n < n3 - 1; ++n) {
            object = BeaconBlockEntity.BEACON_EFFECTS.get(3).get(n);
            beaconPowerButton = new BeaconPowerButton(this.leftPos + 167 + n * 24 - n2 / 2, this.topPos + 47, (Holder<MobEffect>)object, false, 3);
            beaconPowerButton.active = false;
            this.addBeaconButton(beaconPowerButton);
        }
        Holder<MobEffect> holder = BeaconBlockEntity.BEACON_EFFECTS.get(0).get(0);
        object = new BeaconUpgradePowerButton(this.leftPos + 167 + (n3 - 1) * 24 - n2 / 2, this.topPos + 47, holder);
        ((BeaconPowerButton)object).visible = false;
        this.addBeaconButton(object);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        this.updateButtons();
    }

    void updateButtons() {
        int n = ((BeaconMenu)this.menu).getLevels();
        this.beaconButtons.forEach(beaconButton -> beaconButton.updateStatus(n));
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int n, int n2) {
        guiGraphics.drawCenteredString(this.font, PRIMARY_EFFECT_LABEL, 62, 10, -2039584);
        guiGraphics.drawCenteredString(this.font, SECONDARY_EFFECT_LABEL, 169, 10, -2039584);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float f, int n, int n2) {
        int n3 = (this.width - this.imageWidth) / 2;
        int n4 = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BEACON_LOCATION, n3, n4, 0.0f, 0.0f, this.imageWidth, this.imageHeight, 256, 256);
        guiGraphics.renderItem(new ItemStack(Items.NETHERITE_INGOT), n3 + 20, n4 + 109);
        guiGraphics.renderItem(new ItemStack(Items.EMERALD), n3 + 41, n4 + 109);
        guiGraphics.renderItem(new ItemStack(Items.DIAMOND), n3 + 41 + 22, n4 + 109);
        guiGraphics.renderItem(new ItemStack(Items.GOLD_INGOT), n3 + 42 + 44, n4 + 109);
        guiGraphics.renderItem(new ItemStack(Items.IRON_INGOT), n3 + 42 + 66, n4 + 109);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        super.render(guiGraphics, n, n2, f);
        this.renderTooltip(guiGraphics, n, n2);
    }

    static interface BeaconButton {
        public void updateStatus(int var1);
    }

    class BeaconConfirmButton
    extends BeaconSpriteScreenButton {
        public BeaconConfirmButton(int n, int n2) {
            super(n, n2, CONFIRM_SPRITE, CommonComponents.GUI_DONE);
        }

        @Override
        public void onPress() {
            BeaconScreen.this.minecraft.getConnection().send(new ServerboundSetBeaconPacket(Optional.ofNullable(BeaconScreen.this.primary), Optional.ofNullable(BeaconScreen.this.secondary)));
            ((BeaconScreen)BeaconScreen.this).minecraft.player.closeContainer();
        }

        @Override
        public void updateStatus(int n) {
            this.active = ((BeaconMenu)BeaconScreen.this.menu).hasPayment() && BeaconScreen.this.primary != null;
        }
    }

    class BeaconCancelButton
    extends BeaconSpriteScreenButton {
        public BeaconCancelButton(int n, int n2) {
            super(n, n2, CANCEL_SPRITE, CommonComponents.GUI_CANCEL);
        }

        @Override
        public void onPress() {
            ((BeaconScreen)BeaconScreen.this).minecraft.player.closeContainer();
        }

        @Override
        public void updateStatus(int n) {
        }
    }

    class BeaconPowerButton
    extends BeaconScreenButton {
        private final boolean isPrimary;
        protected final int tier;
        private Holder<MobEffect> effect;
        private ResourceLocation sprite;

        public BeaconPowerButton(int n, int n2, Holder<MobEffect> holder, boolean bl, int n3) {
            super(n, n2);
            this.isPrimary = bl;
            this.tier = n3;
            this.setEffect(holder);
        }

        protected void setEffect(Holder<MobEffect> holder) {
            this.effect = holder;
            this.sprite = Gui.getMobEffectSprite(holder);
            this.setTooltip(Tooltip.create(this.createEffectDescription(holder), null));
        }

        protected MutableComponent createEffectDescription(Holder<MobEffect> holder) {
            return Component.translatable(holder.value().getDescriptionId());
        }

        @Override
        public void onPress() {
            if (this.isSelected()) {
                return;
            }
            if (this.isPrimary) {
                BeaconScreen.this.primary = this.effect;
            } else {
                BeaconScreen.this.secondary = this.effect;
            }
            BeaconScreen.this.updateButtons();
        }

        @Override
        protected void renderIcon(GuiGraphics guiGraphics) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.sprite, this.getX() + 2, this.getY() + 2, 18, 18);
        }

        @Override
        public void updateStatus(int n) {
            this.active = this.tier < n;
            this.setSelected(this.effect.equals(this.isPrimary ? BeaconScreen.this.primary : BeaconScreen.this.secondary));
        }

        @Override
        protected MutableComponent createNarrationMessage() {
            return this.createEffectDescription(this.effect);
        }
    }

    class BeaconUpgradePowerButton
    extends BeaconPowerButton {
        public BeaconUpgradePowerButton(int n, int n2, Holder<MobEffect> holder) {
            super(n, n2, holder, false, 3);
        }

        @Override
        protected MutableComponent createEffectDescription(Holder<MobEffect> holder) {
            return Component.translatable(holder.value().getDescriptionId()).append(" II");
        }

        @Override
        public void updateStatus(int n) {
            if (BeaconScreen.this.primary != null) {
                this.visible = true;
                this.setEffect(BeaconScreen.this.primary);
                super.updateStatus(n);
            } else {
                this.visible = false;
            }
        }
    }

    static abstract class BeaconSpriteScreenButton
    extends BeaconScreenButton {
        private final ResourceLocation sprite;

        protected BeaconSpriteScreenButton(int n, int n2, ResourceLocation resourceLocation, Component component) {
            super(n, n2, component);
            this.sprite = resourceLocation;
        }

        @Override
        protected void renderIcon(GuiGraphics guiGraphics) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.sprite, this.getX() + 2, this.getY() + 2, 18, 18);
        }
    }

    static abstract class BeaconScreenButton
    extends AbstractButton
    implements BeaconButton {
        private boolean selected;

        protected BeaconScreenButton(int n, int n2) {
            super(n, n2, 22, 22, CommonComponents.EMPTY);
        }

        protected BeaconScreenButton(int n, int n2, Component component) {
            super(n, n2, 22, 22, component);
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int n, int n2, float f) {
            ResourceLocation resourceLocation = !this.active ? BUTTON_DISABLED_SPRITE : (this.selected ? BUTTON_SELECTED_SPRITE : (this.isHoveredOrFocused() ? BUTTON_HIGHLIGHTED_SPRITE : BUTTON_SPRITE));
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocation, this.getX(), this.getY(), this.width, this.height);
            this.renderIcon(guiGraphics);
        }

        protected abstract void renderIcon(GuiGraphics var1);

        public boolean isSelected() {
            return this.selected;
        }

        public void setSelected(boolean bl) {
            this.selected = bl;
        }

        @Override
        public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
            this.defaultButtonNarrationText(narrationElementOutput);
        }
    }
}

