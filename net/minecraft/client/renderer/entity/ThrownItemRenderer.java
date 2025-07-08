/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.ThrownItemRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Quaternionfc;

public class ThrownItemRenderer<T extends Entity>
extends EntityRenderer<T, ThrownItemRenderState> {
    private final ItemModelResolver itemModelResolver;
    private final float scale;
    private final boolean fullBright;

    public ThrownItemRenderer(EntityRendererProvider.Context context, float f, boolean bl) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
        this.scale = f;
        this.fullBright = bl;
    }

    public ThrownItemRenderer(EntityRendererProvider.Context context) {
        this(context, 1.0f, false);
    }

    @Override
    protected int getBlockLightLevel(T t, BlockPos blockPos) {
        return this.fullBright ? 15 : super.getBlockLightLevel(t, blockPos);
    }

    @Override
    public void render(ThrownItemRenderState thrownItemRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        poseStack.pushPose();
        poseStack.scale(this.scale, this.scale, this.scale);
        poseStack.mulPose((Quaternionfc)this.entityRenderDispatcher.cameraOrientation());
        thrownItemRenderState.item.render(poseStack, multiBufferSource, n, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(thrownItemRenderState, poseStack, multiBufferSource, n);
    }

    @Override
    public ThrownItemRenderState createRenderState() {
        return new ThrownItemRenderState();
    }

    @Override
    public void extractRenderState(T t, ThrownItemRenderState thrownItemRenderState, float f) {
        super.extractRenderState(t, thrownItemRenderState, f);
        this.itemModelResolver.updateForNonLiving(thrownItemRenderState.item, ((ItemSupplier)t).getItem(), ItemDisplayContext.GROUND, (Entity)t);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

