/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package net.minecraft.client.model.geom;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class ModelPart {
    public static final float DEFAULT_SCALE = 1.0f;
    public float x;
    public float y;
    public float z;
    public float xRot;
    public float yRot;
    public float zRot;
    public float xScale = 1.0f;
    public float yScale = 1.0f;
    public float zScale = 1.0f;
    public boolean visible = true;
    public boolean skipDraw;
    private final List<Cube> cubes;
    private final Map<String, ModelPart> children;
    private PartPose initialPose = PartPose.ZERO;

    public ModelPart(List<Cube> list, Map<String, ModelPart> map) {
        this.cubes = list;
        this.children = map;
    }

    public PartPose storePose() {
        return PartPose.offsetAndRotation(this.x, this.y, this.z, this.xRot, this.yRot, this.zRot);
    }

    public PartPose getInitialPose() {
        return this.initialPose;
    }

    public void setInitialPose(PartPose partPose) {
        this.initialPose = partPose;
    }

    public void resetPose() {
        this.loadPose(this.initialPose);
    }

    public void loadPose(PartPose partPose) {
        this.x = partPose.x();
        this.y = partPose.y();
        this.z = partPose.z();
        this.xRot = partPose.xRot();
        this.yRot = partPose.yRot();
        this.zRot = partPose.zRot();
        this.xScale = partPose.xScale();
        this.yScale = partPose.yScale();
        this.zScale = partPose.zScale();
    }

    public void copyFrom(ModelPart modelPart) {
        this.xScale = modelPart.xScale;
        this.yScale = modelPart.yScale;
        this.zScale = modelPart.zScale;
        this.xRot = modelPart.xRot;
        this.yRot = modelPart.yRot;
        this.zRot = modelPart.zRot;
        this.x = modelPart.x;
        this.y = modelPart.y;
        this.z = modelPart.z;
    }

    public boolean hasChild(String string) {
        return this.children.containsKey(string);
    }

    public ModelPart getChild(String string) {
        ModelPart modelPart = this.children.get(string);
        if (modelPart == null) {
            throw new NoSuchElementException("Can't find part " + string);
        }
        return modelPart;
    }

    public void setPos(float f, float f2, float f3) {
        this.x = f;
        this.y = f2;
        this.z = f3;
    }

    public void setRotation(float f, float f2, float f3) {
        this.xRot = f;
        this.yRot = f2;
        this.zRot = f3;
    }

    public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int n, int n2) {
        this.render(poseStack, vertexConsumer, n, n2, -1);
    }

    public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int n, int n2, int n3) {
        if (!this.visible) {
            return;
        }
        if (this.cubes.isEmpty() && this.children.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        this.translateAndRotate(poseStack);
        if (!this.skipDraw) {
            this.compile(poseStack.last(), vertexConsumer, n, n2, n3);
        }
        for (ModelPart modelPart : this.children.values()) {
            modelPart.render(poseStack, vertexConsumer, n, n2, n3);
        }
        poseStack.popPose();
    }

    public void rotateBy(Quaternionf quaternionf) {
        Matrix3f matrix3f = new Matrix3f().rotationZYX(this.zRot, this.yRot, this.xRot);
        Matrix3f matrix3f2 = matrix3f.rotate((Quaternionfc)quaternionf);
        Vector3f vector3f = matrix3f2.getEulerAnglesZYX(new Vector3f());
        this.setRotation(vector3f.x, vector3f.y, vector3f.z);
    }

    public void getExtentsForGui(PoseStack poseStack, Set<Vector3f> set) {
        this.visit(poseStack, (pose, string, n, cube) -> {
            for (Polygon polygon : cube.polygons) {
                for (Vertex vertex : polygon.vertices()) {
                    float f = vertex.pos().x() / 16.0f;
                    float f2 = vertex.pos().y() / 16.0f;
                    float f3 = vertex.pos().z() / 16.0f;
                    Vector3f vector3f = pose.pose().transformPosition(f, f2, f3, new Vector3f());
                    set.add(vector3f);
                }
            }
        });
    }

    public void visit(PoseStack poseStack, Visitor visitor) {
        this.visit(poseStack, visitor, "");
    }

    private void visit(PoseStack poseStack, Visitor visitor, String string) {
        if (this.cubes.isEmpty() && this.children.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        this.translateAndRotate(poseStack);
        PoseStack.Pose pose = poseStack.last();
        for (int i = 0; i < this.cubes.size(); ++i) {
            visitor.visit(pose, string, i, this.cubes.get(i));
        }
        String string3 = string + "/";
        this.children.forEach((string2, modelPart) -> modelPart.visit(poseStack, visitor, string3 + string2));
        poseStack.popPose();
    }

    public void translateAndRotate(PoseStack poseStack) {
        poseStack.translate(this.x / 16.0f, this.y / 16.0f, this.z / 16.0f);
        if (this.xRot != 0.0f || this.yRot != 0.0f || this.zRot != 0.0f) {
            poseStack.mulPose((Quaternionfc)new Quaternionf().rotationZYX(this.zRot, this.yRot, this.xRot));
        }
        if (this.xScale != 1.0f || this.yScale != 1.0f || this.zScale != 1.0f) {
            poseStack.scale(this.xScale, this.yScale, this.zScale);
        }
    }

    private void compile(PoseStack.Pose pose, VertexConsumer vertexConsumer, int n, int n2, int n3) {
        for (Cube cube : this.cubes) {
            cube.compile(pose, vertexConsumer, n, n2, n3);
        }
    }

    public Cube getRandomCube(RandomSource randomSource) {
        return this.cubes.get(randomSource.nextInt(this.cubes.size()));
    }

    public boolean isEmpty() {
        return this.cubes.isEmpty();
    }

    public void offsetPos(Vector3f vector3f) {
        this.x += vector3f.x();
        this.y += vector3f.y();
        this.z += vector3f.z();
    }

    public void offsetRotation(Vector3f vector3f) {
        this.xRot += vector3f.x();
        this.yRot += vector3f.y();
        this.zRot += vector3f.z();
    }

    public void offsetScale(Vector3f vector3f) {
        this.xScale += vector3f.x();
        this.yScale += vector3f.y();
        this.zScale += vector3f.z();
    }

    public List<ModelPart> getAllParts() {
        ArrayList<ModelPart> arrayList = new ArrayList<ModelPart>();
        arrayList.add(this);
        this.addAllChildren((string, modelPart) -> arrayList.add((ModelPart)modelPart));
        return List.copyOf(arrayList);
    }

    public Function<String, ModelPart> createPartLookup() {
        HashMap<String, ModelPart> hashMap = new HashMap<String, ModelPart>();
        hashMap.put("root", this);
        this.addAllChildren(hashMap::putIfAbsent);
        return hashMap::get;
    }

    private void addAllChildren(BiConsumer<String, ModelPart> biConsumer) {
        for (Map.Entry<String, ModelPart> object : this.children.entrySet()) {
            biConsumer.accept(object.getKey(), object.getValue());
        }
        for (ModelPart modelPart : this.children.values()) {
            modelPart.addAllChildren(biConsumer);
        }
    }

    @FunctionalInterface
    public static interface Visitor {
        public void visit(PoseStack.Pose var1, String var2, int var3, Cube var4);
    }

    public static class Cube {
        public final Polygon[] polygons;
        public final float minX;
        public final float minY;
        public final float minZ;
        public final float maxX;
        public final float maxY;
        public final float maxZ;

        public Cube(int n, int n2, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, boolean bl, float f10, float f11, Set<Direction> set) {
            this.minX = f;
            this.minY = f2;
            this.minZ = f3;
            this.maxX = f + f4;
            this.maxY = f2 + f5;
            this.maxZ = f3 + f6;
            this.polygons = new Polygon[set.size()];
            float f12 = f + f4;
            float f13 = f2 + f5;
            float f14 = f3 + f6;
            f -= f7;
            f2 -= f8;
            f3 -= f9;
            f12 += f7;
            f13 += f8;
            f14 += f9;
            if (bl) {
                float f15 = f12;
                f12 = f;
                f = f15;
            }
            Vertex vertex = new Vertex(f, f2, f3, 0.0f, 0.0f);
            Vertex vertex2 = new Vertex(f12, f2, f3, 0.0f, 8.0f);
            Vertex vertex3 = new Vertex(f12, f13, f3, 8.0f, 8.0f);
            Vertex vertex4 = new Vertex(f, f13, f3, 8.0f, 0.0f);
            Vertex vertex5 = new Vertex(f, f2, f14, 0.0f, 0.0f);
            Vertex vertex6 = new Vertex(f12, f2, f14, 0.0f, 8.0f);
            Vertex vertex7 = new Vertex(f12, f13, f14, 8.0f, 8.0f);
            Vertex vertex8 = new Vertex(f, f13, f14, 8.0f, 0.0f);
            float f16 = n;
            float f17 = (float)n + f6;
            float f18 = (float)n + f6 + f4;
            float f19 = (float)n + f6 + f4 + f4;
            float f20 = (float)n + f6 + f4 + f6;
            float f21 = (float)n + f6 + f4 + f6 + f4;
            float f22 = n2;
            float f23 = (float)n2 + f6;
            float f24 = (float)n2 + f6 + f5;
            int n3 = 0;
            if (set.contains(Direction.DOWN)) {
                this.polygons[n3++] = new Polygon(new Vertex[]{vertex6, vertex5, vertex, vertex2}, f17, f22, f18, f23, f10, f11, bl, Direction.DOWN);
            }
            if (set.contains(Direction.UP)) {
                this.polygons[n3++] = new Polygon(new Vertex[]{vertex3, vertex4, vertex8, vertex7}, f18, f23, f19, f22, f10, f11, bl, Direction.UP);
            }
            if (set.contains(Direction.WEST)) {
                this.polygons[n3++] = new Polygon(new Vertex[]{vertex, vertex5, vertex8, vertex4}, f16, f23, f17, f24, f10, f11, bl, Direction.WEST);
            }
            if (set.contains(Direction.NORTH)) {
                this.polygons[n3++] = new Polygon(new Vertex[]{vertex2, vertex, vertex4, vertex3}, f17, f23, f18, f24, f10, f11, bl, Direction.NORTH);
            }
            if (set.contains(Direction.EAST)) {
                this.polygons[n3++] = new Polygon(new Vertex[]{vertex6, vertex2, vertex3, vertex7}, f18, f23, f20, f24, f10, f11, bl, Direction.EAST);
            }
            if (set.contains(Direction.SOUTH)) {
                this.polygons[n3] = new Polygon(new Vertex[]{vertex5, vertex6, vertex7, vertex8}, f20, f23, f21, f24, f10, f11, bl, Direction.SOUTH);
            }
        }

        public void compile(PoseStack.Pose pose, VertexConsumer vertexConsumer, int n, int n2, int n3) {
            Matrix4f matrix4f = pose.pose();
            Vector3f vector3f = new Vector3f();
            for (Polygon polygon : this.polygons) {
                Vector3f vector3f2 = pose.transformNormal((Vector3fc)polygon.normal, vector3f);
                float f = vector3f2.x();
                float f2 = vector3f2.y();
                float f3 = vector3f2.z();
                for (Vertex vertex : polygon.vertices) {
                    float f4 = vertex.pos.x() / 16.0f;
                    float f5 = vertex.pos.y() / 16.0f;
                    float f6 = vertex.pos.z() / 16.0f;
                    Vector3f vector3f3 = matrix4f.transformPosition(f4, f5, f6, vector3f);
                    vertexConsumer.addVertex(vector3f3.x(), vector3f3.y(), vector3f3.z(), n3, vertex.u, vertex.v, n2, n, f, f2, f3);
                }
            }
        }
    }

    public static final class Polygon
    extends Record {
        final Vertex[] vertices;
        final Vector3f normal;

        public Polygon(Vertex[] vertexArray, float f, float f2, float f3, float f4, float f5, float f6, boolean bl, Direction direction) {
            this(vertexArray, direction.step());
            float f7 = 0.0f / f5;
            float f8 = 0.0f / f6;
            vertexArray[0] = vertexArray[0].remap(f3 / f5 - f7, f2 / f6 + f8);
            vertexArray[1] = vertexArray[1].remap(f / f5 + f7, f2 / f6 + f8);
            vertexArray[2] = vertexArray[2].remap(f / f5 + f7, f4 / f6 - f8);
            vertexArray[3] = vertexArray[3].remap(f3 / f5 - f7, f4 / f6 - f8);
            if (bl) {
                int n = vertexArray.length;
                for (int i = 0; i < n / 2; ++i) {
                    Vertex vertex = vertexArray[i];
                    vertexArray[i] = vertexArray[n - 1 - i];
                    vertexArray[n - 1 - i] = vertex;
                }
            }
            if (bl) {
                this.normal.mul(-1.0f, 1.0f, 1.0f);
            }
        }

        public Polygon(Vertex[] vertexArray, Vector3f vector3f) {
            this.vertices = vertexArray;
            this.normal = vector3f;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Polygon.class, "vertices;normal", "vertices", "normal"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Polygon.class, "vertices;normal", "vertices", "normal"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Polygon.class, "vertices;normal", "vertices", "normal"}, this, object);
        }

        public Vertex[] vertices() {
            return this.vertices;
        }

        public Vector3f normal() {
            return this.normal;
        }
    }

    public static final class Vertex
    extends Record {
        final Vector3f pos;
        final float u;
        final float v;

        public Vertex(float f, float f2, float f3, float f4, float f5) {
            this(new Vector3f(f, f2, f3), f4, f5);
        }

        public Vertex(Vector3f vector3f, float f, float f2) {
            this.pos = vector3f;
            this.u = f;
            this.v = f2;
        }

        public Vertex remap(float f, float f2) {
            return new Vertex(this.pos, f, f2);
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Vertex.class, "pos;u;v", "pos", "u", "v"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Vertex.class, "pos;u;v", "pos", "u", "v"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Vertex.class, "pos;u;v", "pos", "u", "v"}, this, object);
        }

        public Vector3f pos() {
            return this.pos;
        }

        public float u() {
            return this.u;
        }

        public float v() {
            return this.v;
        }
    }
}

