/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

public interface BoundingBoxRenderable {
    public Mode renderMode();

    public RenderableBox getRenderableBox();

    public static enum Mode {
        NONE,
        BOX,
        BOX_AND_INVISIBLE_BLOCKS;

    }

    public record RenderableBox(BlockPos localPos, Vec3i size) {
        public static RenderableBox fromCorners(int n, int n2, int n3, int n4, int n5, int n6) {
            int n7 = Math.min(n, n4);
            int n8 = Math.min(n2, n5);
            int n9 = Math.min(n3, n6);
            return new RenderableBox(new BlockPos(n7, n8, n9), new Vec3i(Math.max(n, n4) - n7, Math.max(n2, n5) - n8, Math.max(n3, n6) - n9));
        }
    }
}

