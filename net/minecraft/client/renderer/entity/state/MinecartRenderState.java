/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.entity.state;

import javax.annotation.Nullable;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class MinecartRenderState
extends EntityRenderState {
    public float xRot;
    public float yRot;
    public long offsetSeed;
    public int hurtDir;
    public float hurtTime;
    public float damageTime;
    public int displayOffset;
    public BlockState displayBlockState = Blocks.AIR.defaultBlockState();
    public boolean isNewRender;
    @Nullable
    public Vec3 renderPos;
    @Nullable
    public Vec3 posOnRail;
    @Nullable
    public Vec3 frontPos;
    @Nullable
    public Vec3 backPos;
}

