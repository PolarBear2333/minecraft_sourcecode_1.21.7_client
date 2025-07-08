/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  org.joml.Matrix3x2f
 */
package net.minecraft.client.gui.render.state;

import javax.annotation.Nullable;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.ScreenArea;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix3x2f;

public final class GuiItemRenderState
implements ScreenArea {
    private final String name;
    private final Matrix3x2f pose;
    private final TrackingItemStackRenderState itemStackRenderState;
    private final int x;
    private final int y;
    @Nullable
    private final ScreenRectangle scissorArea;
    @Nullable
    private final ScreenRectangle oversizedItemBounds;
    @Nullable
    private final ScreenRectangle bounds;

    public GuiItemRenderState(String string, Matrix3x2f matrix3x2f, TrackingItemStackRenderState trackingItemStackRenderState, int n, int n2, @Nullable ScreenRectangle screenRectangle) {
        this.name = string;
        this.pose = matrix3x2f;
        this.itemStackRenderState = trackingItemStackRenderState;
        this.x = n;
        this.y = n2;
        this.scissorArea = screenRectangle;
        this.oversizedItemBounds = this.itemStackRenderState().isOversizedInGui() ? this.calculateOversizedItemBounds() : null;
        this.bounds = this.calculateBounds(this.oversizedItemBounds != null ? this.oversizedItemBounds : new ScreenRectangle(this.x, this.y, 16, 16));
    }

    @Nullable
    private ScreenRectangle calculateOversizedItemBounds() {
        AABB aABB = this.itemStackRenderState.getModelBoundingBox();
        int n = Mth.ceil(aABB.getXsize() * 16.0);
        int n2 = Mth.ceil(aABB.getYsize() * 16.0);
        if (n > 16 || n2 > 16) {
            float f = (float)(aABB.minX * 16.0);
            float f2 = (float)(aABB.maxY * 16.0);
            int n3 = Mth.floor(f);
            int n4 = Mth.floor(f2);
            int n5 = this.x + n3 + 8;
            int n6 = this.y - n4 + 8;
            return new ScreenRectangle(n5, n6, n, n2);
        }
        return null;
    }

    @Nullable
    private ScreenRectangle calculateBounds(ScreenRectangle screenRectangle) {
        ScreenRectangle screenRectangle2 = screenRectangle.transformMaxBounds(this.pose);
        return this.scissorArea != null ? this.scissorArea.intersection(screenRectangle2) : screenRectangle2;
    }

    public String name() {
        return this.name;
    }

    public Matrix3x2f pose() {
        return this.pose;
    }

    public TrackingItemStackRenderState itemStackRenderState() {
        return this.itemStackRenderState;
    }

    public int x() {
        return this.x;
    }

    public int y() {
        return this.y;
    }

    @Nullable
    public ScreenRectangle scissorArea() {
        return this.scissorArea;
    }

    @Nullable
    public ScreenRectangle oversizedItemBounds() {
        return this.oversizedItemBounds;
    }

    @Override
    @Nullable
    public ScreenRectangle bounds() {
        return this.bounds;
    }
}

