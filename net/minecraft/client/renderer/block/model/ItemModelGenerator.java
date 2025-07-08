/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package net.minecraft.client.renderer.block.model;

import com.mojang.math.Quadrant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.SimpleUnbakedGeometry;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.client.resources.model.SpriteGetter;
import net.minecraft.client.resources.model.UnbakedGeometry;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class ItemModelGenerator
implements UnbakedModel {
    public static final ResourceLocation GENERATED_ITEM_MODEL_ID = ResourceLocation.withDefaultNamespace("builtin/generated");
    public static final List<String> LAYERS = List.of("layer0", "layer1", "layer2", "layer3", "layer4");
    private static final float MIN_Z = 7.5f;
    private static final float MAX_Z = 8.5f;
    private static final TextureSlots.Data TEXTURE_SLOTS = new TextureSlots.Data.Builder().addReference("particle", "layer0").build();
    private static final BlockElementFace.UVs SOUTH_FACE_UVS = new BlockElementFace.UVs(0.0f, 0.0f, 16.0f, 16.0f);
    private static final BlockElementFace.UVs NORTH_FACE_UVS = new BlockElementFace.UVs(16.0f, 0.0f, 0.0f, 16.0f);

    @Override
    public TextureSlots.Data textureSlots() {
        return TEXTURE_SLOTS;
    }

    @Override
    public UnbakedGeometry geometry() {
        return ItemModelGenerator::bake;
    }

    @Override
    @Nullable
    public UnbakedModel.GuiLight guiLight() {
        return UnbakedModel.GuiLight.FRONT;
    }

    private static QuadCollection bake(TextureSlots textureSlots, ModelBaker modelBaker, ModelState modelState, ModelDebugName modelDebugName) {
        return ItemModelGenerator.bake(textureSlots, modelBaker.sprites(), modelState, modelDebugName);
    }

    private static QuadCollection bake(TextureSlots textureSlots, SpriteGetter spriteGetter, ModelState modelState, ModelDebugName modelDebugName) {
        String string;
        Material material;
        ArrayList<BlockElement> arrayList = new ArrayList<BlockElement>();
        for (int i = 0; i < LAYERS.size() && (material = textureSlots.getMaterial(string = LAYERS.get(i))) != null; ++i) {
            SpriteContents spriteContents = spriteGetter.get(material, modelDebugName).contents();
            arrayList.addAll(ItemModelGenerator.processFrames(i, string, spriteContents));
        }
        return SimpleUnbakedGeometry.bake(arrayList, textureSlots, spriteGetter, modelState, modelDebugName);
    }

    private static List<BlockElement> processFrames(int n, String string, SpriteContents spriteContents) {
        Map<Direction, BlockElementFace> map = Map.of(Direction.SOUTH, new BlockElementFace(null, n, string, SOUTH_FACE_UVS, Quadrant.R0), Direction.NORTH, new BlockElementFace(null, n, string, NORTH_FACE_UVS, Quadrant.R0));
        ArrayList<BlockElement> arrayList = new ArrayList<BlockElement>();
        arrayList.add(new BlockElement((Vector3fc)new Vector3f(0.0f, 0.0f, 7.5f), (Vector3fc)new Vector3f(16.0f, 16.0f, 8.5f), map));
        arrayList.addAll(ItemModelGenerator.createSideElements(spriteContents, string, n));
        return arrayList;
    }

    private static List<BlockElement> createSideElements(SpriteContents spriteContents, String string, int n) {
        float f = spriteContents.width();
        float f2 = spriteContents.height();
        ArrayList<BlockElement> arrayList = new ArrayList<BlockElement>();
        for (Span span : ItemModelGenerator.getSpans(spriteContents)) {
            float f3 = 0.0f;
            float f4 = 0.0f;
            float f5 = 0.0f;
            float f6 = 0.0f;
            float f7 = 0.0f;
            float f8 = 0.0f;
            float f9 = 0.0f;
            float f10 = 0.0f;
            float f11 = 16.0f / f;
            float f12 = 16.0f / f2;
            float f13 = span.getMin();
            float f14 = span.getMax();
            float f15 = span.getAnchor();
            SpanFacing spanFacing = span.getFacing();
            switch (spanFacing.ordinal()) {
                case 0: {
                    f3 = f7 = f13;
                    f5 = f8 = f14 + 1.0f;
                    f4 = f9 = f15;
                    f6 = f15;
                    f10 = f15 + 1.0f;
                    break;
                }
                case 1: {
                    f9 = f15;
                    f10 = f15 + 1.0f;
                    f3 = f7 = f13;
                    f5 = f8 = f14 + 1.0f;
                    f4 = f15 + 1.0f;
                    f6 = f15 + 1.0f;
                    break;
                }
                case 2: {
                    f3 = f7 = f15;
                    f5 = f15;
                    f8 = f15 + 1.0f;
                    f4 = f10 = f13;
                    f6 = f9 = f14 + 1.0f;
                    break;
                }
                case 3: {
                    f7 = f15;
                    f8 = f15 + 1.0f;
                    f3 = f15 + 1.0f;
                    f5 = f15 + 1.0f;
                    f4 = f10 = f13;
                    f6 = f9 = f14 + 1.0f;
                }
            }
            f3 *= f11;
            f5 *= f11;
            f4 *= f12;
            f6 *= f12;
            f4 = 16.0f - f4;
            f6 = 16.0f - f6;
            Map<Direction, BlockElementFace> map = Map.of(spanFacing.getDirection(), new BlockElementFace(null, n, string, new BlockElementFace.UVs(f7 *= f11, f9 *= f12, f8 *= f11, f10 *= f12), Quadrant.R0));
            switch (spanFacing.ordinal()) {
                case 0: {
                    arrayList.add(new BlockElement((Vector3fc)new Vector3f(f3, f4, 7.5f), (Vector3fc)new Vector3f(f5, f4, 8.5f), map));
                    break;
                }
                case 1: {
                    arrayList.add(new BlockElement((Vector3fc)new Vector3f(f3, f6, 7.5f), (Vector3fc)new Vector3f(f5, f6, 8.5f), map));
                    break;
                }
                case 2: {
                    arrayList.add(new BlockElement((Vector3fc)new Vector3f(f3, f4, 7.5f), (Vector3fc)new Vector3f(f3, f6, 8.5f), map));
                    break;
                }
                case 3: {
                    arrayList.add(new BlockElement((Vector3fc)new Vector3f(f5, f4, 7.5f), (Vector3fc)new Vector3f(f5, f6, 8.5f), map));
                }
            }
        }
        return arrayList;
    }

    private static List<Span> getSpans(SpriteContents spriteContents) {
        int n = spriteContents.width();
        int n2 = spriteContents.height();
        ArrayList<Span> arrayList = new ArrayList<Span>();
        spriteContents.getUniqueFrames().forEach(n3 -> {
            for (int i = 0; i < n2; ++i) {
                for (int j = 0; j < n; ++j) {
                    boolean bl = !ItemModelGenerator.isTransparent(spriteContents, n3, j, i, n, n2);
                    ItemModelGenerator.checkTransition(SpanFacing.UP, arrayList, spriteContents, n3, j, i, n, n2, bl);
                    ItemModelGenerator.checkTransition(SpanFacing.DOWN, arrayList, spriteContents, n3, j, i, n, n2, bl);
                    ItemModelGenerator.checkTransition(SpanFacing.LEFT, arrayList, spriteContents, n3, j, i, n, n2, bl);
                    ItemModelGenerator.checkTransition(SpanFacing.RIGHT, arrayList, spriteContents, n3, j, i, n, n2, bl);
                }
            }
        });
        return arrayList;
    }

    private static void checkTransition(SpanFacing spanFacing, List<Span> list, SpriteContents spriteContents, int n, int n2, int n3, int n4, int n5, boolean bl) {
        boolean bl2;
        boolean bl3 = bl2 = ItemModelGenerator.isTransparent(spriteContents, n, n2 + spanFacing.getXOffset(), n3 + spanFacing.getYOffset(), n4, n5) && bl;
        if (bl2) {
            ItemModelGenerator.createOrExpandSpan(list, spanFacing, n2, n3);
        }
    }

    private static void createOrExpandSpan(List<Span> list, SpanFacing spanFacing, int n, int n2) {
        int n3;
        Span span = null;
        for (Span span2 : list) {
            int n4;
            if (span2.getFacing() != spanFacing) continue;
            int n5 = n4 = spanFacing.isHorizontal() ? n2 : n;
            if (span2.getAnchor() != n4) continue;
            span = span2;
            break;
        }
        int n6 = spanFacing.isHorizontal() ? n2 : n;
        int n7 = n3 = spanFacing.isHorizontal() ? n : n2;
        if (span == null) {
            list.add(new Span(spanFacing, n3, n6));
        } else {
            span.expand(n3);
        }
    }

    private static boolean isTransparent(SpriteContents spriteContents, int n, int n2, int n3, int n4, int n5) {
        if (n2 < 0 || n3 < 0 || n2 >= n4 || n3 >= n5) {
            return true;
        }
        return spriteContents.isTransparent(n, n2, n3);
    }

    static class Span {
        private final SpanFacing facing;
        private int min;
        private int max;
        private final int anchor;

        public Span(SpanFacing spanFacing, int n, int n2) {
            this.facing = spanFacing;
            this.min = n;
            this.max = n;
            this.anchor = n2;
        }

        public void expand(int n) {
            if (n < this.min) {
                this.min = n;
            } else if (n > this.max) {
                this.max = n;
            }
        }

        public SpanFacing getFacing() {
            return this.facing;
        }

        public int getMin() {
            return this.min;
        }

        public int getMax() {
            return this.max;
        }

        public int getAnchor() {
            return this.anchor;
        }
    }

    static enum SpanFacing {
        UP(Direction.UP, 0, -1),
        DOWN(Direction.DOWN, 0, 1),
        LEFT(Direction.EAST, -1, 0),
        RIGHT(Direction.WEST, 1, 0);

        private final Direction direction;
        private final int xOffset;
        private final int yOffset;

        private SpanFacing(Direction direction, int n2, int n3) {
            this.direction = direction;
            this.xOffset = n2;
            this.yOffset = n3;
        }

        public Direction getDirection() {
            return this.direction;
        }

        public int getXOffset() {
            return this.xOffset;
        }

        public int getYOffset() {
            return this.yOffset;
        }

        boolean isHorizontal() {
            return this == DOWN || this == UP;
        }
    }
}

