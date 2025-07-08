/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MaterialMapper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class ConduitRenderer
implements BlockEntityRenderer<ConduitBlockEntity> {
    public static final MaterialMapper MAPPER = new MaterialMapper(TextureAtlas.LOCATION_BLOCKS, "entity/conduit");
    public static final Material SHELL_TEXTURE = MAPPER.defaultNamespaceApply("base");
    public static final Material ACTIVE_SHELL_TEXTURE = MAPPER.defaultNamespaceApply("cage");
    public static final Material WIND_TEXTURE = MAPPER.defaultNamespaceApply("wind");
    public static final Material VERTICAL_WIND_TEXTURE = MAPPER.defaultNamespaceApply("wind_vertical");
    public static final Material OPEN_EYE_TEXTURE = MAPPER.defaultNamespaceApply("open_eye");
    public static final Material CLOSED_EYE_TEXTURE = MAPPER.defaultNamespaceApply("closed_eye");
    private final ModelPart eye;
    private final ModelPart wind;
    private final ModelPart shell;
    private final ModelPart cage;
    private final BlockEntityRenderDispatcher renderer;

    public ConduitRenderer(BlockEntityRendererProvider.Context context) {
        this.renderer = context.getBlockEntityRenderDispatcher();
        this.eye = context.bakeLayer(ModelLayers.CONDUIT_EYE);
        this.wind = context.bakeLayer(ModelLayers.CONDUIT_WIND);
        this.shell = context.bakeLayer(ModelLayers.CONDUIT_SHELL);
        this.cage = context.bakeLayer(ModelLayers.CONDUIT_CAGE);
    }

    public static LayerDefinition createEyeLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("eye", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -4.0f, 0.0f, 8.0f, 8.0f, 0.0f, new CubeDeformation(0.01f)), PartPose.ZERO);
        return LayerDefinition.create(meshDefinition, 16, 16);
    }

    public static LayerDefinition createWindLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("wind", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0f, -8.0f, -8.0f, 16.0f, 16.0f, 16.0f), PartPose.ZERO);
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    public static LayerDefinition createShellLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("shell", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0f, -3.0f, -3.0f, 6.0f, 6.0f, 6.0f), PartPose.ZERO);
        return LayerDefinition.create(meshDefinition, 32, 16);
    }

    public static LayerDefinition createCageLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("shell", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -4.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.ZERO);
        return LayerDefinition.create(meshDefinition, 32, 16);
    }

    @Override
    public void render(ConduitBlockEntity conduitBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2, Vec3 vec3) {
        float f2 = (float)conduitBlockEntity.tickCount + f;
        if (!conduitBlockEntity.isActive()) {
            float f3 = conduitBlockEntity.getActiveRotation(0.0f);
            VertexConsumer vertexConsumer = SHELL_TEXTURE.buffer(multiBufferSource, RenderType::entitySolid);
            poseStack.pushPose();
            poseStack.translate(0.5f, 0.5f, 0.5f);
            poseStack.mulPose((Quaternionfc)new Quaternionf().rotationY(f3 * ((float)Math.PI / 180)));
            this.shell.render(poseStack, vertexConsumer, n, n2);
            poseStack.popPose();
            return;
        }
        float f4 = conduitBlockEntity.getActiveRotation(f) * 57.295776f;
        float f5 = Mth.sin(f2 * 0.1f) / 2.0f + 0.5f;
        f5 = f5 * f5 + f5;
        poseStack.pushPose();
        poseStack.translate(0.5f, 0.3f + f5 * 0.2f, 0.5f);
        Vector3f vector3f = new Vector3f(0.5f, 1.0f, 0.5f).normalize();
        poseStack.mulPose((Quaternionfc)new Quaternionf().rotationAxis(f4 * ((float)Math.PI / 180), (Vector3fc)vector3f));
        this.cage.render(poseStack, ACTIVE_SHELL_TEXTURE.buffer(multiBufferSource, RenderType::entityCutoutNoCull), n, n2);
        poseStack.popPose();
        int n3 = conduitBlockEntity.tickCount / 66 % 3;
        poseStack.pushPose();
        poseStack.translate(0.5f, 0.5f, 0.5f);
        if (n3 == 1) {
            poseStack.mulPose((Quaternionfc)new Quaternionf().rotationX(1.5707964f));
        } else if (n3 == 2) {
            poseStack.mulPose((Quaternionfc)new Quaternionf().rotationZ(1.5707964f));
        }
        VertexConsumer vertexConsumer = (n3 == 1 ? VERTICAL_WIND_TEXTURE : WIND_TEXTURE).buffer(multiBufferSource, RenderType::entityCutoutNoCull);
        this.wind.render(poseStack, vertexConsumer, n, n2);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.5f, 0.5f, 0.5f);
        poseStack.scale(0.875f, 0.875f, 0.875f);
        poseStack.mulPose((Quaternionfc)new Quaternionf().rotationXYZ((float)Math.PI, 0.0f, (float)Math.PI));
        this.wind.render(poseStack, vertexConsumer, n, n2);
        poseStack.popPose();
        Camera camera = this.renderer.camera;
        poseStack.pushPose();
        poseStack.translate(0.5f, 0.3f + f5 * 0.2f, 0.5f);
        poseStack.scale(0.5f, 0.5f, 0.5f);
        float f6 = -camera.getYRot();
        poseStack.mulPose((Quaternionfc)new Quaternionf().rotationYXZ(f6 * ((float)Math.PI / 180), camera.getXRot() * ((float)Math.PI / 180), (float)Math.PI));
        float f7 = 1.3333334f;
        poseStack.scale(1.3333334f, 1.3333334f, 1.3333334f);
        this.eye.render(poseStack, (conduitBlockEntity.isHunting() ? OPEN_EYE_TEXTURE : CLOSED_EYE_TEXTURE).buffer(multiBufferSource, RenderType::entityCutoutNoCull), n, n2);
        poseStack.popPose();
    }
}

