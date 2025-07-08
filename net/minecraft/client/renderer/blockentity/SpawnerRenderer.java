/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;

public class SpawnerRenderer
implements BlockEntityRenderer<SpawnerBlockEntity> {
    private final EntityRenderDispatcher entityRenderer;

    public SpawnerRenderer(BlockEntityRendererProvider.Context context) {
        this.entityRenderer = context.getEntityRenderer();
    }

    @Override
    public void render(SpawnerBlockEntity spawnerBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2, Vec3 vec3) {
        Level level = spawnerBlockEntity.getLevel();
        if (level == null) {
            return;
        }
        BaseSpawner baseSpawner = spawnerBlockEntity.getSpawner();
        Entity entity = baseSpawner.getOrCreateDisplayEntity(level, spawnerBlockEntity.getBlockPos());
        if (entity != null) {
            SpawnerRenderer.renderEntityInSpawner(f, poseStack, multiBufferSource, n, entity, this.entityRenderer, baseSpawner.getoSpin(), baseSpawner.getSpin());
        }
    }

    public static void renderEntityInSpawner(float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, Entity entity, EntityRenderDispatcher entityRenderDispatcher, double d, double d2) {
        poseStack.pushPose();
        poseStack.translate(0.5f, 0.0f, 0.5f);
        float f2 = 0.53125f;
        float f3 = Math.max(entity.getBbWidth(), entity.getBbHeight());
        if ((double)f3 > 1.0) {
            f2 /= f3;
        }
        poseStack.translate(0.0f, 0.4f, 0.0f);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees((float)Mth.lerp((double)f, d, d2) * 10.0f));
        poseStack.translate(0.0f, -0.2f, 0.0f);
        poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(-30.0f));
        poseStack.scale(f2, f2, f2);
        entityRenderDispatcher.render(entity, 0.0, 0.0, 0.0, f, poseStack, multiBufferSource, n);
        poseStack.popPose();
    }
}

