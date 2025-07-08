/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.resources.model;

import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SpriteGetter;
import net.minecraft.resources.ResourceLocation;

public interface ModelBaker {
    public ResolvedModel getModel(ResourceLocation var1);

    public SpriteGetter sprites();

    public <T> T compute(SharedOperationKey<T> var1);

    @FunctionalInterface
    public static interface SharedOperationKey<T> {
        public T compute(ModelBaker var1);
    }
}

