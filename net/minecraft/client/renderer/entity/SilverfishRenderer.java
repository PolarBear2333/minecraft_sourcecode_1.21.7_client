/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.SilverfishModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Silverfish;

public class SilverfishRenderer
extends MobRenderer<Silverfish, LivingEntityRenderState, SilverfishModel> {
    private static final ResourceLocation SILVERFISH_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/silverfish.png");

    public SilverfishRenderer(EntityRendererProvider.Context context) {
        super(context, new SilverfishModel(context.bakeLayer(ModelLayers.SILVERFISH)), 0.3f);
    }

    @Override
    protected float getFlipDegrees() {
        return 180.0f;
    }

    @Override
    public ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return SILVERFISH_LOCATION;
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

