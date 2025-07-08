/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.TropicalFishModelA;
import net.minecraft.client.model.TropicalFishModelB;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.TropicalFishPatternLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.TropicalFishRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.TropicalFish;
import org.joml.Quaternionfc;

public class TropicalFishRenderer
extends MobRenderer<TropicalFish, TropicalFishRenderState, EntityModel<TropicalFishRenderState>> {
    private final EntityModel<TropicalFishRenderState> modelA = this.getModel();
    private final EntityModel<TropicalFishRenderState> modelB;
    private static final ResourceLocation MODEL_A_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/fish/tropical_a.png");
    private static final ResourceLocation MODEL_B_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/fish/tropical_b.png");

    public TropicalFishRenderer(EntityRendererProvider.Context context) {
        super(context, new TropicalFishModelA(context.bakeLayer(ModelLayers.TROPICAL_FISH_SMALL)), 0.15f);
        this.modelB = new TropicalFishModelB(context.bakeLayer(ModelLayers.TROPICAL_FISH_LARGE));
        this.addLayer(new TropicalFishPatternLayer(this, context.getModelSet()));
    }

    @Override
    public ResourceLocation getTextureLocation(TropicalFishRenderState tropicalFishRenderState) {
        return switch (tropicalFishRenderState.pattern.base()) {
            default -> throw new MatchException(null, null);
            case TropicalFish.Base.SMALL -> MODEL_A_TEXTURE;
            case TropicalFish.Base.LARGE -> MODEL_B_TEXTURE;
        };
    }

    @Override
    public TropicalFishRenderState createRenderState() {
        return new TropicalFishRenderState();
    }

    @Override
    public void extractRenderState(TropicalFish tropicalFish, TropicalFishRenderState tropicalFishRenderState, float f) {
        super.extractRenderState(tropicalFish, tropicalFishRenderState, f);
        tropicalFishRenderState.pattern = tropicalFish.getPattern();
        tropicalFishRenderState.baseColor = tropicalFish.getBaseColor().getTextureDiffuseColor();
        tropicalFishRenderState.patternColor = tropicalFish.getPatternColor().getTextureDiffuseColor();
    }

    @Override
    public void render(TropicalFishRenderState tropicalFishRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        this.model = switch (tropicalFishRenderState.pattern.base()) {
            default -> throw new MatchException(null, null);
            case TropicalFish.Base.SMALL -> this.modelA;
            case TropicalFish.Base.LARGE -> this.modelB;
        };
        super.render(tropicalFishRenderState, poseStack, multiBufferSource, n);
    }

    @Override
    protected int getModelTint(TropicalFishRenderState tropicalFishRenderState) {
        return tropicalFishRenderState.baseColor;
    }

    @Override
    protected void setupRotations(TropicalFishRenderState tropicalFishRenderState, PoseStack poseStack, float f, float f2) {
        super.setupRotations(tropicalFishRenderState, poseStack, f, f2);
        float f3 = 4.3f * Mth.sin(0.6f * tropicalFishRenderState.ageInTicks);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(f3));
        if (!tropicalFishRenderState.isInWater) {
            poseStack.translate(0.2f, 0.1f, 0.0f);
            poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(90.0f));
        }
    }

    @Override
    public /* synthetic */ ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((TropicalFishRenderState)livingEntityRenderState);
    }

    @Override
    protected /* synthetic */ int getModelTint(LivingEntityRenderState livingEntityRenderState) {
        return this.getModelTint((TropicalFishRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

