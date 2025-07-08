/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public class LivingEntityEmissiveLayer<S extends LivingEntityRenderState, M extends EntityModel<S>>
extends RenderLayer<S, M> {
    private final ResourceLocation texture;
    private final AlphaFunction<S> alphaFunction;
    private final DrawSelector<S, M> drawSelector;
    private final Function<ResourceLocation, RenderType> bufferProvider;
    private final boolean alwaysVisible;

    public LivingEntityEmissiveLayer(RenderLayerParent<S, M> renderLayerParent, ResourceLocation resourceLocation, AlphaFunction<S> alphaFunction, DrawSelector<S, M> drawSelector, Function<ResourceLocation, RenderType> function, boolean bl) {
        super(renderLayerParent);
        this.texture = resourceLocation;
        this.alphaFunction = alphaFunction;
        this.drawSelector = drawSelector;
        this.bufferProvider = function;
        this.alwaysVisible = bl;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, S s, float f, float f2) {
        if (((LivingEntityRenderState)s).isInvisible && !this.alwaysVisible) {
            return;
        }
        if (!this.onlyDrawSelectedParts(s)) {
            return;
        }
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(this.bufferProvider.apply(this.texture));
        float f3 = this.alphaFunction.apply(s, ((LivingEntityRenderState)s).ageInTicks);
        int n2 = ARGB.color(Mth.floor(f3 * 255.0f), 255, 255, 255);
        ((Model)this.getParentModel()).renderToBuffer(poseStack, vertexConsumer, n, LivingEntityRenderer.getOverlayCoords(s, 0.0f), n2);
        this.resetDrawForAllParts();
    }

    private boolean onlyDrawSelectedParts(S s) {
        List<ModelPart> list = this.drawSelector.getPartsToDraw(this.getParentModel(), s);
        if (list.isEmpty()) {
            return false;
        }
        ((Model)this.getParentModel()).allParts().forEach(modelPart -> {
            modelPart.skipDraw = true;
        });
        list.forEach(modelPart -> {
            modelPart.skipDraw = false;
        });
        return true;
    }

    private void resetDrawForAllParts() {
        ((Model)this.getParentModel()).allParts().forEach(modelPart -> {
            modelPart.skipDraw = false;
        });
    }

    public static interface AlphaFunction<S extends LivingEntityRenderState> {
        public float apply(S var1, float var2);
    }

    public static interface DrawSelector<S extends LivingEntityRenderState, M extends EntityModel<S>> {
        public List<ModelPart> getPartsToDraw(M var1, S var2);
    }
}

