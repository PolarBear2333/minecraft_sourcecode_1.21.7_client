/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.fog.environment;

import javax.annotation.Nullable;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.AirBasedFogEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FogType;

public class DimensionOrBossFogEnvironment
extends AirBasedFogEnvironment {
    @Override
    public void setupFog(FogData fogData, Entity entity, BlockPos blockPos, ClientLevel clientLevel, float f, DeltaTracker deltaTracker) {
        fogData.environmentalStart = f * 0.05f;
        fogData.skyEnd = fogData.environmentalEnd = Math.min(f, 192.0f) * 0.5f;
        fogData.cloudEnd = fogData.environmentalEnd;
    }

    @Override
    public boolean isApplicable(@Nullable FogType fogType, Entity entity) {
        return fogType == FogType.DIMENSION_OR_BOSS;
    }
}

