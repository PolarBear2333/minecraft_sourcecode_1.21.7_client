/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Vindicator;

public class VindicatorRenderer
extends IllagerRenderer<Vindicator, IllagerRenderState> {
    private static final ResourceLocation VINDICATOR = ResourceLocation.withDefaultNamespace("textures/entity/illager/vindicator.png");

    public VindicatorRenderer(EntityRendererProvider.Context context) {
        super(context, new IllagerModel(context.bakeLayer(ModelLayers.VINDICATOR)), 0.5f);
        this.addLayer(new ItemInHandLayer<IllagerRenderState, IllagerModel<IllagerRenderState>>(this, (RenderLayerParent)this){

            @Override
            public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, IllagerRenderState illagerRenderState, float f, float f2) {
                if (illagerRenderState.isAggressive) {
                    super.render(poseStack, multiBufferSource, n, illagerRenderState, f, f2);
                }
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(IllagerRenderState illagerRenderState) {
        return VINDICATOR;
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

