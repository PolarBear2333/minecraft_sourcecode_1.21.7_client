/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.state;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.item.ItemStack;

public class EquineRenderState
extends LivingEntityRenderState {
    public ItemStack saddle = ItemStack.EMPTY;
    public boolean isRidden;
    public boolean animateTail;
    public float eatAnimation;
    public float standAnimation;
    public float feedingAnimation;
}

