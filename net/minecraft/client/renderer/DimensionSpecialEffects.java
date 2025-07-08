/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
 *  it.unimi.dsi.fastutil.objects.Object2ObjectMap
 */
package net.minecraft.client.renderer;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;

public abstract class DimensionSpecialEffects {
    private static final Object2ObjectMap<ResourceLocation, DimensionSpecialEffects> EFFECTS = (Object2ObjectMap)Util.make(new Object2ObjectArrayMap(), object2ObjectArrayMap -> {
        OverworldEffects overworldEffects = new OverworldEffects();
        object2ObjectArrayMap.defaultReturnValue((Object)overworldEffects);
        object2ObjectArrayMap.put((Object)BuiltinDimensionTypes.OVERWORLD_EFFECTS, (Object)overworldEffects);
        object2ObjectArrayMap.put((Object)BuiltinDimensionTypes.NETHER_EFFECTS, (Object)new NetherEffects());
        object2ObjectArrayMap.put((Object)BuiltinDimensionTypes.END_EFFECTS, (Object)new EndEffects());
    });
    private final SkyType skyType;
    private final boolean forceBrightLightmap;
    private final boolean constantAmbientLight;

    public DimensionSpecialEffects(SkyType skyType, boolean bl, boolean bl2) {
        this.skyType = skyType;
        this.forceBrightLightmap = bl;
        this.constantAmbientLight = bl2;
    }

    public static DimensionSpecialEffects forType(DimensionType dimensionType) {
        return (DimensionSpecialEffects)EFFECTS.get((Object)dimensionType.effectsLocation());
    }

    public boolean isSunriseOrSunset(float f) {
        return false;
    }

    public int getSunriseOrSunsetColor(float f) {
        return 0;
    }

    public abstract Vec3 getBrightnessDependentFogColor(Vec3 var1, float var2);

    public abstract boolean isFoggyAt(int var1, int var2);

    public SkyType skyType() {
        return this.skyType;
    }

    public boolean forceBrightLightmap() {
        return this.forceBrightLightmap;
    }

    public boolean constantAmbientLight() {
        return this.constantAmbientLight;
    }

    public static enum SkyType {
        NONE,
        OVERWORLD,
        END;

    }

    public static class OverworldEffects
    extends DimensionSpecialEffects {
        private static final float SUNRISE_AND_SUNSET_TIMESPAN = 0.4f;

        public OverworldEffects() {
            super(SkyType.OVERWORLD, false, false);
        }

        @Override
        public boolean isSunriseOrSunset(float f) {
            float f2 = Mth.cos(f * ((float)Math.PI * 2));
            return f2 >= -0.4f && f2 <= 0.4f;
        }

        @Override
        public int getSunriseOrSunsetColor(float f) {
            float f2 = Mth.cos(f * ((float)Math.PI * 2));
            float f3 = f2 / 0.4f * 0.5f + 0.5f;
            float f4 = Mth.square(1.0f - (1.0f - Mth.sin(f3 * (float)Math.PI)) * 0.99f);
            return ARGB.colorFromFloat(f4, f3 * 0.3f + 0.7f, f3 * f3 * 0.7f + 0.2f, 0.2f);
        }

        @Override
        public Vec3 getBrightnessDependentFogColor(Vec3 vec3, float f) {
            return vec3.multiply(f * 0.94f + 0.06f, f * 0.94f + 0.06f, f * 0.91f + 0.09f);
        }

        @Override
        public boolean isFoggyAt(int n, int n2) {
            return false;
        }
    }

    public static class NetherEffects
    extends DimensionSpecialEffects {
        public NetherEffects() {
            super(SkyType.NONE, false, true);
        }

        @Override
        public Vec3 getBrightnessDependentFogColor(Vec3 vec3, float f) {
            return vec3;
        }

        @Override
        public boolean isFoggyAt(int n, int n2) {
            return true;
        }
    }

    public static class EndEffects
    extends DimensionSpecialEffects {
        public EndEffects() {
            super(SkyType.END, true, false);
        }

        @Override
        public Vec3 getBrightnessDependentFogColor(Vec3 vec3, float f) {
            return vec3.scale(0.15f);
        }

        @Override
        public boolean isFoggyAt(int n, int n2) {
            return false;
        }
    }
}

