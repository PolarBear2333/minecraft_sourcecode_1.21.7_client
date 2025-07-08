/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  javax.annotation.Nullable
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 */
package net.minecraft.client.renderer.entity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.HitboxRenderState;
import net.minecraft.client.renderer.entity.state.HitboxesRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.entity.state.ServerHitboxesRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

public class EntityRenderDispatcher
implements ResourceManagerReloadListener {
    private static final RenderType SHADOW_RENDER_TYPE = RenderType.entityShadow(ResourceLocation.withDefaultNamespace("textures/misc/shadow.png"));
    private static final float MAX_SHADOW_RADIUS = 32.0f;
    private static final float SHADOW_POWER_FALLOFF_Y = 0.5f;
    private Map<EntityType<?>, EntityRenderer<?, ?>> renderers = ImmutableMap.of();
    private Map<PlayerSkin.Model, EntityRenderer<? extends Player, ?>> playerRenderers = Map.of();
    public final TextureManager textureManager;
    private Level level;
    public Camera camera;
    private Quaternionf cameraOrientation;
    public Entity crosshairPickEntity;
    private final ItemModelResolver itemModelResolver;
    private final MapRenderer mapRenderer;
    private final BlockRenderDispatcher blockRenderDispatcher;
    private final ItemInHandRenderer itemInHandRenderer;
    private final Font font;
    public final Options options;
    private final Supplier<EntityModelSet> entityModels;
    private final EquipmentAssetManager equipmentAssets;
    private boolean shouldRenderShadow = true;
    private boolean renderHitBoxes;

    public <E extends Entity> int getPackedLightCoords(E e, float f) {
        return this.getRenderer((EntityRenderState)((Object)e)).getPackedLightCoords(e, f);
    }

    public EntityRenderDispatcher(Minecraft minecraft, TextureManager textureManager, ItemModelResolver itemModelResolver, ItemRenderer itemRenderer, MapRenderer mapRenderer, BlockRenderDispatcher blockRenderDispatcher, Font font, Options options, Supplier<EntityModelSet> supplier, EquipmentAssetManager equipmentAssetManager) {
        this.textureManager = textureManager;
        this.itemModelResolver = itemModelResolver;
        this.mapRenderer = mapRenderer;
        this.itemInHandRenderer = new ItemInHandRenderer(minecraft, this, itemRenderer, itemModelResolver);
        this.blockRenderDispatcher = blockRenderDispatcher;
        this.font = font;
        this.options = options;
        this.entityModels = supplier;
        this.equipmentAssets = equipmentAssetManager;
    }

    public <T extends Entity> EntityRenderer<? super T, ?> getRenderer(T t) {
        if (t instanceof AbstractClientPlayer) {
            AbstractClientPlayer abstractClientPlayer = (AbstractClientPlayer)t;
            PlayerSkin.Model model = abstractClientPlayer.getSkin().model();
            EntityRenderer<? extends Player, ?> entityRenderer = this.playerRenderers.get((Object)model);
            if (entityRenderer != null) {
                return entityRenderer;
            }
            return this.playerRenderers.get((Object)PlayerSkin.Model.WIDE);
        }
        return this.renderers.get(t.getType());
    }

    public <S extends EntityRenderState> EntityRenderer<?, ? super S> getRenderer(S s) {
        if (s instanceof PlayerRenderState) {
            PlayerRenderState playerRenderState = (PlayerRenderState)s;
            PlayerSkin.Model model = playerRenderState.skin.model();
            EntityRenderer<? extends Player, ?> entityRenderer = this.playerRenderers.get((Object)model);
            if (entityRenderer != null) {
                return entityRenderer;
            }
            return this.playerRenderers.get((Object)PlayerSkin.Model.WIDE);
        }
        return this.renderers.get(s.entityType);
    }

    public void prepare(Level level, Camera camera, Entity entity) {
        this.level = level;
        this.camera = camera;
        this.cameraOrientation = camera.rotation();
        this.crosshairPickEntity = entity;
    }

    public void overrideCameraOrientation(Quaternionf quaternionf) {
        this.cameraOrientation = quaternionf;
    }

    public void setRenderShadow(boolean bl) {
        this.shouldRenderShadow = bl;
    }

    public void setRenderHitBoxes(boolean bl) {
        this.renderHitBoxes = bl;
    }

    public boolean shouldRenderHitBoxes() {
        return this.renderHitBoxes;
    }

    public <E extends Entity> boolean shouldRender(E e, Frustum frustum, double d, double d2, double d3) {
        EntityRenderer<?, E> entityRenderer = this.getRenderer((EntityRenderState)((Object)e));
        return entityRenderer.shouldRender(e, frustum, d, d2, d3);
    }

    public <E extends Entity> void render(E e, double d, double d2, double d3, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        EntityRenderer<?, E> entityRenderer = this.getRenderer((EntityRenderState)((Object)e));
        this.render(e, d, d2, d3, f, poseStack, multiBufferSource, n, entityRenderer);
    }

    private <E extends Entity, S extends EntityRenderState> void render(E e, double d, double d2, double d3, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, EntityRenderer<? super E, S> entityRenderer) {
        S s;
        try {
            s = entityRenderer.createRenderState(e, f);
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Extracting render state for an entity in world");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Entity being extracted");
            e.fillCrashReportCategory(crashReportCategory);
            CrashReportCategory crashReportCategory2 = this.fillRendererDetails(d, d2, d3, entityRenderer, crashReport);
            crashReportCategory2.setDetail("Delta", Float.valueOf(f));
            throw new ReportedException(crashReport);
        }
        try {
            this.render(s, d, d2, d3, poseStack, multiBufferSource, n, entityRenderer);
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Rendering entity in world");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Entity being rendered");
            e.fillCrashReportCategory(crashReportCategory);
            throw new ReportedException(crashReport);
        }
    }

    public <S extends EntityRenderState> void render(S s, double d, double d2, double d3, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        EntityRenderer<?, S> entityRenderer = this.getRenderer(s);
        this.render(s, d, d2, d3, poseStack, multiBufferSource, n, entityRenderer);
    }

    private <S extends EntityRenderState> void render(S s, double d, double d2, double d3, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, EntityRenderer<?, S> entityRenderer) {
        try {
            double d4;
            float f;
            float f2;
            Vec3 vec3 = entityRenderer.getRenderOffset(s);
            double d5 = d + vec3.x();
            double d6 = d2 + vec3.y();
            double d7 = d3 + vec3.z();
            poseStack.pushPose();
            poseStack.translate(d5, d6, d7);
            entityRenderer.render(s, poseStack, multiBufferSource, n);
            if (s.displayFireAnimation) {
                this.renderFlame(poseStack, multiBufferSource, s, Mth.rotationAroundAxis(Mth.Y_AXIS, this.cameraOrientation, new Quaternionf()));
            }
            if (s instanceof PlayerRenderState) {
                poseStack.translate(-vec3.x(), -vec3.y(), -vec3.z());
            }
            if (this.options.entityShadows().get().booleanValue() && this.shouldRenderShadow && !s.isInvisible && (f2 = entityRenderer.getShadowRadius(s)) > 0.0f && (f = (float)((1.0 - (d4 = s.distanceToCameraSq) / 256.0) * (double)entityRenderer.getShadowStrength(s))) > 0.0f) {
                EntityRenderDispatcher.renderShadow(poseStack, multiBufferSource, s, f, this.level, Math.min(f2, 32.0f));
            }
            if (!(s instanceof PlayerRenderState)) {
                poseStack.translate(-vec3.x(), -vec3.y(), -vec3.z());
            }
            if (s.hitboxesRenderState != null) {
                this.renderHitboxes(poseStack, s, s.hitboxesRenderState, multiBufferSource);
            }
            poseStack.popPose();
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Rendering entity in world");
            CrashReportCategory crashReportCategory = crashReport.addCategory("EntityRenderState being rendered");
            s.fillCrashReportCategory(crashReportCategory);
            this.fillRendererDetails(d, d2, d3, entityRenderer, crashReport);
            throw new ReportedException(crashReport);
        }
    }

    private <S extends EntityRenderState> CrashReportCategory fillRendererDetails(double d, double d2, double d3, EntityRenderer<?, S> entityRenderer, CrashReport crashReport) {
        CrashReportCategory crashReportCategory = crashReport.addCategory("Renderer details");
        crashReportCategory.setDetail("Assigned renderer", entityRenderer);
        crashReportCategory.setDetail("Location", CrashReportCategory.formatLocation((LevelHeightAccessor)this.level, d, d2, d3));
        return crashReportCategory;
    }

    private void renderHitboxes(PoseStack poseStack, EntityRenderState entityRenderState, HitboxesRenderState hitboxesRenderState, MultiBufferSource multiBufferSource) {
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.lines());
        EntityRenderDispatcher.renderHitboxesAndViewVector(poseStack, hitboxesRenderState, vertexConsumer, entityRenderState.eyeHeight);
        ServerHitboxesRenderState serverHitboxesRenderState = entityRenderState.serverHitboxesRenderState;
        if (serverHitboxesRenderState != null) {
            if (serverHitboxesRenderState.missing()) {
                HitboxRenderState hitboxRenderState = (HitboxRenderState)hitboxesRenderState.hitboxes().getFirst();
                DebugRenderer.renderFloatingText(poseStack, multiBufferSource, "Missing", entityRenderState.x, hitboxRenderState.y1() + 1.5, entityRenderState.z, -65536);
            } else if (serverHitboxesRenderState.hitboxes() != null) {
                poseStack.pushPose();
                poseStack.translate(serverHitboxesRenderState.serverEntityX() - entityRenderState.x, serverHitboxesRenderState.serverEntityY() - entityRenderState.y, serverHitboxesRenderState.serverEntityZ() - entityRenderState.z);
                EntityRenderDispatcher.renderHitboxesAndViewVector(poseStack, serverHitboxesRenderState.hitboxes(), vertexConsumer, serverHitboxesRenderState.eyeHeight());
                Vec3 vec3 = new Vec3(serverHitboxesRenderState.deltaMovementX(), serverHitboxesRenderState.deltaMovementY(), serverHitboxesRenderState.deltaMovementZ());
                ShapeRenderer.renderVector(poseStack, vertexConsumer, new Vector3f(), vec3, -256);
                poseStack.popPose();
            }
        }
    }

    private static void renderHitboxesAndViewVector(PoseStack poseStack, HitboxesRenderState hitboxesRenderState, VertexConsumer vertexConsumer, float f) {
        for (HitboxRenderState hitboxRenderState : hitboxesRenderState.hitboxes()) {
            EntityRenderDispatcher.renderHitbox(poseStack, vertexConsumer, hitboxRenderState);
        }
        Vec3 vec3 = new Vec3(hitboxesRenderState.viewX(), hitboxesRenderState.viewY(), hitboxesRenderState.viewZ());
        ShapeRenderer.renderVector(poseStack, vertexConsumer, new Vector3f(0.0f, f, 0.0f), vec3.scale(2.0), -16776961);
    }

    private static void renderHitbox(PoseStack poseStack, VertexConsumer vertexConsumer, HitboxRenderState hitboxRenderState) {
        poseStack.pushPose();
        poseStack.translate(hitboxRenderState.offsetX(), hitboxRenderState.offsetY(), hitboxRenderState.offsetZ());
        ShapeRenderer.renderLineBox(poseStack, vertexConsumer, hitboxRenderState.x0(), hitboxRenderState.y0(), hitboxRenderState.z0(), hitboxRenderState.x1(), hitboxRenderState.y1(), hitboxRenderState.z1(), hitboxRenderState.red(), hitboxRenderState.green(), hitboxRenderState.blue(), 1.0f);
        poseStack.popPose();
    }

    private void renderFlame(PoseStack poseStack, MultiBufferSource multiBufferSource, EntityRenderState entityRenderState, Quaternionf quaternionf) {
        TextureAtlasSprite textureAtlasSprite = ModelBakery.FIRE_0.sprite();
        TextureAtlasSprite textureAtlasSprite2 = ModelBakery.FIRE_1.sprite();
        poseStack.pushPose();
        float f = entityRenderState.boundingBoxWidth * 1.4f;
        poseStack.scale(f, f, f);
        float f2 = 0.5f;
        float f3 = 0.0f;
        float f4 = entityRenderState.boundingBoxHeight / f;
        float f5 = 0.0f;
        poseStack.mulPose((Quaternionfc)quaternionf);
        poseStack.translate(0.0f, 0.0f, 0.3f - (float)((int)f4) * 0.02f);
        float f6 = 0.0f;
        int n = 0;
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(Sheets.cutoutBlockSheet());
        PoseStack.Pose pose = poseStack.last();
        while (f4 > 0.0f) {
            TextureAtlasSprite textureAtlasSprite3 = n % 2 == 0 ? textureAtlasSprite : textureAtlasSprite2;
            float f7 = textureAtlasSprite3.getU0();
            float f8 = textureAtlasSprite3.getV0();
            float f9 = textureAtlasSprite3.getU1();
            float f10 = textureAtlasSprite3.getV1();
            if (n / 2 % 2 == 0) {
                float f11 = f9;
                f9 = f7;
                f7 = f11;
            }
            EntityRenderDispatcher.fireVertex(pose, vertexConsumer, -f2 - 0.0f, 0.0f - f5, f6, f9, f10);
            EntityRenderDispatcher.fireVertex(pose, vertexConsumer, f2 - 0.0f, 0.0f - f5, f6, f7, f10);
            EntityRenderDispatcher.fireVertex(pose, vertexConsumer, f2 - 0.0f, 1.4f - f5, f6, f7, f8);
            EntityRenderDispatcher.fireVertex(pose, vertexConsumer, -f2 - 0.0f, 1.4f - f5, f6, f9, f8);
            f4 -= 0.45f;
            f5 -= 0.45f;
            f2 *= 0.9f;
            f6 -= 0.03f;
            ++n;
        }
        poseStack.popPose();
    }

    private static void fireVertex(PoseStack.Pose pose, VertexConsumer vertexConsumer, float f, float f2, float f3, float f4, float f5) {
        vertexConsumer.addVertex(pose, f, f2, f3).setColor(-1).setUv(f4, f5).setUv1(0, 10).setLight(240).setNormal(pose, 0.0f, 1.0f, 0.0f);
    }

    private static void renderShadow(PoseStack poseStack, MultiBufferSource multiBufferSource, EntityRenderState entityRenderState, float f, LevelReader levelReader, float f2) {
        float f3 = Math.min(f / 0.5f, f2);
        int n = Mth.floor(entityRenderState.x - (double)f2);
        int n2 = Mth.floor(entityRenderState.x + (double)f2);
        int n3 = Mth.floor(entityRenderState.y - (double)f3);
        int n4 = Mth.floor(entityRenderState.y);
        int n5 = Mth.floor(entityRenderState.z - (double)f2);
        int n6 = Mth.floor(entityRenderState.z + (double)f2);
        PoseStack.Pose pose = poseStack.last();
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(SHADOW_RENDER_TYPE);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i = n5; i <= n6; ++i) {
            for (int j = n; j <= n2; ++j) {
                mutableBlockPos.set(j, 0, i);
                ChunkAccess chunkAccess = levelReader.getChunk(mutableBlockPos);
                for (int k = n3; k <= n4; ++k) {
                    mutableBlockPos.setY(k);
                    float f4 = f - (float)(entityRenderState.y - (double)mutableBlockPos.getY()) * 0.5f;
                    EntityRenderDispatcher.renderBlockShadow(pose, vertexConsumer, chunkAccess, levelReader, mutableBlockPos, entityRenderState.x, entityRenderState.y, entityRenderState.z, f2, f4);
                }
            }
        }
    }

    private static void renderBlockShadow(PoseStack.Pose pose, VertexConsumer vertexConsumer, ChunkAccess chunkAccess, LevelReader levelReader, BlockPos blockPos, double d, double d2, double d3, float f, float f2) {
        BlockPos blockPos2 = blockPos.below();
        BlockState blockState = chunkAccess.getBlockState(blockPos2);
        if (blockState.getRenderShape() == RenderShape.INVISIBLE || levelReader.getMaxLocalRawBrightness(blockPos) <= 3) {
            return;
        }
        if (!blockState.isCollisionShapeFullBlock(chunkAccess, blockPos2)) {
            return;
        }
        VoxelShape voxelShape = blockState.getShape(chunkAccess, blockPos2);
        if (voxelShape.isEmpty()) {
            return;
        }
        float f3 = LightTexture.getBrightness(levelReader.dimensionType(), levelReader.getMaxLocalRawBrightness(blockPos));
        float f4 = f2 * 0.5f * f3;
        if (f4 >= 0.0f) {
            if (f4 > 1.0f) {
                f4 = 1.0f;
            }
            int n = ARGB.color(Mth.floor(f4 * 255.0f), 255, 255, 255);
            AABB aABB = voxelShape.bounds();
            double d4 = (double)blockPos.getX() + aABB.minX;
            double d5 = (double)blockPos.getX() + aABB.maxX;
            double d6 = (double)blockPos.getY() + aABB.minY;
            double d7 = (double)blockPos.getZ() + aABB.minZ;
            double d8 = (double)blockPos.getZ() + aABB.maxZ;
            float f5 = (float)(d4 - d);
            float f6 = (float)(d5 - d);
            float f7 = (float)(d6 - d2);
            float f8 = (float)(d7 - d3);
            float f9 = (float)(d8 - d3);
            float f10 = -f5 / 2.0f / f + 0.5f;
            float f11 = -f6 / 2.0f / f + 0.5f;
            float f12 = -f8 / 2.0f / f + 0.5f;
            float f13 = -f9 / 2.0f / f + 0.5f;
            EntityRenderDispatcher.shadowVertex(pose, vertexConsumer, n, f5, f7, f8, f10, f12);
            EntityRenderDispatcher.shadowVertex(pose, vertexConsumer, n, f5, f7, f9, f10, f13);
            EntityRenderDispatcher.shadowVertex(pose, vertexConsumer, n, f6, f7, f9, f11, f13);
            EntityRenderDispatcher.shadowVertex(pose, vertexConsumer, n, f6, f7, f8, f11, f12);
        }
    }

    private static void shadowVertex(PoseStack.Pose pose, VertexConsumer vertexConsumer, int n, float f, float f2, float f3, float f4, float f5) {
        Vector3f vector3f = pose.pose().transformPosition(f, f2, f3, new Vector3f());
        vertexConsumer.addVertex(vector3f.x(), vector3f.y(), vector3f.z(), n, f4, f5, OverlayTexture.NO_OVERLAY, 0xF000F0, 0.0f, 1.0f, 0.0f);
    }

    public void setLevel(@Nullable Level level) {
        this.level = level;
        if (level == null) {
            this.camera = null;
        }
    }

    public double distanceToSqr(Entity entity) {
        return this.camera.getPosition().distanceToSqr(entity.position());
    }

    public double distanceToSqr(double d, double d2, double d3) {
        return this.camera.getPosition().distanceToSqr(d, d2, d3);
    }

    public Quaternionf cameraOrientation() {
        return this.cameraOrientation;
    }

    public ItemInHandRenderer getItemInHandRenderer() {
        return this.itemInHandRenderer;
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        EntityRendererProvider.Context context = new EntityRendererProvider.Context(this, this.itemModelResolver, this.mapRenderer, this.blockRenderDispatcher, resourceManager, this.entityModels.get(), this.equipmentAssets, this.font);
        this.renderers = EntityRenderers.createEntityRenderers(context);
        this.playerRenderers = EntityRenderers.createPlayerRenderers(context);
    }
}

