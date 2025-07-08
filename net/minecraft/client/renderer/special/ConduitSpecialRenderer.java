/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  org.joml.Vector3f
 */
package net.minecraft.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.MapCodec;
import java.util.Set;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.ConduitRenderer;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Vector3f;

public class ConduitSpecialRenderer
implements NoDataSpecialModelRenderer {
    private final ModelPart model;

    public ConduitSpecialRenderer(ModelPart modelPart) {
        this.model = modelPart;
    }

    @Override
    public void render(ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2, boolean bl) {
        VertexConsumer vertexConsumer = ConduitRenderer.SHELL_TEXTURE.buffer(multiBufferSource, RenderType::entitySolid);
        poseStack.pushPose();
        poseStack.translate(0.5f, 0.5f, 0.5f);
        this.model.render(poseStack, vertexConsumer, n, n2);
        poseStack.popPose();
    }

    @Override
    public void getExtents(Set<Vector3f> set) {
        PoseStack poseStack = new PoseStack();
        poseStack.translate(0.5f, 0.5f, 0.5f);
        this.model.getExtentsForGui(poseStack, set);
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked
    {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit((Object)new Unbaked());

        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(EntityModelSet entityModelSet) {
            return new ConduitSpecialRenderer(entityModelSet.bakeLayer(ModelLayers.CONDUIT_SHELL));
        }
    }
}

