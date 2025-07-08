/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.system.MemoryStack
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import net.minecraft.client.DeltaTracker;
import org.lwjgl.system.MemoryStack;

public class GlobalSettingsUniform
implements AutoCloseable {
    public static final int UBO_SIZE = new Std140SizeCalculator().putVec2().putFloat().putFloat().putInt().get();
    private final GpuBuffer buffer = RenderSystem.getDevice().createBuffer(() -> "Global Settings UBO", 136, UBO_SIZE);

    public void update(int n, int n2, double d, long l, DeltaTracker deltaTracker, int n3) {
        try (MemoryStack memoryStack = MemoryStack.stackPush();){
            ByteBuffer byteBuffer = Std140Builder.onStack(memoryStack, UBO_SIZE).putVec2(n, n2).putFloat((float)d).putFloat(((float)(l % 24000L) + deltaTracker.getGameTimeDeltaPartialTick(false)) / 24000.0f).putInt(n3).get();
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(this.buffer.slice(), byteBuffer);
        }
        RenderSystem.setGlobalSettingsUniform(this.buffer);
    }

    @Override
    public void close() {
        this.buffer.close();
    }
}

