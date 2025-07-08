/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.PaintingRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.PaintingTextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.level.Level;
import org.joml.Quaternionfc;

public class PaintingRenderer
extends EntityRenderer<Painting, PaintingRenderState> {
    public PaintingRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(PaintingRenderState paintingRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        PaintingVariant paintingVariant = paintingRenderState.variant;
        if (paintingVariant == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(180 - paintingRenderState.direction.get2DDataValue() * 90));
        PaintingTextureManager paintingTextureManager = Minecraft.getInstance().getPaintingTextures();
        TextureAtlasSprite textureAtlasSprite = paintingTextureManager.getBackSprite();
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entitySolidZOffsetForward(textureAtlasSprite.atlasLocation()));
        this.renderPainting(poseStack, vertexConsumer, paintingRenderState.lightCoords, paintingVariant.width(), paintingVariant.height(), paintingTextureManager.get(paintingVariant), textureAtlasSprite);
        poseStack.popPose();
        super.render(paintingRenderState, poseStack, multiBufferSource, n);
    }

    @Override
    public PaintingRenderState createRenderState() {
        return new PaintingRenderState();
    }

    @Override
    public void extractRenderState(Painting painting, PaintingRenderState paintingRenderState, float f) {
        super.extractRenderState(painting, paintingRenderState, f);
        Direction direction = painting.getDirection();
        PaintingVariant paintingVariant = painting.getVariant().value();
        paintingRenderState.direction = direction;
        paintingRenderState.variant = paintingVariant;
        int n = paintingVariant.width();
        int n2 = paintingVariant.height();
        if (paintingRenderState.lightCoords.length != n * n2) {
            paintingRenderState.lightCoords = new int[n * n2];
        }
        float f2 = (float)(-n) / 2.0f;
        float f3 = (float)(-n2) / 2.0f;
        Level level = painting.level();
        for (int i = 0; i < n2; ++i) {
            for (int j = 0; j < n; ++j) {
                float f4 = (float)j + f2 + 0.5f;
                float f5 = (float)i + f3 + 0.5f;
                int n3 = painting.getBlockX();
                int n4 = Mth.floor(painting.getY() + (double)f5);
                int n5 = painting.getBlockZ();
                switch (direction) {
                    case NORTH: {
                        n3 = Mth.floor(painting.getX() + (double)f4);
                        break;
                    }
                    case WEST: {
                        n5 = Mth.floor(painting.getZ() - (double)f4);
                        break;
                    }
                    case SOUTH: {
                        n3 = Mth.floor(painting.getX() - (double)f4);
                        break;
                    }
                    case EAST: {
                        n5 = Mth.floor(painting.getZ() + (double)f4);
                    }
                }
                paintingRenderState.lightCoords[j + i * n] = LevelRenderer.getLightColor(level, new BlockPos(n3, n4, n5));
            }
        }
    }

    private void renderPainting(PoseStack poseStack, VertexConsumer vertexConsumer, int[] nArray, int n, int n2, TextureAtlasSprite textureAtlasSprite, TextureAtlasSprite textureAtlasSprite2) {
        PoseStack.Pose pose = poseStack.last();
        float f = (float)(-n) / 2.0f;
        float f2 = (float)(-n2) / 2.0f;
        float f3 = 0.03125f;
        float f4 = textureAtlasSprite2.getU0();
        float f5 = textureAtlasSprite2.getU1();
        float f6 = textureAtlasSprite2.getV0();
        float f7 = textureAtlasSprite2.getV1();
        float f8 = textureAtlasSprite2.getU0();
        float f9 = textureAtlasSprite2.getU1();
        float f10 = textureAtlasSprite2.getV0();
        float f11 = textureAtlasSprite2.getV(0.0625f);
        float f12 = textureAtlasSprite2.getU0();
        float f13 = textureAtlasSprite2.getU(0.0625f);
        float f14 = textureAtlasSprite2.getV0();
        float f15 = textureAtlasSprite2.getV1();
        double d = 1.0 / (double)n;
        double d2 = 1.0 / (double)n2;
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n2; ++j) {
                float f16 = f + (float)(i + 1);
                float f17 = f + (float)i;
                float f18 = f2 + (float)(j + 1);
                float f19 = f2 + (float)j;
                int n3 = nArray[i + j * n];
                float f20 = textureAtlasSprite.getU((float)(d * (double)(n - i)));
                float f21 = textureAtlasSprite.getU((float)(d * (double)(n - (i + 1))));
                float f22 = textureAtlasSprite.getV((float)(d2 * (double)(n2 - j)));
                float f23 = textureAtlasSprite.getV((float)(d2 * (double)(n2 - (j + 1))));
                this.vertex(pose, vertexConsumer, f16, f19, f21, f22, -0.03125f, 0, 0, -1, n3);
                this.vertex(pose, vertexConsumer, f17, f19, f20, f22, -0.03125f, 0, 0, -1, n3);
                this.vertex(pose, vertexConsumer, f17, f18, f20, f23, -0.03125f, 0, 0, -1, n3);
                this.vertex(pose, vertexConsumer, f16, f18, f21, f23, -0.03125f, 0, 0, -1, n3);
                this.vertex(pose, vertexConsumer, f16, f18, f5, f6, 0.03125f, 0, 0, 1, n3);
                this.vertex(pose, vertexConsumer, f17, f18, f4, f6, 0.03125f, 0, 0, 1, n3);
                this.vertex(pose, vertexConsumer, f17, f19, f4, f7, 0.03125f, 0, 0, 1, n3);
                this.vertex(pose, vertexConsumer, f16, f19, f5, f7, 0.03125f, 0, 0, 1, n3);
                this.vertex(pose, vertexConsumer, f16, f18, f8, f10, -0.03125f, 0, 1, 0, n3);
                this.vertex(pose, vertexConsumer, f17, f18, f9, f10, -0.03125f, 0, 1, 0, n3);
                this.vertex(pose, vertexConsumer, f17, f18, f9, f11, 0.03125f, 0, 1, 0, n3);
                this.vertex(pose, vertexConsumer, f16, f18, f8, f11, 0.03125f, 0, 1, 0, n3);
                this.vertex(pose, vertexConsumer, f16, f19, f8, f10, 0.03125f, 0, -1, 0, n3);
                this.vertex(pose, vertexConsumer, f17, f19, f9, f10, 0.03125f, 0, -1, 0, n3);
                this.vertex(pose, vertexConsumer, f17, f19, f9, f11, -0.03125f, 0, -1, 0, n3);
                this.vertex(pose, vertexConsumer, f16, f19, f8, f11, -0.03125f, 0, -1, 0, n3);
                this.vertex(pose, vertexConsumer, f16, f18, f13, f14, 0.03125f, -1, 0, 0, n3);
                this.vertex(pose, vertexConsumer, f16, f19, f13, f15, 0.03125f, -1, 0, 0, n3);
                this.vertex(pose, vertexConsumer, f16, f19, f12, f15, -0.03125f, -1, 0, 0, n3);
                this.vertex(pose, vertexConsumer, f16, f18, f12, f14, -0.03125f, -1, 0, 0, n3);
                this.vertex(pose, vertexConsumer, f17, f18, f13, f14, -0.03125f, 1, 0, 0, n3);
                this.vertex(pose, vertexConsumer, f17, f19, f13, f15, -0.03125f, 1, 0, 0, n3);
                this.vertex(pose, vertexConsumer, f17, f19, f12, f15, 0.03125f, 1, 0, 0, n3);
                this.vertex(pose, vertexConsumer, f17, f18, f12, f14, 0.03125f, 1, 0, 0, n3);
            }
        }
    }

    private void vertex(PoseStack.Pose pose, VertexConsumer vertexConsumer, float f, float f2, float f3, float f4, float f5, int n, int n2, int n3, int n4) {
        vertexConsumer.addVertex(pose, f, f2, f5).setColor(-1).setUv(f3, f4).setOverlay(OverlayTexture.NO_OVERLAY).setLight(n4).setNormal(pose, n, n2, n3);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

