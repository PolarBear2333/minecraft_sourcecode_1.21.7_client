/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package net.minecraft.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class ItemStackRenderState {
    ItemDisplayContext displayContext = ItemDisplayContext.NONE;
    private int activeLayerCount;
    private boolean animated;
    private boolean oversizedInGui;
    @Nullable
    private AABB cachedModelBoundingBox;
    private LayerRenderState[] layers = new LayerRenderState[]{new LayerRenderState()};

    public void ensureCapacity(int n) {
        int n2 = this.activeLayerCount + n;
        int n3 = this.layers.length;
        if (n2 > n3) {
            this.layers = Arrays.copyOf(this.layers, n2);
            for (int i = n3; i < n2; ++i) {
                this.layers[i] = new LayerRenderState();
            }
        }
    }

    public LayerRenderState newLayer() {
        this.ensureCapacity(1);
        return this.layers[this.activeLayerCount++];
    }

    public void clear() {
        this.displayContext = ItemDisplayContext.NONE;
        for (int i = 0; i < this.activeLayerCount; ++i) {
            this.layers[i].clear();
        }
        this.activeLayerCount = 0;
        this.animated = false;
        this.oversizedInGui = false;
        this.cachedModelBoundingBox = null;
    }

    public void setAnimated() {
        this.animated = true;
    }

    public boolean isAnimated() {
        return this.animated;
    }

    public void appendModelIdentityElement(Object object) {
    }

    private LayerRenderState firstLayer() {
        return this.layers[0];
    }

    public boolean isEmpty() {
        return this.activeLayerCount == 0;
    }

    public boolean usesBlockLight() {
        return this.firstLayer().usesBlockLight;
    }

    @Nullable
    public TextureAtlasSprite pickParticleIcon(RandomSource randomSource) {
        if (this.activeLayerCount == 0) {
            return null;
        }
        return this.layers[randomSource.nextInt((int)this.activeLayerCount)].particleIcon;
    }

    public void visitExtents(Consumer<Vector3fc> consumer) {
        Vector3f vector3f = new Vector3f();
        PoseStack.Pose pose = new PoseStack.Pose();
        for (int i = 0; i < this.activeLayerCount; ++i) {
            Vector3f[] vector3fArray;
            LayerRenderState layerRenderState = this.layers[i];
            layerRenderState.transform.apply(this.displayContext.leftHand(), pose);
            Matrix4f matrix4f = pose.pose();
            for (Vector3f vector3f2 : vector3fArray = layerRenderState.extents.get()) {
                consumer.accept((Vector3fc)vector3f.set((Vector3fc)vector3f2).mulPosition((Matrix4fc)matrix4f));
            }
            pose.setIdentity();
        }
    }

    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2) {
        for (int i = 0; i < this.activeLayerCount; ++i) {
            this.layers[i].render(poseStack, multiBufferSource, n, n2);
        }
    }

    public AABB getModelBoundingBox() {
        AABB aABB;
        if (this.cachedModelBoundingBox != null) {
            return this.cachedModelBoundingBox;
        }
        AABB.Builder builder = new AABB.Builder();
        this.visitExtents(builder::include);
        this.cachedModelBoundingBox = aABB = builder.build();
        return aABB;
    }

    public void setOversizedInGui(boolean bl) {
        this.oversizedInGui = bl;
    }

    public boolean isOversizedInGui() {
        return this.oversizedInGui;
    }

    public class LayerRenderState {
        private static final Vector3f[] NO_EXTENTS = new Vector3f[0];
        public static final Supplier<Vector3f[]> NO_EXTENTS_SUPPLIER = () -> NO_EXTENTS;
        private final List<BakedQuad> quads = new ArrayList<BakedQuad>();
        boolean usesBlockLight;
        @Nullable
        TextureAtlasSprite particleIcon;
        ItemTransform transform = ItemTransform.NO_TRANSFORM;
        @Nullable
        private RenderType renderType;
        private FoilType foilType = FoilType.NONE;
        private int[] tintLayers = new int[0];
        @Nullable
        private SpecialModelRenderer<Object> specialRenderer;
        @Nullable
        private Object argumentForSpecialRendering;
        Supplier<Vector3f[]> extents = NO_EXTENTS_SUPPLIER;

        public void clear() {
            this.quads.clear();
            this.renderType = null;
            this.foilType = FoilType.NONE;
            this.specialRenderer = null;
            this.argumentForSpecialRendering = null;
            Arrays.fill(this.tintLayers, -1);
            this.usesBlockLight = false;
            this.particleIcon = null;
            this.transform = ItemTransform.NO_TRANSFORM;
            this.extents = NO_EXTENTS_SUPPLIER;
        }

        public List<BakedQuad> prepareQuadList() {
            return this.quads;
        }

        public void setRenderType(RenderType renderType) {
            this.renderType = renderType;
        }

        public void setUsesBlockLight(boolean bl) {
            this.usesBlockLight = bl;
        }

        public void setExtents(Supplier<Vector3f[]> supplier) {
            this.extents = supplier;
        }

        public void setParticleIcon(TextureAtlasSprite textureAtlasSprite) {
            this.particleIcon = textureAtlasSprite;
        }

        public void setTransform(ItemTransform itemTransform) {
            this.transform = itemTransform;
        }

        public <T> void setupSpecialModel(SpecialModelRenderer<T> specialModelRenderer, @Nullable T t) {
            this.specialRenderer = LayerRenderState.eraseSpecialRenderer(specialModelRenderer);
            this.argumentForSpecialRendering = t;
        }

        private static SpecialModelRenderer<Object> eraseSpecialRenderer(SpecialModelRenderer<?> specialModelRenderer) {
            return specialModelRenderer;
        }

        public void setFoilType(FoilType foilType) {
            this.foilType = foilType;
        }

        public int[] prepareTintLayers(int n) {
            if (n > this.tintLayers.length) {
                this.tintLayers = new int[n];
                Arrays.fill(this.tintLayers, -1);
            }
            return this.tintLayers;
        }

        void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2) {
            poseStack.pushPose();
            this.transform.apply(ItemStackRenderState.this.displayContext.leftHand(), poseStack.last());
            if (this.specialRenderer != null) {
                this.specialRenderer.render(this.argumentForSpecialRendering, ItemStackRenderState.this.displayContext, poseStack, multiBufferSource, n, n2, this.foilType != FoilType.NONE);
            } else if (this.renderType != null) {
                ItemRenderer.renderItem(ItemStackRenderState.this.displayContext, poseStack, multiBufferSource, n, n2, this.tintLayers, this.quads, this.renderType, this.foilType);
            }
            poseStack.popPose();
        }
    }

    public static enum FoilType {
        NONE,
        STANDARD,
        SPECIAL;

    }
}

