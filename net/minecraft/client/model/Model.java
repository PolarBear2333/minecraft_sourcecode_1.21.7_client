/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public abstract class Model {
    protected final ModelPart root;
    protected final Function<ResourceLocation, RenderType> renderType;
    private final List<ModelPart> allParts;

    public Model(ModelPart modelPart, Function<ResourceLocation, RenderType> function) {
        this.root = modelPart;
        this.renderType = function;
        this.allParts = modelPart.getAllParts();
    }

    public final RenderType renderType(ResourceLocation resourceLocation) {
        return this.renderType.apply(resourceLocation);
    }

    public final void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int n, int n2, int n3) {
        this.root().render(poseStack, vertexConsumer, n, n2, n3);
    }

    public final void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int n, int n2) {
        this.renderToBuffer(poseStack, vertexConsumer, n, n2, -1);
    }

    public final ModelPart root() {
        return this.root;
    }

    public final List<ModelPart> allParts() {
        return this.allParts;
    }

    public final void resetPose() {
        for (ModelPart modelPart : this.allParts) {
            modelPart.resetPose();
        }
    }

    public static class Simple
    extends Model {
        public Simple(ModelPart modelPart, Function<ResourceLocation, RenderType> function) {
            super(modelPart, function);
        }
    }
}

