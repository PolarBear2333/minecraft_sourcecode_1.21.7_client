/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.ai.goal;

import javax.annotation.Nullable;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

public class WaterAvoidingRandomStrollGoal
extends RandomStrollGoal {
    public static final float PROBABILITY = 0.001f;
    protected final float probability;

    public WaterAvoidingRandomStrollGoal(PathfinderMob pathfinderMob, double d) {
        this(pathfinderMob, d, 0.001f);
    }

    public WaterAvoidingRandomStrollGoal(PathfinderMob pathfinderMob, double d, float f) {
        super(pathfinderMob, d);
        this.probability = f;
    }

    @Override
    @Nullable
    protected Vec3 getPosition() {
        if (this.mob.isInWater()) {
            Vec3 vec3 = LandRandomPos.getPos(this.mob, 15, 7);
            return vec3 == null ? super.getPosition() : vec3;
        }
        if (this.mob.getRandom().nextFloat() >= this.probability) {
            return LandRandomPos.getPos(this.mob, 10, 7);
        }
        return super.getPosition();
    }
}

