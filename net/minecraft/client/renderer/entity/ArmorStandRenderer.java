/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import javax.annotation.Nullable;
import net.minecraft.client.model.ArmorStandArmorModel;
import net.minecraft.client.model.ArmorStandModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.joml.Quaternionfc;

public class ArmorStandRenderer
extends LivingEntityRenderer<ArmorStand, ArmorStandRenderState, ArmorStandArmorModel> {
    public static final ResourceLocation DEFAULT_SKIN_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/armorstand/wood.png");
    private final ArmorStandArmorModel bigModel = (ArmorStandArmorModel)this.getModel();
    private final ArmorStandArmorModel smallModel;

    public ArmorStandRenderer(EntityRendererProvider.Context context) {
        super(context, new ArmorStandModel(context.bakeLayer(ModelLayers.ARMOR_STAND)), 0.0f);
        this.smallModel = new ArmorStandModel(context.bakeLayer(ModelLayers.ARMOR_STAND_SMALL));
        this.addLayer(new HumanoidArmorLayer<ArmorStandRenderState, ArmorStandArmorModel, ArmorStandArmorModel>(this, new ArmorStandArmorModel(context.bakeLayer(ModelLayers.ARMOR_STAND_INNER_ARMOR)), new ArmorStandArmorModel(context.bakeLayer(ModelLayers.ARMOR_STAND_OUTER_ARMOR)), new ArmorStandArmorModel(context.bakeLayer(ModelLayers.ARMOR_STAND_SMALL_INNER_ARMOR)), new ArmorStandArmorModel(context.bakeLayer(ModelLayers.ARMOR_STAND_SMALL_OUTER_ARMOR)), context.getEquipmentRenderer()));
        this.addLayer(new ItemInHandLayer<ArmorStandRenderState, ArmorStandArmorModel>(this));
        this.addLayer(new WingsLayer<ArmorStandRenderState, ArmorStandArmorModel>(this, context.getModelSet(), context.getEquipmentRenderer()));
        this.addLayer(new CustomHeadLayer<ArmorStandRenderState, ArmorStandArmorModel>(this, context.getModelSet()));
    }

    @Override
    public ResourceLocation getTextureLocation(ArmorStandRenderState armorStandRenderState) {
        return DEFAULT_SKIN_LOCATION;
    }

    @Override
    public ArmorStandRenderState createRenderState() {
        return new ArmorStandRenderState();
    }

    @Override
    public void extractRenderState(ArmorStand armorStand, ArmorStandRenderState armorStandRenderState, float f) {
        super.extractRenderState(armorStand, armorStandRenderState, f);
        HumanoidMobRenderer.extractHumanoidRenderState(armorStand, armorStandRenderState, f, this.itemModelResolver);
        armorStandRenderState.yRot = Mth.rotLerp(f, armorStand.yRotO, armorStand.getYRot());
        armorStandRenderState.isMarker = armorStand.isMarker();
        armorStandRenderState.isSmall = armorStand.isSmall();
        armorStandRenderState.showArms = armorStand.showArms();
        armorStandRenderState.showBasePlate = armorStand.showBasePlate();
        armorStandRenderState.bodyPose = armorStand.getBodyPose();
        armorStandRenderState.headPose = armorStand.getHeadPose();
        armorStandRenderState.leftArmPose = armorStand.getLeftArmPose();
        armorStandRenderState.rightArmPose = armorStand.getRightArmPose();
        armorStandRenderState.leftLegPose = armorStand.getLeftLegPose();
        armorStandRenderState.rightLegPose = armorStand.getRightLegPose();
        armorStandRenderState.wiggle = (float)(armorStand.level().getGameTime() - armorStand.lastHit) + f;
    }

    @Override
    public void render(ArmorStandRenderState armorStandRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        this.model = armorStandRenderState.isSmall ? this.smallModel : this.bigModel;
        super.render(armorStandRenderState, poseStack, multiBufferSource, n);
    }

    @Override
    protected void setupRotations(ArmorStandRenderState armorStandRenderState, PoseStack poseStack, float f, float f2) {
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(180.0f - f));
        if (armorStandRenderState.wiggle < 5.0f) {
            poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(Mth.sin(armorStandRenderState.wiggle / 1.5f * (float)Math.PI) * 3.0f));
        }
    }

    @Override
    protected boolean shouldShowName(ArmorStand armorStand, double d) {
        return armorStand.isCustomNameVisible();
    }

    @Override
    @Nullable
    protected RenderType getRenderType(ArmorStandRenderState armorStandRenderState, boolean bl, boolean bl2, boolean bl3) {
        if (!armorStandRenderState.isMarker) {
            return super.getRenderType(armorStandRenderState, bl, bl2, bl3);
        }
        ResourceLocation resourceLocation = this.getTextureLocation(armorStandRenderState);
        if (bl2) {
            return RenderType.entityTranslucent(resourceLocation, false);
        }
        if (bl) {
            return RenderType.entityCutoutNoCull(resourceLocation, false);
        }
        return null;
    }

    @Override
    public /* synthetic */ ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((ArmorStandRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

