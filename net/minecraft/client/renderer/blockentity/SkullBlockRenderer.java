/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.blockentity;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PiglinHeadModel;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.dragon.DragonHeadModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.phys.Vec3;

public class SkullBlockRenderer
implements BlockEntityRenderer<SkullBlockEntity> {
    private final Function<SkullBlock.Type, SkullModelBase> modelByType;
    private static final Map<SkullBlock.Type, ResourceLocation> SKIN_BY_TYPE = Util.make(Maps.newHashMap(), hashMap -> {
        hashMap.put(SkullBlock.Types.SKELETON, ResourceLocation.withDefaultNamespace("textures/entity/skeleton/skeleton.png"));
        hashMap.put(SkullBlock.Types.WITHER_SKELETON, ResourceLocation.withDefaultNamespace("textures/entity/skeleton/wither_skeleton.png"));
        hashMap.put(SkullBlock.Types.ZOMBIE, ResourceLocation.withDefaultNamespace("textures/entity/zombie/zombie.png"));
        hashMap.put(SkullBlock.Types.CREEPER, ResourceLocation.withDefaultNamespace("textures/entity/creeper/creeper.png"));
        hashMap.put(SkullBlock.Types.DRAGON, ResourceLocation.withDefaultNamespace("textures/entity/enderdragon/dragon.png"));
        hashMap.put(SkullBlock.Types.PIGLIN, ResourceLocation.withDefaultNamespace("textures/entity/piglin/piglin.png"));
        hashMap.put(SkullBlock.Types.PLAYER, DefaultPlayerSkin.getDefaultTexture());
    });

    @Nullable
    public static SkullModelBase createModel(EntityModelSet entityModelSet, SkullBlock.Type type) {
        if (type instanceof SkullBlock.Types) {
            SkullBlock.Types types = (SkullBlock.Types)type;
            return switch (types) {
                default -> throw new MatchException(null, null);
                case SkullBlock.Types.SKELETON -> new SkullModel(entityModelSet.bakeLayer(ModelLayers.SKELETON_SKULL));
                case SkullBlock.Types.WITHER_SKELETON -> new SkullModel(entityModelSet.bakeLayer(ModelLayers.WITHER_SKELETON_SKULL));
                case SkullBlock.Types.PLAYER -> new SkullModel(entityModelSet.bakeLayer(ModelLayers.PLAYER_HEAD));
                case SkullBlock.Types.ZOMBIE -> new SkullModel(entityModelSet.bakeLayer(ModelLayers.ZOMBIE_HEAD));
                case SkullBlock.Types.CREEPER -> new SkullModel(entityModelSet.bakeLayer(ModelLayers.CREEPER_HEAD));
                case SkullBlock.Types.DRAGON -> new DragonHeadModel(entityModelSet.bakeLayer(ModelLayers.DRAGON_SKULL));
                case SkullBlock.Types.PIGLIN -> new PiglinHeadModel(entityModelSet.bakeLayer(ModelLayers.PIGLIN_HEAD));
            };
        }
        return null;
    }

    public SkullBlockRenderer(BlockEntityRendererProvider.Context context) {
        EntityModelSet entityModelSet = context.getModelSet();
        this.modelByType = Util.memoize(type -> SkullBlockRenderer.createModel(entityModelSet, type));
    }

    @Override
    public void render(SkullBlockEntity skullBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2, Vec3 vec3) {
        float f2 = skullBlockEntity.getAnimation(f);
        BlockState blockState = skullBlockEntity.getBlockState();
        boolean bl = blockState.getBlock() instanceof WallSkullBlock;
        Direction direction = bl ? blockState.getValue(WallSkullBlock.FACING) : null;
        int n3 = bl ? RotationSegment.convertToSegment(direction.getOpposite()) : blockState.getValue(SkullBlock.ROTATION);
        float f3 = RotationSegment.convertToDegrees(n3);
        SkullBlock.Type type = ((AbstractSkullBlock)blockState.getBlock()).getType();
        SkullModelBase skullModelBase = this.modelByType.apply(type);
        RenderType renderType = SkullBlockRenderer.getRenderType(type, skullBlockEntity.getOwnerProfile());
        SkullBlockRenderer.renderSkull(direction, f3, f2, poseStack, multiBufferSource, n, skullModelBase, renderType);
    }

    public static void renderSkull(@Nullable Direction direction, float f, float f2, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, SkullModelBase skullModelBase, RenderType renderType) {
        poseStack.pushPose();
        if (direction == null) {
            poseStack.translate(0.5f, 0.0f, 0.5f);
        } else {
            float f3 = 0.25f;
            poseStack.translate(0.5f - (float)direction.getStepX() * 0.25f, 0.25f, 0.5f - (float)direction.getStepZ() * 0.25f);
        }
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(renderType);
        skullModelBase.setupAnim(f2, f, 0.0f);
        skullModelBase.renderToBuffer(poseStack, vertexConsumer, n, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    public static RenderType getRenderType(SkullBlock.Type type, @Nullable ResolvableProfile resolvableProfile) {
        if (type != SkullBlock.Types.PLAYER || resolvableProfile == null) {
            return SkullBlockRenderer.getSkullRenderType(type, null);
        }
        return SkullBlockRenderer.getPlayerSkinRenderType(Minecraft.getInstance().getSkinManager().getInsecureSkin(resolvableProfile.gameProfile()).texture());
    }

    public static RenderType getSkullRenderType(SkullBlock.Type type, @Nullable ResourceLocation resourceLocation) {
        return RenderType.entityCutoutNoCullZOffset(resourceLocation != null ? resourceLocation : SKIN_BY_TYPE.get(type));
    }

    public static RenderType getPlayerSkinRenderType(ResourceLocation resourceLocation) {
        return RenderType.entityTranslucent(resourceLocation);
    }
}

