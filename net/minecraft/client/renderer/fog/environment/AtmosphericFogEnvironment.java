/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.fog.environment;

import javax.annotation.Nullable;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.AirBasedFogEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.FogType;

public class AtmosphericFogEnvironment
extends AirBasedFogEnvironment {
    private static final int MIN_RAIN_FOG_SKY_LIGHT = 8;
    private static final float RAIN_FOG_START_OFFSET = -160.0f;
    private static final float RAIN_FOG_END_OFFSET = -256.0f;
    private float rainFogMultiplier;

    @Override
    public void setupFog(FogData fogData, Entity entity, BlockPos blockPos, ClientLevel clientLevel, float f, DeltaTracker deltaTracker) {
        Biome biome = clientLevel.getBiome(blockPos).value();
        float f2 = deltaTracker.getGameTimeDeltaTicks();
        boolean bl = biome.hasPrecipitation();
        float f3 = Mth.clamp(((float)clientLevel.getLightEngine().getLayerListener(LightLayer.SKY).getLightValue(blockPos) - 8.0f) / 7.0f, 0.0f, 1.0f);
        float f4 = clientLevel.getRainLevel(deltaTracker.getGameTimeDeltaPartialTick(false)) * f3 * (bl ? 1.0f : 0.5f);
        this.rainFogMultiplier += (f4 - this.rainFogMultiplier) * f2 * 0.2f;
        fogData.environmentalStart = this.rainFogMultiplier * -160.0f;
        fogData.environmentalEnd = 1024.0f + -256.0f * this.rainFogMultiplier;
        fogData.skyEnd = f;
        fogData.cloudEnd = Minecraft.getInstance().options.cloudRange().get() * 16;
    }

    @Override
    public boolean isApplicable(@Nullable FogType fogType, Entity entity) {
        return fogType == FogType.ATMOSPHERIC;
    }
}

