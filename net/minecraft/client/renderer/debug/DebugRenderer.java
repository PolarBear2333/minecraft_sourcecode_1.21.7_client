/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.BeeDebugRenderer;
import net.minecraft.client.renderer.debug.BrainDebugRenderer;
import net.minecraft.client.renderer.debug.BreezeDebugRenderer;
import net.minecraft.client.renderer.debug.ChunkBorderRenderer;
import net.minecraft.client.renderer.debug.ChunkCullingDebugRenderer;
import net.minecraft.client.renderer.debug.ChunkDebugRenderer;
import net.minecraft.client.renderer.debug.CollisionBoxRenderer;
import net.minecraft.client.renderer.debug.GameEventListenerRenderer;
import net.minecraft.client.renderer.debug.GameTestDebugRenderer;
import net.minecraft.client.renderer.debug.GoalSelectorDebugRenderer;
import net.minecraft.client.renderer.debug.HeightMapRenderer;
import net.minecraft.client.renderer.debug.LightDebugRenderer;
import net.minecraft.client.renderer.debug.LightSectionDebugRenderer;
import net.minecraft.client.renderer.debug.NeighborsUpdateRenderer;
import net.minecraft.client.renderer.debug.OctreeDebugRenderer;
import net.minecraft.client.renderer.debug.PathfindingRenderer;
import net.minecraft.client.renderer.debug.RaidDebugRenderer;
import net.minecraft.client.renderer.debug.RedstoneWireOrientationsRenderer;
import net.minecraft.client.renderer.debug.SolidFaceRenderer;
import net.minecraft.client.renderer.debug.StructureRenderer;
import net.minecraft.client.renderer.debug.SupportBlockRenderer;
import net.minecraft.client.renderer.debug.VillageSectionsDebugRenderer;
import net.minecraft.client.renderer.debug.WaterDebugRenderer;
import net.minecraft.client.renderer.debug.WorldGenAttemptRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Quaternionfc;

public class DebugRenderer {
    public final PathfindingRenderer pathfindingRenderer = new PathfindingRenderer();
    public final SimpleDebugRenderer waterDebugRenderer;
    public final SimpleDebugRenderer chunkBorderRenderer;
    public final SimpleDebugRenderer heightMapRenderer;
    public final SimpleDebugRenderer collisionBoxRenderer;
    public final SimpleDebugRenderer supportBlockRenderer;
    public final NeighborsUpdateRenderer neighborsUpdateRenderer;
    public final RedstoneWireOrientationsRenderer redstoneWireOrientationsRenderer;
    public final StructureRenderer structureRenderer;
    public final SimpleDebugRenderer lightDebugRenderer;
    public final SimpleDebugRenderer worldGenAttemptRenderer;
    public final SimpleDebugRenderer solidFaceRenderer;
    public final SimpleDebugRenderer chunkRenderer;
    public final BrainDebugRenderer brainDebugRenderer;
    public final VillageSectionsDebugRenderer villageSectionsDebugRenderer;
    public final BeeDebugRenderer beeDebugRenderer;
    public final RaidDebugRenderer raidDebugRenderer;
    public final GoalSelectorDebugRenderer goalSelectorRenderer;
    public final GameTestDebugRenderer gameTestDebugRenderer;
    public final GameEventListenerRenderer gameEventListenerRenderer;
    public final LightSectionDebugRenderer skyLightSectionDebugRenderer;
    public final BreezeDebugRenderer breezeDebugRenderer;
    public final ChunkCullingDebugRenderer chunkCullingDebugRenderer;
    public final OctreeDebugRenderer octreeDebugRenderer;
    private boolean renderChunkborder;
    private boolean renderOctree;

    public DebugRenderer(Minecraft minecraft) {
        this.waterDebugRenderer = new WaterDebugRenderer(minecraft);
        this.chunkBorderRenderer = new ChunkBorderRenderer(minecraft);
        this.heightMapRenderer = new HeightMapRenderer(minecraft);
        this.collisionBoxRenderer = new CollisionBoxRenderer(minecraft);
        this.supportBlockRenderer = new SupportBlockRenderer(minecraft);
        this.neighborsUpdateRenderer = new NeighborsUpdateRenderer(minecraft);
        this.redstoneWireOrientationsRenderer = new RedstoneWireOrientationsRenderer(minecraft);
        this.structureRenderer = new StructureRenderer(minecraft);
        this.lightDebugRenderer = new LightDebugRenderer(minecraft);
        this.worldGenAttemptRenderer = new WorldGenAttemptRenderer();
        this.solidFaceRenderer = new SolidFaceRenderer(minecraft);
        this.chunkRenderer = new ChunkDebugRenderer(minecraft);
        this.brainDebugRenderer = new BrainDebugRenderer(minecraft);
        this.villageSectionsDebugRenderer = new VillageSectionsDebugRenderer();
        this.beeDebugRenderer = new BeeDebugRenderer(minecraft);
        this.raidDebugRenderer = new RaidDebugRenderer(minecraft);
        this.goalSelectorRenderer = new GoalSelectorDebugRenderer(minecraft);
        this.gameTestDebugRenderer = new GameTestDebugRenderer();
        this.gameEventListenerRenderer = new GameEventListenerRenderer(minecraft);
        this.skyLightSectionDebugRenderer = new LightSectionDebugRenderer(minecraft, LightLayer.SKY);
        this.breezeDebugRenderer = new BreezeDebugRenderer(minecraft);
        this.chunkCullingDebugRenderer = new ChunkCullingDebugRenderer(minecraft);
        this.octreeDebugRenderer = new OctreeDebugRenderer(minecraft);
    }

    public void clear() {
        this.pathfindingRenderer.clear();
        this.waterDebugRenderer.clear();
        this.chunkBorderRenderer.clear();
        this.heightMapRenderer.clear();
        this.collisionBoxRenderer.clear();
        this.supportBlockRenderer.clear();
        this.neighborsUpdateRenderer.clear();
        this.structureRenderer.clear();
        this.lightDebugRenderer.clear();
        this.worldGenAttemptRenderer.clear();
        this.solidFaceRenderer.clear();
        this.chunkRenderer.clear();
        this.brainDebugRenderer.clear();
        this.villageSectionsDebugRenderer.clear();
        this.beeDebugRenderer.clear();
        this.raidDebugRenderer.clear();
        this.goalSelectorRenderer.clear();
        this.gameTestDebugRenderer.clear();
        this.gameEventListenerRenderer.clear();
        this.skyLightSectionDebugRenderer.clear();
        this.breezeDebugRenderer.clear();
        this.chunkCullingDebugRenderer.clear();
    }

    public boolean switchRenderChunkborder() {
        this.renderChunkborder = !this.renderChunkborder;
        return this.renderChunkborder;
    }

    public boolean toggleRenderOctree() {
        this.renderOctree = !this.renderOctree;
        return this.renderOctree;
    }

    public void render(PoseStack poseStack, Frustum frustum, MultiBufferSource.BufferSource bufferSource, double d, double d2, double d3) {
        if (this.renderChunkborder && !Minecraft.getInstance().showOnlyReducedInfo()) {
            this.chunkBorderRenderer.render(poseStack, bufferSource, d, d2, d3);
        }
        if (this.renderOctree) {
            this.octreeDebugRenderer.render(poseStack, frustum, bufferSource, d, d2, d3);
        }
        this.gameTestDebugRenderer.render(poseStack, bufferSource, d, d2, d3);
    }

    public void renderAfterTranslucents(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, double d, double d2, double d3) {
        this.chunkCullingDebugRenderer.render(poseStack, bufferSource, d, d2, d3);
    }

    public static Optional<Entity> getTargetedEntity(@Nullable Entity entity, int n) {
        int n2;
        AABB aABB;
        Vec3 vec3;
        Vec3 vec32;
        if (entity == null) {
            return Optional.empty();
        }
        Vec3 vec33 = entity.getEyePosition();
        EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(entity, vec33, vec32 = vec33.add(vec3 = entity.getViewVector(1.0f).scale(n)), aABB = entity.getBoundingBox().expandTowards(vec3).inflate(1.0), EntitySelector.CAN_BE_PICKED, n2 = n * n);
        if (entityHitResult == null) {
            return Optional.empty();
        }
        if (vec33.distanceToSqr(entityHitResult.getLocation()) > (double)n2) {
            return Optional.empty();
        }
        return Optional.of(entityHitResult.getEntity());
    }

    public static void renderFilledUnitCube(PoseStack poseStack, MultiBufferSource multiBufferSource, BlockPos blockPos, float f, float f2, float f3, float f4) {
        DebugRenderer.renderFilledBox(poseStack, multiBufferSource, blockPos, blockPos.offset(1, 1, 1), f, f2, f3, f4);
    }

    public static void renderFilledBox(PoseStack poseStack, MultiBufferSource multiBufferSource, BlockPos blockPos, BlockPos blockPos2, float f, float f2, float f3, float f4) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (!camera.isInitialized()) {
            return;
        }
        Vec3 vec3 = camera.getPosition().reverse();
        AABB aABB = AABB.encapsulatingFullBlocks(blockPos, blockPos2).move(vec3);
        DebugRenderer.renderFilledBox(poseStack, multiBufferSource, aABB, f, f2, f3, f4);
    }

    public static void renderFilledBox(PoseStack poseStack, MultiBufferSource multiBufferSource, BlockPos blockPos, float f, float f2, float f3, float f4, float f5) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (!camera.isInitialized()) {
            return;
        }
        Vec3 vec3 = camera.getPosition().reverse();
        AABB aABB = new AABB(blockPos).move(vec3).inflate(f);
        DebugRenderer.renderFilledBox(poseStack, multiBufferSource, aABB, f2, f3, f4, f5);
    }

    public static void renderFilledBox(PoseStack poseStack, MultiBufferSource multiBufferSource, AABB aABB, float f, float f2, float f3, float f4) {
        DebugRenderer.renderFilledBox(poseStack, multiBufferSource, aABB.minX, aABB.minY, aABB.minZ, aABB.maxX, aABB.maxY, aABB.maxZ, f, f2, f3, f4);
    }

    public static void renderFilledBox(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double d2, double d3, double d4, double d5, double d6, float f, float f2, float f3, float f4) {
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.debugFilledBox());
        ShapeRenderer.addChainedFilledBoxVertices(poseStack, vertexConsumer, d, d2, d3, d4, d5, d6, f, f2, f3, f4);
    }

    public static void renderFloatingText(PoseStack poseStack, MultiBufferSource multiBufferSource, String string, int n, int n2, int n3, int n4) {
        DebugRenderer.renderFloatingText(poseStack, multiBufferSource, string, (double)n + 0.5, (double)n2 + 0.5, (double)n3 + 0.5, n4);
    }

    public static void renderFloatingText(PoseStack poseStack, MultiBufferSource multiBufferSource, String string, double d, double d2, double d3, int n) {
        DebugRenderer.renderFloatingText(poseStack, multiBufferSource, string, d, d2, d3, n, 0.02f);
    }

    public static void renderFloatingText(PoseStack poseStack, MultiBufferSource multiBufferSource, String string, double d, double d2, double d3, int n, float f) {
        DebugRenderer.renderFloatingText(poseStack, multiBufferSource, string, d, d2, d3, n, f, true, 0.0f, false);
    }

    public static void renderFloatingText(PoseStack poseStack, MultiBufferSource multiBufferSource, String string, double d, double d2, double d3, int n, float f, boolean bl, float f2, boolean bl2) {
        Minecraft minecraft = Minecraft.getInstance();
        Camera camera = minecraft.gameRenderer.getMainCamera();
        if (!camera.isInitialized() || minecraft.getEntityRenderDispatcher().options == null) {
            return;
        }
        Font font = minecraft.font;
        double d4 = camera.getPosition().x;
        double d5 = camera.getPosition().y;
        double d6 = camera.getPosition().z;
        poseStack.pushPose();
        poseStack.translate((float)(d - d4), (float)(d2 - d5) + 0.07f, (float)(d3 - d6));
        poseStack.mulPose((Quaternionfc)camera.rotation());
        poseStack.scale(f, -f, f);
        float f3 = bl ? (float)(-font.width(string)) / 2.0f : 0.0f;
        font.drawInBatch(string, f3 -= f2 / f, 0.0f, n, false, poseStack.last().pose(), multiBufferSource, bl2 ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, 0, 0xF000F0);
        poseStack.popPose();
    }

    private static Vec3 mixColor(float f) {
        float f2 = 5.99999f;
        int n = (int)(Mth.clamp(f, 0.0f, 1.0f) * 5.99999f);
        float f3 = f * 5.99999f - (float)n;
        return switch (n) {
            case 0 -> new Vec3(1.0, f3, 0.0);
            case 1 -> new Vec3(1.0f - f3, 1.0, 0.0);
            case 2 -> new Vec3(0.0, 1.0, f3);
            case 3 -> new Vec3(0.0, 1.0 - (double)f3, 1.0);
            case 4 -> new Vec3(f3, 0.0, 1.0);
            case 5 -> new Vec3(1.0, 0.0, 1.0 - (double)f3);
            default -> throw new IllegalStateException("Unexpected value: " + n);
        };
    }

    private static Vec3 shiftHue(float f, float f2, float f3, float f4) {
        Vec3 vec3 = DebugRenderer.mixColor(f4).scale(f);
        Vec3 vec32 = DebugRenderer.mixColor((f4 + 0.33333334f) % 1.0f).scale(f2);
        Vec3 vec33 = DebugRenderer.mixColor((f4 + 0.6666667f) % 1.0f).scale(f3);
        Vec3 vec34 = vec3.add(vec32).add(vec33);
        double d = Math.max(Math.max(1.0, vec34.x), Math.max(vec34.y, vec34.z));
        return new Vec3(vec34.x / d, vec34.y / d, vec34.z / d);
    }

    public static void renderVoxelShape(PoseStack poseStack, VertexConsumer vertexConsumer, VoxelShape voxelShape, double d, double d2, double d3, float f, float f2, float f3, float f4, boolean bl) {
        List<AABB> list = voxelShape.toAabbs();
        if (list.isEmpty()) {
            return;
        }
        int n = bl ? list.size() : list.size() * 8;
        ShapeRenderer.renderShape(poseStack, vertexConsumer, Shapes.create(list.get(0)), d, d2, d3, ARGB.colorFromFloat(f4, f, f2, f3));
        for (int i = 1; i < list.size(); ++i) {
            AABB aABB = list.get(i);
            float f5 = (float)i / (float)n;
            Vec3 vec3 = DebugRenderer.shiftHue(f, f2, f3, f5);
            ShapeRenderer.renderShape(poseStack, vertexConsumer, Shapes.create(aABB), d, d2, d3, ARGB.colorFromFloat(f4, (float)vec3.x, (float)vec3.y, (float)vec3.z));
        }
    }

    public static interface SimpleDebugRenderer {
        public void render(PoseStack var1, MultiBufferSource var2, double var3, double var5, double var7);

        default public void clear() {
        }
    }
}

