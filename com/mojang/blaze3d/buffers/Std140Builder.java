/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix4fc
 *  org.joml.Vector2fc
 *  org.joml.Vector2ic
 *  org.joml.Vector3fc
 *  org.joml.Vector3ic
 *  org.joml.Vector4fc
 *  org.joml.Vector4ic
 *  org.lwjgl.system.MemoryStack
 */
package com.mojang.blaze3d.buffers;

import com.mojang.blaze3d.DontObfuscate;
import java.nio.ByteBuffer;
import net.minecraft.util.Mth;
import org.joml.Matrix4fc;
import org.joml.Vector2fc;
import org.joml.Vector2ic;
import org.joml.Vector3fc;
import org.joml.Vector3ic;
import org.joml.Vector4fc;
import org.joml.Vector4ic;
import org.lwjgl.system.MemoryStack;

@DontObfuscate
public class Std140Builder {
    private final ByteBuffer buffer;
    private final int start;

    private Std140Builder(ByteBuffer byteBuffer) {
        this.buffer = byteBuffer;
        this.start = byteBuffer.position();
    }

    public static Std140Builder intoBuffer(ByteBuffer byteBuffer) {
        return new Std140Builder(byteBuffer);
    }

    public static Std140Builder onStack(MemoryStack memoryStack, int n) {
        return new Std140Builder(memoryStack.malloc(n));
    }

    public ByteBuffer get() {
        return this.buffer.flip();
    }

    public Std140Builder align(int n) {
        int n2 = this.buffer.position();
        this.buffer.position(this.start + Mth.roundToward(n2 - this.start, n));
        return this;
    }

    public Std140Builder putFloat(float f) {
        this.align(4);
        this.buffer.putFloat(f);
        return this;
    }

    public Std140Builder putInt(int n) {
        this.align(4);
        this.buffer.putInt(n);
        return this;
    }

    public Std140Builder putVec2(float f, float f2) {
        this.align(8);
        this.buffer.putFloat(f);
        this.buffer.putFloat(f2);
        return this;
    }

    public Std140Builder putVec2(Vector2fc vector2fc) {
        this.align(8);
        vector2fc.get(this.buffer);
        this.buffer.position(this.buffer.position() + 8);
        return this;
    }

    public Std140Builder putIVec2(int n, int n2) {
        this.align(8);
        this.buffer.putInt(n);
        this.buffer.putInt(n2);
        return this;
    }

    public Std140Builder putIVec2(Vector2ic vector2ic) {
        this.align(8);
        vector2ic.get(this.buffer);
        this.buffer.position(this.buffer.position() + 8);
        return this;
    }

    public Std140Builder putVec3(float f, float f2, float f3) {
        this.align(16);
        this.buffer.putFloat(f);
        this.buffer.putFloat(f2);
        this.buffer.putFloat(f3);
        this.buffer.position(this.buffer.position() + 4);
        return this;
    }

    public Std140Builder putVec3(Vector3fc vector3fc) {
        this.align(16);
        vector3fc.get(this.buffer);
        this.buffer.position(this.buffer.position() + 16);
        return this;
    }

    public Std140Builder putIVec3(int n, int n2, int n3) {
        this.align(16);
        this.buffer.putInt(n);
        this.buffer.putInt(n2);
        this.buffer.putInt(n3);
        this.buffer.position(this.buffer.position() + 4);
        return this;
    }

    public Std140Builder putIVec3(Vector3ic vector3ic) {
        this.align(16);
        vector3ic.get(this.buffer);
        this.buffer.position(this.buffer.position() + 16);
        return this;
    }

    public Std140Builder putVec4(float f, float f2, float f3, float f4) {
        this.align(16);
        this.buffer.putFloat(f);
        this.buffer.putFloat(f2);
        this.buffer.putFloat(f3);
        this.buffer.putFloat(f4);
        return this;
    }

    public Std140Builder putVec4(Vector4fc vector4fc) {
        this.align(16);
        vector4fc.get(this.buffer);
        this.buffer.position(this.buffer.position() + 16);
        return this;
    }

    public Std140Builder putIVec4(int n, int n2, int n3, int n4) {
        this.align(16);
        this.buffer.putInt(n);
        this.buffer.putInt(n2);
        this.buffer.putInt(n3);
        this.buffer.putInt(n4);
        return this;
    }

    public Std140Builder putIVec4(Vector4ic vector4ic) {
        this.align(16);
        vector4ic.get(this.buffer);
        this.buffer.position(this.buffer.position() + 16);
        return this;
    }

    public Std140Builder putMat4f(Matrix4fc matrix4fc) {
        this.align(16);
        matrix4fc.get(this.buffer);
        this.buffer.position(this.buffer.position() + 64);
        return this;
    }
}

