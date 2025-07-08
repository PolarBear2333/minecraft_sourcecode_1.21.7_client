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
import net.minecraft.client.renderer.entity.state.EvokerRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.SpellcasterIllager;

public class EvokerRenderer<T extends SpellcasterIllager>
extends IllagerRenderer<T, EvokerRenderState> {
    private static final ResourceLocation EVOKER_ILLAGER = ResourceLocation.withDefaultNamespace("textures/entity/illager/evoker.png");

    public EvokerRenderer(EntityRendererProvider.Context context) {
        super(context, new IllagerModel(context.bakeLayer(ModelLayers.EVOKER)), 0.5f);
        this.addLayer(new ItemInHandLayer<EvokerRenderState, IllagerModel<EvokerRenderState>>(this, (RenderLayerParent)this){

            @Override
            public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, EvokerRenderState evokerRenderState, float f, float f2) {
                if (evokerRenderState.isCastingSpell) {
                    super.render(poseStack, multiBufferSource, n, evokerRenderState, f, f2);
                }
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(EvokerRenderState evokerRenderState) {
        return EVOKER_ILLAGER;
    }

    @Override
    public EvokerRenderState createRenderState() {
        return new EvokerRenderState();
    }

    @Override
    public void extractRenderState(T t, EvokerRenderState evokerRenderState, float f) {
        super.extractRenderState(t, evokerRenderState, f);
        evokerRenderState.isCastingSpell = ((SpellcasterIllager)t).isCastingSpell();
    }

    @Override
    public /* synthetic */ ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((EvokerRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

