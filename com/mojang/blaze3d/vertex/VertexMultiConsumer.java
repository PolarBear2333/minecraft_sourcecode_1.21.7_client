/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Consumer;

public class VertexMultiConsumer {
    public static VertexConsumer create() {
        throw new IllegalArgumentException();
    }

    public static VertexConsumer create(VertexConsumer vertexConsumer) {
        return vertexConsumer;
    }

    public static VertexConsumer create(VertexConsumer vertexConsumer, VertexConsumer vertexConsumer2) {
        return new Double(vertexConsumer, vertexConsumer2);
    }

    public static VertexConsumer create(VertexConsumer ... vertexConsumerArray) {
        return new Multiple(vertexConsumerArray);
    }

    static class Double
    implements VertexConsumer {
        private final VertexConsumer first;
        private final VertexConsumer second;

        public Double(VertexConsumer vertexConsumer, VertexConsumer vertexConsumer2) {
            if (vertexConsumer == vertexConsumer2) {
                throw new IllegalArgumentException("Duplicate delegates");
            }
            this.first = vertexConsumer;
            this.second = vertexConsumer2;
        }

        @Override
        public VertexConsumer addVertex(float f, float f2, float f3) {
            this.first.addVertex(f, f2, f3);
            this.second.addVertex(f, f2, f3);
            return this;
        }

        @Override
        public VertexConsumer setColor(int n, int n2, int n3, int n4) {
            this.first.setColor(n, n2, n3, n4);
            this.second.setColor(n, n2, n3, n4);
            return this;
        }

        @Override
        public VertexConsumer setUv(float f, float f2) {
            this.first.setUv(f, f2);
            this.second.setUv(f, f2);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int n, int n2) {
            this.first.setUv1(n, n2);
            this.second.setUv1(n, n2);
            return this;
        }

        @Override
        public VertexConsumer setUv2(int n, int n2) {
            this.first.setUv2(n, n2);
            this.second.setUv2(n, n2);
            return this;
        }

        @Override
        public VertexConsumer setNormal(float f, float f2, float f3) {
            this.first.setNormal(f, f2, f3);
            this.second.setNormal(f, f2, f3);
            return this;
        }

        @Override
        public void addVertex(float f, float f2, float f3, int n, float f4, float f5, int n2, int n3, float f6, float f7, float f8) {
            this.first.addVertex(f, f2, f3, n, f4, f5, n2, n3, f6, f7, f8);
            this.second.addVertex(f, f2, f3, n, f4, f5, n2, n3, f6, f7, f8);
        }
    }

    record Multiple(VertexConsumer[] delegates) implements VertexConsumer
    {
        Multiple {
            for (int i = 0; i < vertexConsumerArray.length; ++i) {
                for (int j = i + 1; j < vertexConsumerArray.length; ++j) {
                    if (vertexConsumerArray[i] != vertexConsumerArray[j]) continue;
                    throw new IllegalArgumentException("Duplicate delegates");
                }
            }
        }

        private void forEach(Consumer<VertexConsumer> consumer) {
            for (VertexConsumer vertexConsumer : this.delegates) {
                consumer.accept(vertexConsumer);
            }
        }

        @Override
        public VertexConsumer addVertex(float f, float f2, float f3) {
            this.forEach(vertexConsumer -> vertexConsumer.addVertex(f, f2, f3));
            return this;
        }

        @Override
        public VertexConsumer setColor(int n, int n2, int n3, int n4) {
            this.forEach(vertexConsumer -> vertexConsumer.setColor(n, n2, n3, n4));
            return this;
        }

        @Override
        public VertexConsumer setUv(float f, float f2) {
            this.forEach(vertexConsumer -> vertexConsumer.setUv(f, f2));
            return this;
        }

        @Override
        public VertexConsumer setUv1(int n, int n2) {
            this.forEach(vertexConsumer -> vertexConsumer.setUv1(n, n2));
            return this;
        }

        @Override
        public VertexConsumer setUv2(int n, int n2) {
            this.forEach(vertexConsumer -> vertexConsumer.setUv2(n, n2));
            return this;
        }

        @Override
        public VertexConsumer setNormal(float f, float f2, float f3) {
            this.forEach(vertexConsumer -> vertexConsumer.setNormal(f, f2, f3));
            return this;
        }

        @Override
        public void addVertex(float f, float f2, float f3, int n, float f4, float f5, int n2, int n3, float f6, float f7, float f8) {
            this.forEach(vertexConsumer -> vertexConsumer.addVertex(f, f2, f3, n, f4, f5, n2, n3, f6, f7, f8));
        }
    }
}

