/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.client.model.ShulkerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.ShulkerRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;

public class ShulkerRenderer
extends MobRenderer<Shulker, ShulkerRenderState, ShulkerModel> {
    private static final ResourceLocation DEFAULT_TEXTURE_LOCATION = Sheets.DEFAULT_SHULKER_TEXTURE_LOCATION.texture().withPath(string -> "textures/" + string + ".png");
    private static final ResourceLocation[] TEXTURE_LOCATION = (ResourceLocation[])Sheets.SHULKER_TEXTURE_LOCATION.stream().map(material -> material.texture().withPath(string -> "textures/" + string + ".png")).toArray(ResourceLocation[]::new);

    public ShulkerRenderer(EntityRendererProvider.Context context) {
        super(context, new ShulkerModel(context.bakeLayer(ModelLayers.SHULKER)), 0.0f);
    }

    @Override
    public Vec3 getRenderOffset(ShulkerRenderState shulkerRenderState) {
        return shulkerRenderState.renderOffset;
    }

    @Override
    public boolean shouldRender(Shulker shulker, Frustum frustum, double d, double d2, double d3) {
        if (super.shouldRender(shulker, frustum, d, d2, d3)) {
            return true;
        }
        Vec3 vec3 = shulker.getRenderPosition(0.0f);
        if (vec3 == null) {
            return false;
        }
        EntityType<?> entityType = shulker.getType();
        float f = entityType.getHeight() / 2.0f;
        float f2 = entityType.getWidth() / 2.0f;
        Vec3 vec32 = Vec3.atBottomCenterOf(shulker.blockPosition());
        return frustum.isVisible(new AABB(vec3.x, vec3.y + (double)f, vec3.z, vec32.x, vec32.y + (double)f, vec32.z).inflate(f2, f, f2));
    }

    @Override
    public ResourceLocation getTextureLocation(ShulkerRenderState shulkerRenderState) {
        return ShulkerRenderer.getTextureLocation(shulkerRenderState.color);
    }

    @Override
    public ShulkerRenderState createRenderState() {
        return new ShulkerRenderState();
    }

    @Override
    public void extractRenderState(Shulker shulker, ShulkerRenderState shulkerRenderState, float f) {
        super.extractRenderState(shulker, shulkerRenderState, f);
        shulkerRenderState.renderOffset = Objects.requireNonNullElse(shulker.getRenderPosition(f), Vec3.ZERO);
        shulkerRenderState.color = shulker.getColor();
        shulkerRenderState.peekAmount = shulker.getClientPeekAmount(f);
        shulkerRenderState.yHeadRot = shulker.yHeadRot;
        shulkerRenderState.yBodyRot = shulker.yBodyRot;
        shulkerRenderState.attachFace = shulker.getAttachFace();
    }

    public static ResourceLocation getTextureLocation(@Nullable DyeColor dyeColor) {
        if (dyeColor == null) {
            return DEFAULT_TEXTURE_LOCATION;
        }
        return TEXTURE_LOCATION[dyeColor.getId()];
    }

    @Override
    protected void setupRotations(ShulkerRenderState shulkerRenderState, PoseStack poseStack, float f, float f2) {
        super.setupRotations(shulkerRenderState, poseStack, f + 180.0f, f2);
        poseStack.rotateAround((Quaternionfc)shulkerRenderState.attachFace.getOpposite().getRotation(), 0.0f, 0.5f, 0.0f);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

