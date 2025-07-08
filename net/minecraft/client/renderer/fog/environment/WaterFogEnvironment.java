/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.fog.environment;

import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FogType;

public class WaterFogEnvironment
extends FogEnvironment {
    private static final int WATER_FOG_DISTANCE = 96;
    private static final float BIOME_FOG_TRANSITION_TIME = 5000.0f;
    private static int targetBiomeFog = -1;
    private static int previousBiomeFog = -1;
    private static long biomeChangedTime = -1L;

    @Override
    public void setupFog(FogData fogData, Entity entity, BlockPos blockPos, ClientLevel clientLevel, float f, DeltaTracker deltaTracker) {
        fogData.environmentalStart = -8.0f;
        fogData.environmentalEnd = 96.0f;
        if (entity instanceof LocalPlayer) {
            LocalPlayer localPlayer = (LocalPlayer)entity;
            fogData.environmentalEnd *= Math.max(0.25f, localPlayer.getWaterVision());
            if (clientLevel.getBiome(blockPos).is(BiomeTags.HAS_CLOSER_WATER_FOG)) {
                fogData.environmentalEnd *= 0.85f;
            }
        }
        fogData.skyEnd = fogData.environmentalEnd;
        fogData.cloudEnd = fogData.environmentalEnd;
    }

    @Override
    public boolean isApplicable(@Nullable FogType fogType, Entity entity) {
        return fogType == FogType.WATER;
    }

    @Override
    public int getBaseColor(ClientLevel clientLevel, Camera camera, int n, float f) {
        long l = Util.getMillis();
        int n2 = clientLevel.getBiome(camera.getBlockPosition()).value().getWaterFogColor();
        if (biomeChangedTime < 0L) {
            targetBiomeFog = n2;
            previousBiomeFog = n2;
            biomeChangedTime = l;
        }
        float f2 = Mth.clamp((float)(l - biomeChangedTime) / 5000.0f, 0.0f, 1.0f);
        int n3 = ARGB.lerp(f2, previousBiomeFog, targetBiomeFog);
        if (targetBiomeFog != n2) {
            targetBiomeFog = n2;
            previousBiomeFog = n3;
            biomeChangedTime = l;
        }
        return n3;
    }

    @Override
    public void onNotApplicable() {
        biomeChangedTime = -1L;
    }
}

