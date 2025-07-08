/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.TadpoleModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.frog.Tadpole;

public class TadpoleRenderer
extends MobRenderer<Tadpole, LivingEntityRenderState, TadpoleModel> {
    private static final ResourceLocation TADPOLE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/tadpole/tadpole.png");

    public TadpoleRenderer(EntityRendererProvider.Context context) {
        super(context, new TadpoleModel(context.bakeLayer(ModelLayers.TADPOLE)), 0.14f);
    }

    @Override
    public ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return TADPOLE_TEXTURE;
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

