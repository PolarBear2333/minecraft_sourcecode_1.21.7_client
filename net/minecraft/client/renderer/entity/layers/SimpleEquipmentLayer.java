/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Function;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;

public class SimpleEquipmentLayer<S extends LivingEntityRenderState, RM extends EntityModel<? super S>, EM extends EntityModel<? super S>>
extends RenderLayer<S, RM> {
    private final EquipmentLayerRenderer equipmentRenderer;
    private final EquipmentClientInfo.LayerType layer;
    private final Function<S, ItemStack> itemGetter;
    private final EM adultModel;
    private final EM babyModel;

    public SimpleEquipmentLayer(RenderLayerParent<S, RM> renderLayerParent, EquipmentLayerRenderer equipmentLayerRenderer, EquipmentClientInfo.LayerType layerType, Function<S, ItemStack> function, EM EM, EM EM2) {
        super(renderLayerParent);
        this.equipmentRenderer = equipmentLayerRenderer;
        this.layer = layerType;
        this.itemGetter = function;
        this.adultModel = EM;
        this.babyModel = EM2;
    }

    public SimpleEquipmentLayer(RenderLayerParent<S, RM> renderLayerParent, EquipmentLayerRenderer equipmentLayerRenderer, EM EM, EquipmentClientInfo.LayerType layerType, Function<S, ItemStack> function) {
        this(renderLayerParent, equipmentLayerRenderer, layerType, function, EM, EM);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, S s, float f, float f2) {
        ItemStack itemStack = this.itemGetter.apply(s);
        Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
        if (equippable == null || equippable.assetId().isEmpty()) {
            return;
        }
        EM EM = ((LivingEntityRenderState)s).isBaby ? this.babyModel : this.adultModel;
        ((EntityModel)EM).setupAnim(s);
        this.equipmentRenderer.renderLayers(this.layer, equippable.assetId().get(), (Model)EM, itemStack, poseStack, multiBufferSource, n);
    }
}

