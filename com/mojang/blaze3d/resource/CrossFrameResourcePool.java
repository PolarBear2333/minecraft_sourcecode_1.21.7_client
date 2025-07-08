/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 */
package com.mojang.blaze3d.resource;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.resource.ResourceDescriptor;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;

public class CrossFrameResourcePool
implements GraphicsResourceAllocator,
AutoCloseable {
    private final int framesToKeepResource;
    private final Deque<ResourceEntry<?>> pool = new ArrayDeque();

    public CrossFrameResourcePool(int n) {
        this.framesToKeepResource = n;
    }

    public void endFrame() {
        Iterator<ResourceEntry<?>> iterator = this.pool.iterator();
        while (iterator.hasNext()) {
            ResourceEntry<?> resourceEntry = iterator.next();
            if (resourceEntry.framesToLive-- != 0) continue;
            resourceEntry.close();
            iterator.remove();
        }
    }

    @Override
    public <T> T acquire(ResourceDescriptor<T> resourceDescriptor) {
        T t = this.acquireWithoutPreparing(resourceDescriptor);
        resourceDescriptor.prepare(t);
        return t;
    }

    private <T> T acquireWithoutPreparing(ResourceDescriptor<T> resourceDescriptor) {
        Iterator<ResourceEntry<?>> iterator = this.pool.iterator();
        while (iterator.hasNext()) {
            ResourceEntry<?> resourceEntry = iterator.next();
            if (!resourceDescriptor.canUsePhysicalResource(resourceEntry.descriptor)) continue;
            iterator.remove();
            return resourceEntry.value;
        }
        return resourceDescriptor.allocate();
    }

    @Override
    public <T> void release(ResourceDescriptor<T> resourceDescriptor, T t) {
        this.pool.addFirst(new ResourceEntry<T>(resourceDescriptor, t, this.framesToKeepResource));
    }

    public void clear() {
        this.pool.forEach(ResourceEntry::close);
        this.pool.clear();
    }

    @Override
    public void close() {
        this.clear();
    }

    @VisibleForTesting
    protected Collection<ResourceEntry<?>> entries() {
        return this.pool;
    }

    @VisibleForTesting
    protected static final class ResourceEntry<T>
    implements AutoCloseable {
        final ResourceDescriptor<T> descriptor;
        final T value;
        int framesToLive;

        ResourceEntry(ResourceDescriptor<T> resourceDescriptor, T t, int n) {
            this.descriptor = resourceDescriptor;
            this.value = t;
            this.framesToLive = n;
        }

        @Override
        public void close() {
            this.descriptor.free(this.value);
        }
    }
}

