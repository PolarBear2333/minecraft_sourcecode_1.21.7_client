/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.ai.goal;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;

public class FollowParentGoal
extends Goal {
    public static final int HORIZONTAL_SCAN_RANGE = 8;
    public static final int VERTICAL_SCAN_RANGE = 4;
    public static final int DONT_FOLLOW_IF_CLOSER_THAN = 3;
    private final Animal animal;
    @Nullable
    private Animal parent;
    private final double speedModifier;
    private int timeToRecalcPath;

    public FollowParentGoal(Animal animal, double d) {
        this.animal = animal;
        this.speedModifier = d;
    }

    @Override
    public boolean canUse() {
        if (this.animal.getAge() >= 0) {
            return false;
        }
        List<?> list = this.animal.level().getEntitiesOfClass(this.animal.getClass(), this.animal.getBoundingBox().inflate(8.0, 4.0, 8.0));
        Animal animal = null;
        double d = Double.MAX_VALUE;
        for (Animal animal2 : list) {
            double d2;
            if (animal2.getAge() < 0 || (d2 = this.animal.distanceToSqr(animal2)) > d) continue;
            d = d2;
            animal = animal2;
        }
        if (animal == null) {
            return false;
        }
        if (d < 9.0) {
            return false;
        }
        this.parent = animal;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.animal.getAge() >= 0) {
            return false;
        }
        if (!this.parent.isAlive()) {
            return false;
        }
        double d = this.animal.distanceToSqr(this.parent);
        return !(d < 9.0) && !(d > 256.0);
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
    }

    @Override
    public void stop() {
        this.parent = null;
    }

    @Override
    public void tick() {
        if (--this.timeToRecalcPath > 0) {
            return;
        }
        this.timeToRecalcPath = this.adjustedTickDelay(10);
        this.animal.getNavigation().moveTo(this.parent, this.speedModifier);
    }
}

