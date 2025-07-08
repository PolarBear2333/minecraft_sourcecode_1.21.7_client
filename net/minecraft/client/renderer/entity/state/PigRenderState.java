/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.entity.state;

import javax.annotation.Nullable;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.animal.PigVariant;
import net.minecraft.world.item.ItemStack;

public class PigRenderState
extends LivingEntityRenderState {
    public ItemStack saddle = ItemStack.EMPTY;
    @Nullable
    public PigVariant variant;
}

