/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.OptionalInt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.dimension.DimensionType;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class LightTexture
implements AutoCloseable {
    public static final int FULL_BRIGHT = 0xF000F0;
    public static final int FULL_SKY = 0xF00000;
    public static final int FULL_BLOCK = 240;
    private static final int TEXTURE_SIZE = 16;
    private static final int LIGHTMAP_UBO_SIZE = new Std140SizeCalculator().putFloat().putFloat().putFloat().putInt().putFloat().putFloat().putFloat().putFloat().putVec3().get();
    private final GpuTexture texture;
    private final GpuTextureView textureView;
    private boolean updateLightTexture;
    private float blockLightRedFlicker;
    private final GameRenderer renderer;
    private final Minecraft minecraft;
    private final MappableRingBuffer ubo;

    public LightTexture(GameRenderer gameRenderer, Minecraft minecraft) {
        this.renderer = gameRenderer;
        this.minecraft = minecraft;
        GpuDevice gpuDevice = RenderSystem.getDevice();
        this.texture = gpuDevice.createTexture("Light Texture", 12, TextureFormat.RGBA8, 16, 16, 1, 1);
        this.texture.setTextureFilter(FilterMode.LINEAR, false);
        this.textureView = gpuDevice.createTextureView(this.texture);
        gpuDevice.createCommandEncoder().clearColorTexture(this.texture, -1);
        this.ubo = new MappableRingBuffer(() -> "Lightmap UBO", 130, LIGHTMAP_UBO_SIZE);
    }

    public GpuTextureView getTextureView() {
        return this.textureView;
    }

    @Override
    public void close() {
        this.texture.close();
        this.textureView.close();
        this.ubo.close();
    }

    public void tick() {
        this.blockLightRedFlicker += (float)((Math.random() - Math.random()) * Math.random() * Math.random() * 0.1);
        this.blockLightRedFlicker *= 0.9f;
        this.updateLightTexture = true;
    }

    public void turnOffLightLayer() {
        RenderSystem.setShaderTexture(2, null);
    }

    public void turnOnLightLayer() {
        RenderSystem.setShaderTexture(2, this.textureView);
    }

    private float calculateDarknessScale(LivingEntity livingEntity, float f, float f2) {
        float f3 = 0.45f * f;
        return Math.max(0.0f, Mth.cos(((float)livingEntity.tickCount - f2) * (float)Math.PI * 0.025f) * f3);
    }

    public void updateLightTexture(float f) {
        if (!this.updateLightTexture) {
            return;
        }
        this.updateLightTexture = false;
        ProfilerFiller profilerFiller = Profiler.get();
        profilerFiller.push("lightTex");
        ClientLevel clientLevel = this.minecraft.level;
        if (clientLevel == null) {
            return;
        }
        float f2 = clientLevel.getSkyDarken(1.0f);
        float f3 = clientLevel.getSkyFlashTime() > 0 ? 1.0f : f2 * 0.95f + 0.05f;
        float f4 = this.minecraft.options.darknessEffectScale().get().floatValue();
        float f5 = this.minecraft.player.getEffectBlendFactor(MobEffects.DARKNESS, f) * f4;
        float f6 = this.calculateDarknessScale(this.minecraft.player, f5, f) * f4;
        float f7 = this.minecraft.player.getWaterVision();
        float f8 = this.minecraft.player.hasEffect(MobEffects.NIGHT_VISION) ? GameRenderer.getNightVisionScale(this.minecraft.player, f) : (f7 > 0.0f && this.minecraft.player.hasEffect(MobEffects.CONDUIT_POWER) ? f7 : 0.0f);
        Vector3f vector3f = new Vector3f(f2, f2, 1.0f).lerp((Vector3fc)new Vector3f(1.0f, 1.0f, 1.0f), 0.35f);
        float f9 = this.blockLightRedFlicker + 1.5f;
        float f10 = clientLevel.dimensionType().ambientLight();
        boolean bl = clientLevel.effects().forceBrightLightmap();
        float f11 = this.minecraft.options.gamma().get().floatValue();
        RenderSystem.AutoStorageIndexBuffer autoStorageIndexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        GpuBuffer gpuBuffer = autoStorageIndexBuffer.getBuffer(6);
        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
        try (AutoCloseable autoCloseable = commandEncoder.mapBuffer(this.ubo.currentBuffer(), false, true);){
            Std140Builder.intoBuffer(autoCloseable.data()).putFloat(f10).putFloat(f3).putFloat(f9).putInt(bl ? 1 : 0).putFloat(f8).putFloat(f6).putFloat(this.renderer.getDarkenWorldAmount(f)).putFloat(Math.max(0.0f, f11 - f5)).putVec3((Vector3fc)vector3f);
        }
        autoCloseable = commandEncoder.createRenderPass(() -> "Update light", this.textureView, OptionalInt.empty());
        try {
            autoCloseable.setPipeline(RenderPipelines.LIGHTMAP);
            RenderSystem.bindDefaultUniforms((RenderPass)autoCloseable);
            autoCloseable.setUniform("LightmapInfo", this.ubo.currentBuffer());
            autoCloseable.setVertexBuffer(0, RenderSystem.getQuadVertexBuffer());
            autoCloseable.setIndexBuffer(gpuBuffer, autoStorageIndexBuffer.type());
            autoCloseable.drawIndexed(0, 0, 6, 1);
        }
        finally {
            if (autoCloseable != null) {
                autoCloseable.close();
            }
        }
        this.ubo.rotate();
        profilerFiller.pop();
    }

    public static float getBrightness(DimensionType dimensionType, int n) {
        return LightTexture.getBrightness(dimensionType.ambientLight(), n);
    }

    public static float getBrightness(float f, int n) {
        float f2 = (float)n / 15.0f;
        float f3 = f2 / (4.0f - 3.0f * f2);
        return Mth.lerp(f, f3, 1.0f);
    }

    public static int pack(int n, int n2) {
        return n << 4 | n2 << 20;
    }

    public static int block(int n) {
        return n >>> 4 & 0xF;
    }

    public static int sky(int n) {
        return n >>> 20 & 0xF;
    }

    public static int lightCoordsWithEmission(int n, int n2) {
        if (n2 == 0) {
            return n;
        }
        int n3 = Math.max(LightTexture.sky(n), n2);
        int n4 = Math.max(LightTexture.block(n), n2);
        return LightTexture.pack(n4, n3);
    }
}

