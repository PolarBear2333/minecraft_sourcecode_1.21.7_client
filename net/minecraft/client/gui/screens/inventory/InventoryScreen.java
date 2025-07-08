/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 */
package net.minecraft.client.gui.screens.inventory;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import net.minecraft.client.gui.screens.recipebook.CraftingRecipeBookComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

public class InventoryScreen
extends AbstractRecipeBookScreen<InventoryMenu> {
    private float xMouse;
    private float yMouse;
    private boolean buttonClicked;
    private final EffectsInInventory effects;

    public InventoryScreen(Player player) {
        super(player.inventoryMenu, new CraftingRecipeBookComponent(player.inventoryMenu), player.getInventory(), Component.translatable("container.crafting"));
        this.titleLabelX = 97;
        this.effects = new EffectsInInventory(this);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if (this.minecraft.player.hasInfiniteMaterials()) {
            this.minecraft.setScreen(new CreativeModeInventoryScreen(this.minecraft.player, this.minecraft.player.connection.enabledFeatures(), this.minecraft.options.operatorItemsTab().get()));
        }
    }

    @Override
    protected void init() {
        if (this.minecraft.player.hasInfiniteMaterials()) {
            this.minecraft.setScreen(new CreativeModeInventoryScreen(this.minecraft.player, this.minecraft.player.connection.enabledFeatures(), this.minecraft.options.operatorItemsTab().get()));
            return;
        }
        super.init();
    }

    @Override
    protected ScreenPosition getRecipeBookButtonPosition() {
        return new ScreenPosition(this.leftPos + 104, this.height / 2 - 22);
    }

    @Override
    protected void onRecipeBookButtonClick() {
        this.buttonClicked = true;
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int n, int n2) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, -12566464, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        this.effects.renderEffects(guiGraphics, n, n2);
        super.render(guiGraphics, n, n2, f);
        this.effects.renderTooltip(guiGraphics, n, n2);
        this.xMouse = n;
        this.yMouse = n2;
    }

    @Override
    public boolean showsActiveEffects() {
        return this.effects.canSeeEffects();
    }

    @Override
    protected boolean isBiggerResultSlot() {
        return false;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float f, int n, int n2) {
        int n3 = this.leftPos;
        int n4 = this.topPos;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, INVENTORY_LOCATION, n3, n4, 0.0f, 0.0f, this.imageWidth, this.imageHeight, 256, 256);
        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, n3 + 26, n4 + 8, n3 + 75, n4 + 78, 30, 0.0625f, this.xMouse, this.yMouse, this.minecraft.player);
    }

    public static void renderEntityInInventoryFollowsMouse(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, int n5, float f, float f2, float f3, LivingEntity livingEntity) {
        float f4 = (float)(n + n3) / 2.0f;
        float f5 = (float)(n2 + n4) / 2.0f;
        guiGraphics.enableScissor(n, n2, n3, n4);
        float f6 = (float)Math.atan((f4 - f2) / 40.0f);
        float f7 = (float)Math.atan((f5 - f3) / 40.0f);
        Quaternionf quaternionf = new Quaternionf().rotateZ((float)Math.PI);
        Quaternionf quaternionf2 = new Quaternionf().rotateX(f7 * 20.0f * ((float)Math.PI / 180));
        quaternionf.mul((Quaternionfc)quaternionf2);
        float f8 = livingEntity.yBodyRot;
        float f9 = livingEntity.getYRot();
        float f10 = livingEntity.getXRot();
        float f11 = livingEntity.yHeadRotO;
        float f12 = livingEntity.yHeadRot;
        livingEntity.yBodyRot = 180.0f + f6 * 20.0f;
        livingEntity.setYRot(180.0f + f6 * 40.0f);
        livingEntity.setXRot(-f7 * 20.0f);
        livingEntity.yHeadRot = livingEntity.getYRot();
        livingEntity.yHeadRotO = livingEntity.getYRot();
        float f13 = livingEntity.getScale();
        Vector3f vector3f = new Vector3f(0.0f, livingEntity.getBbHeight() / 2.0f + f * f13, 0.0f);
        float f14 = (float)n5 / f13;
        InventoryScreen.renderEntityInInventory(guiGraphics, n, n2, n3, n4, f14, vector3f, quaternionf, quaternionf2, livingEntity);
        livingEntity.yBodyRot = f8;
        livingEntity.setYRot(f9);
        livingEntity.setXRot(f10);
        livingEntity.yHeadRotO = f11;
        livingEntity.yHeadRot = f12;
        guiGraphics.disableScissor();
    }

    public static void renderEntityInInventory(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, float f, Vector3f vector3f, Quaternionf quaternionf, @Nullable Quaternionf quaternionf2, LivingEntity livingEntity) {
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer<?, LivingEntity> entityRenderer = entityRenderDispatcher.getRenderer(livingEntity);
        LivingEntity livingEntity2 = entityRenderer.createRenderState(livingEntity, 1.0f);
        ((EntityRenderState)((Object)livingEntity2)).hitboxesRenderState = null;
        guiGraphics.submitEntityRenderState((EntityRenderState)((Object)livingEntity2), f, vector3f, quaternionf, quaternionf2, n, n2, n3, n4);
    }

    @Override
    public boolean mouseReleased(double d, double d2, int n) {
        if (this.buttonClicked) {
            this.buttonClicked = false;
            return true;
        }
        return super.mouseReleased(d, d2, n);
    }
}

