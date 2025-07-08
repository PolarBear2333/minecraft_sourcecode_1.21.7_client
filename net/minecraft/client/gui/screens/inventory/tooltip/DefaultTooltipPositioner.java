/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector2i
 *  org.joml.Vector2ic
 */
package net.minecraft.client.gui.screens.inventory.tooltip;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import org.joml.Vector2i;
import org.joml.Vector2ic;

public class DefaultTooltipPositioner
implements ClientTooltipPositioner {
    public static final ClientTooltipPositioner INSTANCE = new DefaultTooltipPositioner();

    private DefaultTooltipPositioner() {
    }

    @Override
    public Vector2ic positionTooltip(int n, int n2, int n3, int n4, int n5, int n6) {
        Vector2i vector2i = new Vector2i(n3, n4).add(12, -12);
        this.positionTooltip(n, n2, vector2i, n5, n6);
        return vector2i;
    }

    private void positionTooltip(int n, int n2, Vector2i vector2i, int n3, int n4) {
        int n5;
        if (vector2i.x + n3 > n) {
            vector2i.x = Math.max(vector2i.x - 24 - n3, 4);
        }
        if (vector2i.y + (n5 = n4 + 3) > n2) {
            vector2i.y = n2 - n5;
        }
    }
}

