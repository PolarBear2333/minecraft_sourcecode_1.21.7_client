/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Arrays;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.IllusionerRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class IllusionerRenderer
extends IllagerRenderer<Illusioner, IllusionerRenderState> {
    private static final ResourceLocation ILLUSIONER = ResourceLocation.withDefaultNamespace("textures/entity/illager/illusioner.png");

    public IllusionerRenderer(EntityRendererProvider.Context context) {
        super(context, new IllagerModel(context.bakeLayer(ModelLayers.ILLUSIONER)), 0.5f);
        this.addLayer(new ItemInHandLayer<IllusionerRenderState, IllagerModel<IllusionerRenderState>>(this, (RenderLayerParent)this){

            @Override
            public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, IllusionerRenderState illusionerRenderState, float f, float f2) {
                if (illusionerRenderState.isCastingSpell || illusionerRenderState.isAggressive) {
                    super.render(poseStack, multiBufferSource, n, illusionerRenderState, f, f2);
                }
            }
        });
        ((IllagerModel)this.model).getHat().visible = true;
    }

    @Override
    public ResourceLocation getTextureLocation(IllusionerRenderState illusionerRenderState) {
        return ILLUSIONER;
    }

    @Override
    public IllusionerRenderState createRenderState() {
        return new IllusionerRenderState();
    }

    @Override
    public void extractRenderState(Illusioner illusioner, IllusionerRenderState illusionerRenderState, float f) {
        super.extractRenderState(illusioner, illusionerRenderState, f);
        Vec3[] vec3Array = illusioner.getIllusionOffsets(f);
        illusionerRenderState.illusionOffsets = Arrays.copyOf(vec3Array, vec3Array.length);
        illusionerRenderState.isCastingSpell = illusioner.isCastingSpell();
    }

    @Override
    public void render(IllusionerRenderState illusionerRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        if (illusionerRenderState.isInvisible) {
            Vec3[] vec3Array = illusionerRenderState.illusionOffsets;
            for (int i = 0; i < vec3Array.length; ++i) {
                poseStack.pushPose();
                poseStack.translate(vec3Array[i].x + (double)Mth.cos((float)i + illusionerRenderState.ageInTicks * 0.5f) * 0.025, vec3Array[i].y + (double)Mth.cos((float)i + illusionerRenderState.ageInTicks * 0.75f) * 0.0125, vec3Array[i].z + (double)Mth.cos((float)i + illusionerRenderState.ageInTicks * 0.7f) * 0.025);
                super.render(illusionerRenderState, poseStack, multiBufferSource, n);
                poseStack.popPose();
            }
        } else {
            super.render(illusionerRenderState, poseStack, multiBufferSource, n);
        }
    }

    @Override
    protected boolean isBodyVisible(IllusionerRenderState illusionerRenderState) {
        return true;
    }

    @Override
    protected AABB getBoundingBoxForCulling(Illusioner illusioner) {
        return super.getBoundingBoxForCulling(illusioner).inflate(3.0, 0.0, 3.0);
    }

    @Override
    protected /* synthetic */ boolean isBodyVisible(LivingEntityRenderState livingEntityRenderState) {
        return this.isBodyVisible((IllusionerRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((IllusionerRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

