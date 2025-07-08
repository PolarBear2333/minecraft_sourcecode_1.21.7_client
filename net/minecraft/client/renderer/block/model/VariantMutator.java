/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.block.model;

import com.mojang.math.Quadrant;
import java.util.function.UnaryOperator;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.resources.ResourceLocation;

@FunctionalInterface
public interface VariantMutator
extends UnaryOperator<Variant> {
    public static final VariantProperty<Quadrant> X_ROT = Variant::withXRot;
    public static final VariantProperty<Quadrant> Y_ROT = Variant::withYRot;
    public static final VariantProperty<ResourceLocation> MODEL = Variant::withModel;
    public static final VariantProperty<Boolean> UV_LOCK = Variant::withUvLock;

    default public VariantMutator then(VariantMutator variantMutator) {
        return variant -> (Variant)variantMutator.apply((Variant)this.apply(variant));
    }

    @FunctionalInterface
    public static interface VariantProperty<T> {
        public Variant apply(Variant var1, T var2);

        default public VariantMutator withValue(T t) {
            return variant -> this.apply((Variant)variant, t);
        }
    }
}

