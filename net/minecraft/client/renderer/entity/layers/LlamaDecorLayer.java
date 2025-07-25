/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.LlamaModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LlamaRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;

public class LlamaDecorLayer
extends RenderLayer<LlamaRenderState, LlamaModel> {
    private final LlamaModel adultModel;
    private final LlamaModel babyModel;
    private final EquipmentLayerRenderer equipmentRenderer;

    public LlamaDecorLayer(RenderLayerParent<LlamaRenderState, LlamaModel> renderLayerParent, EntityModelSet entityModelSet, EquipmentLayerRenderer equipmentLayerRenderer) {
        super(renderLayerParent);
        this.equipmentRenderer = equipmentLayerRenderer;
        this.adultModel = new LlamaModel(entityModelSet.bakeLayer(ModelLayers.LLAMA_DECOR));
        this.babyModel = new LlamaModel(entityModelSet.bakeLayer(ModelLayers.LLAMA_BABY_DECOR));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, LlamaRenderState llamaRenderState, float f, float f2) {
        ItemStack itemStack = llamaRenderState.bodyItem;
        Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
        if (equippable != null && equippable.assetId().isPresent()) {
            this.renderEquipment(poseStack, multiBufferSource, llamaRenderState, itemStack, equippable.assetId().get(), n);
        } else if (llamaRenderState.isTraderLlama) {
            this.renderEquipment(poseStack, multiBufferSource, llamaRenderState, ItemStack.EMPTY, EquipmentAssets.TRADER_LLAMA, n);
        }
    }

    private void renderEquipment(PoseStack poseStack, MultiBufferSource multiBufferSource, LlamaRenderState llamaRenderState, ItemStack itemStack, ResourceKey<EquipmentAsset> resourceKey, int n) {
        LlamaModel llamaModel = llamaRenderState.isBaby ? this.babyModel : this.adultModel;
        llamaModel.setupAnim(llamaRenderState);
        this.equipmentRenderer.renderLayers(EquipmentClientInfo.LayerType.LLAMA_BODY, resourceKey, llamaModel, itemStack, poseStack, multiBufferSource, n);
    }
}

