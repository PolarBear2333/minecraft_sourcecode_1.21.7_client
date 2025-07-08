/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.block.entity;

import java.util.List;

public interface BeaconBeamOwner {
    public List<Section> getBeamSections();

    public static class Section {
        private final int color;
        private int height;

        public Section(int n) {
            this.color = n;
            this.height = 1;
        }

        public void increaseHeight() {
            ++this.height;
        }

        public int getColor() {
            return this.color;
        }

        public int getHeight() {
            return this.height;
        }
    }
}

