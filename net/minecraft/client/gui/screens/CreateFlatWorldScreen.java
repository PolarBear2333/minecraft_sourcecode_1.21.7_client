/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens;

import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.PresetFlatWorldScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

public class CreateFlatWorldScreen
extends Screen {
    private static final Component TITLE = Component.translatable("createWorld.customize.flat.title");
    static final ResourceLocation SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot");
    private static final int SLOT_BG_SIZE = 18;
    private static final int SLOT_STAT_HEIGHT = 20;
    private static final int SLOT_BG_X = 1;
    private static final int SLOT_BG_Y = 1;
    private static final int SLOT_FG_X = 2;
    private static final int SLOT_FG_Y = 2;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 33, 64);
    protected final CreateWorldScreen parent;
    private final Consumer<FlatLevelGeneratorSettings> applySettings;
    FlatLevelGeneratorSettings generator;
    @Nullable
    private DetailsList list;
    @Nullable
    private Button deleteLayerButton;

    public CreateFlatWorldScreen(CreateWorldScreen createWorldScreen, Consumer<FlatLevelGeneratorSettings> consumer, FlatLevelGeneratorSettings flatLevelGeneratorSettings) {
        super(TITLE);
        this.parent = createWorldScreen;
        this.applySettings = consumer;
        this.generator = flatLevelGeneratorSettings;
    }

    public FlatLevelGeneratorSettings settings() {
        return this.generator;
    }

    public void setConfig(FlatLevelGeneratorSettings flatLevelGeneratorSettings) {
        this.generator = flatLevelGeneratorSettings;
        if (this.list != null) {
            this.list.resetRows();
            this.updateButtonValidity();
        }
    }

    @Override
    protected void init() {
        this.layout.addTitleHeader(this.title, this.font);
        this.list = this.layout.addToContents(new DetailsList());
        LinearLayout linearLayout = this.layout.addToFooter(LinearLayout.vertical().spacing(4));
        linearLayout.defaultCellSetting().alignVerticallyMiddle();
        LinearLayout linearLayout2 = linearLayout.addChild(LinearLayout.horizontal().spacing(8));
        LinearLayout linearLayout3 = linearLayout.addChild(LinearLayout.horizontal().spacing(8));
        this.deleteLayerButton = linearLayout2.addChild(Button.builder(Component.translatable("createWorld.customize.flat.removeLayer"), button -> {
            if (!this.hasValidSelection()) {
                return;
            }
            List<FlatLayerInfo> list = this.generator.getLayersInfo();
            int n = this.list.children().indexOf(this.list.getSelected());
            int n2 = list.size() - n - 1;
            list.remove(n2);
            this.list.setSelected(list.isEmpty() ? null : (DetailsList.Entry)this.list.children().get(Math.min(n, list.size() - 1)));
            this.generator.updateLayers();
            this.list.resetRows();
            this.updateButtonValidity();
        }).build());
        linearLayout2.addChild(Button.builder(Component.translatable("createWorld.customize.presets"), button -> {
            this.minecraft.setScreen(new PresetFlatWorldScreen(this));
            this.generator.updateLayers();
            this.updateButtonValidity();
        }).build());
        linearLayout3.addChild(Button.builder(CommonComponents.GUI_DONE, button -> {
            this.applySettings.accept(this.generator);
            this.onClose();
            this.generator.updateLayers();
        }).build());
        linearLayout3.addChild(Button.builder(CommonComponents.GUI_CANCEL, button -> {
            this.onClose();
            this.generator.updateLayers();
        }).build());
        this.generator.updateLayers();
        this.updateButtonValidity();
        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        if (this.list != null) {
            this.list.updateSize(this.width, this.layout);
        }
        this.layout.arrangeElements();
    }

    void updateButtonValidity() {
        if (this.deleteLayerButton != null) {
            this.deleteLayerButton.active = this.hasValidSelection();
        }
    }

    private boolean hasValidSelection() {
        return this.list != null && this.list.getSelected() != null;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    class DetailsList
    extends ObjectSelectionList<Entry> {
        private static final Component LAYER_MATERIAL_TITLE = Component.translatable("createWorld.customize.flat.tile").withStyle(ChatFormatting.UNDERLINE);
        private static final Component HEIGHT_TITLE = Component.translatable("createWorld.customize.flat.height").withStyle(ChatFormatting.UNDERLINE);

        public DetailsList() {
            super(CreateFlatWorldScreen.this.minecraft, CreateFlatWorldScreen.this.width, CreateFlatWorldScreen.this.height - 103, 43, 24, (int)((double)CreateFlatWorldScreen.this.font.lineHeight * 1.5));
            for (int i = 0; i < CreateFlatWorldScreen.this.generator.getLayersInfo().size(); ++i) {
                this.addEntry(new Entry());
            }
        }

        @Override
        public void setSelected(@Nullable Entry entry) {
            super.setSelected(entry);
            CreateFlatWorldScreen.this.updateButtonValidity();
        }

        public void resetRows() {
            int n = this.children().indexOf(this.getSelected());
            this.clearEntries();
            for (int i = 0; i < CreateFlatWorldScreen.this.generator.getLayersInfo().size(); ++i) {
                this.addEntry(new Entry());
            }
            List list = this.children();
            if (n >= 0 && n < list.size()) {
                this.setSelected((Entry)list.get(n));
            }
        }

        @Override
        protected void renderHeader(GuiGraphics guiGraphics, int n, int n2) {
            guiGraphics.drawString(CreateFlatWorldScreen.this.font, LAYER_MATERIAL_TITLE, n, n2, -1);
            guiGraphics.drawString(CreateFlatWorldScreen.this.font, HEIGHT_TITLE, n + this.getRowWidth() - CreateFlatWorldScreen.this.font.width(HEIGHT_TITLE) - 8, n2, -1);
        }

        class Entry
        extends ObjectSelectionList.Entry<Entry> {
            Entry() {
            }

            @Override
            public void render(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, int n5, int n6, int n7, boolean bl, float f) {
                FlatLayerInfo flatLayerInfo = CreateFlatWorldScreen.this.generator.getLayersInfo().get(CreateFlatWorldScreen.this.generator.getLayersInfo().size() - n - 1);
                BlockState blockState = flatLayerInfo.getBlockState();
                ItemStack itemStack = this.getDisplayItem(blockState);
                this.blitSlot(guiGraphics, n3, n2, itemStack);
                int n8 = n2 + n5 / 2 - CreateFlatWorldScreen.this.font.lineHeight / 2;
                guiGraphics.drawString(CreateFlatWorldScreen.this.font, itemStack.getHoverName(), n3 + 18 + 5, n8, -1);
                MutableComponent mutableComponent = n == 0 ? Component.translatable("createWorld.customize.flat.layer.top", flatLayerInfo.getHeight()) : (n == CreateFlatWorldScreen.this.generator.getLayersInfo().size() - 1 ? Component.translatable("createWorld.customize.flat.layer.bottom", flatLayerInfo.getHeight()) : Component.translatable("createWorld.customize.flat.layer", flatLayerInfo.getHeight()));
                guiGraphics.drawString(CreateFlatWorldScreen.this.font, mutableComponent, n3 + n4 - CreateFlatWorldScreen.this.font.width(mutableComponent) - 8, n8, -1);
            }

            private ItemStack getDisplayItem(BlockState blockState) {
                Item item = blockState.getBlock().asItem();
                if (item == Items.AIR) {
                    if (blockState.is(Blocks.WATER)) {
                        item = Items.WATER_BUCKET;
                    } else if (blockState.is(Blocks.LAVA)) {
                        item = Items.LAVA_BUCKET;
                    }
                }
                return new ItemStack(item);
            }

            @Override
            public Component getNarration() {
                FlatLayerInfo flatLayerInfo = CreateFlatWorldScreen.this.generator.getLayersInfo().get(CreateFlatWorldScreen.this.generator.getLayersInfo().size() - DetailsList.this.children().indexOf(this) - 1);
                ItemStack itemStack = this.getDisplayItem(flatLayerInfo.getBlockState());
                if (!itemStack.isEmpty()) {
                    return Component.translatable("narrator.select", itemStack.getHoverName());
                }
                return CommonComponents.EMPTY;
            }

            @Override
            public boolean mouseClicked(double d, double d2, int n) {
                DetailsList.this.setSelected(this);
                return super.mouseClicked(d, d2, n);
            }

            private void blitSlot(GuiGraphics guiGraphics, int n, int n2, ItemStack itemStack) {
                this.blitSlotBg(guiGraphics, n + 1, n2 + 1);
                if (!itemStack.isEmpty()) {
                    guiGraphics.renderFakeItem(itemStack, n + 2, n2 + 2);
                }
            }

            private void blitSlotBg(GuiGraphics guiGraphics, int n, int n2) {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_SPRITE, n, n2, 18, 18);
            }
        }
    }
}

