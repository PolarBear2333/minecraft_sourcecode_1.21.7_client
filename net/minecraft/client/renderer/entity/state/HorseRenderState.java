/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.state;

import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.world.entity.animal.horse.Markings;
import net.minecraft.world.entity.animal.horse.Variant;
import net.minecraft.world.item.ItemStack;

public class HorseRenderState
extends EquineRenderState {
    public Variant variant = Variant.WHITE;
    public Markings markings = Markings.NONE;
    public ItemStack bodyArmorItem = ItemStack.EMPTY;
}

