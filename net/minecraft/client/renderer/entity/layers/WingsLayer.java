/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;

public class WingsLayer<S extends HumanoidRenderState, M extends EntityModel<S>>
extends RenderLayer<S, M> {
    private final ElytraModel elytraModel;
    private final ElytraModel elytraBabyModel;
    private final EquipmentLayerRenderer equipmentRenderer;

    public WingsLayer(RenderLayerParent<S, M> renderLayerParent, EntityModelSet entityModelSet, EquipmentLayerRenderer equipmentLayerRenderer) {
        super(renderLayerParent);
        this.elytraModel = new ElytraModel(entityModelSet.bakeLayer(ModelLayers.ELYTRA));
        this.elytraBabyModel = new ElytraModel(entityModelSet.bakeLayer(ModelLayers.ELYTRA_BABY));
        this.equipmentRenderer = equipmentLayerRenderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, S s, float f, float f2) {
        ItemStack itemStack = ((HumanoidRenderState)s).chestEquipment;
        Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
        if (equippable == null || equippable.assetId().isEmpty()) {
            return;
        }
        ResourceLocation resourceLocation = WingsLayer.getPlayerElytraTexture(s);
        ElytraModel elytraModel = ((HumanoidRenderState)s).isBaby ? this.elytraBabyModel : this.elytraModel;
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.0f, 0.125f);
        elytraModel.setupAnim((HumanoidRenderState)s);
        this.equipmentRenderer.renderLayers(EquipmentClientInfo.LayerType.WINGS, equippable.assetId().get(), elytraModel, itemStack, poseStack, multiBufferSource, n, resourceLocation);
        poseStack.popPose();
    }

    @Nullable
    private static ResourceLocation getPlayerElytraTexture(HumanoidRenderState humanoidRenderState) {
        if (humanoidRenderState instanceof PlayerRenderState) {
            PlayerRenderState playerRenderState = (PlayerRenderState)humanoidRenderState;
            PlayerSkin playerSkin = playerRenderState.skin;
            if (playerSkin.elytraTexture() != null) {
                return playerSkin.elytraTexture();
            }
            if (playerSkin.capeTexture() != null && playerRenderState.showCape) {
                return playerSkin.capeTexture();
            }
        }
        return null;
    }
}

