/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.util.SequencedMap;
import net.minecraft.Util;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.SectionBufferBuilderPool;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.resources.model.ModelBakery;

public class RenderBuffers {
    private final SectionBufferBuilderPack fixedBufferPack = new SectionBufferBuilderPack();
    private final SectionBufferBuilderPool sectionBufferPool;
    private final MultiBufferSource.BufferSource bufferSource;
    private final MultiBufferSource.BufferSource crumblingBufferSource;
    private final OutlineBufferSource outlineBufferSource;

    public RenderBuffers(int n) {
        this.sectionBufferPool = SectionBufferBuilderPool.allocate(n);
        SequencedMap sequencedMap = (SequencedMap)Util.make(new Object2ObjectLinkedOpenHashMap(), object2ObjectLinkedOpenHashMap -> {
            object2ObjectLinkedOpenHashMap.put((Object)Sheets.solidBlockSheet(), (Object)this.fixedBufferPack.buffer(ChunkSectionLayer.SOLID));
            object2ObjectLinkedOpenHashMap.put((Object)Sheets.cutoutBlockSheet(), (Object)this.fixedBufferPack.buffer(ChunkSectionLayer.CUTOUT));
            object2ObjectLinkedOpenHashMap.put((Object)Sheets.bannerSheet(), (Object)this.fixedBufferPack.buffer(ChunkSectionLayer.CUTOUT_MIPPED));
            object2ObjectLinkedOpenHashMap.put((Object)Sheets.translucentItemSheet(), (Object)this.fixedBufferPack.buffer(ChunkSectionLayer.TRANSLUCENT));
            RenderBuffers.put((Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>)object2ObjectLinkedOpenHashMap, Sheets.shieldSheet());
            RenderBuffers.put((Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>)object2ObjectLinkedOpenHashMap, Sheets.bedSheet());
            RenderBuffers.put((Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>)object2ObjectLinkedOpenHashMap, Sheets.shulkerBoxSheet());
            RenderBuffers.put((Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>)object2ObjectLinkedOpenHashMap, Sheets.signSheet());
            RenderBuffers.put((Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>)object2ObjectLinkedOpenHashMap, Sheets.hangingSignSheet());
            object2ObjectLinkedOpenHashMap.put((Object)Sheets.chestSheet(), (Object)new ByteBufferBuilder(786432));
            RenderBuffers.put((Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>)object2ObjectLinkedOpenHashMap, RenderType.armorEntityGlint());
            RenderBuffers.put((Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>)object2ObjectLinkedOpenHashMap, RenderType.glint());
            RenderBuffers.put((Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>)object2ObjectLinkedOpenHashMap, RenderType.glintTranslucent());
            RenderBuffers.put((Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>)object2ObjectLinkedOpenHashMap, RenderType.entityGlint());
            RenderBuffers.put((Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>)object2ObjectLinkedOpenHashMap, RenderType.waterMask());
        });
        this.bufferSource = MultiBufferSource.immediateWithBuffers(sequencedMap, new ByteBufferBuilder(786432));
        this.outlineBufferSource = new OutlineBufferSource(this.bufferSource);
        SequencedMap sequencedMap2 = (SequencedMap)Util.make(new Object2ObjectLinkedOpenHashMap(), object2ObjectLinkedOpenHashMap -> ModelBakery.DESTROY_TYPES.forEach(renderType -> RenderBuffers.put((Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>)object2ObjectLinkedOpenHashMap, renderType)));
        this.crumblingBufferSource = MultiBufferSource.immediateWithBuffers(sequencedMap2, new ByteBufferBuilder(0));
    }

    private static void put(Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder> object2ObjectLinkedOpenHashMap, RenderType renderType) {
        object2ObjectLinkedOpenHashMap.put((Object)renderType, (Object)new ByteBufferBuilder(renderType.bufferSize()));
    }

    public SectionBufferBuilderPack fixedBufferPack() {
        return this.fixedBufferPack;
    }

    public SectionBufferBuilderPool sectionBufferPool() {
        return this.sectionBufferPool;
    }

    public MultiBufferSource.BufferSource bufferSource() {
        return this.bufferSource;
    }

    public MultiBufferSource.BufferSource crumblingBufferSource() {
        return this.crumblingBufferSource;
    }

    public OutlineBufferSource outlineBufferSource() {
        return this.outlineBufferSource;
    }
}

