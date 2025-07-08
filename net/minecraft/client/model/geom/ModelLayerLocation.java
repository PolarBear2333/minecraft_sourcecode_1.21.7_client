/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model.geom;

import net.minecraft.resources.ResourceLocation;

public record ModelLayerLocation(ResourceLocation model, String layer) {
    @Override
    public String toString() {
        return String.valueOf(this.model) + "#" + this.layer;
    }
}

