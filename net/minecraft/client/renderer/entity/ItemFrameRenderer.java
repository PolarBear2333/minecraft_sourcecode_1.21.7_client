/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BlockStateDefinitions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;

public class ItemFrameRenderer<T extends ItemFrame>
extends EntityRenderer<T, ItemFrameRenderState> {
    public static final int GLOW_FRAME_BRIGHTNESS = 5;
    public static final int BRIGHT_MAP_LIGHT_ADJUSTMENT = 30;
    private final ItemModelResolver itemModelResolver;
    private final MapRenderer mapRenderer;
    private final BlockRenderDispatcher blockRenderer;

    public ItemFrameRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
        this.mapRenderer = context.getMapRenderer();
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    protected int getBlockLightLevel(T t, BlockPos blockPos) {
        if (((Entity)t).getType() == EntityType.GLOW_ITEM_FRAME) {
            return Math.max(5, super.getBlockLightLevel(t, blockPos));
        }
        return super.getBlockLightLevel(t, blockPos);
    }

    @Override
    public void render(ItemFrameRenderState itemFrameRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        float f;
        float f2;
        super.render(itemFrameRenderState, poseStack, multiBufferSource, n);
        poseStack.pushPose();
        Direction direction = itemFrameRenderState.direction;
        Vec3 vec3 = this.getRenderOffset(itemFrameRenderState);
        poseStack.translate(-vec3.x(), -vec3.y(), -vec3.z());
        double d = 0.46875;
        poseStack.translate((double)direction.getStepX() * 0.46875, (double)direction.getStepY() * 0.46875, (double)direction.getStepZ() * 0.46875);
        if (direction.getAxis().isHorizontal()) {
            f2 = 0.0f;
            f = 180.0f - direction.toYRot();
        } else {
            f2 = -90 * direction.getAxisDirection().getStep();
            f = 180.0f;
        }
        poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(f2));
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(f));
        if (!itemFrameRenderState.isInvisible) {
            BlockState blockState = BlockStateDefinitions.getItemFrameFakeState(itemFrameRenderState.isGlowFrame, itemFrameRenderState.mapId != null);
            BlockStateModel blockStateModel = this.blockRenderer.getBlockModel(blockState);
            poseStack.pushPose();
            poseStack.translate(-0.5f, -0.5f, -0.5f);
            ModelBlockRenderer.renderModel(poseStack.last(), multiBufferSource.getBuffer(RenderType.entitySolidZOffsetForward(TextureAtlas.LOCATION_BLOCKS)), blockStateModel, 1.0f, 1.0f, 1.0f, n, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
        if (itemFrameRenderState.isInvisible) {
            poseStack.translate(0.0f, 0.0f, 0.5f);
        } else {
            poseStack.translate(0.0f, 0.0f, 0.4375f);
        }
        if (itemFrameRenderState.mapId != null) {
            int n2 = itemFrameRenderState.rotation % 4 * 2;
            poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees((float)n2 * 360.0f / 8.0f));
            poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(180.0f));
            float f3 = 0.0078125f;
            poseStack.scale(0.0078125f, 0.0078125f, 0.0078125f);
            poseStack.translate(-64.0f, -64.0f, 0.0f);
            poseStack.translate(0.0f, 0.0f, -1.0f);
            int n3 = this.getLightCoords(itemFrameRenderState.isGlowFrame, 15728850, n);
            this.mapRenderer.render(itemFrameRenderState.mapRenderState, poseStack, multiBufferSource, true, n3);
        } else if (!itemFrameRenderState.item.isEmpty()) {
            poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees((float)itemFrameRenderState.rotation * 360.0f / 8.0f));
            int n4 = this.getLightCoords(itemFrameRenderState.isGlowFrame, 0xF000F0, n);
            poseStack.scale(0.5f, 0.5f, 0.5f);
            itemFrameRenderState.item.render(poseStack, multiBufferSource, n4, OverlayTexture.NO_OVERLAY);
        }
        poseStack.popPose();
    }

    private int getLightCoords(boolean bl, int n, int n2) {
        return bl ? n : n2;
    }

    @Override
    public Vec3 getRenderOffset(ItemFrameRenderState itemFrameRenderState) {
        return new Vec3((float)itemFrameRenderState.direction.getStepX() * 0.3f, -0.25, (float)itemFrameRenderState.direction.getStepZ() * 0.3f);
    }

    @Override
    protected boolean shouldShowName(T t, double d) {
        return Minecraft.renderNames() && this.entityRenderDispatcher.crosshairPickEntity == t && ((ItemFrame)t).getItem().getCustomName() != null;
    }

    @Override
    protected Component getNameTag(T t) {
        return ((ItemFrame)t).getItem().getHoverName();
    }

    @Override
    public ItemFrameRenderState createRenderState() {
        return new ItemFrameRenderState();
    }

    @Override
    public void extractRenderState(T t, ItemFrameRenderState itemFrameRenderState, float f) {
        MapItemSavedData mapItemSavedData;
        MapId mapId;
        super.extractRenderState(t, itemFrameRenderState, f);
        itemFrameRenderState.direction = ((HangingEntity)t).getDirection();
        ItemStack itemStack = ((ItemFrame)t).getItem();
        this.itemModelResolver.updateForNonLiving(itemFrameRenderState.item, itemStack, ItemDisplayContext.FIXED, (Entity)t);
        itemFrameRenderState.rotation = ((ItemFrame)t).getRotation();
        itemFrameRenderState.isGlowFrame = ((Entity)t).getType() == EntityType.GLOW_ITEM_FRAME;
        itemFrameRenderState.mapId = null;
        if (!itemStack.isEmpty() && (mapId = ((ItemFrame)t).getFramedMapId(itemStack)) != null && (mapItemSavedData = ((Entity)t).level().getMapData(mapId)) != null) {
            this.mapRenderer.extractRenderState(mapId, mapItemSavedData, itemFrameRenderState.mapRenderState);
            itemFrameRenderState.mapId = mapId;
        }
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }

    @Override
    protected /* synthetic */ Component getNameTag(Entity entity) {
        return this.getNameTag((T)((ItemFrame)entity));
    }
}

