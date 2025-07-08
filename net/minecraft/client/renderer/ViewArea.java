/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;

public class ViewArea {
    protected final LevelRenderer levelRenderer;
    protected final Level level;
    protected int sectionGridSizeY;
    protected int sectionGridSizeX;
    protected int sectionGridSizeZ;
    private int viewDistance;
    private SectionPos cameraSectionPos;
    public SectionRenderDispatcher.RenderSection[] sections;

    public ViewArea(SectionRenderDispatcher sectionRenderDispatcher, Level level, int n, LevelRenderer levelRenderer) {
        this.levelRenderer = levelRenderer;
        this.level = level;
        this.setViewDistance(n);
        this.createSections(sectionRenderDispatcher);
        this.cameraSectionPos = SectionPos.of(this.viewDistance + 1, 0, this.viewDistance + 1);
    }

    protected void createSections(SectionRenderDispatcher sectionRenderDispatcher) {
        if (!Minecraft.getInstance().isSameThread()) {
            throw new IllegalStateException("createSections called from wrong thread: " + Thread.currentThread().getName());
        }
        int n = this.sectionGridSizeX * this.sectionGridSizeY * this.sectionGridSizeZ;
        this.sections = new SectionRenderDispatcher.RenderSection[n];
        for (int i = 0; i < this.sectionGridSizeX; ++i) {
            for (int j = 0; j < this.sectionGridSizeY; ++j) {
                for (int k = 0; k < this.sectionGridSizeZ; ++k) {
                    int n2 = this.getSectionIndex(i, j, k);
                    SectionRenderDispatcher sectionRenderDispatcher2 = sectionRenderDispatcher;
                    Objects.requireNonNull(sectionRenderDispatcher2);
                    this.sections[n2] = sectionRenderDispatcher2.new SectionRenderDispatcher.RenderSection(n2, SectionPos.asLong(i, j + this.level.getMinSectionY(), k));
                }
            }
        }
    }

    public void releaseAllBuffers() {
        for (SectionRenderDispatcher.RenderSection renderSection : this.sections) {
            renderSection.reset();
        }
    }

    private int getSectionIndex(int n, int n2, int n3) {
        return (n3 * this.sectionGridSizeY + n2) * this.sectionGridSizeX + n;
    }

    protected void setViewDistance(int n) {
        int n2;
        this.sectionGridSizeX = n2 = n * 2 + 1;
        this.sectionGridSizeY = this.level.getSectionsCount();
        this.sectionGridSizeZ = n2;
        this.viewDistance = n;
    }

    public int getViewDistance() {
        return this.viewDistance;
    }

    public LevelHeightAccessor getLevelHeightAccessor() {
        return this.level;
    }

    public void repositionCamera(SectionPos sectionPos) {
        for (int i = 0; i < this.sectionGridSizeX; ++i) {
            int n = sectionPos.x() - this.viewDistance;
            int n2 = n + Math.floorMod(i - n, this.sectionGridSizeX);
            for (int j = 0; j < this.sectionGridSizeZ; ++j) {
                int n3 = sectionPos.z() - this.viewDistance;
                int n4 = n3 + Math.floorMod(j - n3, this.sectionGridSizeZ);
                for (int k = 0; k < this.sectionGridSizeY; ++k) {
                    int n5 = this.level.getMinSectionY() + k;
                    SectionRenderDispatcher.RenderSection renderSection = this.sections[this.getSectionIndex(i, k, j)];
                    long l = renderSection.getSectionNode();
                    if (l == SectionPos.asLong(n2, n5, n4)) continue;
                    renderSection.setSectionNode(SectionPos.asLong(n2, n5, n4));
                }
            }
        }
        this.cameraSectionPos = sectionPos;
        this.levelRenderer.getSectionOcclusionGraph().invalidate();
    }

    public SectionPos getCameraSectionPos() {
        return this.cameraSectionPos;
    }

    public void setDirty(int n, int n2, int n3, boolean bl) {
        SectionRenderDispatcher.RenderSection renderSection = this.getRenderSection(n, n2, n3);
        if (renderSection != null) {
            renderSection.setDirty(bl);
        }
    }

    @Nullable
    protected SectionRenderDispatcher.RenderSection getRenderSectionAt(BlockPos blockPos) {
        return this.getRenderSection(SectionPos.asLong(blockPos));
    }

    @Nullable
    protected SectionRenderDispatcher.RenderSection getRenderSection(long l) {
        int n = SectionPos.x(l);
        int n2 = SectionPos.y(l);
        int n3 = SectionPos.z(l);
        return this.getRenderSection(n, n2, n3);
    }

    @Nullable
    private SectionRenderDispatcher.RenderSection getRenderSection(int n, int n2, int n3) {
        if (!this.containsSection(n, n2, n3)) {
            return null;
        }
        int n4 = n2 - this.level.getMinSectionY();
        int n5 = Math.floorMod(n, this.sectionGridSizeX);
        int n6 = Math.floorMod(n3, this.sectionGridSizeZ);
        return this.sections[this.getSectionIndex(n5, n4, n6)];
    }

    private boolean containsSection(int n, int n2, int n3) {
        if (n2 < this.level.getMinSectionY() || n2 > this.level.getMaxSectionY()) {
            return false;
        }
        if (n < this.cameraSectionPos.x() - this.viewDistance || n > this.cameraSectionPos.x() + this.viewDistance) {
            return false;
        }
        return n3 >= this.cameraSectionPos.z() - this.viewDistance && n3 <= this.cameraSectionPos.z() + this.viewDistance;
    }
}

