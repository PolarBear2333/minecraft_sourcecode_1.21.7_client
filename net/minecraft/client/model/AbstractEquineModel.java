/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import java.util.Set;
import net.minecraft.client.model.BabyModelTransform;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.util.Mth;

public abstract class AbstractEquineModel<T extends EquineRenderState>
extends EntityModel<T> {
    private static final float DEG_125 = 2.1816616f;
    private static final float DEG_60 = 1.0471976f;
    private static final float DEG_45 = 0.7853982f;
    private static final float DEG_30 = 0.5235988f;
    private static final float DEG_15 = 0.2617994f;
    protected static final String HEAD_PARTS = "head_parts";
    protected static final MeshTransformer BABY_TRANSFORMER = new BabyModelTransform(true, 16.2f, 1.36f, 2.7272f, 2.0f, 20.0f, Set.of("head_parts"));
    protected final ModelPart body;
    protected final ModelPart headParts;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart tail;

    public AbstractEquineModel(ModelPart modelPart) {
        super(modelPart);
        this.body = modelPart.getChild("body");
        this.headParts = modelPart.getChild(HEAD_PARTS);
        this.rightHindLeg = modelPart.getChild("right_hind_leg");
        this.leftHindLeg = modelPart.getChild("left_hind_leg");
        this.rightFrontLeg = modelPart.getChild("right_front_leg");
        this.leftFrontLeg = modelPart.getChild("left_front_leg");
        this.tail = this.body.getChild("tail");
    }

    public static MeshDefinition createBodyMesh(CubeDeformation cubeDeformation) {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 32).addBox(-5.0f, -8.0f, -17.0f, 10.0f, 10.0f, 22.0f, new CubeDeformation(0.05f)), PartPose.offset(0.0f, 11.0f, 5.0f));
        PartDefinition partDefinition3 = partDefinition.addOrReplaceChild(HEAD_PARTS, CubeListBuilder.create().texOffs(0, 35).addBox(-2.05f, -6.0f, -2.0f, 4.0f, 12.0f, 7.0f), PartPose.offsetAndRotation(0.0f, 4.0f, -12.0f, 0.5235988f, 0.0f, 0.0f));
        PartDefinition partDefinition4 = partDefinition3.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 13).addBox(-3.0f, -11.0f, -2.0f, 6.0f, 5.0f, 7.0f, cubeDeformation), PartPose.ZERO);
        partDefinition3.addOrReplaceChild("mane", CubeListBuilder.create().texOffs(56, 36).addBox(-1.0f, -11.0f, 5.01f, 2.0f, 16.0f, 2.0f, cubeDeformation), PartPose.ZERO);
        partDefinition3.addOrReplaceChild("upper_mouth", CubeListBuilder.create().texOffs(0, 25).addBox(-2.0f, -11.0f, -7.0f, 4.0f, 5.0f, 5.0f, cubeDeformation), PartPose.ZERO);
        partDefinition.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(48, 21).mirror().addBox(-3.0f, -1.01f, -1.0f, 4.0f, 11.0f, 4.0f, cubeDeformation), PartPose.offset(4.0f, 14.0f, 7.0f));
        partDefinition.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(48, 21).addBox(-1.0f, -1.01f, -1.0f, 4.0f, 11.0f, 4.0f, cubeDeformation), PartPose.offset(-4.0f, 14.0f, 7.0f));
        partDefinition.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(48, 21).mirror().addBox(-3.0f, -1.01f, -1.9f, 4.0f, 11.0f, 4.0f, cubeDeformation), PartPose.offset(4.0f, 14.0f, -10.0f));
        partDefinition.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(48, 21).addBox(-1.0f, -1.01f, -1.9f, 4.0f, 11.0f, 4.0f, cubeDeformation), PartPose.offset(-4.0f, 14.0f, -10.0f));
        partDefinition2.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(42, 36).addBox(-1.5f, 0.0f, 0.0f, 3.0f, 14.0f, 4.0f, cubeDeformation), PartPose.offsetAndRotation(0.0f, -5.0f, 2.0f, 0.5235988f, 0.0f, 0.0f));
        partDefinition4.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(19, 16).addBox(0.55f, -13.0f, 4.0f, 2.0f, 3.0f, 1.0f, new CubeDeformation(-0.001f)), PartPose.ZERO);
        partDefinition4.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(19, 16).addBox(-2.55f, -13.0f, 4.0f, 2.0f, 3.0f, 1.0f, new CubeDeformation(-0.001f)), PartPose.ZERO);
        return meshDefinition;
    }

    public static MeshDefinition createBabyMesh(CubeDeformation cubeDeformation) {
        return BABY_TRANSFORMER.apply(AbstractEquineModel.createFullScaleBabyMesh(cubeDeformation));
    }

    protected static MeshDefinition createFullScaleBabyMesh(CubeDeformation cubeDeformation) {
        MeshDefinition meshDefinition = AbstractEquineModel.createBodyMesh(cubeDeformation);
        PartDefinition partDefinition = meshDefinition.getRoot();
        CubeDeformation cubeDeformation2 = cubeDeformation.extend(0.0f, 5.5f, 0.0f);
        partDefinition.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(48, 21).mirror().addBox(-3.0f, -1.01f, -1.0f, 4.0f, 11.0f, 4.0f, cubeDeformation2), PartPose.offset(4.0f, 14.0f, 7.0f));
        partDefinition.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(48, 21).addBox(-1.0f, -1.01f, -1.0f, 4.0f, 11.0f, 4.0f, cubeDeformation2), PartPose.offset(-4.0f, 14.0f, 7.0f));
        partDefinition.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(48, 21).mirror().addBox(-3.0f, -1.01f, -1.9f, 4.0f, 11.0f, 4.0f, cubeDeformation2), PartPose.offset(4.0f, 14.0f, -10.0f));
        partDefinition.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(48, 21).addBox(-1.0f, -1.01f, -1.9f, 4.0f, 11.0f, 4.0f, cubeDeformation2), PartPose.offset(-4.0f, 14.0f, -10.0f));
        return meshDefinition;
    }

    @Override
    public void setupAnim(T t) {
        super.setupAnim(t);
        float f = Mth.clamp(((EquineRenderState)t).yRot, -20.0f, 20.0f);
        float f2 = ((EquineRenderState)t).xRot * ((float)Math.PI / 180);
        float f3 = ((EquineRenderState)t).walkAnimationSpeed;
        float f4 = ((EquineRenderState)t).walkAnimationPos;
        if (f3 > 0.2f) {
            f2 += Mth.cos(f4 * 0.8f) * 0.15f * f3;
        }
        float f5 = ((EquineRenderState)t).eatAnimation;
        float f6 = ((EquineRenderState)t).standAnimation;
        float f7 = 1.0f - f6;
        float f8 = ((EquineRenderState)t).feedingAnimation;
        boolean bl = ((EquineRenderState)t).animateTail;
        this.headParts.xRot = 0.5235988f + f2;
        this.headParts.yRot = f * ((float)Math.PI / 180);
        float f9 = ((EquineRenderState)t).isInWater ? 0.2f : 1.0f;
        float f10 = Mth.cos(f9 * f4 * 0.6662f + (float)Math.PI);
        float f11 = f10 * 0.8f * f3;
        float f12 = (1.0f - Math.max(f6, f5)) * (0.5235988f + f2 + f8 * Mth.sin(((EquineRenderState)t).ageInTicks) * 0.05f);
        this.headParts.xRot = f6 * (0.2617994f + f2) + f5 * (2.1816616f + Mth.sin(((EquineRenderState)t).ageInTicks) * 0.05f) + f12;
        this.headParts.yRot = f6 * f * ((float)Math.PI / 180) + (1.0f - Math.max(f6, f5)) * this.headParts.yRot;
        float f13 = ((EquineRenderState)t).ageScale;
        this.headParts.y += Mth.lerp(f5, Mth.lerp(f6, 0.0f, -8.0f * f13), 7.0f * f13);
        this.headParts.z = Mth.lerp(f6, this.headParts.z, -4.0f * f13);
        this.body.xRot = f6 * -0.7853982f + f7 * this.body.xRot;
        float f14 = 0.2617994f * f6;
        float f15 = Mth.cos(((EquineRenderState)t).ageInTicks * 0.6f + (float)Math.PI);
        this.leftFrontLeg.y -= 12.0f * f13 * f6;
        this.leftFrontLeg.z += 4.0f * f13 * f6;
        this.rightFrontLeg.y = this.leftFrontLeg.y;
        this.rightFrontLeg.z = this.leftFrontLeg.z;
        float f16 = (-1.0471976f + f15) * f6 + f11 * f7;
        float f17 = (-1.0471976f - f15) * f6 - f11 * f7;
        this.leftHindLeg.xRot = f14 - f10 * 0.5f * f3 * f7;
        this.rightHindLeg.xRot = f14 + f10 * 0.5f * f3 * f7;
        this.leftFrontLeg.xRot = f16;
        this.rightFrontLeg.xRot = f17;
        this.tail.xRot = 0.5235988f + f3 * 0.75f;
        this.tail.y += f3 * f13;
        this.tail.z += f3 * 2.0f * f13;
        this.tail.yRot = bl ? Mth.cos(((EquineRenderState)t).ageInTicks * 0.7f) : 0.0f;
    }
}

