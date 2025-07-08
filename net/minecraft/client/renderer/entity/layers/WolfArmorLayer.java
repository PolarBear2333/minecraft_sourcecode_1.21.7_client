/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Crackiness;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;

public class WolfArmorLayer
extends RenderLayer<WolfRenderState, WolfModel> {
    private final WolfModel adultModel;
    private final WolfModel babyModel;
    private final EquipmentLayerRenderer equipmentRenderer;
    private static final Map<Crackiness.Level, ResourceLocation> ARMOR_CRACK_LOCATIONS = Map.of(Crackiness.Level.LOW, ResourceLocation.withDefaultNamespace("textures/entity/wolf/wolf_armor_crackiness_low.png"), Crackiness.Level.MEDIUM, ResourceLocation.withDefaultNamespace("textures/entity/wolf/wolf_armor_crackiness_medium.png"), Crackiness.Level.HIGH, ResourceLocation.withDefaultNamespace("textures/entity/wolf/wolf_armor_crackiness_high.png"));

    public WolfArmorLayer(RenderLayerParent<WolfRenderState, WolfModel> renderLayerParent, EntityModelSet entityModelSet, EquipmentLayerRenderer equipmentLayerRenderer) {
        super(renderLayerParent);
        this.adultModel = new WolfModel(entityModelSet.bakeLayer(ModelLayers.WOLF_ARMOR));
        this.babyModel = new WolfModel(entityModelSet.bakeLayer(ModelLayers.WOLF_BABY_ARMOR));
        this.equipmentRenderer = equipmentLayerRenderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, WolfRenderState wolfRenderState, float f, float f2) {
        ItemStack itemStack = wolfRenderState.bodyArmorItem;
        Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
        if (equippable == null || equippable.assetId().isEmpty()) {
            return;
        }
        WolfModel wolfModel = wolfRenderState.isBaby ? this.babyModel : this.adultModel;
        wolfModel.setupAnim(wolfRenderState);
        this.equipmentRenderer.renderLayers(EquipmentClientInfo.LayerType.WOLF_BODY, equippable.assetId().get(), wolfModel, itemStack, poseStack, multiBufferSource, n);
        this.maybeRenderCracks(poseStack, multiBufferSource, n, itemStack, wolfModel);
    }

    private void maybeRenderCracks(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, ItemStack itemStack, Model model) {
        Crackiness.Level level = Crackiness.WOLF_ARMOR.byDamage(itemStack);
        if (level == Crackiness.Level.NONE) {
            return;
        }
        ResourceLocation resourceLocation = ARMOR_CRACK_LOCATIONS.get((Object)level);
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.armorTranslucent(resourceLocation));
        model.renderToBuffer(poseStack, vertexConsumer, n, OverlayTexture.NO_OVERLAY);
    }
}

