/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 */
package net.minecraft.client.renderer.chunk;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.client.renderer.chunk.SectionCopy;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

public class RenderRegionCache {
    private final Long2ObjectMap<SectionCopy> sectionCopyCache = new Long2ObjectOpenHashMap();

    public RenderSectionRegion createRegion(Level level, long l) {
        int n = SectionPos.x(l);
        int n2 = SectionPos.y(l);
        int n3 = SectionPos.z(l);
        int n4 = n - 1;
        int n5 = n2 - 1;
        int n6 = n3 - 1;
        int n7 = n + 1;
        int n8 = n2 + 1;
        int n9 = n3 + 1;
        SectionCopy[] sectionCopyArray = new SectionCopy[27];
        for (int i = n6; i <= n9; ++i) {
            for (int j = n5; j <= n8; ++j) {
                for (int k = n4; k <= n7; ++k) {
                    int n10 = RenderSectionRegion.index(n4, n5, n6, k, j, i);
                    sectionCopyArray[n10] = this.getSectionDataCopy(level, k, j, i);
                }
            }
        }
        return new RenderSectionRegion(level, n4, n5, n6, sectionCopyArray);
    }

    private SectionCopy getSectionDataCopy(Level level, int n, int n2, int n3) {
        return (SectionCopy)this.sectionCopyCache.computeIfAbsent(SectionPos.asLong(n, n2, n3), l -> {
            LevelChunk levelChunk = level.getChunk(n, n3);
            return new SectionCopy(levelChunk, levelChunk.getSectionIndexFromSectionY(n2));
        });
    }
}

