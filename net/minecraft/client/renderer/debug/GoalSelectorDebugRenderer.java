/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 */
package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.GoalDebugPayload;

public class GoalSelectorDebugRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private static final int MAX_RENDER_DIST = 160;
    private final Minecraft minecraft;
    private final Int2ObjectMap<EntityGoalInfo> goalSelectors = new Int2ObjectOpenHashMap();

    @Override
    public void clear() {
        this.goalSelectors.clear();
    }

    public void addGoalSelector(int n, BlockPos blockPos, List<GoalDebugPayload.DebugGoal> list) {
        this.goalSelectors.put(n, (Object)new EntityGoalInfo(blockPos, list));
    }

    public void removeGoalSelector(int n) {
        this.goalSelectors.remove(n);
    }

    public GoalSelectorDebugRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double d2, double d3) {
        Camera camera = this.minecraft.gameRenderer.getMainCamera();
        BlockPos blockPos = BlockPos.containing(camera.getPosition().x, 0.0, camera.getPosition().z);
        for (EntityGoalInfo entityGoalInfo : this.goalSelectors.values()) {
            BlockPos blockPos2 = entityGoalInfo.entityPos;
            if (!blockPos.closerThan(blockPos2, 160.0)) continue;
            for (int i = 0; i < entityGoalInfo.goals.size(); ++i) {
                GoalDebugPayload.DebugGoal debugGoal = entityGoalInfo.goals.get(i);
                double d4 = (double)blockPos2.getX() + 0.5;
                double d5 = (double)blockPos2.getY() + 2.0 + (double)i * 0.25;
                double d6 = (double)blockPos2.getZ() + 0.5;
                int n = debugGoal.isRunning() ? -16711936 : -3355444;
                DebugRenderer.renderFloatingText(poseStack, multiBufferSource, debugGoal.name(), d4, d5, d6, n);
            }
        }
    }

    static final class EntityGoalInfo
    extends Record {
        final BlockPos entityPos;
        final List<GoalDebugPayload.DebugGoal> goals;

        EntityGoalInfo(BlockPos blockPos, List<GoalDebugPayload.DebugGoal> list) {
            this.entityPos = blockPos;
            this.goals = list;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{EntityGoalInfo.class, "entityPos;goals", "entityPos", "goals"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{EntityGoalInfo.class, "entityPos;goals", "entityPos", "goals"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{EntityGoalInfo.class, "entityPos;goals", "entityPos", "goals"}, this, object);
        }

        public BlockPos entityPos() {
            return this.entityPos;
        }

        public List<GoalDebugPayload.DebugGoal> goals() {
            return this.goals;
        }
    }
}

