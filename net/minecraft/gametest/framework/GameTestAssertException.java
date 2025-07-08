/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.gametest.framework;

import net.minecraft.gametest.framework.GameTestException;
import net.minecraft.network.chat.Component;

public class GameTestAssertException
extends GameTestException {
    protected final Component message;
    protected final int tick;

    public GameTestAssertException(Component component, int n) {
        super(component.getString());
        this.message = component;
        this.tick = n;
    }

    @Override
    public Component getDescription() {
        return Component.translatable("test.error.tick", this.message, this.tick);
    }

    @Override
    public String getMessage() {
        return this.getDescription().getString();
    }
}

