/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  org.joml.Matrix3x2f
 *  org.joml.Vector2f
 */
package net.minecraft.client.gui.navigation;

import javax.annotation.Nullable;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.util.Mth;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;

public record ScreenRectangle(ScreenPosition position, int width, int height) {
    private static final ScreenRectangle EMPTY = new ScreenRectangle(0, 0, 0, 0);

    public ScreenRectangle(int n, int n2, int n3, int n4) {
        this(new ScreenPosition(n, n2), n3, n4);
    }

    public static ScreenRectangle empty() {
        return EMPTY;
    }

    public static ScreenRectangle of(ScreenAxis screenAxis, int n, int n2, int n3, int n4) {
        return switch (screenAxis) {
            default -> throw new MatchException(null, null);
            case ScreenAxis.HORIZONTAL -> new ScreenRectangle(n, n2, n3, n4);
            case ScreenAxis.VERTICAL -> new ScreenRectangle(n2, n, n4, n3);
        };
    }

    public ScreenRectangle step(ScreenDirection screenDirection) {
        return new ScreenRectangle(this.position.step(screenDirection), this.width, this.height);
    }

    public int getLength(ScreenAxis screenAxis) {
        return switch (screenAxis) {
            default -> throw new MatchException(null, null);
            case ScreenAxis.HORIZONTAL -> this.width;
            case ScreenAxis.VERTICAL -> this.height;
        };
    }

    public int getBoundInDirection(ScreenDirection screenDirection) {
        ScreenAxis screenAxis = screenDirection.getAxis();
        if (screenDirection.isPositive()) {
            return this.position.getCoordinate(screenAxis) + this.getLength(screenAxis) - 1;
        }
        return this.position.getCoordinate(screenAxis);
    }

    public ScreenRectangle getBorder(ScreenDirection screenDirection) {
        int n = this.getBoundInDirection(screenDirection);
        ScreenAxis screenAxis = screenDirection.getAxis().orthogonal();
        int n2 = this.getBoundInDirection(screenAxis.getNegative());
        int n3 = this.getLength(screenAxis);
        return ScreenRectangle.of(screenDirection.getAxis(), n, n2, 1, n3).step(screenDirection);
    }

    public boolean overlaps(ScreenRectangle screenRectangle) {
        return this.overlapsInAxis(screenRectangle, ScreenAxis.HORIZONTAL) && this.overlapsInAxis(screenRectangle, ScreenAxis.VERTICAL);
    }

    public boolean overlapsInAxis(ScreenRectangle screenRectangle, ScreenAxis screenAxis) {
        int n = this.getBoundInDirection(screenAxis.getNegative());
        int n2 = screenRectangle.getBoundInDirection(screenAxis.getNegative());
        int n3 = this.getBoundInDirection(screenAxis.getPositive());
        int n4 = screenRectangle.getBoundInDirection(screenAxis.getPositive());
        return Math.max(n, n2) <= Math.min(n3, n4);
    }

    public int getCenterInAxis(ScreenAxis screenAxis) {
        return (this.getBoundInDirection(screenAxis.getPositive()) + this.getBoundInDirection(screenAxis.getNegative())) / 2;
    }

    @Nullable
    public ScreenRectangle intersection(ScreenRectangle screenRectangle) {
        int n = Math.max(this.left(), screenRectangle.left());
        int n2 = Math.max(this.top(), screenRectangle.top());
        int n3 = Math.min(this.right(), screenRectangle.right());
        int n4 = Math.min(this.bottom(), screenRectangle.bottom());
        if (n >= n3 || n2 >= n4) {
            return null;
        }
        return new ScreenRectangle(n, n2, n3 - n, n4 - n2);
    }

    public boolean intersects(ScreenRectangle screenRectangle) {
        return this.left() < screenRectangle.right() && this.right() > screenRectangle.left() && this.top() < screenRectangle.bottom() && this.bottom() > screenRectangle.top();
    }

    public boolean encompasses(ScreenRectangle screenRectangle) {
        return screenRectangle.left() >= this.left() && screenRectangle.top() >= this.top() && screenRectangle.right() <= this.right() && screenRectangle.bottom() <= this.bottom();
    }

    public int top() {
        return this.position.y();
    }

    public int bottom() {
        return this.position.y() + this.height;
    }

    public int left() {
        return this.position.x();
    }

    public int right() {
        return this.position.x() + this.width;
    }

    public boolean containsPoint(int n, int n2) {
        return n >= this.left() && n < this.right() && n2 >= this.top() && n2 < this.bottom();
    }

    public ScreenRectangle transformAxisAligned(Matrix3x2f matrix3x2f) {
        Vector2f vector2f = matrix3x2f.transformPosition((float)this.left(), (float)this.top(), new Vector2f());
        Vector2f vector2f2 = matrix3x2f.transformPosition((float)this.right(), (float)this.bottom(), new Vector2f());
        return new ScreenRectangle(Mth.floor(vector2f.x), Mth.floor(vector2f.y), Mth.floor(vector2f2.x - vector2f.x), Mth.floor(vector2f2.y - vector2f.y));
    }

    public ScreenRectangle transformMaxBounds(Matrix3x2f matrix3x2f) {
        Vector2f vector2f = matrix3x2f.transformPosition((float)this.left(), (float)this.top(), new Vector2f());
        Vector2f vector2f2 = matrix3x2f.transformPosition((float)this.right(), (float)this.top(), new Vector2f());
        Vector2f vector2f3 = matrix3x2f.transformPosition((float)this.left(), (float)this.bottom(), new Vector2f());
        Vector2f vector2f4 = matrix3x2f.transformPosition((float)this.right(), (float)this.bottom(), new Vector2f());
        float f = Math.min(Math.min(vector2f.x(), vector2f3.x()), Math.min(vector2f2.x(), vector2f4.x()));
        float f2 = Math.max(Math.max(vector2f.x(), vector2f3.x()), Math.max(vector2f2.x(), vector2f4.x()));
        float f3 = Math.min(Math.min(vector2f.y(), vector2f3.y()), Math.min(vector2f2.y(), vector2f4.y()));
        float f4 = Math.max(Math.max(vector2f.y(), vector2f3.y()), Math.max(vector2f2.y(), vector2f4.y()));
        return new ScreenRectangle(Mth.floor(f), Mth.floor(f3), Mth.ceil(f2 - f), Mth.ceil(f4 - f3));
    }
}

