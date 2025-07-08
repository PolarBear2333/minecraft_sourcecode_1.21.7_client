/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.GuardianRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class GuardianModel
extends EntityModel<GuardianRenderState> {
    public static final MeshTransformer ELDER_GUARDIAN_SCALE = MeshTransformer.scaling(2.35f);
    private static final float[] SPIKE_X_ROT = new float[]{1.75f, 0.25f, 0.0f, 0.0f, 0.5f, 0.5f, 0.5f, 0.5f, 1.25f, 0.75f, 0.0f, 0.0f};
    private static final float[] SPIKE_Y_ROT = new float[]{0.0f, 0.0f, 0.0f, 0.0f, 0.25f, 1.75f, 1.25f, 0.75f, 0.0f, 0.0f, 0.0f, 0.0f};
    private static final float[] SPIKE_Z_ROT = new float[]{0.0f, 0.0f, 0.25f, 1.75f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.75f, 1.25f};
    private static final float[] SPIKE_X = new float[]{0.0f, 0.0f, 8.0f, -8.0f, -8.0f, 8.0f, 8.0f, -8.0f, 0.0f, 0.0f, 8.0f, -8.0f};
    private static final float[] SPIKE_Y = new float[]{-8.0f, -8.0f, -8.0f, -8.0f, 0.0f, 0.0f, 0.0f, 0.0f, 8.0f, 8.0f, 8.0f, 8.0f};
    private static final float[] SPIKE_Z = new float[]{8.0f, -8.0f, 0.0f, 0.0f, -8.0f, -8.0f, 8.0f, 8.0f, 8.0f, -8.0f, 0.0f, 0.0f};
    private static final String EYE = "eye";
    private static final String TAIL_0 = "tail0";
    private static final String TAIL_1 = "tail1";
    private static final String TAIL_2 = "tail2";
    private final ModelPart head;
    private final ModelPart eye;
    private final ModelPart[] spikeParts = new ModelPart[12];
    private final ModelPart[] tailParts;

    public GuardianModel(ModelPart modelPart) {
        super(modelPart);
        this.head = modelPart.getChild("head");
        for (int i = 0; i < this.spikeParts.length; ++i) {
            this.spikeParts[i] = this.head.getChild(GuardianModel.createSpikeName(i));
        }
        this.eye = this.head.getChild(EYE);
        this.tailParts = new ModelPart[3];
        this.tailParts[0] = this.head.getChild(TAIL_0);
        this.tailParts[1] = this.tailParts[0].getChild(TAIL_1);
        this.tailParts[2] = this.tailParts[1].getChild(TAIL_2);
    }

    private static String createSpikeName(int n) {
        return "spike" + n;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0f, 10.0f, -8.0f, 12.0f, 12.0f, 16.0f).texOffs(0, 28).addBox(-8.0f, 10.0f, -6.0f, 2.0f, 12.0f, 12.0f).texOffs(0, 28).addBox(6.0f, 10.0f, -6.0f, 2.0f, 12.0f, 12.0f, true).texOffs(16, 40).addBox(-6.0f, 8.0f, -6.0f, 12.0f, 2.0f, 12.0f).texOffs(16, 40).addBox(-6.0f, 22.0f, -6.0f, 12.0f, 2.0f, 12.0f), PartPose.ZERO);
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(0, 0).addBox(-1.0f, -4.5f, -1.0f, 2.0f, 9.0f, 2.0f);
        for (int i = 0; i < 12; ++i) {
            float f = GuardianModel.getSpikeX(i, 0.0f, 0.0f);
            float f2 = GuardianModel.getSpikeY(i, 0.0f, 0.0f);
            float f3 = GuardianModel.getSpikeZ(i, 0.0f, 0.0f);
            float f4 = (float)Math.PI * SPIKE_X_ROT[i];
            float f5 = (float)Math.PI * SPIKE_Y_ROT[i];
            float f6 = (float)Math.PI * SPIKE_Z_ROT[i];
            partDefinition2.addOrReplaceChild(GuardianModel.createSpikeName(i), cubeListBuilder, PartPose.offsetAndRotation(f, f2, f3, f4, f5, f6));
        }
        partDefinition2.addOrReplaceChild(EYE, CubeListBuilder.create().texOffs(8, 0).addBox(-1.0f, 15.0f, 0.0f, 2.0f, 2.0f, 1.0f), PartPose.offset(0.0f, 0.0f, -8.25f));
        PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild(TAIL_0, CubeListBuilder.create().texOffs(40, 0).addBox(-2.0f, 14.0f, 7.0f, 4.0f, 4.0f, 8.0f), PartPose.ZERO);
        PartDefinition partDefinition4 = partDefinition3.addOrReplaceChild(TAIL_1, CubeListBuilder.create().texOffs(0, 54).addBox(0.0f, 14.0f, 0.0f, 3.0f, 3.0f, 7.0f), PartPose.offset(-1.5f, 0.5f, 14.0f));
        partDefinition4.addOrReplaceChild(TAIL_2, CubeListBuilder.create().texOffs(41, 32).addBox(0.0f, 14.0f, 0.0f, 2.0f, 2.0f, 6.0f).texOffs(25, 19).addBox(1.0f, 10.5f, 3.0f, 1.0f, 9.0f, 9.0f), PartPose.offset(0.5f, 0.5f, 6.0f));
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    public static LayerDefinition createElderGuardianLayer() {
        return GuardianModel.createBodyLayer().apply(ELDER_GUARDIAN_SCALE);
    }

    @Override
    public void setupAnim(GuardianRenderState guardianRenderState) {
        super.setupAnim(guardianRenderState);
        this.head.yRot = guardianRenderState.yRot * ((float)Math.PI / 180);
        this.head.xRot = guardianRenderState.xRot * ((float)Math.PI / 180);
        float f = (1.0f - guardianRenderState.spikesAnimation) * 0.55f;
        this.setupSpikes(guardianRenderState.ageInTicks, f);
        if (guardianRenderState.lookAtPosition != null && guardianRenderState.lookDirection != null) {
            double d = guardianRenderState.lookAtPosition.y - guardianRenderState.eyePosition.y;
            this.eye.y = d > 0.0 ? 0.0f : 1.0f;
            Vec3 vec3 = guardianRenderState.lookDirection;
            vec3 = new Vec3(vec3.x, 0.0, vec3.z);
            Vec3 vec32 = new Vec3(guardianRenderState.eyePosition.x - guardianRenderState.lookAtPosition.x, 0.0, guardianRenderState.eyePosition.z - guardianRenderState.lookAtPosition.z).normalize().yRot(1.5707964f);
            double d2 = vec3.dot(vec32);
            this.eye.x = Mth.sqrt((float)Math.abs(d2)) * 2.0f * (float)Math.signum(d2);
        }
        this.eye.visible = true;
        float f2 = guardianRenderState.tailAnimation;
        this.tailParts[0].yRot = Mth.sin(f2) * (float)Math.PI * 0.05f;
        this.tailParts[1].yRot = Mth.sin(f2) * (float)Math.PI * 0.1f;
        this.tailParts[2].yRot = Mth.sin(f2) * (float)Math.PI * 0.15f;
    }

    private void setupSpikes(float f, float f2) {
        for (int i = 0; i < 12; ++i) {
            this.spikeParts[i].x = GuardianModel.getSpikeX(i, f, f2);
            this.spikeParts[i].y = GuardianModel.getSpikeY(i, f, f2);
            this.spikeParts[i].z = GuardianModel.getSpikeZ(i, f, f2);
        }
    }

    private static float getSpikeOffset(int n, float f, float f2) {
        return 1.0f + Mth.cos(f * 1.5f + (float)n) * 0.01f - f2;
    }

    private static float getSpikeX(int n, float f, float f2) {
        return SPIKE_X[n] * GuardianModel.getSpikeOffset(n, f, f2);
    }

    private static float getSpikeY(int n, float f, float f2) {
        return 16.0f + SPIKE_Y[n] * GuardianModel.getSpikeOffset(n, f, f2);
    }

    private static float getSpikeZ(int n, float f, float f2) {
        return SPIKE_Z[n] * GuardianModel.getSpikeOffset(n, f, f2);
    }
}

