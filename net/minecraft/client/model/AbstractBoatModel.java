/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.util.Mth;

public abstract class AbstractBoatModel
extends EntityModel<BoatRenderState> {
    private final ModelPart leftPaddle;
    private final ModelPart rightPaddle;

    public AbstractBoatModel(ModelPart modelPart) {
        super(modelPart);
        this.leftPaddle = modelPart.getChild("left_paddle");
        this.rightPaddle = modelPart.getChild("right_paddle");
    }

    @Override
    public void setupAnim(BoatRenderState boatRenderState) {
        super.setupAnim(boatRenderState);
        AbstractBoatModel.animatePaddle(boatRenderState.rowingTimeLeft, 0, this.leftPaddle);
        AbstractBoatModel.animatePaddle(boatRenderState.rowingTimeRight, 1, this.rightPaddle);
    }

    private static void animatePaddle(float f, int n, ModelPart modelPart) {
        modelPart.xRot = Mth.clampedLerp(-1.0471976f, -0.2617994f, (Mth.sin(-f) + 1.0f) / 2.0f);
        modelPart.yRot = Mth.clampedLerp(-0.7853982f, 0.7853982f, (Mth.sin(-f + 1.0f) + 1.0f) / 2.0f);
        if (n == 1) {
            modelPart.yRot = (float)Math.PI - modelPart.yRot;
        }
    }
}

