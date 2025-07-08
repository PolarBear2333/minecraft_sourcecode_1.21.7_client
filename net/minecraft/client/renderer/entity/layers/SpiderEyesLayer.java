/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;

public class SpiderEyesLayer<M extends SpiderModel>
extends EyesLayer<LivingEntityRenderState, M> {
    private static final RenderType SPIDER_EYES = RenderType.eyes(ResourceLocation.withDefaultNamespace("textures/entity/spider_eyes.png"));

    public SpiderEyesLayer(RenderLayerParent<LivingEntityRenderState, M> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public RenderType renderType() {
        return SPIDER_EYES;
    }
}

