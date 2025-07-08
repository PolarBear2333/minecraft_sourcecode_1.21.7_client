/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.entity.state;

import javax.annotation.Nullable;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.boss.enderdragon.DragonFlightHistory;
import net.minecraft.world.phys.Vec3;

public class EnderDragonRenderState
extends EntityRenderState {
    public float flapTime;
    public float deathTime;
    public boolean hasRedOverlay;
    @Nullable
    public Vec3 beamOffset;
    public boolean isLandingOrTakingOff;
    public boolean isSitting;
    public double distanceToEgg;
    public float partialTicks;
    public final DragonFlightHistory flightHistory = new DragonFlightHistory();

    public DragonFlightHistory.Sample getHistoricalPos(int n) {
        return this.flightHistory.get(n, this.partialTicks);
    }

    public float getHeadPartYOffset(int n, DragonFlightHistory.Sample sample, DragonFlightHistory.Sample sample2) {
        double d = this.isLandingOrTakingOff ? (double)n / Math.max(this.distanceToEgg / 4.0, 1.0) : (this.isSitting ? (double)n : (n == 6 ? 0.0 : sample2.y() - sample.y()));
        return (float)d;
    }
}

