/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 */
package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.StructuresDebugPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class StructureRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;
    private final Map<ResourceKey<Level>, Map<String, BoundingBox>> postMainBoxes = Maps.newIdentityHashMap();
    private final Map<ResourceKey<Level>, Map<String, StructuresDebugPayload.PieceInfo>> postPieces = Maps.newIdentityHashMap();
    private static final int MAX_RENDER_DIST = 500;

    public StructureRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double d2, double d3) {
        Object object;
        Camera camera = this.minecraft.gameRenderer.getMainCamera();
        ResourceKey<Level> resourceKey = this.minecraft.level.dimension();
        BlockPos blockPos = BlockPos.containing(camera.getPosition().x, 0.0, camera.getPosition().z);
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.lines());
        if (this.postMainBoxes.containsKey(resourceKey)) {
            object = this.postMainBoxes.get(resourceKey).values().iterator();
            while (object.hasNext()) {
                BoundingBox boundingBox = (BoundingBox)object.next();
                if (!blockPos.closerThan(boundingBox.getCenter(), 500.0)) continue;
                ShapeRenderer.renderLineBox(poseStack, vertexConsumer, (double)boundingBox.minX() - d, (double)boundingBox.minY() - d2, (double)boundingBox.minZ() - d3, (double)(boundingBox.maxX() + 1) - d, (double)(boundingBox.maxY() + 1) - d2, (double)(boundingBox.maxZ() + 1) - d3, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
            }
        }
        if ((object = this.postPieces.get(resourceKey)) != null) {
            for (StructuresDebugPayload.PieceInfo pieceInfo : object.values()) {
                BoundingBox boundingBox = pieceInfo.boundingBox();
                if (!blockPos.closerThan(boundingBox.getCenter(), 500.0)) continue;
                if (pieceInfo.isStart()) {
                    ShapeRenderer.renderLineBox(poseStack, vertexConsumer, (double)boundingBox.minX() - d, (double)boundingBox.minY() - d2, (double)boundingBox.minZ() - d3, (double)(boundingBox.maxX() + 1) - d, (double)(boundingBox.maxY() + 1) - d2, (double)(boundingBox.maxZ() + 1) - d3, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f);
                    continue;
                }
                ShapeRenderer.renderLineBox(poseStack, vertexConsumer, (double)boundingBox.minX() - d, (double)boundingBox.minY() - d2, (double)boundingBox.minZ() - d3, (double)(boundingBox.maxX() + 1) - d, (double)(boundingBox.maxY() + 1) - d2, (double)(boundingBox.maxZ() + 1) - d3, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f);
            }
        }
    }

    public void addBoundingBox(BoundingBox boundingBox, List<StructuresDebugPayload.PieceInfo> list, ResourceKey<Level> resourceKey2) {
        this.postMainBoxes.computeIfAbsent(resourceKey2, resourceKey -> new HashMap()).put(boundingBox.toString(), boundingBox);
        Map map = this.postPieces.computeIfAbsent(resourceKey2, resourceKey -> new HashMap());
        for (StructuresDebugPayload.PieceInfo pieceInfo : list) {
            map.put(pieceInfo.boundingBox().toString(), pieceInfo);
        }
    }

    @Override
    public void clear() {
        this.postMainBoxes.clear();
        this.postPieces.clear();
    }
}

