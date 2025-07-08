/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.texture;

import com.google.common.collect.ImmutableList;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.StitcherException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class Stitcher<T extends Entry> {
    private static final Comparator<Holder<?>> HOLDER_COMPARATOR = Comparator.comparing(holder -> -holder.height).thenComparing(holder -> -holder.width).thenComparing(holder -> holder.entry.name());
    private final int mipLevel;
    private final List<Holder<T>> texturesToBeStitched = new ArrayList<Holder<T>>();
    private final List<Region<T>> storage = new ArrayList<Region<T>>();
    private int storageX;
    private int storageY;
    private final int maxWidth;
    private final int maxHeight;

    public Stitcher(int n, int n2, int n3) {
        this.mipLevel = n3;
        this.maxWidth = n;
        this.maxHeight = n2;
    }

    public int getWidth() {
        return this.storageX;
    }

    public int getHeight() {
        return this.storageY;
    }

    public void registerSprite(T t) {
        Holder<T> holder = new Holder<T>(t, this.mipLevel);
        this.texturesToBeStitched.add(holder);
    }

    public void stitch() {
        ArrayList<Holder<T>> arrayList = new ArrayList<Holder<T>>(this.texturesToBeStitched);
        arrayList.sort(HOLDER_COMPARATOR);
        for (Holder holder2 : arrayList) {
            if (this.addToStorage(holder2)) continue;
            throw new StitcherException((Entry)holder2.entry, (Collection)arrayList.stream().map(holder -> holder.entry).collect(ImmutableList.toImmutableList()));
        }
    }

    public void gatherSprites(SpriteLoader<T> spriteLoader) {
        for (Region<T> region : this.storage) {
            region.walk(spriteLoader);
        }
    }

    static int smallestFittingMinTexel(int n, int n2) {
        return (n >> n2) + ((n & (1 << n2) - 1) == 0 ? 0 : 1) << n2;
    }

    private boolean addToStorage(Holder<T> holder) {
        for (Region<T> region : this.storage) {
            if (!region.add(holder)) continue;
            return true;
        }
        return this.expand(holder);
    }

    private boolean expand(Holder<T> holder) {
        Region<T> region;
        boolean bl;
        boolean bl2;
        boolean bl3;
        int n = Mth.smallestEncompassingPowerOfTwo(this.storageX);
        int n2 = Mth.smallestEncompassingPowerOfTwo(this.storageY);
        int n3 = Mth.smallestEncompassingPowerOfTwo(this.storageX + holder.width);
        int n4 = Mth.smallestEncompassingPowerOfTwo(this.storageY + holder.height);
        boolean bl4 = n3 <= this.maxWidth;
        boolean bl5 = bl3 = n4 <= this.maxHeight;
        if (!bl4 && !bl3) {
            return false;
        }
        boolean bl6 = bl4 && n != n3;
        boolean bl7 = bl2 = bl3 && n2 != n4;
        if (bl6 ^ bl2) {
            bl = bl6;
        } else {
            boolean bl8 = bl = bl4 && n <= n2;
        }
        if (bl) {
            if (this.storageY == 0) {
                this.storageY = n4;
            }
            region = new Region(this.storageX, 0, n3 - this.storageX, this.storageY);
            this.storageX = n3;
        } else {
            region = new Region<T>(0, this.storageY, this.storageX, n4 - this.storageY);
            this.storageY = n4;
        }
        region.add(holder);
        this.storage.add(region);
        return true;
    }

    static final class Holder<T extends Entry>
    extends Record {
        final T entry;
        final int width;
        final int height;

        public Holder(T t, int n) {
            this(t, Stitcher.smallestFittingMinTexel(t.width(), n), Stitcher.smallestFittingMinTexel(t.height(), n));
        }

        private Holder(T t, int n, int n2) {
            this.entry = t;
            this.width = n;
            this.height = n2;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Holder.class, "entry;width;height", "entry", "width", "height"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Holder.class, "entry;width;height", "entry", "width", "height"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Holder.class, "entry;width;height", "entry", "width", "height"}, this, object);
        }

        public T entry() {
            return this.entry;
        }

        public int width() {
            return this.width;
        }

        public int height() {
            return this.height;
        }
    }

    public static interface Entry {
        public int width();

        public int height();

        public ResourceLocation name();
    }

    public static class Region<T extends Entry> {
        private final int originX;
        private final int originY;
        private final int width;
        private final int height;
        @Nullable
        private List<Region<T>> subSlots;
        @Nullable
        private Holder<T> holder;

        public Region(int n, int n2, int n3, int n4) {
            this.originX = n;
            this.originY = n2;
            this.width = n3;
            this.height = n4;
        }

        public int getX() {
            return this.originX;
        }

        public int getY() {
            return this.originY;
        }

        public boolean add(Holder<T> holder) {
            if (this.holder != null) {
                return false;
            }
            int n = holder.width;
            int n2 = holder.height;
            if (n > this.width || n2 > this.height) {
                return false;
            }
            if (n == this.width && n2 == this.height) {
                this.holder = holder;
                return true;
            }
            if (this.subSlots == null) {
                this.subSlots = new ArrayList<Region<T>>(1);
                this.subSlots.add(new Region<T>(this.originX, this.originY, n, n2));
                int n3 = this.width - n;
                int n4 = this.height - n2;
                if (n4 > 0 && n3 > 0) {
                    int n5;
                    int n6 = Math.max(this.height, n3);
                    if (n6 >= (n5 = Math.max(this.width, n4))) {
                        this.subSlots.add(new Region<T>(this.originX, this.originY + n2, n, n4));
                        this.subSlots.add(new Region<T>(this.originX + n, this.originY, n3, this.height));
                    } else {
                        this.subSlots.add(new Region<T>(this.originX + n, this.originY, n3, n2));
                        this.subSlots.add(new Region<T>(this.originX, this.originY + n2, this.width, n4));
                    }
                } else if (n3 == 0) {
                    this.subSlots.add(new Region<T>(this.originX, this.originY + n2, n, n4));
                } else if (n4 == 0) {
                    this.subSlots.add(new Region<T>(this.originX + n, this.originY, n3, n2));
                }
            }
            for (Region<T> region : this.subSlots) {
                if (!region.add(holder)) continue;
                return true;
            }
            return false;
        }

        public void walk(SpriteLoader<T> spriteLoader) {
            if (this.holder != null) {
                spriteLoader.load(this.holder.entry, this.getX(), this.getY());
            } else if (this.subSlots != null) {
                for (Region region : this.subSlots) {
                    region.walk(spriteLoader);
                }
            }
        }

        public String toString() {
            return "Slot{originX=" + this.originX + ", originY=" + this.originY + ", width=" + this.width + ", height=" + this.height + ", texture=" + String.valueOf(this.holder) + ", subSlots=" + String.valueOf(this.subSlots) + "}";
        }
    }

    public static interface SpriteLoader<T extends Entry> {
        public void load(T var1, int var2, int var3);
    }
}

