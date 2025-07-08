/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.BeeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BeeRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Bee;

public class BeeRenderer
extends AgeableMobRenderer<Bee, BeeRenderState, BeeModel> {
    private static final ResourceLocation ANGRY_BEE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/bee/bee_angry.png");
    private static final ResourceLocation ANGRY_NECTAR_BEE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/bee/bee_angry_nectar.png");
    private static final ResourceLocation BEE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/bee/bee.png");
    private static final ResourceLocation NECTAR_BEE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/bee/bee_nectar.png");

    public BeeRenderer(EntityRendererProvider.Context context) {
        super(context, new BeeModel(context.bakeLayer(ModelLayers.BEE)), new BeeModel(context.bakeLayer(ModelLayers.BEE_BABY)), 0.4f);
    }

    @Override
    public ResourceLocation getTextureLocation(BeeRenderState beeRenderState) {
        if (beeRenderState.isAngry) {
            if (beeRenderState.hasNectar) {
                return ANGRY_NECTAR_BEE_TEXTURE;
            }
            return ANGRY_BEE_TEXTURE;
        }
        if (beeRenderState.hasNectar) {
            return NECTAR_BEE_TEXTURE;
        }
        return BEE_TEXTURE;
    }

    @Override
    public BeeRenderState createRenderState() {
        return new BeeRenderState();
    }

    @Override
    public void extractRenderState(Bee bee, BeeRenderState beeRenderState, float f) {
        super.extractRenderState(bee, beeRenderState, f);
        beeRenderState.rollAmount = bee.getRollAmount(f);
        beeRenderState.hasStinger = !bee.hasStung();
        beeRenderState.isOnGround = bee.onGround() && bee.getDeltaMovement().lengthSqr() < 1.0E-7;
        beeRenderState.isAngry = bee.isAngry();
        beeRenderState.hasNectar = bee.hasNectar();
    }

    @Override
    public /* synthetic */ ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((BeeRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

