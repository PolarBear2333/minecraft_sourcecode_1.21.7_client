/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.commands;

import java.util.function.Predicate;

public interface PermissionCheck<T>
extends Predicate<T> {
    public int requiredLevel();
}

