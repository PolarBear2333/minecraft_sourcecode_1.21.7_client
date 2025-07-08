/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.item.properties.numeric;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.CompassAngleState;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class CompassAngle
implements RangeSelectItemModelProperty {
    public static final MapCodec<CompassAngle> MAP_CODEC = CompassAngleState.MAP_CODEC.xmap(CompassAngle::new, compassAngle -> compassAngle.state);
    private final CompassAngleState state;

    public CompassAngle(boolean bl, CompassAngleState.CompassTarget compassTarget) {
        this(new CompassAngleState(bl, compassTarget));
    }

    private CompassAngle(CompassAngleState compassAngleState) {
        this.state = compassAngleState;
    }

    @Override
    public float get(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int n) {
        return this.state.get(itemStack, clientLevel, livingEntity, n);
    }

    public MapCodec<CompassAngle> type() {
        return MAP_CODEC;
    }
}

