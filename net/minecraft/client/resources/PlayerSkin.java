/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.resources;

import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;

public record PlayerSkin(ResourceLocation texture, @Nullable String textureUrl, @Nullable ResourceLocation capeTexture, @Nullable ResourceLocation elytraTexture, Model model, boolean secure) {

    public static enum Model {
        SLIM("slim"),
        WIDE("default");

        private final String id;

        private Model(String string2) {
            this.id = string2;
        }

        public static Model byName(@Nullable String string) {
            if (string == null) {
                return WIDE;
            }
            return switch (string) {
                case "slim" -> SLIM;
                default -> WIDE;
            };
        }

        public String id() {
            return this.id;
        }
    }
}

