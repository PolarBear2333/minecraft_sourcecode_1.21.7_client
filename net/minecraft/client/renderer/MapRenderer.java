/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix4f
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MapDecorationTextureManager;
import net.minecraft.client.resources.MapTextureManager;
import net.minecraft.util.Mth;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.joml.Matrix4f;
import org.joml.Quaternionfc;

public class MapRenderer {
    private static final float MAP_Z_OFFSET = -0.01f;
    private static final float DECORATION_Z_OFFSET = -0.001f;
    public static final int WIDTH = 128;
    public static final int HEIGHT = 128;
    private final MapTextureManager mapTextureManager;
    private final MapDecorationTextureManager decorationTextures;

    public MapRenderer(MapDecorationTextureManager mapDecorationTextureManager, MapTextureManager mapTextureManager) {
        this.decorationTextures = mapDecorationTextureManager;
        this.mapTextureManager = mapTextureManager;
    }

    public void render(MapRenderState mapRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, boolean bl, int n) {
        Matrix4f matrix4f = poseStack.last().pose();
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.text(mapRenderState.texture));
        vertexConsumer.addVertex(matrix4f, 0.0f, 128.0f, -0.01f).setColor(-1).setUv(0.0f, 1.0f).setLight(n);
        vertexConsumer.addVertex(matrix4f, 128.0f, 128.0f, -0.01f).setColor(-1).setUv(1.0f, 1.0f).setLight(n);
        vertexConsumer.addVertex(matrix4f, 128.0f, 0.0f, -0.01f).setColor(-1).setUv(1.0f, 0.0f).setLight(n);
        vertexConsumer.addVertex(matrix4f, 0.0f, 0.0f, -0.01f).setColor(-1).setUv(0.0f, 0.0f).setLight(n);
        int n2 = 0;
        for (MapRenderState.MapDecorationRenderState mapDecorationRenderState : mapRenderState.decorations) {
            Object object;
            if (bl && !mapDecorationRenderState.renderOnFrame) continue;
            poseStack.pushPose();
            poseStack.translate((float)mapDecorationRenderState.x / 2.0f + 64.0f, (float)mapDecorationRenderState.y / 2.0f + 64.0f, -0.02f);
            poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees((float)(mapDecorationRenderState.rot * 360) / 16.0f));
            poseStack.scale(4.0f, 4.0f, 3.0f);
            poseStack.translate(-0.125f, 0.125f, 0.0f);
            Matrix4f matrix4f2 = poseStack.last().pose();
            TextureAtlasSprite textureAtlasSprite = mapDecorationRenderState.atlasSprite;
            if (textureAtlasSprite != null) {
                object = multiBufferSource.getBuffer(RenderType.text(textureAtlasSprite.atlasLocation()));
                object.addVertex(matrix4f2, -1.0f, 1.0f, (float)n2 * -0.001f).setColor(-1).setUv(textureAtlasSprite.getU0(), textureAtlasSprite.getV0()).setLight(n);
                object.addVertex(matrix4f2, 1.0f, 1.0f, (float)n2 * -0.001f).setColor(-1).setUv(textureAtlasSprite.getU1(), textureAtlasSprite.getV0()).setLight(n);
                object.addVertex(matrix4f2, 1.0f, -1.0f, (float)n2 * -0.001f).setColor(-1).setUv(textureAtlasSprite.getU1(), textureAtlasSprite.getV1()).setLight(n);
                object.addVertex(matrix4f2, -1.0f, -1.0f, (float)n2 * -0.001f).setColor(-1).setUv(textureAtlasSprite.getU0(), textureAtlasSprite.getV1()).setLight(n);
                poseStack.popPose();
            }
            if (mapDecorationRenderState.name != null) {
                object = Minecraft.getInstance().font;
                float f = ((Font)object).width(mapDecorationRenderState.name);
                float f2 = 25.0f / f;
                Objects.requireNonNull(object);
                float f3 = Mth.clamp(f2, 0.0f, 6.0f / 9.0f);
                poseStack.pushPose();
                poseStack.translate((float)mapDecorationRenderState.x / 2.0f + 64.0f - f * f3 / 2.0f, (float)mapDecorationRenderState.y / 2.0f + 64.0f + 4.0f, -0.025f);
                poseStack.scale(f3, f3, -1.0f);
                poseStack.translate(0.0f, 0.0f, 0.1f);
                ((Font)object).drawInBatch(mapDecorationRenderState.name, 0.0f, 0.0f, -1, false, poseStack.last().pose(), multiBufferSource, Font.DisplayMode.NORMAL, Integer.MIN_VALUE, n);
                poseStack.popPose();
            }
            ++n2;
        }
    }

    public void extractRenderState(MapId mapId, MapItemSavedData mapItemSavedData, MapRenderState mapRenderState) {
        mapRenderState.texture = this.mapTextureManager.prepareMapTexture(mapId, mapItemSavedData);
        mapRenderState.decorations.clear();
        for (MapDecoration mapDecoration : mapItemSavedData.getDecorations()) {
            mapRenderState.decorations.add(this.extractDecorationRenderState(mapDecoration));
        }
    }

    private MapRenderState.MapDecorationRenderState extractDecorationRenderState(MapDecoration mapDecoration) {
        MapRenderState.MapDecorationRenderState mapDecorationRenderState = new MapRenderState.MapDecorationRenderState();
        mapDecorationRenderState.atlasSprite = this.decorationTextures.get(mapDecoration);
        mapDecorationRenderState.x = mapDecoration.x();
        mapDecorationRenderState.y = mapDecoration.y();
        mapDecorationRenderState.rot = mapDecoration.rot();
        mapDecorationRenderState.name = mapDecoration.name().orElse(null);
        mapDecorationRenderState.renderOnFrame = mapDecoration.renderOnFrame();
        return mapDecorationRenderState;
    }
}

