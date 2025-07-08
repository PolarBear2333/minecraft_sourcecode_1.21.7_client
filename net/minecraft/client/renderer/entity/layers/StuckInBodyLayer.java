/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Quaternionfc;

public abstract class StuckInBodyLayer<M extends PlayerModel>
extends RenderLayer<PlayerRenderState, M> {
    private final Model model;
    private final ResourceLocation texture;
    private final PlacementStyle placementStyle;

    public StuckInBodyLayer(LivingEntityRenderer<?, PlayerRenderState, M> livingEntityRenderer, Model model, ResourceLocation resourceLocation, PlacementStyle placementStyle) {
        super(livingEntityRenderer);
        this.model = model;
        this.texture = resourceLocation;
        this.placementStyle = placementStyle;
    }

    protected abstract int numStuck(PlayerRenderState var1);

    private void renderStuckItem(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, float f, float f2, float f3) {
        float f4 = Mth.sqrt(f * f + f3 * f3);
        float f5 = (float)(Math.atan2(f, f3) * 57.2957763671875);
        float f6 = (float)(Math.atan2(f2, f4) * 57.2957763671875);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(f5 - 90.0f));
        poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(f6));
        this.model.renderToBuffer(poseStack, multiBufferSource.getBuffer(this.model.renderType(this.texture)), n, OverlayTexture.NO_OVERLAY);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, PlayerRenderState playerRenderState, float f, float f2) {
        int n2 = this.numStuck(playerRenderState);
        if (n2 <= 0) {
            return;
        }
        RandomSource randomSource = RandomSource.create(playerRenderState.id);
        for (int i = 0; i < n2; ++i) {
            poseStack.pushPose();
            ModelPart modelPart = ((PlayerModel)this.getParentModel()).getRandomBodyPart(randomSource);
            ModelPart.Cube cube = modelPart.getRandomCube(randomSource);
            modelPart.translateAndRotate(poseStack);
            float f3 = randomSource.nextFloat();
            float f4 = randomSource.nextFloat();
            float f5 = randomSource.nextFloat();
            if (this.placementStyle == PlacementStyle.ON_SURFACE) {
                int n3 = randomSource.nextInt(3);
                switch (n3) {
                    case 0: {
                        f3 = StuckInBodyLayer.snapToFace(f3);
                        break;
                    }
                    case 1: {
                        f4 = StuckInBodyLayer.snapToFace(f4);
                        break;
                    }
                    default: {
                        f5 = StuckInBodyLayer.snapToFace(f5);
                    }
                }
            }
            poseStack.translate(Mth.lerp(f3, cube.minX, cube.maxX) / 16.0f, Mth.lerp(f4, cube.minY, cube.maxY) / 16.0f, Mth.lerp(f5, cube.minZ, cube.maxZ) / 16.0f);
            this.renderStuckItem(poseStack, multiBufferSource, n, -(f3 * 2.0f - 1.0f), -(f4 * 2.0f - 1.0f), -(f5 * 2.0f - 1.0f));
            poseStack.popPose();
        }
    }

    private static float snapToFace(float f) {
        return f > 0.5f ? 1.0f : 0.5f;
    }

    public static enum PlacementStyle {
        IN_CUBE,
        ON_SURFACE;

    }
}

