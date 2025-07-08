/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemClusterRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultClientData;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;

public class VaultRenderer
implements BlockEntityRenderer<VaultBlockEntity> {
    private final ItemModelResolver itemModelResolver;
    private final RandomSource random = RandomSource.create();
    private final ItemClusterRenderState renderState = new ItemClusterRenderState();

    public VaultRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public void render(VaultBlockEntity vaultBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2, Vec3 vec3) {
        if (!VaultBlockEntity.Client.shouldDisplayActiveEffects(vaultBlockEntity.getSharedData())) {
            return;
        }
        Level level = vaultBlockEntity.getLevel();
        if (level == null) {
            return;
        }
        ItemStack itemStack = vaultBlockEntity.getSharedData().getDisplayItem();
        if (itemStack.isEmpty()) {
            return;
        }
        this.itemModelResolver.updateForTopItem(this.renderState.item, itemStack, ItemDisplayContext.GROUND, level, null, 0);
        this.renderState.count = ItemClusterRenderState.getRenderedAmount(itemStack.getCount());
        this.renderState.seed = ItemClusterRenderState.getSeedForItemStack(itemStack);
        VaultClientData vaultClientData = vaultBlockEntity.getClientData();
        poseStack.pushPose();
        poseStack.translate(0.5f, 0.4f, 0.5f);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(Mth.rotLerp(f, vaultClientData.previousSpin(), vaultClientData.currentSpin())));
        ItemEntityRenderer.renderMultipleFromCount(poseStack, multiBufferSource, n, this.renderState, this.random);
        poseStack.popPose();
    }
}

