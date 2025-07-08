/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList$Builder
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 */
package net.minecraft.client.renderer.entity;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.dragon.EnderDragonModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EndCrystalRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EnderDragonRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.HitboxRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

public class EnderDragonRenderer
extends EntityRenderer<EnderDragon, EnderDragonRenderState> {
    public static final ResourceLocation CRYSTAL_BEAM_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/end_crystal/end_crystal_beam.png");
    private static final ResourceLocation DRAGON_EXPLODING_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/enderdragon/dragon_exploding.png");
    private static final ResourceLocation DRAGON_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/enderdragon/dragon.png");
    private static final ResourceLocation DRAGON_EYES_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/enderdragon/dragon_eyes.png");
    private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(DRAGON_LOCATION);
    private static final RenderType DECAL = RenderType.entityDecal(DRAGON_LOCATION);
    private static final RenderType EYES = RenderType.eyes(DRAGON_EYES_LOCATION);
    private static final RenderType BEAM = RenderType.entitySmoothCutout(CRYSTAL_BEAM_LOCATION);
    private static final float HALF_SQRT_3 = (float)(Math.sqrt(3.0) / 2.0);
    private final EnderDragonModel model;

    public EnderDragonRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5f;
        this.model = new EnderDragonModel(context.bakeLayer(ModelLayers.ENDER_DRAGON));
    }

    @Override
    public void render(EnderDragonRenderState enderDragonRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        poseStack.pushPose();
        float f = enderDragonRenderState.getHistoricalPos(7).yRot();
        float f2 = (float)(enderDragonRenderState.getHistoricalPos(5).y() - enderDragonRenderState.getHistoricalPos(10).y());
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(-f));
        poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(f2 * 10.0f));
        poseStack.translate(0.0f, 0.0f, 1.0f);
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        poseStack.translate(0.0f, -1.501f, 0.0f);
        this.model.setupAnim(enderDragonRenderState);
        if (enderDragonRenderState.deathTime > 0.0f) {
            float f3 = enderDragonRenderState.deathTime / 200.0f;
            int n2 = ARGB.color(Mth.floor(f3 * 255.0f), -1);
            VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.dragonExplosionAlpha(DRAGON_EXPLODING_LOCATION));
            this.model.renderToBuffer(poseStack, vertexConsumer, n, OverlayTexture.NO_OVERLAY, n2);
            VertexConsumer vertexConsumer2 = multiBufferSource.getBuffer(DECAL);
            this.model.renderToBuffer(poseStack, vertexConsumer2, n, OverlayTexture.pack(0.0f, enderDragonRenderState.hasRedOverlay));
        } else {
            VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RENDER_TYPE);
            this.model.renderToBuffer(poseStack, vertexConsumer, n, OverlayTexture.pack(0.0f, enderDragonRenderState.hasRedOverlay));
        }
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(EYES);
        this.model.renderToBuffer(poseStack, vertexConsumer, n, OverlayTexture.NO_OVERLAY);
        if (enderDragonRenderState.deathTime > 0.0f) {
            float f4 = enderDragonRenderState.deathTime / 200.0f;
            poseStack.pushPose();
            poseStack.translate(0.0f, -1.0f, -2.0f);
            EnderDragonRenderer.renderRays(poseStack, f4, multiBufferSource.getBuffer(RenderType.dragonRays()));
            EnderDragonRenderer.renderRays(poseStack, f4, multiBufferSource.getBuffer(RenderType.dragonRaysDepth()));
            poseStack.popPose();
        }
        poseStack.popPose();
        if (enderDragonRenderState.beamOffset != null) {
            EnderDragonRenderer.renderCrystalBeams((float)enderDragonRenderState.beamOffset.x, (float)enderDragonRenderState.beamOffset.y, (float)enderDragonRenderState.beamOffset.z, enderDragonRenderState.ageInTicks, poseStack, multiBufferSource, n);
        }
        super.render(enderDragonRenderState, poseStack, multiBufferSource, n);
    }

    private static void renderRays(PoseStack poseStack, float f, VertexConsumer vertexConsumer) {
        poseStack.pushPose();
        float f2 = Math.min(f > 0.8f ? (f - 0.8f) / 0.2f : 0.0f, 1.0f);
        int n = ARGB.colorFromFloat(1.0f - f2, 1.0f, 1.0f, 1.0f);
        int n2 = 0xFF00FF;
        RandomSource randomSource = RandomSource.create(432L);
        Vector3f vector3f = new Vector3f();
        Vector3f vector3f2 = new Vector3f();
        Vector3f vector3f3 = new Vector3f();
        Vector3f vector3f4 = new Vector3f();
        Quaternionf quaternionf = new Quaternionf();
        int n3 = Mth.floor((f + f * f) / 2.0f * 60.0f);
        for (int i = 0; i < n3; ++i) {
            quaternionf.rotationXYZ(randomSource.nextFloat() * ((float)Math.PI * 2), randomSource.nextFloat() * ((float)Math.PI * 2), randomSource.nextFloat() * ((float)Math.PI * 2)).rotateXYZ(randomSource.nextFloat() * ((float)Math.PI * 2), randomSource.nextFloat() * ((float)Math.PI * 2), randomSource.nextFloat() * ((float)Math.PI * 2) + f * 1.5707964f);
            poseStack.mulPose((Quaternionfc)quaternionf);
            float f3 = randomSource.nextFloat() * 20.0f + 5.0f + f2 * 10.0f;
            float f4 = randomSource.nextFloat() * 2.0f + 1.0f + f2 * 2.0f;
            vector3f2.set(-HALF_SQRT_3 * f4, f3, -0.5f * f4);
            vector3f3.set(HALF_SQRT_3 * f4, f3, -0.5f * f4);
            vector3f4.set(0.0f, f3, f4);
            PoseStack.Pose pose = poseStack.last();
            vertexConsumer.addVertex(pose, vector3f).setColor(n);
            vertexConsumer.addVertex(pose, vector3f2).setColor(0xFF00FF);
            vertexConsumer.addVertex(pose, vector3f3).setColor(0xFF00FF);
            vertexConsumer.addVertex(pose, vector3f).setColor(n);
            vertexConsumer.addVertex(pose, vector3f3).setColor(0xFF00FF);
            vertexConsumer.addVertex(pose, vector3f4).setColor(0xFF00FF);
            vertexConsumer.addVertex(pose, vector3f).setColor(n);
            vertexConsumer.addVertex(pose, vector3f4).setColor(0xFF00FF);
            vertexConsumer.addVertex(pose, vector3f2).setColor(0xFF00FF);
        }
        poseStack.popPose();
    }

    public static void renderCrystalBeams(float f, float f2, float f3, float f4, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        float f5 = Mth.sqrt(f * f + f3 * f3);
        float f6 = Mth.sqrt(f * f + f2 * f2 + f3 * f3);
        poseStack.pushPose();
        poseStack.translate(0.0f, 2.0f, 0.0f);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotation((float)(-Math.atan2(f3, f)) - 1.5707964f));
        poseStack.mulPose((Quaternionfc)Axis.XP.rotation((float)(-Math.atan2(f5, f2)) - 1.5707964f));
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(BEAM);
        float f7 = 0.0f - f4 * 0.01f;
        float f8 = f6 / 32.0f - f4 * 0.01f;
        int n2 = 8;
        float f9 = 0.0f;
        float f10 = 0.75f;
        float f11 = 0.0f;
        PoseStack.Pose pose = poseStack.last();
        for (int i = 1; i <= 8; ++i) {
            float f12 = Mth.sin((float)i * ((float)Math.PI * 2) / 8.0f) * 0.75f;
            float f13 = Mth.cos((float)i * ((float)Math.PI * 2) / 8.0f) * 0.75f;
            float f14 = (float)i / 8.0f;
            vertexConsumer.addVertex(pose, f9 * 0.2f, f10 * 0.2f, 0.0f).setColor(-16777216).setUv(f11, f7).setOverlay(OverlayTexture.NO_OVERLAY).setLight(n).setNormal(pose, 0.0f, -1.0f, 0.0f);
            vertexConsumer.addVertex(pose, f9, f10, f6).setColor(-1).setUv(f11, f8).setOverlay(OverlayTexture.NO_OVERLAY).setLight(n).setNormal(pose, 0.0f, -1.0f, 0.0f);
            vertexConsumer.addVertex(pose, f12, f13, f6).setColor(-1).setUv(f14, f8).setOverlay(OverlayTexture.NO_OVERLAY).setLight(n).setNormal(pose, 0.0f, -1.0f, 0.0f);
            vertexConsumer.addVertex(pose, f12 * 0.2f, f13 * 0.2f, 0.0f).setColor(-16777216).setUv(f14, f7).setOverlay(OverlayTexture.NO_OVERLAY).setLight(n).setNormal(pose, 0.0f, -1.0f, 0.0f);
            f9 = f12;
            f10 = f13;
            f11 = f14;
        }
        poseStack.popPose();
    }

    @Override
    public EnderDragonRenderState createRenderState() {
        return new EnderDragonRenderState();
    }

    @Override
    public void extractRenderState(EnderDragon enderDragon, EnderDragonRenderState enderDragonRenderState, float f) {
        Object object;
        super.extractRenderState(enderDragon, enderDragonRenderState, f);
        enderDragonRenderState.flapTime = Mth.lerp(f, enderDragon.oFlapTime, enderDragon.flapTime);
        enderDragonRenderState.deathTime = enderDragon.dragonDeathTime > 0 ? (float)enderDragon.dragonDeathTime + f : 0.0f;
        enderDragonRenderState.hasRedOverlay = enderDragon.hurtTime > 0;
        EndCrystal endCrystal = enderDragon.nearestCrystal;
        if (endCrystal != null) {
            object = endCrystal.getPosition(f).add(0.0, EndCrystalRenderer.getY((float)endCrystal.time + f), 0.0);
            enderDragonRenderState.beamOffset = ((Vec3)object).subtract(enderDragon.getPosition(f));
        } else {
            enderDragonRenderState.beamOffset = null;
        }
        object = enderDragon.getPhaseManager().getCurrentPhase();
        enderDragonRenderState.isLandingOrTakingOff = object == EnderDragonPhase.LANDING || object == EnderDragonPhase.TAKEOFF;
        enderDragonRenderState.isSitting = object.isSitting();
        BlockPos blockPos = enderDragon.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.getLocation(enderDragon.getFightOrigin()));
        enderDragonRenderState.distanceToEgg = blockPos.distToCenterSqr(enderDragon.position());
        enderDragonRenderState.partialTicks = enderDragon.isDeadOrDying() ? 0.0f : f;
        enderDragonRenderState.flightHistory.copyFrom(enderDragon.flightHistory);
    }

    @Override
    protected void extractAdditionalHitboxes(EnderDragon enderDragon, ImmutableList.Builder<HitboxRenderState> builder, float f) {
        super.extractAdditionalHitboxes(enderDragon, builder, f);
        double d = -Mth.lerp((double)f, enderDragon.xOld, enderDragon.getX());
        double d2 = -Mth.lerp((double)f, enderDragon.yOld, enderDragon.getY());
        double d3 = -Mth.lerp((double)f, enderDragon.zOld, enderDragon.getZ());
        for (EnderDragonPart enderDragonPart : enderDragon.getSubEntities()) {
            AABB aABB = enderDragonPart.getBoundingBox();
            HitboxRenderState hitboxRenderState = new HitboxRenderState(aABB.minX - enderDragonPart.getX(), aABB.minY - enderDragonPart.getY(), aABB.minZ - enderDragonPart.getZ(), aABB.maxX - enderDragonPart.getX(), aABB.maxY - enderDragonPart.getY(), aABB.maxZ - enderDragonPart.getZ(), (float)(d + Mth.lerp((double)f, enderDragonPart.xOld, enderDragonPart.getX())), (float)(d2 + Mth.lerp((double)f, enderDragonPart.yOld, enderDragonPart.getY())), (float)(d3 + Mth.lerp((double)f, enderDragonPart.zOld, enderDragonPart.getZ())), 0.25f, 1.0f, 0.0f);
            builder.add((Object)hitboxRenderState);
        }
    }

    @Override
    protected boolean affectedByCulling(EnderDragon enderDragon) {
        return false;
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }

    @Override
    protected /* synthetic */ boolean affectedByCulling(Entity entity) {
        return this.affectedByCulling((EnderDragon)entity);
    }
}

