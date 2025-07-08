/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens.inventory;

import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.CartographyTableMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class CartographyTableScreen
extends AbstractContainerScreen<CartographyTableMenu> {
    private static final ResourceLocation ERROR_SPRITE = ResourceLocation.withDefaultNamespace("container/cartography_table/error");
    private static final ResourceLocation SCALED_MAP_SPRITE = ResourceLocation.withDefaultNamespace("container/cartography_table/scaled_map");
    private static final ResourceLocation DUPLICATED_MAP_SPRITE = ResourceLocation.withDefaultNamespace("container/cartography_table/duplicated_map");
    private static final ResourceLocation MAP_SPRITE = ResourceLocation.withDefaultNamespace("container/cartography_table/map");
    private static final ResourceLocation LOCKED_SPRITE = ResourceLocation.withDefaultNamespace("container/cartography_table/locked");
    private static final ResourceLocation BG_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/container/cartography_table.png");
    private final MapRenderState mapRenderState = new MapRenderState();

    public CartographyTableScreen(CartographyTableMenu cartographyTableMenu, Inventory inventory, Component component) {
        super(cartographyTableMenu, inventory, component);
        this.titleLabelY -= 2;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        super.render(guiGraphics, n, n2, f);
        this.renderTooltip(guiGraphics, n, n2);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float f, int n, int n2) {
        MapItemSavedData mapItemSavedData;
        int n3 = this.leftPos;
        int n4 = this.topPos;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BG_LOCATION, n3, n4, 0.0f, 0.0f, this.imageWidth, this.imageHeight, 256, 256);
        ItemStack itemStack = ((CartographyTableMenu)this.menu).getSlot(1).getItem();
        boolean bl = itemStack.is(Items.MAP);
        boolean bl2 = itemStack.is(Items.PAPER);
        boolean bl3 = itemStack.is(Items.GLASS_PANE);
        ItemStack itemStack2 = ((CartographyTableMenu)this.menu).getSlot(0).getItem();
        MapId mapId = itemStack2.get(DataComponents.MAP_ID);
        boolean bl4 = false;
        if (mapId != null) {
            mapItemSavedData = MapItem.getSavedData(mapId, (Level)this.minecraft.level);
            if (mapItemSavedData != null) {
                if (mapItemSavedData.locked) {
                    bl4 = true;
                    if (bl2 || bl3) {
                        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, ERROR_SPRITE, n3 + 35, n4 + 31, 28, 21);
                    }
                }
                if (bl2 && mapItemSavedData.scale >= 4) {
                    bl4 = true;
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, ERROR_SPRITE, n3 + 35, n4 + 31, 28, 21);
                }
            }
        } else {
            mapItemSavedData = null;
        }
        this.renderResultingMap(guiGraphics, mapId, mapItemSavedData, bl, bl2, bl3, bl4);
    }

    private void renderResultingMap(GuiGraphics guiGraphics, @Nullable MapId mapId, @Nullable MapItemSavedData mapItemSavedData, boolean bl, boolean bl2, boolean bl3, boolean bl4) {
        int n = this.leftPos;
        int n2 = this.topPos;
        if (bl2 && !bl4) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCALED_MAP_SPRITE, n + 67, n2 + 13, 66, 66);
            this.renderMap(guiGraphics, mapId, mapItemSavedData, n + 85, n2 + 31, 0.226f);
        } else if (bl) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, DUPLICATED_MAP_SPRITE, n + 67 + 16, n2 + 13, 50, 66);
            this.renderMap(guiGraphics, mapId, mapItemSavedData, n + 86, n2 + 16, 0.34f);
            guiGraphics.nextStratum();
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, DUPLICATED_MAP_SPRITE, n + 67, n2 + 13 + 16, 50, 66);
            this.renderMap(guiGraphics, mapId, mapItemSavedData, n + 70, n2 + 32, 0.34f);
        } else if (bl3) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, MAP_SPRITE, n + 67, n2 + 13, 66, 66);
            this.renderMap(guiGraphics, mapId, mapItemSavedData, n + 71, n2 + 17, 0.45f);
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, LOCKED_SPRITE, n + 118, n2 + 60, 10, 14);
        } else {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, MAP_SPRITE, n + 67, n2 + 13, 66, 66);
            this.renderMap(guiGraphics, mapId, mapItemSavedData, n + 71, n2 + 17, 0.45f);
        }
    }

    private void renderMap(GuiGraphics guiGraphics, @Nullable MapId mapId, @Nullable MapItemSavedData mapItemSavedData, int n, int n2, float f) {
        if (mapId != null && mapItemSavedData != null) {
            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().translate((float)n, (float)n2);
            guiGraphics.pose().scale(f, f);
            this.minecraft.getMapRenderer().extractRenderState(mapId, mapItemSavedData, this.mapRenderState);
            guiGraphics.submitMapRenderState(this.mapRenderState);
            guiGraphics.pose().popMatrix();
        }
    }
}

