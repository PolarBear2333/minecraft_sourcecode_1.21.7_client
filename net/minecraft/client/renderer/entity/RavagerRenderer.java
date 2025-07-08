/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.RavagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.RavagerRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Ravager;

public class RavagerRenderer
extends MobRenderer<Ravager, RavagerRenderState, RavagerModel> {
    private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/illager/ravager.png");

    public RavagerRenderer(EntityRendererProvider.Context context) {
        super(context, new RavagerModel(context.bakeLayer(ModelLayers.RAVAGER)), 1.1f);
    }

    @Override
    public ResourceLocation getTextureLocation(RavagerRenderState ravagerRenderState) {
        return TEXTURE_LOCATION;
    }

    @Override
    public RavagerRenderState createRenderState() {
        return new RavagerRenderState();
    }

    @Override
    public void extractRenderState(Ravager ravager, RavagerRenderState ravagerRenderState, float f) {
        super.extractRenderState(ravager, ravagerRenderState, f);
        ravagerRenderState.stunnedTicksRemaining = (float)ravager.getStunnedTick() > 0.0f ? (float)ravager.getStunnedTick() - f : 0.0f;
        ravagerRenderState.attackTicksRemaining = (float)ravager.getAttackTick() > 0.0f ? (float)ravager.getAttackTick() - f : 0.0f;
        ravagerRenderState.roarAnimation = ravager.getRoarTick() > 0 ? ((float)(20 - ravager.getRoarTick()) + f) / 20.0f : 0.0f;
    }

    @Override
    public /* synthetic */ ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((RavagerRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

