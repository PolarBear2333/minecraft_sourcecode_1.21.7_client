/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public interface RenderLayerParent<S extends EntityRenderState, M extends EntityModel<? super S>> {
    public M getModel();
}

