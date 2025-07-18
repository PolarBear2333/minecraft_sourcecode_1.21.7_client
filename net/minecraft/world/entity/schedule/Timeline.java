/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Lists
 *  it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap
 */
package net.minecraft.world.entity.schedule;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import java.util.Collection;
import java.util.List;
import net.minecraft.world.entity.schedule.Keyframe;

public class Timeline {
    private final List<Keyframe> keyframes = Lists.newArrayList();
    private int previousIndex;

    public ImmutableList<Keyframe> getKeyframes() {
        return ImmutableList.copyOf(this.keyframes);
    }

    public Timeline addKeyframe(int n, float f) {
        this.keyframes.add(new Keyframe(n, f));
        this.sortAndDeduplicateKeyframes();
        return this;
    }

    public Timeline addKeyframes(Collection<Keyframe> collection) {
        this.keyframes.addAll(collection);
        this.sortAndDeduplicateKeyframes();
        return this;
    }

    private void sortAndDeduplicateKeyframes() {
        Int2ObjectAVLTreeMap int2ObjectAVLTreeMap = new Int2ObjectAVLTreeMap();
        this.keyframes.forEach(arg_0 -> Timeline.lambda$sortAndDeduplicateKeyframes$0((Int2ObjectSortedMap)int2ObjectAVLTreeMap, arg_0));
        this.keyframes.clear();
        this.keyframes.addAll((Collection<Keyframe>)int2ObjectAVLTreeMap.values());
        this.previousIndex = 0;
    }

    public float getValueAt(int n) {
        Keyframe keyframe;
        if (this.keyframes.size() <= 0) {
            return 0.0f;
        }
        Keyframe keyframe2 = this.keyframes.get(this.previousIndex);
        Keyframe keyframe3 = this.keyframes.get(this.keyframes.size() - 1);
        boolean bl = n < keyframe2.getTimeStamp();
        int n2 = bl ? 0 : this.previousIndex;
        float f = bl ? keyframe3.getValue() : keyframe2.getValue();
        int n3 = n2;
        while (n3 < this.keyframes.size() && (keyframe = this.keyframes.get(n3)).getTimeStamp() <= n) {
            this.previousIndex = n3++;
            f = keyframe.getValue();
        }
        return f;
    }

    private static /* synthetic */ void lambda$sortAndDeduplicateKeyframes$0(Int2ObjectSortedMap int2ObjectSortedMap, Keyframe keyframe) {
        int2ObjectSortedMap.put(keyframe.getTimeStamp(), (Object)keyframe);
    }
}

