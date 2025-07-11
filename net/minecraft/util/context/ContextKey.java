/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.context;

import net.minecraft.resources.ResourceLocation;

public class ContextKey<T> {
    private final ResourceLocation name;

    public ContextKey(ResourceLocation resourceLocation) {
        this.name = resourceLocation;
    }

    public static <T> ContextKey<T> vanilla(String string) {
        return new ContextKey<T>(ResourceLocation.withDefaultNamespace(string));
    }

    public ResourceLocation name() {
        return this.name;
    }

    public String toString() {
        return "<parameter " + String.valueOf(this.name) + ">";
    }
}

