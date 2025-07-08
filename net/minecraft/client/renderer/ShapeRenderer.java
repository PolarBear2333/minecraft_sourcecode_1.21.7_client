/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix4f
 *  org.joml.Vector3f
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class ShapeRenderer {
    public static void renderShape(PoseStack poseStack, VertexConsumer vertexConsumer, VoxelShape voxelShape, double d, double d2, double d3, int n) {
        PoseStack.Pose pose = poseStack.last();
        voxelShape.forAllEdges((d4, d5, d6, d7, d8, d9) -> {
            Vector3f vector3f = new Vector3f((float)(d7 - d4), (float)(d8 - d5), (float)(d9 - d6)).normalize();
            vertexConsumer.addVertex(pose, (float)(d4 + d), (float)(d5 + d2), (float)(d6 + d3)).setColor(n).setNormal(pose, vector3f);
            vertexConsumer.addVertex(pose, (float)(d7 + d), (float)(d8 + d2), (float)(d9 + d3)).setColor(n).setNormal(pose, vector3f);
        });
    }

    public static void renderLineBox(PoseStack poseStack, VertexConsumer vertexConsumer, AABB aABB, float f, float f2, float f3, float f4) {
        ShapeRenderer.renderLineBox(poseStack, vertexConsumer, aABB.minX, aABB.minY, aABB.minZ, aABB.maxX, aABB.maxY, aABB.maxZ, f, f2, f3, f4, f, f2, f3);
    }

    public static void renderLineBox(PoseStack poseStack, VertexConsumer vertexConsumer, double d, double d2, double d3, double d4, double d5, double d6, float f, float f2, float f3, float f4) {
        ShapeRenderer.renderLineBox(poseStack, vertexConsumer, d, d2, d3, d4, d5, d6, f, f2, f3, f4, f, f2, f3);
    }

    public static void renderLineBox(PoseStack poseStack, VertexConsumer vertexConsumer, double d, double d2, double d3, double d4, double d5, double d6, float f, float f2, float f3, float f4, float f5, float f6, float f7) {
        PoseStack.Pose pose = poseStack.last();
        float f8 = (float)d;
        float f9 = (float)d2;
        float f10 = (float)d3;
        float f11 = (float)d4;
        float f12 = (float)d5;
        float f13 = (float)d6;
        vertexConsumer.addVertex(pose, f8, f9, f10).setColor(f, f6, f7, f4).setNormal(pose, 1.0f, 0.0f, 0.0f);
        vertexConsumer.addVertex(pose, f11, f9, f10).setColor(f, f6, f7, f4).setNormal(pose, 1.0f, 0.0f, 0.0f);
        vertexConsumer.addVertex(pose, f8, f9, f10).setColor(f5, f2, f7, f4).setNormal(pose, 0.0f, 1.0f, 0.0f);
        vertexConsumer.addVertex(pose, f8, f12, f10).setColor(f5, f2, f7, f4).setNormal(pose, 0.0f, 1.0f, 0.0f);
        vertexConsumer.addVertex(pose, f8, f9, f10).setColor(f5, f6, f3, f4).setNormal(pose, 0.0f, 0.0f, 1.0f);
        vertexConsumer.addVertex(pose, f8, f9, f13).setColor(f5, f6, f3, f4).setNormal(pose, 0.0f, 0.0f, 1.0f);
        vertexConsumer.addVertex(pose, f11, f9, f10).setColor(f, f2, f3, f4).setNormal(pose, 0.0f, 1.0f, 0.0f);
        vertexConsumer.addVertex(pose, f11, f12, f10).setColor(f, f2, f3, f4).setNormal(pose, 0.0f, 1.0f, 0.0f);
        vertexConsumer.addVertex(pose, f11, f12, f10).setColor(f, f2, f3, f4).setNormal(pose, -1.0f, 0.0f, 0.0f);
        vertexConsumer.addVertex(pose, f8, f12, f10).setColor(f, f2, f3, f4).setNormal(pose, -1.0f, 0.0f, 0.0f);
        vertexConsumer.addVertex(pose, f8, f12, f10).setColor(f, f2, f3, f4).setNormal(pose, 0.0f, 0.0f, 1.0f);
        vertexConsumer.addVertex(pose, f8, f12, f13).setColor(f, f2, f3, f4).setNormal(pose, 0.0f, 0.0f, 1.0f);
        vertexConsumer.addVertex(pose, f8, f12, f13).setColor(f, f2, f3, f4).setNormal(pose, 0.0f, -1.0f, 0.0f);
        vertexConsumer.addVertex(pose, f8, f9, f13).setColor(f, f2, f3, f4).setNormal(pose, 0.0f, -1.0f, 0.0f);
        vertexConsumer.addVertex(pose, f8, f9, f13).setColor(f, f2, f3, f4).setNormal(pose, 1.0f, 0.0f, 0.0f);
        vertexConsumer.addVertex(pose, f11, f9, f13).setColor(f, f2, f3, f4).setNormal(pose, 1.0f, 0.0f, 0.0f);
        vertexConsumer.addVertex(pose, f11, f9, f13).setColor(f, f2, f3, f4).setNormal(pose, 0.0f, 0.0f, -1.0f);
        vertexConsumer.addVertex(pose, f11, f9, f10).setColor(f, f2, f3, f4).setNormal(pose, 0.0f, 0.0f, -1.0f);
        vertexConsumer.addVertex(pose, f8, f12, f13).setColor(f, f2, f3, f4).setNormal(pose, 1.0f, 0.0f, 0.0f);
        vertexConsumer.addVertex(pose, f11, f12, f13).setColor(f, f2, f3, f4).setNormal(pose, 1.0f, 0.0f, 0.0f);
        vertexConsumer.addVertex(pose, f11, f9, f13).setColor(f, f2, f3, f4).setNormal(pose, 0.0f, 1.0f, 0.0f);
        vertexConsumer.addVertex(pose, f11, f12, f13).setColor(f, f2, f3, f4).setNormal(pose, 0.0f, 1.0f, 0.0f);
        vertexConsumer.addVertex(pose, f11, f12, f10).setColor(f, f2, f3, f4).setNormal(pose, 0.0f, 0.0f, 1.0f);
        vertexConsumer.addVertex(pose, f11, f12, f13).setColor(f, f2, f3, f4).setNormal(pose, 0.0f, 0.0f, 1.0f);
    }

    public static void addChainedFilledBoxVertices(PoseStack poseStack, VertexConsumer vertexConsumer, double d, double d2, double d3, double d4, double d5, double d6, float f, float f2, float f3, float f4) {
        ShapeRenderer.addChainedFilledBoxVertices(poseStack, vertexConsumer, (float)d, (float)d2, (float)d3, (float)d4, (float)d5, (float)d6, f, f2, f3, f4);
    }

    public static void addChainedFilledBoxVertices(PoseStack poseStack, VertexConsumer vertexConsumer, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10) {
        Matrix4f matrix4f = poseStack.last().pose();
        vertexConsumer.addVertex(matrix4f, f, f2, f3).setColor(f7, f8, f9, f10);
        vertexConsumer.addVertex(matrix4f, f, f2, f3).setColor(f7, f8, f9, f10);
        vertexConsumer.addVertex(matrix4f, f, f2, f3).setColor(f7, f8, f9, f10);
        vertexConsumer.addVertex(matrix4f, f, f2, f6).setColor(f7, f8, f9, f10);
        vertexConsumer.addVertex(matrix4f, f, f5, f3).setColor(f7, f8, f9, f10);
        vertexConsumer.addVertex(matrix4f, f, f5, f6).setColor(f7, f8, f9, f10);
        vertexConsumer.addVertex(matrix4f, f, f5, f6).setColor(f7, f8, f9, f10);
        vertexConsumer.addVertex(matrix4f, f, f2, f6).setColor(f7, f8, f9, f10);
        vertexConsumer.addVertex(matrix4f, f4, f5, f6).setColor(f7, f8, f9, f10);
        vertexConsumer.addVertex(matrix4f, f4, f2, f6).setColor(f7, f8, f9, f10);
        vertexConsumer.addVertex(matrix4f, f4, f2, f6).setColor(f7, f8, f9, f10);
        vertexConsumer.addVertex(matrix4f, f4, f2, f3).setColor(f7, f8, f9, f10);
        vertexConsumer.addVertex(matrix4f, f4, f5, f6).setColor(f7, f8, f9, f10);
        vertexConsumer.addVertex(matrix4f, f4, f5, f3).setColor(f7, f8, f9, f10);
        vertexConsumer.addVertex(matrix4f, f4, f5, f3).setColor(f7, f8, f9, f10);
        vertexConsumer.addVertex(matrix4f, f4, f2, f3).setColor(f7, f8, f9, f10);
        vertexConsumer.addVertex(matrix4f, f, f5, f3).setColor(f7, f8, f9, f10);
        vertexConsumer.addVertex(matrix4f, f, f2, f3).setColor(f7, f8, f9, f10);
        vertexConsumer.addVertex(matrix4f, f, f2, f3).setColor(f7, f8, f9, f10);
        vertexConsumer.addVertex(matrix4f, f4, f2, f3).setColor(f7, f8, f9, f10);
        vertexConsumer.addVertex(matrix4f, f, f2, f6).setColor(f7, f8, f9, f10);
        vertexConsumer.addVertex(matrix4f, f4, f2, f6).setColor(f7, f8, f9, f10);
        vertexConsumer.addVertex(matrix4f, f4, f2, f6).setColor(f7, f8, f9, f10);
        vertexConsumer.addVertex(matrix4f, f, f5, f3).setColor(f7, f8, f9, f10);
        vertexConsumer.addVertex(matrix4f, f, f5, f3).setColor(f7, f8, f9, f10);
        vertexConsumer.addVertex(matrix4f, f, f5, f6).setColor(f7, f8, f9, f10);
        vertexConsumer.addVertex(matrix4f, f4, f5, f3).setColor(f7, f8, f9, f10);
        vertexConsumer.addVertex(matrix4f, f4, f5, f6).setColor(f7, f8, f9, f10);
        vertexConsumer.addVertex(matrix4f, f4, f5, f6).setColor(f7, f8, f9, f10);
        vertexConsumer.addVertex(matrix4f, f4, f5, f6).setColor(f7, f8, f9, f10);
    }

    public static void renderFace(PoseStack poseStack, VertexConsumer vertexConsumer, Direction direction, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10) {
        Matrix4f matrix4f = poseStack.last().pose();
        switch (direction) {
            case DOWN: {
                vertexConsumer.addVertex(matrix4f, f, f2, f3).setColor(f7, f8, f9, f10);
                vertexConsumer.addVertex(matrix4f, f4, f2, f3).setColor(f7, f8, f9, f10);
                vertexConsumer.addVertex(matrix4f, f4, f2, f6).setColor(f7, f8, f9, f10);
                vertexConsumer.addVertex(matrix4f, f, f2, f6).setColor(f7, f8, f9, f10);
                break;
            }
            case UP: {
                vertexConsumer.addVertex(matrix4f, f, f5, f3).setColor(f7, f8, f9, f10);
                vertexConsumer.addVertex(matrix4f, f, f5, f6).setColor(f7, f8, f9, f10);
                vertexConsumer.addVertex(matrix4f, f4, f5, f6).setColor(f7, f8, f9, f10);
                vertexConsumer.addVertex(matrix4f, f4, f5, f3).setColor(f7, f8, f9, f10);
                break;
            }
            case NORTH: {
                vertexConsumer.addVertex(matrix4f, f, f2, f3).setColor(f7, f8, f9, f10);
                vertexConsumer.addVertex(matrix4f, f, f5, f3).setColor(f7, f8, f9, f10);
                vertexConsumer.addVertex(matrix4f, f4, f5, f3).setColor(f7, f8, f9, f10);
                vertexConsumer.addVertex(matrix4f, f4, f2, f3).setColor(f7, f8, f9, f10);
                break;
            }
            case SOUTH: {
                vertexConsumer.addVertex(matrix4f, f, f2, f6).setColor(f7, f8, f9, f10);
                vertexConsumer.addVertex(matrix4f, f4, f2, f6).setColor(f7, f8, f9, f10);
                vertexConsumer.addVertex(matrix4f, f4, f5, f6).setColor(f7, f8, f9, f10);
                vertexConsumer.addVertex(matrix4f, f, f5, f6).setColor(f7, f8, f9, f10);
                break;
            }
            case WEST: {
                vertexConsumer.addVertex(matrix4f, f, f2, f3).setColor(f7, f8, f9, f10);
                vertexConsumer.addVertex(matrix4f, f, f2, f6).setColor(f7, f8, f9, f10);
                vertexConsumer.addVertex(matrix4f, f, f5, f6).setColor(f7, f8, f9, f10);
                vertexConsumer.addVertex(matrix4f, f, f5, f3).setColor(f7, f8, f9, f10);
                break;
            }
            case EAST: {
                vertexConsumer.addVertex(matrix4f, f4, f2, f3).setColor(f7, f8, f9, f10);
                vertexConsumer.addVertex(matrix4f, f4, f5, f3).setColor(f7, f8, f9, f10);
                vertexConsumer.addVertex(matrix4f, f4, f5, f6).setColor(f7, f8, f9, f10);
                vertexConsumer.addVertex(matrix4f, f4, f2, f6).setColor(f7, f8, f9, f10);
            }
        }
    }

    public static void renderVector(PoseStack poseStack, VertexConsumer vertexConsumer, Vector3f vector3f, Vec3 vec3, int n) {
        PoseStack.Pose pose = poseStack.last();
        vertexConsumer.addVertex(pose, vector3f).setColor(n).setNormal(pose, (float)vec3.x, (float)vec3.y, (float)vec3.z);
        vertexConsumer.addVertex(pose, (float)((double)vector3f.x() + vec3.x), (float)((double)vector3f.y() + vec3.y), (float)((double)vector3f.z() + vec3.z)).setColor(n).setNormal(pose, (float)vec3.x, (float)vec3.y, (float)vec3.z);
    }
}

