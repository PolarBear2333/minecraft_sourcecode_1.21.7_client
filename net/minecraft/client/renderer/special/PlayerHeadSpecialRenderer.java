/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  javax.annotation.Nullable
 *  org.joml.Vector3f
 */
package net.minecraft.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.SkullBlock;
import org.joml.Vector3f;

public class PlayerHeadSpecialRenderer
implements SpecialModelRenderer<PlayerHeadRenderInfo> {
    private final Map<ResolvableProfile, PlayerHeadRenderInfo> updatedResolvableProfiles = new HashMap<ResolvableProfile, PlayerHeadRenderInfo>();
    private final SkinManager skinManager;
    private final SkullModelBase modelBase;
    private final PlayerHeadRenderInfo defaultPlayerHeadRenderInfo;

    PlayerHeadSpecialRenderer(SkinManager skinManager, SkullModelBase skullModelBase, PlayerHeadRenderInfo playerHeadRenderInfo) {
        this.skinManager = skinManager;
        this.modelBase = skullModelBase;
        this.defaultPlayerHeadRenderInfo = playerHeadRenderInfo;
    }

    @Override
    public void render(@Nullable PlayerHeadRenderInfo playerHeadRenderInfo, ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2, boolean bl) {
        PlayerHeadRenderInfo playerHeadRenderInfo2 = Objects.requireNonNullElse(playerHeadRenderInfo, this.defaultPlayerHeadRenderInfo);
        RenderType renderType = playerHeadRenderInfo2.renderType();
        SkullBlockRenderer.renderSkull(null, 180.0f, 0.0f, poseStack, multiBufferSource, n, this.modelBase, renderType);
    }

    @Override
    public void getExtents(Set<Vector3f> set) {
        PoseStack poseStack = new PoseStack();
        poseStack.translate(0.5f, 0.0f, 0.5f);
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        this.modelBase.root().getExtentsForGui(poseStack, set);
    }

    @Override
    @Nullable
    public PlayerHeadRenderInfo extractArgument(ItemStack itemStack) {
        ResolvableProfile resolvableProfile = itemStack.get(DataComponents.PROFILE);
        if (resolvableProfile == null) {
            return null;
        }
        PlayerHeadRenderInfo playerHeadRenderInfo = this.updatedResolvableProfiles.get(resolvableProfile);
        if (playerHeadRenderInfo != null) {
            return playerHeadRenderInfo;
        }
        ResolvableProfile resolvableProfile2 = resolvableProfile.pollResolve();
        if (resolvableProfile2 != null) {
            return this.createAndCacheIfTextureIsUnpacked(resolvableProfile2);
        }
        return null;
    }

    @Nullable
    private PlayerHeadRenderInfo createAndCacheIfTextureIsUnpacked(ResolvableProfile resolvableProfile) {
        PlayerSkin playerSkin = this.skinManager.getInsecureSkin(resolvableProfile.gameProfile(), null);
        if (playerSkin != null) {
            PlayerHeadRenderInfo playerHeadRenderInfo = PlayerHeadRenderInfo.create(playerSkin);
            this.updatedResolvableProfiles.put(resolvableProfile, playerHeadRenderInfo);
            return playerHeadRenderInfo;
        }
        return null;
    }

    @Override
    @Nullable
    public /* synthetic */ Object extractArgument(ItemStack itemStack) {
        return this.extractArgument(itemStack);
    }

    public record PlayerHeadRenderInfo(RenderType renderType) {
        static PlayerHeadRenderInfo create(PlayerSkin playerSkin) {
            return new PlayerHeadRenderInfo(SkullBlockRenderer.getPlayerSkinRenderType(playerSkin.texture()));
        }
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked
    {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(Unbaked::new);

        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        @Nullable
        public SpecialModelRenderer<?> bake(EntityModelSet entityModelSet) {
            SkullModelBase skullModelBase = SkullBlockRenderer.createModel(entityModelSet, SkullBlock.Types.PLAYER);
            if (skullModelBase == null) {
                return null;
            }
            return new PlayerHeadSpecialRenderer(Minecraft.getInstance().getSkinManager(), skullModelBase, PlayerHeadRenderInfo.create(DefaultPlayerSkin.getDefaultSkin()));
        }
    }
}

