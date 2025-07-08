/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens.inventory.tooltip;

import java.lang.runtime.SwitchBootstraps;
import java.util.Objects;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientActivePlayersTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientBundleTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public interface ClientTooltipComponent {
    public static ClientTooltipComponent create(FormattedCharSequence formattedCharSequence) {
        return new ClientTextTooltip(formattedCharSequence);
    }

    public static ClientTooltipComponent create(TooltipComponent tooltipComponent) {
        TooltipComponent tooltipComponent2 = tooltipComponent;
        Objects.requireNonNull(tooltipComponent2);
        TooltipComponent tooltipComponent3 = tooltipComponent2;
        int n = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{BundleTooltip.class, ClientActivePlayersTooltip.ActivePlayersTooltip.class}, (Object)tooltipComponent3, n)) {
            case 0 -> {
                BundleTooltip var3_3 = (BundleTooltip)tooltipComponent3;
                yield new ClientBundleTooltip(var3_3.contents());
            }
            case 1 -> {
                ClientActivePlayersTooltip.ActivePlayersTooltip var4_4 = (ClientActivePlayersTooltip.ActivePlayersTooltip)tooltipComponent3;
                yield new ClientActivePlayersTooltip(var4_4);
            }
            default -> throw new IllegalArgumentException("Unknown TooltipComponent");
        };
    }

    public int getHeight(Font var1);

    public int getWidth(Font var1);

    default public boolean showTooltipWithItemInHand() {
        return false;
    }

    default public void renderText(GuiGraphics guiGraphics, Font font, int n, int n2) {
    }

    default public void renderImage(Font font, int n, int n2, int n3, int n4, GuiGraphics guiGraphics) {
    }
}

