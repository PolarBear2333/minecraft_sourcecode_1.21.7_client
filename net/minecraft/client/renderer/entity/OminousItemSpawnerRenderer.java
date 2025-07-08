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
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.ItemClusterRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.OminousItemSpawner;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionfc;

public class OminousItemSpawnerRenderer
extends EntityRenderer<OminousItemSpawner, ItemClusterRenderState> {
    private static final float ROTATION_SPEED = 40.0f;
    private static final int TICKS_SCALING = 50;
    private final ItemModelResolver itemModelResolver;
    private final RandomSource random = RandomSource.create();

    protected OminousItemSpawnerRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public ItemClusterRenderState createRenderState() {
        return new ItemClusterRenderState();
    }

    @Override
    public void extractRenderState(OminousItemSpawner ominousItemSpawner, ItemClusterRenderState itemClusterRenderState, float f) {
        super.extractRenderState(ominousItemSpawner, itemClusterRenderState, f);
        ItemStack itemStack = ominousItemSpawner.getItem();
        itemClusterRenderState.extractItemGroupRenderState(ominousItemSpawner, itemStack, this.itemModelResolver);
    }

    @Override
    public void render(ItemClusterRenderState itemClusterRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        float f;
        if (itemClusterRenderState.item.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        if (itemClusterRenderState.ageInTicks <= 50.0f) {
            f = Math.min(itemClusterRenderState.ageInTicks, 50.0f) / 50.0f;
            poseStack.scale(f, f, f);
        }
        f = Mth.wrapDegrees(itemClusterRenderState.ageInTicks * 40.0f);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(f));
        ItemEntityRenderer.renderMultipleFromCount(poseStack, multiBufferSource, 0xF000F0, itemClusterRenderState, this.random);
        poseStack.popPose();
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

