/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectListIterator
 *  org.joml.Matrix4f
 *  org.joml.Vector4f
 */
package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SectionOcclusionGraph;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class ChunkCullingDebugRenderer
implements DebugRenderer.SimpleDebugRenderer {
    public static final Direction[] DIRECTIONS = Direction.values();
    private final Minecraft minecraft;

    public ChunkCullingDebugRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double d2, double d3) {
        Object object;
        Object object2;
        Object object3;
        LevelRenderer levelRenderer = this.minecraft.levelRenderer;
        if (this.minecraft.sectionPath || this.minecraft.sectionVisibility) {
            object3 = levelRenderer.getSectionOcclusionGraph();
            for (Vector4f[] vector4fArray : levelRenderer.getVisibleSections()) {
                int n;
                VertexConsumer vertexConsumer;
                object2 = ((SectionOcclusionGraph)object3).getNode((SectionRenderDispatcher.RenderSection)vector4fArray);
                if (object2 == null) continue;
                object = vector4fArray.getRenderOrigin();
                poseStack.pushPose();
                poseStack.translate((double)((Vec3i)object).getX() - d, (double)((Vec3i)object).getY() - d2, (double)((Vec3i)object).getZ() - d3);
                Matrix4f matrix4f = poseStack.last().pose();
                if (this.minecraft.sectionPath) {
                    vertexConsumer = multiBufferSource.getBuffer(RenderType.lines());
                    n = ((SectionOcclusionGraph.Node)object2).step == 0 ? 0 : Mth.hsvToRgb((float)((SectionOcclusionGraph.Node)object2).step / 50.0f, 0.9f, 0.9f);
                    int n2 = n >> 16 & 0xFF;
                    int n3 = n >> 8 & 0xFF;
                    int n4 = n & 0xFF;
                    for (int i = 0; i < DIRECTIONS.length; ++i) {
                        if (!((SectionOcclusionGraph.Node)object2).hasSourceDirection(i)) continue;
                        Direction direction = DIRECTIONS[i];
                        vertexConsumer.addVertex(matrix4f, 8.0f, 8.0f, 8.0f).setColor(n2, n3, n4, 255).setNormal(direction.getStepX(), direction.getStepY(), direction.getStepZ());
                        vertexConsumer.addVertex(matrix4f, (float)(8 - 16 * direction.getStepX()), (float)(8 - 16 * direction.getStepY()), (float)(8 - 16 * direction.getStepZ())).setColor(n2, n3, n4, 255).setNormal(direction.getStepX(), direction.getStepY(), direction.getStepZ());
                    }
                }
                if (this.minecraft.sectionVisibility && vector4fArray.getSectionMesh().hasRenderableLayers()) {
                    vertexConsumer = multiBufferSource.getBuffer(RenderType.lines());
                    n = 0;
                    for (Direction direction : DIRECTIONS) {
                        for (Direction direction2 : DIRECTIONS) {
                            boolean bl = vector4fArray.getSectionMesh().facesCanSeeEachother(direction, direction2);
                            if (bl) continue;
                            ++n;
                            vertexConsumer.addVertex(matrix4f, (float)(8 + 8 * direction.getStepX()), (float)(8 + 8 * direction.getStepY()), (float)(8 + 8 * direction.getStepZ())).setColor(255, 0, 0, 255).setNormal(direction.getStepX(), direction.getStepY(), direction.getStepZ());
                            vertexConsumer.addVertex(matrix4f, (float)(8 + 8 * direction2.getStepX()), (float)(8 + 8 * direction2.getStepY()), (float)(8 + 8 * direction2.getStepZ())).setColor(255, 0, 0, 255).setNormal(direction2.getStepX(), direction2.getStepY(), direction2.getStepZ());
                        }
                    }
                    if (n > 0) {
                        VertexConsumer vertexConsumer2 = multiBufferSource.getBuffer(RenderType.debugQuads());
                        float f = 0.5f;
                        float f2 = 0.2f;
                        vertexConsumer2.addVertex(matrix4f, 0.5f, 15.5f, 0.5f).setColor(0.9f, 0.9f, 0.0f, 0.2f);
                        vertexConsumer2.addVertex(matrix4f, 15.5f, 15.5f, 0.5f).setColor(0.9f, 0.9f, 0.0f, 0.2f);
                        vertexConsumer2.addVertex(matrix4f, 15.5f, 15.5f, 15.5f).setColor(0.9f, 0.9f, 0.0f, 0.2f);
                        vertexConsumer2.addVertex(matrix4f, 0.5f, 15.5f, 15.5f).setColor(0.9f, 0.9f, 0.0f, 0.2f);
                        vertexConsumer2.addVertex(matrix4f, 0.5f, 0.5f, 15.5f).setColor(0.9f, 0.9f, 0.0f, 0.2f);
                        vertexConsumer2.addVertex(matrix4f, 15.5f, 0.5f, 15.5f).setColor(0.9f, 0.9f, 0.0f, 0.2f);
                        vertexConsumer2.addVertex(matrix4f, 15.5f, 0.5f, 0.5f).setColor(0.9f, 0.9f, 0.0f, 0.2f);
                        vertexConsumer2.addVertex(matrix4f, 0.5f, 0.5f, 0.5f).setColor(0.9f, 0.9f, 0.0f, 0.2f);
                        vertexConsumer2.addVertex(matrix4f, 0.5f, 15.5f, 0.5f).setColor(0.9f, 0.9f, 0.0f, 0.2f);
                        vertexConsumer2.addVertex(matrix4f, 0.5f, 15.5f, 15.5f).setColor(0.9f, 0.9f, 0.0f, 0.2f);
                        vertexConsumer2.addVertex(matrix4f, 0.5f, 0.5f, 15.5f).setColor(0.9f, 0.9f, 0.0f, 0.2f);
                        vertexConsumer2.addVertex(matrix4f, 0.5f, 0.5f, 0.5f).setColor(0.9f, 0.9f, 0.0f, 0.2f);
                        vertexConsumer2.addVertex(matrix4f, 15.5f, 0.5f, 0.5f).setColor(0.9f, 0.9f, 0.0f, 0.2f);
                        vertexConsumer2.addVertex(matrix4f, 15.5f, 0.5f, 15.5f).setColor(0.9f, 0.9f, 0.0f, 0.2f);
                        vertexConsumer2.addVertex(matrix4f, 15.5f, 15.5f, 15.5f).setColor(0.9f, 0.9f, 0.0f, 0.2f);
                        vertexConsumer2.addVertex(matrix4f, 15.5f, 15.5f, 0.5f).setColor(0.9f, 0.9f, 0.0f, 0.2f);
                        vertexConsumer2.addVertex(matrix4f, 0.5f, 0.5f, 0.5f).setColor(0.9f, 0.9f, 0.0f, 0.2f);
                        vertexConsumer2.addVertex(matrix4f, 15.5f, 0.5f, 0.5f).setColor(0.9f, 0.9f, 0.0f, 0.2f);
                        vertexConsumer2.addVertex(matrix4f, 15.5f, 15.5f, 0.5f).setColor(0.9f, 0.9f, 0.0f, 0.2f);
                        vertexConsumer2.addVertex(matrix4f, 0.5f, 15.5f, 0.5f).setColor(0.9f, 0.9f, 0.0f, 0.2f);
                        vertexConsumer2.addVertex(matrix4f, 0.5f, 15.5f, 15.5f).setColor(0.9f, 0.9f, 0.0f, 0.2f);
                        vertexConsumer2.addVertex(matrix4f, 15.5f, 15.5f, 15.5f).setColor(0.9f, 0.9f, 0.0f, 0.2f);
                        vertexConsumer2.addVertex(matrix4f, 15.5f, 0.5f, 15.5f).setColor(0.9f, 0.9f, 0.0f, 0.2f);
                        vertexConsumer2.addVertex(matrix4f, 0.5f, 0.5f, 15.5f).setColor(0.9f, 0.9f, 0.0f, 0.2f);
                    }
                }
                poseStack.popPose();
            }
        }
        if ((object3 = levelRenderer.getCapturedFrustum()) != null) {
            Vector4f[] vector4fArray;
            poseStack.pushPose();
            poseStack.translate((float)(((Frustum)object3).getCamX() - d), (float)(((Frustum)object3).getCamY() - d2), (float)(((Frustum)object3).getCamZ() - d3));
            ObjectListIterator objectListIterator = poseStack.last().pose();
            vector4fArray = ((Frustum)object3).getFrustumPoints();
            object2 = multiBufferSource.getBuffer(RenderType.debugQuads());
            this.addFrustumQuad((VertexConsumer)object2, (Matrix4f)objectListIterator, vector4fArray, 0, 1, 2, 3, 0, 1, 1);
            this.addFrustumQuad((VertexConsumer)object2, (Matrix4f)objectListIterator, vector4fArray, 4, 5, 6, 7, 1, 0, 0);
            this.addFrustumQuad((VertexConsumer)object2, (Matrix4f)objectListIterator, vector4fArray, 0, 1, 5, 4, 1, 1, 0);
            this.addFrustumQuad((VertexConsumer)object2, (Matrix4f)objectListIterator, vector4fArray, 2, 3, 7, 6, 0, 0, 1);
            this.addFrustumQuad((VertexConsumer)object2, (Matrix4f)objectListIterator, vector4fArray, 0, 4, 7, 3, 0, 1, 0);
            this.addFrustumQuad((VertexConsumer)object2, (Matrix4f)objectListIterator, vector4fArray, 1, 5, 6, 2, 1, 0, 1);
            object = multiBufferSource.getBuffer(RenderType.lines());
            this.addFrustumVertex((VertexConsumer)object, (Matrix4f)objectListIterator, vector4fArray[0]);
            this.addFrustumVertex((VertexConsumer)object, (Matrix4f)objectListIterator, vector4fArray[1]);
            this.addFrustumVertex((VertexConsumer)object, (Matrix4f)objectListIterator, vector4fArray[1]);
            this.addFrustumVertex((VertexConsumer)object, (Matrix4f)objectListIterator, vector4fArray[2]);
            this.addFrustumVertex((VertexConsumer)object, (Matrix4f)objectListIterator, vector4fArray[2]);
            this.addFrustumVertex((VertexConsumer)object, (Matrix4f)objectListIterator, vector4fArray[3]);
            this.addFrustumVertex((VertexConsumer)object, (Matrix4f)objectListIterator, vector4fArray[3]);
            this.addFrustumVertex((VertexConsumer)object, (Matrix4f)objectListIterator, vector4fArray[0]);
            this.addFrustumVertex((VertexConsumer)object, (Matrix4f)objectListIterator, vector4fArray[4]);
            this.addFrustumVertex((VertexConsumer)object, (Matrix4f)objectListIterator, vector4fArray[5]);
            this.addFrustumVertex((VertexConsumer)object, (Matrix4f)objectListIterator, vector4fArray[5]);
            this.addFrustumVertex((VertexConsumer)object, (Matrix4f)objectListIterator, vector4fArray[6]);
            this.addFrustumVertex((VertexConsumer)object, (Matrix4f)objectListIterator, vector4fArray[6]);
            this.addFrustumVertex((VertexConsumer)object, (Matrix4f)objectListIterator, vector4fArray[7]);
            this.addFrustumVertex((VertexConsumer)object, (Matrix4f)objectListIterator, vector4fArray[7]);
            this.addFrustumVertex((VertexConsumer)object, (Matrix4f)objectListIterator, vector4fArray[4]);
            this.addFrustumVertex((VertexConsumer)object, (Matrix4f)objectListIterator, vector4fArray[0]);
            this.addFrustumVertex((VertexConsumer)object, (Matrix4f)objectListIterator, vector4fArray[4]);
            this.addFrustumVertex((VertexConsumer)object, (Matrix4f)objectListIterator, vector4fArray[1]);
            this.addFrustumVertex((VertexConsumer)object, (Matrix4f)objectListIterator, vector4fArray[5]);
            this.addFrustumVertex((VertexConsumer)object, (Matrix4f)objectListIterator, vector4fArray[2]);
            this.addFrustumVertex((VertexConsumer)object, (Matrix4f)objectListIterator, vector4fArray[6]);
            this.addFrustumVertex((VertexConsumer)object, (Matrix4f)objectListIterator, vector4fArray[3]);
            this.addFrustumVertex((VertexConsumer)object, (Matrix4f)objectListIterator, vector4fArray[7]);
            poseStack.popPose();
        }
    }

    private void addFrustumVertex(VertexConsumer vertexConsumer, Matrix4f matrix4f, Vector4f vector4f) {
        vertexConsumer.addVertex(matrix4f, vector4f.x(), vector4f.y(), vector4f.z()).setColor(-16777216).setNormal(0.0f, 0.0f, -1.0f);
    }

    private void addFrustumQuad(VertexConsumer vertexConsumer, Matrix4f matrix4f, Vector4f[] vector4fArray, int n, int n2, int n3, int n4, int n5, int n6, int n7) {
        float f = 0.25f;
        vertexConsumer.addVertex(matrix4f, vector4fArray[n].x(), vector4fArray[n].y(), vector4fArray[n].z()).setColor((float)n5, (float)n6, (float)n7, 0.25f);
        vertexConsumer.addVertex(matrix4f, vector4fArray[n2].x(), vector4fArray[n2].y(), vector4fArray[n2].z()).setColor((float)n5, (float)n6, (float)n7, 0.25f);
        vertexConsumer.addVertex(matrix4f, vector4fArray[n3].x(), vector4fArray[n3].y(), vector4fArray[n3].z()).setColor((float)n5, (float)n6, (float)n7, 0.25f);
        vertexConsumer.addVertex(matrix4f, vector4fArray[n4].x(), vector4fArray[n4].y(), vector4fArray[n4].z()).setColor((float)n5, (float)n6, (float)n7, 0.25f);
    }
}

