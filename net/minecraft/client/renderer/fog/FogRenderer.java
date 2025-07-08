/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  org.joml.Vector4f
 *  org.joml.Vector4fc
 *  org.lwjgl.system.MemoryStack
 */
package net.minecraft.client.renderer.fog;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import net.minecraft.client.renderer.fog.environment.BlindnessFogEnvironment;
import net.minecraft.client.renderer.fog.environment.DarknessFogEnvironment;
import net.minecraft.client.renderer.fog.environment.DimensionOrBossFogEnvironment;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.client.renderer.fog.environment.LavaFogEnvironment;
import net.minecraft.client.renderer.fog.environment.PowderedSnowFogEnvironment;
import net.minecraft.client.renderer.fog.environment.WaterFogEnvironment;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryStack;

public class FogRenderer
implements AutoCloseable {
    public static final int FOG_UBO_SIZE = new Std140SizeCalculator().putVec4().putFloat().putFloat().putFloat().putFloat().putFloat().putFloat().get();
    private static final List<FogEnvironment> FOG_ENVIRONMENTS = Lists.newArrayList((Object[])new FogEnvironment[]{new LavaFogEnvironment(), new PowderedSnowFogEnvironment(), new BlindnessFogEnvironment(), new DarknessFogEnvironment(), new WaterFogEnvironment(), new DimensionOrBossFogEnvironment(), new AtmosphericFogEnvironment()});
    private static boolean fogEnabled = true;
    private final GpuBuffer emptyBuffer;
    private final MappableRingBuffer regularBuffer;

    public FogRenderer() {
        GpuDevice gpuDevice = RenderSystem.getDevice();
        this.regularBuffer = new MappableRingBuffer(() -> "Fog UBO", 130, FOG_UBO_SIZE);
        try (MemoryStack memoryStack = MemoryStack.stackPush();){
            ByteBuffer byteBuffer = memoryStack.malloc(FOG_UBO_SIZE);
            this.updateBuffer(byteBuffer, 0, new Vector4f(0.0f), Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
            this.emptyBuffer = gpuDevice.createBuffer(() -> "Empty fog", 128, byteBuffer.flip());
        }
        RenderSystem.setShaderFog(this.getBuffer(FogMode.NONE));
    }

    @Override
    public void close() {
        this.emptyBuffer.close();
        this.regularBuffer.close();
    }

    public void endFrame() {
        this.regularBuffer.rotate();
    }

    public GpuBufferSlice getBuffer(FogMode fogMode) {
        if (!fogEnabled) {
            return this.emptyBuffer.slice(0, FOG_UBO_SIZE);
        }
        return switch (fogMode.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> this.emptyBuffer.slice(0, FOG_UBO_SIZE);
            case 1 -> this.regularBuffer.currentBuffer().slice(0, FOG_UBO_SIZE);
        };
    }

    private Vector4f computeFogColor(Camera camera, float f, ClientLevel clientLevel, int n, float f2, boolean bl) {
        LivingEntity livingEntity;
        float f3;
        FogType fogType = this.getFogType(camera, bl);
        Entity entity = camera.getEntity();
        FogEnvironment fogEnvironment = null;
        FogEnvironment fogEnvironment2 = null;
        for (FogEnvironment fogEnvironment3 : FOG_ENVIRONMENTS) {
            if (fogEnvironment3.isApplicable(fogType, entity)) {
                if (fogEnvironment == null && fogEnvironment3.providesColor()) {
                    fogEnvironment = fogEnvironment3;
                }
                if (fogEnvironment2 != null || !fogEnvironment3.modifiesDarkness()) continue;
                fogEnvironment2 = fogEnvironment3;
                continue;
            }
            fogEnvironment3.onNotApplicable();
        }
        if (fogEnvironment == null) {
            throw new IllegalStateException("No color source environment found");
        }
        int n2 = fogEnvironment.getBaseColor(clientLevel, camera, n, f2);
        float f4 = clientLevel.getLevelData().voidDarknessOnsetRange();
        float f5 = Mth.clamp((f4 + (float)clientLevel.getMinY() - (float)camera.getPosition().y) / f4, 0.0f, 1.0f);
        if (fogEnvironment2 != null) {
            LivingEntity livingEntity2 = (LivingEntity)entity;
            f5 = fogEnvironment2.getModifiedDarkness(livingEntity2, f5, f);
        }
        float f6 = ARGB.redFloat(n2);
        float f7 = ARGB.greenFloat(n2);
        float f8 = ARGB.blueFloat(n2);
        if (f5 > 0.0f && fogType != FogType.LAVA && fogType != FogType.POWDER_SNOW) {
            f3 = Mth.square(1.0f - f5);
            f6 *= f3;
            f7 *= f3;
            f8 *= f3;
        }
        if (f2 > 0.0f) {
            f6 = Mth.lerp(f2, f6, f6 * 0.7f);
            f7 = Mth.lerp(f2, f7, f7 * 0.6f);
            f8 = Mth.lerp(f2, f8, f8 * 0.6f);
        }
        f3 = fogType == FogType.WATER ? (entity instanceof LocalPlayer ? ((LocalPlayer)entity).getWaterVision() : 1.0f) : (entity instanceof LivingEntity && (livingEntity = (LivingEntity)entity).hasEffect(MobEffects.NIGHT_VISION) && !livingEntity.hasEffect(MobEffects.DARKNESS) ? GameRenderer.getNightVisionScale(livingEntity, f) : 0.0f);
        if (f6 != 0.0f && f7 != 0.0f && f8 != 0.0f) {
            float f9 = 1.0f / Math.max(f6, Math.max(f7, f8));
            f6 = Mth.lerp(f3, f6, f6 * f9);
            f7 = Mth.lerp(f3, f7, f7 * f9);
            f8 = Mth.lerp(f3, f8, f8 * f9);
        }
        return new Vector4f(f6, f7, f8, 1.0f);
    }

    public static boolean toggleFog() {
        fogEnabled = !fogEnabled;
        return fogEnabled;
    }

    public Vector4f setupFog(Camera camera, int n, boolean bl, DeltaTracker deltaTracker, float f, ClientLevel clientLevel) {
        float f2 = deltaTracker.getGameTimeDeltaPartialTick(false);
        Vector4f vector4f = this.computeFogColor(camera, f2, clientLevel, n, f, bl);
        float f3 = n * 16;
        FogType fogType = this.getFogType(camera, bl);
        Entity entity = camera.getEntity();
        FogData fogData = new FogData();
        for (FogEnvironment object2 : FOG_ENVIRONMENTS) {
            if (!object2.isApplicable(fogType, entity)) continue;
            object2.setupFog(fogData, entity, camera.getBlockPosition(), clientLevel, f3, deltaTracker);
            break;
        }
        float f4 = Mth.clamp(f3 / 10.0f, 4.0f, 64.0f);
        fogData.renderDistanceStart = f3 - f4;
        fogData.renderDistanceEnd = f3;
        try (GpuBuffer.MappedView mappedView = RenderSystem.getDevice().createCommandEncoder().mapBuffer(this.regularBuffer.currentBuffer(), false, true);){
            this.updateBuffer(mappedView.data(), 0, vector4f, fogData.environmentalStart, fogData.environmentalEnd, fogData.renderDistanceStart, fogData.renderDistanceEnd, fogData.skyEnd, fogData.cloudEnd);
        }
        return vector4f;
    }

    private FogType getFogType(Camera camera, boolean bl) {
        FogType fogType = camera.getFluidInCamera();
        if (fogType == FogType.NONE) {
            if (bl) {
                return FogType.DIMENSION_OR_BOSS;
            }
            return FogType.ATMOSPHERIC;
        }
        return fogType;
    }

    private void updateBuffer(ByteBuffer byteBuffer, int n, Vector4f vector4f, float f, float f2, float f3, float f4, float f5, float f6) {
        byteBuffer.position(n);
        Std140Builder.intoBuffer(byteBuffer).putVec4((Vector4fc)vector4f).putFloat(f).putFloat(f2).putFloat(f3).putFloat(f4).putFloat(f5).putFloat(f6);
    }

    public static enum FogMode {
        NONE,
        WORLD;

    }
}

