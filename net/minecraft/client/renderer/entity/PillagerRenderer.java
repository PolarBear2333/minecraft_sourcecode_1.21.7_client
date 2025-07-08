/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Pillager;

public class PillagerRenderer
extends IllagerRenderer<Pillager, IllagerRenderState> {
    private static final ResourceLocation PILLAGER = ResourceLocation.withDefaultNamespace("textures/entity/illager/pillager.png");

    public PillagerRenderer(EntityRendererProvider.Context context) {
        super(context, new IllagerModel(context.bakeLayer(ModelLayers.PILLAGER)), 0.5f);
        this.addLayer(new ItemInHandLayer<IllagerRenderState, IllagerModel<IllagerRenderState>>(this));
    }

    @Override
    public ResourceLocation getTextureLocation(IllagerRenderState illagerRenderState) {
        return PILLAGER;
    }

    @Override
    public IllagerRenderState createRenderState() {
        return new IllagerRenderState();
    }

    @Override
    public /* synthetic */ ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((IllagerRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

