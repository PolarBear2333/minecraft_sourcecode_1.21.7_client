/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;

public class HumanoidArmorLayer<S extends HumanoidRenderState, M extends HumanoidModel<S>, A extends HumanoidModel<S>>
extends RenderLayer<S, M> {
    private final A innerModel;
    private final A outerModel;
    private final A innerModelBaby;
    private final A outerModelBaby;
    private final EquipmentLayerRenderer equipmentRenderer;

    public HumanoidArmorLayer(RenderLayerParent<S, M> renderLayerParent, A a, A a2, EquipmentLayerRenderer equipmentLayerRenderer) {
        this(renderLayerParent, a, a2, a, a2, equipmentLayerRenderer);
    }

    public HumanoidArmorLayer(RenderLayerParent<S, M> renderLayerParent, A a, A a2, A a3, A a4, EquipmentLayerRenderer equipmentLayerRenderer) {
        super(renderLayerParent);
        this.innerModel = a;
        this.outerModel = a2;
        this.innerModelBaby = a3;
        this.outerModelBaby = a4;
        this.equipmentRenderer = equipmentLayerRenderer;
    }

    public static boolean shouldRender(ItemStack itemStack, EquipmentSlot equipmentSlot) {
        Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
        return equippable != null && HumanoidArmorLayer.shouldRender(equippable, equipmentSlot);
    }

    private static boolean shouldRender(Equippable equippable, EquipmentSlot equipmentSlot) {
        return equippable.assetId().isPresent() && equippable.slot() == equipmentSlot;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, S s, float f, float f2) {
        this.renderArmorPiece(poseStack, multiBufferSource, ((HumanoidRenderState)s).chestEquipment, EquipmentSlot.CHEST, n, this.getArmorModel(s, EquipmentSlot.CHEST));
        this.renderArmorPiece(poseStack, multiBufferSource, ((HumanoidRenderState)s).legsEquipment, EquipmentSlot.LEGS, n, this.getArmorModel(s, EquipmentSlot.LEGS));
        this.renderArmorPiece(poseStack, multiBufferSource, ((HumanoidRenderState)s).feetEquipment, EquipmentSlot.FEET, n, this.getArmorModel(s, EquipmentSlot.FEET));
        this.renderArmorPiece(poseStack, multiBufferSource, ((HumanoidRenderState)s).headEquipment, EquipmentSlot.HEAD, n, this.getArmorModel(s, EquipmentSlot.HEAD));
    }

    private void renderArmorPiece(PoseStack poseStack, MultiBufferSource multiBufferSource, ItemStack itemStack, EquipmentSlot equipmentSlot, int n, A a) {
        Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
        if (equippable == null || !HumanoidArmorLayer.shouldRender(equippable, equipmentSlot)) {
            return;
        }
        ((HumanoidModel)this.getParentModel()).copyPropertiesTo(a);
        this.setPartVisibility(a, equipmentSlot);
        EquipmentClientInfo.LayerType layerType = this.usesInnerModel(equipmentSlot) ? EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS : EquipmentClientInfo.LayerType.HUMANOID;
        this.equipmentRenderer.renderLayers(layerType, equippable.assetId().orElseThrow(), (Model)a, itemStack, poseStack, multiBufferSource, n);
    }

    protected void setPartVisibility(A a, EquipmentSlot equipmentSlot) {
        ((HumanoidModel)a).setAllVisible(false);
        switch (equipmentSlot) {
            case HEAD: {
                ((HumanoidModel)a).head.visible = true;
                ((HumanoidModel)a).hat.visible = true;
                break;
            }
            case CHEST: {
                ((HumanoidModel)a).body.visible = true;
                ((HumanoidModel)a).rightArm.visible = true;
                ((HumanoidModel)a).leftArm.visible = true;
                break;
            }
            case LEGS: {
                ((HumanoidModel)a).body.visible = true;
                ((HumanoidModel)a).rightLeg.visible = true;
                ((HumanoidModel)a).leftLeg.visible = true;
                break;
            }
            case FEET: {
                ((HumanoidModel)a).rightLeg.visible = true;
                ((HumanoidModel)a).leftLeg.visible = true;
            }
        }
    }

    private A getArmorModel(S s, EquipmentSlot equipmentSlot) {
        if (this.usesInnerModel(equipmentSlot)) {
            return ((HumanoidRenderState)s).isBaby ? this.innerModelBaby : this.innerModel;
        }
        return ((HumanoidRenderState)s).isBaby ? this.outerModelBaby : this.outerModel;
    }

    private boolean usesInnerModel(EquipmentSlot equipmentSlot) {
        return equipmentSlot == EquipmentSlot.LEGS;
    }
}

