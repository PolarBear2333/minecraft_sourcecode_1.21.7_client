/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;

public class LecternRenderer
implements BlockEntityRenderer<LecternBlockEntity> {
    private final BookModel bookModel;

    public LecternRenderer(BlockEntityRendererProvider.Context context) {
        this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
    }

    @Override
    public void render(LecternBlockEntity lecternBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2, Vec3 vec3) {
        BlockState blockState = lecternBlockEntity.getBlockState();
        if (!blockState.getValue(LecternBlock.HAS_BOOK).booleanValue()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5f, 1.0625f, 0.5f);
        float f2 = blockState.getValue(LecternBlock.FACING).getClockWise().toYRot();
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(-f2));
        poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(67.5f));
        poseStack.translate(0.0f, -0.125f, 0.0f);
        this.bookModel.setupAnim(0.0f, 0.1f, 0.9f, 1.2f);
        VertexConsumer vertexConsumer = EnchantTableRenderer.BOOK_LOCATION.buffer(multiBufferSource, RenderType::entitySolid);
        this.bookModel.renderToBuffer(poseStack, vertexConsumer, n, n2);
        poseStack.popPose();
    }
}

