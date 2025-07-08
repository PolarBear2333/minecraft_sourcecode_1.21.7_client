/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.entity.state;

import javax.annotation.Nullable;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.level.block.state.BlockState;

public class EndermanRenderState
extends HumanoidRenderState {
    public boolean isCreepy;
    @Nullable
    public BlockState carriedBlock;
}

