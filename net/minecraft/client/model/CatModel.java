/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import net.minecraft.client.model.FelineModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.renderer.entity.state.CatRenderState;

public class CatModel
extends FelineModel<CatRenderState> {
    public static final MeshTransformer CAT_TRANSFORMER = MeshTransformer.scaling(0.8f);

    public CatModel(ModelPart modelPart) {
        super(modelPart);
    }
}

