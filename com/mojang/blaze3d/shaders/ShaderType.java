/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package com.mojang.blaze3d.shaders;

import com.mojang.blaze3d.DontObfuscate;
import javax.annotation.Nullable;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;

@DontObfuscate
public enum ShaderType {
    VERTEX("vertex", ".vsh"),
    FRAGMENT("fragment", ".fsh");

    private static final ShaderType[] TYPES;
    private final String name;
    private final String extension;

    private ShaderType(String string2, String string3) {
        this.name = string2;
        this.extension = string3;
    }

    @Nullable
    public static ShaderType byLocation(ResourceLocation resourceLocation) {
        for (ShaderType shaderType : TYPES) {
            if (!resourceLocation.getPath().endsWith(shaderType.extension)) continue;
            return shaderType;
        }
        return null;
    }

    public String getName() {
        return this.name;
    }

    public FileToIdConverter idConverter() {
        return new FileToIdConverter("shaders", this.extension);
    }

    static {
        TYPES = ShaderType.values();
    }
}

