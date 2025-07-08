/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.GuardianModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ElderGuardianRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.joml.Quaternionfc;

public class MobAppearanceParticle
extends Particle {
    private final Model model;
    private final RenderType renderType = RenderType.entityTranslucent(ElderGuardianRenderer.GUARDIAN_ELDER_LOCATION);

    MobAppearanceParticle(ClientLevel clientLevel, double d, double d2, double d3) {
        super(clientLevel, d, d2, d3);
        this.model = new GuardianModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.ELDER_GUARDIAN));
        this.gravity = 0.0f;
        this.lifetime = 30;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    @Override
    public void renderCustom(PoseStack poseStack, MultiBufferSource multiBufferSource, Camera camera, float f) {
        float f2 = ((float)this.age + f) / (float)this.lifetime;
        float f3 = 0.05f + 0.5f * Mth.sin(f2 * (float)Math.PI);
        int n = ARGB.colorFromFloat(f3, 1.0f, 1.0f, 1.0f);
        poseStack.pushPose();
        poseStack.mulPose((Quaternionfc)camera.rotation());
        poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(60.0f - 150.0f * f2));
        float f4 = 0.42553192f;
        poseStack.scale(0.42553192f, -0.42553192f, -0.42553192f);
        poseStack.translate(0.0f, -0.56f, 3.5f);
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(this.renderType);
        this.model.renderToBuffer(poseStack, vertexConsumer, 0xF000F0, OverlayTexture.NO_OVERLAY, n);
        poseStack.popPose();
    }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float f) {
    }

    public static class Provider
    implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            return new MobAppearanceParticle(clientLevel, d, d2, d3);
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            return this.createParticle((SimpleParticleType)particleOptions, clientLevel, d, d2, d3, d4, d5, d6);
        }
    }
}

