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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FogType;

public class PowderedSnowFogEnvironment
extends FogEnvironment {
    private static final int COLOR = -6308916;

    @Override
    public int getBaseColor(ClientLevel clientLevel, Camera camera, int n, float f) {
        return -6308916;
    }

    @Override
    public void setupFog(FogData fogData, Entity entity, BlockPos blockPos, ClientLevel clientLevel, float f, DeltaTracker deltaTracker) {
        if (entity.isSpectator()) {
            fogData.environmentalStart = -8.0f;
            fogData.environmentalEnd = f * 0.5f;
        } else {
            fogData.environmentalStart = 0.0f;
            fogData.environmentalEnd = 2.0f;
        }
        fogData.skyEnd = fogData.environmentalEnd;
        fogData.cloudEnd = fogData.environmentalEnd;
    }

    @Override
    public boolean isApplicable(@Nullable FogType fogType, Entity entity) {
        return fogType == FogType.POWDER_SNOW;
    }
}

