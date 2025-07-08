/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.openal.AL10
 */
package com.mojang.blaze3d.audio;

import com.mojang.blaze3d.audio.ListenerTransform;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.openal.AL10;

public class Listener {
    private float gain = 1.0f;
    private ListenerTransform transform = ListenerTransform.INITIAL;

    public void setTransform(ListenerTransform listenerTransform) {
        this.transform = listenerTransform;
        Vec3 vec3 = listenerTransform.position();
        Vec3 vec32 = listenerTransform.forward();
        Vec3 vec33 = listenerTransform.up();
        AL10.alListener3f((int)4100, (float)((float)vec3.x), (float)((float)vec3.y), (float)((float)vec3.z));
        AL10.alListenerfv((int)4111, (float[])new float[]{(float)vec32.x, (float)vec32.y, (float)vec32.z, (float)vec33.x(), (float)vec33.y(), (float)vec33.z()});
    }

    public void setGain(float f) {
        AL10.alListenerf((int)4106, (float)f);
        this.gain = f;
    }

    public float getGain() {
        return this.gain;
    }

    public void reset() {
        this.setTransform(ListenerTransform.INITIAL);
    }

    public ListenerTransform getTransform() {
        return this.transform;
    }
}

