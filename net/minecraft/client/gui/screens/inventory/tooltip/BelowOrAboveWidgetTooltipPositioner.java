/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector2i
 *  org.joml.Vector2ic
 */
package net.minecraft.client.gui.screens.inventory.tooltip;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import org.joml.Vector2i;
import org.joml.Vector2ic;

public class BelowOrAboveWidgetTooltipPositioner
implements ClientTooltipPositioner {
    private final ScreenRectangle screenRectangle;

    public BelowOrAboveWidgetTooltipPositioner(ScreenRectangle screenRectangle) {
        this.screenRectangle = screenRectangle;
    }

    @Override
    public Vector2ic positionTooltip(int n, int n2, int n3, int n4, int n5, int n6) {
        Vector2i vector2i = new Vector2i();
        vector2i.x = this.screenRectangle.left() + 3;
        vector2i.y = this.screenRectangle.bottom() + 3 + 1;
        if (vector2i.y + n6 + 3 > n2) {
            vector2i.y = this.screenRectangle.top() - n6 - 3 - 1;
        }
        if (vector2i.x + n5 > n) {
            vector2i.x = Math.max(this.screenRectangle.right() - n5 - 3, 4);
        }
        return vector2i;
    }
}

