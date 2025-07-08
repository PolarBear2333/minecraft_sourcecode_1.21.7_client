/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;

public abstract class AbstractZombieModel<S extends ZombieRenderState>
extends HumanoidModel<S> {
    protected AbstractZombieModel(ModelPart modelPart) {
        super(modelPart);
    }

    @Override
    public void setupAnim(S s) {
        super.setupAnim(s);
        float f = ((ZombieRenderState)s).attackTime;
        AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, ((ZombieRenderState)s).isAggressive, f, ((ZombieRenderState)s).ageInTicks);
    }
}

