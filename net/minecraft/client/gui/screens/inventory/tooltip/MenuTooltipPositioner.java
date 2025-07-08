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
import net.minecraft.util.Mth;
import org.joml.Vector2i;
import org.joml.Vector2ic;

public class MenuTooltipPositioner
implements ClientTooltipPositioner {
    private static final int MARGIN = 5;
    private static final int MOUSE_OFFSET_X = 12;
    public static final int MAX_OVERLAP_WITH_WIDGET = 3;
    public static final int MAX_DISTANCE_TO_WIDGET = 5;
    private final ScreenRectangle screenRectangle;

    public MenuTooltipPositioner(ScreenRectangle screenRectangle) {
        this.screenRectangle = screenRectangle;
    }

    @Override
    public Vector2ic positionTooltip(int n, int n2, int n3, int n4, int n5, int n6) {
        int n7;
        Vector2i vector2i = new Vector2i(n3 + 12, n4);
        if (vector2i.x + n5 > n - 5) {
            vector2i.x = Math.max(n3 - 12 - n5, 9);
        }
        vector2i.y += 3;
        int n8 = n6 + 3 + 3;
        int n9 = this.screenRectangle.bottom() + 3 + MenuTooltipPositioner.getOffset(0, 0, this.screenRectangle.height());
        vector2i.y = n9 + n8 <= (n7 = n2 - 5) ? (vector2i.y += MenuTooltipPositioner.getOffset(vector2i.y, this.screenRectangle.top(), this.screenRectangle.height())) : (vector2i.y -= n8 + MenuTooltipPositioner.getOffset(vector2i.y, this.screenRectangle.bottom(), this.screenRectangle.height()));
        return vector2i;
    }

    private static int getOffset(int n, int n2, int n3) {
        int n4 = Math.min(Math.abs(n - n2), n3);
        return Math.round(Mth.lerp((float)n4 / (float)n3, n3 - 3, 5.0f));
    }
}

