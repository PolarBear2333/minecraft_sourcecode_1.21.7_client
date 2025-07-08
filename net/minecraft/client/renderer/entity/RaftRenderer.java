/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.RaftModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.AbstractBoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.resources.ResourceLocation;

public class RaftRenderer
extends AbstractBoatRenderer {
    private final EntityModel<BoatRenderState> model;
    private final ResourceLocation texture;

    public RaftRenderer(EntityRendererProvider.Context context, ModelLayerLocation modelLayerLocation) {
        super(context);
        this.texture = modelLayerLocation.model().withPath(string -> "textures/entity/" + string + ".png");
        this.model = new RaftModel(context.bakeLayer(modelLayerLocation));
    }

    @Override
    protected EntityModel<BoatRenderState> model() {
        return this.model;
    }

    @Override
    protected RenderType renderType() {
        return this.model.renderType(this.texture);
    }
}

