/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.EndermiteModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Endermite;

public class EndermiteRenderer
extends MobRenderer<Endermite, LivingEntityRenderState, EndermiteModel> {
    private static final ResourceLocation ENDERMITE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/endermite.png");

    public EndermiteRenderer(EntityRendererProvider.Context context) {
        super(context, new EndermiteModel(context.bakeLayer(ModelLayers.ENDERMITE)), 0.3f);
    }

    @Override
    protected float getFlipDegrees() {
        return 180.0f;
    }

    @Override
    public ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return ENDERMITE_LOCATION;
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

