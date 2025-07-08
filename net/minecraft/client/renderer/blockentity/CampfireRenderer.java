/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;

public class CampfireRenderer
implements BlockEntityRenderer<CampfireBlockEntity> {
    private static final float SIZE = 0.375f;
    private final ItemRenderer itemRenderer;

    public CampfireRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(CampfireBlockEntity campfireBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2, Vec3 vec3) {
        Direction direction = campfireBlockEntity.getBlockState().getValue(CampfireBlock.FACING);
        NonNullList<ItemStack> nonNullList = campfireBlockEntity.getItems();
        int n3 = (int)campfireBlockEntity.getBlockPos().asLong();
        for (int i = 0; i < nonNullList.size(); ++i) {
            ItemStack itemStack = nonNullList.get(i);
            if (itemStack == ItemStack.EMPTY) continue;
            poseStack.pushPose();
            poseStack.translate(0.5f, 0.44921875f, 0.5f);
            Direction direction2 = Direction.from2DDataValue((i + direction.get2DDataValue()) % 4);
            float f2 = -direction2.toYRot();
            poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(f2));
            poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(90.0f));
            poseStack.translate(-0.3125f, -0.3125f, 0.0f);
            poseStack.scale(0.375f, 0.375f, 0.375f);
            this.itemRenderer.renderStatic(itemStack, ItemDisplayContext.FIXED, n, n2, poseStack, multiBufferSource, campfireBlockEntity.getLevel(), n3 + i);
            poseStack.popPose();
        }
    }
}

