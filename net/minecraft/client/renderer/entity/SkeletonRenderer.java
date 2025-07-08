/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractSkeletonRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Skeleton;

public class SkeletonRenderer
extends AbstractSkeletonRenderer<Skeleton, SkeletonRenderState> {
    private static final ResourceLocation SKELETON_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/skeleton/skeleton.png");

    public SkeletonRenderer(EntityRendererProvider.Context context) {
        super(context, ModelLayers.SKELETON, ModelLayers.SKELETON_INNER_ARMOR, ModelLayers.SKELETON_OUTER_ARMOR);
    }

    @Override
    public ResourceLocation getTextureLocation(SkeletonRenderState skeletonRenderState) {
        return SKELETON_LOCATION;
    }

    @Override
    public SkeletonRenderState createRenderState() {
        return new SkeletonRenderState();
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

