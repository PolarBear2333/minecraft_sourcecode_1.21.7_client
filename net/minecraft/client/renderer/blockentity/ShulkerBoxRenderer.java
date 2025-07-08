/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Set;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

public class ShulkerBoxRenderer
implements BlockEntityRenderer<ShulkerBoxBlockEntity> {
    private final ShulkerBoxModel model;

    public ShulkerBoxRenderer(BlockEntityRendererProvider.Context context) {
        this(context.getModelSet());
    }

    public ShulkerBoxRenderer(EntityModelSet entityModelSet) {
        this.model = new ShulkerBoxModel(entityModelSet.bakeLayer(ModelLayers.SHULKER_BOX));
    }

    @Override
    public void render(ShulkerBoxBlockEntity shulkerBoxBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2, Vec3 vec3) {
        Direction direction = shulkerBoxBlockEntity.getBlockState().getValueOrElse(ShulkerBoxBlock.FACING, Direction.UP);
        DyeColor dyeColor = shulkerBoxBlockEntity.getColor();
        Material material = dyeColor == null ? Sheets.DEFAULT_SHULKER_TEXTURE_LOCATION : Sheets.getShulkerBoxMaterial(dyeColor);
        float f2 = shulkerBoxBlockEntity.getProgress(f);
        this.render(poseStack, multiBufferSource, n, n2, direction, f2, material);
    }

    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2, Direction direction, float f, Material material) {
        poseStack.pushPose();
        this.prepareModel(poseStack, direction, f);
        VertexConsumer vertexConsumer = material.buffer(multiBufferSource, this.model::renderType);
        this.model.renderToBuffer(poseStack, vertexConsumer, n, n2);
        poseStack.popPose();
    }

    private void prepareModel(PoseStack poseStack, Direction direction, float f) {
        poseStack.translate(0.5f, 0.5f, 0.5f);
        float f2 = 0.9995f;
        poseStack.scale(0.9995f, 0.9995f, 0.9995f);
        poseStack.mulPose((Quaternionfc)direction.getRotation());
        poseStack.scale(1.0f, -1.0f, -1.0f);
        poseStack.translate(0.0f, -1.0f, 0.0f);
        this.model.animate(f);
    }

    public void getExtents(Direction direction, float f, Set<Vector3f> set) {
        PoseStack poseStack = new PoseStack();
        this.prepareModel(poseStack, direction, f);
        this.model.root().getExtentsForGui(poseStack, set);
    }

    static class ShulkerBoxModel
    extends Model {
        private final ModelPart lid;

        public ShulkerBoxModel(ModelPart modelPart) {
            super(modelPart, RenderType::entityCutoutNoCull);
            this.lid = modelPart.getChild("lid");
        }

        public void animate(float f) {
            this.lid.setPos(0.0f, 24.0f - f * 0.5f * 16.0f, 0.0f);
            this.lid.yRot = 270.0f * f * ((float)Math.PI / 180);
        }
    }
}

