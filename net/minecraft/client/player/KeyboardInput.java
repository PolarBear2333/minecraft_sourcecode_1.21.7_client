/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.player;

import net.minecraft.client.Options;
import net.minecraft.client.player.ClientInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;

public class KeyboardInput
extends ClientInput {
    private final Options options;

    public KeyboardInput(Options options) {
        this.options = options;
    }

    private static float calculateImpulse(boolean bl, boolean bl2) {
        if (bl == bl2) {
            return 0.0f;
        }
        return bl ? 1.0f : -1.0f;
    }

    @Override
    public void tick() {
        this.keyPresses = new Input(this.options.keyUp.isDown(), this.options.keyDown.isDown(), this.options.keyLeft.isDown(), this.options.keyRight.isDown(), this.options.keyJump.isDown(), this.options.keyShift.isDown(), this.options.keySprint.isDown());
        float f = KeyboardInput.calculateImpulse(this.keyPresses.forward(), this.keyPresses.backward());
        float f2 = KeyboardInput.calculateImpulse(this.keyPresses.left(), this.keyPresses.right());
        this.moveVector = new Vec2(f2, f).normalized();
    }
}

