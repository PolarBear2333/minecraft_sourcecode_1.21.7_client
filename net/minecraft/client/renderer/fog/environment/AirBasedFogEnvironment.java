/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package net.minecraft.client.renderer.fog.environment;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.util.ARGB;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public abstract class AirBasedFogEnvironment
extends FogEnvironment {
    @Override
    public int getBaseColor(ClientLevel clientLevel, Camera camera, int n4, float f) {
        float f2;
        float f3;
        float f4;
        float f5 = Mth.clamp(Mth.cos(clientLevel.getTimeOfDay(f) * ((float)Math.PI * 2)) * 2.0f + 0.5f, 0.0f, 1.0f);
        BiomeManager biomeManager = clientLevel.getBiomeManager();
        Vec3 vec3 = camera.getPosition().subtract(2.0, 2.0, 2.0).scale(0.25);
        Vec3 vec32 = clientLevel.effects().getBrightnessDependentFogColor(CubicSampler.gaussianSampleVec3(vec3, (n, n2, n3) -> Vec3.fromRGB24(biomeManager.getNoiseBiomeAtQuart(n, n2, n3).value().getFogColor())), f5);
        float f6 = (float)vec32.x();
        float f7 = (float)vec32.y();
        float f8 = (float)vec32.z();
        if (n4 >= 4) {
            float f9 = Mth.sin(clientLevel.getSunAngle(f)) > 0.0f ? -1.0f : 1.0f;
            Vector3f vector3f = new Vector3f(f9, 0.0f, 0.0f);
            f4 = camera.getLookVector().dot((Vector3fc)vector3f);
            if (f4 > 0.0f && clientLevel.effects().isSunriseOrSunset(clientLevel.getTimeOfDay(f))) {
                int n5 = clientLevel.effects().getSunriseOrSunsetColor(clientLevel.getTimeOfDay(f));
                f6 = Mth.lerp(f4 *= ARGB.alphaFloat(n5), f6, ARGB.redFloat(n5));
                f7 = Mth.lerp(f4, f7, ARGB.greenFloat(n5));
                f8 = Mth.lerp(f4, f8, ARGB.blueFloat(n5));
            }
        }
        int n6 = clientLevel.getSkyColor(camera.getPosition(), f);
        float f10 = ARGB.redFloat(n6);
        f4 = ARGB.greenFloat(n6);
        float f11 = ARGB.blueFloat(n6);
        float f12 = 0.25f + 0.75f * (float)n4 / 32.0f;
        f12 = 1.0f - (float)Math.pow(f12, 0.25);
        f6 += (f10 - f6) * f12;
        f7 += (f4 - f7) * f12;
        f8 += (f11 - f8) * f12;
        float f13 = clientLevel.getRainLevel(f);
        if (f13 > 0.0f) {
            f3 = 1.0f - f13 * 0.5f;
            f2 = 1.0f - f13 * 0.4f;
            f6 *= f3;
            f7 *= f3;
            f8 *= f2;
        }
        if ((f3 = clientLevel.getThunderLevel(f)) > 0.0f) {
            f2 = 1.0f - f3 * 0.5f;
            f6 *= f2;
            f7 *= f2;
            f8 *= f2;
        }
        return ARGB.colorFromFloat(1.0f, f6, f7, f8);
    }
}

