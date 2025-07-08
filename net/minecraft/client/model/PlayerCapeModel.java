/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionf
 */
package net.minecraft.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.joml.Quaternionf;

public class PlayerCapeModel<T extends PlayerRenderState>
extends HumanoidModel<T> {
    private static final String CAPE = "cape";
    private final ModelPart cape;

    public PlayerCapeModel(ModelPart modelPart) {
        super(modelPart);
        this.cape = this.body.getChild(CAPE);
    }

    public static LayerDefinition createCapeLayer() {
        MeshDefinition meshDefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0f);
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.clearChild("head");
        partDefinition2.clearChild("hat");
        PartDefinition partDefinition3 = partDefinition.clearChild("body");
        partDefinition.clearChild("left_arm");
        partDefinition.clearChild("right_arm");
        partDefinition.clearChild("left_leg");
        partDefinition.clearChild("right_leg");
        partDefinition3.addOrReplaceChild(CAPE, CubeListBuilder.create().texOffs(0, 0).addBox(-5.0f, 0.0f, -1.0f, 10.0f, 16.0f, 1.0f, CubeDeformation.NONE, 1.0f, 0.5f), PartPose.offsetAndRotation(0.0f, 0.0f, 2.0f, 0.0f, (float)Math.PI, 0.0f));
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void setupAnim(T t) {
        super.setupAnim(t);
        this.cape.rotateBy(new Quaternionf().rotateY((float)(-Math.PI)).rotateX((6.0f + ((PlayerRenderState)t).capeLean / 2.0f + ((PlayerRenderState)t).capeFlap) * ((float)Math.PI / 180)).rotateZ(((PlayerRenderState)t).capeLean2 / 2.0f * ((float)Math.PI / 180)).rotateY((180.0f - ((PlayerRenderState)t).capeLean2 / 2.0f) * ((float)Math.PI / 180)));
    }
}

