/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.Mob;

@Deprecated
public abstract class AgeableMobRenderer<T extends Mob, S extends LivingEntityRenderState, M extends EntityModel<? super S>>
extends MobRenderer<T, S, M> {
    private final M adultModel;
    private final M babyModel;

    public AgeableMobRenderer(EntityRendererProvider.Context context, M m, M m2, float f) {
        super(context, m, f);
        this.adultModel = m;
        this.babyModel = m2;
    }

    @Override
    public void render(S s, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        this.model = ((LivingEntityRenderState)s).isBaby ? this.babyModel : this.adultModel;
        super.render(s, poseStack, multiBufferSource, n);
    }
}

