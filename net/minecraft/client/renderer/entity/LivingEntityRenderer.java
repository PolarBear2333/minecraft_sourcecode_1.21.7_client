/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList$Builder
 *  com.google.common.collect.Lists
 *  javax.annotation.Nullable
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.HitboxRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.joml.Quaternionfc;

public abstract class LivingEntityRenderer<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>>
extends EntityRenderer<T, S>
implements RenderLayerParent<S, M> {
    private static final float EYE_BED_OFFSET = 0.1f;
    protected M model;
    protected final ItemModelResolver itemModelResolver;
    protected final List<RenderLayer<S, M>> layers = Lists.newArrayList();

    public LivingEntityRenderer(EntityRendererProvider.Context context, M m, float f) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
        this.model = m;
        this.shadowRadius = f;
    }

    protected final boolean addLayer(RenderLayer<S, M> renderLayer) {
        return this.layers.add(renderLayer);
    }

    @Override
    public M getModel() {
        return this.model;
    }

    @Override
    protected AABB getBoundingBoxForCulling(T t) {
        AABB aABB = super.getBoundingBoxForCulling(t);
        if (((LivingEntity)t).getItemBySlot(EquipmentSlot.HEAD).is(Items.DRAGON_HEAD)) {
            float f = 0.5f;
            return aABB.inflate(0.5, 0.5, 0.5);
        }
        return aABB;
    }

    @Override
    public void render(S s, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        Direction direction;
        poseStack.pushPose();
        if (((LivingEntityRenderState)s).hasPose(Pose.SLEEPING) && (direction = ((LivingEntityRenderState)s).bedOrientation) != null) {
            float f = ((LivingEntityRenderState)s).eyeHeight - 0.1f;
            poseStack.translate((float)(-direction.getStepX()) * f, 0.0f, (float)(-direction.getStepZ()) * f);
        }
        float f = ((LivingEntityRenderState)s).scale;
        poseStack.scale(f, f, f);
        this.setupRotations(s, poseStack, ((LivingEntityRenderState)s).bodyRot, f);
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        this.scale(s, poseStack);
        poseStack.translate(0.0f, -1.501f, 0.0f);
        ((EntityModel)this.model).setupAnim(s);
        boolean bl = this.isBodyVisible(s);
        boolean bl2 = !bl && !((LivingEntityRenderState)s).isInvisibleToPlayer;
        RenderType renderType = this.getRenderType(s, bl, bl2, ((LivingEntityRenderState)s).appearsGlowing);
        if (renderType != null) {
            VertexConsumer vertexConsumer = multiBufferSource.getBuffer(renderType);
            int n2 = LivingEntityRenderer.getOverlayCoords(s, this.getWhiteOverlayProgress(s));
            int n3 = bl2 ? 0x26FFFFFF : -1;
            int n4 = ARGB.multiply(n3, this.getModelTint(s));
            ((Model)this.model).renderToBuffer(poseStack, vertexConsumer, n, n2, n4);
        }
        if (this.shouldRenderLayers(s)) {
            for (RenderLayer renderLayer : this.layers) {
                renderLayer.render(poseStack, multiBufferSource, n, s, ((LivingEntityRenderState)s).yRot, ((LivingEntityRenderState)s).xRot);
            }
        }
        poseStack.popPose();
        super.render(s, poseStack, multiBufferSource, n);
    }

    protected boolean shouldRenderLayers(S s) {
        return true;
    }

    protected int getModelTint(S s) {
        return -1;
    }

    public abstract ResourceLocation getTextureLocation(S var1);

    @Nullable
    protected RenderType getRenderType(S s, boolean bl, boolean bl2, boolean bl3) {
        ResourceLocation resourceLocation = this.getTextureLocation(s);
        if (bl2) {
            return RenderType.itemEntityTranslucentCull(resourceLocation);
        }
        if (bl) {
            return ((Model)this.model).renderType(resourceLocation);
        }
        if (bl3) {
            return RenderType.outline(resourceLocation);
        }
        return null;
    }

    public static int getOverlayCoords(LivingEntityRenderState livingEntityRenderState, float f) {
        return OverlayTexture.pack(OverlayTexture.u(f), OverlayTexture.v(livingEntityRenderState.hasRedOverlay));
    }

    protected boolean isBodyVisible(S s) {
        return !((LivingEntityRenderState)s).isInvisible;
    }

    private static float sleepDirectionToRotation(Direction direction) {
        switch (direction) {
            case SOUTH: {
                return 90.0f;
            }
            case WEST: {
                return 0.0f;
            }
            case NORTH: {
                return 270.0f;
            }
            case EAST: {
                return 180.0f;
            }
        }
        return 0.0f;
    }

    protected boolean isShaking(S s) {
        return ((LivingEntityRenderState)s).isFullyFrozen;
    }

    protected void setupRotations(S s, PoseStack poseStack, float f, float f2) {
        if (this.isShaking(s)) {
            f += (float)(Math.cos((float)Mth.floor(((LivingEntityRenderState)s).ageInTicks) * 3.25f) * Math.PI * (double)0.4f);
        }
        if (!((LivingEntityRenderState)s).hasPose(Pose.SLEEPING)) {
            poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(180.0f - f));
        }
        if (((LivingEntityRenderState)s).deathTime > 0.0f) {
            float f3 = (((LivingEntityRenderState)s).deathTime - 1.0f) / 20.0f * 1.6f;
            if ((f3 = Mth.sqrt(f3)) > 1.0f) {
                f3 = 1.0f;
            }
            poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(f3 * this.getFlipDegrees()));
        } else if (((LivingEntityRenderState)s).isAutoSpinAttack) {
            poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(-90.0f - ((LivingEntityRenderState)s).xRot));
            poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(((LivingEntityRenderState)s).ageInTicks * -75.0f));
        } else if (((LivingEntityRenderState)s).hasPose(Pose.SLEEPING)) {
            Direction direction = ((LivingEntityRenderState)s).bedOrientation;
            float f4 = direction != null ? LivingEntityRenderer.sleepDirectionToRotation(direction) : f;
            poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(f4));
            poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(this.getFlipDegrees()));
            poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(270.0f));
        } else if (((LivingEntityRenderState)s).isUpsideDown) {
            poseStack.translate(0.0f, (((LivingEntityRenderState)s).boundingBoxHeight + 0.1f) / f2, 0.0f);
            poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(180.0f));
        }
    }

    protected float getFlipDegrees() {
        return 90.0f;
    }

    protected float getWhiteOverlayProgress(S s) {
        return 0.0f;
    }

    protected void scale(S s, PoseStack poseStack) {
    }

    @Override
    protected boolean shouldShowName(T t, double d) {
        boolean bl;
        if (((Entity)t).isDiscrete()) {
            float f = 32.0f;
            if (d >= 1024.0) {
                return false;
            }
        }
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer localPlayer = minecraft.player;
        boolean bl2 = bl = !((Entity)t).isInvisibleTo(localPlayer);
        if (t != localPlayer) {
            PlayerTeam playerTeam = ((Entity)t).getTeam();
            PlayerTeam playerTeam2 = localPlayer.getTeam();
            if (playerTeam != null) {
                Team.Visibility visibility = ((Team)playerTeam).getNameTagVisibility();
                switch (visibility) {
                    case ALWAYS: {
                        return bl;
                    }
                    case NEVER: {
                        return false;
                    }
                    case HIDE_FOR_OTHER_TEAMS: {
                        return playerTeam2 == null ? bl : playerTeam.isAlliedTo(playerTeam2) && (((Team)playerTeam).canSeeFriendlyInvisibles() || bl);
                    }
                    case HIDE_FOR_OWN_TEAM: {
                        return playerTeam2 == null ? bl : !playerTeam.isAlliedTo(playerTeam2) && bl;
                    }
                }
                return true;
            }
        }
        return Minecraft.renderNames() && t != minecraft.getCameraEntity() && bl && !((Entity)t).isVehicle();
    }

    public static boolean isEntityUpsideDown(LivingEntity livingEntity) {
        String string;
        if ((livingEntity instanceof Player || livingEntity.hasCustomName()) && ("Dinnerbone".equals(string = ChatFormatting.stripFormatting(livingEntity.getName().getString())) || "Grumm".equals(string))) {
            Player player;
            return !(livingEntity instanceof Player) || (player = (Player)livingEntity).isModelPartShown(PlayerModelPart.CAPE);
        }
        return false;
    }

    @Override
    protected float getShadowRadius(S s) {
        return super.getShadowRadius(s) * ((LivingEntityRenderState)s).scale;
    }

    @Override
    public void extractRenderState(T t, S s, float f) {
        DataComponentGetter dataComponentGetter;
        super.extractRenderState(t, s, f);
        float f2 = Mth.rotLerp(f, ((LivingEntity)t).yHeadRotO, ((LivingEntity)t).yHeadRot);
        ((LivingEntityRenderState)s).bodyRot = LivingEntityRenderer.solveBodyRot(t, f2, f);
        ((LivingEntityRenderState)s).yRot = Mth.wrapDegrees(f2 - ((LivingEntityRenderState)s).bodyRot);
        ((LivingEntityRenderState)s).xRot = ((Entity)t).getXRot(f);
        ((LivingEntityRenderState)s).customName = ((Entity)t).getCustomName();
        ((LivingEntityRenderState)s).isUpsideDown = LivingEntityRenderer.isEntityUpsideDown(t);
        if (((LivingEntityRenderState)s).isUpsideDown) {
            ((LivingEntityRenderState)s).xRot *= -1.0f;
            ((LivingEntityRenderState)s).yRot *= -1.0f;
        }
        if (!((Entity)t).isPassenger() && ((LivingEntity)t).isAlive()) {
            ((LivingEntityRenderState)s).walkAnimationPos = ((LivingEntity)t).walkAnimation.position(f);
            ((LivingEntityRenderState)s).walkAnimationSpeed = ((LivingEntity)t).walkAnimation.speed(f);
        } else {
            ((LivingEntityRenderState)s).walkAnimationPos = 0.0f;
            ((LivingEntityRenderState)s).walkAnimationSpeed = 0.0f;
        }
        Object object = ((Entity)t).getVehicle();
        if (object instanceof LivingEntity) {
            dataComponentGetter = (LivingEntity)object;
            ((LivingEntityRenderState)s).wornHeadAnimationPos = ((LivingEntity)dataComponentGetter).walkAnimation.position(f);
        } else {
            ((LivingEntityRenderState)s).wornHeadAnimationPos = ((LivingEntityRenderState)s).walkAnimationPos;
        }
        ((LivingEntityRenderState)s).scale = ((LivingEntity)t).getScale();
        ((LivingEntityRenderState)s).ageScale = ((LivingEntity)t).getAgeScale();
        ((LivingEntityRenderState)s).pose = ((Entity)t).getPose();
        ((LivingEntityRenderState)s).bedOrientation = ((LivingEntity)t).getBedOrientation();
        if (((LivingEntityRenderState)s).bedOrientation != null) {
            ((LivingEntityRenderState)s).eyeHeight = ((Entity)t).getEyeHeight(Pose.STANDING);
        }
        ((LivingEntityRenderState)s).isFullyFrozen = ((Entity)t).isFullyFrozen();
        ((LivingEntityRenderState)s).isBaby = ((LivingEntity)t).isBaby();
        ((LivingEntityRenderState)s).isInWater = ((Entity)t).isInWater();
        ((LivingEntityRenderState)s).isAutoSpinAttack = ((LivingEntity)t).isAutoSpinAttack();
        ((LivingEntityRenderState)s).hasRedOverlay = ((LivingEntity)t).hurtTime > 0 || ((LivingEntity)t).deathTime > 0;
        dataComponentGetter = ((LivingEntity)t).getItemBySlot(EquipmentSlot.HEAD);
        FeatureElement featureElement = ((ItemStack)dataComponentGetter).getItem();
        if (featureElement instanceof BlockItem && (featureElement = ((BlockItem)(object = (BlockItem)featureElement)).getBlock()) instanceof AbstractSkullBlock) {
            AbstractSkullBlock abstractSkullBlock = (AbstractSkullBlock)featureElement;
            ((LivingEntityRenderState)s).wornHeadType = abstractSkullBlock.getType();
            ((LivingEntityRenderState)s).wornHeadProfile = dataComponentGetter.get(DataComponents.PROFILE);
            ((LivingEntityRenderState)s).headItem.clear();
        } else {
            ((LivingEntityRenderState)s).wornHeadType = null;
            ((LivingEntityRenderState)s).wornHeadProfile = null;
            if (!HumanoidArmorLayer.shouldRender((ItemStack)dataComponentGetter, EquipmentSlot.HEAD)) {
                this.itemModelResolver.updateForLiving(((LivingEntityRenderState)s).headItem, (ItemStack)dataComponentGetter, ItemDisplayContext.HEAD, (LivingEntity)t);
            } else {
                ((LivingEntityRenderState)s).headItem.clear();
            }
        }
        ((LivingEntityRenderState)s).deathTime = ((LivingEntity)t).deathTime > 0 ? (float)((LivingEntity)t).deathTime + f : 0.0f;
        object = Minecraft.getInstance();
        ((LivingEntityRenderState)s).isInvisibleToPlayer = ((LivingEntityRenderState)s).isInvisible && ((Entity)t).isInvisibleTo(((Minecraft)object).player);
        ((LivingEntityRenderState)s).appearsGlowing = ((Minecraft)object).shouldEntityAppearGlowing((Entity)t);
    }

    @Override
    protected void extractAdditionalHitboxes(T t, ImmutableList.Builder<HitboxRenderState> builder, float f) {
        AABB aABB = ((Entity)t).getBoundingBox();
        float f2 = 0.01f;
        HitboxRenderState hitboxRenderState = new HitboxRenderState(aABB.minX - ((Entity)t).getX(), ((Entity)t).getEyeHeight() - 0.01f, aABB.minZ - ((Entity)t).getZ(), aABB.maxX - ((Entity)t).getX(), ((Entity)t).getEyeHeight() + 0.01f, aABB.maxZ - ((Entity)t).getZ(), 1.0f, 0.0f, 0.0f);
        builder.add((Object)hitboxRenderState);
    }

    private static float solveBodyRot(LivingEntity livingEntity, float f, float f2) {
        Entity entity = livingEntity.getVehicle();
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity2 = (LivingEntity)entity;
            float f3 = Mth.rotLerp(f2, livingEntity2.yBodyRotO, livingEntity2.yBodyRot);
            float f4 = 85.0f;
            float f5 = Mth.clamp(Mth.wrapDegrees(f - f3), -85.0f, 85.0f);
            f3 = f - f5;
            if (Math.abs(f5) > 50.0f) {
                f3 += f5 * 0.2f;
            }
            return f3;
        }
        return Mth.rotLerp(f2, livingEntity.yBodyRotO, livingEntity.yBodyRot);
    }

    @Override
    protected /* synthetic */ float getShadowRadius(EntityRenderState entityRenderState) {
        return this.getShadowRadius((S)((LivingEntityRenderState)entityRenderState));
    }
}

