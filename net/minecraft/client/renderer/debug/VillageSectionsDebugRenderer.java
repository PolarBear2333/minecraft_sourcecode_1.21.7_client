/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 */
package net.minecraft.client.renderer.debug;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Set;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

public class VillageSectionsDebugRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private static final int MAX_RENDER_DIST_FOR_VILLAGE_SECTIONS = 60;
    private final Set<SectionPos> villageSections = Sets.newHashSet();

    VillageSectionsDebugRenderer() {
    }

    @Override
    public void clear() {
        this.villageSections.clear();
    }

    public void setVillageSection(SectionPos sectionPos) {
        this.villageSections.add(sectionPos);
    }

    public void setNotVillageSection(SectionPos sectionPos) {
        this.villageSections.remove(sectionPos);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double d2, double d3) {
        BlockPos blockPos = BlockPos.containing(d, d2, d3);
        this.villageSections.forEach(sectionPos -> {
            if (blockPos.closerThan(sectionPos.center(), 60.0)) {
                VillageSectionsDebugRenderer.highlightVillageSection(poseStack, multiBufferSource, sectionPos);
            }
        });
    }

    private static void highlightVillageSection(PoseStack poseStack, MultiBufferSource multiBufferSource, SectionPos sectionPos) {
        DebugRenderer.renderFilledUnitCube(poseStack, multiBufferSource, sectionPos.center(), 0.2f, 1.0f, 0.2f, 0.15f);
    }
}

