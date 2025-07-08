/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.fog.environment;

import javax.annotation.Nullable;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;

public class LavaFogEnvironment
extends FogEnvironment {
    private static final int COLOR = -6743808;

    @Override
    public int getBaseColor(ClientLevel clientLevel, Camera camera, int n, float f) {
        return -6743808;
    }

    @Override
    public void setupFog(FogData fogData, Entity entity, BlockPos blockPos, ClientLevel clientLevel, float f, DeltaTracker deltaTracker) {
        LivingEntity livingEntity;
        if (entity.isSpectator()) {
            fogData.environmentalStart = -8.0f;
            fogData.environmentalEnd = f * 0.5f;
        } else if (entity instanceof LivingEntity && (livingEntity = (LivingEntity)entity).hasEffect(MobEffects.FIRE_RESISTANCE)) {
            fogData.environmentalStart = 0.0f;
            fogData.environmentalEnd = 5.0f;
        } else {
            fogData.environmentalStart = 0.25f;
            fogData.environmentalEnd = 1.0f;
        }
        fogData.skyEnd = fogData.environmentalEnd;
        fogData.cloudEnd = fogData.environmentalEnd;
    }

    @Override
    public boolean isApplicable(@Nullable FogType fogType, Entity entity) {
        return fogType == FogType.LAVA;
    }
}

