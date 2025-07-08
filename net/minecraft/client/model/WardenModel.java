/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 */
package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.animation.definitions.WardenAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.WardenRenderState;
import net.minecraft.util.Mth;

public class WardenModel
extends EntityModel<WardenRenderState> {
    private static final float DEFAULT_ARM_X_Y = 13.0f;
    private static final float DEFAULT_ARM_Z = 1.0f;
    protected final ModelPart bone;
    protected final ModelPart body;
    protected final ModelPart head;
    protected final ModelPart rightTendril;
    protected final ModelPart leftTendril;
    protected final ModelPart leftLeg;
    protected final ModelPart leftArm;
    protected final ModelPart leftRibcage;
    protected final ModelPart rightArm;
    protected final ModelPart rightLeg;
    protected final ModelPart rightRibcage;
    private final List<ModelPart> tendrilsLayerModelParts;
    private final List<ModelPart> heartLayerModelParts;
    private final List<ModelPart> bioluminescentLayerModelParts;
    private final List<ModelPart> pulsatingSpotsLayerModelParts;
    private final KeyframeAnimation attackAnimation;
    private final KeyframeAnimation sonicBoomAnimation;
    private final KeyframeAnimation diggingAnimation;
    private final KeyframeAnimation emergeAnimation;
    private final KeyframeAnimation roarAnimation;
    private final KeyframeAnimation sniffAnimation;

    public WardenModel(ModelPart modelPart) {
        super(modelPart, RenderType::entityCutoutNoCull);
        this.bone = modelPart.getChild("bone");
        this.body = this.bone.getChild("body");
        this.head = this.body.getChild("head");
        this.rightLeg = this.bone.getChild("right_leg");
        this.leftLeg = this.bone.getChild("left_leg");
        this.rightArm = this.body.getChild("right_arm");
        this.leftArm = this.body.getChild("left_arm");
        this.rightTendril = this.head.getChild("right_tendril");
        this.leftTendril = this.head.getChild("left_tendril");
        this.rightRibcage = this.body.getChild("right_ribcage");
        this.leftRibcage = this.body.getChild("left_ribcage");
        this.tendrilsLayerModelParts = ImmutableList.of((Object)this.leftTendril, (Object)this.rightTendril);
        this.heartLayerModelParts = ImmutableList.of((Object)this.body);
        this.bioluminescentLayerModelParts = ImmutableList.of((Object)this.head, (Object)this.leftArm, (Object)this.rightArm, (Object)this.leftLeg, (Object)this.rightLeg);
        this.pulsatingSpotsLayerModelParts = ImmutableList.of((Object)this.body, (Object)this.head, (Object)this.leftArm, (Object)this.rightArm, (Object)this.leftLeg, (Object)this.rightLeg);
        this.attackAnimation = WardenAnimation.WARDEN_ATTACK.bake(modelPart);
        this.sonicBoomAnimation = WardenAnimation.WARDEN_SONIC_BOOM.bake(modelPart);
        this.diggingAnimation = WardenAnimation.WARDEN_DIG.bake(modelPart);
        this.emergeAnimation = WardenAnimation.WARDEN_EMERGE.bake(modelPart);
        this.roarAnimation = WardenAnimation.WARDEN_ROAR.bake(modelPart);
        this.sniffAnimation = WardenAnimation.WARDEN_SNIFF.bake(modelPart);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0f, 24.0f, 0.0f));
        PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-9.0f, -13.0f, -4.0f, 18.0f, 21.0f, 11.0f), PartPose.offset(0.0f, -21.0f, 0.0f));
        partDefinition3.addOrReplaceChild("right_ribcage", CubeListBuilder.create().texOffs(90, 11).addBox(-2.0f, -11.0f, -0.1f, 9.0f, 21.0f, 0.0f), PartPose.offset(-7.0f, -2.0f, -4.0f));
        partDefinition3.addOrReplaceChild("left_ribcage", CubeListBuilder.create().texOffs(90, 11).mirror().addBox(-7.0f, -11.0f, -0.1f, 9.0f, 21.0f, 0.0f).mirror(false), PartPose.offset(7.0f, -2.0f, -4.0f));
        PartDefinition partDefinition4 = partDefinition3.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 32).addBox(-8.0f, -16.0f, -5.0f, 16.0f, 16.0f, 10.0f), PartPose.offset(0.0f, -13.0f, 0.0f));
        partDefinition4.addOrReplaceChild("right_tendril", CubeListBuilder.create().texOffs(52, 32).addBox(-16.0f, -13.0f, 0.0f, 16.0f, 16.0f, 0.0f), PartPose.offset(-8.0f, -12.0f, 0.0f));
        partDefinition4.addOrReplaceChild("left_tendril", CubeListBuilder.create().texOffs(58, 0).addBox(0.0f, -13.0f, 0.0f, 16.0f, 16.0f, 0.0f), PartPose.offset(8.0f, -12.0f, 0.0f));
        partDefinition3.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(44, 50).addBox(-4.0f, 0.0f, -4.0f, 8.0f, 28.0f, 8.0f), PartPose.offset(-13.0f, -13.0f, 1.0f));
        partDefinition3.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 58).addBox(-4.0f, 0.0f, -4.0f, 8.0f, 28.0f, 8.0f), PartPose.offset(13.0f, -13.0f, 1.0f));
        partDefinition2.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(76, 48).addBox(-3.1f, 0.0f, -3.0f, 6.0f, 13.0f, 6.0f), PartPose.offset(-5.9f, -13.0f, 0.0f));
        partDefinition2.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(76, 76).addBox(-2.9f, 0.0f, -3.0f, 6.0f, 13.0f, 6.0f), PartPose.offset(5.9f, -13.0f, 0.0f));
        return LayerDefinition.create(meshDefinition, 128, 128);
    }

    @Override
    public void setupAnim(WardenRenderState wardenRenderState) {
        super.setupAnim(wardenRenderState);
        this.animateHeadLookTarget(wardenRenderState.yRot, wardenRenderState.xRot);
        this.animateWalk(wardenRenderState.walkAnimationPos, wardenRenderState.walkAnimationSpeed);
        this.animateIdlePose(wardenRenderState.ageInTicks);
        this.animateTendrils(wardenRenderState, wardenRenderState.ageInTicks);
        this.attackAnimation.apply(wardenRenderState.attackAnimationState, wardenRenderState.ageInTicks);
        this.sonicBoomAnimation.apply(wardenRenderState.sonicBoomAnimationState, wardenRenderState.ageInTicks);
        this.diggingAnimation.apply(wardenRenderState.diggingAnimationState, wardenRenderState.ageInTicks);
        this.emergeAnimation.apply(wardenRenderState.emergeAnimationState, wardenRenderState.ageInTicks);
        this.roarAnimation.apply(wardenRenderState.roarAnimationState, wardenRenderState.ageInTicks);
        this.sniffAnimation.apply(wardenRenderState.sniffAnimationState, wardenRenderState.ageInTicks);
    }

    private void animateHeadLookTarget(float f, float f2) {
        this.head.xRot = f2 * ((float)Math.PI / 180);
        this.head.yRot = f * ((float)Math.PI / 180);
    }

    private void animateIdlePose(float f) {
        float f2 = f * 0.1f;
        float f3 = Mth.cos(f2);
        float f4 = Mth.sin(f2);
        this.head.zRot += 0.06f * f3;
        this.head.xRot += 0.06f * f4;
        this.body.zRot += 0.025f * f4;
        this.body.xRot += 0.025f * f3;
    }

    private void animateWalk(float f, float f2) {
        float f3 = Math.min(0.5f, 3.0f * f2);
        float f4 = f * 0.8662f;
        float f5 = Mth.cos(f4);
        float f6 = Mth.sin(f4);
        float f7 = Math.min(0.35f, f3);
        this.head.zRot += 0.3f * f6 * f3;
        this.head.xRot += 1.2f * Mth.cos(f4 + 1.5707964f) * f7;
        this.body.zRot = 0.1f * f6 * f3;
        this.body.xRot = 1.0f * f5 * f7;
        this.leftLeg.xRot = 1.0f * f5 * f3;
        this.rightLeg.xRot = 1.0f * Mth.cos(f4 + (float)Math.PI) * f3;
        this.leftArm.xRot = -(0.8f * f5 * f3);
        this.leftArm.zRot = 0.0f;
        this.rightArm.xRot = -(0.8f * f6 * f3);
        this.rightArm.zRot = 0.0f;
        this.resetArmPoses();
    }

    private void resetArmPoses() {
        this.leftArm.yRot = 0.0f;
        this.leftArm.z = 1.0f;
        this.leftArm.x = 13.0f;
        this.leftArm.y = -13.0f;
        this.rightArm.yRot = 0.0f;
        this.rightArm.z = 1.0f;
        this.rightArm.x = -13.0f;
        this.rightArm.y = -13.0f;
    }

    private void animateTendrils(WardenRenderState wardenRenderState, float f) {
        float f2;
        this.leftTendril.xRot = f2 = wardenRenderState.tendrilAnimation * (float)(Math.cos((double)f * 2.25) * Math.PI * (double)0.1f);
        this.rightTendril.xRot = -f2;
    }

    public List<ModelPart> getTendrilsLayerModelParts(WardenRenderState wardenRenderState) {
        return this.tendrilsLayerModelParts;
    }

    public List<ModelPart> getHeartLayerModelParts(WardenRenderState wardenRenderState) {
        return this.heartLayerModelParts;
    }

    public List<ModelPart> getBioluminescentLayerModelParts(WardenRenderState wardenRenderState) {
        return this.bioluminescentLayerModelParts;
    }

    public List<ModelPart> getPulsatingSpotsLayerModelParts(WardenRenderState wardenRenderState) {
        return this.pulsatingSpotsLayerModelParts;
    }
}

