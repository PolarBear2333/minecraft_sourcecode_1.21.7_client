/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SpawnerRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerStateData;
import net.minecraft.world.phys.Vec3;

public class TrialSpawnerRenderer
implements BlockEntityRenderer<TrialSpawnerBlockEntity> {
    private final EntityRenderDispatcher entityRenderer;

    public TrialSpawnerRenderer(BlockEntityRendererProvider.Context context) {
        this.entityRenderer = context.getEntityRenderer();
    }

    @Override
    public void render(TrialSpawnerBlockEntity trialSpawnerBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2, Vec3 vec3) {
        Level level = trialSpawnerBlockEntity.getLevel();
        if (level == null) {
            return;
        }
        TrialSpawner trialSpawner = trialSpawnerBlockEntity.getTrialSpawner();
        TrialSpawnerStateData trialSpawnerStateData = trialSpawner.getStateData();
        Entity entity = trialSpawnerStateData.getOrCreateDisplayEntity(trialSpawner, level, trialSpawner.getState());
        if (entity != null) {
            SpawnerRenderer.renderEntityInSpawner(f, poseStack, multiBufferSource, n, entity, this.entityRenderer, trialSpawnerStateData.getOSpin(), trialSpawnerStateData.getSpin());
        }
    }
}

