/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.world.entity.monster.Zombie;

public class ZombieRenderer
extends AbstractZombieRenderer<Zombie, ZombieRenderState, ZombieModel<ZombieRenderState>> {
    public ZombieRenderer(EntityRendererProvider.Context context) {
        this(context, ModelLayers.ZOMBIE, ModelLayers.ZOMBIE_BABY, ModelLayers.ZOMBIE_INNER_ARMOR, ModelLayers.ZOMBIE_OUTER_ARMOR, ModelLayers.ZOMBIE_BABY_INNER_ARMOR, ModelLayers.ZOMBIE_BABY_OUTER_ARMOR);
    }

    @Override
    public ZombieRenderState createRenderState() {
        return new ZombieRenderState();
    }

    public ZombieRenderer(EntityRendererProvider.Context context, ModelLayerLocation modelLayerLocation, ModelLayerLocation modelLayerLocation2, ModelLayerLocation modelLayerLocation3, ModelLayerLocation modelLayerLocation4, ModelLayerLocation modelLayerLocation5, ModelLayerLocation modelLayerLocation6) {
        super(context, new ZombieModel(context.bakeLayer(modelLayerLocation)), new ZombieModel(context.bakeLayer(modelLayerLocation2)), new ZombieModel(context.bakeLayer(modelLayerLocation3)), new ZombieModel(context.bakeLayer(modelLayerLocation4)), new ZombieModel(context.bakeLayer(modelLayerLocation5)), new ZombieModel(context.bakeLayer(modelLayerLocation6)));
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

