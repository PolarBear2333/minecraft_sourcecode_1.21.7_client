/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ParrotModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.ParrotRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Parrot;

public class ParrotRenderer
extends MobRenderer<Parrot, ParrotRenderState, ParrotModel> {
    private static final ResourceLocation RED_BLUE = ResourceLocation.withDefaultNamespace("textures/entity/parrot/parrot_red_blue.png");
    private static final ResourceLocation BLUE = ResourceLocation.withDefaultNamespace("textures/entity/parrot/parrot_blue.png");
    private static final ResourceLocation GREEN = ResourceLocation.withDefaultNamespace("textures/entity/parrot/parrot_green.png");
    private static final ResourceLocation YELLOW_BLUE = ResourceLocation.withDefaultNamespace("textures/entity/parrot/parrot_yellow_blue.png");
    private static final ResourceLocation GREY = ResourceLocation.withDefaultNamespace("textures/entity/parrot/parrot_grey.png");

    public ParrotRenderer(EntityRendererProvider.Context context) {
        super(context, new ParrotModel(context.bakeLayer(ModelLayers.PARROT)), 0.3f);
    }

    @Override
    public ResourceLocation getTextureLocation(ParrotRenderState parrotRenderState) {
        return ParrotRenderer.getVariantTexture(parrotRenderState.variant);
    }

    @Override
    public ParrotRenderState createRenderState() {
        return new ParrotRenderState();
    }

    @Override
    public void extractRenderState(Parrot parrot, ParrotRenderState parrotRenderState, float f) {
        super.extractRenderState(parrot, parrotRenderState, f);
        parrotRenderState.variant = parrot.getVariant();
        float f2 = Mth.lerp(f, parrot.oFlap, parrot.flap);
        float f3 = Mth.lerp(f, parrot.oFlapSpeed, parrot.flapSpeed);
        parrotRenderState.flapAngle = (Mth.sin(f2) + 1.0f) * f3;
        parrotRenderState.pose = ParrotModel.getPose(parrot);
    }

    public static ResourceLocation getVariantTexture(Parrot.Variant variant) {
        return switch (variant) {
            default -> throw new MatchException(null, null);
            case Parrot.Variant.RED_BLUE -> RED_BLUE;
            case Parrot.Variant.BLUE -> BLUE;
            case Parrot.Variant.GREEN -> GREEN;
            case Parrot.Variant.YELLOW_BLUE -> YELLOW_BLUE;
            case Parrot.Variant.GRAY -> GREY;
        };
    }

    @Override
    public /* synthetic */ ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((ParrotRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

