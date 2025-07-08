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
import java.util.Objects;
import net.minecraft.client.model.MinecartModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.MinecartRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartBehavior;
import net.minecraft.world.entity.vehicle.NewMinecartBehavior;
import net.minecraft.world.entity.vehicle.OldMinecartBehavior;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;

public abstract class AbstractMinecartRenderer<T extends AbstractMinecart, S extends MinecartRenderState>
extends EntityRenderer<T, S> {
    private static final ResourceLocation MINECART_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/minecart.png");
    private static final float DISPLAY_BLOCK_SCALE = 0.75f;
    protected final MinecartModel model;
    private final BlockRenderDispatcher blockRenderer;

    public AbstractMinecartRenderer(EntityRendererProvider.Context context, ModelLayerLocation modelLayerLocation) {
        super(context);
        this.shadowRadius = 0.7f;
        this.model = new MinecartModel(context.bakeLayer(modelLayerLocation));
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(S s, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        BlockState blockState;
        super.render(s, poseStack, multiBufferSource, n);
        poseStack.pushPose();
        long l = ((MinecartRenderState)s).offsetSeed;
        float f = (((float)(l >> 16 & 7L) + 0.5f) / 8.0f - 0.5f) * 0.004f;
        float f2 = (((float)(l >> 20 & 7L) + 0.5f) / 8.0f - 0.5f) * 0.004f;
        float f3 = (((float)(l >> 24 & 7L) + 0.5f) / 8.0f - 0.5f) * 0.004f;
        poseStack.translate(f, f2, f3);
        if (((MinecartRenderState)s).isNewRender) {
            AbstractMinecartRenderer.newRender(s, poseStack);
        } else {
            AbstractMinecartRenderer.oldRender(s, poseStack);
        }
        float f4 = ((MinecartRenderState)s).hurtTime;
        if (f4 > 0.0f) {
            poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(Mth.sin(f4) * f4 * ((MinecartRenderState)s).damageTime / 10.0f * (float)((MinecartRenderState)s).hurtDir));
        }
        if ((blockState = ((MinecartRenderState)s).displayBlockState).getRenderShape() != RenderShape.INVISIBLE) {
            poseStack.pushPose();
            poseStack.scale(0.75f, 0.75f, 0.75f);
            poseStack.translate(-0.5f, (float)(((MinecartRenderState)s).displayOffset - 8) / 16.0f, 0.5f);
            poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(90.0f));
            this.renderMinecartContents(s, blockState, poseStack, multiBufferSource, n);
            poseStack.popPose();
        }
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        this.model.setupAnim(s);
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(this.model.renderType(MINECART_LOCATION));
        this.model.renderToBuffer(poseStack, vertexConsumer, n, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private static <S extends MinecartRenderState> void newRender(S s, PoseStack poseStack) {
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(s.yRot));
        poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(-s.xRot));
        poseStack.translate(0.0f, 0.375f, 0.0f);
    }

    private static <S extends MinecartRenderState> void oldRender(S s, PoseStack poseStack) {
        double d = s.x;
        double d2 = s.y;
        double d3 = s.z;
        float f = s.xRot;
        float f2 = s.yRot;
        if (s.posOnRail != null && s.frontPos != null && s.backPos != null) {
            Vec3 vec3 = s.frontPos;
            Vec3 vec32 = s.backPos;
            poseStack.translate(s.posOnRail.x - d, (vec3.y + vec32.y) / 2.0 - d2, s.posOnRail.z - d3);
            Vec3 vec33 = vec32.add(-vec3.x, -vec3.y, -vec3.z);
            if (vec33.length() != 0.0) {
                vec33 = vec33.normalize();
                f2 = (float)(Math.atan2(vec33.z, vec33.x) * 180.0 / Math.PI);
                f = (float)(Math.atan(vec33.y) * 73.0);
            }
        }
        poseStack.translate(0.0f, 0.375f, 0.0f);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(180.0f - f2));
        poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(-f));
    }

    @Override
    public void extractRenderState(T t, S s, float f) {
        super.extractRenderState(t, s, f);
        MinecartBehavior minecartBehavior = ((AbstractMinecart)t).getBehavior();
        if (minecartBehavior instanceof NewMinecartBehavior) {
            NewMinecartBehavior newMinecartBehavior = (NewMinecartBehavior)minecartBehavior;
            AbstractMinecartRenderer.newExtractState(t, newMinecartBehavior, s, f);
            ((MinecartRenderState)s).isNewRender = true;
        } else {
            minecartBehavior = ((AbstractMinecart)t).getBehavior();
            if (minecartBehavior instanceof OldMinecartBehavior) {
                OldMinecartBehavior oldMinecartBehavior = (OldMinecartBehavior)minecartBehavior;
                AbstractMinecartRenderer.oldExtractState(t, oldMinecartBehavior, s, f);
                ((MinecartRenderState)s).isNewRender = false;
            }
        }
        long l = (long)((Entity)t).getId() * 493286711L;
        ((MinecartRenderState)s).offsetSeed = l * l * 4392167121L + l * 98761L;
        ((MinecartRenderState)s).hurtTime = (float)((VehicleEntity)t).getHurtTime() - f;
        ((MinecartRenderState)s).hurtDir = ((VehicleEntity)t).getHurtDir();
        ((MinecartRenderState)s).damageTime = Math.max(((VehicleEntity)t).getDamage() - f, 0.0f);
        ((MinecartRenderState)s).displayOffset = ((AbstractMinecart)t).getDisplayOffset();
        ((MinecartRenderState)s).displayBlockState = ((AbstractMinecart)t).getDisplayBlockState();
    }

    private static <T extends AbstractMinecart, S extends MinecartRenderState> void newExtractState(T t, NewMinecartBehavior newMinecartBehavior, S s, float f) {
        if (newMinecartBehavior.cartHasPosRotLerp()) {
            s.renderPos = newMinecartBehavior.getCartLerpPosition(f);
            s.xRot = newMinecartBehavior.getCartLerpXRot(f);
            s.yRot = newMinecartBehavior.getCartLerpYRot(f);
        } else {
            s.renderPos = null;
            s.xRot = t.getXRot();
            s.yRot = t.getYRot();
        }
    }

    private static <T extends AbstractMinecart, S extends MinecartRenderState> void oldExtractState(T t, OldMinecartBehavior oldMinecartBehavior, S s, float f) {
        float f2 = 0.3f;
        s.xRot = t.getXRot(f);
        s.yRot = t.getYRot(f);
        double d = s.x;
        double d2 = s.y;
        double d3 = s.z;
        Vec3 vec3 = oldMinecartBehavior.getPos(d, d2, d3);
        if (vec3 != null) {
            s.posOnRail = vec3;
            Vec3 vec32 = oldMinecartBehavior.getPosOffs(d, d2, d3, 0.3f);
            Vec3 vec33 = oldMinecartBehavior.getPosOffs(d, d2, d3, -0.3f);
            s.frontPos = Objects.requireNonNullElse(vec32, vec3);
            s.backPos = Objects.requireNonNullElse(vec33, vec3);
        } else {
            s.posOnRail = null;
            s.frontPos = null;
            s.backPos = null;
        }
    }

    protected void renderMinecartContents(S s, BlockState blockState, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        this.blockRenderer.renderSingleBlock(blockState, poseStack, multiBufferSource, n, OverlayTexture.NO_OVERLAY);
    }

    @Override
    protected AABB getBoundingBoxForCulling(T t) {
        AABB aABB = super.getBoundingBoxForCulling(t);
        if (!((AbstractMinecart)t).getDisplayBlockState().isAir()) {
            return aABB.expandTowards(0.0, (float)((AbstractMinecart)t).getDisplayOffset() * 0.75f / 16.0f, 0.0);
        }
        return aABB;
    }

    @Override
    public Vec3 getRenderOffset(S s) {
        Vec3 vec3 = super.getRenderOffset(s);
        if (((MinecartRenderState)s).isNewRender && ((MinecartRenderState)s).renderPos != null) {
            return vec3.add(((MinecartRenderState)s).renderPos.x - ((MinecartRenderState)s).x, ((MinecartRenderState)s).renderPos.y - ((MinecartRenderState)s).y, ((MinecartRenderState)s).renderPos.z - ((MinecartRenderState)s).z);
        }
        return vec3;
    }
}

