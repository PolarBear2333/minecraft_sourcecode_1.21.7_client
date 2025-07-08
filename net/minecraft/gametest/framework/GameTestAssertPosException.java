/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.gametest.framework;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.network.chat.Component;

public class GameTestAssertPosException
extends GameTestAssertException {
    private final BlockPos absolutePos;
    private final BlockPos relativePos;

    public GameTestAssertPosException(Component component, BlockPos blockPos, BlockPos blockPos2, int n) {
        super(component, n);
        this.absolutePos = blockPos;
        this.relativePos = blockPos2;
    }

    @Override
    public Component getDescription() {
        return Component.translatable("test.error.position", this.message, this.absolutePos.getX(), this.absolutePos.getY(), this.absolutePos.getZ(), this.relativePos.getX(), this.relativePos.getY(), this.relativePos.getZ(), this.tick);
    }

    @Nullable
    public String getMessageToShowAtBlock() {
        return super.getMessage();
    }

    @Nullable
    public BlockPos getRelativePos() {
        return this.relativePos;
    }

    @Nullable
    public BlockPos getAbsolutePos() {
        return this.absolutePos;
    }
}

