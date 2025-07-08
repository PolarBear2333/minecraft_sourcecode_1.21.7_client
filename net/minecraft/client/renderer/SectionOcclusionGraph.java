/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Queues
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.longs.LongIterator
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  javax.annotation.Nullable
 *  org.joml.Vector3d
 *  org.joml.Vector3dc
 *  org.slf4j.Logger
 */
package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.Octree;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.CompiledSectionMesh;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkTrackingView;
import net.minecraft.util.Mth;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.slf4j.Logger;

public class SectionOcclusionGraph {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final int MINIMUM_ADVANCED_CULLING_DISTANCE = 60;
    private static final int MINIMUM_ADVANCED_CULLING_SECTION_DISTANCE = SectionPos.blockToSectionCoord(60);
    private static final double CEILED_SECTION_DIAGONAL = Math.ceil(Math.sqrt(3.0) * 16.0);
    private boolean needsFullUpdate = true;
    @Nullable
    private Future<?> fullUpdateTask;
    @Nullable
    private ViewArea viewArea;
    private final AtomicReference<GraphState> currentGraph = new AtomicReference();
    private final AtomicReference<GraphEvents> nextGraphEvents = new AtomicReference();
    private final AtomicBoolean needsFrustumUpdate = new AtomicBoolean(false);

    public void waitAndReset(@Nullable ViewArea viewArea) {
        if (this.fullUpdateTask != null) {
            try {
                this.fullUpdateTask.get();
                this.fullUpdateTask = null;
            }
            catch (Exception exception) {
                LOGGER.warn("Full update failed", (Throwable)exception);
            }
        }
        this.viewArea = viewArea;
        if (viewArea != null) {
            this.currentGraph.set(new GraphState(viewArea));
            this.invalidate();
        } else {
            this.currentGraph.set(null);
        }
    }

    public void invalidate() {
        this.needsFullUpdate = true;
    }

    public void addSectionsInFrustum(Frustum frustum, List<SectionRenderDispatcher.RenderSection> list, List<SectionRenderDispatcher.RenderSection> list2) {
        this.currentGraph.get().storage().sectionTree.visitNodes((node, bl, n, bl2) -> {
            SectionRenderDispatcher.RenderSection renderSection = node.getSection();
            if (renderSection != null) {
                list.add(renderSection);
                if (bl2) {
                    list2.add(renderSection);
                }
            }
        }, frustum, 32);
    }

    public boolean consumeFrustumUpdate() {
        return this.needsFrustumUpdate.compareAndSet(true, false);
    }

    public void onChunkReadyToRender(ChunkPos chunkPos) {
        GraphEvents graphEvents;
        GraphEvents graphEvents2 = this.nextGraphEvents.get();
        if (graphEvents2 != null) {
            this.addNeighbors(graphEvents2, chunkPos);
        }
        if ((graphEvents = this.currentGraph.get().events) != graphEvents2) {
            this.addNeighbors(graphEvents, chunkPos);
        }
    }

    public void schedulePropagationFrom(SectionRenderDispatcher.RenderSection renderSection) {
        GraphEvents graphEvents;
        GraphEvents graphEvents2 = this.nextGraphEvents.get();
        if (graphEvents2 != null) {
            graphEvents2.sectionsToPropagateFrom.add(renderSection);
        }
        if ((graphEvents = this.currentGraph.get().events) != graphEvents2) {
            graphEvents.sectionsToPropagateFrom.add(renderSection);
        }
    }

    public void update(boolean bl, Camera camera, Frustum frustum, List<SectionRenderDispatcher.RenderSection> list, LongOpenHashSet longOpenHashSet) {
        Vec3 vec3 = camera.getPosition();
        if (this.needsFullUpdate && (this.fullUpdateTask == null || this.fullUpdateTask.isDone())) {
            this.scheduleFullUpdate(bl, camera, vec3, longOpenHashSet);
        }
        this.runPartialUpdate(bl, frustum, list, vec3, longOpenHashSet);
    }

    private void scheduleFullUpdate(boolean bl, Camera camera, Vec3 vec3, LongOpenHashSet longOpenHashSet) {
        this.needsFullUpdate = false;
        LongOpenHashSet longOpenHashSet2 = longOpenHashSet.clone();
        this.fullUpdateTask = CompletableFuture.runAsync(() -> {
            GraphState graphState = new GraphState(this.viewArea);
            this.nextGraphEvents.set(graphState.events);
            ArrayDeque arrayDeque = Queues.newArrayDeque();
            this.initializeQueueForFullUpdate(camera, arrayDeque);
            arrayDeque.forEach(node -> graphState.storage.sectionToNodeMap.put(node.section, (Node)node));
            this.runUpdates(graphState.storage, vec3, arrayDeque, bl, renderSection -> {}, longOpenHashSet2);
            this.currentGraph.set(graphState);
            this.nextGraphEvents.set(null);
            this.needsFrustumUpdate.set(true);
        }, Util.backgroundExecutor());
    }

    private void runPartialUpdate(boolean bl, Frustum frustum, List<SectionRenderDispatcher.RenderSection> list, Vec3 vec3, LongOpenHashSet longOpenHashSet) {
        GraphState graphState = this.currentGraph.get();
        this.queueSectionsWithNewNeighbors(graphState);
        if (!graphState.events.sectionsToPropagateFrom.isEmpty()) {
            Object object;
            Object object2;
            ArrayDeque arrayDeque = Queues.newArrayDeque();
            while (!graphState.events.sectionsToPropagateFrom.isEmpty()) {
                object2 = (SectionRenderDispatcher.RenderSection)graphState.events.sectionsToPropagateFrom.poll();
                object = graphState.storage.sectionToNodeMap.get((SectionRenderDispatcher.RenderSection)object2);
                if (object == null || ((Node)object).section != object2) continue;
                arrayDeque.add(object);
            }
            object2 = LevelRenderer.offsetFrustum(frustum);
            object = arg_0 -> this.lambda$runPartialUpdate$4((Frustum)object2, arg_0);
            this.runUpdates(graphState.storage, vec3, arrayDeque, bl, (Consumer<SectionRenderDispatcher.RenderSection>)object, longOpenHashSet);
        }
    }

    private void queueSectionsWithNewNeighbors(GraphState graphState) {
        LongIterator longIterator = graphState.events.chunksWhichReceivedNeighbors.iterator();
        while (longIterator.hasNext()) {
            long l = longIterator.nextLong();
            List list = (List)graphState.storage.chunksWaitingForNeighbors.get(l);
            if (list == null || !((SectionRenderDispatcher.RenderSection)list.get(0)).hasAllNeighbors()) continue;
            graphState.events.sectionsToPropagateFrom.addAll(list);
            graphState.storage.chunksWaitingForNeighbors.remove(l);
        }
        graphState.events.chunksWhichReceivedNeighbors.clear();
    }

    private void addNeighbors(GraphEvents graphEvents, ChunkPos chunkPos) {
        graphEvents.chunksWhichReceivedNeighbors.add(ChunkPos.asLong(chunkPos.x - 1, chunkPos.z));
        graphEvents.chunksWhichReceivedNeighbors.add(ChunkPos.asLong(chunkPos.x, chunkPos.z - 1));
        graphEvents.chunksWhichReceivedNeighbors.add(ChunkPos.asLong(chunkPos.x + 1, chunkPos.z));
        graphEvents.chunksWhichReceivedNeighbors.add(ChunkPos.asLong(chunkPos.x, chunkPos.z + 1));
        graphEvents.chunksWhichReceivedNeighbors.add(ChunkPos.asLong(chunkPos.x - 1, chunkPos.z - 1));
        graphEvents.chunksWhichReceivedNeighbors.add(ChunkPos.asLong(chunkPos.x - 1, chunkPos.z + 1));
        graphEvents.chunksWhichReceivedNeighbors.add(ChunkPos.asLong(chunkPos.x + 1, chunkPos.z - 1));
        graphEvents.chunksWhichReceivedNeighbors.add(ChunkPos.asLong(chunkPos.x + 1, chunkPos.z + 1));
    }

    private void initializeQueueForFullUpdate(Camera camera, Queue<Node> queue) {
        BlockPos blockPos = camera.getBlockPosition();
        long l = SectionPos.asLong(blockPos);
        int n = SectionPos.y(l);
        SectionRenderDispatcher.RenderSection renderSection = this.viewArea.getRenderSection(l);
        if (renderSection == null) {
            LevelHeightAccessor levelHeightAccessor = this.viewArea.getLevelHeightAccessor();
            boolean bl = n < levelHeightAccessor.getMinSectionY();
            int n2 = bl ? levelHeightAccessor.getMinSectionY() : levelHeightAccessor.getMaxSectionY();
            int n3 = this.viewArea.getViewDistance();
            ArrayList arrayList = Lists.newArrayList();
            int n4 = SectionPos.x(l);
            int n5 = SectionPos.z(l);
            for (int i = -n3; i <= n3; ++i) {
                for (int j = -n3; j <= n3; ++j) {
                    SectionRenderDispatcher.RenderSection renderSection2 = this.viewArea.getRenderSection(SectionPos.asLong(i + n4, n2, j + n5));
                    if (renderSection2 == null || !this.isInViewDistance(l, renderSection2.getSectionNode())) continue;
                    Direction direction = bl ? Direction.UP : Direction.DOWN;
                    Node node2 = new Node(renderSection2, direction, 0);
                    node2.setDirections(node2.directions, direction);
                    if (i > 0) {
                        node2.setDirections(node2.directions, Direction.EAST);
                    } else if (i < 0) {
                        node2.setDirections(node2.directions, Direction.WEST);
                    }
                    if (j > 0) {
                        node2.setDirections(node2.directions, Direction.SOUTH);
                    } else if (j < 0) {
                        node2.setDirections(node2.directions, Direction.NORTH);
                    }
                    arrayList.add(node2);
                }
            }
            arrayList.sort(Comparator.comparingDouble(node -> blockPos.distSqr(SectionPos.of(node.section.getSectionNode()).center())));
            queue.addAll(arrayList);
        } else {
            queue.add(new Node(renderSection, null, 0));
        }
    }

    private void runUpdates(GraphStorage graphStorage, Vec3 vec3, Queue<Node> queue, boolean bl, Consumer<SectionRenderDispatcher.RenderSection> consumer, LongOpenHashSet longOpenHashSet) {
        SectionPos sectionPos = SectionPos.of(vec3);
        long l2 = sectionPos.asLong();
        BlockPos blockPos = sectionPos.center();
        while (!queue.isEmpty()) {
            long l3;
            Node node = queue.poll();
            SectionRenderDispatcher.RenderSection renderSection = node.section;
            if (!longOpenHashSet.contains(node.section.getSectionNode())) {
                if (graphStorage.sectionTree.add(node.section)) {
                    consumer.accept(node.section);
                }
            } else {
                node.section.sectionMesh.compareAndSet(CompiledSectionMesh.UNCOMPILED, CompiledSectionMesh.EMPTY);
            }
            boolean bl2 = Math.abs(SectionPos.x(l3 = renderSection.getSectionNode()) - sectionPos.x()) > MINIMUM_ADVANCED_CULLING_SECTION_DISTANCE || Math.abs(SectionPos.y(l3) - sectionPos.y()) > MINIMUM_ADVANCED_CULLING_SECTION_DISTANCE || Math.abs(SectionPos.z(l3) - sectionPos.z()) > MINIMUM_ADVANCED_CULLING_SECTION_DISTANCE;
            for (Direction direction : DIRECTIONS) {
                int n;
                int n2;
                Object object;
                SectionRenderDispatcher.RenderSection renderSection2 = this.getRelativeFrom(l2, renderSection, direction);
                if (renderSection2 == null || bl && node.hasDirection(direction.getOpposite())) continue;
                if (bl && node.hasSourceDirections()) {
                    object = renderSection.getSectionMesh();
                    n2 = 0;
                    for (n = 0; n < DIRECTIONS.length; ++n) {
                        if (!node.hasSourceDirection(n) || !object.facesCanSeeEachother(DIRECTIONS[n].getOpposite(), direction)) continue;
                        n2 = 1;
                        break;
                    }
                    if (n2 == 0) continue;
                }
                if (bl && bl2) {
                    boolean bl3;
                    boolean bl4;
                    int n3 = SectionPos.sectionToBlockCoord(SectionPos.x(l3));
                    n2 = SectionPos.sectionToBlockCoord(SectionPos.y(l3));
                    n = SectionPos.sectionToBlockCoord(SectionPos.z(l3));
                    boolean bl5 = direction.getAxis() == Direction.Axis.X ? blockPos.getX() > n3 : (bl4 = blockPos.getX() < n3);
                    boolean bl6 = direction.getAxis() == Direction.Axis.Y ? blockPos.getY() > n2 : (bl3 = blockPos.getY() < n2);
                    boolean bl7 = direction.getAxis() == Direction.Axis.Z ? blockPos.getZ() > n : blockPos.getZ() < n;
                    Vector3d vector3d = new Vector3d((double)(n3 + (bl4 ? 16 : 0)), (double)(n2 + (bl3 ? 16 : 0)), (double)(n + (bl7 ? 16 : 0)));
                    Vector3d vector3d2 = new Vector3d(vec3.x, vec3.y, vec3.z).sub((Vector3dc)vector3d).normalize().mul(CEILED_SECTION_DIAGONAL);
                    boolean bl8 = true;
                    while (vector3d.distanceSquared(vec3.x, vec3.y, vec3.z) > 3600.0) {
                        vector3d.add((Vector3dc)vector3d2);
                        LevelHeightAccessor levelHeightAccessor = this.viewArea.getLevelHeightAccessor();
                        if (vector3d.y > (double)levelHeightAccessor.getMaxY() || vector3d.y < (double)levelHeightAccessor.getMinY()) break;
                        SectionRenderDispatcher.RenderSection renderSection3 = this.viewArea.getRenderSectionAt(BlockPos.containing(vector3d.x, vector3d.y, vector3d.z));
                        if (renderSection3 != null && graphStorage.sectionToNodeMap.get(renderSection3) != null) continue;
                        bl8 = false;
                        break;
                    }
                    if (!bl8) continue;
                }
                if ((object = graphStorage.sectionToNodeMap.get(renderSection2)) != null) {
                    ((Node)object).addSourceDirection(direction);
                    continue;
                }
                Node node2 = new Node(renderSection2, direction, node.step + 1);
                node2.setDirections(node.directions, direction);
                if (renderSection2.hasAllNeighbors()) {
                    queue.add(node2);
                    graphStorage.sectionToNodeMap.put(renderSection2, node2);
                    continue;
                }
                if (!this.isInViewDistance(l2, renderSection2.getSectionNode())) continue;
                graphStorage.sectionToNodeMap.put(renderSection2, node2);
                long l4 = SectionPos.sectionToChunk(renderSection2.getSectionNode());
                ((List)graphStorage.chunksWaitingForNeighbors.computeIfAbsent(l4, l -> new ArrayList())).add(renderSection2);
            }
        }
    }

    private boolean isInViewDistance(long l, long l2) {
        return ChunkTrackingView.isInViewDistance(SectionPos.x(l), SectionPos.z(l), this.viewArea.getViewDistance(), SectionPos.x(l2), SectionPos.z(l2));
    }

    @Nullable
    private SectionRenderDispatcher.RenderSection getRelativeFrom(long l, SectionRenderDispatcher.RenderSection renderSection, Direction direction) {
        long l2 = renderSection.getNeighborSectionNode(direction);
        if (!this.isInViewDistance(l, l2)) {
            return null;
        }
        if (Mth.abs(SectionPos.y(l) - SectionPos.y(l2)) > this.viewArea.getViewDistance()) {
            return null;
        }
        return this.viewArea.getRenderSection(l2);
    }

    @Nullable
    @VisibleForDebug
    public Node getNode(SectionRenderDispatcher.RenderSection renderSection) {
        return this.currentGraph.get().storage.sectionToNodeMap.get(renderSection);
    }

    public Octree getOctree() {
        return this.currentGraph.get().storage.sectionTree;
    }

    private /* synthetic */ void lambda$runPartialUpdate$4(Frustum frustum, SectionRenderDispatcher.RenderSection renderSection) {
        if (frustum.isVisible(renderSection.getBoundingBox())) {
            this.needsFrustumUpdate.set(true);
        }
    }

    static final class GraphState
    extends Record {
        final GraphStorage storage;
        final GraphEvents events;

        GraphState(ViewArea viewArea) {
            this(new GraphStorage(viewArea), new GraphEvents());
        }

        private GraphState(GraphStorage graphStorage, GraphEvents graphEvents) {
            this.storage = graphStorage;
            this.events = graphEvents;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{GraphState.class, "storage;events", "storage", "events"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{GraphState.class, "storage;events", "storage", "events"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{GraphState.class, "storage;events", "storage", "events"}, this, object);
        }

        public GraphStorage storage() {
            return this.storage;
        }

        public GraphEvents events() {
            return this.events;
        }
    }

    static class GraphStorage {
        public final SectionToNodeMap sectionToNodeMap;
        public final Octree sectionTree;
        public final Long2ObjectMap<List<SectionRenderDispatcher.RenderSection>> chunksWaitingForNeighbors;

        public GraphStorage(ViewArea viewArea) {
            this.sectionToNodeMap = new SectionToNodeMap(viewArea.sections.length);
            this.sectionTree = new Octree(viewArea.getCameraSectionPos(), viewArea.getViewDistance(), viewArea.sectionGridSizeY, viewArea.level.getMinY());
            this.chunksWaitingForNeighbors = new Long2ObjectOpenHashMap();
        }
    }

    static final class GraphEvents
    extends Record {
        final LongSet chunksWhichReceivedNeighbors;
        final BlockingQueue<SectionRenderDispatcher.RenderSection> sectionsToPropagateFrom;

        GraphEvents() {
            this((LongSet)new LongOpenHashSet(), new LinkedBlockingQueue<SectionRenderDispatcher.RenderSection>());
        }

        private GraphEvents(LongSet longSet, BlockingQueue<SectionRenderDispatcher.RenderSection> blockingQueue) {
            this.chunksWhichReceivedNeighbors = longSet;
            this.sectionsToPropagateFrom = blockingQueue;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{GraphEvents.class, "chunksWhichReceivedNeighbors;sectionsToPropagateFrom", "chunksWhichReceivedNeighbors", "sectionsToPropagateFrom"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{GraphEvents.class, "chunksWhichReceivedNeighbors;sectionsToPropagateFrom", "chunksWhichReceivedNeighbors", "sectionsToPropagateFrom"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{GraphEvents.class, "chunksWhichReceivedNeighbors;sectionsToPropagateFrom", "chunksWhichReceivedNeighbors", "sectionsToPropagateFrom"}, this, object);
        }

        public LongSet chunksWhichReceivedNeighbors() {
            return this.chunksWhichReceivedNeighbors;
        }

        public BlockingQueue<SectionRenderDispatcher.RenderSection> sectionsToPropagateFrom() {
            return this.sectionsToPropagateFrom;
        }
    }

    static class SectionToNodeMap {
        private final Node[] nodes;

        SectionToNodeMap(int n) {
            this.nodes = new Node[n];
        }

        public void put(SectionRenderDispatcher.RenderSection renderSection, Node node) {
            this.nodes[renderSection.index] = node;
        }

        @Nullable
        public Node get(SectionRenderDispatcher.RenderSection renderSection) {
            int n = renderSection.index;
            if (n < 0 || n >= this.nodes.length) {
                return null;
            }
            return this.nodes[n];
        }
    }

    @VisibleForDebug
    public static class Node {
        @VisibleForDebug
        protected final SectionRenderDispatcher.RenderSection section;
        private byte sourceDirections;
        byte directions;
        @VisibleForDebug
        public final int step;

        Node(SectionRenderDispatcher.RenderSection renderSection, @Nullable Direction direction, int n) {
            this.section = renderSection;
            if (direction != null) {
                this.addSourceDirection(direction);
            }
            this.step = n;
        }

        void setDirections(byte by, Direction direction) {
            this.directions = (byte)(this.directions | (by | 1 << direction.ordinal()));
        }

        boolean hasDirection(Direction direction) {
            return (this.directions & 1 << direction.ordinal()) > 0;
        }

        void addSourceDirection(Direction direction) {
            this.sourceDirections = (byte)(this.sourceDirections | (this.sourceDirections | 1 << direction.ordinal()));
        }

        @VisibleForDebug
        public boolean hasSourceDirection(int n) {
            return (this.sourceDirections & 1 << n) > 0;
        }

        boolean hasSourceDirections() {
            return this.sourceDirections != 0;
        }

        public int hashCode() {
            return Long.hashCode(this.section.getSectionNode());
        }

        public boolean equals(Object object) {
            if (!(object instanceof Node)) {
                return false;
            }
            Node node = (Node)object;
            return this.section.getSectionNode() == node.section.getSectionNode();
        }
    }
}

