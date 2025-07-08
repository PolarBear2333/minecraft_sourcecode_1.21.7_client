/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  javax.annotation.Nullable
 */
package net.minecraft.util.parsing.packrat;

import com.google.common.annotations.VisibleForTesting;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.util.parsing.packrat.Atom;

public final class Scope {
    private static final int NOT_FOUND = -1;
    private static final Object FRAME_START_MARKER = new Object(){

        public String toString() {
            return "frame";
        }
    };
    private static final int ENTRY_STRIDE = 2;
    private Object[] stack = new Object[128];
    private int topEntryKeyIndex = 0;
    private int topMarkerKeyIndex = 0;

    public Scope() {
        this.stack[0] = FRAME_START_MARKER;
        this.stack[1] = null;
    }

    private int valueIndex(Atom<?> atom) {
        for (int i = this.topEntryKeyIndex; i > this.topMarkerKeyIndex; i -= 2) {
            Object object = this.stack[i];
            assert (object instanceof Atom);
            if (object != atom) continue;
            return i + 1;
        }
        return -1;
    }

    public int valueIndexForAny(Atom<?> ... atomArray) {
        for (int i = this.topEntryKeyIndex; i > this.topMarkerKeyIndex; i -= 2) {
            Object object = this.stack[i];
            assert (object instanceof Atom);
            for (Atom<?> atom : atomArray) {
                if (atom != object) continue;
                return i + 1;
            }
        }
        return -1;
    }

    private void ensureCapacity(int n) {
        int n2 = this.topEntryKeyIndex + 1;
        int n3 = n2 + n * 2;
        int n4 = this.stack.length;
        if (n3 >= n4) {
            int n5 = Util.growByHalf(n4, n3 + 1);
            Object[] objectArray = new Object[n5];
            System.arraycopy(this.stack, 0, objectArray, 0, n4);
            this.stack = objectArray;
        }
        assert (this.validateStructure());
    }

    private void setupNewFrame() {
        this.topEntryKeyIndex += 2;
        this.stack[this.topEntryKeyIndex] = FRAME_START_MARKER;
        this.stack[this.topEntryKeyIndex + 1] = this.topMarkerKeyIndex;
        this.topMarkerKeyIndex = this.topEntryKeyIndex;
    }

    public void pushFrame() {
        this.ensureCapacity(1);
        this.setupNewFrame();
        assert (this.validateStructure());
    }

    private int getPreviousMarkerIndex(int n) {
        return (Integer)this.stack[n + 1];
    }

    public void popFrame() {
        assert (this.topMarkerKeyIndex != 0);
        this.topEntryKeyIndex = this.topMarkerKeyIndex - 2;
        this.topMarkerKeyIndex = this.getPreviousMarkerIndex(this.topMarkerKeyIndex);
        assert (this.validateStructure());
    }

    public void splitFrame() {
        int n = this.topMarkerKeyIndex;
        int n2 = (this.topEntryKeyIndex - this.topMarkerKeyIndex) / 2;
        this.ensureCapacity(n2 + 1);
        this.setupNewFrame();
        int n3 = n + 2;
        int n4 = this.topEntryKeyIndex;
        for (int i = 0; i < n2; ++i) {
            n4 += 2;
            Object object = this.stack[n3];
            assert (object != null);
            this.stack[n4] = object;
            this.stack[n4 + 1] = null;
            n3 += 2;
        }
        this.topEntryKeyIndex = n4;
        assert (this.validateStructure());
    }

    public void clearFrameValues() {
        for (int i = this.topEntryKeyIndex; i > this.topMarkerKeyIndex; i -= 2) {
            assert (this.stack[i] instanceof Atom);
            this.stack[i + 1] = null;
        }
        assert (this.validateStructure());
    }

    public void mergeFrame() {
        int n;
        int n2 = n = this.getPreviousMarkerIndex(this.topMarkerKeyIndex);
        int n3 = this.topMarkerKeyIndex;
        while (n3 < this.topEntryKeyIndex) {
            n2 += 2;
            Object object = this.stack[n3 += 2];
            assert (object instanceof Atom);
            Object object2 = this.stack[n3 + 1];
            Object object3 = this.stack[n2];
            if (object3 != object) {
                this.stack[n2] = object;
                this.stack[n2 + 1] = object2;
                continue;
            }
            if (object2 == null) continue;
            this.stack[n2 + 1] = object2;
        }
        this.topEntryKeyIndex = n2;
        this.topMarkerKeyIndex = n;
        assert (this.validateStructure());
    }

    public <T> void put(Atom<T> atom, @Nullable T t) {
        int n = this.valueIndex(atom);
        if (n != -1) {
            this.stack[n] = t;
        } else {
            this.ensureCapacity(1);
            this.topEntryKeyIndex += 2;
            this.stack[this.topEntryKeyIndex] = atom;
            this.stack[this.topEntryKeyIndex + 1] = t;
        }
        assert (this.validateStructure());
    }

    @Nullable
    public <T> T get(Atom<T> atom) {
        int n = this.valueIndex(atom);
        return (T)(n != -1 ? this.stack[n] : null);
    }

    public <T> T getOrThrow(Atom<T> atom) {
        int n = this.valueIndex(atom);
        if (n == -1) {
            throw new IllegalArgumentException("No value for atom " + String.valueOf(atom));
        }
        return (T)this.stack[n];
    }

    public <T> T getOrDefault(Atom<T> atom, T t) {
        int n = this.valueIndex(atom);
        return (T)(n != -1 ? this.stack[n] : t);
    }

    @Nullable
    @SafeVarargs
    public final <T> T getAny(Atom<? extends T> ... atomArray) {
        int n = this.valueIndexForAny(atomArray);
        return (T)(n != -1 ? this.stack[n] : null);
    }

    @SafeVarargs
    public final <T> T getAnyOrThrow(Atom<? extends T> ... atomArray) {
        int n = this.valueIndexForAny(atomArray);
        if (n == -1) {
            throw new IllegalArgumentException("No value for atoms " + Arrays.toString(atomArray));
        }
        return (T)this.stack[n];
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        boolean bl = true;
        for (int i = 0; i <= this.topEntryKeyIndex; i += 2) {
            Object object = this.stack[i];
            Object object2 = this.stack[i + 1];
            if (object == FRAME_START_MARKER) {
                stringBuilder.append('|');
                bl = true;
                continue;
            }
            if (!bl) {
                stringBuilder.append(',');
            }
            bl = false;
            stringBuilder.append(object).append(':').append(object2);
        }
        return stringBuilder.toString();
    }

    @VisibleForTesting
    public Map<Atom<?>, ?> lastFrame() {
        HashMap<Atom, Object> hashMap = new HashMap<Atom, Object>();
        for (int i = this.topEntryKeyIndex; i > this.topMarkerKeyIndex; i -= 2) {
            Object object = this.stack[i];
            Object object2 = this.stack[i + 1];
            hashMap.put((Atom)object, object2);
        }
        return hashMap;
    }

    public boolean hasOnlySingleFrame() {
        for (int i = this.topEntryKeyIndex; i > 0; --i) {
            if (this.stack[i] != FRAME_START_MARKER) continue;
            return false;
        }
        if (this.stack[0] != FRAME_START_MARKER) {
            throw new IllegalStateException("Corrupted stack");
        }
        return true;
    }

    private boolean validateStructure() {
        Object object;
        int n;
        assert (this.topMarkerKeyIndex >= 0);
        assert (this.topEntryKeyIndex >= this.topMarkerKeyIndex);
        for (n = 0; n <= this.topEntryKeyIndex; n += 2) {
            object = this.stack[n];
            if (object == FRAME_START_MARKER || object instanceof Atom) continue;
            return false;
        }
        n = this.topMarkerKeyIndex;
        while (n != 0) {
            object = this.stack[n];
            if (object != FRAME_START_MARKER) {
                return false;
            }
            n = this.getPreviousMarkerIndex(n);
        }
        return true;
    }
}

