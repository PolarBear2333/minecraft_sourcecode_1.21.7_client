/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.model.EndermanModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.state.EndermanRenderState;
import net.minecraft.resources.ResourceLocation;

public class EnderEyesLayer
extends EyesLayer<EndermanRenderState, EndermanModel<EndermanRenderState>> {
    private static final RenderType ENDERMAN_EYES = RenderType.eyes(ResourceLocation.withDefaultNamespace("textures/entity/enderman/enderman_eyes.png"));

    public EnderEyesLayer(RenderLayerParent<EndermanRenderState, EndermanModel<EndermanRenderState>> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public RenderType renderType() {
        return ENDERMAN_EYES;
    }
}

