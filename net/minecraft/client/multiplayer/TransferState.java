/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.multiplayer;

import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public record TransferState(Map<ResourceLocation, byte[]> cookies) {
}

