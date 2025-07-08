/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 */
package net.minecraft.client.renderer.entity.layers;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.IronGolemRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Crackiness;

public class IronGolemCrackinessLayer
extends RenderLayer<IronGolemRenderState, IronGolemModel> {
    private static final Map<Crackiness.Level, ResourceLocation> resourceLocations = ImmutableMap.of((Object)((Object)Crackiness.Level.LOW), (Object)ResourceLocation.withDefaultNamespace("textures/entity/iron_golem/iron_golem_crackiness_low.png"), (Object)((Object)Crackiness.Level.MEDIUM), (Object)ResourceLocation.withDefaultNamespace("textures/entity/iron_golem/iron_golem_crackiness_medium.png"), (Object)((Object)Crackiness.Level.HIGH), (Object)ResourceLocation.withDefaultNamespace("textures/entity/iron_golem/iron_golem_crackiness_high.png"));

    public IronGolemCrackinessLayer(RenderLayerParent<IronGolemRenderState, IronGolemModel> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, IronGolemRenderState ironGolemRenderState, float f, float f2) {
        if (ironGolemRenderState.isInvisible) {
            return;
        }
        Crackiness.Level level = ironGolemRenderState.crackiness;
        if (level == Crackiness.Level.NONE) {
            return;
        }
        ResourceLocation resourceLocation = resourceLocations.get((Object)level);
        IronGolemCrackinessLayer.renderColoredCutoutModel(this.getParentModel(), resourceLocation, poseStack, multiBufferSource, n, ironGolemRenderState, -1);
    }
}

