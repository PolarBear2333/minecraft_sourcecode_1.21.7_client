/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix4f
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LightningBoltRenderState;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import org.joml.Matrix4f;

public class LightningBoltRenderer
extends EntityRenderer<LightningBolt, LightningBoltRenderState> {
    public LightningBoltRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(LightningBoltRenderState lightningBoltRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        float[] fArray = new float[8];
        float[] fArray2 = new float[8];
        float f = 0.0f;
        float f2 = 0.0f;
        Object object = RandomSource.create(lightningBoltRenderState.seed);
        for (int i = 7; i >= 0; --i) {
            fArray[i] = f;
            fArray2[i] = f2;
            f += (float)(object.nextInt(11) - 5);
            f2 += (float)(object.nextInt(11) - 5);
        }
        object = multiBufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix4f = poseStack.last().pose();
        for (int i = 0; i < 4; ++i) {
            RandomSource randomSource = RandomSource.create(lightningBoltRenderState.seed);
            for (int j = 0; j < 3; ++j) {
                int n2 = 7;
                int n3 = 0;
                if (j > 0) {
                    n2 = 7 - j;
                }
                if (j > 0) {
                    n3 = n2 - 2;
                }
                float f3 = fArray[n2] - f;
                float f4 = fArray2[n2] - f2;
                for (int k = n2; k >= n3; --k) {
                    float f5 = f3;
                    float f6 = f4;
                    if (j == 0) {
                        f3 += (float)(randomSource.nextInt(11) - 5);
                        f4 += (float)(randomSource.nextInt(11) - 5);
                    } else {
                        f3 += (float)(randomSource.nextInt(31) - 15);
                        f4 += (float)(randomSource.nextInt(31) - 15);
                    }
                    float f7 = 0.5f;
                    float f8 = 0.45f;
                    float f9 = 0.45f;
                    float f10 = 0.5f;
                    float f11 = 0.1f + (float)i * 0.2f;
                    if (j == 0) {
                        f11 *= (float)k * 0.1f + 1.0f;
                    }
                    float f12 = 0.1f + (float)i * 0.2f;
                    if (j == 0) {
                        f12 *= ((float)k - 1.0f) * 0.1f + 1.0f;
                    }
                    LightningBoltRenderer.quad(matrix4f, (VertexConsumer)object, f3, f4, k, f5, f6, 0.45f, 0.45f, 0.5f, f11, f12, false, false, true, false);
                    LightningBoltRenderer.quad(matrix4f, (VertexConsumer)object, f3, f4, k, f5, f6, 0.45f, 0.45f, 0.5f, f11, f12, true, false, true, true);
                    LightningBoltRenderer.quad(matrix4f, (VertexConsumer)object, f3, f4, k, f5, f6, 0.45f, 0.45f, 0.5f, f11, f12, true, true, false, true);
                    LightningBoltRenderer.quad(matrix4f, (VertexConsumer)object, f3, f4, k, f5, f6, 0.45f, 0.45f, 0.5f, f11, f12, false, true, false, false);
                }
            }
        }
    }

    private static void quad(Matrix4f matrix4f, VertexConsumer vertexConsumer, float f, float f2, int n, float f3, float f4, float f5, float f6, float f7, float f8, float f9, boolean bl, boolean bl2, boolean bl3, boolean bl4) {
        vertexConsumer.addVertex(matrix4f, f + (bl ? f9 : -f9), (float)(n * 16), f2 + (bl2 ? f9 : -f9)).setColor(f5, f6, f7, 0.3f);
        vertexConsumer.addVertex(matrix4f, f3 + (bl ? f8 : -f8), (float)((n + 1) * 16), f4 + (bl2 ? f8 : -f8)).setColor(f5, f6, f7, 0.3f);
        vertexConsumer.addVertex(matrix4f, f3 + (bl3 ? f8 : -f8), (float)((n + 1) * 16), f4 + (bl4 ? f8 : -f8)).setColor(f5, f6, f7, 0.3f);
        vertexConsumer.addVertex(matrix4f, f + (bl3 ? f9 : -f9), (float)(n * 16), f2 + (bl4 ? f9 : -f9)).setColor(f5, f6, f7, 0.3f);
    }

    @Override
    public LightningBoltRenderState createRenderState() {
        return new LightningBoltRenderState();
    }

    @Override
    public void extractRenderState(LightningBolt lightningBolt, LightningBoltRenderState lightningBoltRenderState, float f) {
        super.extractRenderState(lightningBolt, lightningBoltRenderState, f);
        lightningBoltRenderState.seed = lightningBolt.seed;
    }

    @Override
    protected boolean affectedByCulling(LightningBolt lightningBolt) {
        return false;
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }

    @Override
    protected /* synthetic */ boolean affectedByCulling(Entity entity) {
        return this.affectedByCulling((LightningBolt)entity);
    }
}

