/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.components;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class CommonButtons {
    public static SpriteIconButton language(int n, Button.OnPress onPress, boolean bl) {
        return SpriteIconButton.builder(Component.translatable("options.language"), onPress, bl).width(n).sprite(ResourceLocation.withDefaultNamespace("icon/language"), 15, 15).build();
    }

    public static SpriteIconButton accessibility(int n, Button.OnPress onPress, boolean bl) {
        MutableComponent mutableComponent = bl ? Component.translatable("options.accessibility") : Component.translatable("accessibility.onboarding.accessibility.button");
        return SpriteIconButton.builder(mutableComponent, onPress, bl).width(n).sprite(ResourceLocation.withDefaultNamespace("icon/accessibility"), 15, 15).build();
    }
}

