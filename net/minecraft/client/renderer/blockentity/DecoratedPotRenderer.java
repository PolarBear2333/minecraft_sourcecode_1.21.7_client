/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotPatterns;
import net.minecraft.world.level.block.entity.PotDecorations;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

public class DecoratedPotRenderer
implements BlockEntityRenderer<DecoratedPotBlockEntity> {
    private static final String NECK = "neck";
    private static final String FRONT = "front";
    private static final String BACK = "back";
    private static final String LEFT = "left";
    private static final String RIGHT = "right";
    private static final String TOP = "top";
    private static final String BOTTOM = "bottom";
    private final ModelPart neck;
    private final ModelPart frontSide;
    private final ModelPart backSide;
    private final ModelPart leftSide;
    private final ModelPart rightSide;
    private final ModelPart top;
    private final ModelPart bottom;
    private static final float WOBBLE_AMPLITUDE = 0.125f;

    public DecoratedPotRenderer(BlockEntityRendererProvider.Context context) {
        this(context.getModelSet());
    }

    public DecoratedPotRenderer(EntityModelSet entityModelSet) {
        ModelPart modelPart = entityModelSet.bakeLayer(ModelLayers.DECORATED_POT_BASE);
        this.neck = modelPart.getChild(NECK);
        this.top = modelPart.getChild(TOP);
        this.bottom = modelPart.getChild(BOTTOM);
        ModelPart modelPart2 = entityModelSet.bakeLayer(ModelLayers.DECORATED_POT_SIDES);
        this.frontSide = modelPart2.getChild(FRONT);
        this.backSide = modelPart2.getChild(BACK);
        this.leftSide = modelPart2.getChild(LEFT);
        this.rightSide = modelPart2.getChild(RIGHT);
    }

    public static LayerDefinition createBaseLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        CubeDeformation cubeDeformation = new CubeDeformation(0.2f);
        CubeDeformation cubeDeformation2 = new CubeDeformation(-0.1f);
        partDefinition.addOrReplaceChild(NECK, CubeListBuilder.create().texOffs(0, 0).addBox(4.0f, 17.0f, 4.0f, 8.0f, 3.0f, 8.0f, cubeDeformation2).texOffs(0, 5).addBox(5.0f, 20.0f, 5.0f, 6.0f, 1.0f, 6.0f, cubeDeformation), PartPose.offsetAndRotation(0.0f, 37.0f, 16.0f, (float)Math.PI, 0.0f, 0.0f));
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(-14, 13).addBox(0.0f, 0.0f, 0.0f, 14.0f, 0.0f, 14.0f);
        partDefinition.addOrReplaceChild(TOP, cubeListBuilder, PartPose.offsetAndRotation(1.0f, 16.0f, 1.0f, 0.0f, 0.0f, 0.0f));
        partDefinition.addOrReplaceChild(BOTTOM, cubeListBuilder, PartPose.offsetAndRotation(1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f));
        return LayerDefinition.create(meshDefinition, 32, 32);
    }

    public static LayerDefinition createSidesLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(1, 0).addBox(0.0f, 0.0f, 0.0f, 14.0f, 16.0f, 0.0f, EnumSet.of(Direction.NORTH));
        partDefinition.addOrReplaceChild(BACK, cubeListBuilder, PartPose.offsetAndRotation(15.0f, 16.0f, 1.0f, 0.0f, 0.0f, (float)Math.PI));
        partDefinition.addOrReplaceChild(LEFT, cubeListBuilder, PartPose.offsetAndRotation(1.0f, 16.0f, 1.0f, 0.0f, -1.5707964f, (float)Math.PI));
        partDefinition.addOrReplaceChild(RIGHT, cubeListBuilder, PartPose.offsetAndRotation(15.0f, 16.0f, 15.0f, 0.0f, 1.5707964f, (float)Math.PI));
        partDefinition.addOrReplaceChild(FRONT, cubeListBuilder, PartPose.offsetAndRotation(1.0f, 16.0f, 15.0f, (float)Math.PI, 0.0f, 0.0f));
        return LayerDefinition.create(meshDefinition, 16, 16);
    }

    private static Material getSideMaterial(Optional<Item> optional) {
        Material material;
        if (optional.isPresent() && (material = Sheets.getDecoratedPotMaterial(DecoratedPotPatterns.getPatternFromItem(optional.get()))) != null) {
            return material;
        }
        return Sheets.DECORATED_POT_SIDE;
    }

    @Override
    public void render(DecoratedPotBlockEntity decoratedPotBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2, Vec3 vec3) {
        float f2;
        poseStack.pushPose();
        Direction direction = decoratedPotBlockEntity.getDirection();
        poseStack.translate(0.5, 0.0, 0.5);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(180.0f - direction.toYRot()));
        poseStack.translate(-0.5, 0.0, -0.5);
        DecoratedPotBlockEntity.WobbleStyle wobbleStyle = decoratedPotBlockEntity.lastWobbleStyle;
        if (wobbleStyle != null && decoratedPotBlockEntity.getLevel() != null && (f2 = ((float)(decoratedPotBlockEntity.getLevel().getGameTime() - decoratedPotBlockEntity.wobbleStartedAtTick) + f) / (float)wobbleStyle.duration) >= 0.0f && f2 <= 1.0f) {
            if (wobbleStyle == DecoratedPotBlockEntity.WobbleStyle.POSITIVE) {
                float f3 = 0.015625f;
                float f4 = f2 * ((float)Math.PI * 2);
                float f5 = -1.5f * (Mth.cos(f4) + 0.5f) * Mth.sin(f4 / 2.0f);
                poseStack.rotateAround((Quaternionfc)Axis.XP.rotation(f5 * 0.015625f), 0.5f, 0.0f, 0.5f);
                float f6 = Mth.sin(f4);
                poseStack.rotateAround((Quaternionfc)Axis.ZP.rotation(f6 * 0.015625f), 0.5f, 0.0f, 0.5f);
            } else {
                float f7 = Mth.sin(-f2 * 3.0f * (float)Math.PI) * 0.125f;
                float f8 = 1.0f - f2;
                poseStack.rotateAround((Quaternionfc)Axis.YP.rotation(f7 * f8), 0.5f, 0.0f, 0.5f);
            }
        }
        this.render(poseStack, multiBufferSource, n, n2, decoratedPotBlockEntity.getDecorations());
        poseStack.popPose();
    }

    public void renderInHand(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2, PotDecorations potDecorations) {
        this.render(poseStack, multiBufferSource, n, n2, potDecorations);
    }

    private void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2, PotDecorations potDecorations) {
        VertexConsumer vertexConsumer = Sheets.DECORATED_POT_BASE.buffer(multiBufferSource, RenderType::entitySolid);
        this.neck.render(poseStack, vertexConsumer, n, n2);
        this.top.render(poseStack, vertexConsumer, n, n2);
        this.bottom.render(poseStack, vertexConsumer, n, n2);
        this.renderSide(this.frontSide, poseStack, multiBufferSource, n, n2, DecoratedPotRenderer.getSideMaterial(potDecorations.front()));
        this.renderSide(this.backSide, poseStack, multiBufferSource, n, n2, DecoratedPotRenderer.getSideMaterial(potDecorations.back()));
        this.renderSide(this.leftSide, poseStack, multiBufferSource, n, n2, DecoratedPotRenderer.getSideMaterial(potDecorations.left()));
        this.renderSide(this.rightSide, poseStack, multiBufferSource, n, n2, DecoratedPotRenderer.getSideMaterial(potDecorations.right()));
    }

    private void renderSide(ModelPart modelPart, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2, Material material) {
        modelPart.render(poseStack, material.buffer(multiBufferSource, RenderType::entitySolid), n, n2);
    }

    public void getExtents(Set<Vector3f> set) {
        PoseStack poseStack = new PoseStack();
        this.neck.getExtentsForGui(poseStack, set);
        this.top.getExtentsForGui(poseStack, set);
        this.bottom.getExtentsForGui(poseStack, set);
    }
}

