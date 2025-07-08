/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.DrownedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.DrownedOuterLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Quaternionfc;

public class DrownedRenderer
extends AbstractZombieRenderer<Drowned, ZombieRenderState, DrownedModel> {
    private static final ResourceLocation DROWNED_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/zombie/drowned.png");

    public DrownedRenderer(EntityRendererProvider.Context context) {
        super(context, new DrownedModel(context.bakeLayer(ModelLayers.DROWNED)), new DrownedModel(context.bakeLayer(ModelLayers.DROWNED_BABY)), new DrownedModel(context.bakeLayer(ModelLayers.DROWNED_INNER_ARMOR)), new DrownedModel(context.bakeLayer(ModelLayers.DROWNED_OUTER_ARMOR)), new DrownedModel(context.bakeLayer(ModelLayers.DROWNED_BABY_INNER_ARMOR)), new DrownedModel(context.bakeLayer(ModelLayers.DROWNED_BABY_OUTER_ARMOR)));
        this.addLayer(new DrownedOuterLayer(this, context.getModelSet()));
    }

    @Override
    public ZombieRenderState createRenderState() {
        return new ZombieRenderState();
    }

    @Override
    public ResourceLocation getTextureLocation(ZombieRenderState zombieRenderState) {
        return DROWNED_LOCATION;
    }

    @Override
    protected void setupRotations(ZombieRenderState zombieRenderState, PoseStack poseStack, float f, float f2) {
        super.setupRotations(zombieRenderState, poseStack, f, f2);
        float f3 = zombieRenderState.swimAmount;
        if (f3 > 0.0f) {
            float f4 = -10.0f - zombieRenderState.xRot;
            float f5 = Mth.lerp(f3, 0.0f, f4);
            poseStack.rotateAround((Quaternionfc)Axis.XP.rotationDegrees(f5), 0.0f, zombieRenderState.boundingBoxHeight / 2.0f / f2, 0.0f);
        }
    }

    @Override
    protected HumanoidModel.ArmPose getArmPose(Drowned drowned, HumanoidArm humanoidArm) {
        ItemStack itemStack = drowned.getItemHeldByArm(humanoidArm);
        if (drowned.getMainArm() == humanoidArm && drowned.isAggressive() && itemStack.is(Items.TRIDENT)) {
            return HumanoidModel.ArmPose.THROW_SPEAR;
        }
        return HumanoidModel.ArmPose.EMPTY;
    }

    @Override
    public /* synthetic */ ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((ZombieRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

