/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 */
package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.MushroomCowMushroomLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.MushroomCowRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.MushroomCow;

public class MushroomCowRenderer
extends AgeableMobRenderer<MushroomCow, MushroomCowRenderState, CowModel> {
    private static final Map<MushroomCow.Variant, ResourceLocation> TEXTURES = Util.make(Maps.newHashMap(), hashMap -> {
        hashMap.put(MushroomCow.Variant.BROWN, ResourceLocation.withDefaultNamespace("textures/entity/cow/brown_mooshroom.png"));
        hashMap.put(MushroomCow.Variant.RED, ResourceLocation.withDefaultNamespace("textures/entity/cow/red_mooshroom.png"));
    });

    public MushroomCowRenderer(EntityRendererProvider.Context context) {
        super(context, new CowModel(context.bakeLayer(ModelLayers.MOOSHROOM)), new CowModel(context.bakeLayer(ModelLayers.MOOSHROOM_BABY)), 0.7f);
        this.addLayer(new MushroomCowMushroomLayer(this, context.getBlockRenderDispatcher()));
    }

    @Override
    public ResourceLocation getTextureLocation(MushroomCowRenderState mushroomCowRenderState) {
        return TEXTURES.get(mushroomCowRenderState.variant);
    }

    @Override
    public MushroomCowRenderState createRenderState() {
        return new MushroomCowRenderState();
    }

    @Override
    public void extractRenderState(MushroomCow mushroomCow, MushroomCowRenderState mushroomCowRenderState, float f) {
        super.extractRenderState(mushroomCow, mushroomCowRenderState, f);
        mushroomCowRenderState.variant = mushroomCow.getVariant();
    }

    @Override
    public /* synthetic */ ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((MushroomCowRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

