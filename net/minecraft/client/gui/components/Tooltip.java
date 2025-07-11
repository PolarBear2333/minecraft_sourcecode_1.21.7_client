/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.components;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarrationSupplier;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public class Tooltip
implements NarrationSupplier {
    private static final int MAX_WIDTH = 170;
    private final Component message;
    @Nullable
    private List<FormattedCharSequence> cachedTooltip;
    @Nullable
    private Language splitWithLanguage;
    @Nullable
    private final Component narration;

    private Tooltip(Component component, @Nullable Component component2) {
        this.message = component;
        this.narration = component2;
    }

    public static Tooltip create(Component component, @Nullable Component component2) {
        return new Tooltip(component, component2);
    }

    public static Tooltip create(Component component) {
        return new Tooltip(component, component);
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
        if (this.narration != null) {
            narrationElementOutput.add(NarratedElementType.HINT, this.narration);
        }
    }

    public List<FormattedCharSequence> toCharSequence(Minecraft minecraft) {
        Language language = Language.getInstance();
        if (this.cachedTooltip == null || language != this.splitWithLanguage) {
            this.cachedTooltip = Tooltip.splitTooltip(minecraft, this.message);
            this.splitWithLanguage = language;
        }
        return this.cachedTooltip;
    }

    public static List<FormattedCharSequence> splitTooltip(Minecraft minecraft, Component component) {
        return minecraft.font.split(component, 170);
    }
}

