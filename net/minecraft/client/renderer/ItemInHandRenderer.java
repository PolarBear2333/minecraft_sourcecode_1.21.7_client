/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.base.MoreObjects
 *  org.joml.Matrix4f
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.joml.Matrix4f;
import org.joml.Quaternionfc;

public class ItemInHandRenderer {
    private static final RenderType MAP_BACKGROUND = RenderType.text(ResourceLocation.withDefaultNamespace("textures/map/map_background.png"));
    private static final RenderType MAP_BACKGROUND_CHECKERBOARD = RenderType.text(ResourceLocation.withDefaultNamespace("textures/map/map_background_checkerboard.png"));
    private static final float ITEM_SWING_X_POS_SCALE = -0.4f;
    private static final float ITEM_SWING_Y_POS_SCALE = 0.2f;
    private static final float ITEM_SWING_Z_POS_SCALE = -0.2f;
    private static final float ITEM_HEIGHT_SCALE = -0.6f;
    private static final float ITEM_POS_X = 0.56f;
    private static final float ITEM_POS_Y = -0.52f;
    private static final float ITEM_POS_Z = -0.72f;
    private static final float ITEM_PRESWING_ROT_Y = 45.0f;
    private static final float ITEM_SWING_X_ROT_AMOUNT = -80.0f;
    private static final float ITEM_SWING_Y_ROT_AMOUNT = -20.0f;
    private static final float ITEM_SWING_Z_ROT_AMOUNT = -20.0f;
    private static final float EAT_JIGGLE_X_ROT_AMOUNT = 10.0f;
    private static final float EAT_JIGGLE_Y_ROT_AMOUNT = 90.0f;
    private static final float EAT_JIGGLE_Z_ROT_AMOUNT = 30.0f;
    private static final float EAT_JIGGLE_X_POS_SCALE = 0.6f;
    private static final float EAT_JIGGLE_Y_POS_SCALE = -0.5f;
    private static final float EAT_JIGGLE_Z_POS_SCALE = 0.0f;
    private static final double EAT_JIGGLE_EXPONENT = 27.0;
    private static final float EAT_EXTRA_JIGGLE_CUTOFF = 0.8f;
    private static final float EAT_EXTRA_JIGGLE_SCALE = 0.1f;
    private static final float ARM_SWING_X_POS_SCALE = -0.3f;
    private static final float ARM_SWING_Y_POS_SCALE = 0.4f;
    private static final float ARM_SWING_Z_POS_SCALE = -0.4f;
    private static final float ARM_SWING_Y_ROT_AMOUNT = 70.0f;
    private static final float ARM_SWING_Z_ROT_AMOUNT = -20.0f;
    private static final float ARM_HEIGHT_SCALE = -0.6f;
    private static final float ARM_POS_SCALE = 0.8f;
    private static final float ARM_POS_X = 0.8f;
    private static final float ARM_POS_Y = -0.75f;
    private static final float ARM_POS_Z = -0.9f;
    private static final float ARM_PRESWING_ROT_Y = 45.0f;
    private static final float ARM_PREROTATION_X_OFFSET = -1.0f;
    private static final float ARM_PREROTATION_Y_OFFSET = 3.6f;
    private static final float ARM_PREROTATION_Z_OFFSET = 3.5f;
    private static final float ARM_POSTROTATION_X_OFFSET = 5.6f;
    private static final int ARM_ROT_X = 200;
    private static final int ARM_ROT_Y = -135;
    private static final int ARM_ROT_Z = 120;
    private static final float MAP_SWING_X_POS_SCALE = -0.4f;
    private static final float MAP_SWING_Z_POS_SCALE = -0.2f;
    private static final float MAP_HANDS_POS_X = 0.0f;
    private static final float MAP_HANDS_POS_Y = 0.04f;
    private static final float MAP_HANDS_POS_Z = -0.72f;
    private static final float MAP_HANDS_HEIGHT_SCALE = -1.2f;
    private static final float MAP_HANDS_TILT_SCALE = -0.5f;
    private static final float MAP_PLAYER_PITCH_SCALE = 45.0f;
    private static final float MAP_HANDS_Z_ROT_AMOUNT = -85.0f;
    private static final float MAPHAND_X_ROT_AMOUNT = 45.0f;
    private static final float MAPHAND_Y_ROT_AMOUNT = 92.0f;
    private static final float MAPHAND_Z_ROT_AMOUNT = -41.0f;
    private static final float MAP_HAND_X_POS = 0.3f;
    private static final float MAP_HAND_Y_POS = -1.1f;
    private static final float MAP_HAND_Z_POS = 0.45f;
    private static final float MAP_SWING_X_ROT_AMOUNT = 20.0f;
    private static final float MAP_PRE_ROT_SCALE = 0.38f;
    private static final float MAP_GLOBAL_X_POS = -0.5f;
    private static final float MAP_GLOBAL_Y_POS = -0.5f;
    private static final float MAP_GLOBAL_Z_POS = 0.0f;
    private static final float MAP_FINAL_SCALE = 0.0078125f;
    private static final int MAP_BORDER = 7;
    private static final int MAP_HEIGHT = 128;
    private static final int MAP_WIDTH = 128;
    private static final float BOW_CHARGE_X_POS_SCALE = 0.0f;
    private static final float BOW_CHARGE_Y_POS_SCALE = 0.0f;
    private static final float BOW_CHARGE_Z_POS_SCALE = 0.04f;
    private static final float BOW_CHARGE_SHAKE_X_SCALE = 0.0f;
    private static final float BOW_CHARGE_SHAKE_Y_SCALE = 0.004f;
    private static final float BOW_CHARGE_SHAKE_Z_SCALE = 0.0f;
    private static final float BOW_CHARGE_Z_SCALE = 0.2f;
    private static final float BOW_MIN_SHAKE_CHARGE = 0.1f;
    private final Minecraft minecraft;
    private final MapRenderState mapRenderState = new MapRenderState();
    private ItemStack mainHandItem = ItemStack.EMPTY;
    private ItemStack offHandItem = ItemStack.EMPTY;
    private float mainHandHeight;
    private float oMainHandHeight;
    private float offHandHeight;
    private float oOffHandHeight;
    private final EntityRenderDispatcher entityRenderDispatcher;
    private final ItemRenderer itemRenderer;
    private final ItemModelResolver itemModelResolver;

    public ItemInHandRenderer(Minecraft minecraft, EntityRenderDispatcher entityRenderDispatcher, ItemRenderer itemRenderer, ItemModelResolver itemModelResolver) {
        this.minecraft = minecraft;
        this.entityRenderDispatcher = entityRenderDispatcher;
        this.itemRenderer = itemRenderer;
        this.itemModelResolver = itemModelResolver;
    }

    public void renderItem(LivingEntity livingEntity, ItemStack itemStack, ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        if (itemStack.isEmpty()) {
            return;
        }
        this.itemRenderer.renderStatic(livingEntity, itemStack, itemDisplayContext, poseStack, multiBufferSource, livingEntity.level(), n, OverlayTexture.NO_OVERLAY, livingEntity.getId() + itemDisplayContext.ordinal());
    }

    private float calculateMapTilt(float f) {
        float f2 = 1.0f - f / 45.0f + 0.1f;
        f2 = Mth.clamp(f2, 0.0f, 1.0f);
        f2 = -Mth.cos(f2 * (float)Math.PI) * 0.5f + 0.5f;
        return f2;
    }

    private void renderMapHand(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, HumanoidArm humanoidArm) {
        PlayerRenderer playerRenderer = (PlayerRenderer)this.entityRenderDispatcher.getRenderer(this.minecraft.player);
        poseStack.pushPose();
        float f = humanoidArm == HumanoidArm.RIGHT ? 1.0f : -1.0f;
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(92.0f));
        poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(45.0f));
        poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(f * -41.0f));
        poseStack.translate(f * 0.3f, -1.1f, 0.45f);
        ResourceLocation resourceLocation = this.minecraft.player.getSkin().texture();
        if (humanoidArm == HumanoidArm.RIGHT) {
            playerRenderer.renderRightHand(poseStack, multiBufferSource, n, resourceLocation, this.minecraft.player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE));
        } else {
            playerRenderer.renderLeftHand(poseStack, multiBufferSource, n, resourceLocation, this.minecraft.player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE));
        }
        poseStack.popPose();
    }

    private void renderOneHandedMap(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, float f, HumanoidArm humanoidArm, float f2, ItemStack itemStack) {
        float f3 = humanoidArm == HumanoidArm.RIGHT ? 1.0f : -1.0f;
        poseStack.translate(f3 * 0.125f, -0.125f, 0.0f);
        if (!this.minecraft.player.isInvisible()) {
            poseStack.pushPose();
            poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(f3 * 10.0f));
            this.renderPlayerArm(poseStack, multiBufferSource, n, f, f2, humanoidArm);
            poseStack.popPose();
        }
        poseStack.pushPose();
        poseStack.translate(f3 * 0.51f, -0.08f + f * -1.2f, -0.75f);
        float f4 = Mth.sqrt(f2);
        float f5 = Mth.sin(f4 * (float)Math.PI);
        float f6 = -0.5f * f5;
        float f7 = 0.4f * Mth.sin(f4 * ((float)Math.PI * 2));
        float f8 = -0.3f * Mth.sin(f2 * (float)Math.PI);
        poseStack.translate(f3 * f6, f7 - 0.3f * f5, f8);
        poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(f5 * -45.0f));
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(f3 * f5 * -30.0f));
        this.renderMap(poseStack, multiBufferSource, n, itemStack);
        poseStack.popPose();
    }

    private void renderTwoHandedMap(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, float f, float f2, float f3) {
        float f4 = Mth.sqrt(f3);
        float f5 = -0.2f * Mth.sin(f3 * (float)Math.PI);
        float f6 = -0.4f * Mth.sin(f4 * (float)Math.PI);
        poseStack.translate(0.0f, -f5 / 2.0f, f6);
        float f7 = this.calculateMapTilt(f);
        poseStack.translate(0.0f, 0.04f + f2 * -1.2f + f7 * -0.5f, -0.72f);
        poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(f7 * -85.0f));
        if (!this.minecraft.player.isInvisible()) {
            poseStack.pushPose();
            poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(90.0f));
            this.renderMapHand(poseStack, multiBufferSource, n, HumanoidArm.RIGHT);
            this.renderMapHand(poseStack, multiBufferSource, n, HumanoidArm.LEFT);
            poseStack.popPose();
        }
        float f8 = Mth.sin(f4 * (float)Math.PI);
        poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(f8 * 20.0f));
        poseStack.scale(2.0f, 2.0f, 2.0f);
        this.renderMap(poseStack, multiBufferSource, n, this.mainHandItem);
    }

    private void renderMap(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, ItemStack itemStack) {
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(180.0f));
        poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(180.0f));
        poseStack.scale(0.38f, 0.38f, 0.38f);
        poseStack.translate(-0.5f, -0.5f, 0.0f);
        poseStack.scale(0.0078125f, 0.0078125f, 0.0078125f);
        MapId mapId = itemStack.get(DataComponents.MAP_ID);
        MapItemSavedData mapItemSavedData = MapItem.getSavedData(mapId, (Level)this.minecraft.level);
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(mapItemSavedData == null ? MAP_BACKGROUND : MAP_BACKGROUND_CHECKERBOARD);
        Matrix4f matrix4f = poseStack.last().pose();
        vertexConsumer.addVertex(matrix4f, -7.0f, 135.0f, 0.0f).setColor(-1).setUv(0.0f, 1.0f).setLight(n);
        vertexConsumer.addVertex(matrix4f, 135.0f, 135.0f, 0.0f).setColor(-1).setUv(1.0f, 1.0f).setLight(n);
        vertexConsumer.addVertex(matrix4f, 135.0f, -7.0f, 0.0f).setColor(-1).setUv(1.0f, 0.0f).setLight(n);
        vertexConsumer.addVertex(matrix4f, -7.0f, -7.0f, 0.0f).setColor(-1).setUv(0.0f, 0.0f).setLight(n);
        if (mapItemSavedData != null) {
            MapRenderer mapRenderer = this.minecraft.getMapRenderer();
            mapRenderer.extractRenderState(mapId, mapItemSavedData, this.mapRenderState);
            mapRenderer.render(this.mapRenderState, poseStack, multiBufferSource, false, n);
        }
    }

    private void renderPlayerArm(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, float f, float f2, HumanoidArm humanoidArm) {
        boolean bl = humanoidArm != HumanoidArm.LEFT;
        float f3 = bl ? 1.0f : -1.0f;
        float f4 = Mth.sqrt(f2);
        float f5 = -0.3f * Mth.sin(f4 * (float)Math.PI);
        float f6 = 0.4f * Mth.sin(f4 * ((float)Math.PI * 2));
        float f7 = -0.4f * Mth.sin(f2 * (float)Math.PI);
        poseStack.translate(f3 * (f5 + 0.64000005f), f6 + -0.6f + f * -0.6f, f7 + -0.71999997f);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(f3 * 45.0f));
        float f8 = Mth.sin(f2 * f2 * (float)Math.PI);
        float f9 = Mth.sin(f4 * (float)Math.PI);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(f3 * f9 * 70.0f));
        poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(f3 * f8 * -20.0f));
        LocalPlayer localPlayer = this.minecraft.player;
        poseStack.translate(f3 * -1.0f, 3.6f, 3.5f);
        poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(f3 * 120.0f));
        poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(200.0f));
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(f3 * -135.0f));
        poseStack.translate(f3 * 5.6f, 0.0f, 0.0f);
        PlayerRenderer playerRenderer = (PlayerRenderer)this.entityRenderDispatcher.getRenderer(localPlayer);
        ResourceLocation resourceLocation = localPlayer.getSkin().texture();
        if (bl) {
            playerRenderer.renderRightHand(poseStack, multiBufferSource, n, resourceLocation, localPlayer.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE));
        } else {
            playerRenderer.renderLeftHand(poseStack, multiBufferSource, n, resourceLocation, localPlayer.isModelPartShown(PlayerModelPart.LEFT_SLEEVE));
        }
    }

    private void applyEatTransform(PoseStack poseStack, float f, HumanoidArm humanoidArm, ItemStack itemStack, Player player) {
        float f2;
        float f3 = (float)player.getUseItemRemainingTicks() - f + 1.0f;
        float f4 = f3 / (float)itemStack.getUseDuration(player);
        if (f4 < 0.8f) {
            f2 = Mth.abs(Mth.cos(f3 / 4.0f * (float)Math.PI) * 0.1f);
            poseStack.translate(0.0f, f2, 0.0f);
        }
        f2 = 1.0f - (float)Math.pow(f4, 27.0);
        int n = humanoidArm == HumanoidArm.RIGHT ? 1 : -1;
        poseStack.translate(f2 * 0.6f * (float)n, f2 * -0.5f, f2 * 0.0f);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees((float)n * f2 * 90.0f));
        poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(f2 * 10.0f));
        poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees((float)n * f2 * 30.0f));
    }

    private void applyBrushTransform(PoseStack poseStack, float f, HumanoidArm humanoidArm, ItemStack itemStack, Player player, float f2) {
        this.applyItemArmTransform(poseStack, humanoidArm, f2);
        float f3 = player.getUseItemRemainingTicks() % 10;
        float f4 = f3 - f + 1.0f;
        float f5 = 1.0f - f4 / 10.0f;
        float f6 = -90.0f;
        float f7 = 60.0f;
        float f8 = 150.0f;
        float f9 = -15.0f;
        int n = 2;
        float f10 = -15.0f + 75.0f * Mth.cos(f5 * 2.0f * (float)Math.PI);
        if (humanoidArm != HumanoidArm.RIGHT) {
            poseStack.translate(0.1, 0.83, 0.35);
            poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(-80.0f));
            poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(-90.0f));
            poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(f10));
            poseStack.translate(-0.3, 0.22, 0.35);
        } else {
            poseStack.translate(-0.25, 0.22, 0.35);
            poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(-80.0f));
            poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(90.0f));
            poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(0.0f));
            poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(f10));
        }
    }

    private void applyItemArmAttackTransform(PoseStack poseStack, HumanoidArm humanoidArm, float f) {
        int n = humanoidArm == HumanoidArm.RIGHT ? 1 : -1;
        float f2 = Mth.sin(f * f * (float)Math.PI);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees((float)n * (45.0f + f2 * -20.0f)));
        float f3 = Mth.sin(Mth.sqrt(f) * (float)Math.PI);
        poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees((float)n * f3 * -20.0f));
        poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(f3 * -80.0f));
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees((float)n * -45.0f));
    }

    private void applyItemArmTransform(PoseStack poseStack, HumanoidArm humanoidArm, float f) {
        int n = humanoidArm == HumanoidArm.RIGHT ? 1 : -1;
        poseStack.translate((float)n * 0.56f, -0.52f + f * -0.6f, -0.72f);
    }

    public void renderHandsWithItems(float f, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, LocalPlayer localPlayer, int n) {
        float f2;
        float f3;
        float f4 = localPlayer.getAttackAnim(f);
        InteractionHand interactionHand = (InteractionHand)((Object)MoreObjects.firstNonNull((Object)((Object)localPlayer.swingingArm), (Object)((Object)InteractionHand.MAIN_HAND)));
        float f5 = localPlayer.getXRot(f);
        HandRenderSelection handRenderSelection = ItemInHandRenderer.evaluateWhichHandsToRender(localPlayer);
        float f6 = Mth.lerp(f, localPlayer.xBobO, localPlayer.xBob);
        float f7 = Mth.lerp(f, localPlayer.yBobO, localPlayer.yBob);
        poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees((localPlayer.getViewXRot(f) - f6) * 0.1f));
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees((localPlayer.getViewYRot(f) - f7) * 0.1f));
        if (handRenderSelection.renderMainHand) {
            f3 = interactionHand == InteractionHand.MAIN_HAND ? f4 : 0.0f;
            f2 = 1.0f - Mth.lerp(f, this.oMainHandHeight, this.mainHandHeight);
            this.renderArmWithItem(localPlayer, f, f5, InteractionHand.MAIN_HAND, f3, this.mainHandItem, f2, poseStack, bufferSource, n);
        }
        if (handRenderSelection.renderOffHand) {
            f3 = interactionHand == InteractionHand.OFF_HAND ? f4 : 0.0f;
            f2 = 1.0f - Mth.lerp(f, this.oOffHandHeight, this.offHandHeight);
            this.renderArmWithItem(localPlayer, f, f5, InteractionHand.OFF_HAND, f3, this.offHandItem, f2, poseStack, bufferSource, n);
        }
        bufferSource.endBatch();
    }

    @VisibleForTesting
    static HandRenderSelection evaluateWhichHandsToRender(LocalPlayer localPlayer) {
        boolean bl;
        ItemStack itemStack = localPlayer.getMainHandItem();
        ItemStack itemStack2 = localPlayer.getOffhandItem();
        boolean bl2 = itemStack.is(Items.BOW) || itemStack2.is(Items.BOW);
        boolean bl3 = bl = itemStack.is(Items.CROSSBOW) || itemStack2.is(Items.CROSSBOW);
        if (!bl2 && !bl) {
            return HandRenderSelection.RENDER_BOTH_HANDS;
        }
        if (localPlayer.isUsingItem()) {
            return ItemInHandRenderer.selectionUsingItemWhileHoldingBowLike(localPlayer);
        }
        if (ItemInHandRenderer.isChargedCrossbow(itemStack)) {
            return HandRenderSelection.RENDER_MAIN_HAND_ONLY;
        }
        return HandRenderSelection.RENDER_BOTH_HANDS;
    }

    private static HandRenderSelection selectionUsingItemWhileHoldingBowLike(LocalPlayer localPlayer) {
        ItemStack itemStack = localPlayer.getUseItem();
        InteractionHand interactionHand = localPlayer.getUsedItemHand();
        if (itemStack.is(Items.BOW) || itemStack.is(Items.CROSSBOW)) {
            return HandRenderSelection.onlyForHand(interactionHand);
        }
        return interactionHand == InteractionHand.MAIN_HAND && ItemInHandRenderer.isChargedCrossbow(localPlayer.getOffhandItem()) ? HandRenderSelection.RENDER_MAIN_HAND_ONLY : HandRenderSelection.RENDER_BOTH_HANDS;
    }

    private static boolean isChargedCrossbow(ItemStack itemStack) {
        return itemStack.is(Items.CROSSBOW) && CrossbowItem.isCharged(itemStack);
    }

    private void renderArmWithItem(AbstractClientPlayer abstractClientPlayer, float f, float f2, InteractionHand interactionHand, float f3, ItemStack itemStack, float f4, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        if (abstractClientPlayer.isScoping()) {
            return;
        }
        boolean bl = interactionHand == InteractionHand.MAIN_HAND;
        HumanoidArm humanoidArm = bl ? abstractClientPlayer.getMainArm() : abstractClientPlayer.getMainArm().getOpposite();
        poseStack.pushPose();
        if (itemStack.isEmpty()) {
            if (bl && !abstractClientPlayer.isInvisible()) {
                this.renderPlayerArm(poseStack, multiBufferSource, n, f4, f3, humanoidArm);
            }
        } else if (itemStack.has(DataComponents.MAP_ID)) {
            if (bl && this.offHandItem.isEmpty()) {
                this.renderTwoHandedMap(poseStack, multiBufferSource, n, f2, f4, f3);
            } else {
                this.renderOneHandedMap(poseStack, multiBufferSource, n, f4, humanoidArm, f3, itemStack);
            }
        } else if (itemStack.is(Items.CROSSBOW)) {
            int n2;
            boolean bl2 = CrossbowItem.isCharged(itemStack);
            boolean bl3 = humanoidArm == HumanoidArm.RIGHT;
            int n3 = n2 = bl3 ? 1 : -1;
            if (abstractClientPlayer.isUsingItem() && abstractClientPlayer.getUseItemRemainingTicks() > 0 && abstractClientPlayer.getUsedItemHand() == interactionHand && !bl2) {
                this.applyItemArmTransform(poseStack, humanoidArm, f4);
                poseStack.translate((float)n2 * -0.4785682f, -0.094387f, 0.05731531f);
                poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(-11.935f));
                poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees((float)n2 * 65.3f));
                poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees((float)n2 * -9.785f));
                float f5 = (float)itemStack.getUseDuration(abstractClientPlayer) - ((float)abstractClientPlayer.getUseItemRemainingTicks() - f + 1.0f);
                float f6 = f5 / (float)CrossbowItem.getChargeDuration(itemStack, abstractClientPlayer);
                if (f6 > 1.0f) {
                    f6 = 1.0f;
                }
                if (f6 > 0.1f) {
                    float f7 = Mth.sin((f5 - 0.1f) * 1.3f);
                    float f8 = f6 - 0.1f;
                    float f9 = f7 * f8;
                    poseStack.translate(f9 * 0.0f, f9 * 0.004f, f9 * 0.0f);
                }
                poseStack.translate(f6 * 0.0f, f6 * 0.0f, f6 * 0.04f);
                poseStack.scale(1.0f, 1.0f, 1.0f + f6 * 0.2f);
                poseStack.mulPose((Quaternionfc)Axis.YN.rotationDegrees((float)n2 * 45.0f));
            } else {
                this.swingArm(f3, f4, poseStack, n2, humanoidArm);
                if (bl2 && f3 < 0.001f && bl) {
                    poseStack.translate((float)n2 * -0.641864f, 0.0f, 0.0f);
                    poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees((float)n2 * 10.0f));
                }
            }
            this.renderItem(abstractClientPlayer, itemStack, bl3 ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, poseStack, multiBufferSource, n);
        } else {
            int n4;
            boolean bl4 = humanoidArm == HumanoidArm.RIGHT;
            int n5 = n4 = bl4 ? 1 : -1;
            if (abstractClientPlayer.isUsingItem() && abstractClientPlayer.getUseItemRemainingTicks() > 0 && abstractClientPlayer.getUsedItemHand() == interactionHand) {
                switch (itemStack.getUseAnimation()) {
                    case NONE: {
                        this.applyItemArmTransform(poseStack, humanoidArm, f4);
                        break;
                    }
                    case EAT: 
                    case DRINK: {
                        this.applyEatTransform(poseStack, f, humanoidArm, itemStack, abstractClientPlayer);
                        this.applyItemArmTransform(poseStack, humanoidArm, f4);
                        break;
                    }
                    case BLOCK: {
                        this.applyItemArmTransform(poseStack, humanoidArm, f4);
                        if (itemStack.getItem() instanceof ShieldItem) break;
                        poseStack.translate((float)n4 * -0.14142136f, 0.08f, 0.14142136f);
                        poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(-102.25f));
                        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees((float)n4 * 13.365f));
                        poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees((float)n4 * 78.05f));
                        break;
                    }
                    case BOW: {
                        this.applyItemArmTransform(poseStack, humanoidArm, f4);
                        poseStack.translate((float)n4 * -0.2785682f, 0.18344387f, 0.15731531f);
                        poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(-13.935f));
                        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees((float)n4 * 35.3f));
                        poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees((float)n4 * -9.785f));
                        float f10 = (float)itemStack.getUseDuration(abstractClientPlayer) - ((float)abstractClientPlayer.getUseItemRemainingTicks() - f + 1.0f);
                        float f11 = f10 / 20.0f;
                        f11 = (f11 * f11 + f11 * 2.0f) / 3.0f;
                        if (f11 > 1.0f) {
                            f11 = 1.0f;
                        }
                        if (f11 > 0.1f) {
                            float f12 = Mth.sin((f10 - 0.1f) * 1.3f);
                            float f13 = f11 - 0.1f;
                            float f14 = f12 * f13;
                            poseStack.translate(f14 * 0.0f, f14 * 0.004f, f14 * 0.0f);
                        }
                        poseStack.translate(f11 * 0.0f, f11 * 0.0f, f11 * 0.04f);
                        poseStack.scale(1.0f, 1.0f, 1.0f + f11 * 0.2f);
                        poseStack.mulPose((Quaternionfc)Axis.YN.rotationDegrees((float)n4 * 45.0f));
                        break;
                    }
                    case SPEAR: {
                        this.applyItemArmTransform(poseStack, humanoidArm, f4);
                        poseStack.translate((float)n4 * -0.5f, 0.7f, 0.1f);
                        poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(-55.0f));
                        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees((float)n4 * 35.3f));
                        poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees((float)n4 * -9.785f));
                        float f15 = (float)itemStack.getUseDuration(abstractClientPlayer) - ((float)abstractClientPlayer.getUseItemRemainingTicks() - f + 1.0f);
                        float f16 = f15 / 10.0f;
                        if (f16 > 1.0f) {
                            f16 = 1.0f;
                        }
                        if (f16 > 0.1f) {
                            float f17 = Mth.sin((f15 - 0.1f) * 1.3f);
                            float f18 = f16 - 0.1f;
                            float f19 = f17 * f18;
                            poseStack.translate(f19 * 0.0f, f19 * 0.004f, f19 * 0.0f);
                        }
                        poseStack.translate(0.0f, 0.0f, f16 * 0.2f);
                        poseStack.scale(1.0f, 1.0f, 1.0f + f16 * 0.2f);
                        poseStack.mulPose((Quaternionfc)Axis.YN.rotationDegrees((float)n4 * 45.0f));
                        break;
                    }
                    case BRUSH: {
                        this.applyBrushTransform(poseStack, f, humanoidArm, itemStack, abstractClientPlayer, f4);
                        break;
                    }
                    case BUNDLE: {
                        this.swingArm(f3, f4, poseStack, n4, humanoidArm);
                    }
                }
            } else if (abstractClientPlayer.isAutoSpinAttack()) {
                this.applyItemArmTransform(poseStack, humanoidArm, f4);
                poseStack.translate((float)n4 * -0.4f, 0.8f, 0.3f);
                poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees((float)n4 * 65.0f));
                poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees((float)n4 * -85.0f));
            } else {
                this.swingArm(f3, f4, poseStack, n4, humanoidArm);
            }
            this.renderItem(abstractClientPlayer, itemStack, bl4 ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, poseStack, multiBufferSource, n);
        }
        poseStack.popPose();
    }

    private void swingArm(float f, float f2, PoseStack poseStack, int n, HumanoidArm humanoidArm) {
        float f3 = -0.4f * Mth.sin(Mth.sqrt(f) * (float)Math.PI);
        float f4 = 0.2f * Mth.sin(Mth.sqrt(f) * ((float)Math.PI * 2));
        float f5 = -0.2f * Mth.sin(f * (float)Math.PI);
        poseStack.translate((float)n * f3, f4, f5);
        this.applyItemArmTransform(poseStack, humanoidArm, f2);
        this.applyItemArmAttackTransform(poseStack, humanoidArm, f);
    }

    private boolean shouldInstantlyReplaceVisibleItem(ItemStack itemStack, ItemStack itemStack2) {
        if (ItemStack.matches(itemStack, itemStack2)) {
            return true;
        }
        return !this.itemModelResolver.shouldPlaySwapAnimation(itemStack2);
    }

    public void tick() {
        this.oMainHandHeight = this.mainHandHeight;
        this.oOffHandHeight = this.offHandHeight;
        LocalPlayer localPlayer = this.minecraft.player;
        ItemStack itemStack = localPlayer.getMainHandItem();
        ItemStack itemStack2 = localPlayer.getOffhandItem();
        if (this.shouldInstantlyReplaceVisibleItem(this.mainHandItem, itemStack)) {
            this.mainHandItem = itemStack;
        }
        if (this.shouldInstantlyReplaceVisibleItem(this.offHandItem, itemStack2)) {
            this.offHandItem = itemStack2;
        }
        if (localPlayer.isHandsBusy()) {
            this.mainHandHeight = Mth.clamp(this.mainHandHeight - 0.4f, 0.0f, 1.0f);
            this.offHandHeight = Mth.clamp(this.offHandHeight - 0.4f, 0.0f, 1.0f);
        } else {
            float f = localPlayer.getAttackStrengthScale(1.0f);
            float f2 = this.mainHandItem != itemStack ? 0.0f : f * f * f;
            float f3 = this.offHandItem != itemStack2 ? 0.0f : 1.0f;
            this.mainHandHeight += Mth.clamp(f2 - this.mainHandHeight, -0.4f, 0.4f);
            this.offHandHeight += Mth.clamp(f3 - this.offHandHeight, -0.4f, 0.4f);
        }
        if (this.mainHandHeight < 0.1f) {
            this.mainHandItem = itemStack;
        }
        if (this.offHandHeight < 0.1f) {
            this.offHandItem = itemStack2;
        }
    }

    public void itemUsed(InteractionHand interactionHand) {
        if (interactionHand == InteractionHand.MAIN_HAND) {
            this.mainHandHeight = 0.0f;
        } else {
            this.offHandHeight = 0.0f;
        }
    }

    @VisibleForTesting
    static enum HandRenderSelection {
        RENDER_BOTH_HANDS(true, true),
        RENDER_MAIN_HAND_ONLY(true, false),
        RENDER_OFF_HAND_ONLY(false, true);

        final boolean renderMainHand;
        final boolean renderOffHand;

        private HandRenderSelection(boolean bl, boolean bl2) {
            this.renderMainHand = bl;
            this.renderOffHand = bl2;
        }

        public static HandRenderSelection onlyForHand(InteractionHand interactionHand) {
            return interactionHand == InteractionHand.MAIN_HAND ? RENDER_MAIN_HAND_ONLY : RENDER_OFF_HAND_ONLY;
        }
    }
}

