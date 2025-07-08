/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.entity.state;

import javax.annotation.Nullable;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

public class WolfRenderState
extends LivingEntityRenderState {
    private static final ResourceLocation DEFAULT_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/wolf/wolf.png");
    public boolean isAngry;
    public boolean isSitting;
    public float tailAngle = 0.62831855f;
    public float headRollAngle;
    public float shakeAnim;
    public float wetShade = 1.0f;
    public ResourceLocation texture = DEFAULT_TEXTURE;
    @Nullable
    public DyeColor collarColor;
    public ItemStack bodyArmorItem = ItemStack.EMPTY;

    public float getBodyRollAngle(float f) {
        float f2 = (this.shakeAnim + f) / 1.8f;
        if (f2 < 0.0f) {
            f2 = 0.0f;
        } else if (f2 > 1.0f) {
            f2 = 1.0f;
        }
        return Mth.sin(f2 * (float)Math.PI) * Mth.sin(f2 * (float)Math.PI * 11.0f) * 0.15f * (float)Math.PI;
    }
}

