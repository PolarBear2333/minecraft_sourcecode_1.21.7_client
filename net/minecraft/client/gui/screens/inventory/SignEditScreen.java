/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  org.joml.Vector3f
 */
package net.minecraft.client.gui.screens.inventory;

import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.joml.Vector3f;

public class SignEditScreen
extends AbstractSignEditScreen {
    public static final float MAGIC_SCALE_NUMBER = 62.500004f;
    public static final float MAGIC_TEXT_SCALE = 0.9765628f;
    private static final Vector3f TEXT_SCALE = new Vector3f(0.9765628f, 0.9765628f, 0.9765628f);
    @Nullable
    private Model signModel;

    public SignEditScreen(SignBlockEntity signBlockEntity, boolean bl, boolean bl2) {
        super(signBlockEntity, bl, bl2);
    }

    @Override
    protected void init() {
        super.init();
        boolean bl = this.sign.getBlockState().getBlock() instanceof StandingSignBlock;
        this.signModel = SignRenderer.createSignModel(this.minecraft.getEntityModels(), this.woodType, bl);
    }

    @Override
    protected float getSignYOffset() {
        return 90.0f;
    }

    @Override
    protected void renderSignBackground(GuiGraphics guiGraphics) {
        if (this.signModel == null) {
            return;
        }
        int n = this.width / 2;
        int n2 = n - 48;
        int n3 = 66;
        int n4 = n + 48;
        int n5 = 168;
        guiGraphics.submitSignRenderState(this.signModel, 62.500004f, this.woodType, n2, 66, n4, 168);
    }

    @Override
    protected Vector3f getSignTextScale() {
        return TEXT_SCALE;
    }
}

