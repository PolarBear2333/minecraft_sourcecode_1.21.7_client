/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.packs;

import net.minecraft.server.packs.repository.Pack;

public record PackSelectionConfig(boolean required, Pack.Position defaultPosition, boolean fixedPosition) {
}

