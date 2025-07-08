/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Map;
import net.minecraft.client.model.PandaModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.PandaHoldsItemLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.HoldingEntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.PandaRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Panda;
import org.joml.Quaternionfc;

public class PandaRenderer
extends AgeableMobRenderer<Panda, PandaRenderState, PandaModel> {
    private static final Map<Panda.Gene, ResourceLocation> TEXTURES = Maps.newEnumMap(Map.of(Panda.Gene.NORMAL, ResourceLocation.withDefaultNamespace("textures/entity/panda/panda.png"), Panda.Gene.LAZY, ResourceLocation.withDefaultNamespace("textures/entity/panda/lazy_panda.png"), Panda.Gene.WORRIED, ResourceLocation.withDefaultNamespace("textures/entity/panda/worried_panda.png"), Panda.Gene.PLAYFUL, ResourceLocation.withDefaultNamespace("textures/entity/panda/playful_panda.png"), Panda.Gene.BROWN, ResourceLocation.withDefaultNamespace("textures/entity/panda/brown_panda.png"), Panda.Gene.WEAK, ResourceLocation.withDefaultNamespace("textures/entity/panda/weak_panda.png"), Panda.Gene.AGGRESSIVE, ResourceLocation.withDefaultNamespace("textures/entity/panda/aggressive_panda.png")));

    public PandaRenderer(EntityRendererProvider.Context context) {
        super(context, new PandaModel(context.bakeLayer(ModelLayers.PANDA)), new PandaModel(context.bakeLayer(ModelLayers.PANDA_BABY)), 0.9f);
        this.addLayer(new PandaHoldsItemLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(PandaRenderState pandaRenderState) {
        return TEXTURES.getOrDefault(pandaRenderState.variant, TEXTURES.get(Panda.Gene.NORMAL));
    }

    @Override
    public PandaRenderState createRenderState() {
        return new PandaRenderState();
    }

    @Override
    public void extractRenderState(Panda panda, PandaRenderState pandaRenderState, float f) {
        super.extractRenderState(panda, pandaRenderState, f);
        HoldingEntityRenderState.extractHoldingEntityRenderState(panda, pandaRenderState, this.itemModelResolver);
        pandaRenderState.variant = panda.getVariant();
        pandaRenderState.isUnhappy = panda.getUnhappyCounter() > 0;
        pandaRenderState.isSneezing = panda.isSneezing();
        pandaRenderState.sneezeTime = panda.getSneezeCounter();
        pandaRenderState.isEating = panda.isEating();
        pandaRenderState.isScared = panda.isScared();
        pandaRenderState.isSitting = panda.isSitting();
        pandaRenderState.sitAmount = panda.getSitAmount(f);
        pandaRenderState.lieOnBackAmount = panda.getLieOnBackAmount(f);
        pandaRenderState.rollAmount = panda.isBaby() ? 0.0f : panda.getRollAmount(f);
        pandaRenderState.rollTime = panda.rollCounter > 0 ? (float)panda.rollCounter + f : 0.0f;
    }

    @Override
    protected void setupRotations(PandaRenderState pandaRenderState, PoseStack poseStack, float f, float f2) {
        float f3;
        float f4;
        super.setupRotations(pandaRenderState, poseStack, f, f2);
        if (pandaRenderState.rollTime > 0.0f) {
            float f5;
            f4 = Mth.frac(pandaRenderState.rollTime);
            int n = Mth.floor(pandaRenderState.rollTime);
            int n2 = n + 1;
            float f6 = 7.0f;
            float f7 = f5 = pandaRenderState.isBaby ? 0.3f : 0.8f;
            if ((float)n < 8.0f) {
                float f8 = 90.0f * (float)n / 7.0f;
                float f9 = 90.0f * (float)n2 / 7.0f;
                float f10 = this.getAngle(f8, f9, n2, f4, 8.0f);
                poseStack.translate(0.0f, (f5 + 0.2f) * (f10 / 90.0f), 0.0f);
                poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(-f10));
            } else if ((float)n < 16.0f) {
                float f11 = ((float)n - 8.0f) / 7.0f;
                float f12 = 90.0f + 90.0f * f11;
                float f13 = 90.0f + 90.0f * ((float)n2 - 8.0f) / 7.0f;
                float f14 = this.getAngle(f12, f13, n2, f4, 16.0f);
                poseStack.translate(0.0f, f5 + 0.2f + (f5 - 0.2f) * (f14 - 90.0f) / 90.0f, 0.0f);
                poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(-f14));
            } else if ((float)n < 24.0f) {
                float f15 = ((float)n - 16.0f) / 7.0f;
                float f16 = 180.0f + 90.0f * f15;
                float f17 = 180.0f + 90.0f * ((float)n2 - 16.0f) / 7.0f;
                float f18 = this.getAngle(f16, f17, n2, f4, 24.0f);
                poseStack.translate(0.0f, f5 + f5 * (270.0f - f18) / 90.0f, 0.0f);
                poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(-f18));
            } else if (n < 32) {
                float f19 = ((float)n - 24.0f) / 7.0f;
                float f20 = 270.0f + 90.0f * f19;
                float f21 = 270.0f + 90.0f * ((float)n2 - 24.0f) / 7.0f;
                float f22 = this.getAngle(f20, f21, n2, f4, 32.0f);
                poseStack.translate(0.0f, f5 * ((360.0f - f22) / 90.0f), 0.0f);
                poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(-f22));
            }
        }
        if ((f4 = pandaRenderState.sitAmount) > 0.0f) {
            poseStack.translate(0.0f, 0.8f * f4, 0.0f);
            poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(Mth.lerp(f4, pandaRenderState.xRot, pandaRenderState.xRot + 90.0f)));
            poseStack.translate(0.0f, -1.0f * f4, 0.0f);
            if (pandaRenderState.isScared) {
                float f23 = (float)(Math.cos(pandaRenderState.ageInTicks * 1.25f) * Math.PI * (double)0.05f);
                poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(f23));
                if (pandaRenderState.isBaby) {
                    poseStack.translate(0.0f, 0.8f, 0.55f);
                }
            }
        }
        if ((f3 = pandaRenderState.lieOnBackAmount) > 0.0f) {
            float f24 = pandaRenderState.isBaby ? 0.5f : 1.3f;
            poseStack.translate(0.0f, f24 * f3, 0.0f);
            poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(Mth.lerp(f3, pandaRenderState.xRot, pandaRenderState.xRot + 180.0f)));
        }
    }

    private float getAngle(float f, float f2, int n, float f3, float f4) {
        if ((float)n < f4) {
            return Mth.lerp(f3, f, f2);
        }
        return f;
    }

    @Override
    public /* synthetic */ ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((PandaRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

