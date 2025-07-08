/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import net.minecraft.client.model.AbstractPiglinModel;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.ZombifiedPiglinRenderState;

public class ZombifiedPiglinModel
extends AbstractPiglinModel<ZombifiedPiglinRenderState> {
    public ZombifiedPiglinModel(ModelPart modelPart) {
        super(modelPart);
    }

    @Override
    public void setupAnim(ZombifiedPiglinRenderState zombifiedPiglinRenderState) {
        super.setupAnim(zombifiedPiglinRenderState);
        AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, zombifiedPiglinRenderState.isAggressive, zombifiedPiglinRenderState.attackTime, zombifiedPiglinRenderState.ageInTicks);
    }

    @Override
    public void setAllVisible(boolean bl) {
        super.setAllVisible(bl);
        this.leftSleeve.visible = bl;
        this.rightSleeve.visible = bl;
        this.leftPants.visible = bl;
        this.rightPants.visible = bl;
        this.jacket.visible = bl;
    }
}

