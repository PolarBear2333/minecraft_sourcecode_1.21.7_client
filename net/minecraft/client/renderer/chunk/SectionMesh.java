/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.chunk;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.chunk.SectionBuffers;
import net.minecraft.client.renderer.chunk.TranslucencyPointOfView;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface SectionMesh
extends AutoCloseable {
    default public boolean isDifferentPointOfView(TranslucencyPointOfView translucencyPointOfView) {
        return false;
    }

    default public boolean hasRenderableLayers() {
        return false;
    }

    default public boolean hasTranslucentGeometry() {
        return false;
    }

    default public boolean isEmpty(ChunkSectionLayer chunkSectionLayer) {
        return true;
    }

    default public List<BlockEntity> getRenderableBlockEntities() {
        return Collections.emptyList();
    }

    public boolean facesCanSeeEachother(Direction var1, Direction var2);

    @Nullable
    default public SectionBuffers getBuffers(ChunkSectionLayer chunkSectionLayer) {
        return null;
    }

    @Override
    default public void close() {
    }
}

