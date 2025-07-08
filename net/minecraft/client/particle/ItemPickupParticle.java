/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;

public class ItemPickupParticle
extends Particle {
    private static final int LIFE_TIME = 3;
    private final Entity itemEntity;
    private final Entity target;
    private int life;
    private final EntityRenderDispatcher entityRenderDispatcher;
    private double targetX;
    private double targetY;
    private double targetZ;
    private double targetXOld;
    private double targetYOld;
    private double targetZOld;

    public ItemPickupParticle(EntityRenderDispatcher entityRenderDispatcher, ClientLevel clientLevel, Entity entity, Entity entity2) {
        this(entityRenderDispatcher, clientLevel, entity, entity2, entity.getDeltaMovement());
    }

    private ItemPickupParticle(EntityRenderDispatcher entityRenderDispatcher, ClientLevel clientLevel, Entity entity, Entity entity2, Vec3 vec3) {
        super(clientLevel, entity.getX(), entity.getY(), entity.getZ(), vec3.x, vec3.y, vec3.z);
        this.itemEntity = this.getSafeCopy(entity);
        this.target = entity2;
        this.entityRenderDispatcher = entityRenderDispatcher;
        this.updatePosition();
        this.saveOldPosition();
    }

    private Entity getSafeCopy(Entity entity) {
        if (!(entity instanceof ItemEntity)) {
            return entity;
        }
        return ((ItemEntity)entity).copy();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    @Override
    public void renderCustom(PoseStack poseStack, MultiBufferSource multiBufferSource, Camera camera, float f) {
        float f2 = ((float)this.life + f) / 3.0f;
        f2 *= f2;
        double d = Mth.lerp((double)f, this.targetXOld, this.targetX);
        double d2 = Mth.lerp((double)f, this.targetYOld, this.targetY);
        double d3 = Mth.lerp((double)f, this.targetZOld, this.targetZ);
        double d4 = Mth.lerp((double)f2, this.itemEntity.getX(), d);
        double d5 = Mth.lerp((double)f2, this.itemEntity.getY(), d2);
        double d6 = Mth.lerp((double)f2, this.itemEntity.getZ(), d3);
        Vec3 vec3 = camera.getPosition();
        this.entityRenderDispatcher.render(this.itemEntity, d4 - vec3.x(), d5 - vec3.y(), d6 - vec3.z(), f, new PoseStack(), multiBufferSource, this.entityRenderDispatcher.getPackedLightCoords(this.itemEntity, f));
    }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float f) {
    }

    @Override
    public void tick() {
        ++this.life;
        if (this.life == 3) {
            this.remove();
        }
        this.saveOldPosition();
        this.updatePosition();
    }

    private void updatePosition() {
        this.targetX = this.target.getX();
        this.targetY = (this.target.getY() + this.target.getEyeY()) / 2.0;
        this.targetZ = this.target.getZ();
    }

    private void saveOldPosition() {
        this.targetXOld = this.targetX;
        this.targetYOld = this.targetY;
        this.targetZOld = this.targetZ;
    }
}

