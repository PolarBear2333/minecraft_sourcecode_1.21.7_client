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
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;

public abstract class FogEnvironment {
    public abstract void setupFog(FogData var1, Entity var2, BlockPos var3, ClientLevel var4, float var5, DeltaTracker var6);

    public boolean providesColor() {
        return true;
    }

    public int getBaseColor(ClientLevel clientLevel, Camera camera, int n, float f) {
        return -1;
    }

    public boolean modifiesDarkness() {
        return false;
    }

    public float getModifiedDarkness(LivingEntity livingEntity, float f, float f2) {
        return f;
    }

    public abstract boolean isApplicable(@Nullable FogType var1, Entity var2);

    public void onNotApplicable() {
    }
}

