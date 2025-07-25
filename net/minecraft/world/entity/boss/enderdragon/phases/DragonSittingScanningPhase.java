/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.boss.enderdragon.phases;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonSittingPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class DragonSittingScanningPhase
extends AbstractDragonSittingPhase {
    private static final int SITTING_SCANNING_IDLE_TICKS = 100;
    private static final int SITTING_ATTACK_Y_VIEW_RANGE = 10;
    private static final int SITTING_ATTACK_VIEW_RANGE = 20;
    private static final int SITTING_CHARGE_VIEW_RANGE = 150;
    private static final TargetingConditions CHARGE_TARGETING = TargetingConditions.forCombat().range(150.0);
    private final TargetingConditions scanTargeting = TargetingConditions.forCombat().range(20.0).selector((livingEntity, serverLevel) -> Math.abs(livingEntity.getY() - enderDragon.getY()) <= 10.0);
    private int scanningTime;

    public DragonSittingScanningPhase(EnderDragon enderDragon) {
        super(enderDragon);
    }

    @Override
    public void doServerTick(ServerLevel serverLevel) {
        ++this.scanningTime;
        Player player = serverLevel.getNearestPlayer(this.scanTargeting, this.dragon, this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
        if (player != null) {
            if (this.scanningTime > 25) {
                this.dragon.getPhaseManager().setPhase(EnderDragonPhase.SITTING_ATTACKING);
            } else {
                Vec3 vec3 = new Vec3(player.getX() - this.dragon.getX(), 0.0, player.getZ() - this.dragon.getZ()).normalize();
                Vec3 vec32 = new Vec3(Mth.sin(this.dragon.getYRot() * ((float)Math.PI / 180)), 0.0, -Mth.cos(this.dragon.getYRot() * ((float)Math.PI / 180))).normalize();
                float f = (float)vec32.dot(vec3);
                float f2 = (float)(Math.acos(f) * 57.2957763671875) + 0.5f;
                if (f2 < 0.0f || f2 > 10.0f) {
                    float f3;
                    double d = player.getX() - this.dragon.head.getX();
                    double d2 = player.getZ() - this.dragon.head.getZ();
                    double d3 = Mth.clamp(Mth.wrapDegrees(180.0 - Mth.atan2(d, d2) * 57.2957763671875 - (double)this.dragon.getYRot()), -100.0, 100.0);
                    this.dragon.yRotA *= 0.8f;
                    float f4 = f3 = (float)Math.sqrt(d * d + d2 * d2) + 1.0f;
                    if (f3 > 40.0f) {
                        f3 = 40.0f;
                    }
                    this.dragon.yRotA += (float)d3 * (0.7f / f3 / f4);
                    this.dragon.setYRot(this.dragon.getYRot() + this.dragon.yRotA);
                }
            }
        } else if (this.scanningTime >= 100) {
            player = serverLevel.getNearestPlayer(CHARGE_TARGETING, this.dragon, this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
            this.dragon.getPhaseManager().setPhase(EnderDragonPhase.TAKEOFF);
            if (player != null) {
                this.dragon.getPhaseManager().setPhase(EnderDragonPhase.CHARGING_PLAYER);
                this.dragon.getPhaseManager().getPhase(EnderDragonPhase.CHARGING_PLAYER).setTarget(new Vec3(player.getX(), player.getY(), player.getZ()));
            }
        }
    }

    @Override
    public void begin() {
        this.scanningTime = 0;
    }

    public EnderDragonPhase<DragonSittingScanningPhase> getPhase() {
        return EnderDragonPhase.SITTING_SCANNING;
    }
}

