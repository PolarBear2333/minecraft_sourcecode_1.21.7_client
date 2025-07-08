/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public interface NoDataSpecialModelRenderer
extends SpecialModelRenderer<Void> {
    @Override
    @Nullable
    default public Void extractArgument(ItemStack itemStack) {
        return null;
    }

    @Override
    default public void render(@Nullable Void void_, ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2, boolean bl) {
        this.render(itemDisplayContext, poseStack, multiBufferSource, n, n2, bl);
    }

    public void render(ItemDisplayContext var1, PoseStack var2, MultiBufferSource var3, int var4, int var5, boolean var6);

    @Override
    @Nullable
    default public /* synthetic */ Object extractArgument(ItemStack itemStack) {
        return this.extractArgument(itemStack);
    }
}

