/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import java.util.Optional;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.ARGB;

public class OutlineBufferSource
implements MultiBufferSource {
    private final MultiBufferSource.BufferSource bufferSource;
    private final MultiBufferSource.BufferSource outlineBufferSource = MultiBufferSource.immediate(new ByteBufferBuilder(1536));
    private int teamR = 255;
    private int teamG = 255;
    private int teamB = 255;
    private int teamA = 255;

    public OutlineBufferSource(MultiBufferSource.BufferSource bufferSource) {
        this.bufferSource = bufferSource;
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        if (renderType.isOutline()) {
            VertexConsumer vertexConsumer = this.outlineBufferSource.getBuffer(renderType);
            return new EntityOutlineGenerator(vertexConsumer, this.teamR, this.teamG, this.teamB, this.teamA);
        }
        VertexConsumer vertexConsumer = this.bufferSource.getBuffer(renderType);
        Optional<RenderType> optional = renderType.outline();
        if (optional.isPresent()) {
            VertexConsumer vertexConsumer2 = this.outlineBufferSource.getBuffer(optional.get());
            EntityOutlineGenerator entityOutlineGenerator = new EntityOutlineGenerator(vertexConsumer2, this.teamR, this.teamG, this.teamB, this.teamA);
            return VertexMultiConsumer.create((VertexConsumer)entityOutlineGenerator, vertexConsumer);
        }
        return vertexConsumer;
    }

    public void setColor(int n, int n2, int n3, int n4) {
        this.teamR = n;
        this.teamG = n2;
        this.teamB = n3;
        this.teamA = n4;
    }

    public void endOutlineBatch() {
        this.outlineBufferSource.endBatch();
    }

    record EntityOutlineGenerator(VertexConsumer delegate, int color) implements VertexConsumer
    {
        public EntityOutlineGenerator(VertexConsumer vertexConsumer, int n, int n2, int n3, int n4) {
            this(vertexConsumer, ARGB.color(n4, n, n2, n3));
        }

        @Override
        public VertexConsumer addVertex(float f, float f2, float f3) {
            this.delegate.addVertex(f, f2, f3).setColor(this.color);
            return this;
        }

        @Override
        public VertexConsumer setColor(int n, int n2, int n3, int n4) {
            return this;
        }

        @Override
        public VertexConsumer setUv(float f, float f2) {
            this.delegate.setUv(f, f2);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int n, int n2) {
            return this;
        }

        @Override
        public VertexConsumer setUv2(int n, int n2) {
            return this;
        }

        @Override
        public VertexConsumer setNormal(float f, float f2, float f3) {
            return this;
        }
    }
}

