/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 */
package net.minecraft.client.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.Zone;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;

public class RemotePlayer
extends AbstractClientPlayer {
    private Vec3 lerpDeltaMovement = Vec3.ZERO;
    private int lerpDeltaMovementSteps;

    public RemotePlayer(ClientLevel clientLevel, GameProfile gameProfile) {
        super(clientLevel, gameProfile);
        this.noPhysics = true;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d) {
        double d2 = this.getBoundingBox().getSize() * 10.0;
        if (Double.isNaN(d2)) {
            d2 = 1.0;
        }
        return d < (d2 *= 64.0 * RemotePlayer.getViewScale()) * d2;
    }

    @Override
    public boolean hurtClient(DamageSource damageSource) {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        this.calculateEntityAnimation(false);
    }

    @Override
    public void aiStep() {
        if (this.isInterpolating()) {
            this.getInterpolation().interpolate();
        }
        if (this.lerpHeadSteps > 0) {
            this.lerpHeadRotationStep(this.lerpHeadSteps, this.lerpYHeadRot);
            --this.lerpHeadSteps;
        }
        if (this.lerpDeltaMovementSteps > 0) {
            this.addDeltaMovement(new Vec3((this.lerpDeltaMovement.x - this.getDeltaMovement().x) / (double)this.lerpDeltaMovementSteps, (this.lerpDeltaMovement.y - this.getDeltaMovement().y) / (double)this.lerpDeltaMovementSteps, (this.lerpDeltaMovement.z - this.getDeltaMovement().z) / (double)this.lerpDeltaMovementSteps));
            --this.lerpDeltaMovementSteps;
        }
        this.oBob = this.bob;
        this.updateSwingTime();
        float f = !this.onGround() || this.isDeadOrDying() ? 0.0f : (float)Math.min(0.1, this.getDeltaMovement().horizontalDistance());
        this.bob += (f - this.bob) * 0.4f;
        try (Zone zone = Profiler.get().zone("push");){
            this.pushEntities();
        }
    }

    @Override
    public void lerpMotion(double d, double d2, double d3) {
        this.lerpDeltaMovement = new Vec3(d, d2, d3);
        this.lerpDeltaMovementSteps = this.getType().updateInterval() + 1;
    }

    @Override
    protected void updatePlayerPose() {
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket clientboundAddEntityPacket) {
        super.recreateFromPacket(clientboundAddEntityPacket);
        this.setOldPosAndRot();
    }
}

