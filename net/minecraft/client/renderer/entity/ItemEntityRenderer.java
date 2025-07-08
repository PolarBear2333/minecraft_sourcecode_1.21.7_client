/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.ItemClusterRenderState;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import org.joml.Quaternionfc;

public class ItemEntityRenderer
extends EntityRenderer<ItemEntity, ItemEntityRenderState> {
    private static final float ITEM_MIN_HOVER_HEIGHT = 0.0625f;
    private static final float ITEM_BUNDLE_OFFSET_SCALE = 0.15f;
    private static final float FLAT_ITEM_DEPTH_THRESHOLD = 0.0625f;
    private final ItemModelResolver itemModelResolver;
    private final RandomSource random = RandomSource.create();

    public ItemEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
        this.shadowRadius = 0.15f;
        this.shadowStrength = 0.75f;
    }

    @Override
    public ItemEntityRenderState createRenderState() {
        return new ItemEntityRenderState();
    }

    @Override
    public void extractRenderState(ItemEntity itemEntity, ItemEntityRenderState itemEntityRenderState, float f) {
        super.extractRenderState(itemEntity, itemEntityRenderState, f);
        itemEntityRenderState.ageInTicks = (float)itemEntity.getAge() + f;
        itemEntityRenderState.bobOffset = itemEntity.bobOffs;
        itemEntityRenderState.extractItemGroupRenderState(itemEntity, itemEntity.getItem(), this.itemModelResolver);
    }

    @Override
    public void render(ItemEntityRenderState itemEntityRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        if (itemEntityRenderState.item.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        AABB aABB = itemEntityRenderState.item.getModelBoundingBox();
        float f = -((float)aABB.minY) + 0.0625f;
        float f2 = Mth.sin(itemEntityRenderState.ageInTicks / 10.0f + itemEntityRenderState.bobOffset) * 0.1f + 0.1f;
        poseStack.translate(0.0f, f2 + f, 0.0f);
        float f3 = ItemEntity.getSpin(itemEntityRenderState.ageInTicks, itemEntityRenderState.bobOffset);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotation(f3));
        ItemEntityRenderer.renderMultipleFromCount(poseStack, multiBufferSource, n, itemEntityRenderState, this.random, aABB);
        poseStack.popPose();
        super.render(itemEntityRenderState, poseStack, multiBufferSource, n);
    }

    public static void renderMultipleFromCount(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, ItemClusterRenderState itemClusterRenderState, RandomSource randomSource) {
        ItemEntityRenderer.renderMultipleFromCount(poseStack, multiBufferSource, n, itemClusterRenderState, randomSource, itemClusterRenderState.item.getModelBoundingBox());
    }

    public static void renderMultipleFromCount(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, ItemClusterRenderState itemClusterRenderState, RandomSource randomSource, AABB aABB) {
        int n2 = itemClusterRenderState.count;
        if (n2 == 0) {
            return;
        }
        randomSource.setSeed(itemClusterRenderState.seed);
        ItemStackRenderState itemStackRenderState = itemClusterRenderState.item;
        float f = (float)aABB.getZsize();
        if (f > 0.0625f) {
            itemStackRenderState.render(poseStack, multiBufferSource, n, OverlayTexture.NO_OVERLAY);
            for (int i = 1; i < n2; ++i) {
                poseStack.pushPose();
                float f2 = (randomSource.nextFloat() * 2.0f - 1.0f) * 0.15f;
                float f3 = (randomSource.nextFloat() * 2.0f - 1.0f) * 0.15f;
                float f4 = (randomSource.nextFloat() * 2.0f - 1.0f) * 0.15f;
                poseStack.translate(f2, f3, f4);
                itemStackRenderState.render(poseStack, multiBufferSource, n, OverlayTexture.NO_OVERLAY);
                poseStack.popPose();
            }
        } else {
            float f5 = f * 1.5f;
            poseStack.translate(0.0f, 0.0f, -(f5 * (float)(n2 - 1) / 2.0f));
            itemStackRenderState.render(poseStack, multiBufferSource, n, OverlayTexture.NO_OVERLAY);
            poseStack.translate(0.0f, 0.0f, f5);
            for (int i = 1; i < n2; ++i) {
                poseStack.pushPose();
                float f6 = (randomSource.nextFloat() * 2.0f - 1.0f) * 0.15f * 0.5f;
                float f7 = (randomSource.nextFloat() * 2.0f - 1.0f) * 0.15f * 0.5f;
                poseStack.translate(f6, f7, 0.0f);
                itemStackRenderState.render(poseStack, multiBufferSource, n, OverlayTexture.NO_OVERLAY);
                poseStack.popPose();
                poseStack.translate(0.0f, 0.0f, f5);
            }
        }
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

