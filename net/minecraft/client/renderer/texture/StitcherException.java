/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.texture;

import java.util.Collection;
import java.util.Locale;
import net.minecraft.client.renderer.texture.Stitcher;

public class StitcherException
extends RuntimeException {
    private final Collection<Stitcher.Entry> allSprites;

    public StitcherException(Stitcher.Entry entry, Collection<Stitcher.Entry> collection) {
        super(String.format(Locale.ROOT, "Unable to fit: %s - size: %dx%d - Maybe try a lower resolution resourcepack?", entry.name(), entry.width(), entry.height()));
        this.allSprites = collection;
    }

    public Collection<Stitcher.Entry> getAllSprites() {
        return this.allSprites;
    }
}

