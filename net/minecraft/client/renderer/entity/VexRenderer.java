/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.VexModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.VexRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Vex;

public class VexRenderer
extends MobRenderer<Vex, VexRenderState, VexModel> {
    private static final ResourceLocation VEX_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/illager/vex.png");
    private static final ResourceLocation VEX_CHARGING_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/illager/vex_charging.png");

    public VexRenderer(EntityRendererProvider.Context context) {
        super(context, new VexModel(context.bakeLayer(ModelLayers.VEX)), 0.3f);
        this.addLayer(new ItemInHandLayer<VexRenderState, VexModel>(this));
    }

    @Override
    protected int getBlockLightLevel(Vex vex, BlockPos blockPos) {
        return 15;
    }

    @Override
    public ResourceLocation getTextureLocation(VexRenderState vexRenderState) {
        if (vexRenderState.isCharging) {
            return VEX_CHARGING_LOCATION;
        }
        return VEX_LOCATION;
    }

    @Override
    public VexRenderState createRenderState() {
        return new VexRenderState();
    }

    @Override
    public void extractRenderState(Vex vex, VexRenderState vexRenderState, float f) {
        super.extractRenderState(vex, vexRenderState, f);
        ArmedEntityRenderState.extractArmedEntityRenderState(vex, vexRenderState, this.itemModelResolver);
        vexRenderState.isCharging = vex.isCharging();
    }

    @Override
    public /* synthetic */ ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((VexRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

