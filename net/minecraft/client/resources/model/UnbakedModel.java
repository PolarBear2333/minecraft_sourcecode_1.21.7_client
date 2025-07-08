/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.resources.model;

import javax.annotation.Nullable;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.resources.model.UnbakedGeometry;
import net.minecraft.resources.ResourceLocation;

public interface UnbakedModel {
    public static final String PARTICLE_TEXTURE_REFERENCE = "particle";

    @Nullable
    default public Boolean ambientOcclusion() {
        return null;
    }

    @Nullable
    default public GuiLight guiLight() {
        return null;
    }

    @Nullable
    default public ItemTransforms transforms() {
        return null;
    }

    default public TextureSlots.Data textureSlots() {
        return TextureSlots.Data.EMPTY;
    }

    @Nullable
    default public UnbakedGeometry geometry() {
        return null;
    }

    @Nullable
    default public ResourceLocation parent() {
        return null;
    }

    public static enum GuiLight {
        FRONT("front"),
        SIDE("side");

        private final String name;

        private GuiLight(String string2) {
            this.name = string2;
        }

        public static GuiLight getByName(String string) {
            for (GuiLight guiLight : GuiLight.values()) {
                if (!guiLight.name.equals(string)) continue;
                return guiLight;
            }
            throw new IllegalArgumentException("Invalid gui light: " + string);
        }

        public boolean lightLikeBlock() {
            return this == SIDE;
        }
    }
}

