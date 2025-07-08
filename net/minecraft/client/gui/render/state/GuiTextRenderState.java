/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  org.joml.Matrix3x2f
 */
package net.minecraft.client.gui.render.state;

import javax.annotation.Nullable;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.ScreenArea;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix3x2f;

public final class GuiTextRenderState
implements ScreenArea {
    public final Font font;
    public final FormattedCharSequence text;
    public final Matrix3x2f pose;
    public final int x;
    public final int y;
    public final int color;
    public final int backgroundColor;
    public final boolean dropShadow;
    @Nullable
    public final ScreenRectangle scissor;
    @Nullable
    private Font.PreparedText preparedText;
    @Nullable
    private ScreenRectangle bounds;

    public GuiTextRenderState(Font font, FormattedCharSequence formattedCharSequence, Matrix3x2f matrix3x2f, int n, int n2, int n3, int n4, boolean bl, @Nullable ScreenRectangle screenRectangle) {
        this.font = font;
        this.text = formattedCharSequence;
        this.pose = matrix3x2f;
        this.x = n;
        this.y = n2;
        this.color = n3;
        this.backgroundColor = n4;
        this.dropShadow = bl;
        this.scissor = screenRectangle;
    }

    public Font.PreparedText ensurePrepared() {
        if (this.preparedText == null) {
            this.preparedText = this.font.prepareText(this.text, (float)this.x, (float)this.y, this.color, this.dropShadow, this.backgroundColor);
            ScreenRectangle screenRectangle = this.preparedText.bounds();
            if (screenRectangle != null) {
                screenRectangle = screenRectangle.transformMaxBounds(this.pose);
                this.bounds = this.scissor != null ? this.scissor.intersection(screenRectangle) : screenRectangle;
            }
        }
        return this.preparedText;
    }

    @Override
    @Nullable
    public ScreenRectangle bounds() {
        this.ensurePrepared();
        return this.bounds;
    }
}

