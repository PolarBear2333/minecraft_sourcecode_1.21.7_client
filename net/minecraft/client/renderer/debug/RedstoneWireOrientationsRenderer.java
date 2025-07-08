/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  org.joml.Vector3f
 */
package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.network.protocol.common.custom.RedstoneWireOrientationsDebugPayload;
import net.minecraft.world.level.redstone.Orientation;
import org.joml.Vector3f;

public class RedstoneWireOrientationsRenderer
implements DebugRenderer.SimpleDebugRenderer {
    public static final int TIMEOUT = 200;
    private final Minecraft minecraft;
    private final List<RedstoneWireOrientationsDebugPayload> updatedWires = Lists.newArrayList();

    RedstoneWireOrientationsRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public void addWireOrientations(RedstoneWireOrientationsDebugPayload redstoneWireOrientationsDebugPayload) {
        this.updatedWires.add(redstoneWireOrientationsDebugPayload);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double d2, double d3) {
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.lines());
        long l = this.minecraft.level.getGameTime();
        Iterator<RedstoneWireOrientationsDebugPayload> iterator = this.updatedWires.iterator();
        while (iterator.hasNext()) {
            RedstoneWireOrientationsDebugPayload redstoneWireOrientationsDebugPayload = iterator.next();
            long l2 = l - redstoneWireOrientationsDebugPayload.time();
            if (l2 > 200L) {
                iterator.remove();
                continue;
            }
            for (RedstoneWireOrientationsDebugPayload.Wire wire : redstoneWireOrientationsDebugPayload.wires()) {
                Vector3f vector3f = wire.pos().getBottomCenter().subtract(d, d2 - 0.1, d3).toVector3f();
                Orientation orientation = wire.orientation();
                ShapeRenderer.renderVector(poseStack, vertexConsumer, vector3f, orientation.getFront().getUnitVec3().scale(0.5), -16776961);
                ShapeRenderer.renderVector(poseStack, vertexConsumer, vector3f, orientation.getUp().getUnitVec3().scale(0.4), -65536);
                ShapeRenderer.renderVector(poseStack, vertexConsumer, vector3f, orientation.getSide().getUnitVec3().scale(0.3), -256);
            }
        }
    }
}

