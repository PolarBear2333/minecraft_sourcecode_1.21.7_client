/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.WitchModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.WitchItemLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.HoldingEntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.WitchRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class WitchRenderer
extends MobRenderer<Witch, WitchRenderState, WitchModel> {
    private static final ResourceLocation WITCH_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/witch.png");

    public WitchRenderer(EntityRendererProvider.Context context) {
        super(context, new WitchModel(context.bakeLayer(ModelLayers.WITCH)), 0.5f);
        this.addLayer(new WitchItemLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(WitchRenderState witchRenderState) {
        return WITCH_LOCATION;
    }

    @Override
    public WitchRenderState createRenderState() {
        return new WitchRenderState();
    }

    @Override
    public void extractRenderState(Witch witch, WitchRenderState witchRenderState, float f) {
        super.extractRenderState(witch, witchRenderState, f);
        HoldingEntityRenderState.extractHoldingEntityRenderState(witch, witchRenderState, this.itemModelResolver);
        witchRenderState.entityId = witch.getId();
        ItemStack itemStack = witch.getMainHandItem();
        witchRenderState.isHoldingItem = !itemStack.isEmpty();
        witchRenderState.isHoldingPotion = itemStack.is(Items.POTION);
    }

    @Override
    public /* synthetic */ ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((WitchRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

