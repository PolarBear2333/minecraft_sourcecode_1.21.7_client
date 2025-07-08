/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.Int2IntFunction
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import java.util.Set;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BrightnessCombiner;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

public class BedRenderer
implements BlockEntityRenderer<BedBlockEntity> {
    private final Model headModel;
    private final Model footModel;

    public BedRenderer(BlockEntityRendererProvider.Context context) {
        this(context.getModelSet());
    }

    public BedRenderer(EntityModelSet entityModelSet) {
        this.headModel = new Model.Simple(entityModelSet.bakeLayer(ModelLayers.BED_HEAD), RenderType::entitySolid);
        this.footModel = new Model.Simple(entityModelSet.bakeLayer(ModelLayers.BED_FOOT), RenderType::entitySolid);
    }

    public static LayerDefinition createHeadLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 16.0f, 16.0f, 6.0f), PartPose.ZERO);
        partDefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(50, 6).addBox(0.0f, 6.0f, 0.0f, 3.0f, 3.0f, 3.0f), PartPose.rotation(1.5707964f, 0.0f, 1.5707964f));
        partDefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(50, 18).addBox(-16.0f, 6.0f, 0.0f, 3.0f, 3.0f, 3.0f), PartPose.rotation(1.5707964f, 0.0f, (float)Math.PI));
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    public static LayerDefinition createFootLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 22).addBox(0.0f, 0.0f, 0.0f, 16.0f, 16.0f, 6.0f), PartPose.ZERO);
        partDefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(50, 0).addBox(0.0f, 6.0f, -16.0f, 3.0f, 3.0f, 3.0f), PartPose.rotation(1.5707964f, 0.0f, 0.0f));
        partDefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(50, 12).addBox(-16.0f, 6.0f, -16.0f, 3.0f, 3.0f, 3.0f), PartPose.rotation(1.5707964f, 0.0f, 4.712389f));
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void render(BedBlockEntity bedBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2, Vec3 vec3) {
        Level level = bedBlockEntity.getLevel();
        if (level != null) {
            Material material = Sheets.getBedMaterial(bedBlockEntity.getColor());
            BlockState blockState = bedBlockEntity.getBlockState();
            DoubleBlockCombiner.NeighborCombineResult<BedBlockEntity> neighborCombineResult = DoubleBlockCombiner.combineWithNeigbour(BlockEntityType.BED, BedBlock::getBlockType, BedBlock::getConnectedDirection, ChestBlock.FACING, blockState, level, bedBlockEntity.getBlockPos(), (levelAccessor, blockPos) -> false);
            int n3 = ((Int2IntFunction)neighborCombineResult.apply(new BrightnessCombiner())).get(n);
            this.renderPiece(poseStack, multiBufferSource, blockState.getValue(BedBlock.PART) == BedPart.HEAD ? this.headModel : this.footModel, (Direction)blockState.getValue(BedBlock.FACING), material, n3, n2, false);
        }
    }

    public void renderInHand(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2, Material material) {
        this.renderPiece(poseStack, multiBufferSource, this.headModel, Direction.SOUTH, material, n, n2, false);
        this.renderPiece(poseStack, multiBufferSource, this.footModel, Direction.SOUTH, material, n, n2, true);
    }

    private void renderPiece(PoseStack poseStack, MultiBufferSource multiBufferSource, Model model, Direction direction, Material material, int n, int n2, boolean bl) {
        poseStack.pushPose();
        BedRenderer.preparePose(poseStack, bl, direction);
        VertexConsumer vertexConsumer = material.buffer(multiBufferSource, RenderType::entitySolid);
        model.renderToBuffer(poseStack, vertexConsumer, n, n2);
        poseStack.popPose();
    }

    private static void preparePose(PoseStack poseStack, boolean bl, Direction direction) {
        poseStack.translate(0.0f, 0.5625f, bl ? -1.0f : 0.0f);
        poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(90.0f));
        poseStack.translate(0.5f, 0.5f, 0.5f);
        poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(180.0f + direction.toYRot()));
        poseStack.translate(-0.5f, -0.5f, -0.5f);
    }

    public void getExtents(Set<Vector3f> set) {
        PoseStack poseStack = new PoseStack();
        BedRenderer.preparePose(poseStack, false, Direction.SOUTH);
        this.headModel.root().getExtentsForGui(poseStack, set);
        poseStack.setIdentity();
        BedRenderer.preparePose(poseStack, true, Direction.SOUTH);
        this.footModel.root().getExtentsForGui(poseStack, set);
    }
}

