/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.phys.Vec3;

public class TheEndGatewayRenderer
extends TheEndPortalRenderer<TheEndGatewayBlockEntity> {
    private static final ResourceLocation BEAM_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/end_gateway_beam.png");

    public TheEndGatewayRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(TheEndGatewayBlockEntity theEndGatewayBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2, Vec3 vec3) {
        if (theEndGatewayBlockEntity.isSpawning() || theEndGatewayBlockEntity.isCoolingDown()) {
            float f2 = theEndGatewayBlockEntity.isSpawning() ? theEndGatewayBlockEntity.getSpawnPercent(f) : theEndGatewayBlockEntity.getCooldownPercent(f);
            double d = theEndGatewayBlockEntity.isSpawning() ? (double)theEndGatewayBlockEntity.getLevel().getMaxY() : 50.0;
            f2 = Mth.sin(f2 * (float)Math.PI);
            int n3 = Mth.floor((double)f2 * d);
            int n4 = theEndGatewayBlockEntity.isSpawning() ? DyeColor.MAGENTA.getTextureDiffuseColor() : DyeColor.PURPLE.getTextureDiffuseColor();
            long l = theEndGatewayBlockEntity.getLevel().getGameTime();
            BeaconRenderer.renderBeaconBeam(poseStack, multiBufferSource, BEAM_LOCATION, f, f2, l, -n3, n3 * 2, n4, 0.15f, 0.175f);
        }
        super.render(theEndGatewayBlockEntity, f, poseStack, multiBufferSource, n, n2, vec3);
    }

    @Override
    protected float getOffsetUp() {
        return 1.0f;
    }

    @Override
    protected float getOffsetDown() {
        return 0.0f;
    }

    @Override
    protected RenderType renderType() {
        return RenderType.endGateway();
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}

