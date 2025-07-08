/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.DragonFireball;
import org.joml.Quaternionfc;

public class DragonFireballRenderer
extends EntityRenderer<DragonFireball, EntityRenderState> {
    private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/enderdragon/dragon_fireball.png");
    private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(TEXTURE_LOCATION);

    public DragonFireballRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected int getBlockLightLevel(DragonFireball dragonFireball, BlockPos blockPos) {
        return 15;
    }

    @Override
    public void render(EntityRenderState entityRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        poseStack.pushPose();
        poseStack.scale(2.0f, 2.0f, 2.0f);
        poseStack.mulPose((Quaternionfc)this.entityRenderDispatcher.cameraOrientation());
        PoseStack.Pose pose = poseStack.last();
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RENDER_TYPE);
        DragonFireballRenderer.vertex(vertexConsumer, pose, n, 0.0f, 0, 0, 1);
        DragonFireballRenderer.vertex(vertexConsumer, pose, n, 1.0f, 0, 1, 1);
        DragonFireballRenderer.vertex(vertexConsumer, pose, n, 1.0f, 1, 1, 0);
        DragonFireballRenderer.vertex(vertexConsumer, pose, n, 0.0f, 1, 0, 0);
        poseStack.popPose();
        super.render(entityRenderState, poseStack, multiBufferSource, n);
    }

    private static void vertex(VertexConsumer vertexConsumer, PoseStack.Pose pose, int n, float f, int n2, int n3, int n4) {
        vertexConsumer.addVertex(pose, f - 0.5f, (float)n2 - 0.25f, 0.0f).setColor(-1).setUv(n3, n4).setOverlay(OverlayTexture.NO_OVERLAY).setLight(n).setNormal(pose, 0.0f, 1.0f, 0.0f);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}

