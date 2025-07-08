/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import net.minecraft.client.renderer.entity.layers.BeeStingerLayer;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.Deadmau5EarsLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ParrotOnShoulderLayer;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.SpinAttackEffectLayer;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.Scoreboard;
import org.joml.Quaternionfc;

public class PlayerRenderer
extends LivingEntityRenderer<AbstractClientPlayer, PlayerRenderState, PlayerModel> {
    public PlayerRenderer(EntityRendererProvider.Context context, boolean bl) {
        super(context, new PlayerModel(context.bakeLayer(bl ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), bl), 0.5f);
        this.addLayer(new HumanoidArmorLayer(this, new HumanoidArmorModel(context.bakeLayer(bl ? ModelLayers.PLAYER_SLIM_INNER_ARMOR : ModelLayers.PLAYER_INNER_ARMOR)), new HumanoidArmorModel(context.bakeLayer(bl ? ModelLayers.PLAYER_SLIM_OUTER_ARMOR : ModelLayers.PLAYER_OUTER_ARMOR)), context.getEquipmentRenderer()));
        this.addLayer(new PlayerItemInHandLayer<PlayerRenderState, PlayerModel>(this));
        this.addLayer(new ArrowLayer<PlayerModel>(this, context));
        this.addLayer(new Deadmau5EarsLayer(this, context.getModelSet()));
        this.addLayer(new CapeLayer(this, context.getModelSet(), context.getEquipmentAssets()));
        this.addLayer(new CustomHeadLayer<PlayerRenderState, PlayerModel>(this, context.getModelSet()));
        this.addLayer(new WingsLayer<PlayerRenderState, PlayerModel>(this, context.getModelSet(), context.getEquipmentRenderer()));
        this.addLayer(new ParrotOnShoulderLayer(this, context.getModelSet()));
        this.addLayer(new SpinAttackEffectLayer(this, context.getModelSet()));
        this.addLayer(new BeeStingerLayer<PlayerModel>(this, context));
    }

    @Override
    protected boolean shouldRenderLayers(PlayerRenderState playerRenderState) {
        return !playerRenderState.isSpectator;
    }

    @Override
    public Vec3 getRenderOffset(PlayerRenderState playerRenderState) {
        Vec3 vec3 = super.getRenderOffset(playerRenderState);
        if (playerRenderState.isCrouching) {
            return vec3.add(0.0, (double)(playerRenderState.scale * -2.0f) / 16.0, 0.0);
        }
        return vec3;
    }

    private static HumanoidModel.ArmPose getArmPose(AbstractClientPlayer abstractClientPlayer, HumanoidArm humanoidArm) {
        ItemStack itemStack = abstractClientPlayer.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack itemStack2 = abstractClientPlayer.getItemInHand(InteractionHand.OFF_HAND);
        HumanoidModel.ArmPose armPose = PlayerRenderer.getArmPose(abstractClientPlayer, itemStack, InteractionHand.MAIN_HAND);
        HumanoidModel.ArmPose armPose2 = PlayerRenderer.getArmPose(abstractClientPlayer, itemStack2, InteractionHand.OFF_HAND);
        if (armPose.isTwoHanded()) {
            HumanoidModel.ArmPose armPose3 = armPose2 = itemStack2.isEmpty() ? HumanoidModel.ArmPose.EMPTY : HumanoidModel.ArmPose.ITEM;
        }
        if (abstractClientPlayer.getMainArm() == humanoidArm) {
            return armPose;
        }
        return armPose2;
    }

    private static HumanoidModel.ArmPose getArmPose(Player player, ItemStack itemStack, InteractionHand interactionHand) {
        if (itemStack.isEmpty()) {
            return HumanoidModel.ArmPose.EMPTY;
        }
        if (!player.swinging && itemStack.is(Items.CROSSBOW) && CrossbowItem.isCharged(itemStack)) {
            return HumanoidModel.ArmPose.CROSSBOW_HOLD;
        }
        if (player.getUsedItemHand() == interactionHand && player.getUseItemRemainingTicks() > 0) {
            ItemUseAnimation itemUseAnimation = itemStack.getUseAnimation();
            if (itemUseAnimation == ItemUseAnimation.BLOCK) {
                return HumanoidModel.ArmPose.BLOCK;
            }
            if (itemUseAnimation == ItemUseAnimation.BOW) {
                return HumanoidModel.ArmPose.BOW_AND_ARROW;
            }
            if (itemUseAnimation == ItemUseAnimation.SPEAR) {
                return HumanoidModel.ArmPose.THROW_SPEAR;
            }
            if (itemUseAnimation == ItemUseAnimation.CROSSBOW) {
                return HumanoidModel.ArmPose.CROSSBOW_CHARGE;
            }
            if (itemUseAnimation == ItemUseAnimation.SPYGLASS) {
                return HumanoidModel.ArmPose.SPYGLASS;
            }
            if (itemUseAnimation == ItemUseAnimation.TOOT_HORN) {
                return HumanoidModel.ArmPose.TOOT_HORN;
            }
            if (itemUseAnimation == ItemUseAnimation.BRUSH) {
                return HumanoidModel.ArmPose.BRUSH;
            }
        }
        return HumanoidModel.ArmPose.ITEM;
    }

    @Override
    public ResourceLocation getTextureLocation(PlayerRenderState playerRenderState) {
        return playerRenderState.skin.texture();
    }

    @Override
    protected void scale(PlayerRenderState playerRenderState, PoseStack poseStack) {
        float f = 0.9375f;
        poseStack.scale(0.9375f, 0.9375f, 0.9375f);
    }

    @Override
    protected void renderNameTag(PlayerRenderState playerRenderState, Component component, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        poseStack.pushPose();
        if (playerRenderState.scoreText != null) {
            super.renderNameTag(playerRenderState, playerRenderState.scoreText, poseStack, multiBufferSource, n);
            Objects.requireNonNull(this.getFont());
            poseStack.translate(0.0f, 9.0f * 1.15f * 0.025f, 0.0f);
        }
        super.renderNameTag(playerRenderState, component, poseStack, multiBufferSource, n);
        poseStack.popPose();
    }

    @Override
    public PlayerRenderState createRenderState() {
        return new PlayerRenderState();
    }

    @Override
    public void extractRenderState(AbstractClientPlayer abstractClientPlayer, PlayerRenderState playerRenderState, float f) {
        Object object;
        super.extractRenderState(abstractClientPlayer, playerRenderState, f);
        HumanoidMobRenderer.extractHumanoidRenderState(abstractClientPlayer, playerRenderState, f, this.itemModelResolver);
        playerRenderState.leftArmPose = PlayerRenderer.getArmPose(abstractClientPlayer, HumanoidArm.LEFT);
        playerRenderState.rightArmPose = PlayerRenderer.getArmPose(abstractClientPlayer, HumanoidArm.RIGHT);
        playerRenderState.skin = abstractClientPlayer.getSkin();
        playerRenderState.arrowCount = abstractClientPlayer.getArrowCount();
        playerRenderState.stingerCount = abstractClientPlayer.getStingerCount();
        playerRenderState.useItemRemainingTicks = abstractClientPlayer.getUseItemRemainingTicks();
        playerRenderState.swinging = abstractClientPlayer.swinging;
        playerRenderState.isSpectator = abstractClientPlayer.isSpectator();
        playerRenderState.showHat = abstractClientPlayer.isModelPartShown(PlayerModelPart.HAT);
        playerRenderState.showJacket = abstractClientPlayer.isModelPartShown(PlayerModelPart.JACKET);
        playerRenderState.showLeftPants = abstractClientPlayer.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG);
        playerRenderState.showRightPants = abstractClientPlayer.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG);
        playerRenderState.showLeftSleeve = abstractClientPlayer.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
        playerRenderState.showRightSleeve = abstractClientPlayer.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);
        playerRenderState.showCape = abstractClientPlayer.isModelPartShown(PlayerModelPart.CAPE);
        PlayerRenderer.extractFlightData(abstractClientPlayer, playerRenderState, f);
        PlayerRenderer.extractCapeState(abstractClientPlayer, playerRenderState, f);
        if (playerRenderState.distanceToCameraSq < 100.0) {
            object = abstractClientPlayer.getScoreboard();
            Objective objective = ((Scoreboard)object).getDisplayObjective(DisplaySlot.BELOW_NAME);
            if (objective != null) {
                ReadOnlyScoreInfo readOnlyScoreInfo = ((Scoreboard)object).getPlayerScoreInfo(abstractClientPlayer, objective);
                MutableComponent mutableComponent = ReadOnlyScoreInfo.safeFormatValue(readOnlyScoreInfo, objective.numberFormatOrDefault(StyledFormat.NO_STYLE));
                playerRenderState.scoreText = Component.empty().append(mutableComponent).append(CommonComponents.SPACE).append(objective.getDisplayName());
            } else {
                playerRenderState.scoreText = null;
            }
        } else {
            playerRenderState.scoreText = null;
        }
        playerRenderState.parrotOnLeftShoulder = PlayerRenderer.getParrotOnShoulder(abstractClientPlayer, true);
        playerRenderState.parrotOnRightShoulder = PlayerRenderer.getParrotOnShoulder(abstractClientPlayer, false);
        playerRenderState.id = abstractClientPlayer.getId();
        playerRenderState.name = abstractClientPlayer.getGameProfile().getName();
        playerRenderState.heldOnHead.clear();
        if (playerRenderState.isUsingItem && ((ItemStack)(object = abstractClientPlayer.getItemInHand(playerRenderState.useItemHand))).is(Items.SPYGLASS)) {
            this.itemModelResolver.updateForLiving(playerRenderState.heldOnHead, (ItemStack)object, ItemDisplayContext.HEAD, abstractClientPlayer);
        }
    }

    private static void extractFlightData(AbstractClientPlayer abstractClientPlayer, PlayerRenderState playerRenderState, float f) {
        playerRenderState.fallFlyingTimeInTicks = (float)abstractClientPlayer.getFallFlyingTicks() + f;
        Vec3 vec3 = abstractClientPlayer.getViewVector(f);
        Vec3 vec32 = abstractClientPlayer.getDeltaMovementLerped(f);
        if (vec32.horizontalDistanceSqr() > (double)1.0E-5f && vec3.horizontalDistanceSqr() > (double)1.0E-5f) {
            playerRenderState.shouldApplyFlyingYRot = true;
            double d = vec32.horizontal().normalize().dot(vec3.horizontal().normalize());
            double d2 = vec32.x * vec3.z - vec32.z * vec3.x;
            playerRenderState.flyingYRot = (float)(Math.signum(d2) * Math.acos(Math.min(1.0, Math.abs(d))));
        } else {
            playerRenderState.shouldApplyFlyingYRot = false;
            playerRenderState.flyingYRot = 0.0f;
        }
    }

    private static void extractCapeState(AbstractClientPlayer abstractClientPlayer, PlayerRenderState playerRenderState, float f) {
        double d = Mth.lerp((double)f, abstractClientPlayer.xCloakO, abstractClientPlayer.xCloak) - Mth.lerp((double)f, abstractClientPlayer.xo, abstractClientPlayer.getX());
        double d2 = Mth.lerp((double)f, abstractClientPlayer.yCloakO, abstractClientPlayer.yCloak) - Mth.lerp((double)f, abstractClientPlayer.yo, abstractClientPlayer.getY());
        double d3 = Mth.lerp((double)f, abstractClientPlayer.zCloakO, abstractClientPlayer.zCloak) - Mth.lerp((double)f, abstractClientPlayer.zo, abstractClientPlayer.getZ());
        float f2 = Mth.rotLerp(f, abstractClientPlayer.yBodyRotO, abstractClientPlayer.yBodyRot);
        double d4 = Mth.sin(f2 * ((float)Math.PI / 180));
        double d5 = -Mth.cos(f2 * ((float)Math.PI / 180));
        playerRenderState.capeFlap = (float)d2 * 10.0f;
        playerRenderState.capeFlap = Mth.clamp(playerRenderState.capeFlap, -6.0f, 32.0f);
        playerRenderState.capeLean = (float)(d * d4 + d3 * d5) * 100.0f;
        playerRenderState.capeLean *= 1.0f - playerRenderState.fallFlyingScale();
        playerRenderState.capeLean = Mth.clamp(playerRenderState.capeLean, 0.0f, 150.0f);
        playerRenderState.capeLean2 = (float)(d * d5 - d3 * d4) * 100.0f;
        playerRenderState.capeLean2 = Mth.clamp(playerRenderState.capeLean2, -20.0f, 20.0f);
        float f3 = Mth.lerp(f, abstractClientPlayer.oBob, abstractClientPlayer.bob);
        float f4 = Mth.lerp(f, abstractClientPlayer.walkDistO, abstractClientPlayer.walkDist);
        playerRenderState.capeFlap += Mth.sin(f4 * 6.0f) * 32.0f * f3;
    }

    @Nullable
    private static Parrot.Variant getParrotOnShoulder(AbstractClientPlayer abstractClientPlayer, boolean bl) {
        CompoundTag compoundTag;
        CompoundTag compoundTag2 = compoundTag = bl ? abstractClientPlayer.getShoulderEntityLeft() : abstractClientPlayer.getShoulderEntityRight();
        if (compoundTag.isEmpty()) {
            return null;
        }
        EntityType entityType = compoundTag.read("id", EntityType.CODEC).orElse(null);
        if (entityType == EntityType.PARROT) {
            return compoundTag.read("Variant", Parrot.Variant.LEGACY_CODEC).orElse(Parrot.Variant.RED_BLUE);
        }
        return null;
    }

    public void renderRightHand(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, ResourceLocation resourceLocation, boolean bl) {
        this.renderHand(poseStack, multiBufferSource, n, resourceLocation, ((PlayerModel)this.model).rightArm, bl);
    }

    public void renderLeftHand(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, ResourceLocation resourceLocation, boolean bl) {
        this.renderHand(poseStack, multiBufferSource, n, resourceLocation, ((PlayerModel)this.model).leftArm, bl);
    }

    private void renderHand(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, ResourceLocation resourceLocation, ModelPart modelPart, boolean bl) {
        PlayerModel playerModel = (PlayerModel)this.getModel();
        modelPart.resetPose();
        modelPart.visible = true;
        playerModel.leftSleeve.visible = bl;
        playerModel.rightSleeve.visible = bl;
        playerModel.leftArm.zRot = -0.1f;
        playerModel.rightArm.zRot = 0.1f;
        modelPart.render(poseStack, multiBufferSource.getBuffer(RenderType.entityTranslucent(resourceLocation)), n, OverlayTexture.NO_OVERLAY);
    }

    @Override
    protected void setupRotations(PlayerRenderState playerRenderState, PoseStack poseStack, float f, float f2) {
        float f3 = playerRenderState.swimAmount;
        float f4 = playerRenderState.xRot;
        if (playerRenderState.isFallFlying) {
            super.setupRotations(playerRenderState, poseStack, f, f2);
            float f5 = playerRenderState.fallFlyingScale();
            if (!playerRenderState.isAutoSpinAttack) {
                poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(f5 * (-90.0f - f4)));
            }
            if (playerRenderState.shouldApplyFlyingYRot) {
                poseStack.mulPose((Quaternionfc)Axis.YP.rotation(playerRenderState.flyingYRot));
            }
        } else if (f3 > 0.0f) {
            super.setupRotations(playerRenderState, poseStack, f, f2);
            float f6 = playerRenderState.isInWater ? -90.0f - f4 : -90.0f;
            float f7 = Mth.lerp(f3, 0.0f, f6);
            poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(f7));
            if (playerRenderState.isVisuallySwimming) {
                poseStack.translate(0.0f, -1.0f, 0.3f);
            }
        } else {
            super.setupRotations(playerRenderState, poseStack, f, f2);
        }
    }

    @Override
    public /* synthetic */ ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((PlayerRenderState)livingEntityRenderState);
    }

    @Override
    protected /* synthetic */ boolean shouldRenderLayers(LivingEntityRenderState livingEntityRenderState) {
        return this.shouldRenderLayers((PlayerRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }

    @Override
    public /* synthetic */ Vec3 getRenderOffset(EntityRenderState entityRenderState) {
        return this.getRenderOffset((PlayerRenderState)entityRenderState);
    }
}

