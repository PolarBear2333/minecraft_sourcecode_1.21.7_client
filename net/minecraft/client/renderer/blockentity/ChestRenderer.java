/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.Int2IntFunction
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import java.util.Calendar;
import net.minecraft.client.model.ChestModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BrightnessCombiner;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;

public class ChestRenderer<T extends BlockEntity>
implements BlockEntityRenderer<T> {
    private final ChestModel singleModel;
    private final ChestModel doubleLeftModel;
    private final ChestModel doubleRightModel;
    private final boolean xmasTextures = ChestRenderer.xmasTextures();

    public ChestRenderer(BlockEntityRendererProvider.Context context) {
        this.singleModel = new ChestModel(context.bakeLayer(ModelLayers.CHEST));
        this.doubleLeftModel = new ChestModel(context.bakeLayer(ModelLayers.DOUBLE_CHEST_LEFT));
        this.doubleRightModel = new ChestModel(context.bakeLayer(ModelLayers.DOUBLE_CHEST_RIGHT));
    }

    public static boolean xmasTextures() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(2) + 1 == 12 && calendar.get(5) >= 24 && calendar.get(5) <= 26;
    }

    @Override
    public void render(T t, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2, Vec3 vec3) {
        Level level = ((BlockEntity)t).getLevel();
        boolean bl = level != null;
        BlockState blockState = bl ? ((BlockEntity)t).getBlockState() : (BlockState)Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH);
        ChestType chestType = blockState.hasProperty(ChestBlock.TYPE) ? blockState.getValue(ChestBlock.TYPE) : ChestType.SINGLE;
        Block block = blockState.getBlock();
        if (!(block instanceof AbstractChestBlock)) {
            return;
        }
        AbstractChestBlock abstractChestBlock = (AbstractChestBlock)block;
        boolean bl2 = chestType != ChestType.SINGLE;
        poseStack.pushPose();
        float f2 = blockState.getValue(ChestBlock.FACING).toYRot();
        poseStack.translate(0.5f, 0.5f, 0.5f);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(-f2));
        poseStack.translate(-0.5f, -0.5f, -0.5f);
        DoubleBlockCombiner.NeighborCombineResult<Object> neighborCombineResult = bl ? abstractChestBlock.combine(blockState, level, ((BlockEntity)t).getBlockPos(), true) : DoubleBlockCombiner.Combiner::acceptNone;
        float f3 = neighborCombineResult.apply(ChestBlock.opennessCombiner((LidBlockEntity)t)).get(f);
        f3 = 1.0f - f3;
        f3 = 1.0f - f3 * f3 * f3;
        int n3 = ((Int2IntFunction)neighborCombineResult.apply(new BrightnessCombiner())).applyAsInt(n);
        Material material = Sheets.chooseMaterial(t, chestType, this.xmasTextures);
        VertexConsumer vertexConsumer = material.buffer(multiBufferSource, RenderType::entityCutout);
        if (bl2) {
            if (chestType == ChestType.LEFT) {
                this.render(poseStack, vertexConsumer, this.doubleLeftModel, f3, n3, n2);
            } else {
                this.render(poseStack, vertexConsumer, this.doubleRightModel, f3, n3, n2);
            }
        } else {
            this.render(poseStack, vertexConsumer, this.singleModel, f3, n3, n2);
        }
        poseStack.popPose();
    }

    private void render(PoseStack poseStack, VertexConsumer vertexConsumer, ChestModel chestModel, float f, int n, int n2) {
        chestModel.setupAnim(f);
        chestModel.renderToBuffer(poseStack, vertexConsumer, n, n2);
    }
}

