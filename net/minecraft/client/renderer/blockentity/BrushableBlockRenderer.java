/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;

public class BrushableBlockRenderer
implements BlockEntityRenderer<BrushableBlockEntity> {
    private final ItemRenderer itemRenderer;

    public BrushableBlockRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(BrushableBlockEntity brushableBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2, Vec3 vec3) {
        if (brushableBlockEntity.getLevel() == null) {
            return;
        }
        int n3 = brushableBlockEntity.getBlockState().getValue(BlockStateProperties.DUSTED);
        if (n3 <= 0) {
            return;
        }
        Direction direction = brushableBlockEntity.getHitDirection();
        if (direction == null) {
            return;
        }
        ItemStack itemStack = brushableBlockEntity.getItem();
        if (itemStack.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.5f, 0.0f);
        float[] fArray = this.translations(direction, n3);
        poseStack.translate(fArray[0], fArray[1], fArray[2]);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(75.0f));
        boolean bl = direction == Direction.EAST || direction == Direction.WEST;
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees((bl ? 90 : 0) + 11));
        poseStack.scale(0.5f, 0.5f, 0.5f);
        int n4 = LevelRenderer.getLightColor(LevelRenderer.BrightnessGetter.DEFAULT, brushableBlockEntity.getLevel(), brushableBlockEntity.getBlockState(), brushableBlockEntity.getBlockPos().relative(direction));
        this.itemRenderer.renderStatic(itemStack, ItemDisplayContext.FIXED, n4, OverlayTexture.NO_OVERLAY, poseStack, multiBufferSource, brushableBlockEntity.getLevel(), 0);
        poseStack.popPose();
    }

    private float[] translations(Direction direction, int n) {
        float[] fArray = new float[]{0.5f, 0.0f, 0.5f};
        float f = (float)n / 10.0f * 0.75f;
        switch (direction) {
            case EAST: {
                fArray[0] = 0.73f + f;
                break;
            }
            case WEST: {
                fArray[0] = 0.25f - f;
                break;
            }
            case UP: {
                fArray[1] = 0.25f + f;
                break;
            }
            case DOWN: {
                fArray[1] = -0.23f - f;
                break;
            }
            case NORTH: {
                fArray[2] = 0.25f - f;
                break;
            }
            case SOUTH: {
                fArray[2] = 0.73f + f;
            }
        }
        return fArray;
    }
}

