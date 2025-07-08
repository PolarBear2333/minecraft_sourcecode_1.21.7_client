/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix4f
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class TheEndPortalRenderer<T extends TheEndPortalBlockEntity>
implements BlockEntityRenderer<T> {
    public static final ResourceLocation END_SKY_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/end_sky.png");
    public static final ResourceLocation END_PORTAL_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/end_portal.png");

    public TheEndPortalRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(T t, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2, Vec3 vec3) {
        Matrix4f matrix4f = poseStack.last().pose();
        this.renderCube(t, matrix4f, multiBufferSource.getBuffer(this.renderType()));
    }

    private void renderCube(T t, Matrix4f matrix4f, VertexConsumer vertexConsumer) {
        float f = this.getOffsetDown();
        float f2 = this.getOffsetUp();
        this.renderFace(t, matrix4f, vertexConsumer, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, Direction.SOUTH);
        this.renderFace(t, matrix4f, vertexConsumer, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, Direction.NORTH);
        this.renderFace(t, matrix4f, vertexConsumer, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, Direction.EAST);
        this.renderFace(t, matrix4f, vertexConsumer, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, Direction.WEST);
        this.renderFace(t, matrix4f, vertexConsumer, 0.0f, 1.0f, f, f, 0.0f, 0.0f, 1.0f, 1.0f, Direction.DOWN);
        this.renderFace(t, matrix4f, vertexConsumer, 0.0f, 1.0f, f2, f2, 1.0f, 1.0f, 0.0f, 0.0f, Direction.UP);
    }

    private void renderFace(T t, Matrix4f matrix4f, VertexConsumer vertexConsumer, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, Direction direction) {
        if (((TheEndPortalBlockEntity)t).shouldRenderFace(direction)) {
            vertexConsumer.addVertex(matrix4f, f, f3, f5);
            vertexConsumer.addVertex(matrix4f, f2, f3, f6);
            vertexConsumer.addVertex(matrix4f, f2, f4, f7);
            vertexConsumer.addVertex(matrix4f, f, f4, f8);
        }
    }

    protected float getOffsetUp() {
        return 0.75f;
    }

    protected float getOffsetDown() {
        return 0.375f;
    }

    protected RenderType renderType() {
        return RenderType.endPortal();
    }
}

