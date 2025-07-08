/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.AbstractMinecartRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.MinecartRenderState;
import net.minecraft.world.entity.vehicle.AbstractMinecart;

public class MinecartRenderer
extends AbstractMinecartRenderer<AbstractMinecart, MinecartRenderState> {
    public MinecartRenderer(EntityRendererProvider.Context context, ModelLayerLocation modelLayerLocation) {
        super(context, modelLayerLocation);
    }

    @Override
    public MinecartRenderState createRenderState() {
        return new MinecartRenderState();
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

