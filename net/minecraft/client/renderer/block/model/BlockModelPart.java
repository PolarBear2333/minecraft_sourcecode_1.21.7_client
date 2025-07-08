/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.block.model;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.core.Direction;

public interface BlockModelPart {
    public List<BakedQuad> getQuads(@Nullable Direction var1);

    public boolean useAmbientOcclusion();

    public TextureAtlasSprite particleIcon();

    public static interface Unbaked
    extends ResolvableModel {
        public BlockModelPart bake(ModelBaker var1);
    }
}

