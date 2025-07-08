/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BeaconBeamOwner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;

public class BeaconRenderer<T extends BlockEntity>
implements BlockEntityRenderer<T> {
    public static final ResourceLocation BEAM_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/beacon_beam.png");
    public static final int MAX_RENDER_Y = 2048;
    private static final float BEAM_SCALE_THRESHOLD = 96.0f;
    public static final float SOLID_BEAM_RADIUS = 0.2f;
    public static final float BEAM_GLOW_RADIUS = 0.25f;

    public BeaconRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(T t, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2, Vec3 vec3) {
        long l = ((BlockEntity)t).getLevel().getGameTime();
        float f2 = (float)vec3.subtract(((BlockEntity)t).getBlockPos().getCenter()).horizontalDistance();
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        float f3 = localPlayer != null && localPlayer.isScoping() ? 1.0f : Math.max(1.0f, f2 / 96.0f);
        List<BeaconBeamOwner.Section> list = ((BeaconBeamOwner)t).getBeamSections();
        int n3 = 0;
        for (int i = 0; i < list.size(); ++i) {
            BeaconBeamOwner.Section section = list.get(i);
            BeaconRenderer.renderBeaconBeam(poseStack, multiBufferSource, f, f3, l, n3, i == list.size() - 1 ? 2048 : section.getHeight(), section.getColor());
            n3 += section.getHeight();
        }
    }

    private static void renderBeaconBeam(PoseStack poseStack, MultiBufferSource multiBufferSource, float f, float f2, long l, int n, int n2, int n3) {
        BeaconRenderer.renderBeaconBeam(poseStack, multiBufferSource, BEAM_LOCATION, f, 1.0f, l, n, n2, n3, 0.2f * f2, 0.25f * f2);
    }

    public static void renderBeaconBeam(PoseStack poseStack, MultiBufferSource multiBufferSource, ResourceLocation resourceLocation, float f, float f2, long l, int n, int n2, int n3, float f3, float f4) {
        int n4 = n + n2;
        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);
        float f5 = (float)Math.floorMod(l, 40) + f;
        float f6 = n2 < 0 ? f5 : -f5;
        float f7 = Mth.frac(f6 * 0.2f - (float)Mth.floor(f6 * 0.1f));
        poseStack.pushPose();
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(f5 * 2.25f - 45.0f));
        float f8 = 0.0f;
        float f9 = f3;
        float f10 = f3;
        float f11 = 0.0f;
        float f12 = -f3;
        float f13 = 0.0f;
        float f14 = 0.0f;
        float f15 = -f3;
        float f16 = 0.0f;
        float f17 = 1.0f;
        float f18 = -1.0f + f7;
        float f19 = (float)n2 * f2 * (0.5f / f3) + f18;
        BeaconRenderer.renderPart(poseStack, multiBufferSource.getBuffer(RenderType.beaconBeam(resourceLocation, false)), n3, n, n4, 0.0f, f9, f10, 0.0f, f12, 0.0f, 0.0f, f15, 0.0f, 1.0f, f19, f18);
        poseStack.popPose();
        f8 = -f4;
        f9 = -f4;
        f10 = f4;
        f11 = -f4;
        f12 = -f4;
        f13 = f4;
        f14 = f4;
        f15 = f4;
        f16 = 0.0f;
        f17 = 1.0f;
        f18 = -1.0f + f7;
        f19 = (float)n2 * f2 + f18;
        BeaconRenderer.renderPart(poseStack, multiBufferSource.getBuffer(RenderType.beaconBeam(resourceLocation, true)), ARGB.color(32, n3), n, n4, f8, f9, f10, f11, f12, f13, f14, f15, 0.0f, 1.0f, f19, f18);
        poseStack.popPose();
    }

    private static void renderPart(PoseStack poseStack, VertexConsumer vertexConsumer, int n, int n2, int n3, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10, float f11, float f12) {
        PoseStack.Pose pose = poseStack.last();
        BeaconRenderer.renderQuad(pose, vertexConsumer, n, n2, n3, f, f2, f3, f4, f9, f10, f11, f12);
        BeaconRenderer.renderQuad(pose, vertexConsumer, n, n2, n3, f7, f8, f5, f6, f9, f10, f11, f12);
        BeaconRenderer.renderQuad(pose, vertexConsumer, n, n2, n3, f3, f4, f7, f8, f9, f10, f11, f12);
        BeaconRenderer.renderQuad(pose, vertexConsumer, n, n2, n3, f5, f6, f, f2, f9, f10, f11, f12);
    }

    private static void renderQuad(PoseStack.Pose pose, VertexConsumer vertexConsumer, int n, int n2, int n3, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8) {
        BeaconRenderer.addVertex(pose, vertexConsumer, n, n3, f, f2, f6, f7);
        BeaconRenderer.addVertex(pose, vertexConsumer, n, n2, f, f2, f6, f8);
        BeaconRenderer.addVertex(pose, vertexConsumer, n, n2, f3, f4, f5, f8);
        BeaconRenderer.addVertex(pose, vertexConsumer, n, n3, f3, f4, f5, f7);
    }

    private static void addVertex(PoseStack.Pose pose, VertexConsumer vertexConsumer, int n, int n2, float f, float f2, float f3, float f4) {
        vertexConsumer.addVertex(pose, f, (float)n2, f2).setColor(n).setUv(f3, f4).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0.0f, 1.0f, 0.0f);
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return Minecraft.getInstance().options.getEffectiveRenderDistance() * 16;
    }

    @Override
    public boolean shouldRender(T t, Vec3 vec3) {
        return Vec3.atCenterOf(((BlockEntity)t).getBlockPos()).multiply(1.0, 0.0, 1.0).closerThan(vec3.multiply(1.0, 0.0, 1.0), this.getViewDistance());
    }
}

