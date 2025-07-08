/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix4f
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mojang.math.Transformation;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BlockDisplayEntityRenderState;
import net.minecraft.client.renderer.entity.state.DisplayEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.ItemDisplayEntityRenderState;
import net.minecraft.client.renderer.entity.state.TextDisplayEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

public abstract class DisplayRenderer<T extends Display, S, ST extends DisplayEntityRenderState>
extends EntityRenderer<T, ST> {
    private final EntityRenderDispatcher entityRenderDispatcher;

    protected DisplayRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.entityRenderDispatcher = context.getEntityRenderDispatcher();
    }

    @Override
    protected AABB getBoundingBoxForCulling(T t) {
        return ((Display)t).getBoundingBoxForCulling();
    }

    @Override
    protected boolean affectedByCulling(T t) {
        return ((Display)t).affectedByCulling();
    }

    private static int getBrightnessOverride(Display display) {
        Display.RenderState renderState = display.renderState();
        return renderState != null ? renderState.brightnessOverride() : -1;
    }

    @Override
    protected int getSkyLightLevel(T t, BlockPos blockPos) {
        int n = DisplayRenderer.getBrightnessOverride(t);
        if (n != -1) {
            return LightTexture.sky(n);
        }
        return super.getSkyLightLevel(t, blockPos);
    }

    @Override
    protected int getBlockLightLevel(T t, BlockPos blockPos) {
        int n = DisplayRenderer.getBrightnessOverride(t);
        if (n != -1) {
            return LightTexture.block(n);
        }
        return super.getBlockLightLevel(t, blockPos);
    }

    @Override
    protected float getShadowRadius(ST ST) {
        Display.RenderState renderState = ((DisplayEntityRenderState)ST).renderState;
        if (renderState == null) {
            return 0.0f;
        }
        return renderState.shadowRadius().get(((DisplayEntityRenderState)ST).interpolationProgress);
    }

    @Override
    protected float getShadowStrength(ST ST) {
        Display.RenderState renderState = ((DisplayEntityRenderState)ST).renderState;
        if (renderState == null) {
            return 0.0f;
        }
        return renderState.shadowStrength().get(((DisplayEntityRenderState)ST).interpolationProgress);
    }

    @Override
    public void render(ST ST, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        Display.RenderState renderState = ((DisplayEntityRenderState)ST).renderState;
        if (renderState == null || !((DisplayEntityRenderState)ST).hasSubState()) {
            return;
        }
        float f = ((DisplayEntityRenderState)ST).interpolationProgress;
        super.render(ST, poseStack, multiBufferSource, n);
        poseStack.pushPose();
        poseStack.mulPose((Quaternionfc)this.calculateOrientation(renderState, ST, new Quaternionf()));
        Transformation transformation = renderState.transformation().get(f);
        poseStack.mulPose(transformation.getMatrix());
        this.renderInner(ST, poseStack, multiBufferSource, n, f);
        poseStack.popPose();
    }

    private Quaternionf calculateOrientation(Display.RenderState renderState, ST ST, Quaternionf quaternionf) {
        Camera camera = this.entityRenderDispatcher.camera;
        return switch (renderState.billboardConstraints()) {
            default -> throw new MatchException(null, null);
            case Display.BillboardConstraints.FIXED -> quaternionf.rotationYXZ((float)(-Math.PI) / 180 * ((DisplayEntityRenderState)ST).entityYRot, (float)Math.PI / 180 * ((DisplayEntityRenderState)ST).entityXRot, 0.0f);
            case Display.BillboardConstraints.HORIZONTAL -> quaternionf.rotationYXZ((float)(-Math.PI) / 180 * ((DisplayEntityRenderState)ST).entityYRot, (float)Math.PI / 180 * DisplayRenderer.cameraXRot(camera), 0.0f);
            case Display.BillboardConstraints.VERTICAL -> quaternionf.rotationYXZ((float)(-Math.PI) / 180 * DisplayRenderer.cameraYrot(camera), (float)Math.PI / 180 * ((DisplayEntityRenderState)ST).entityXRot, 0.0f);
            case Display.BillboardConstraints.CENTER -> quaternionf.rotationYXZ((float)(-Math.PI) / 180 * DisplayRenderer.cameraYrot(camera), (float)Math.PI / 180 * DisplayRenderer.cameraXRot(camera), 0.0f);
        };
    }

    private static float cameraYrot(Camera camera) {
        return camera.getYRot() - 180.0f;
    }

    private static float cameraXRot(Camera camera) {
        return -camera.getXRot();
    }

    private static <T extends Display> float entityYRot(T t, float f) {
        return t.getYRot(f);
    }

    private static <T extends Display> float entityXRot(T t, float f) {
        return t.getXRot(f);
    }

    protected abstract void renderInner(ST var1, PoseStack var2, MultiBufferSource var3, int var4, float var5);

    @Override
    public void extractRenderState(T t, ST ST, float f) {
        super.extractRenderState(t, ST, f);
        ((DisplayEntityRenderState)ST).renderState = ((Display)t).renderState();
        ((DisplayEntityRenderState)ST).interpolationProgress = ((Display)t).calculateInterpolationProgress(f);
        ((DisplayEntityRenderState)ST).entityYRot = DisplayRenderer.entityYRot(t, f);
        ((DisplayEntityRenderState)ST).entityXRot = DisplayRenderer.entityXRot(t, f);
    }

    @Override
    protected /* synthetic */ float getShadowRadius(EntityRenderState entityRenderState) {
        return this.getShadowRadius((ST)((DisplayEntityRenderState)entityRenderState));
    }

    @Override
    protected /* synthetic */ int getBlockLightLevel(Entity entity, BlockPos blockPos) {
        return this.getBlockLightLevel((T)((Display)entity), blockPos);
    }

    @Override
    protected /* synthetic */ int getSkyLightLevel(Entity entity, BlockPos blockPos) {
        return this.getSkyLightLevel((T)((Display)entity), blockPos);
    }

    public static class TextDisplayRenderer
    extends DisplayRenderer<Display.TextDisplay, Display.TextDisplay.TextRenderState, TextDisplayEntityRenderState> {
        private final Font font;

        protected TextDisplayRenderer(EntityRendererProvider.Context context) {
            super(context);
            this.font = context.getFont();
        }

        @Override
        public TextDisplayEntityRenderState createRenderState() {
            return new TextDisplayEntityRenderState();
        }

        @Override
        public void extractRenderState(Display.TextDisplay textDisplay, TextDisplayEntityRenderState textDisplayEntityRenderState, float f) {
            super.extractRenderState(textDisplay, textDisplayEntityRenderState, f);
            textDisplayEntityRenderState.textRenderState = textDisplay.textRenderState();
            textDisplayEntityRenderState.cachedInfo = textDisplay.cacheDisplay(this::splitLines);
        }

        private Display.TextDisplay.CachedInfo splitLines(Component component, int n) {
            List<FormattedCharSequence> list = this.font.split(component, n);
            ArrayList<Display.TextDisplay.CachedLine> arrayList = new ArrayList<Display.TextDisplay.CachedLine>(list.size());
            int n2 = 0;
            for (FormattedCharSequence formattedCharSequence : list) {
                int n3 = this.font.width(formattedCharSequence);
                n2 = Math.max(n2, n3);
                arrayList.add(new Display.TextDisplay.CachedLine(formattedCharSequence, n3));
            }
            return new Display.TextDisplay.CachedInfo(arrayList, n2);
        }

        @Override
        public void renderInner(TextDisplayEntityRenderState textDisplayEntityRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, float f) {
            int n2;
            float f2;
            Display.TextDisplay.TextRenderState textRenderState = textDisplayEntityRenderState.textRenderState;
            byte by = textRenderState.flags();
            boolean bl = (by & 2) != 0;
            boolean bl2 = (by & 4) != 0;
            boolean bl3 = (by & 1) != 0;
            Display.TextDisplay.Align align = Display.TextDisplay.getAlign(by);
            byte by2 = (byte)textRenderState.textOpacity().get(f);
            if (bl2) {
                f2 = Minecraft.getInstance().options.getBackgroundOpacity(0.25f);
                n2 = (int)(f2 * 255.0f) << 24;
            } else {
                n2 = textRenderState.backgroundColor().get(f);
            }
            f2 = 0.0f;
            Matrix4f matrix4f = poseStack.last().pose();
            matrix4f.rotate((float)Math.PI, 0.0f, 1.0f, 0.0f);
            matrix4f.scale(-0.025f, -0.025f, -0.025f);
            Display.TextDisplay.CachedInfo cachedInfo = textDisplayEntityRenderState.cachedInfo;
            boolean bl4 = true;
            int n3 = this.font.lineHeight + 1;
            int n4 = cachedInfo.width();
            int n5 = cachedInfo.lines().size() * n3 - 1;
            matrix4f.translate(1.0f - (float)n4 / 2.0f, (float)(-n5), 0.0f);
            if (n2 != 0) {
                VertexConsumer vertexConsumer = multiBufferSource.getBuffer(bl ? RenderType.textBackgroundSeeThrough() : RenderType.textBackground());
                vertexConsumer.addVertex(matrix4f, -1.0f, -1.0f, 0.0f).setColor(n2).setLight(n);
                vertexConsumer.addVertex(matrix4f, -1.0f, (float)n5, 0.0f).setColor(n2).setLight(n);
                vertexConsumer.addVertex(matrix4f, (float)n4, (float)n5, 0.0f).setColor(n2).setLight(n);
                vertexConsumer.addVertex(matrix4f, (float)n4, -1.0f, 0.0f).setColor(n2).setLight(n);
            }
            for (Display.TextDisplay.CachedLine cachedLine : cachedInfo.lines()) {
                float f3 = switch (align) {
                    default -> throw new MatchException(null, null);
                    case Display.TextDisplay.Align.LEFT -> 0.0f;
                    case Display.TextDisplay.Align.RIGHT -> n4 - cachedLine.width();
                    case Display.TextDisplay.Align.CENTER -> (float)n4 / 2.0f - (float)cachedLine.width() / 2.0f;
                };
                this.font.drawInBatch(cachedLine.contents(), f3, f2, by2 << 24 | 0xFFFFFF, bl3, matrix4f, multiBufferSource, bl ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.POLYGON_OFFSET, 0, n);
                f2 += (float)n3;
            }
        }

        @Override
        public /* synthetic */ EntityRenderState createRenderState() {
            return this.createRenderState();
        }

        @Override
        protected /* synthetic */ float getShadowRadius(EntityRenderState entityRenderState) {
            return super.getShadowRadius((DisplayEntityRenderState)entityRenderState);
        }

        @Override
        protected /* synthetic */ int getBlockLightLevel(Entity entity, BlockPos blockPos) {
            return super.getBlockLightLevel((Display)entity, blockPos);
        }

        @Override
        protected /* synthetic */ int getSkyLightLevel(Entity entity, BlockPos blockPos) {
            return super.getSkyLightLevel((Display)entity, blockPos);
        }
    }

    public static class ItemDisplayRenderer
    extends DisplayRenderer<Display.ItemDisplay, Display.ItemDisplay.ItemRenderState, ItemDisplayEntityRenderState> {
        private final ItemModelResolver itemModelResolver;

        protected ItemDisplayRenderer(EntityRendererProvider.Context context) {
            super(context);
            this.itemModelResolver = context.getItemModelResolver();
        }

        @Override
        public ItemDisplayEntityRenderState createRenderState() {
            return new ItemDisplayEntityRenderState();
        }

        @Override
        public void extractRenderState(Display.ItemDisplay itemDisplay, ItemDisplayEntityRenderState itemDisplayEntityRenderState, float f) {
            super.extractRenderState(itemDisplay, itemDisplayEntityRenderState, f);
            Display.ItemDisplay.ItemRenderState itemRenderState = itemDisplay.itemRenderState();
            if (itemRenderState != null) {
                this.itemModelResolver.updateForNonLiving(itemDisplayEntityRenderState.item, itemRenderState.itemStack(), itemRenderState.itemTransform(), itemDisplay);
            } else {
                itemDisplayEntityRenderState.item.clear();
            }
        }

        @Override
        public void renderInner(ItemDisplayEntityRenderState itemDisplayEntityRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, float f) {
            if (itemDisplayEntityRenderState.item.isEmpty()) {
                return;
            }
            poseStack.mulPose((Quaternionfc)Axis.YP.rotation((float)Math.PI));
            itemDisplayEntityRenderState.item.render(poseStack, multiBufferSource, n, OverlayTexture.NO_OVERLAY);
        }

        @Override
        public /* synthetic */ EntityRenderState createRenderState() {
            return this.createRenderState();
        }

        @Override
        protected /* synthetic */ float getShadowRadius(EntityRenderState entityRenderState) {
            return super.getShadowRadius((DisplayEntityRenderState)entityRenderState);
        }

        @Override
        protected /* synthetic */ int getBlockLightLevel(Entity entity, BlockPos blockPos) {
            return super.getBlockLightLevel((Display)entity, blockPos);
        }

        @Override
        protected /* synthetic */ int getSkyLightLevel(Entity entity, BlockPos blockPos) {
            return super.getSkyLightLevel((Display)entity, blockPos);
        }
    }

    public static class BlockDisplayRenderer
    extends DisplayRenderer<Display.BlockDisplay, Display.BlockDisplay.BlockRenderState, BlockDisplayEntityRenderState> {
        private final BlockRenderDispatcher blockRenderer;

        protected BlockDisplayRenderer(EntityRendererProvider.Context context) {
            super(context);
            this.blockRenderer = context.getBlockRenderDispatcher();
        }

        @Override
        public BlockDisplayEntityRenderState createRenderState() {
            return new BlockDisplayEntityRenderState();
        }

        @Override
        public void extractRenderState(Display.BlockDisplay blockDisplay, BlockDisplayEntityRenderState blockDisplayEntityRenderState, float f) {
            super.extractRenderState(blockDisplay, blockDisplayEntityRenderState, f);
            blockDisplayEntityRenderState.blockRenderState = blockDisplay.blockRenderState();
        }

        @Override
        public void renderInner(BlockDisplayEntityRenderState blockDisplayEntityRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, float f) {
            this.blockRenderer.renderSingleBlock(blockDisplayEntityRenderState.blockRenderState.blockState(), poseStack, multiBufferSource, n, OverlayTexture.NO_OVERLAY);
        }

        @Override
        public /* synthetic */ EntityRenderState createRenderState() {
            return this.createRenderState();
        }

        @Override
        protected /* synthetic */ float getShadowRadius(EntityRenderState entityRenderState) {
            return super.getShadowRadius((DisplayEntityRenderState)entityRenderState);
        }

        @Override
        protected /* synthetic */ int getBlockLightLevel(Entity entity, BlockPos blockPos) {
            return super.getBlockLightLevel((Display)entity, blockPos);
        }

        @Override
        protected /* synthetic */ int getSkyLightLevel(Entity entity, BlockPos blockPos) {
            return super.getSkyLightLevel((Display)entity, blockPos);
        }
    }
}

