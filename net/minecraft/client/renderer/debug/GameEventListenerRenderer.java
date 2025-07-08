/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 */
package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;

public class GameEventListenerRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;
    private static final int LISTENER_RENDER_DIST = 32;
    private static final float BOX_HEIGHT = 1.0f;
    private final List<TrackedGameEvent> trackedGameEvents = Lists.newArrayList();
    private final List<TrackedListener> trackedListeners = Lists.newArrayList();

    public GameEventListenerRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double d2, double d3) {
        ClientLevel clientLevel = this.minecraft.level;
        if (clientLevel == null) {
            this.trackedGameEvents.clear();
            this.trackedListeners.clear();
            return;
        }
        Vec3 vec32 = new Vec3(d, 0.0, d3);
        this.trackedGameEvents.removeIf(TrackedGameEvent::isExpired);
        this.trackedListeners.removeIf(trackedListener -> trackedListener.isExpired(clientLevel, vec32));
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.lines());
        for (TrackedListener iterator : this.trackedListeners) {
            iterator.getPosition(clientLevel).ifPresent(vec3 -> {
                double d4 = vec3.x() - (double)iterator.getListenerRadius();
                double d5 = vec3.y() - (double)iterator.getListenerRadius();
                double d6 = vec3.z() - (double)iterator.getListenerRadius();
                double d7 = vec3.x() + (double)iterator.getListenerRadius();
                double d8 = vec3.y() + (double)iterator.getListenerRadius();
                double d9 = vec3.z() + (double)iterator.getListenerRadius();
                DebugRenderer.renderVoxelShape(poseStack, vertexConsumer, Shapes.create(new AABB(d4, d5, d6, d7, d8, d9)), -d, -d2, -d3, 1.0f, 1.0f, 0.0f, 0.35f, true);
            });
        }
        VertexConsumer vertexConsumer2 = multiBufferSource.getBuffer(RenderType.debugFilledBox());
        for (TrackedListener trackedListener2 : this.trackedListeners) {
            trackedListener2.getPosition(clientLevel).ifPresent(vec3 -> ShapeRenderer.addChainedFilledBoxVertices(poseStack, vertexConsumer2, vec3.x() - 0.25 - d, vec3.y() - d2, vec3.z() - 0.25 - d3, vec3.x() + 0.25 - d, vec3.y() - d2 + 1.0, vec3.z() + 0.25 - d3, 1.0f, 1.0f, 0.0f, 0.35f));
        }
        for (TrackedListener trackedListener3 : this.trackedListeners) {
            trackedListener3.getPosition(clientLevel).ifPresent(vec3 -> {
                DebugRenderer.renderFloatingText(poseStack, multiBufferSource, "Listener Origin", vec3.x(), vec3.y() + (double)1.8f, vec3.z(), -1, 0.025f);
                DebugRenderer.renderFloatingText(poseStack, multiBufferSource, BlockPos.containing(vec3).toString(), vec3.x(), vec3.y() + 1.5, vec3.z(), -6959665, 0.025f);
            });
        }
        for (TrackedGameEvent trackedGameEvent : this.trackedGameEvents) {
            Vec3 vec33 = trackedGameEvent.position;
            double d4 = 0.2f;
            double d5 = vec33.x - (double)0.2f;
            double d6 = vec33.y - (double)0.2f;
            double d7 = vec33.z - (double)0.2f;
            double d8 = vec33.x + (double)0.2f;
            double d9 = vec33.y + (double)0.2f + 0.5;
            double d10 = vec33.z + (double)0.2f;
            GameEventListenerRenderer.renderFilledBox(poseStack, multiBufferSource, new AABB(d5, d6, d7, d8, d9, d10), 1.0f, 1.0f, 1.0f, 0.2f);
            DebugRenderer.renderFloatingText(poseStack, multiBufferSource, trackedGameEvent.gameEvent.location().toString(), vec33.x, vec33.y + (double)0.85f, vec33.z, -7564911, 0.0075f);
        }
    }

    private static void renderFilledBox(PoseStack poseStack, MultiBufferSource multiBufferSource, AABB aABB, float f, float f2, float f3, float f4) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (!camera.isInitialized()) {
            return;
        }
        Vec3 vec3 = camera.getPosition().reverse();
        DebugRenderer.renderFilledBox(poseStack, multiBufferSource, aABB.move(vec3), f, f2, f3, f4);
    }

    public void trackGameEvent(ResourceKey<GameEvent> resourceKey, Vec3 vec3) {
        this.trackedGameEvents.add(new TrackedGameEvent(Util.getMillis(), resourceKey, vec3));
    }

    public void trackListener(PositionSource positionSource, int n) {
        this.trackedListeners.add(new TrackedListener(positionSource, n));
    }

    static class TrackedListener
    implements GameEventListener {
        public final PositionSource listenerSource;
        public final int listenerRange;

        public TrackedListener(PositionSource positionSource, int n) {
            this.listenerSource = positionSource;
            this.listenerRange = n;
        }

        public boolean isExpired(Level level, Vec3 vec3) {
            return this.listenerSource.getPosition(level).filter(vec32 -> vec32.distanceToSqr(vec3) <= 1024.0).isPresent();
        }

        public Optional<Vec3> getPosition(Level level) {
            return this.listenerSource.getPosition(level);
        }

        @Override
        public PositionSource getListenerSource() {
            return this.listenerSource;
        }

        @Override
        public int getListenerRadius() {
            return this.listenerRange;
        }

        @Override
        public boolean handleGameEvent(ServerLevel serverLevel, Holder<GameEvent> holder, GameEvent.Context context, Vec3 vec3) {
            return false;
        }
    }

    static final class TrackedGameEvent
    extends Record {
        private final long timeStamp;
        final ResourceKey<GameEvent> gameEvent;
        final Vec3 position;

        TrackedGameEvent(long l, ResourceKey<GameEvent> resourceKey, Vec3 vec3) {
            this.timeStamp = l;
            this.gameEvent = resourceKey;
            this.position = vec3;
        }

        public boolean isExpired() {
            return Util.getMillis() - this.timeStamp > 3000L;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{TrackedGameEvent.class, "timeStamp;gameEvent;position", "timeStamp", "gameEvent", "position"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{TrackedGameEvent.class, "timeStamp;gameEvent;position", "timeStamp", "gameEvent", "position"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{TrackedGameEvent.class, "timeStamp;gameEvent;position", "timeStamp", "gameEvent", "position"}, this, object);
        }

        public long timeStamp() {
            return this.timeStamp;
        }

        public ResourceKey<GameEvent> gameEvent() {
            return this.gameEvent;
        }

        public Vec3 position() {
            return this.position;
        }
    }
}

